package de.SparkArmy.eventListener.globalEvents;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ModmailListener extends CustomEventListener {


    private final File directory = FileHandler.getDirectoryInUserDirectory("botstuff/modmail");

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        if (!modalId.contains(";")) return;
        String[] strings = modalId.split(";");
        if (Arrays.stream(strings).filter("modmail"::equals).toList().isEmpty()) return;
        @NonNls String idExtension = strings[1];
        if (null == directory) {
            this.logger.warning("MODMAIL-ModalInteraction: directory is null");
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
            this.logger.warning("MODMAIL-ModalInteraction: can#t write values in file");
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }


        SelectMenu.Builder guilds = SelectMenu.create("modmailGuildPicker;" + idExtension);

        event.getJDA().getGuilds().forEach(g -> {
            JSONObject guildConfig = this.controller.getSpecificGuildConfig(g, "config.json");
            if (null != guildConfig) {
                if (!guildConfig.keySet().contains("command-permissions")) return;
                if (guildConfig.getJSONObject("command-permissions").getBoolean("modmail")) {
                    guilds.addOption(g.getName(), g.getId());
                }
            }
        });

        event.reply("Please select the target guild").addActionRow(guilds.build()).setEphemeral(true).queue();
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        String menuName = event.getComponentId();
        if (!menuName.contains(";")) return;
        String[] strings = menuName.split(";");
        if (Arrays.stream(strings).filter("modmailGuildPicker"::equals).toList().isEmpty()) return;
        if (null == directory) {
            this.logger.warning("MODMAIL-SelectMenuInteraction: directory is null");
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
                                event.editComponents().queue();
                                sendEmbedToServer(builder, guild, event.getUser(),event);
                            }

                            if (f.getComponentId().equals("modmailYes")) {
                                f.reply("Please sent the required Attachments in this Channel and click the button below")
                                        .setEphemeral(true)
                                        .addActionRow(Button.success("modmailOk", "OK"))
                                        .queue();
                                waiter.waitForEvent(ButtonInteractionEvent.class, g -> g.getUser().getId().equals(event.getUser().getId()), g -> {
                                    if (g.getComponentId().equals("modmailOk")) {
                                        x.editOriginalComponents().queue();
                                        sendEmbedToServer(builder, guild, event.getUser(),event);
                                        event.editComponents().queue();
                                        g.editComponents().queue();
                                    }
                                });
                            }
                        }, 5, TimeUnit.MINUTES, () -> {
                            x.editOriginal("Your time is over, the message will be send to the server").queue();
                            x.editOriginalComponents().queue();
                            sendEmbedToServer(builder, guild, event.getUser(),event);
                            event.editComponents().queue();
                        }));

    }

    // Method to send Message to specific server
    private void sendEmbedToServer(EmbedBuilder embedFromUser, Guild guild, User user, SelectMenuInteractionEvent e) {
        JSONObject config = this.controller.getSpecificGuildConfig(guild, "config.json");
        if (config.isNull("modmail")) {
            Category modmailCategory = guild.createCategory("MODMAIL").complete();
            // Denied Permissions for public Role
            // Admin will be change this
            Collection<Permission> deniedPermissions = new ArrayList<>() {{
                add(Permission.VIEW_CHANNEL);
            }};
            modmailCategory.getManager().putRolePermissionOverride(guild.getPublicRole().getIdLong(), null, deniedPermissions).queue();
            TextChannel modmailChannel = modmailCategory.createTextChannel("modmail-log").complete();
            //noinspection ResultOfMethodCallIgnored
            modmailChannel.getManager().putRolePermissionOverride(guild.getPublicRole().getIdLong(), null, deniedPermissions);

            JSONObject modmail = new JSONObject();
            modmail.put("category", modmailCategory.getId());
            modmail.put("log-channel", modmailChannel.getId());

            config.put("modmail", modmail);

            this.controller.writeInSpecificGuildConfig(guild, "config.json", config);
        }
        Category modmailCategory;
        TextChannel modmailLogChannel;


            modmailCategory = guild.getCategoryById(config.getJSONObject("modmail").getString("category"));
            if (modmailCategory == null){
            try {
                // Try to create a new category
                modmailCategory = guild.createCategory("MODMAIL").complete();

                Collection<Permission> deniedPermissions = new ArrayList<>() {{
                    add(Permission.VIEW_CHANNEL);
                }};

                modmailCategory.getManager().putRolePermissionOverride(guild.getPublicRole().getIdLong(),null,deniedPermissions).queue();
                JSONObject modmail = config.getJSONObject("modmail");
                modmail.put("category", modmailCategory.getId());
                config.put("modmail", modmail);
                this.controller.writeInSpecificGuildConfig(guild, "config.json", config);
            } catch (IllegalArgumentException | InsufficientPermissionException categoryCreateExeption) {
                logger.config("The bot has no permissions to create a channel");
                return;
            }
        }


            modmailLogChannel = guild.getTextChannelById(config.getJSONObject("modmail").getString("log-channel"));
            if (modmailLogChannel == null){
                try {
                    // Try to create a new channel
                    modmailLogChannel = modmailCategory.createTextChannel("modmail-log").complete();
                    modmailLogChannel.getManager().sync().queue();
                    JSONObject modmail = config.getJSONObject("modmail");
                    modmail.put("log-channel", modmailLogChannel.getId());
                    config.put("modmail", modmail);
                    this.controller.writeInSpecificGuildConfig(guild, "config.json", config);

                } catch (InsufficientPermissionException | IllegalArgumentException channelCreateExeption) {
                    // Returns if the bot has no permissions
                    logger.config("The bot has no permissions to create a channel");
                    return;
                }
        }


        // Embed for logging
        EmbedBuilder loggingEmbed = new EmbedBuilder();
        loggingEmbed.setDescription("A new Ticket was created:\n " + embedFromUser.build().getTitle());
        loggingEmbed.setColor(new Color(0, 255, 0));
        loggingEmbed.setFooter(user.getAsTag(), user.getEffectiveAvatarUrl());
        loggingEmbed.setTimestamp(LocalDateTime.now());
        modmailLogChannel.sendMessageEmbeds(loggingEmbed.build()).queue();


        // Create the question channel
        TextChannel modmailChannel = modmailCategory.createTextChannel(user.getAsTag()).complete();
        modmailChannel.getManager().sync().queue();
        modmailChannel.getManager().setTopic("This is a modmail channel, please not delete it").queue();


        // String Builder for MessageAttachments

        StringBuilder attachmentStrings = new StringBuilder();
        user.openPrivateChannel().complete().getIterableHistory().complete().stream().filter(x->!x.getAttachments().isEmpty()
                && x.getAuthor().getId().equals(user.getId())
                && x.getTimeCreated().isAfter(e.getTimeCreated())).forEach(x->{
            if (!x.getAttachments().isEmpty()){
                x.getAttachments().forEach(y-> attachmentStrings.append(y.getUrl()).append("\n"));
            }
        });

        modmailChannel.sendMessageEmbeds(embedFromUser.build()).
                setActionRows(
                        ActionRow.of(Button.primary(String.format("modmailChannelReply;%s;%s", modmailChannel.getId(), user.getId()), "Reply")),ActionRow.of(Button.danger(String.format("modmailChannelClose;%s;%s", modmailChannel.getId(), user.getId()), "Close")))
                .queue();
        if (!attachmentStrings.isEmpty()){
            modmailChannel.sendMessage(attachmentStrings).queue();
        }


        // Send the user a message, that the ticket was received
        String message = """
                Your ticket was received.
                Please use the button under this message to reply or adding a text.
                """;
        try {
            // Try to send a DM to the User
            user.openPrivateChannel().complete().sendMessage(message).setActionRow(Button.success(String.format("modmailUserReply;%s;%s", modmailChannel.getId(), user.getId()), "Reply")).queue();
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

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        switch (buttonId.split(";")[0]) {
            case "modmailChannelReply" -> modmailChannelReply(event);
            case "modmailChannelClose" -> modmailChannelClose(event);
            case "modmailUserReply" -> modmailUserReply(event);
            default -> {
            }
        }

    }

    private void modmailUserReply(ButtonInteractionEvent event) {
        String[] modmailId = event.getComponentId().split(";");
        TextInput modmailUserInput = TextInput.create("modmailUserReply","Text", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Your answer or your addition")
                .setMinLength(10)
                .build();

        Modal.Builder modmailUserReply = Modal.create("modmailUserReply;" + modmailId[1],"Modmail");

        event.replyModal(modmailUserReply.addActionRow(modmailUserInput).build()).queue();
    }

    private void modmailChannelClose(ButtonInteractionEvent event) {
        @SuppressWarnings("unused") String[] buttonId = event.getComponentId().split(";");
        event.reply("You are sure you want to close this channel")
                .addActionRows(ActionRow.of(Button.danger("modmailCloseYes","Yes"),Button.success("modmailCloseNo","No")))
                .queue(x->waiter.waitForEvent(ButtonInteractionEvent.class,f->{
                    String id = f.getComponentId();
                    return id.equals("modmailCloseYes") || id.equals("modmailCloseNo");
                },f->{
                    String id = f.getComponentId();
                    if (id.equals("modmailCloseNo")) {
                        x.editOriginalComponents().queue();
                        x.deleteOriginal().queue();
                        return;
                    }
                    saveMessagesFromModmailChannel(event.getGuildChannel().asTextChannel()).start();
                }));
    }

    @SuppressWarnings("unused")
    private void modmailChannelReply(ButtonInteractionEvent event) {

    }

    private boolean messagesInTextChannel(TextChannel channel){
        return new MessageHistory(channel).retrievePast(100).complete().size() > 0;
    }

    private Thread saveMessagesFromModmailChannel(TextChannel channel){
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
        return new Thread(()->{
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") ArrayList<String> messageHistoryReversed = new ArrayList<>();
            while (messagesInTextChannel(channel)){
                    List<Message> messages = new MessageHistory(channel).retrievePast(100).complete().stream().toList();
                    messages.forEach(m -> {
                        try {
                            messageHistoryReversed.add(String.format("%s | %s / %s",
                                    m.getTimeCreated().format(timeFormatter), String.format("%s(%s)", m.getAuthor().getName(), m.getAuthor().getId()), m.getContentStripped()));

                            m.delete().complete();
                            TimeUnit.SECONDS.sleep(5);
                        } catch (Exception e) {
                            logger.warning(e.getMessage());
                        }
                    });
                }
        });
    }
}
