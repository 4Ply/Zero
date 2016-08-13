package com.netply.zero.service.base;

import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;

public enum HttpMethod implements ClientResponseProvider {
    GET {
        @Override
        public ClientResponse execute(WebResource webResource, Object requestEntity) {
            return webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        }
    }, POST {
        @Override
        public ClientResponse execute(WebResource webResource, Object requestEntity) {
            return webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, gson.toJson(requestEntity));
        }
    }, PUT {
        @Override
        public ClientResponse execute(WebResource webResource, Object requestEntity) {
            return webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).put(ClientResponse.class, gson.toJson(requestEntity));
        }
    };

    private static Gson gson = new Gson();
}
