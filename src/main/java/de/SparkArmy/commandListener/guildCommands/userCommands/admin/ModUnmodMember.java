package de.SparkArmy.commandListener.guildCommands.userCommands.admin;

import de.SparkArmy.commandListener.UserCommand;
import de.SparkArmy.controller.ConfigController;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class ModUnmodMember extends UserCommand {

    @Override
    public void dispatch(@NotNull UserContextInteractionEvent event, JDA jda, ConfigController controller) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Use this app on a guild-member").setEphemeral(true).queue();
            return;
        }

        JSONObject config = controller.getGuildMainConfig(guild);
        if (config.isNull("moderation")) {
            event.reply("Please add a moderation-role with /moderation-config").setEphemeral(true).queue();
            return;
        }
        JSONObject moderationConfig = config.getJSONObject("moderation");
        JSONArray roleIds = moderationConfig.getJSONArray("roles");
        if (roleIds.isEmpty()) {
            event.reply("Please add a moderation-role with /moderation-config").setEphemeral(true).queue();
            return;
        }

        List<Role> roles = roleIds.toList().stream().map(x -> {
            Role r = guild.getRoleById(x.toString());
            if (r != null) return r;
            return guild.getPublicRole();
        }).toList();

        if (roles.contains(guild.getPublicRole())){
            event.reply("PLease check this role-ids").setEphemeral(true).queue();
            return;
        }

        Member targetMember = event.getTargetMember();

        if (targetMember == null) {
            event.reply("Please give a valid target").setEphemeral(true).queue();
            return;
        }

        Member bot = event.getGuild().getMember(event.getJDA().getSelfUser());
        if (bot == null) {
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        if (!roles.stream().allMatch(bot::canInteract)) {
            event.reply("Please place the bot over the target member or the roles under the bot").setEphemeral(true).queue();
            return;
        }

        if (targetMember.getRoles().stream().anyMatch(x -> roleIds.toList().contains(x.getId()))) {
            roles.forEach(x -> guild.removeRoleFromMember(targetMember, x).queue());
            event.reply("Moderation rights from member removed").setEphemeral(true).queue();
        } else {
            roles.forEach(x -> guild.addRoleToMember(targetMember, x).queue());
            event.reply("User has moderator rights now").setEphemeral(true).queue();
        }
    }

    @Override
    public String getName() {
        return "Mod/Unmod Member";
    }
}
