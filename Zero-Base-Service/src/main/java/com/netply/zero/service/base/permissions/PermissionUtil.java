package com.netply.zero.service.base.permissions;

import com.netply.botchan.web.model.Message;
import com.netply.botchan.web.model.User;
import com.netply.zero.service.base.ListUtil;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ServiceCallback;
import com.netply.zero.service.base.credentials.BasicSessionCredentials;
import com.sun.jersey.api.client.ClientResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PermissionUtil {
    public static void checkPermission(String baseURL, Message message, String permission, PermissionsCallback permissionsCallback) {
        String permissionURL;
        try {
            permissionURL = URLEncoder.encode(permission, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        Service.create(baseURL).post(String.format("/hasPermission?permission=%s", permissionURL), new BasicSessionCredentials(), new User(message.getSender(), message.getPlatform()), Boolean.class, new ServiceCallback<Boolean>() {
            @Override
            public void onError(ClientResponse response) {
                permissionsCallback.permissionDenied(permission);
            }

            @Override
            public void onSuccess(String output) {

            }

            @Override
            public void onSuccess(Boolean parsedResponse) {
                if (parsedResponse != null && parsedResponse) {
                    permissionsCallback.permissionGranted(permission);
                } else {
                    permissionsCallback.permissionDenied(permission);
                }
            }
        });
    }

    public static void getUsersForPermission(String baseURL, String permission, Consumer<List<Integer>> userListConsumer) {
        String permissionURL;
        try {
            permissionURL = URLEncoder.encode(permission, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        Service.create(baseURL).get(String.format("/usersForPermission?permission=%s", permissionURL), new BasicSessionCredentials(), null, new ServiceCallback<Object>() {
            @Override
            public void onError(ClientResponse response) {

            }

            @Override
            public void onSuccess(String output) {
                List<Integer> users = ListUtil.stringToArray(output, Integer[].class).stream().distinct().collect(Collectors.toList());
                userListConsumer.accept(users);
            }

            @Override
            public void onSuccess(Object parsedResponse) {

            }
        });
    }
}
