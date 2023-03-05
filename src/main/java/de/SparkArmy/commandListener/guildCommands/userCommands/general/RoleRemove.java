package de.SparkArmy.commandListener.guildCommands.userCommands.general;

import de.SparkArmy.commandListener.UserCommand;
import de.SparkArmy.controller.ConfigController;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Objects;

public class RoleRemove extends UserCommand {

    @Override
    public void dispatch(@NotNull UserContextInteractionEvent event, JDA jda, ConfigController controller) {
        if (event.getGuild() == null) {
            event.reply("Please use this command on a guild").setEphemeral(true).queue();
            return;
        }

        if (event.getTargetMember() == null) {
            event.reply("Ups something went wrong, please try again").setEphemeral(true).queue();
            return;
        }

        if (!Objects.equals(event.getMember(), event.getTargetMember())) {
            event.reply("You can only remove roles from yourself").setEphemeral(true).queue();
            return;
        }
        JSONObject config = controller.getGuildMainConfig(event.getGuild());

        event.getTargetMember().getRoles().stream().filter(x->{
            // Filter moderation roles
            if (x.isPublicRole()) return true;
            if (!config.isNull("moderation") && !config.getJSONObject("moderation").getJSONArray("roles").isEmpty()){
               return config.getJSONObject("moderation").getJSONArray("roles").toList().stream().anyMatch(y->!y.equals(x.getId()));
            }
            return true;
        }).filter(x->{
            // filter punishment roles
            if (x.isPublicRole()) return true;
            if (config.isNull("punishments")) return true;
            JSONObject punishments = config.getJSONObject("punishments");
            return punishments.keySet().stream().noneMatch(y -> {
                JSONObject punishment = punishments.getJSONObject(y);
                return !punishment.isNull("role-id") && !punishment.getString("role-id").isEmpty() && punishment.getString("role-id").equals(x.getId());
            });
        }).filter(x -> {
            // Filter permissions
            if (x.isPublicRole()) return true;
            if (x.getPermissions().contains(Permission.ADMINISTRATOR)) return false;
            return !x.getPermissions().contains(Permission.KICK_MEMBERS);
        }).filter(x -> {
            // Filter managed roles
            return !x.isManaged();
        }).forEach(x -> event.getGuild().removeRoleFromMember(event.getTargetMember().getUser(), x).queue());

        event.reply("All roles was removed from you").setEphemeral(true).queue();
    }

    @Override
    public String getName() {
        return "Remove Roles";
    }
}
