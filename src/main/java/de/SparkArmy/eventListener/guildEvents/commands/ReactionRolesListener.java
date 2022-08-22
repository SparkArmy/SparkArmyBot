package de.SparkArmy.eventListener.guildEvents.commands;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.ReactionRoleUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class ReactionRolesListener extends CustomEventListener {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().contains(";")) {
            createModalsFromStartEmbed(event);
            editNewEmbeds(event);
            finishReactionRoles(event);
        }
        ReactionRoleUtil.giveOrRemoveMemberRole(event);
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        if (!modalId.contains(";")) return;
        if (event.getGuild() == null) return;

        String[] splitModalId = modalId.split(";");
        if (splitModalId[0].contains(",")){
            switch (splitModalId[0].split(",")[0]) {
                case "messageChoiceModal" -> modalActionForNextModal(event, splitModalId);
                case "editHeaderModal" -> modalActionForEditHeaderModal(event, splitModalId);
                case "editRolesModal" -> modalActionForEditRoleModal(event, splitModalId);
            }
        }
        if (splitModalId[0].equals("newReactionRoleModal")){
            modalActionForCreateModal(event,splitModalId);
        }
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        editRolesList(event);
        deleteSelectRoleFromReactionRole(event);
        ReactionRoleUtil.giveOrRemoveMemberRole(event);

    }

    private void editRolesList(@NotNull SelectMenuInteractionEvent event){
        String menuId = event.getComponentId();
        if (!menuId.contains(";")) return;
        String[] splitMenuId = menuId.split(";");
        if (splitMenuId[0].contains(",") && splitMenuId[0].split(",")[0].equals("editRolesList")){
            if (event.getValues().isEmpty()) return;
            String id = event.getValues().get(0);

            File file = ReactionRoleUtil.getReactionRoleFileOrTempFile(splitMenuId[1],splitMenuId[0].split(",")[1],event.getGuild());
            if (file == null) {
                event.reply("Ups something went wrong").setEphemeral(true).queue();
                return;
            }
            if (!file.exists()){
                event.reply("Ups something went wrong").setEphemeral(true).queue();
                return;
            }

            JSONObject content = ReactionRoleUtil.getContent(file);

            if (content == null){
                event.reply("Ups something went wrong").setEphemeral(true).queue();
                return;
            }

            switch (id) {
                case "deleteRole" ->
                        ReactionRoleUtil.deleteRoleFromEmbed(event, splitMenuId[0].split(",")[1], splitMenuId[1]);
                case "return" -> {
                    if (event.getValues().isEmpty()) return;
                    String suffix = splitMenuId[1];
                    JSONObject fields = content.getJSONObject("fields");
                    String title = content.getString("title");
                    String description = content.getString("description");
                    String color = content.getString("color");
                    String[] splitColor = color.split(" ");
                    int r = Integer.parseInt(splitColor[0]);
                    int g = Integer.parseInt(splitColor[1]);
                    int b = Integer.parseInt(splitColor[2]);

                    // Not Editable
                    EmbedBuilder reactionRoleEmbed = new EmbedBuilder();
                    reactionRoleEmbed.setTitle(title);
                    reactionRoleEmbed.setDescription(description);
                    reactionRoleEmbed.setColor(new Color(r, g, b));
                    fields.keySet().forEach(x -> {
                        JSONObject field = fields.getJSONObject(x);
                        reactionRoleEmbed.addField(field.getString("roleName"), field.getString("roleDescription"), field.getBoolean("inline"));
                    });
                    Collection<Button> buttons;
                    if (splitMenuId[0].split(",")[1].equals("create")) {
                        buttons = new ArrayList<>() {{
                            add(Button.primary(String.format("editNewHeader;%s", suffix), "Edit Header"));
                            add(Button.primary(String.format("editNewRoles;%s", suffix), "Edit Roles"));
                            add(Button.success(String.format("finishRoles;%s", suffix), "Finish"));
                        }};
                    } else {
                        buttons = new ArrayList<>() {{
                            add(Button.primary(String.format("editHeader;%s", suffix), "Edit Header"));
                            add(Button.primary(String.format("editRoles;%s", suffix), "Edit Roles"));
                            add(Button.success(String.format("finishRoles;%s", suffix), "Finish"));
                        }};
                    }
                    event.editMessageEmbeds(reactionRoleEmbed.build()).setComponents(ActionRow.of(buttons)).queue();
                }
                default -> ReactionRoleUtil.createEditRolesEmbedOrModal(content, event, splitMenuId[0].split(",")[1]);
            }
        }
    }
    private void deleteSelectRoleFromReactionRole(@NotNull SelectMenuInteractionEvent event){
        String menuId = event.getComponentId();
        if (!menuId.contains(";")) return;
        String[] splitMenuId = menuId.split(";");
        if (splitMenuId[0].contains(",") && splitMenuId[0].split(",")[0].equals("selectRoleForDelete")) {
            if (event.getValues().isEmpty()) return;
            String id = event.getValues().get(0);
            String suffix = splitMenuId[1];

            File file = ReactionRoleUtil.getReactionRoleFileOrTempFile(splitMenuId[1], splitMenuId[0].split(",")[1], event.getGuild());
            if (file == null) {
                event.reply("Ups something went wrong").setEphemeral(true).queue();
                return;
            }
            if (!file.exists()) {
                event.reply("Ups something went wrong").setEphemeral(true).queue();
                return;
            }

            JSONObject content = ReactionRoleUtil.getContent(file);

            if (content == null) {
                event.reply("Ups something went wrong").setEphemeral(true).queue();
                return;
            }
            JSONObject fields = content.getJSONObject("fields");

            if (!id.equals("return")){
                fields.remove(id);
                content.put("fields",fields);
            }


            String title = content.getString("title");
            String description = content.getString("description");
            String color = content.getString("color");
            String[] splitColor = color.split(" ");
            int r = Integer.parseInt(splitColor[0]);
            int g = Integer.parseInt(splitColor[1]);
            int b = Integer.parseInt(splitColor[2]);

            // Not Editable
            EmbedBuilder reactionRoleEmbed = new EmbedBuilder();
            reactionRoleEmbed.setTitle(title);
            reactionRoleEmbed.setDescription(description);
            reactionRoleEmbed.setColor(new Color(r,g,b));

            if (!fields.isEmpty()) {
                fields.keySet().forEach(x -> {
                    JSONObject field = fields.getJSONObject(x);
                    reactionRoleEmbed.addField(field.getString("roleName"), field.getString("roleDescription"), field.getBoolean("inline"));
                });
            }

            Collection<Button> buttons;
            if (!fields.isEmpty()) {
                if (splitMenuId[0].split(",")[1].equals("create")) {
                    buttons = new ArrayList<>() {{
                        add(Button.primary(String.format("editNewHeader;%s", suffix), "Edit Header"));
                        add(Button.primary(String.format("editNewRoles;%s", suffix), "Edit Roles"));
                        add(Button.success(String.format("finishRoles;%s", suffix), "Finish"));
                    }};
                } else {
                    buttons = new ArrayList<>() {{
                        add(Button.primary(String.format("editHeader;%s", suffix), "Edit Header"));
                        add(Button.primary(String.format("editRoles;%s", suffix), "Edit Roles"));
                        add(Button.success(String.format("finishRoles;%s", suffix), "Finish"));
                    }};
                }
            }else {
                if (splitMenuId[0].split(",")[1].equals("create")) {
                    buttons = new ArrayList<>() {{
                        add(Button.primary(String.format("editNewHeader;%s", suffix), "Edit Header"));
                        add(Button.primary(String.format("editNewRoles;%s", suffix), "Edit Roles"));
                    }};
                } else {
                    buttons = new ArrayList<>() {{
                        add(Button.primary(String.format("editHeader;%s", suffix), "Edit Header"));
                        add(Button.primary(String.format("editRoles;%s", suffix), "Edit Roles"));
                    }};
                }
            }

            FileHandler.writeValuesInFile(file,content);
            event.editMessageEmbeds(reactionRoleEmbed.build()).setComponents(ActionRow.of(buttons)).queue();


        }
    }
    private void createModalsFromStartEmbed(@NotNull ButtonInteractionEvent event){
        String componentId = event.getComponentId();
        if (!componentId.contains(";")) return;
        String[] splitComponentId = componentId.split(";");
        if (!splitComponentId[0].contains(",")) return;
        if (!splitComponentId[1].equals(event.getUser().getId())) return;
        String[] id = splitComponentId[0].split(",");
        switch (id[0]) {
            case "reactionRolesStart" -> {
                switch (id[1]) {
                    case "create" -> ReactionRoleUtil.createEmbedOrModalByAction("create", event.getUser(), event);
                    case "edit" -> ReactionRoleUtil.createEmbedOrModalByAction("edit", event.getUser(), event);
                    case "delete" -> ReactionRoleUtil.createEmbedOrModalByAction("delete", event.getUser(), event);
                    default -> {
                    }
                }
            }
            case "reactionRolesNext" -> {
                TextInput channel = TextInput.create("reactionRoleChannelChoice", "Channel-ID", TextInputStyle.SHORT)
                        .setMaxLength(50)
                        .setPlaceholder("The channel-id")
                        .setRequired(true)
                        .build();
                TextInput message = TextInput.create("reactionRoleMessageChoice", "Message-ID", TextInputStyle.SHORT)
                        .setMaxLength(50)
                        .setPlaceholder("The message-id")
                        .setRequired(true)
                        .build();

                Modal messageChoiceModal = Modal.create(String.format("messageChoiceModal,%s;%s", id[1], splitComponentId[1]), "Embed Choice").addActionRows(
                        ActionRow.of(channel),
                        ActionRow.of(message)
                ).build();
                event.replyModal(messageChoiceModal).queue();
            }
            default -> {
            }
        }
    }
    private void editNewEmbeds(@NotNull ButtonInteractionEvent event){
        String componentId = event.getComponentId();
        if (!componentId.contains(";")) return;
        String[] splitComponentId = componentId.split(";");
        if (!splitComponentId[0].equals("editNewHeader") && !splitComponentId[0].equals("editNewRoles") && !splitComponentId[0].equals("editHeader") && !splitComponentId[0].equals("editRoles")) return;
        String suffix = splitComponentId[1];
        String[] splitSuffix = suffix.split(",");
        User user = jda.getUserById(splitSuffix[1]);
        if (user == null || !user.equals(event.getUser())) return;
        File file;
        if (!splitComponentId[0].equals("editNewHeader") && !splitComponentId[0].equals("editNewRoles")){
            file = ReactionRoleUtil.getReactionRoleFileOrTempFile(suffix,"edit",event.getGuild());
        }else {
            file = ReactionRoleUtil.getReactionRoleFileOrTempFile(suffix,"create",event.getGuild());
        }

        JSONObject content = ReactionRoleUtil.getContent(file);
        if (content == null){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }


        if (splitComponentId[0].equals("editNewHeader")){
            ReactionRoleUtil.createEditHeaderModal(content, event, "create");
        }else if (splitComponentId[0].equals("editNewRoles")) {
            ReactionRoleUtil.createEditRolesEmbedOrModal(content, event, "create");
        } if (splitComponentId[0].equals("editHeader")){
            ReactionRoleUtil.createEditHeaderModal(content, event, "edit");
        }else if (splitComponentId[0].equals("editRoles")) {
            ReactionRoleUtil.createEditRolesEmbedOrModal(content, event, "edit");
        }
    }
    private void finishReactionRoles(@NotNull ButtonInteractionEvent event){
        String componentId = event.getComponentId();
        if (!componentId.contains(";") || event.getGuild() == null) return;
        String[] splitComponentId = componentId.split(";");
        if (!splitComponentId[0].equals("finishRoles")) return;

        String message = componentId.split(";")[1].split(",")[0];

        File file;
        if (message.length() == 17){
            file = ReactionRoleUtil.getReactionRoleFileOrTempFile(splitComponentId[1],"create",event.getGuild());
        }else {
            file = ReactionRoleUtil.getReactionRoleFileOrTempFile(splitComponentId[1],"edit",event.getGuild());
        }
        if (file == null){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        JSONObject content = ReactionRoleUtil.getContent(file);
        if (content == null){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        String title = content.getString("title");
        String description = content.getString("description");
        String color = content.getString("color");
        String[] splitColor = color.split(" ");
        int r = Integer.parseInt(splitColor[0]);
        int g = Integer.parseInt(splitColor[1]);
        int b = Integer.parseInt(splitColor[2]);

        JSONObject fields = content.getJSONObject("fields");



        EmbedBuilder finalReactionRoleEmbed = new EmbedBuilder();
        finalReactionRoleEmbed.setTitle(title);
        finalReactionRoleEmbed.setDescription(description);
        finalReactionRoleEmbed.setColor(new Color(r,g,b));

        fields.keySet().forEach(x->{
            JSONObject field = fields.getJSONObject(x);
            finalReactionRoleEmbed.addField(field.getString("roleName"),field.getString("roleDescription"),field.getBoolean("inline"));
        });

        //noinspection ResultOfMethodCallIgnored
        file.delete();

        Collection<ActionRow> actionRows = new ArrayList<>();
        if ((long) fields.keySet().size() <= 4) {
            fields.keySet().forEach(x -> {
                JSONObject field = fields.getJSONObject(x);
                actionRows.add(ActionRow.of(Button.primary(x, field.getString("roleName"))));
            });
        }else {
            SelectMenu.Builder roles = SelectMenu.create("getReactionRoleList");
            fields.keySet().forEach(x -> {
                        JSONObject field = fields.getJSONObject(x);
                        roles.addOption(field.getString("roleName"),x);
            });
            actionRows.add(ActionRow.of(roles.build()));
        }



        if (message.length() == 17) {
            event.getChannel().sendMessageEmbeds(finalReactionRoleEmbed.build()).setComponents(actionRows).queue(x -> {
                String messageId = x.getId() + ".json";
                File directory = FileHandler.getDirectoryInUserDirectory("botstuff/reactionRoles/" + event.getGuild().getId());
                if (directory == null) {
                    x.editMessage("Something is wrong").queue();
                    return;
                }
                FileHandler.createFile(directory, messageId);
                File messageFile = FileHandler.getFileInDirectory(directory, messageId);
                FileHandler.writeValuesInFile(messageFile, content);
                event.editComponents().queue();
                event.reply("Embed was edit").setEphemeral(true).queue();
            });
        }else {
            event.getChannel().retrieveMessageById(message).complete().editMessageEmbeds(finalReactionRoleEmbed.build()).setComponents(actionRows).queue(x->{
                String messageId = x.getId() + ".json";
                File directory = FileHandler.getDirectoryInUserDirectory("botstuff/reactionRoles/" + event.getGuild().getId());
                if (directory == null) {
                    x.editMessage("Something is wrong").queue();
                    return;
                }
                FileHandler.createFile(directory, messageId);
                File messageFile = FileHandler.getFileInDirectory(directory, messageId);
                FileHandler.writeValuesInFile(messageFile, content);
                event.editMessage("The Embed was edit").queue();
            });
        }
    }
    private void modalActionForNextModal(@NotNull ModalInteractionEvent event, String @NotNull [] splitModalId){
        String choiceId = splitModalId[0].split(",")[1];
        ModalMapping channel = event.getValue("reactionRoleChannelChoice");
        if (channel == null) {
            event.reply("The channelId is null").setEphemeral(true).queue();
            return;
        }
        String channelId = channel.getAsString();

        ModalMapping message = event.getValue("reactionRoleMessageChoice");
        if (message == null) {
            event.reply("The messageId is null").setEphemeral(true).queue();
            return;
        }
        String messageId = message.getAsString();
        if (choiceId.equals("Edit")) {
            ReactionRoleUtil.sendEditEmbed(messageId, channelId, event);
        } else {
            ReactionRoleUtil.deleteReactionRoleEmbed(channelId, messageId, event);
        }
    }
    private void modalActionForEditHeaderModal(@NotNull ModalInteractionEvent event, String @NotNull [] splitModalId){
        String action = splitModalId[0].split(",")[1];
        String suffix = splitModalId[1];
        User user = jda.getUserById(suffix.split(",")[1]);
        if (user == null || !user.equals(event.getUser()) || event.getGuild() == null) return;

        File directory;
        if (action.equals("create")){
            directory = FileHandler.getDirectoryInUserDirectory("botstuff/reactionRoles/temp");
        }else {
            directory = FileHandler.getDirectoryInUserDirectory("botstuff/reactionRoles/" + event.getGuild().getId());
        }

        if (directory == null){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        File file;
        if (action.equals("create")){
            file = FileHandler.getFileInDirectory(directory,suffix + ".json");
        }else {
            file = FileHandler.getFileInDirectory(directory,suffix.split(",")[0] + ".json");
        }

        if (!file.exists()){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        String contentString = FileHandler.getFileContent(file);
        if (contentString == null) {
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        JSONObject content = new JSONObject(contentString);

        ModalMapping title = event.getValue("editTitle");
        ModalMapping description = event.getValue("editDescription");
        ModalMapping color = event.getValue("editColor");

        if (title == null || description == null || color == null){
            event.reply("Please write in all Fields a value").setEphemeral(true).queue();
            return;
        }

        String[] splitColor = color.getAsString().split(" ");
        if (splitColor.length != 3){
            event.reply("Please write the color in the right format").setEphemeral(true).queue();
            return;
        }
        int r = Integer.parseInt(splitColor[0]);
        int g = Integer.parseInt(splitColor[1]);
        int b = Integer.parseInt(splitColor[2]);

        content.put("title",title.getAsString());
        content.put("description",description.getAsString());
        content.put("color",color.getAsString());


        EmbedBuilder editHeaderEmbed = new EmbedBuilder();
        editHeaderEmbed.setTitle(title.getAsString());
        editHeaderEmbed.setDescription(description.getAsString());
        editHeaderEmbed.setColor(new Color(r,g,b));


        if (!content.isNull("fields") && !content.getJSONObject("fields").isEmpty()){
            JSONObject fields = content.getJSONObject("fields");
            fields.keySet().forEach(x->{
                JSONObject field = fields.getJSONObject(x);
                editHeaderEmbed.addField(field.getString("roleName"),field.getString("roleDescription"),field.getBoolean("inline"));
            });
        }
        FileHandler.writeValuesInFile(file,content);
        event.editMessageEmbeds(editHeaderEmbed.build()).queue();
    }
    private void modalActionForEditRoleModal(@NotNull ModalInteractionEvent event, String @NotNull [] splitModalId){
        String action = splitModalId[0].split(",")[1];
        String suffix = splitModalId[1];
        User user = jda.getUserById(suffix.split(",")[1]);
        if (user == null || !user.equals(event.getUser()) || event.getGuild() == null) return;

        File file = ReactionRoleUtil.getReactionRoleFileOrTempFile(suffix,action,event.getGuild());

        if (file == null){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        if (!file.exists()){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        String contentString = FileHandler.getFileContent(file);
        if (contentString == null) {
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        JSONObject content = new JSONObject(contentString);

        ModalMapping roleId = event.getValue("roleId");
        ModalMapping roleDescription = event.getValue("roleDescription");
        ModalMapping inline = event.getValue("fieldInline");

        if (roleId == null || roleDescription == null || inline == null){
            event.reply("Please write a value in all fields").setEphemeral(true).queue();
            return;
        }

        JSONObject fields;
        if (content.isNull("fields")){
            fields = new JSONObject();
        }else {
            fields = content.getJSONObject("fields");
        }

        Role role = event.getGuild().getRoleById(roleId.getAsString());
        if (role == null){
            event.reply("Please give a valid role-id").setEphemeral(true).queue();
            return;
        }

        fields.put(roleId.getAsString(),new JSONObject(){{
            put("roleName",role.getName());
            put("roleDescription",roleDescription.getAsString());
            put("inline",inline.getAsString().equals("true"));
        }});

        content.put("fields",fields);

        FileHandler.writeValuesInFile(file,content);

        String title = content.getString("title");
        String description = content.getString("description");
        String color = content.getString("color");
        String[] splitColor = color.split(" ");
        int r = Integer.parseInt(splitColor[0]);
        int g = Integer.parseInt(splitColor[1]);
        int b = Integer.parseInt(splitColor[2]);

        // Not Editable
        EmbedBuilder reactionRoleEmbed = new EmbedBuilder();
        reactionRoleEmbed.setTitle(title);
        reactionRoleEmbed.setDescription(description);
        reactionRoleEmbed.setColor(new Color(r,g,b));

        fields.keySet().forEach(x->{
            JSONObject field = fields.getJSONObject(x);
            reactionRoleEmbed.addField(field.getString("roleName"),field.getString("roleDescription"),field.getBoolean("inline"));
        });

        Collection<Button> buttons;
        if (action.equals("create")){
          buttons = new ArrayList<>(){{
                add(Button.primary(String.format("editNewHeader;%s",suffix),"Edit Header"));
                add(Button.primary(String.format("editNewRoles;%s",suffix),"Edit Roles"));
                add(Button.success(String.format("finishRoles;%s",suffix),"Finish"));
            }};
        }else {
            buttons = new ArrayList<>(){{
                add(Button.primary(String.format("editHeader;%s",suffix),"Edit Header"));
                add(Button.primary(String.format("editRoles;%s",suffix),"Edit Roles"));
                add(Button.success(String.format("finishRoles;%s",suffix),"Finish"));
            }};
        }


        event.editMessageEmbeds(reactionRoleEmbed.build()).setComponents(ActionRow.of(buttons)).queue();
    }
    private void modalActionForCreateModal(@NotNull ModalInteractionEvent event, String @NotNull [] splitModalId){
        String idExtension = splitModalId[1].split(",")[0];
        User user = jda.getUserById(splitModalId[1].split(",")[1]);
        if (user == null || !user.equals(event.getUser())) return;

        File directory = FileHandler.getDirectoryInUserDirectory("botstuff/reactionRoles/temp");
        if (directory == null){
            event.reply("Ups somthing went wrong").setEphemeral(true).queue();
            return;
        }
        if(directory.listFiles() == null) {
            event.reply("No creation-files exist, please create a new reaction-role").setEphemeral(true).queue();
            return;
        }

        File tempFile = FileHandler.getFileInDirectory(directory,idExtension + "," + user.getId() + ".json");
        JSONObject content = new JSONObject();

        ModalMapping title = event.getValue("newReactionRoleTitle");
        ModalMapping description = event.getValue("newReactionRoleDescription");
        ModalMapping color = event.getValue("newReactionRoleColor");
        if (title == null || description == null || color == null){
            event.reply("Please write in all Fields a value").setEphemeral(true).queue();
            return;
        }

        String[] splitColor = color.getAsString().split(" ");
        if (splitColor.length != 3){
            event.reply("Please write the color in the right format").setEphemeral(true).queue();
            return;
        }
        int r = Integer.parseInt(splitColor[0]);
        int g = Integer.parseInt(splitColor[1]);
        int b = Integer.parseInt(splitColor[2]);

        content.put("title",title.getAsString());
        content.put("description",description.getAsString());
        content.put("color",color.getAsString());

        EmbedBuilder createReactionRole = new EmbedBuilder();
        createReactionRole.setTitle(title.getAsString());
        createReactionRole.setDescription(description.getAsString());
        createReactionRole.setColor(new Color(r,g,b));

        MessageCreateBuilder createReactionRoleMessage = new MessageCreateBuilder();
        createReactionRoleMessage.addContent("Use the buttons below to edit this embed and add roles");
        createReactionRoleMessage.setEmbeds(createReactionRole.build());

        Collection<Button> buttons = new ArrayList<>(){{
            add(Button.primary(String.format("editNewHeader;%s,%s",idExtension,user.getId()),"Edit Header"));
            add(Button.primary(String.format("editNewRoles;%s,%s",idExtension,user.getId()),"Edit Roles"));
        }};

        FileHandler.writeValuesInFile(tempFile,content);
        event.reply(createReactionRoleMessage.build()).setComponents(ActionRow.of(buttons)).setEphemeral(true).queue();
    }
}