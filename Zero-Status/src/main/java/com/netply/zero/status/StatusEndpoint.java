package com.netply.zero.status;

public class StatusEndpoint {
    private String description;
    private String address;
    private int port;
    private boolean isTCP;


    public StatusEndpoint(String description, String address, int port, boolean isTCP) {
        this.description = description;
        this.address = address;
        this.port = port;
        this.isTCP = isTCP;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public boolean isTCP() {
        return isTCP;
    }
}
