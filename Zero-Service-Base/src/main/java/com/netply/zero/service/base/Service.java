package com.netply.zero.service.base;

import com.google.gson.Gson;
import com.netply.botchan.web.model.LoginAttemptResponse;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.security.InvalidParameterException;

public class Service {
    private static Client client;


    public static <T> T request(String url, ZeroCredentials credentials) {
        if (credentials == null) {
            throw new InvalidParameterException("credentials cannot be null");
        } else {
            Client client = getClient();

            WebResource webResource = client.resource("https://app2.bot-chan.com:21000/" + url)
                    .queryParam("botUser", credentials.getBotUser())
                    .queryParam("token", credentials.getBotToken())
                    .queryParam("botType", credentials.getBotType());

            ClientResponse response = webResource.get(ClientResponse.class);

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }

            String output = response.getEntity(String.class);
            System.out.println(output);

            LoginAttemptResponse loginAttemptResponse = new Gson().fromJson(output, LoginAttemptResponse.class);
            System.out.println(loginAttemptResponse);

            return null;
        }
    }

    private static Client getClient() {
        if (client == null) {
            client = Client.create();
        }
        return client;
    }
}
