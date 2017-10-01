package com.netply.zero.service.base;

import com.google.gson.Gson;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.log4j.Logger;

public class Service {
    private static Logger logger = Logger.getLogger(Service.class);
    private static Client client;
    private static String authenticationToken;
    private String baseURL;


    public Service(String baseURL) {
        this.baseURL = baseURL;
    }

    private static void consumeItem(ServiceInvocation element) {
        String url = element.getWebResource().getURI().toString();
        logger.info(element.getMethod().name() + " | " + url);
        logger.info("Request Data: " + element.getRequestEntity());

        try {
            ClientResponse response = element.getMethod().execute(element.getWebResource(), element.getRequestEntity());

            if (response.getStatus() == 200) {
                processSuccess(element, response);
            } else {
                processError(element, url, response);
            }
        } catch (Exception e) {
            logger.fatal(e.getMessage());
            e.printStackTrace();
            processError(element, url, null);
        }
    }

    private static void processSuccess(ServiceInvocation element, ClientResponse response) {
        String output = response.getEntity(String.class);
        logger.info("Basic Response: " + output);

        try {
            if (!isResponseClassOfTypeString(element)) {
                element.getServiceCallback().onSuccess(output);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        processComplexResult(element, output);
    }

    private static boolean isResponseClassOfTypeString(ServiceInvocation element) {
        Class responseClass = element.getResponseClass();
        return responseClass != null && responseClass.equals(String.class);
    }

    private static void processComplexResult(ServiceInvocation element, String output) {
        if (element.getResponseClass() != null) {
            Object parsedResponse = new Gson().fromJson(output, element.getResponseClass());
            if (parsedResponse != null) {
                logger.info(parsedResponse.toString());
                try {
                    element.getServiceCallback().onSuccess(parsedResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void processError(ServiceInvocation element, String url, ClientResponse response) {
        logger.info("Request Data: " + element.getRequestEntity());
        Object status = response != null ? response.getStatus() : "<ERROR>";
        logger.fatal(String.format("Service call failed : HTTP error code : %s | %s \n%s", status, url, element.getRequestEntity()));
        try {
            element.getServiceCallback().onError(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Service create(String baseURL) {
        return new Service(baseURL);
    }

    public <T> void get(String url, Class<T> responseClass) {
        getConcurrent(url, responseClass, new EmptyServiceCallback<>());
    }

    public <T> void getConcurrent(String url, Class<T> responseClass, ServiceCallback<T> serviceCallback) {
        get(url, responseClass, serviceCallback);
    }

    public void delete(String url, ServiceCallback<Object> serviceCallback) {
        delete(url, null, serviceCallback);
    }

    public void delete(String url, Object requestEntity, ServiceCallback<Object> serviceCallback) {
        exec(url, HttpMethod.DELETE, null, requestEntity, serviceCallback);
    }

    public <T> void get(String url, Class<T> responseClass, ServiceCallback<T> serviceCallback) {
        exec(url, HttpMethod.GET, responseClass, null, serviceCallback);
    }

    public <T> void get(String url, Class<T> responseClass, ServiceCallback<T> serviceCallback, MultivaluedMapImpl params) {
        exec(url, HttpMethod.GET, responseClass, null, serviceCallback, params);
    }

    public <T> void post(String url, Class<T> responseClass, Object requestEntity) {
        post(url, requestEntity, responseClass, new EmptyServiceCallback<>());
    }

    public <T> void post(String url, Object requestEntity, Class<T> responseClass, ServiceCallback<T> serviceCallback) {
        post(url, requestEntity, responseClass, serviceCallback, new MultivaluedMapImpl());
    }

    public <T> void post(String url, Object requestEntity, Class<T> responseClass, ServiceCallback<T> serviceCallback, MultivaluedMapImpl params) {
        exec(url, HttpMethod.POST, responseClass, requestEntity, serviceCallback, params);
    }

    public void put(String url, Object requestEntity) {
        put(url, requestEntity, new EmptyServiceCallback<>());
    }

    public void put(String url, Object requestEntity, ServiceCallback<Object> serviceCallback) {
        exec(url, HttpMethod.PUT, null, requestEntity, serviceCallback);
    }

    public <T> void exec(String url, HttpMethod method, Class<T> responseClass, Object requestEntity, ServiceCallback<T> serviceCallback) {
        exec(url, method, responseClass, requestEntity, serviceCallback, new MultivaluedMapImpl());
    }

    public <T> void exec(String url, HttpMethod method, Class<T> responseClass, Object requestEntity, ServiceCallback<T> serviceCallback, MultivaluedMapImpl params) {
        Client client = getClient();

        WebResource webResource = client.resource(baseURL + url);

        webResource.queryParams(params);

        ServiceInvocation<T> serviceInvocation = new ServiceInvocation<>(webResource, requestEntity, method, responseClass, serviceCallback);
        consumeItem(serviceInvocation);
    }

    private Client getClient() {
        if (client == null) {
            DefaultClientConfig clientConfig = new DefaultClientConfig();
            clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
            clientConfig.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, 30000);
            clientConfig.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, 30000);

            client = Client.create(clientConfig);
            client.addFilter(new ClientFilter() {
                @Override
                public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
                    if (!clientRequest.getHeaders().containsKey("apikey")) {
                        clientRequest.getHeaders().add("apikey", authenticationToken);
                    }

                    return this.getNext().handle(clientRequest);
                }
            });
        }
        return client;
    }

    public void setAuthenticateToken(String authenticationToken) {
        this.authenticationToken = authenticationToken;
    }
}
