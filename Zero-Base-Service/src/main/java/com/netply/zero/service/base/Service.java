package com.netply.zero.service.base;

import com.google.gson.Gson;
import com.netply.core.running.ProcessRunner;
import com.netply.core.running.queue.QueueManger;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

import java.security.InvalidParameterException;

public class Service {
    private static Client client;
    private static QueueManger<ServiceInvocation<?>> queueManger = new QueueManger<>();
    private String baseURL;


    public Service(String baseURL) {
        this.baseURL = baseURL;
    }

    static {
        new Thread() {
            @Override
            public void run() {
                while (ProcessRunner.run) {
                    consumeQueue();
                }
            }
        }.start();
    }

    private static void consumeQueue() {
        try {
            ServiceInvocation element = queueManger.getNextQueueElement();
            ClientResponse response = element.getMethod().execute(element.getWebResource(), element.getRequestEntity());

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }

            String output = response.getEntity(String.class);
            System.out.println(output);

            if (element.getResponseClass() != null) {
                Object parsedResponse = new Gson().fromJson(output, element.getResponseClass());
                System.out.println(parsedResponse);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Service create(String baseURL) {
        return new Service(baseURL);
    }

    public <T> void get(String url, ZeroCredentials credentials, Class<T> responseClass) {
        exec(url, HttpMethod.GET, credentials, responseClass, null);
    }

    public <T> void post(String url, ZeroCredentials credentials, Class<T> responseClass, Object requestEntity) {
        exec(url, HttpMethod.POST, credentials, responseClass, requestEntity);
    }

    public void put(String url, ZeroCredentials credentials, Object requestEntity) {
        exec(url, HttpMethod.PUT, credentials, null, requestEntity);
    }

    public <T> void exec(String url, HttpMethod method, ZeroCredentials credentials, Class<T> responseClass, Object requestEntity) {
        if (credentials == null) {
            throw new InvalidParameterException("credentials cannot be null");
        } else {
            Client client = getClient();

            WebResource webResource = client.resource(baseURL + url)
                    .queryParam("sessionKey", credentials.getSessionKey())
                    .queryParam("botType", credentials.getBotType());

            queueManger.add(new ServiceInvocation(webResource, requestEntity, method, responseClass));
        }
    }

    private static Client getClient() {
        if (client == null) {
            DefaultClientConfig clientConfig = new DefaultClientConfig();
            clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

            client = Client.create(clientConfig);
        }
        return client;
    }
}
