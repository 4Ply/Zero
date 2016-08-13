package com.netply.zero.service.base;

import com.google.gson.Gson;
import com.netply.botchan.web.model.BasicResultResponse;
import com.netply.core.running.ProcessRunner;
import com.netply.core.running.queue.QueueManger;
import com.netply.zero.service.base.credentials.ZeroCredentials;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import java.security.InvalidParameterException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            consumeItem(queueManger.getNextQueueElement());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void consumeItem(ServiceInvocation element) {
        System.out.println(element.getMethod().name() + " | " + element.getWebResource().getURI().toString());
        System.out.println("Request Data: " + element.getRequestEntity());

        ClientResponse response = element.getMethod().execute(element.getWebResource(), element.getRequestEntity());

        if (response.getStatus() != 200) {
            Logger.getGlobal().log(Level.SEVERE, "Service call failed : HTTP error code : " + response.getStatus());
            element.getServiceCallback().onError(response);
            return;
        }

        String output = response.getEntity(String.class);
        System.out.println("Basic Response: " + output);

        element.getServiceCallback().onSuccess(output);

        if (element.getResponseClass() != null) {
            Object parsedResponse = new Gson().fromJson(output, element.getResponseClass());
            if (parsedResponse != null) {
                Logger.getGlobal().log(Level.FINER, parsedResponse.toString());
                element.getServiceCallback().onSuccess(parsedResponse);
            }
        }
    }

    public static Service create(String baseURL) {
        return new Service(baseURL);
    }

    public <T> void get(String url, ZeroCredentials credentials, Class<T> responseClass) {
        getConcurrent(url, credentials, responseClass, new EmptyServiceCallback<>());
    }

    public <T> void getConcurrent(String url, ZeroCredentials credentials, Class<T> responseClass, ServiceCallback<T> serviceCallback) {
        get(url, false, credentials, responseClass, serviceCallback);
    }

    public void delete(String url, boolean concurrent, ZeroCredentials credentials, ServiceCallback<Object> serviceCallback) {
        exec(url, HttpMethod.DELETE, concurrent, credentials, null, null, serviceCallback);
    }

    public <T> void get(String url, boolean concurrent, ZeroCredentials credentials, Class<T> responseClass, ServiceCallback<T> serviceCallback) {
        exec(url, HttpMethod.GET, concurrent, credentials, responseClass, null, serviceCallback);
    }

    public <T> void get(String url, ZeroCredentials credentials, Class<T> responseClass, ServiceCallback<T> serviceCallback, MultivaluedMapImpl params) {
        exec(url, HttpMethod.GET, false, credentials, responseClass, null, serviceCallback, params);
    }

    public <T> void post(String url, ZeroCredentials credentials, Class<T> responseClass, Object requestEntity) {
        post(url, credentials, responseClass, requestEntity, new EmptyServiceCallback<>());
    }

    public <T> void post(String url, ZeroCredentials credentials, Class<T> responseClass, Object requestEntity, ServiceCallback<T> serviceCallback) {
        exec(url, HttpMethod.POST, false, credentials, responseClass, requestEntity, serviceCallback);
    }

    public void put(String url, ZeroCredentials credentials, Object requestEntity) {
        put(url, credentials, requestEntity, new EmptyServiceCallback<>());
    }

    public void put(String url, ZeroCredentials credentials, Object requestEntity, ServiceCallback<Object> serviceCallback) {
        exec(url, HttpMethod.PUT, false, credentials, null, requestEntity, serviceCallback);
    }

    public <T> void exec(String url, HttpMethod method, boolean concurrent, ZeroCredentials credentials, Class<T> responseClass, Object requestEntity, ServiceCallback<T> serviceCallback) {
        exec(url, method, concurrent, credentials, responseClass, requestEntity, serviceCallback, new MultivaluedMapImpl());
    }

    public <T> void exec(String url, HttpMethod method, boolean concurrent, ZeroCredentials credentials, Class<T> responseClass, Object requestEntity, ServiceCallback<T> serviceCallback, MultivaluedMapImpl params) {
        if (credentials == null) {
            throw new InvalidParameterException("credentials cannot be null");
        } else {
            Client client = getClient();

            WebResource webResource = client.resource(baseURL + url)
                    .queryParam("sessionKey", credentials.getSessionKey());

            webResource.queryParams(params);

            ServiceInvocation<T> serviceInvocation = new ServiceInvocation<>(webResource, requestEntity, method, responseClass, serviceCallback);
            if (concurrent) {
                queueManger.add(serviceInvocation);
            } else {
                consumeItem(serviceInvocation);
            }
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
