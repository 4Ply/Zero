package com.netply.zero.service.base;

import com.sun.jersey.api.client.WebResource;

public class ServiceInvocation<T> {
    private final WebResource webResource;
    private final Object requestEntity;
    private final HttpMethod method;
    private final Class<T> responseClass;


    public ServiceInvocation(WebResource webResource, Object requestEntity, HttpMethod method, Class<T> responseClass) {
        this.webResource = webResource;
        this.requestEntity = requestEntity;
        this.method = method;
        this.responseClass = responseClass;
    }

    public WebResource getWebResource() {
        return webResource;
    }

    public Object getRequestEntity() {
        return requestEntity;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Class<T> getResponseClass() {
        return responseClass;
    }
}
