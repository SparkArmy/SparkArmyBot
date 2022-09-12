package de.SparkArmy.commandBuilder;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

enum MessageCommands {
    ;
    @Contract(value = " -> new", pure = true)
    static @NotNull Collection<CommandData> generalMessageCommands(){
        return new ArrayList<>(){{
            add(Commands.message("report").setGuildOnly(true));
        }};
    }
}
