package de.SparkArmy.util;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import de.SparkArmy.Main;
import de.SparkArmy.controller.ConfigController;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.json.JSONObject;
import org.slf4j.Logger;

public class Utils {
    public static Logger logger;
    public static ConfigController controller;
    public static Main main;
    public static JSONObject mainConfig;
    public static JDA jda;
    public static EventWaiter waiter;
    public static Guild storageServer;

}
