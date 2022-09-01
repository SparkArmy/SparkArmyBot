package de.SparkArmy.eventListener.guildEvents.commands;

import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.punishmentUtils.PunishmentType;
import de.SparkArmy.utils.punishmentUtils.PunishmentUtil;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class PunishmentListener extends CustomEventListener {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.contains(";")) return;
        if (id.split(";").length > 2 && id.split(";")[2].split(",")[0].equals("punishment")) {
            PunishmentUtil.sendPunishmentParamEmbed(event, event.getGuild());
            return;
        }
        if (!id.contains(",")) return;
        if (id.split(";").length < 3) return;
        String punishment = id.split(";")[2].split(",")[0];
        PunishmentType type = PunishmentType.getByName(punishment);
        if (type.equals(PunishmentType.UNKNOW)) return;
        JSONObject config = controller.getSpecificGuildConfig(Objects.requireNonNull(event.getGuild()), GuildConfigType.MAIN);
        JSONObject psm = config.getJSONObject("punishments").getJSONObject(type.getName());
        if (id.split(";")[2].split(",")[1].equals("Edit")) {
            Collection<ActionRow> inputs = new ArrayList<>();
            switch (type) {
                case WARN, MUTE -> {
                    TextInput enabled = TextInput.create(String.format("%s,enabled", type.getName()), "Enabled", TextInputStyle.SHORT)
                            .setMinLength(3)
                            .setMaxLength(5)
                            .setPlaceholder("true || false")
                            .setValue(String.valueOf(psm.getBoolean("active"))).build();
                    inputs.add(ActionRow.of(enabled));

                    TextInput roleId = TextInput.create(String.format("%s,role", type.getName()), "Role ID", TextInputStyle.SHORT)
                            .setMinLength(5)
                            .setPlaceholder("The punishment role id")
                            .setValue(psm.getString("role-id")).build();
                    inputs.add(ActionRow.of(roleId));
                }
                case KICK -> {
                    TextInput reason = TextInput.create(String.format("%s,reason", type.getName()), "Reason", TextInputStyle.SHORT)
                            .setMinLength(15)
                            .setMaxLength(200)
                            .setValue(psm.getString("standard-reason")).build();
                    inputs.add(ActionRow.of(reason));
                }
                case BAN -> {
                    logger.info("ban");
                    TextInput reason = TextInput.create(String.format("%s,reason", type.getName()), " Kick Reason", TextInputStyle.SHORT)
                            .setMinLength(15)
                            .setMaxLength(200)
                            .setValue(psm.getString("standard-reason")).build();
                    inputs.add(ActionRow.of(reason));

                    TextInput days = TextInput.create(String.format("%s,days", type.getName()), "Days", TextInputStyle.SHORT)
                            .setMaxLength(1)
                            .setValue(psm.getString("standard-deleted-days")).build();
                    inputs.add(ActionRow.of(days));
                }
                case TIMEOUT -> {
                    TextInput duration = TextInput.create(String.format("%s,duration", type.getName()), "Duration", TextInputStyle.SHORT)
                            .setValue(psm.getString("standard-duration")).build();
                    inputs.add(ActionRow.of(duration));
                    TextInput timeUnit = TextInput.create(String.format("%s,timeunit", type.getName()), "Time Unit", TextInputStyle.SHORT)
                            .setValue(psm.getString("standard-time-unit")).build();
                    inputs.add(ActionRow.of(timeUnit));
                    TextInput reason = TextInput.create(String.format("%s,reason", type.getName()), "Ban Reason", TextInputStyle.SHORT)
                            .setMinLength(15)
                            .setMaxLength(200)
                            .setValue(psm.getString("standard-reason")).build();
                    inputs.add(ActionRow.of(reason));
                }
                default -> {
                    event.reply("Somthing went wrong").setEphemeral(true).queue();
                    event.editComponents().queue();
                    return;
                }
            }

            Modal.Builder modal = Modal.create(String.format("%s,punishmentModal", type.getName()), type + " Parameter").addActionRows(inputs);
            event.replyModal(modal.build()).complete();
        } else if (id.split(";")[2].split(",")[1].equals("Exit")) {
            event.editComponents().queue();
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        if (!modalId.contains(",")) return;
        String[] modalSplitId = modalId.split(",");
        if (!modalSplitId[1].equals("punishmentModal")) return;
        if (event.getGuild() == null) return;
        PunishmentType type = PunishmentType.getByName(modalSplitId[0]);

        JSONObject config = controller.getSpecificGuildConfig(event.getGuild(), GuildConfigType.MAIN);
        JSONObject punishments = config.getJSONObject("punishments");
        JSONObject psm = punishments.getJSONObject(type.getName());

        String errorString = "";

        switch (type) {
            case WARN -> {
                ModalMapping enabled = event.getValue("warn,enabled");
                String enabledString;
                if (enabled == null) {
                    enabledString = "false";
                    errorString = "Enabled in Warn";
                } else {
                    enabledString = enabled.getAsString();
                }
                psm.put("active", enabledString.equals("true"));

                ModalMapping role = event.getValue("warn,role");
                String roleId;
                if (role == null) {
                    roleId = psm.getString("role-id");
                    errorString = "Role in Warn";
                } else {
                    roleId = role.getAsString();
                }
                psm.put("role-id", roleId);
            }
            case MUTE -> {
                ModalMapping enabled = event.getValue("mute,enabled");
                String enabledString;
                if (enabled == null) {
                    enabledString = "false";
                    errorString = "Enabled in Mute";
                } else {
                    enabledString = enabled.getAsString();
                }
                psm.put("active", enabledString.equals("true"));

                ModalMapping role = event.getValue("mute,role");
                String roleId;
                if (role == null) {
                    roleId = psm.getString("role-id");
                    errorString = "Role in Mute";
                } else {
                    roleId = role.getAsString();
                }
                psm.put("role-id", roleId);
            }
            case KICK -> {
                ModalMapping reason = event.getValue("kick,reason");
                String reasonString;
                if (reason == null) {
                    reasonString = psm.getString("reason");
                    errorString = "Reason in Kick";
                } else {
                    reasonString = reason.getAsString();
                }
                psm.put("standard-reason", reasonString);
            }
            case BAN -> {
                ModalMapping reason = event.getValue("kick,reason");
                String reasonString;
                if (reason == null) {
                    reasonString = psm.getString("reason");
                    errorString = "Reason in Ban";
                } else {
                    reasonString = reason.getAsString();
                }
                psm.put("standard-reason", reasonString);

                ModalMapping days = event.getValue("ban,days");
                String daysString;
                if (days == null) {
                    daysString = psm.getString("standard-deleted-days");
                    errorString = "Days in Ban";
                } else {
                    daysString = days.getAsString();
                }
                psm.put("standard-deleted-days", daysString);
            }
            case TIMEOUT -> {
                ModalMapping reason = event.getValue("timeout,reason");
                String reasonString;
                if (reason == null) {
                    reasonString = psm.getString("reason");
                    errorString = "Reason in Timeout";
                } else {
                    reasonString = reason.getAsString();
                }
                psm.put("standard-reason", reasonString);

                ModalMapping duration = event.getValue("timeout,duration");
                String durationString = "";
                Integer durationInt;
                if (duration == null){
                    durationString = psm.getString("standard-duration");
                    durationInt = 2;
                    errorString = "Duration in Timeout";
                }else {
                    durationInt = Integer.parseInt(duration.getAsString());
                }

                ModalMapping timeUnit = event.getValue("timeout,timeunit");
                String timeUnitString;
                if (timeUnit == null) {
                    timeUnitString = psm.getString("standard-time-unit");
                    errorString = "TimeUnit in Timeout";
                } else {
                    String s = timeUnit.getAsString();
                    if (s.equalsIgnoreCase("seconds")) {
                        timeUnitString = "seconds";
                        if (durationInt > 60*60*24*27){
                            timeUnitString = "minuets";
                            durationInt = 2;
                            errorString = "Please check the duration";
                        }
                        durationString = String.valueOf(durationInt);
                    } else if (s.equalsIgnoreCase("minuets")) {
                        timeUnitString = "minuets";
                        if (durationInt > 60*24*27){
                            timeUnitString = "minuets";
                            durationInt = 2;
                            errorString = "Please check the duration";
                        }
                        durationString = String.valueOf(durationInt);
                    } else if (s.equalsIgnoreCase("hours")) {
                        timeUnitString = "hours";
                        if (durationInt > 24*27){
                            timeUnitString = "minuets";
                            durationInt = 2;
                            errorString = "Please check the duration";
                        }
                        durationString = String.valueOf(durationInt);
                    } else if (s.equalsIgnoreCase("days")) {
                        timeUnitString = "days";
                        if (durationInt > 27){
                            timeUnitString = "minuets";
                            durationInt = 2;
                            errorString = "Please check the duration";
                        }
                        durationString = String.valueOf(durationInt);
                    } else {
                        timeUnitString = "minuets";
                        durationInt = 2;
                        durationString = String.valueOf(durationInt);
                        errorString = "Please enter a valid TimeUnit";
                    }
                }
                psm.put("standard-duration",durationString);
                psm.put("standard-time-unit", timeUnitString);
            }default -> {
                event.reply("No valid Punishment").setEphemeral(true).queue();
                return;
            }
        }
        if (!errorString.isEmpty()){
            event.reply(errorString).setEphemeral(true).queue();
            return;
        }

        punishments.put(type.getName(),psm);
        config.put("punishments",punishments);
        controller.writeInSpecificGuildConfig(event.getGuild(), GuildConfigType.MAIN,config);

        event.reply("Parameters from " + type + " successfully edit").setEphemeral(true).queue();
    }
}
