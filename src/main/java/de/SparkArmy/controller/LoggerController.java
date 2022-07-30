package de.SparkArmy.controller;

import de.SparkArmy.Main;
import de.SparkArmy.utils.FileHandler;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerController {

    private final Logger logger;

    public LoggerController() {
        this.logger = Logger.getLogger(this.getClass().getName());
        logger.setLevel(Level.ALL);
        try {
            if (!FileHandler.createDirectory("logs")) {
                Main.systemExit(10);
            }
            File directory = FileHandler.getDirectoryInUserDirectory("logs");
            for (File f : Objects.requireNonNull(Objects.requireNonNull(directory).listFiles())) {
                if (f.getName().endsWith(".lck")) {
                    f.deleteOnExit();
                }
            }
            java.util.logging.FileHandler fileHandler = new java.util.logging.FileHandler(directory.getAbsolutePath() + "/BotLog_" +
                    new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date(System.currentTimeMillis())) + ".log");
            logger.addHandler(fileHandler);
            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.CONFIG);
            logger.addHandler(consoleHandler);
            logger.setUseParentHandlers(false);
            if (Objects.requireNonNull(directory.listFiles()).length > 11) {
                Arrays.stream(Objects.requireNonNull(directory.listFiles())).toList().get(0).deleteOnExit();
            }
        } catch (IOException e) {
            Main.systemExit(1);
        }
        logger.info("Logger was successful initialed");
    }

    public Logger getLogger() {
        return logger;
    }
}
