package de.SparkArmy.eventListener;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.logging.Logger;

public abstract class CustomEventListener extends ListenerAdapter {
    public ConfigController controller = MainUtil.controller;
    public Logger logger = MainUtil.logger;

}
