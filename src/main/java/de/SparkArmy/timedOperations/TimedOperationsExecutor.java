package de.SparkArmy.timedOperations;

import de.SparkArmy.eventListener.globalEvents.ModmailListener;
import de.SparkArmy.utils.reactionRoleUtils.ReactionRoleUtlis;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TimedOperationsExecutor {
    public TimedOperationsExecutor() {
        ScheduledThreadPoolExecutor execute = new ScheduledThreadPoolExecutor(2);
        execute.scheduleAtFixedRate(tenSeconds(),1,10, TimeUnit.SECONDS);
        execute.scheduleAtFixedRate(twoSeconds(),1,2,TimeUnit.SECONDS);
    }

    @Contract(pure = true)
    private static @NotNull Runnable twoSeconds(){
        return TimedOperations::removeOldTemporaryPunishments;
    }

    @Contract(pure = true)
    private static @NotNull Runnable tenSeconds() {
       return ()->{
           ModmailListener.deleteOldFiles();
           ReactionRoleUtlis.deleteOldTempFiles();
       };
    }


}
