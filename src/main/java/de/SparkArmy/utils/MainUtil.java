package de.SparkArmy.utils;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.timedOperations.TimedOperationsExecutor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.json.JSONObject;

import java.util.logging.Logger;

public enum MainUtil {
    ;
    public static Logger logger;
    public static ConfigController controller;
    public static JSONObject mainConfig;
    public static JDA jda;
    public static EventWaiter waiter;
    public static TimedOperationsExecutor timedOperations;

    public static Guild storageServer;

}
