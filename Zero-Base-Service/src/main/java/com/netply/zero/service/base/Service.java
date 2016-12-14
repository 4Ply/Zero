package com.netply.zero.service.base;

import com.google.gson.Gson;
import com.netply.botchan.web.model.BasicResultResponse;
import com.netply.zero.service.base.credentials.ZeroCredentials;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import java.security.InvalidParameterException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Service {
    private static Client client;
    private String baseURL;


    public Service(String baseURL) {
        this.baseURL = baseURL;
    }

    private static void consumeItem(ServiceInvocation element) {
        String url = element.getWebResource().getURI().toString();
        System.out.println(element.getMethod().name() + " | " + url);
        System.out.println("Request Data: " + element.getRequestEntity());

        try {
            ClientResponse response = element.getMethod().execute(element.getWebResource(), element.getRequestEntity());

            if (response.getStatus() == 200) {
                processSuccess(element, response);
            } else {
                processError(element, url, response);
            }
        } catch (Exception e) {
            Logger.getGlobal().severe(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processSuccess(ServiceInvocation element, ClientResponse response) {
        String output = response.getEntity(String.class);
        System.out.println("Basic Response: " + output);

        try {
            element.getServiceCallback().onSuccess(output);
        } catch (Exception e) {
            e.printStackTrace();
        }

        processComplexResult(element, output);
    }

    private static void processComplexResult(ServiceInvocation element, String output) {
        if (element.getResponseClass() != null) {
            Object parsedResponse = new Gson().fromJson(output, element.getResponseClass());
            if (parsedResponse != null) {
                Logger.getGlobal().log(Level.FINER, parsedResponse.toString());
                try {
                    element.getServiceCallback().onSuccess(parsedResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void processError(ServiceInvocation element, String url, ClientResponse response) {
        System.out.println("Request Data: " + element.getRequestEntity());
        Logger.getGlobal().log(Level.SEVERE, "Service call failed : HTTP error code : " + response.getStatus() + " | " + url + " \n" + element.getRequestEntity());
        try {
            element.getServiceCallback().onError(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Service create(String baseURL) {
        return new Service(baseURL);
    }

    public <T> void get(String url, ZeroCredentials credentials, Class<T> responseClass) {
        getConcurrent(url, credentials, responseClass, new EmptyServiceCallback<>());
    }

    public <T> void getConcurrent(String url, ZeroCredentials credentials, Class<T> responseClass, ServiceCallback<T> serviceCallback) {
        get(url, credentials, responseClass, serviceCallback);
    }

    public void delete(String url, ZeroCredentials credentials, ServiceCallback<Object> serviceCallback) {
        exec(url, HttpMethod.DELETE, credentials, null, null, serviceCallback);
    }

    public <T> void get(String url, ZeroCredentials credentials, Class<T> responseClass, ServiceCallback<T> serviceCallback) {
        exec(url, HttpMethod.GET, credentials, responseClass, null, serviceCallback);
    }

    public <T> void get(String url, ZeroCredentials credentials, Class<T> responseClass, ServiceCallback<T> serviceCallback, MultivaluedMapImpl params) {
        exec(url, HttpMethod.GET, credentials, responseClass, null, serviceCallback, params);
    }

    public <T> void post(String url, ZeroCredentials credentials, Class<T> responseClass, Object requestEntity) {
        post(url, credentials, requestEntity, responseClass, new EmptyServiceCallback<>());
    }

    public <T> void post(String url, ZeroCredentials credentials, Object requestEntity, Class<T> responseClass, ServiceCallback<T> serviceCallback) {
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
                    .queryParam("sessionKey", credentials.getSessionKey());

            webResource.queryParams(params);

            ServiceInvocation<T> serviceInvocation = new ServiceInvocation<>(webResource, requestEntity, method, responseClass, serviceCallback);
            consumeItem(serviceInvocation);
        }
    }

    private static Client getClient() {
        if (client == null) {
            DefaultClientConfig clientConfig = new DefaultClientConfig();
            clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
            clientConfig.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, 30000);
            clientConfig.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, 30000);

            client = Client.create(clientConfig);
        }
        return client;
    }

    public void login(String username, String passwordHash, ServiceCallback<BasicResultResponse> serviceCallback) {
        Client client = getClient();
        client.addFilter(new HTTPBasicAuthFilter(username, passwordHash));

        WebResource webResource = client
                .resource(baseURL + "/login")
                .queryParam("username", username)
                .queryParam("password", passwordHash);

        consumeItem(new ServiceInvocation<>(webResource, null, HttpMethod.POST, BasicResultResponse.class, serviceCallback));
    }
}
