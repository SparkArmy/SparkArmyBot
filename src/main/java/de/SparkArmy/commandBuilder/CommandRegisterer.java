package de.SparkArmy.commandBuilder;

import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.JDA;


@SuppressWarnings("unused")
public enum CommandRegisterer {
    ;
    private static final JDA jda = MainUtil.jda;

    public static void registerGlobalSlashCommands(){
        CommandRegisterer.jda.updateCommands().queue();
        GlobalSlashCommands.globalSlashCommands().forEach(c-> {
            CommandRegisterer.jda.upsertCommand(c).queue();
            MainUtil.logger.info(c.getName() + " has been updated/created.");
        });
    }
}
