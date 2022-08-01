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
        logger = Logger.getLogger(getClass().getName());
        this.logger.setLevel(Level.ALL);
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
            this.logger.addHandler(fileHandler);
            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.CONFIG);
            this.logger.addHandler(consoleHandler);
            this.logger.setUseParentHandlers(false);
            if (11 < Objects.requireNonNull(directory.listFiles()).length) {
                Arrays.stream(Objects.requireNonNull(directory.listFiles())).toList().get(0).deleteOnExit();
            }
        } catch (IOException e) {
            Main.systemExit(1);
        }
        this.logger.info("Logger was successful initialed");
    }

    public Logger getLogger() {
        return this.logger;
    }
}
