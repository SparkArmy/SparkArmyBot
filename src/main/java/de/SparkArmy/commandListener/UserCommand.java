package de.SparkArmy.commandListener;

import de.SparkArmy.controller.ConfigController;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

public abstract class UserCommand extends Command {

    public abstract void dispatch(UserContextInteractionEvent event, JDA jda, ConfigController controller);

}
