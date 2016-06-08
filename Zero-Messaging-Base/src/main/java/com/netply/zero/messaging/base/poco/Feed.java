package com.netply.zero.messaging.base.poco;

public class Feed {
    private String name;
    private String url;
    private FeedLocation location;


    public Feed(String name, String url, FeedLocation location) {
        this.name = name;
        this.url = url;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public FeedLocation getLocation() {
        return location;
    }

    public void setLocation(FeedLocation location) {
        this.location = location;
    }
}
