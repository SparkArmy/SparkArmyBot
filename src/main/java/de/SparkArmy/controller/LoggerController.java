package de.SparkArmy.controller;

import de.SparkArmy.utils.FileHandler;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;

public class LoggerController {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public LoggerController() {
        try {
            if (!FileHandler.createDirectory("logs")){
                System.exit(10);
            }
            File directory = FileHandler.getDirectoryInUserDirectory( "logs");
            java.util.logging.FileHandler fileHandler = new java.util.logging.FileHandler(directory.getAbsolutePath() + "/BotLog_" +
                     new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date(System.currentTimeMillis())) + ".log");
            logger.addHandler(fileHandler);
            if (Objects.requireNonNull(directory.listFiles()).length > 11){
                Arrays.stream(Objects.requireNonNull(directory.listFiles())).toList().get(0).delete();
            }
        }catch (IOException e) {
            System.exit(1);
        }
        logger.info("Logger was successful initialed");
    }

    public Logger getLogger() {
        return logger;
    }
}
