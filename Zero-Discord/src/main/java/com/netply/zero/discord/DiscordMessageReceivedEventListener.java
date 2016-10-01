package com.netply.zero.discord;

import com.netply.botchan.web.model.Message;
import com.netply.zero.discord.persistence.TrackedUserManager;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.credentials.BasicSessionCredentials;
import sx.blah.discord.api.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordMessageReceivedEventListener implements IListener<MessageReceivedEvent> {
    private String botChanURL;
    private TrackedUserManager trackedUserManager;


    public DiscordMessageReceivedEventListener(String botChanURL, TrackedUserManager trackedUserManager) {
        this.botChanURL = botChanURL;
        this.trackedUserManager = trackedUserManager;
    }

    public void handle(MessageReceivedEvent messageReceivedEvent) {
        String sender = messageReceivedEvent.getMessage().getAuthor().getID();
        String content = messageReceivedEvent.getMessage().getContent();
        Logger.getGlobal().log(Level.INFO, String.format("[Message] %s: %s\n", sender, content));
        if (!messageReceivedEvent.getMessage().getChannel().isPrivate()) {
            sender = messageReceivedEvent.getMessage().getChannel().getID();
        }
        trackedUserManager.addUser(sender);
        Service.create(botChanURL).put("/message", new BasicSessionCredentials(), new Message(null, content, sender));
    }

//    private static final Logger log = Log.getLogger();
//    private ArrayList<NameFilterMessageHandler> messageHandlers = new ArrayList<>();
//
//
//    public DiscordMessageReceivedEventListener() {
//        messageHandlers.add(new HateMessageHandler());
//        messageHandlers.add(new HelloMessageHandler());
//        messageHandlers.add(new GreetingMessageHandler());
//        messageHandlers.add(new DropThatBaseMessageHandler());
//        messageHandlers.add(new GoodWorkMessageHandler());
//        messageHandlers.add(new GoAwayMessageHandler());
//        messageHandlers.add(new NekoChanMessageHandler());
//
//        messageHandlers.addAll(LeagueMessageHandlerProvider.getAllLeagueMessageHandlers());
//        messageHandlers.addAll(MangaMessageHandlerProvider.getAllMangaHandlers());
//        messageHandlers.addAll(SubtleMessageHandlerProvider.getSubtleHandlers());
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public void handle(MessageReceivedEvent event) {
//        String message = event.getMessage().getContent();
//        log.info(String.format("Discord message: [%s] %s", event.getMessage().getAuthor().getName(), message));
//        Database.getInstance().addToQueue(statement -> Database.logIncomingDiscordMessage(statement, event.getMessage()), null);
//        String cleanMessage = message.toLowerCase().replaceAll("[^a-zA-Z\\s-]", "").replaceAll("\\s+", " ");
//
//        messageHandlers.stream().filter(isMessageValid(message, cleanMessage))
//                .filter(whatsAppMessageHandler -> !(whatsAppMessageHandler.isNameRequired() && !event.getMessage().getChannel().isPrivate())
//                        || Self.containsName(message) || Self.containsName(event.getMessage()))
//                .forEach(messageHandler -> {
//                    log.info("Invoking message handler: " + messageHandler.getClass().getName());
//                    messageHandler.parse(new ReceivedDiscordMessage(event.getMessage(), event.getClient(), event.getMessage().getContent()));
//                });
//
//        MessageQueue.getInstance().add(new ReceivedDiscordMessage(event.getMessage(), event.getClient(), message));
//    }
//
//    private Predicate<NameFilterMessageHandler> isMessageValid(String message, String cleanMessage) {
//        return messageHandler -> {
//            if (messageHandler.isCleanMessageRequired()) {
//                return messageHandler.isValid(cleanMessage);
//            } else {
//                return messageHandler.isValid(message.toLowerCase());
//            }
//        };
//    }
}
