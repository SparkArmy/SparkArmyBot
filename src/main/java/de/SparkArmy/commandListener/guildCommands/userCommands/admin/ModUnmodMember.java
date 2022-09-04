package de.SparkArmy.commandListener.guildCommands.userCommands.admin;

import de.SparkArmy.commandListener.CustomCommandListener;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class ModUnmodMember extends CustomCommandListener {
    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        if (!event.getName().equals("Mod/Unmod Member")) return;
        if (event.getGuild() == null) {
            event.reply("Use this app on a guild-member").setEphemeral(true).queue();
            return;
        }

        JSONObject config = getGuildMainConfig(event.getGuild());
        if (config.isNull("moderation")) {
            event.reply("Please add a moderation-role with /moderation-config").setEphemeral(true).queue();
            return;
        }
        JSONObject moderationConfig = config.getJSONObject("moderation");
        JSONArray roles = moderationConfig.getJSONArray("roles");
        if (roles.isEmpty()) {
            event.reply("Please add a moderation-role with /moderation-config").setEphemeral(true).queue();
            return;
        }

        Member targetMember = event.getTargetMember();

        if (targetMember == null) {
            event.reply("Please give a valid target").setEphemeral(true).queue();
            return;
        }

        if (targetMember.getRoles().stream().anyMatch(x -> roles.toList().contains(x.getId()))) {
            StringBuilder failedRoles = new StringBuilder();
            roles.forEach(role -> {
                try {
                    Role r = event.getGuild().getRoleById(role.toString());
                    //noinspection ConstantConditions
                    event.getGuild().removeRoleFromMember(targetMember, r).queue();
                } catch (NullPointerException ignored) {
                    failedRoles.append(role).append(" ");
                }
            });
            if (!failedRoles.isEmpty()) {
                event.reply("Please check this roles: " + failedRoles).setEphemeral(true).queue();
                return;
            }

            event.reply("Moderation rights from member removed").setEphemeral(true).queue();
        } else {
            StringBuilder failedRoles = new StringBuilder();
            roles.forEach(role -> {
                try {
                    Role r = event.getGuild().getRoleById(role.toString());
                    //noinspection ConstantConditions
                    event.getGuild().addRoleToMember(targetMember.getUser(), r).queue();
                } catch (NullPointerException ignored) {
                    failedRoles.append(role).append(" ");
                }
            });
            if (!failedRoles.isEmpty()) {
                event.reply("Please check this roles: " + failedRoles).setEphemeral(true).queue();
                return;
            }

            event.reply("User has moderator rights now").setEphemeral(true).queue();
        }
    }
}
