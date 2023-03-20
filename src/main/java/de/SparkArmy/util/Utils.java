package de.SparkArmy.util;

import de.SparkArmy.controller.ConfigController;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.sql.SQLException;

public class Utils {
    public static Logger logger;
    public static ConfigController controller;
    public static JDA jda;

    public static void handleSQLExeptions(@NotNull SQLException e) {
        logger.error(e.getMessage());
    }

}
