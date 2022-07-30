package de.SparkArmy;

import de.SparkArmy.controller.LoggerController;

import java.util.logging.Logger;

public class Main {


    private final LoggerController loggerController;
    private final Logger logger;
    public Main() {
        this.loggerController = new LoggerController();
        this.logger = loggerController.getLogger();

    }

    public static void main(String[] args) {
        new Main();
    }

    public LoggerController getLoggerController() {
        return loggerController;
    }
}
