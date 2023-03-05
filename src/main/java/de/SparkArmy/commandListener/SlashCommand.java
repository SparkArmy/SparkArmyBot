package de.SparkArmy.commandListener;

import de.SparkArmy.controller.ConfigController;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class SlashCommand extends Command {

    public abstract void dispatch(SlashCommandInteractionEvent event, JDA jda, ConfigController controller);

}
