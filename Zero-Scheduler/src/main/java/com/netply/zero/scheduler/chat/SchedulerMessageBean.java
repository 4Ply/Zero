package com.netply.zero.scheduler.chat;

import com.netply.botchan.web.model.Event;
import com.netply.botchan.web.model.MatcherList;
import com.netply.botchan.web.model.Message;
import com.netply.zero.scheduler.job.SimpleJob;
import com.netply.zero.service.base.ListUtil;
import com.netply.zero.service.base.Service;
import com.netply.zero.service.base.ServiceCallback;
import com.netply.zero.service.base.credentials.BasicSessionCredentials;
import com.netply.zero.service.base.credentials.SessionManager;
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
    private final String platform;
    private final Scheduler scheduler;
    private MessageListener messageListener;
    private Process songProcess;


    @Autowired
    public SchedulerMessageBean(@Value("${key.server.bot-chan.url}") String botChanURL, @Value("${key.platform}") String platform, Scheduler scheduler) {
        this.botChanURL = botChanURL;
        this.platform = platform;
        messageListener = new MessageListener(this.botChanURL);
        this.scheduler = scheduler;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkForMessages() {
        ArrayList<String> messageMatchers = new ArrayList<>();
        messageMatchers.add(EventMatchers.REMIND_ME_MATCHER);
        messageListener.checkMessages("/messages", new MatcherList(SessionManager.getClientID(), messageMatchers), this::parseMessage);
    }

    private void parseMessage(Message message) {
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
            String cronName = message.getSender() + "--" + messageText;
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
        checkEvents("/events", new MatcherList(SessionManager.getClientID(), messageMatchers), this::parseEvent);
    }

    public void checkEvents(String url, MatcherList matcherList, final Consumer<Event> eventConsumer) {
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
        String deleteMessageURL = String.format("/message?clientID=%s&id=%s", String.valueOf(SessionManager.getClientID()), event.getId());
        Service.create(botChanURL).delete(deleteMessageURL, new BasicSessionCredentials(), new ServiceCallback<Object>() {
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
