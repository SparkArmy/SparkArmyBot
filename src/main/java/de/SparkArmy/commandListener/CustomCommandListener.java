package de.SparkArmy.commandListener;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@SuppressWarnings("unused")
public abstract class CustomCommandListener extends ListenerAdapter {
    public ConfigController controller = MainUtil.controller;
    public Logger logger = MainUtil.logger;
    public JDA jda = MainUtil.jda;
    public EventWaiter waiter = MainUtil.waiter;
    public Guild storageServer = MainUtil.storageServer;


    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);
    }
}
