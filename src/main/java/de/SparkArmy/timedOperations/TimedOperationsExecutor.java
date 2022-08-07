package de.SparkArmy.timedOperations;

import de.SparkArmy.eventListener.globalEvents.ModmailListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class TimedOperationsExecutor {
    public TimedOperationsExecutor() {
        new ScheduledThreadPoolExecutor(20).scheduleAtFixedRate(tenSeconds(),0,10, TimeUnit.SECONDS);
    }

    @Contract(pure = true)
    private @NotNull Runnable tenSeconds() {
       return ModmailListener::deleteOldFiles;
    }

    @Contract(pure = true)
    private @NotNull Runnable twentySeconds(){
        return TimedOperations::removeOldTemporaryPunishments;
    }
}
