package de.SparkArmy.commandBuilder;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.ArrayList;
import java.util.Collection;

public class GlobalSlashCommands {

    protected static Collection<CommandData> globalSlashCommands(){
      return new ArrayList<>(){{
          add(Commands.slash("modmail","Contact to the server team for help"));
          add(Commands.slash("feedback","Contact for feedback"));
      }};
    }
}
