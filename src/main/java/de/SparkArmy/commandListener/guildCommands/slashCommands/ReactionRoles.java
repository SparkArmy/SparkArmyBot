package de.SparkArmy.commandListener.guildCommands.slashCommands;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.ReactionRoleUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ReactionRoles extends CustomCommandListener {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()){
            event.reply("Please execute this command in a guild channel").setEphemeral(true).queue();
            return;
        }
        String eventName = event.getName();
        if (!eventName.equals("reactionroles")) return;

        User user = event.getUser();

        OptionMapping action = event.getOption("action");
        if (action == null){
            EmbedBuilder startEmbed = new EmbedBuilder();
            startEmbed.setTitle("Reaction Roles");
            startEmbed.setDescription("Please click one button below. The explanations you find in the fields below this description");
            startEmbed.addField("Create", "Create a new Reaction-Role-Embed",false);
            startEmbed.addField("Edit","Edit a Reaction-Role-Embed",false);
            startEmbed.addField("Delete","Delete a Reaction-Role-Embed",false);
            startEmbed.setColor(new Color(0x941D9E));
            event.replyEmbeds(startEmbed.build()).setEphemeral(true).addActionRows(ActionRow.of(
                    Button.primary(String.format("reactionRolesStart,create;%s",user.getId()),"Create"),
                    Button.primary(String.format("reactionRolesStart,edit;%s",user.getId()),"Edit"),
                    Button.primary(String.format("reactionRolesStart,delete;%s",user.getId()),"Delete")
            )).queue();
            return;
        }

        String actionString = action.getAsString();
        OptionMapping message = event.getOption("message");
        if (message == null){
            ReactionRoleUtil.createEmbedOrModalByAction(actionString,user,event);
            return;
        }

        if (actionString.equals("create")){
            event.reply("Use the message-option only for the actions Edit and Delete").setEphemeral(true).queue();
            return;
        }

        switch (actionString){
            case "edit" -> ReactionRoleUtil.sendEditEmbed(message.getAsString(), event.getChannel().getId(), event);
            case "delete" -> ReactionRoleUtil.deleteReactionRoleEmbed(event.getChannel().getId(),message.getAsString(),event);
            default -> {}
        }


    }
}
