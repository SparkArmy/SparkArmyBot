package de.SparkArmy.eventListener;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;
import org.slf4j.Logger;


public abstract class CustomEventListener extends ListenerAdapter {
    public ConfigController controller = MainUtil.controller;
    public Logger logger = MainUtil.logger;
    public JDA jda = MainUtil.jda;
    public EventWaiter waiter = MainUtil.waiter;
    public Guild storageServer = MainUtil.storageServer;


    public JSONObject getGuildMainConfig(Guild guild){
      return controller.getSpecificGuildConfig(guild, GuildConfigType.MAIN);
    }

    public void writeInGuildMainConfig(Guild guild, JSONObject config){
        controller.writeInSpecificGuildConfig(guild,GuildConfigType.MAIN,config);
    }
}
