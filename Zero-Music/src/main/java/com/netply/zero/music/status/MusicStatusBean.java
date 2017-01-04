package com.netply.zero.music.status;

import com.netply.botchan.web.model.Status;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class MusicStatusBean {
    @RequestMapping(value = "/status", produces = "application/json", method = RequestMethod.GET)
    public @ResponseBody List<Status> getStatus() {
        ArrayList<Status> statuses = new ArrayList<>();
        statuses.add(new Status("initDate", String.valueOf(StatusUtil.getInitDate())));
        return statuses;
    }
}
