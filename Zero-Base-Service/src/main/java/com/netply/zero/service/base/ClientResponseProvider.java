package com.netply.zero.service.base;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public interface ClientResponseProvider {
    ClientResponse execute(WebResource webResource, Object requestEntity);
}
