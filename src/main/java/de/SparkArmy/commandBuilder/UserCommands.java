package de.SparkArmy.commandBuilder;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

enum UserCommands {
    ;
    @Contract(value = " -> new", pure = true)
    static @NotNull Collection<CommandData> adminUserCommands(){
        return new ArrayList<>(){{
            add(Commands.context(Command.Type.USER,"Mod/Unmod Member"));
        }};
    }

    @Contract(" -> new")
    static @NotNull Collection<CommandData> modUserCommands(){
        return new ArrayList<>(){{
           add(Commands.context(Command.Type.USER,"warn"));
           add(Commands.context(Command.Type.USER,"mute"));
        }};
    }

    @Contract(value = " -> new", pure = true)
    static @NotNull Collection<CommandData> generalUserCommands(){
        return new ArrayList<>(){{
            add(Commands.context(Command.Type.USER,"Remove Roles"));
        }};
    }

}
