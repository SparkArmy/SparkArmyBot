package de.SparkArmy.tasks;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.tasks.runnables.CleanPeriodicMessages;
import de.SparkArmy.tasks.runnables.DeleteOldMessageAttachments;
import de.SparkArmy.tasks.runnables.YouTubePubSubSubscriber;
import de.SparkArmy.utils.Util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadController {

    @SuppressWarnings("resource")
    public ThreadController() {
        ScheduledExecutorService service = new ScheduledThreadPoolExecutor(3);
        ConfigController controller = Util.controller;
        service.scheduleWithFixedDelay(new YouTubePubSubSubscriber(controller), 0, 4, TimeUnit.DAYS);
        service.scheduleWithFixedDelay(new DeleteOldMessageAttachments(), 0, 1, TimeUnit.DAYS);
        service.scheduleWithFixedDelay(new CleanPeriodicMessages(), 0, 5, TimeUnit.MINUTES);
    }


}
