package de.sparkarmy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Main(String[] args) {

    }

    public static void main(String[] args) {
        LOGGER.info("Main start requested");
        new Main(args);
    }
}
