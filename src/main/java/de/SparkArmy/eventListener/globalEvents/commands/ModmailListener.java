package de.SparkArmy.eventListener.globalEvents.commands;

import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.LoggingMarker;
import de.SparkArmy.utils.MainUtil;
import de.SparkArmy.utils.jda.FileHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ModmailListener extends CustomEventListener {


    private final File directory = FileHandler.getDirectoryInUserDirectory("botstuff/modmail");


    // Modal Interactions
    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().contains(";")) return;
        String modalId = event.getModalId().split(";")[0];
        switch (modalId){
            case "modmail" -> userStartMessage(event);
            case "modmailReply" -> userReplyMessage(event);
            default -> {}
        }

    }

    private void userReplyMessage(@NotNull ModalInteractionEvent event){
        event.reply("Will you add attachments?").setEphemeral(true).setComponents(ActionRow.of(
                        Button.primary("modmailReplyAttachmentsYes","Yes"),
                        Button.primary("modmailReplyAttachmentsNo","No")))
                .queue(x->waiter.waitForEvent(ButtonInteractionEvent.class,f-> event.getUser().equals(f.getUser())
                                && (f.getComponentId().equals("modmailReplyAttachmentsYes") || f.getComponentId().equals("modmailReplyAttachmentsNo")),
                        f->{
                            if (f.getComponentId().equals("modmailReplyAttachmentsNo")){
                                sendReplyEmbed(event);
                                x.editOriginalComponents().queue();
                            }else {
                                x.editOriginalComponents().queue();
                                f.reply("Push the button when you sent all attachments")
                                        .addActionRow(Button.success("modmailReplyAttachmentsOk","Ok"))
                                        .setEphemeral(true).queue(y->
                                        waiter.waitForEvent(ButtonInteractionEvent.class,g->g.getComponentId().equals("modmailReplyAttachmentsOk") && g.getUser().equals(f.getUser()),g->{
                                            g.editComponents().queue();
                                            sendReplyEmbed(event);
                                        }));
                            }
                        },5,TimeUnit.MINUTES,()->{
                            x.editOriginalComponents().queue();
                            sendReplyEmbed(event);
                            event.reply("Your time is over").setEphemeral(true).queue();
                        }));
    }

    private void userStartMessage(@NotNull ModalInteractionEvent event){
        String modalId = event.getModalId();
        String[] strings = modalId.split(";");
        if (Arrays.stream(strings).filter("modmail"::equals).toList().isEmpty()) return;
        @NonNls String idExtension = strings[1];
        if (null == directory) {
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        List<File> fileList = new ArrayList<>();
        if (null != directory.listFiles())
            fileList = Arrays.stream(Objects.requireNonNull(directory.listFiles())).filter(f -> f.getName().equals(idExtension + ".json")).toList();
        if (fileList.isEmpty()) {
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        JSONObject modalData = new JSONObject() {{
            put("topic", Objects.requireNonNull(event.getValue("topic;" + idExtension)).getAsString());
            put("body", Objects.requireNonNull(event.getValue("body;" + idExtension)).getAsString());
        }};
        if (!FileHandler.writeValuesInFile(fileList.get(0).getAbsolutePath(), modalData)) {
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }


        SelectMenu.Builder guilds = SelectMenu.create("modmailGuildPicker;" + idExtension);

        event.getJDA().getGuilds().forEach(g -> guilds.addOption(g.getName(), g.getId()));
        if (guilds.build().getOptions().isEmpty()){
            event.reply("All server were you member have disabled the modmail-feature").setEphemeral(true).queue();
            return;
        }
        event.reply("Please select the target guild").addActionRow(guilds.build()).setEphemeral(true).queue();
    }


    // Select menu interactions
    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        String menuName = event.getComponentId();
        if (!menuName.contains(";")) return;
        String[] strings = menuName.split(";");
        if (Arrays.stream(strings).filter("modmailGuildPicker"::equals).toList().isEmpty()) return;
        if (null == directory) {
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        List<File> fileList = new ArrayList<>();
        if (null != directory.listFiles())
            fileList = Arrays.stream(Objects.requireNonNull(directory.listFiles())).filter(f -> f.getName().equals(strings[1] + ".json")).toList();
        if (fileList.isEmpty()) {
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        JSONObject modalData = new JSONObject(Objects.requireNonNull(FileHandler.getFileContent(fileList.get(0).getAbsolutePath())));

        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(event.getUser().getAsTag(), null, event.getUser().getAvatarUrl());
        builder.setTitle(modalData.getString("topic"));
        builder.setDescription(modalData.getString("body"));

        //noinspection ResultOfMethodCallIgnored
        fileList.get(0).delete();
        Guild guild = MainUtil.jda.getGuildById(event.getValues().get(0));

        event.reply("Will you sent attachments to this server?").setEphemeral(true)
                .addActionRow(Button.primary("modmailYes", "Yes"))
                .addActionRow(Button.primary("modmailNo", "No")).queue(x ->
                        waiter.waitForEvent(ButtonInteractionEvent.class, f -> f.getUser().getId().equals(event.getUser().getId()), f -> {
                            if (f.getComponentId().equals("modmailNo")) {
                                x.editOriginal("Your message was sent to the server").queue();
                                x.editOriginalComponents().queue();
                                sendStartEmbedToServer(builder, guild, event.getUser(), event);
                            }

                            if (f.getComponentId().equals("modmailYes")) {
                                f.reply("Please sent the required Attachments in this Channel and click the button below")
                                        .setEphemeral(true)
                                        .addActionRow(Button.success("modmailOk", "OK"))
                                        .queue();
                                waiter.waitForEvent(ButtonInteractionEvent.class, g -> g.getUser().getId().equals(event.getUser().getId()), g -> {
                                    if (g.getComponentId().equals("modmailOk")) {
                                        x.editOriginalComponents().queue();
                                        sendStartEmbedToServer(builder, guild, event.getUser(), event);
                                        g.editComponents().queue();
                                    }
                                });
                            }
                        }, 5, TimeUnit.MINUTES, () -> {
                            x.editOriginal("Your time is over, the message will be send to the server").queue();
                            x.editOriginalComponents().queue();
                            sendStartEmbedToServer(builder, guild, event.getUser(), event);
                            event.editComponents().queue();
                        }));

    }

    // Button Interactions
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        switch (buttonId.split(";")[0]) {
            case "modmailReply" -> modmailReply(event);
            case "modmailChannelClose" -> modmailChannelClose(event);
            default -> {
            }
        }

    }
    private void modmailChannelClose(@NotNull ButtonInteractionEvent event) {
        String[] buttonId = event.getComponentId().split(";");
        event.reply("You are sure you want to close this channel")
                .addComponents(ActionRow.of(Button.danger("modmailCloseYes", "Yes"), Button.success("modmailCloseNo", "No")))
                .setEphemeral(true)
                .queue(x -> waiter.waitForEvent(ButtonInteractionEvent.class, f -> {
                    String id = f.getComponentId();
                    return id.equals("modmailCloseYes") || id.equals("modmailCloseNo");
                }, f -> {
                    String id = f.getComponentId();
                    if (id.equals("modmailCloseNo")) {
                        x.editOriginalComponents().queue();
                        return;
                    }

                    TextInput closeReason = TextInput.create("modmailCloseReason","Reason",TextInputStyle.SHORT)
                            .setMaxLength(100).build();

                    f.replyModal(Modal.create("modmailCloseModal","Close Channel").addActionRow(closeReason).build()).queue(y-> waiter.waitForEvent(ModalInteractionEvent.class, g->g.getModalId().equals("modmailCloseModal") && f.getUser().equals(g.getUser()), g->{
                        x.editOriginalComponents().queue();
                        g.reply("Channel will be deleted, started by " + f.getUser().getAsTag()).queue();
                        //noinspection ConstantConditions
                        String reason = g.getValue("modmailCloseReason").getAsString();
                        saveMessagesFromModmailChannel(event.getChannel().asTextChannel(), jda.getUserById(buttonId[2]),f.getUser(),reason).start();

                        // Send the user a message that the ticket was closed
                        try {
                            PrivateChannel privateChannel = Objects.requireNonNull(jda.getUserById(buttonId[2])).openPrivateChannel().complete();
                            new Thread(()-> privateChannel.getHistory().retrievePast(30).complete().forEach(m->{
                                if (m.getAuthor().equals(jda.getSelfUser()) && m.getEmbeds().isEmpty()){
                                    m.editMessageComponents().complete();
                                    try {
                                        TimeUnit.SECONDS.sleep(3);
                                    } catch (InterruptedException ignored) {}
                                }
                            })).start();
                            privateChannel.sendMessage("Your ticket was closed").queue(null,new ErrorHandler()
                                    .ignore(ErrorResponse.CANNOT_SEND_TO_USER));
                        }catch (NullPointerException | UnsupportedOperationException ignored){
                        }

                    }));

                }));
    }


    private void modmailReply(@NotNull ButtonInteractionEvent event) {
        String[] modmailId = event.getComponentId().split(";");
        TextInput modmailUserInput = TextInput.create("modmailReply", "Text", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Your answer or your addition")
                .setMinLength(10)
                .build();

        Modal.Builder modmailChannelReply = Modal.create(String.format("modmailReply;%s;%s",modmailId[1],modmailId[2]),"Modmail Reply");
        event.replyModal(modmailChannelReply.addActionRow(modmailUserInput).build()).queue();
    }


    // Method to send start message to specific server
    private void sendStartEmbedToServer(EmbedBuilder embedFromUser, Guild guild, User user, SelectMenuInteractionEvent e) {
        JSONObject config = this.controller.getSpecificGuildConfig(guild, GuildConfigType.MAIN);
        if (config.isNull("modmail")) {
           config = createConfig(config,guild);
           if (config == null) return;
        }
        Category modmailCategory;
        TextChannel modmailLogChannel;


        modmailCategory = guild.getCategoryById(config.getJSONObject("modmail").getString("category"));
        if (modmailCategory == null) {
            modmailCategory = createModmailCategory(guild,config);
            if (modmailCategory == null) return;
        }


        modmailLogChannel = guild.getTextChannelById(config.getJSONObject("modmail").getString("log-channel"));
        if (modmailLogChannel == null) {
            modmailLogChannel = createModmailTextChannel(guild,config,modmailCategory,"log");
            if (modmailLogChannel == null) return;
        }


        // Embed for logging
        EmbedBuilder loggingEmbed = new EmbedBuilder();
        loggingEmbed.setTitle("Ticket was opened");
        loggingEmbed.setDescription("A new Ticket was created:\n " + embedFromUser.build().getTitle());
        loggingEmbed.setColor(new Color(0, 255, 0));
        loggingEmbed.setFooter(user.getAsTag(), user.getEffectiveAvatarUrl());
        loggingEmbed.setTimestamp(LocalDateTime.now());
        modmailLogChannel.sendMessageEmbeds(loggingEmbed.build()).queue();


        // Create the question channel
        TextChannel modmailChannel = modmailCategory.createTextChannel(user.getAsTag()).complete();
        modmailChannel.getManager().sync().queue();
        modmailChannel.getManager().setTopic("This is a modmail channel, please not delete it").queue();




        modmailChannel.sendMessageEmbeds(embedFromUser.build()).
                setComponents(
                        ActionRow.of(Button.primary(String.format("modmailReply;%s;%s", modmailChannel.getId(), user.getId()), "Reply")), ActionRow.of(Button.danger(String.format("modmailChannelClose;%s;%s", modmailChannel.getId(), user.getId()), "Close")))
                .queue(x->x.pin().queue());


        String replyAttachments = getAttachmentStringsFromChannel(e.getMessageChannel(),e.getTimeCreated(),e.getUser()).toString();
        if (!replyAttachments.isEmpty()) {
            modmailChannel.sendMessage(replyAttachments).queue();
        }



        // Send the user a message, that the ticket was received
        String message = """
                Your ticket was received.
                Please use the button under this message to reply or adding a text.
                """;
        try {
            PrivateChannel privateChannel =  user.openPrivateChannel().complete();
            // Try to send a DM to the User
            privateChannel.sendMessage(message).setActionRow(Button.success(String.format("modmailReply;%s;%s", modmailChannel.getId(), user.getId()), "Reply")).queue();
            privateChannel.sendMessageEmbeds((embedFromUser.build())).queue();
        } catch (InsufficientPermissionException | IllegalArgumentException | UnsupportedOperationException ignored) {
            // Catch has the user disabled the DM from server members
            modmailChannel.delete().reason("The target user has direct-messages from server members disabled").queue();
            EmbedBuilder closeEmbed = new EmbedBuilder();
            closeEmbed.setTitle("Ticket " + user.getAsTag() + " closed");
            closeEmbed.setDescription("This ticket was automated closed, because the user have PN's disabled");
            closeEmbed.setFooter(jda.getSelfUser().getAsTag(), jda.getSelfUser().getEffectiveAvatarUrl());
            modmailLogChannel.sendMessageEmbeds(closeEmbed.build()).queue();
        }
    }

    // Method to send reply message from server to user or in the other direction
    private void sendReplyEmbed(@NotNull ModalInteractionEvent event){
        String[] modalIds = event.getModalId().split(";");
        TextChannel modmailChannel = jda.getTextChannelById(modalIds[1]);
        if (modmailChannel == null) return;
        Guild guild = modmailChannel.getGuild();
        User offender = jda.getUserById(modalIds[2]);
        if (offender == null ) return;
        User interactionUser = event.getUser();

        //noinspection ConstantConditions
        String replyString = event.getValue("modmailReply").getAsString();
        String replyAttachments = getAttachmentStringsFromChannel(event.getMessageChannel(),event.getTimeCreated(),interactionUser).toString();

        // Create and send the embed + optional attachments to the server
        EmbedBuilder modmailServerEmbed = new EmbedBuilder();
        modmailServerEmbed.setAuthor(interactionUser.getAsTag(),null,interactionUser.getEffectiveAvatarUrl());
        modmailServerEmbed.setTitle(interactionUser.equals(offender) ? "Reply from " + interactionUser.getAsTag() : "Reply to " + offender.getAsTag());
        modmailServerEmbed.setDescription(replyString);

        var ref = new Object() {
            String message;
        };
        modmailChannel.sendMessageEmbeds(modmailServerEmbed.build()).queue(x-> ref.message = x.getId());


        // Create and send the embed + optional attachment to the user
        EmbedBuilder modmailUserEmbed = new EmbedBuilder();
        modmailUserEmbed.setTitle(interactionUser.equals(offender) ? "Reply to " + guild.getName() : "Reply from " + guild.getName());
        modmailUserEmbed.setDescription(replyString);

        PrivateChannel privateChannel =  offender.openPrivateChannel().complete();
        privateChannel.sendMessageEmbeds(modmailUserEmbed.build()).queue(null,new ErrorHandler()
                .handle(ErrorResponse.CANNOT_SEND_TO_USER,e->{
                    modmailChannel.deleteMessageById(ref.message).queue();
                    modmailChannel.sendMessage("This user had disabled private messages, channel will be closed").queue();
                    saveMessagesFromModmailChannel(modmailChannel,offender,interactionUser,"User has disabled DM's").start();
                    new Thread(()-> privateChannel.getHistory().retrievePast(30).complete().forEach(m->{
                        if (m.getAuthor().equals(jda.getSelfUser()) && m.getEmbeds().isEmpty()){
                            m.editMessageComponents().complete();
                            try {
                                TimeUnit.SECONDS.sleep(3);
                            } catch (InterruptedException ignored) {}
                        }
                    })).start();
                }));
        if (replyAttachments.isEmpty()) return;
        if (event.getUser().equals(offender)){
            modmailChannel.sendMessage(replyAttachments).complete();
        }else {
            offender.openPrivateChannel().complete().sendMessage(replyAttachments).queue(null,new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
        }
    }

    // Method and Variables to delete old files
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final File staticDirectory = FileHandler.getDirectoryInUserDirectory("botstuff/modmail");

    public static void deleteOldFiles() {
        if (Objects.requireNonNull(staticDirectory).listFiles() == null) return;
        Objects.requireNonNull(FileHandler.getFilesInDirectory(staticDirectory)).forEach(file -> {
            String name = file.getName().split(",")[1].split("\\.")[0];
            if (LocalDateTime.parse(name, formatter).plusMinutes(10).isBefore(LocalDateTime.parse(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis())), formatter))) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        });
    }


    // Helper Methods

    private boolean messagesInTextChannel(TextChannel channel) {
        return new MessageHistory(channel).retrievePast(100).complete().size() > 0;
    }

    private @NotNull Thread saveMessagesFromModmailChannel(TextChannel channel, User modmailUser, User moderator,String reason) {
        return new Thread(() -> {
            List<Message> messageList = new ArrayList<>();
                while (messagesInTextChannel(channel)) {
                    List<Message> messages = new MessageHistory(channel).retrievePast(100).complete().stream().toList();
                    messages.forEach(m -> {
                        try {
                            messageList.add(m);
                            m.delete().complete();
                            TimeUnit.SECONDS.sleep(4);
                        } catch (Exception ignored) {
                        }
                    });
                }

            Collections.reverse(messageList);

            // Create and get a archive channel
            Guild guild = channel.getGuild();
            JSONObject config = controller.getSpecificGuildConfig(guild, GuildConfigType.MAIN);
            // Create a new entry in config if the json object null
            if (config.isNull("modmail")) {
                config = createConfig(config, guild);
                if (config == null) return;
            }

            JSONObject modmail = config.getJSONObject("modmail");

            Category modmailCategory;
            TextChannel modmailArchiveChannel;
            TextChannel modmailLogChannel;

            if (modmail.isNull("category")) {
                modmailCategory = createModmailCategory(guild, config);
                if (modmailCategory == null) return;
            } else {
                modmailCategory = guild.getCategoryById(modmail.getString("category"));
                if (modmailCategory == null) {
                    modmailCategory = createModmailCategory(guild, config);
                    if (modmailCategory == null) return;
                }
            }

            if (modmail.isNull("archive-channel")) {
                modmailArchiveChannel = createModmailTextChannel(guild, config, modmailCategory, "archive-channel");
                if (modmailArchiveChannel == null) return;
            } else {
                modmailArchiveChannel = guild.getTextChannelById(modmail.getString("archive-channel"));
                if (modmailArchiveChannel == null) {
                    modmailArchiveChannel = createModmailTextChannel(guild, config, modmailCategory, "archive-channel");
                    if (modmailArchiveChannel == null) return;
                }
            }

            if (modmail.isNull("log-channel")) {
                modmailLogChannel = createModmailTextChannel(guild, config, modmailCategory, "log");
                if (modmailLogChannel == null) return;
            } else {
                modmailLogChannel = guild.getTextChannelById(modmail.getString("log-channel"));
                if (modmailLogChannel == null) {
                    modmailLogChannel = createModmailTextChannel(guild, config, modmailCategory, "log");
                    if (modmailLogChannel == null) return;
                }
            }

            // Send the messages in a thread in modmail archive

            channel.delete().reason(reason).complete();

            ThreadChannel threadChannel =  modmailArchiveChannel
                    .createThreadChannel(modmailUser.getAsTag() + "||"
                            + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date(System.currentTimeMillis()))).complete();


            messageList.forEach(m->{
                if (!m.getEmbeds().isEmpty() || !m.getContentRaw().isEmpty()){
                    threadChannel.sendMessage(MessageCreateData.fromMessage(m)).queue(x->x.editMessageComponents().queue());
                    try {
                        TimeUnit.SECONDS.sleep(4);
                    } catch (InterruptedException ignored) {
                    }
                }
            });

            // Send message in modmail log
            EmbedBuilder logEmbed = new EmbedBuilder();
            logEmbed.setTitle(channel.getName() + " was closed");
            logEmbed.setDescription("Reason: " + reason);
            logEmbed.setColor(new Color(255,0,0));
            logEmbed.setFooter(moderator.getAsTag(),moderator.getEffectiveAvatarUrl());
            logEmbed.setTimestamp(LocalDateTime.now());

            modmailLogChannel.sendMessageEmbeds(logEmbed.build()).queue();
            threadChannel.getManager().setArchived(true).queue();
        });
    }


    private @NotNull StringBuilder getAttachmentStringsFromChannel(@NotNull MessageChannel channel, OffsetDateTime timeCreated, User referenceUser){
        StringBuilder messageAttachmentsAsString = new StringBuilder();
        channel.getHistory().retrievePast(100).complete().forEach(m->{
            if (!m.getAttachments().isEmpty() && m.getAuthor().equals(referenceUser) && m.getTimeCreated().isAfter(timeCreated)){
                m.getAttachments().forEach(x->messageAttachmentsAsString.append(x.getUrl()).append("\n"));
            }
        });
        return messageAttachmentsAsString;
    }

    private @Nullable JSONObject createConfig(JSONObject config, @NotNull Guild guild){
        Collection<Permission> deniedPermissions = new ArrayList<>() {{
            add(Permission.VIEW_CHANNEL);
        }};
        Category modmailCategory;
        TextChannel modmailArchiveChannel;
        TextChannel modmailLogChannel;
        try {
            modmailCategory = guild.createCategory("MODMAIL").complete();
            modmailCategory.getManager().putRolePermissionOverride(guild.getPublicRole().getIdLong(), null, deniedPermissions).queue();
            modmailArchiveChannel = modmailCategory.createTextChannel("modmail-archive").complete();
            modmailLogChannel = modmailCategory.createTextChannel("modmail-log").complete();
            //noinspection ResultOfMethodCallIgnored
            modmailArchiveChannel.getManager().putRolePermissionOverride(guild.getPublicRole().getIdLong(), null, deniedPermissions);
            //noinspection ResultOfMethodCallIgnored
            modmailLogChannel.getManager().putRolePermissionOverride(guild.getPublicRole().getIdLong(),null,deniedPermissions);
        } catch (InsufficientPermissionException | IllegalArgumentException createCategoryExeption) {
            logger.warn(LoggingMarker.CONFIG,"The bot has no permissions to create a channel");
            return null;
        }

        JSONObject modmail = new JSONObject();
        modmail.put("category", modmailCategory.getId());
        modmail.put("archive-channel", modmailArchiveChannel.getId());
        modmail.put("log-channel",modmailLogChannel.getId());

        config.put("modmail", modmail);
        this.controller.writeInSpecificGuildConfig(guild, GuildConfigType.MAIN, config);

        return config;
    }

    private @Nullable Category createModmailCategory(@NotNull Guild guild, @NotNull JSONObject config){
        Category modmailCategory;
        try {
            // Try to create a new category
            modmailCategory = guild.createCategory("MODMAIL").complete();

            Collection<Permission> deniedPermissions = new ArrayList<>() {{
                add(Permission.VIEW_CHANNEL);
            }};

            modmailCategory.getManager().putRolePermissionOverride(guild.getPublicRole().getIdLong(), null, deniedPermissions).complete();
            JSONObject modmail = config.getJSONObject("modmail");
            modmail.put("category", modmailCategory.getId());
            config.put("modmail", modmail);
            this.controller.writeInSpecificGuildConfig(guild, GuildConfigType.MAIN, config);
            return modmailCategory;
        } catch (IllegalArgumentException | InsufficientPermissionException categoryCreateExeption) {
            logger.warn(LoggingMarker.CONFIG,"The bot has no permissions to create a channel");
            return null;
        }
    }

    private @Nullable TextChannel createModmailTextChannel(@NotNull Guild guild, @NotNull JSONObject config, @NotNull Category modmailCategory, String name){
        TextChannel modmailChannel;
        try {
            Collection<Permission> deniedPermissions = new ArrayList<>() {{
                add(Permission.VIEW_CHANNEL);
            }};
            // Try to create a new channel
            modmailChannel = modmailCategory.createTextChannel("modmail-" + name).complete();
            modmailChannel.getManager().putRolePermissionOverride(guild.getPublicRole().getIdLong(),null,deniedPermissions).queue();
            JSONObject modmail = config.getJSONObject("modmail");
            modmail.put(name + "-channel", modmailChannel.getId());
            config.put("modmail", modmail);
            this.controller.writeInSpecificGuildConfig(guild, GuildConfigType.MAIN, config);
            return modmailChannel;
        } catch (InsufficientPermissionException | IllegalArgumentException channelCreateExeption) {
            // Returns if the bot has no permissions
            logger.warn(LoggingMarker.CONFIG,"The bot has no permissions to create a channel");
            return null;
        }
    }


}