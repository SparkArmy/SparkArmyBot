package de.sparkarmy.tasks;

import de.sparkarmy.config.ConfigController;
import de.sparkarmy.misc.Util;
import de.sparkarmy.tasks.runnables.DeleteOldMessageAttachments;
import de.sparkarmy.tasks.runnables.YouTubePubSubSubscriber;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadController {

    @SuppressWarnings("resource")
    public ThreadController() {
        ScheduledExecutorService service = new ScheduledThreadPoolExecutor(2);
        ConfigController controller = Util.controller;
        service.scheduleWithFixedDelay(new YouTubePubSubSubscriber(controller), 0, 4, TimeUnit.DAYS);
        service.scheduleWithFixedDelay(new DeleteOldMessageAttachments(), 0, 1, TimeUnit.DAYS);
    }


}
