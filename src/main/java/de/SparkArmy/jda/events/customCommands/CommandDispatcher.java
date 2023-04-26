package de.SparkArmy.jda.events.customCommands;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.JdaApi;
import de.SparkArmy.jda.events.customCommands.commands.*;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CommandDispatcher {

    private final Set<CustomCommand> commands = ConcurrentHashMap.newKeySet();
    private final ConfigController controller;
    private final Logger logger;

    public CommandDispatcher(@NotNull JdaApi jdaApi) {
        this.controller = jdaApi.getController();
        this.logger = jdaApi.getLogger();
        registerCommands();
    }

    private void registerCommands() {
        registerCommand(new ArchiveSlashCommand());
        registerCommand(new UpdateSlashCommand());
        registerCommand(new BanSlashCommand());
        registerCommand(new KickSlashCommand());
        registerCommand(new MuteSlashCommand());
        registerCommand(new WarnSlashCommand());
        registerCommand(new UnbanSlashCommand());
        registerCommand(new SoftbanSlashCommand());
        registerCommand(new PingSlashCommand());
        registerCommand(new NoteSlashCommand());
        registerCommand(new NicknameSlashCommand());
    }

    private void registerCommand(CustomCommand c) {
        if (commands.contains(c)) {
            logger.error("Command: " + c.getName() + " already registered");
            return;
        }
        commands.add(c);
    }

    @SubscribeEvent
    public void onCommandInteraction(@NotNull GenericCommandInteractionEvent event) {
        for (CustomCommand c : commands) {
            if (event instanceof SlashCommandInteractionEvent && event.getName().equals(c.getName())) {
                c.dispatchSlashEvent((SlashCommandInteractionEvent) event, controller);
            } else if (event instanceof MessageContextInteractionEvent && event.getName().equals(c.getName())) {
                c.dispatchMessageContextEvent((MessageContextInteractionEvent) event, controller);
            } else if (event instanceof UserContextInteractionEvent && event.getName().equals(c.getName())) {
                c.dispatchUserContextEvent((UserContextInteractionEvent) event, controller);
            }
        }
    }
}
