package com.netply.zero.scheduler.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SimpleJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String message = "Reminder: " + jobExecutionContext.getJobDetail().getDescription();
//        MessageUtil.reply("https://app2.bot-chan.com:20000", new Message(0, null, "120238245031182336", null, false), message);
    }
}
