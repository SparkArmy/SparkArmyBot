package de.SparkArmy.commandListener;

import de.SparkArmy.commandListener.globalCommands.slashCommands.ModmailCommand;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.JDA;

import java.util.ArrayList;

public class CommandListenerRegisterer {

    private final ArrayList<CustomCommandListener> commands = new ArrayList<>();
    private final JDA jda = MainUtil.jda;

    public CommandListenerRegisterer() {
        this.registerCommandListeners();
    }

    private void registerCommandListeners() {
        commands.add(
                new ModmailCommand()
        );

        this.commands.forEach(this.jda::addEventListener);
    }
}
