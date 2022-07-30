package de.SparkArmy.commandListener;

import de.SparkArmy.commandListener.globalCommands.slashCommands.ModmailCommand;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.JDA;

import java.util.ArrayList;

public class CommandListenerRegisterer {

    private final ArrayList<CustomCommandListener> commands = new ArrayList<>();
    private final JDA jda = MainUtil.jda;

    public CommandListenerRegisterer() {
        registerCommandListeners();
    }

    private void registerCommandListeners() {
        this.commands.add(
                new ModmailCommand()
        );

        commands.forEach(jda::addEventListener);
    }
}
