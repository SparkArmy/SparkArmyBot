package de.SparkArmy.eventListener.guildEvents.commands;

import de.SparkArmy.eventListener.CustomEventListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModerationConfigListener extends CustomEventListener {
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        if (!buttonId.contains(";")) return;
        if (event.getGuild() == null) return;

        // Check if button user equals event user
        if (!buttonId.split(";")[1].equals(event.getUser().getId())) return;

        if ("modConfigRoles".equals(buttonId.split(";")[0])) {
            modConfigRolesButtonEvent(event);
        }
    }

    private void modConfigRolesButtonEvent(@NotNull ButtonInteractionEvent event){
        MessageEmbed embed = embedOfRoleConfig(event.getGuild(),0);
        event.editMessageEmbeds(embed).setComponents(actionRowOfRoleConfig(event.getUser(),embed.getFields())).queue();
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        String menuId = event.getComponentId();
        if (!menuId.contains(";")) return;
        if (event.getGuild() == null) return;

        if (!menuId.split(";")[1].equals(event.getUser().getId())) return;

        if ("roleConfigSelectMenu".equals(menuId.split(";")[0])){
            modConfigSelectRoleEvent(event);
        }
    }

    private void modConfigSelectRoleEvent(@NotNull SelectMenuInteractionEvent event){
        String value = event.getValues().get(0);

        if (value.equals("add")){
            TextInput roleId = TextInput.create("roleId","Role Id", TextInputStyle.SHORT)
                    .setPlaceholder("The role Id")
                    .setRequired(true)
                    .setMinLength(5)
                    .build();

            Modal addRoleModal = Modal.create("modConfigAddRoleModal;" + event.getUser().getId(),"Add Moderation Role").addActionRow(roleId).build();

            event.replyModal(addRoleModal).queue();
            event.editSelectMenu(event.getSelectMenu()).queue();
        }else {
            JSONObject config = getGuildMainConfig(event.getGuild());
            JSONObject moderation = config.optJSONObject("moderation",new JSONObject());
            JSONArray roles = moderation.getJSONArray("roles");

            roles = new JSONArray(roles.toList().stream().filter(x->!x.equals(value)).toList());
            moderation.put("roles",roles);
            config.put("moderation",moderation);
            writeInGuildMainConfig(event.getGuild(),config);

            event.editMessage("The role was successfully removed").queue(x->{
                x.editOriginalComponents().queue();
                x.editOriginalEmbeds().queue();
            });
        }

    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        Guild guild = event.getGuild();

        if (!modalId.contains(";")) return;
        if (guild == null) return;

        if (!modalId.split(";")[1].equals(event.getUser().getId())) return;

        if ("modConfigAddRoleModal".equals(modalId.split(";")[0])){
            JSONObject config = getGuildMainConfig(guild);
            JSONObject moderation = config.optJSONObject("moderation",new JSONObject());

            //noinspection ConstantConditions
            Role role = event.getGuild().getRoleById(event.getValue("roleId").getAsString());
            if (role == null){
                event.reply("Please give a valid role-id").setEphemeral(true).queue();
                return;
            }

            JSONArray roles = moderation.isNull("roles") ? new JSONArray() : moderation.getJSONArray("roles");
            if (roles.toList().contains(role.getId())) {
                event.reply("This role already exist in this list").setEphemeral(true).queue();
                return;
            }

            moderation.append("roles",role.getId());
            config.put("moderation",moderation);
            writeInGuildMainConfig(event.getGuild(),config);

            event.editMessage("Role was success append").queue(x->{
                x.editOriginalComponents().queue();
                x.editOriginalEmbeds().queue();
            });
        }
    }

    @SuppressWarnings("SameParameterValue")
    private @NotNull MessageEmbed embedOfRoleConfig(Guild guild, int from){
        JSONObject config = getGuildMainConfig(guild);
        JSONObject moderation = config.optJSONObject("moderation",new JSONObject());

        List<MessageEmbed.Field> finalFields;
        List<MessageEmbed.Field> fields = new ArrayList<>();
        if (!moderation.isEmpty() && !moderation.isNull("roles") && !moderation.getJSONArray("roles").isEmpty()){
            moderation.getJSONArray("roles").forEach(roleId->{
                Role role = guild.getRoleById(roleId.toString());
                if (role == null) return;
                fields.add(new MessageEmbed.Field(role.getName(),role.getId(),false));
            });
        }
        finalFields = fields.subList(from, Math.min(fields.size(), 23));

        return new EmbedBuilder(){{
            setTitle("Moderation-Roles");
            if (finalFields.isEmpty()) setDescription("Please add a role with the \"Add Role\" option");
            else setDescription("Delete a role or add a role in the select menu");
            setColor(new Color(0x2C103D));
            if (!finalFields.isEmpty()) //noinspection ConstantConditions
                finalFields.forEach(x->addField(x.getName(),x.getValue(),!x.isInline()));
        }}.build();
    }

    private @NotNull ActionRow actionRowOfRoleConfig(@NotNull User user, @NotNull List<MessageEmbed.Field> fields){
        String userId = user.getId();

        SelectMenu.Builder selectMenu = SelectMenu.create("roleConfigSelectMenu;" + userId);

        selectMenu.addOption("Add Role","add");
        //noinspection ConstantConditions
        fields.forEach(x->selectMenu.addOption(x.getName(), x.getValue()));

        return ActionRow.of(selectMenu.build());
    }
}
