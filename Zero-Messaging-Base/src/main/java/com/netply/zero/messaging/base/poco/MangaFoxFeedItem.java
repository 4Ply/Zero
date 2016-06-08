package com.netply.zero.messaging.base.poco;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MangaFoxFeedItem extends FeedItem {
    public MangaFoxFeedItem(String mangaName, String name, String url, String timeStamp) {
        super(mangaName, name, url, timeStamp);
    }

    @Override
    public String getTimeStamp() {
        try {
            SimpleDateFormat rssPubDateFormat = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);
            SimpleDateFormat mySqlFriendlyFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return mySqlFriendlyFormat.format(rssPubDateFormat.parse(timeStamp.trim()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return super.getTimeStamp();
    }
}
