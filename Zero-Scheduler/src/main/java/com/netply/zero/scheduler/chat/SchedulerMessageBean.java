package com.netply.zero.scheduler.chat;

import com.netply.botchan.web.model.Event;
import com.netply.botchan.web.model.FromUserMessage;
import com.netply.zero.scheduler.job.SimpleJob;
import com.netply.zero.service.base.ListUtil;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ServiceCallback;
import com.netply.zero.service.base.messaging.MessageListener;
import com.netply.zero.service.base.messaging.MessageUtil;
import com.sun.jersey.api.client.ClientResponse;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Component
public class SchedulerMessageBean {
    private final String botChanURL;
    private final Scheduler scheduler;
    private MessageListener messageListener;


    @Autowired
    public SchedulerMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL, Scheduler scheduler) {
        this.botChanURL = botChanURL;
        this.scheduler = scheduler;
        messageListener = new MessageListener(botChanURL);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForMessages() {
        ArrayList<String> messageMatchers = new ArrayList<>();
        messageMatchers.add(EventMatchers.REMIND_ME_MATCHER);
        messageListener.checkMessages("/messages", messageMatchers, this::parseMessage);
    }

    private void parseMessage(FromUserMessage message) {
        String messageText = message.getMessage();
        if (messageText.matches(EventMatchers.REMIND_ME_MATCHER)) {
            messageText = messageText.replaceAll(EventMatchers.REMIND_ME_MATCHER.replace("(.*)", "").replace("(.*)", ""), "").trim();
            if (!messageText.contains("#")) {
                MessageUtil.reply(botChanURL, message, "Invalid format, use Remind me at * * * * * ?#Message");
                return;
            }
            String[] split = messageText.split("#");
            String remindMeCronTime = split[0];
            String reminder = split[1];
            String cronName = message.getPlatformID() + "--" + messageText;
            JobDetail job = newJob(SimpleJob.class)
                    .withIdentity(cronName, "reminders")
                    .withDescription(reminder)
                    .build();

            CronTrigger trigger = newTrigger()
                    .withIdentity(cronName, "reminders")
                    .withSchedule(cronSchedule(remindMeCronTime))
                    .build();

            System.out.println(trigger.getNextFireTime());

            try {
                if (!scheduler.isStarted()) {
                    scheduler.start();
                }
                scheduler.scheduleJob(job, trigger);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForEvents() {
        ArrayList<String> messageMatchers = new ArrayList<>();
        messageMatchers.add(EventMatchers.REMIND_ME_MATCHER);
        checkEvents("/events", messageMatchers, this::parseEvent);
    }

    public void checkEvents(String url, ArrayList<String> matcherList, final Consumer<Event> eventConsumer) {
        messageListener.checkSubscribedObjects(url, matcherList, output -> {
            List<Event> events = ListUtil.stringToArray(output, Event[].class);
            for (Event event : events) {
                System.out.println(event.toString());
                deleteEvent(event, eventConsumer);
            }

            System.out.println("Events Parsed: " + output);
        });
    }

    private void deleteEvent(Event event, Consumer<Event> eventConsumer) {
        String deleteEventURL = String.format("/message?id=%s", event.getId());
        Service.create(botChanURL).delete(deleteEventURL, new ServiceCallback<Object>() {
            @Override
            public void onError(ClientResponse response) {

            }

            @Override
            public void onSuccess(String output) {
                eventConsumer.accept(event);
            }

            @Override
            public void onSuccess(Object parsedResponse) {

            }
        });
    }

    private void parseEvent(Event event) {

    }
}
