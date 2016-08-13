package com.netply.zero.service.base;

import com.google.gson.Gson;
import com.netply.botchan.web.model.BasicResultResponse;
import com.netply.core.running.ProcessRunner;
import com.netply.core.running.queue.QueueManger;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;

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
                System.err.println("Service call failed : HTTP error code : " + response.getStatus());
                element.getServiceCallback().onError(response);
                return;
            }

            String output = response.getEntity(String.class);
            System.out.println(output);

            element.getServiceCallback().onSuccess(output);

            if (element.getResponseClass() != null) {
                Object parsedResponse = new Gson().fromJson(output, element.getResponseClass());
                System.out.println(parsedResponse);
                element.getServiceCallback().onSuccess(parsedResponse);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Service create(String baseURL) {
        return new Service(baseURL);
    }

    public <T> void get(String url, ZeroCredentials credentials, Class<T> responseClass) {
        get(url, credentials, responseClass, new EmptyServiceCallback<>());
    }

    public <T> void get(String url, ZeroCredentials credentials, Class<T> responseClass, ServiceCallback<T> serviceCallback) {
        exec(url, HttpMethod.GET, credentials, responseClass, null, serviceCallback);
    }

    public <T> void post(String url, ZeroCredentials credentials, Class<T> responseClass, Object requestEntity) {
        post(url, credentials, responseClass, requestEntity, new EmptyServiceCallback<>());
    }

    public <T> void post(String url, ZeroCredentials credentials, Class<T> responseClass, Object requestEntity, ServiceCallback<T> serviceCallback) {
        exec(url, HttpMethod.POST, credentials, responseClass, requestEntity, serviceCallback);
    }

    public void put(String url, ZeroCredentials credentials, Object requestEntity) {
        put(url, credentials, requestEntity, new EmptyServiceCallback<>());
    }

    public void put(String url, ZeroCredentials credentials, Object requestEntity, ServiceCallback<Object> serviceCallback) {
        exec(url, HttpMethod.PUT, credentials, null, requestEntity, serviceCallback);
    }

    public <T> void exec(String url, HttpMethod method, ZeroCredentials credentials, Class<T> responseClass, Object requestEntity, ServiceCallback<T> serviceCallback) {
        exec(url, method, credentials, responseClass, requestEntity, serviceCallback, new MultivaluedMapImpl());
    }

    public <T> void exec(String url, HttpMethod method, ZeroCredentials credentials, Class<T> responseClass, Object requestEntity, ServiceCallback<T> serviceCallback, MultivaluedMapImpl params) {
        if (credentials == null) {
            throw new InvalidParameterException("credentials cannot be null");
        } else {
            Client client = getClient();

            WebResource webResource = client.resource(baseURL + url)
                    .queryParam("sessionKey", credentials.getSessionKey())
                    .queryParam("botType", credentials.getBotType());

            webResource.queryParams(params);
            queueManger.add(new ServiceInvocation<>(webResource, requestEntity, method, responseClass, serviceCallback));
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

    public void login(String username, String passwordHash, ServiceCallback<BasicResultResponse> serviceCallback) {
        WebResource webResource = getClient().resource(baseURL + "/login")
                .queryParam("username", username)
                .queryParam("password", passwordHash);

        queueManger.add(new ServiceInvocation<>(webResource, null, HttpMethod.POST, BasicResultResponse.class, serviceCallback));
    }
}
