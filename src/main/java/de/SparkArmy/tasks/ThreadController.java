package de.SparkArmy.tasks;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.utils.Util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadController {

    private final ScheduledExecutorService service;
    private final ConfigController controller = Util.controller;


    public ThreadController(){
        this.service = new ScheduledThreadPoolExecutor(10);
        this.service.scheduleWithFixedDelay(new YouTubePubSubSubscriber(controller),0,4, TimeUnit.DAYS);
    }


}
