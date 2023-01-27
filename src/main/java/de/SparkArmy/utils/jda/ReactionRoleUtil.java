package de.SparkArmy.utils.jda;

import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReactionRoleUtil {

    public static void createEmbedOrModalByAction(@NotNull String action, User user, Event event){
        switch (action) {
            case "create"-> {
                TextInput title = TextInput.create("newReactionRoleTitle","Title", TextInputStyle.SHORT)
                        .setMaxLength(255)
                        .setPlaceholder("The title from reaction-role-embed")
                        .setRequired(true)
                        .build();

                TextInput description = TextInput.create("newReactionRoleDescription","Description",TextInputStyle.PARAGRAPH)
                        .setPlaceholder("The description from the reaction-role-embed")
                        .setRequired(true)
                        .build();

                TextInput color = TextInput.create("newReactionRoleColor","Color",TextInputStyle.SHORT)
                        .setPlaceholder("The color in the RGB-Format (f.E. 255 255 255")
                        .setMaxLength(12)
                        .setRequired(true)
                        .build();
                String extension =  new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date(System.currentTimeMillis()));
                File directory = FileHandler.getDirectoryInUserDirectory("botstuff/reactionRoles/temp");
                if (directory == null) return;
                FileHandler.createFile(directory, String.format("%s,%s.json",extension,user.getId()));

                Modal newModal = Modal.create("newReactionRoleModal;" + String.format("%s,%s",extension,user.getId()),"Create ReactionRole").addActionRows(
                        ActionRow.of(title),
                        ActionRow.of(description),
                        ActionRow.of(color))
                        .build();
                if (event.getClass().equals(SlashCommandInteractionEvent.class)){
                    new SlashCommandInteractionEvent(event.getJDA(), event.getResponseNumber(),((SlashCommandInteractionEvent) event).getInteraction())
                            .replyModal(newModal).queue();
                }else if (event.getClass().equals(ButtonInteractionEvent.class)){
                    new ButtonInteractionEvent(event.getJDA(), event.getResponseNumber(),((ButtonInteractionEvent) event).getInteraction())
                            .replyModal(newModal).queue();
                    ((ButtonInteractionEvent) event).getHook().editOriginalComponents().queue();
                }
            }
            case "edit" -> {
                EmbedBuilder editEmbed = new EmbedBuilder();
                editEmbed.setTitle("Edit ReactionRole");
                editEmbed.setDescription("""
            You will edit a ReactionRoleEmbed.
            Before you push the button below, copy the channel-id and message-id from the ReactionRoleEmbed you will edit.
            """);

                if (event.getClass().equals(SlashCommandInteractionEvent.class)) {
                    new SlashCommandInteractionEvent(event.getJDA(), event.getResponseNumber(), ((SlashCommandInteractionEvent) event).getInteraction())
                            .replyEmbeds(editEmbed.build()).addComponents(ActionRow.of(Button.primary("reactionRolesNext,Edit;" + user.getId(), "Next"))).setEphemeral(true).queue();
                }else if (event.getClass().equals(ButtonInteractionEvent.class)){
                    new ButtonInteractionEvent(event.getJDA(), event.getResponseNumber(),((ButtonInteractionEvent) event).getInteraction())
                            .editMessageEmbeds(editEmbed.build()).setComponents(ActionRow.of(Button.primary("reactionRolesNext,Edit;" + user.getId(), "Next"))).queue();
                }
            }
            case "delete" -> {
                    EmbedBuilder deleteEmbed = new EmbedBuilder();
                    deleteEmbed.setTitle("Delete ReactionRole");
                    deleteEmbed.setDescription("""
            You will delete a ReactionRoleEmbed.
            Before you push the button below, copy the channel-id and message-id from the ReactionRoleEmbed you will delete.
            """);

                    if (event.getClass().equals(SlashCommandInteractionEvent.class)) {
                        new SlashCommandInteractionEvent(event.getJDA(), event.getResponseNumber(), ((SlashCommandInteractionEvent) event).getInteraction())
                                .replyEmbeds(deleteEmbed.build()).addComponents(ActionRow.of(Button.danger("reactionRolesNext,Delete;" + user.getId(), "Next"))).setEphemeral(true).queue();
                    }else if (event.getClass().equals(ButtonInteractionEvent.class)){
                        new ButtonInteractionEvent(event.getJDA(), event.getResponseNumber(),((ButtonInteractionEvent) event).getInteraction())
                                .editMessageEmbeds(deleteEmbed.build()).setComponents(ActionRow.of(Button.danger("reactionRolesNext,Delete;" + user.getId(), "Next"))).queue();
                        ((ButtonInteractionEvent) event).editComponents().queue();
                    }
            }
        }
    }

    public static void sendEditEmbed(String messageId, String channelId, @NotNull Event event){
        if (event.getClass().equals(SlashCommandInteractionEvent.class)) {
            SlashCommandInteractionEvent slashEvent = new SlashCommandInteractionEvent(event.getJDA(), event.getResponseNumber(), ((SlashCommandInteractionEvent) event).getInteraction());
            MessageChannel messageChannel = ChannelUtil.rightChannel(slashEvent.getChannel());
            if (messageChannel == null) {
                slashEvent.reply("Please execute this command in a message channel").setEphemeral(true).queue();
                return;
            }
            Message message;

            message = messageChannel.retrieveMessageById(messageId).complete();

            if (message == null) {
                slashEvent.reply("The message is null").setEphemeral(true).queue();
                return;
            }

            if (message.getEmbeds().isEmpty()) {
                slashEvent.reply("This message not contains embeds").setEphemeral(true).queue();
                return;
            }

            if (slashEvent.getGuild() == null) return;

            File directory = FileHandler.getDirectoryInUserDirectory("botstuff/reactionRoles/" + slashEvent.getGuild().getId() + "/");
            if (directory == null){
                slashEvent.reply("Ups somthing went wrong, please try it again").setEphemeral(true).queue();
                return;
            }
            if (directory.listFiles() == null){
                slashEvent.reply("This server has no reaction roles, please create one with /reactionroles").setEphemeral(true).queue();
                return;
            }
            File file = FileHandler.getFileInDirectory(directory,messageId + ".json");
            if (!file.exists()){
                slashEvent.reply("This message is not an reaction-role-embed").setEphemeral(true).queue();
                return;
            }
            String userId = slashEvent.getUser().getId();
            String id = String.format("%s,%s",messageId,userId);
            Collection<Button> buttons;
            buttons = new ArrayList<>(){{
                add(Button.primary(String.format("editHeader;%s",id),"Edit Header"));
                add(Button.primary(String.format("editRoles;%s",id),"Edit Roles"));
                add(Button.success(String.format("finishRoles;%s",id),"Finish"));
            }};


            MessageEmbed reactionRoleEmbed = message.getEmbeds().get(0);
            MessageCreateBuilder editMessage = new MessageCreateBuilder();
            editMessage.addContent("This is the reaction-role-embed, you can edit this with the buttons below");
            editMessage.setEmbeds(reactionRoleEmbed);
            slashEvent.reply(editMessage.build()).addComponents(ActionRow.of(buttons)).setEphemeral(true).queue();
            return;
        }
        if (event.getClass().equals(ModalInteractionEvent.class)){
            ModalInteractionEvent modalEvent = new ModalInteractionEvent(event.getJDA(), event.getResponseNumber(), ((ModalInteractionEvent) event).getInteraction());
            GuildChannel guildChannel = MainUtil.jda.getGuildChannelById(channelId);
            if (guildChannel == null){
                modalEvent.reply("This is not a guild-channel").setEphemeral(true).queue();
                return;
            }
            MessageChannel messageChannel = ChannelUtil.rightChannel(guildChannel);
            if (messageChannel == null) {
                modalEvent.reply("Please execute this command in a message channel").setEphemeral(true).queue();
                return;
            }
            Message message;

            message = messageChannel.retrieveMessageById(messageId).complete();

            if (message == null) {
                modalEvent.reply("The message is null").setEphemeral(true).queue();
                return;
            }

            if (message.getEmbeds().isEmpty()) {
                modalEvent.reply("This message not contains embeds").setEphemeral(true).queue();
                return;
            }

            if (modalEvent.getGuild() == null) return;

            File directory = FileHandler.getDirectoryInUserDirectory("botstuff/reactionRoles/" + modalEvent.getGuild().getId() + "/");
            if (directory == null){
                modalEvent.reply("Ups somthing went wrong, please try it again").setEphemeral(true).queue();
                return;
            }
            if (directory.listFiles() == null){
                modalEvent.reply("This server has no reaction roles, please create one with /reactionroles").setEphemeral(true).queue();
                return;
            }
            File file = FileHandler.getFileInDirectory(directory,messageId + ".json");
            if (!file.exists()){
                modalEvent.reply("This message is not an reaction-role-embed").setEphemeral(true).queue();
                return;
            }


            MessageEmbed reactionRoleEmbed = message.getEmbeds().get(0);
            MessageCreateBuilder editMessage = new MessageCreateBuilder();
            editMessage.addContent("This is the reaction-role-embed, you can edit this with the buttons below");
            editMessage.setEmbeds(reactionRoleEmbed);

            String id = modalEvent.getModalId().split(";")[1];
            Collection<Button> buttons;
            buttons = new ArrayList<>(){{
                add(Button.primary(String.format("editHeader;%s,%s",messageId,id),"Edit Header"));
                add(Button.primary(String.format("editRoles;%s,%s",messageId,id),"Edit Roles"));
                add(Button.success(String.format("finishRoles;%s,%s",messageId,id),"Finish"));
            }};

            modalEvent.editMessage(MessageEditData.fromCreateData(editMessage.build())).setComponents(ActionRow.of(buttons)).queue();
        }
    }

    public static void giveOrRemoveMemberRole(@NotNull ButtonInteractionEvent event){
        String name = event.getMessage().getId() + ".json";
        if (event.getGuild() == null) return;
        File directory = FileHandler.getDirectoryInUserDirectory("botstuff/reactionRoles/" + event.getGuild().getId());

        if (directory == null){
            return;
        }
        File[] files = directory.listFiles();
        if (files == null){
            return;
        }

        if (Arrays.stream(files).filter(x->x.getName().equals(name)).toList().isEmpty()) return;

        Member member = event.getMember();
        if (member == null){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        Role role = event.getGuild().getRoleById(event.getComponentId());
        if (role == null){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        if (!member.getRoles().contains(role)){
            event.getGuild().addRoleToMember(member,role).reason("Add role from reaction-role-embed").queue();
            event.reply("You have the role \"" + role.getName() + "\" now").setEphemeral(true).queue();
        }else {
            event.getGuild().removeRoleFromMember(member,role).reason("Remove role from reaction-role-embed").queue();
            event.reply("You have the role \"" + role.getName() + "\" removed").setEphemeral(true).queue();
        }

    }

    public static void giveOrRemoveMemberRole(@NotNull StringSelectInteractionEvent event){
        String name = event.getMessage().getId() + ".json";
        if (event.getGuild() == null) return;
        File directory = FileHandler.getDirectoryInUserDirectory("botstuff/reactionRoles/" + event.getGuild().getId());

        if (directory == null){
            return;
        }

        if (directory.listFiles() == null){
            return;
        }

        File[] files = directory.listFiles();
        if (files == null){
            return;
        }

        if (Arrays.stream(files).filter(x->x.getName().equals(name)).toList().isEmpty()) return;

        Member member = event.getMember();
        if (member == null){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        Role role = event.getGuild().getRoleById(event.getValues().get(0));
        if (role == null){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        SelectMenu menu = event.getSelectMenu().createCopy().build();
        if (!member.getRoles().contains(role)){
            event.getGuild().addRoleToMember(member,role).reason("Add role from reaction-role-embed").queue();
            event.reply("You have the role \"" + role.getName() + "\" now").setEphemeral(true).queue();
            event.editSelectMenu(menu).queue();
        }else {
            event.getGuild().removeRoleFromMember(member,role).reason("Add role from reaction-role-embed").queue();
            event.reply("You have the role \"" + role.getName() + "\" removed").setEphemeral(true).queue();
            event.editSelectMenu(menu).queue();
        }
    }



    public static void deleteReactionRoleEmbed(String channelId, String messageId, @NotNull Event event) {
        if (event.getClass().equals(SlashCommandInteractionEvent.class)) {
            SlashCommandInteractionEvent slashEvent = new SlashCommandInteractionEvent(event.getJDA(), event.getResponseNumber(), ((SlashCommandInteractionEvent) event).getInteraction());
            MessageChannel messageChannel = ChannelUtil.rightChannel(slashEvent.getChannel());

            if (messageChannel == null) {
                slashEvent.reply("Please execute this command in a message channel").setEphemeral(true).queue();
                return;
            }
            Message message;

            message = messageChannel.retrieveMessageById(messageId).complete();

            if (message == null) {
                slashEvent.reply("The message is null").setEphemeral(true).queue();
                return;
            }

            if (message.getEmbeds().isEmpty()) {
                slashEvent.reply("This message not contains embeds").setEphemeral(true).queue();
                return;
            }

            if (slashEvent.getGuild() == null) return;

            File directory = FileHandler.getDirectoryInUserDirectory("botstuff/reactionRoles/" + slashEvent.getGuild().getId() + "/");
            if (directory == null) {
                slashEvent.reply("Ups somthing went wrong, please try it again").setEphemeral(true).queue();
                return;
            }
            if (directory.listFiles() == null) {
                slashEvent.reply("This server has no reaction roles, please create one with /reactionroles").setEphemeral(true).queue();
                return;
            }
            File file = FileHandler.getFileInDirectory(directory, messageId + ".json");
            if (!file.exists()) {
                slashEvent.reply("This message is not an reaction-role-embed").setEphemeral(true).queue();
                return;
            }

            messageChannel.deleteMessageById(messageId).complete();
            //noinspection ResultOfMethodCallIgnored
            file.delete();

            slashEvent.reply("The reaction-role-embed was deleted").setEphemeral(true).queue();
            return;
        }
        if (event.getClass().equals(ModalInteractionEvent.class)) {
            ModalInteractionEvent modalEvent = new ModalInteractionEvent(event.getJDA(), event.getResponseNumber(), ((ModalInteractionEvent) event).getInteraction());
            GuildChannel guildChannel = MainUtil.jda.getGuildChannelById(channelId);
            if (guildChannel == null) {
                modalEvent.reply("This is not a guild-channel").setEphemeral(true).queue();
                return;
            }
            MessageChannel messageChannel = ChannelUtil.rightChannel(guildChannel);
            if (messageChannel == null) {
                modalEvent.reply("Please execute this command in a message channel").setEphemeral(true).queue();
                return;
            }
            Message message;

            message = messageChannel.retrieveMessageById(messageId).complete();

            if (message == null) {
                modalEvent.reply("The message is null").setEphemeral(true).queue();
                return;
            }

            if (message.getEmbeds().isEmpty()) {
                modalEvent.reply("This message not contains embeds").setEphemeral(true).queue();
                return;
            }

            if (modalEvent.getGuild() == null) return;

            File directory = FileHandler.getDirectoryInUserDirectory("botstuff/reactionRoles/" + modalEvent.getGuild().getId() + "/");
            if (directory == null) {
                modalEvent.reply("Ups somthing went wrong, please try it again").setEphemeral(true).queue();
                return;
            }
            if (directory.listFiles() == null) {
                modalEvent.reply("This server has no reaction roles, please create one with /reactionroles").setEphemeral(true).queue();
                return;
            }
            File file = FileHandler.getFileInDirectory(directory, messageId + ".json");
            if (!file.exists()) {
                modalEvent.reply("This message is not an reaction-role-embed").setEphemeral(true).queue();
                return;
            }

            messageChannel.deleteMessageById(messageId).complete();
            //noinspection ResultOfMethodCallIgnored
            file.delete();

            modalEvent.editComponents().queue();
            modalEvent.reply("The reaction-role-embed was deleted").queue();

        }
    }

    public static void deleteOldTempFiles(){
        File directory = FileHandler.getDirectoryInUserDirectory("botstuff/reactionRoles/temp");
        if (directory == null) return;
        if (directory.listFiles() == null) return;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        Arrays.stream(Objects.requireNonNull(directory.listFiles())).toList().forEach(file->{
            String fileTime = file.getName().split(",")[0];
            String actualTime = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date(System.currentTimeMillis()));
            boolean bool = LocalDateTime.parse(fileTime,formatter).plusMinutes(10).isBefore(LocalDateTime.parse(actualTime,formatter));
            if (bool){
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }

        });
    }

    public static void createEditHeaderModal(@NotNull JSONObject content, ButtonInteractionEvent event,String action){
        if (content.isEmpty()){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        String suffix = event.getComponentId().split(";")[1];
        String titleString = content.isNull("title") ? "No title found" : content.getString("title");
        String descriptionString = content.isNull("description") ? "No description found" : content.getString("description");
        String colorString = content.isNull("color") ? "No color found" : content.getString("color");

        TextInput title = TextInput.create("editTitle","Title", TextInputStyle.SHORT)
                .setMaxLength(255)
                .setValue(titleString)
                .setPlaceholder("The title from reaction-role-embed")
                .setRequired(true)
                .build();

        TextInput description = TextInput.create("editDescription","Description",TextInputStyle.PARAGRAPH)
                .setPlaceholder("The description from the reaction-role-embed")
                .setValue(descriptionString)
                .setRequired(true)
                .build();

        TextInput color = TextInput.create("editColor","Color",TextInputStyle.SHORT)
                .setPlaceholder("The color in the RGB-Format (f.E. 255 255 255")
                .setValue(colorString)
                .setMaxLength(12)
                .setRequired(true)
                .build();

        Modal editModal = Modal.create("editHeaderModal," + action + ";" + suffix,"Edit Header").addActionRows(
                ActionRow.of(title),
                ActionRow.of(description),
                ActionRow.of(color))
                .build();
        event.replyModal(editModal).queue();
    }

    public static void createEditRolesEmbedOrModal(@NotNull JSONObject content, @NotNull Event event, String action){
        if (event.getClass().equals(ButtonInteractionEvent.class)){
            ButtonInteractionEvent buttonEvent;
           buttonEvent = new ButtonInteractionEvent(event.getJDA(), event.getResponseNumber(),((ButtonInteractionEvent) event).getInteraction());

            if (content.isEmpty()){

                buttonEvent.reply("Ups something went wrong").setEphemeral(true).queue();
                return;
            }
            String suffix = buttonEvent.getComponentId().split(";")[1];

            if (content.isNull("fields") || content.getJSONObject("fields").isEmpty()){
                buttonEvent.replyModal(editRolesModal(action,suffix,null,null)).queue();
                return;
            }

            EmbedBuilder roleEditChoiceEmbed = new EmbedBuilder();
            roleEditChoiceEmbed.setTitle("Edit Roles");
            roleEditChoiceEmbed.setDescription("""
                Choice the role you will edit.
                To create a new Role choice the option "Add new Role"
                To delete a role choice the option " Delete Role \"""");
            roleEditChoiceEmbed.setColor(new Color(0x941D9E));

            StringSelectMenu.Builder roles = StringSelectMenu.create("editRolesList," + action + ";" + suffix);

            if (!content.isNull("fields")) {
                content.getJSONObject("fields").keySet().forEach(x -> {
                    JSONObject field = content.getJSONObject("fields").getJSONObject(x);
                    roles.addOption(field.getString("roleName"), x);
                });
            }

            roles.addOption("Delete Role","deleteRole");
            roles.addOption("Return","return");
            if (roles.getOptions().size() < 24){
                roles.addOption("Add Role","addRole");
            }

            buttonEvent.editMessageEmbeds(roleEditChoiceEmbed.build()).setComponents(ActionRow.of(roles.build())).queue();
        }else if (event.getClass().equals(StringSelectInteractionEvent.class)) {
            StringSelectInteractionEvent selectEvent;
            selectEvent = new StringSelectInteractionEvent(event.getJDA(), event.getResponseNumber(), ((StringSelectInteractionEvent) event).getInteraction());
            if (content.isEmpty()) {
                selectEvent.reply("Ups something went wrong").setEphemeral(true).queue();
                return;
            }
            String suffix = selectEvent.getComponentId().split(";")[1];
            if (selectEvent.getValues().get(0).equals("addRole")){
                selectEvent.replyModal(editRolesModal(action, suffix,null,null)).queue();
                return;
            }
            String value = selectEvent.getValues().get(0);
            JSONObject field = content.getJSONObject("fields").getJSONObject(value);
            selectEvent.replyModal(editRolesModal(action, suffix, field,value)).queue();
        }
    }

    private static @NotNull Modal editRolesModal(String action, String suffix, JSONObject field,String stringRoleId){
        TextInput.Builder roleId = TextInput.create("roleId","Role Id",TextInputStyle.SHORT)
                .setRequired(true)
                .setPlaceholder("The role-id from the target role");


        TextInput.Builder fieldDescription = TextInput.create("roleDescription","Role Description",TextInputStyle.PARAGRAPH)
                .setPlaceholder("The description of the reaction-role")
                .setRequired(true)
                .setMaxLength(1023);

        TextInput.Builder fieldInline = TextInput.create("fieldInline","Field Inline",TextInputStyle.SHORT)
                .setPlaceholder("true or false")
                .setRequiredRange(4,5)
                .setRequired(true)
                .setValue("false");

        if (field != null && stringRoleId != null){
            roleId.setValue(stringRoleId);
            fieldDescription.setValue(field.getString("roleDescription"));
            fieldInline.setValue(String.valueOf(field.getBoolean("inline")));
        }
        return Modal.create("editRolesModal," + action + ";" + suffix,action.equals("create") ? "Create" : "Edit" + "Role").addActionRows(
                ActionRow.of(roleId.build()),
                ActionRow.of(fieldDescription.build()),
                ActionRow.of(fieldInline.build())
        ).build();
    }

    public static @Nullable File getReactionRoleFileOrTempFile(String id, @NotNull String action, Guild guild){
        File directory;
        if (action.equals("create")){
            directory = FileHandler.getDirectoryInUserDirectory("botstuff/reactionRoles/temp");
        }else {
            directory = FileHandler.getDirectoryInUserDirectory("botstuff/reactionRoles/" + guild.getId());
        }

        if (directory == null){
            return null;
        }

        if (action.equals("create")){
            return FileHandler.getFileInDirectory(directory,id + ".json");
        }else {
            return FileHandler.getFileInDirectory(directory,id.split(",")[0] + ".json");
        }
    }

    public static @Nullable JSONObject getContent(File file){
        String contentString = FileHandler.getFileContent(file);
        if (contentString == null) return null;
        return new JSONObject(contentString);
    }

    public static void deleteRoleFromEmbed(@NotNull StringSelectInteractionEvent event, String action, String id){
        File file = getReactionRoleFileOrTempFile(id,action,event.getGuild());
        if (file == null){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        JSONObject content = getContent(file);
        if (content == null){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        StringSelectMenu.Builder selectRole = StringSelectMenu.create(String.format("selectRoleForDelete,%s;%s",action,id));

        JSONObject fields = content.getJSONObject("fields");

        if (fields.isEmpty()){
            event.reply("You can't delete roles were no exist").setEphemeral(true).queue();
            return;
        }

        fields.keySet().forEach(x->{
            JSONObject field = content.getJSONObject("fields").getJSONObject(x);
            selectRole.addOption(field.getString("roleName"),x);
        });

        selectRole.addOption("Return","return");

        EmbedBuilder selectRoleForDelete = new EmbedBuilder();
        selectRoleForDelete.setTitle("Delete ReactionRole from embed");
        selectRoleForDelete.setDescription("Select on reaction-role below or return with the value \"Return\"");

        event.editMessageEmbeds(selectRoleForDelete.build()).setComponents(ActionRow.of(selectRole.build())).queue();
    }
}
