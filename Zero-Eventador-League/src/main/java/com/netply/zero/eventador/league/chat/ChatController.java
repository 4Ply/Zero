package com.netply.zero.eventador.league.chat;

import com.netply.botchan.web.model.Greeting;
import com.netply.botchan.web.model.Message;
import com.netply.zero.eventador.league.chat.persistence.LeagueChatDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class ChatController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    @Autowired
    private LeagueChatDatabase leagueChatDatabase;


    @RequestMapping(value = "/message", produces = "application/json", method = RequestMethod.PUT)
    public @ResponseBody
    Greeting message(
            @RequestParam(value = "sessionKey", required = false) String sessionKey,
            @RequestBody Message message) {
        System.out.println(message.toString());
        leagueChatDatabase.addMessage(message.getTarget(), message.getMessage());

        return new Greeting(counter.incrementAndGet(), String.format(template, ""));
    }
}
