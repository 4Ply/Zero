package com.netply.zero.status;

import com.netply.botchan.web.model.FromUserMessage;
import com.netply.zero.service.base.messaging.MessageListener;
import com.netply.zero.service.base.messaging.MessageUtil;
import com.netply.zero.service.base.permissions.PermissionUtil;
import com.netply.zero.service.base.permissions.PermissionsCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class StatusMessageBean {
    private String botChanURL;
    private MessageListener messageListener;
    private Map<String, Consumer<FromUserMessage>> messageMatchers;
    private List<StatusEndpoint> statusEndpoints;
    private String serverStatusPermission = "bot.chan.status.info";


    @Autowired
    public StatusMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL) {
        this.botChanURL = botChanURL;
        messageListener = new MessageListener(botChanURL);
        initStatusEndpoints();
        initMessageMatchers();
    }

    private void initStatusEndpoints() {
        statusEndpoints = new ArrayList<>();
        statusEndpoints.add(new StatusEndpoint("app2.bot-chan.com", "app2.bot-chan.com", 80, true));
        statusEndpoints.add(new StatusEndpoint("TeamSpeak3 Server (ts.slq.co.za)", "ts.slq.co.za", 10011, true));
    }

    private void initMessageMatchers() {
        messageMatchers = new HashMap<>();
        messageMatchers.put(ChatMatchers.STATUS, this::sendCurrentStatuses);
    }

    private void sendCurrentStatuses(FromUserMessage message) {
        PermissionUtil.checkPermission(botChanURL, message, serverStatusPermission, new PermissionsCallback() {
            @Override
            public void permissionGranted(String permission) {
                StatusStringBuilder stringBuilder = new StatusStringBuilder("Server statuses:");

                for (StatusEndpoint statusEndpoint : statusEndpoints) {
                    String address = statusEndpoint.getAddress();
                    int port = statusEndpoint.getPort();
                    boolean status = statusEndpoint.isTCP() ? testTCP(address, port) : testUDP(address, port);
                    stringBuilder.appendStatus(statusEndpoint.getDescription(), status);
                }

                MessageUtil.reply(botChanURL, message, stringBuilder.toString());
            }

            @Override
            public void permissionDenied(String permission) {
                if (message.isDirect()) {
                    MessageUtil.reply(botChanURL, message, "Sorry, you don't have permission to do that");
                }
            }
        });
    }

    private boolean testTCP(String address, int port) {
        try (Socket socket = new Socket(address, port)) {
            return socket.isConnected();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean testUDP(String address, int port) {
        try (DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.connect(InetAddress.getByName(address), port);
            datagramSocket.send(new DatagramPacket(new byte[0], 0));
            return datagramSocket.isConnected() && datagramSocket.isBound();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForMessages() {
        messageListener.checkMessages("/messages", new ArrayList<>(messageMatchers.keySet()), this::parseMessage);
    }

    private void parseMessage(FromUserMessage message) {
        messageMatchers.keySet().stream()
                .filter(message.getMessage()::matches)
                .map(messageMatchers::get)
                .forEach(messageConsumer -> messageConsumer.accept(message));
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 60000)
    public void checkStatuses() {
        StatusStringBuilder stringBuilder = new StatusStringBuilder("Warning - The following servers are not responding:");
        boolean anyEndpointFailed = false;
        for (StatusEndpoint statusEndpoint : statusEndpoints) {
            String address = statusEndpoint.getAddress();
            int port = statusEndpoint.getPort();
            boolean status = statusEndpoint.isTCP() ? testTCP(address, port) : testUDP(address, port);
            if (!status) {
                stringBuilder.appendStatus(statusEndpoint.getDescription(), false);
                anyEndpointFailed = true;
            }
        }

        if (anyEndpointFailed) {
            PermissionUtil.getUsersForPermission(botChanURL, serverStatusPermission, users -> {
                for (Integer user : users) {
                    MessageUtil.sendMessage(botChanURL, user, stringBuilder.toString());
                }
            });
        }
    }
}
