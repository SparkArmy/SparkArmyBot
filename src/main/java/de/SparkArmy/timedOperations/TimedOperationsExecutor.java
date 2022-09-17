package de.SparkArmy.timedOperations;

import de.SparkArmy.eventListener.globalEvents.commands.ModmailListener;
import de.SparkArmy.utils.jda.ReactionRoleUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TimedOperationsExecutor {
    public TimedOperationsExecutor() {
        ScheduledThreadPoolExecutor execute = new ScheduledThreadPoolExecutor(20);
        execute.scheduleAtFixedRate(tenSeconds(),1,10, TimeUnit.SECONDS);
        execute.scheduleAtFixedRate(twoSeconds(),1,2,TimeUnit.SECONDS);
        execute.scheduleAtFixedRate(ninetySeconds(),2,90,TimeUnit.SECONDS);
        execute.scheduleAtFixedRate(fiveMinutes(), 0,5,TimeUnit.MINUTES);
        execute.scheduleAtFixedRate(tenMinutes(),0,10,TimeUnit.MINUTES);
        execute.scheduleAtFixedRate(fourDays(),0,4,TimeUnit.DAYS);

    }

    @Contract(pure = true)
    private static @NotNull Runnable twoSeconds(){
        return TimedOperations::removeOldTemporaryPunishments;
    }

    @Contract(pure = true)
    private static @NotNull Runnable tenSeconds() {
       return ()->{
           ModmailListener.deleteOldFiles();
           ReactionRoleUtil.deleteOldTempFiles();
       };
    }
    @Contract(pure = true)
    private static @NotNull Runnable ninetySeconds(){
        // Rate Limit from Twitter 104 requests per 90 seconds (100.000 per 24h)
        // Rate Limit from Twitch 800 requests per 60 seconds
        return TimedOperations::checkForNotificationUpdates;
    }

    @Contract(pure = true)
    private static @NotNull Runnable fiveMinutes(){
        return TimedOperations::updateStatusPhrase;
    }

    @Contract(pure = true)
    private static @NotNull Runnable tenMinutes(){
        return TimedOperations::updateUserCount;
    }

    @Contract(pure = true)
    private static @NotNull Runnable fourDays(){
        return TimedOperations::updateYouTubeSubscriptions;
    }
}
