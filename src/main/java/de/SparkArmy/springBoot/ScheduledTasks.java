package de.SparkArmy.springBoot;

import de.SparkArmy.eventListener.globalEvents.commands.ModmailListener;
import de.SparkArmy.utils.TimedOperations;
import de.SparkArmy.utils.jda.ReactionRoleUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ScheduledTasks {


    @Scheduled(initialDelay = 10,fixedDelay = 2,timeUnit = TimeUnit.SECONDS)
    public void twoSeconds(){
        TimedOperations.removeOldTemporaryPunishments();
    }

    @Scheduled(initialDelay = 10,fixedDelay = 10,timeUnit = TimeUnit.SECONDS)
    public void tenSeconds(){
        ModmailListener.deleteOldFiles();
        ReactionRoleUtil.deleteOldTempFiles();
    }

    @Scheduled(initialDelay = 10,fixedDelay = 90, timeUnit = TimeUnit.SECONDS)
    public void ninetySeconds(){
        TimedOperations.checkForNotificationUpdates();
    }

    @Scheduled(initialDelay = 10,fixedDelay = 300,timeUnit = TimeUnit.SECONDS)
    public void fiveMinutes(){
        TimedOperations.updateStatusPhrase();
    }

    @Scheduled(initialDelay = 10,fixedDelay = 600, timeUnit = TimeUnit.SECONDS)
    public void tenMinutes(){
        TimedOperations.updateUserCount();
    }

    @Scheduled(initialDelay = 10,fixedDelay = 1,timeUnit = TimeUnit.DAYS)
    public void oneDay(){
        TimedOperations.deleteOldLogs();
    }

    @Scheduled(initialDelay = 10,fixedDelay = 345600, timeUnit = TimeUnit.SECONDS)
    public void fourDays(){
        TimedOperations.updateYouTubeSubscriptions();
    }
}

