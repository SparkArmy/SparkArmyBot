package de.SparkArmy.commandBuilder;

import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.JDA;

public class CommandRegisterer {
    private static final JDA jda = MainUtil.jda;

    public static void registerGlobalSlashCommands(){
        jda.updateCommands().queue();
        GlobalSlashCommands.globalSlashCommands().forEach(c->jda.upsertCommand(c).queue());
    }
}
