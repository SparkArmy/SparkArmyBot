package de.SparkArmy.commandListener;

import de.SparkArmy.controller.ConfigController;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

public abstract class MessageCommand extends Command {

    public abstract void dispatch(MessageContextInteractionEvent event, JDA jda, ConfigController controller);

}
