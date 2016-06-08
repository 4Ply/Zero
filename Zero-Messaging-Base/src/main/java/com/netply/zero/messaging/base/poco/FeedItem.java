package com.netply.zero.messaging.base.poco;

public class FeedItem {
    protected final String name;
    protected final String url;
    protected final String timeStamp;
    protected String mangaName;


    public FeedItem(String mangaName, String name, String url, String timeStamp) {
        this.mangaName = mangaName;
        this.name = name;
        this.url = url;
        this.timeStamp = timeStamp;
    }

    public String getMangaName() {
        return mangaName;
    }

    public void setMangaName(String mangaName) {
        this.mangaName = mangaName;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "FeedItem{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                '}';
    }
}
