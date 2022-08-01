package de.SparkArmy.timedOperations;

import de.SparkArmy.eventListener.globalEvents.ModmailListener;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TimedOperationsExecutor {
    public TimedOperationsExecutor() {
        new ScheduledThreadPoolExecutor(20).scheduleAtFixedRate(tenSeconds(),0,10, TimeUnit.SECONDS);
    }

    private Runnable tenSeconds() {
       return ModmailListener::deleteOldFiles;
    }
}
