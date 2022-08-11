package de.SparkArmy.timedOperations;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.utils.MainUtil;
import de.SparkArmy.utils.punishmentUtils.TemporaryPunishment;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

import java.time.OffsetDateTime;

public class TimedOperations {
    private static final JDA jda = MainUtil.jda;
    private static final ConfigController controller = MainUtil.controller;

    protected static void removeOldTemporaryPunishments(){
        JSONObject entrys = TemporaryPunishment.getTimedPunishmentsFromPunishmentFile();
        if (entrys == null) return;

        if (entrys.isEmpty()) return;
        entrys.keySet().forEach(key->{
            JSONObject entry = entrys.getJSONObject(key);
            if (OffsetDateTime.parse(entry.getString("expirationTime")).isAfter(OffsetDateTime.now())){
                Guild guild = jda.getGuildById(entry.getString("guild"));
                if (guild == null) return;
                Role punishmentRole;
                User user = jda.getUserById(entry.getString("user"));
                if (user == null) return;
                switch (entry.getString("type")){
                    case "warn" -> punishmentRole = guild.getRoleById(controller.getSpecificGuildConfig(guild, GuildConfigType.MAIN).getJSONObject("punishment-roles").getString("warn-role-id"));
                    case "mute" -> punishmentRole = guild.getRoleById(controller.getSpecificGuildConfig(guild, GuildConfigType.MAIN).getJSONObject("punishment-roles").getString("mute-role-id"));
                    default -> {
                        guild.unban(user).reason("Automatic Unban").queue();
                        return;
                    }
                }
                if (punishmentRole == null) return;
                if (!guild.isMember(user)) return;
                guild.removeRoleFromMember(user,punishmentRole).queue();
            }
        });
    }
}
