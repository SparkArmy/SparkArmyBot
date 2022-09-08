package de.SparkArmy.commandListener.guildCommands.slashCommands.admin;

import de.SparkArmy.commandListener.CustomCommandListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class ModerationConfig extends CustomCommandListener {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("moderation-config")) return;
        if (event.getGuild() == null){
            event.reply("Please use this command on a guild").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder selectEmbed = new EmbedBuilder();
        selectEmbed.setTitle("Moderation Config");
        selectEmbed.setDescription("Click a button below to edit the content");
        selectEmbed.addField("Roles","Edit the moderation-roles",true);

        User user = event.getUser();

        Collection<Button> buttons = new ArrayList<>(){{
           add(Button.primary(String.format("modConfigRoles;%s",user.getId()),"Roles"));
        }};

        event.replyEmbeds(selectEmbed.build()).addComponents(ActionRow.of(buttons)).setEphemeral(true).queue();
    }
}
