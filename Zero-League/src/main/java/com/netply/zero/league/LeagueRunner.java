package com.netply.zero.league;

import com.google.gson.Gson;
import com.netply.botchan.web.model.LoginAttemptResponse;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class LeagueRunner {
    public static void main(String[] args) {
        Client client = Client.create();

        WebResource webResource = client.resource("https://app2.bot-chan.com:21000/login")
                .queryParam("username", "test_user3").queryParam("password", "$2y$10$PAlJzaGG0pdCJWz6f/W8FOHubkFEld3uwYJeYlHHxx.u7Rxl/4zFS");

        ClientResponse response = webResource.get(ClientResponse.class);

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        String output = response.getEntity(String.class);
        System.out.println(output);

        LoginAttemptResponse loginAttemptResponse = new Gson().fromJson(output, LoginAttemptResponse.class);
        System.out.println(loginAttemptResponse);

//        ProcessRunner.startParserThread(LeagueGameManager.getInstance()::parseCurrentGames, 60000);
    }
}
