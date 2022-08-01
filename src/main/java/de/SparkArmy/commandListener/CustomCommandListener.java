package de.SparkArmy.commandListener;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public abstract class CustomCommandListener extends ListenerAdapter {
    public ConfigController controller = MainUtil.controller;
    public Logger logger = MainUtil.logger;


    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);
    }
}
