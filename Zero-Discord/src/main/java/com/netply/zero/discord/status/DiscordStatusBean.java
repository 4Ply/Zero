package com.netply.zero.discord.status;

import com.netply.botchan.web.model.Status;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class DiscordStatusBean {
    @RequestMapping(value = "/status", produces = "application/json", method = RequestMethod.GET)
    public @ResponseBody List<Status> getReplies() {
        ArrayList<Status> statuses = new ArrayList<>();
        statuses.add(new Status("initDate", StatusUtil.getInitDate().toString()));
        statuses.add(new Status("lastReceivedMessageDate", StatusUtil.getLastMessageReceivedDate().toString()));
        statuses.add(new Status("receivedMessages", String.valueOf(StatusUtil.getReceivedMessages())));
        return statuses;
    }
}
