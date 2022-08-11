package de.SparkArmy.commandListener.guildCommands.slashCommands;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.utils.punishmentUtils.PunishmentType;
import de.SparkArmy.utils.punishmentUtils.PunishmentUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class Punishment extends CustomCommandListener {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String eventName = event.getName();
        if (!eventName.equals("punishment")) return;
        Member executedMember = event.getMember();
        if (executedMember == null) return;
        if (!executedMember.hasPermission(Permission.ADMINISTRATOR)){
            event.reply("You have not the permission to change parameters, please contact an administrator").setEphemeral(true).queue();
            return;
        }
        Guild guild = event.getGuild();
        if (guild == null){
            event.reply("Please use this command on a server").setEphemeral(true).queue();
            return;
        }

        OptionMapping punishment = event.getOption("punishment");

        JSONObject config = controller.getSpecificGuildConfig(guild, GuildConfigType.MAIN);
        JSONObject punishmentConfig;
        if (config.isNull("punishments")){
            punishmentConfig = new JSONObject();
            punishmentConfig.put("warn",new JSONObject(){{
                put("active",false);
                put("role-id","Empty");
            }});
            punishmentConfig.put("mute",new JSONObject(){{
                put("active",false);
                put("role-id","Empty");
            }});
            punishmentConfig.put("kick",new JSONObject(){{
                put("standard-reason","No reason provided");
            }});
            punishmentConfig.put("timeout",new JSONObject(){{
                put("standard-reason","No reason provided");
                put("standard-duration","10");
                put("standard-time-unit","minuets");
            }});
            punishmentConfig.put("ban",new JSONObject(){{
                put("standard-deleted-days","2");
                put("standard-reason","No reason provided");
            }});
            config.put("punishments",punishmentConfig);
            controller.writeInSpecificGuildConfig(guild,GuildConfigType.MAIN,config);

        }else {
            punishmentConfig = config.getJSONObject("punishments");
        }


        if (punishment == null){
            EmbedBuilder embedForStateOverview = new EmbedBuilder();
            embedForStateOverview.setTitle("Punishment overview");
            embedForStateOverview.setDescription("""
            This is a overview of punishments you can change parameters
            If you push a button under this embed you can change the parameters of this punishment
            """);

            event.replyEmbeds(embedForStateOverview.build()).setEphemeral(true).addActionRows(ActionRow.of(
                    Button.primary(String.format("%s;%s;punishment,ban",guild.getId(),executedMember.getId()),"Ban"),
                    Button.primary(String.format("%s;%s;punishment,kick",guild.getId(),executedMember.getId()),"Kick"),
                    Button.primary(String.format("%s;%s;punishment,timeout",guild.getId(),executedMember.getId()),"Timeout"),
                    Button.primary(String.format("%s;%s;punishment,warn",guild.getId(),executedMember.getId()),"Warn"),
                    Button.primary(String.format("%s;%s;punishment,mute",guild.getId(),executedMember.getId()),"Mute")
            )).queue(x->waiter.waitForEvent(ButtonInteractionEvent.class,f->f.getUser().equals(event.getUser()),f-> x.editOriginalComponents().queue(),30, TimeUnit.SECONDS,()->x.editOriginalComponents().queue()));
            return;
        }

        String punishmentString = punishment.getAsString();

        OptionMapping punishmentRole = event.getOption("punishment-role");
        if (punishmentRole == null){
            PunishmentUtil.sendPunishmentParamEmbed(event,punishment, punishmentConfig);
            return;
        }

        if (!punishmentString.equals("warn") && !punishmentString.equals("mute")){
            event.reply("You can only set a punishment-role for Warn and Mute").setEphemeral(true).queue();
            return;
        }

        JSONObject punishments = config.getJSONObject("punishments");
        JSONObject psmConfig = punishments.getJSONObject(punishmentString);
        psmConfig.put("role-id",punishmentRole.getAsRole().getId());
        punishments.put(punishmentString,psmConfig);
        config.put("punishments",punishments);
        controller.writeInSpecificGuildConfig(guild,GuildConfigType.MAIN,config);

        event.reply( " For " + PunishmentType.getByName(punishmentString) + " was the role " + punishmentRole.getAsRole().getName() + " set").setEphemeral(true).queue();
    }

}
