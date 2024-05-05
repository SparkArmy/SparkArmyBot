package de.SparkArmy.jda.events.customEvents.otherEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.annotations.events.JDAButtonInteractionEvent;
import de.SparkArmy.jda.annotations.internal.JDAEvent;
import de.SparkArmy.jda.events.EventManager;
import de.SparkArmy.jda.events.iEvent.IJDAEvent;
import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ModMailEvents implements IJDAEvent {
    private final ConfigController controller;

    public ModMailEvents(EventManager dispatcher) {
        this.controller = dispatcher.getController();
    }

    ResourceBundle modMailBundle(DiscordLocale locale) {
        return Util.getResourceBundle("modMail", locale);
    }

    ResourceBundle standardPhrases(DiscordLocale locale) {
        return Util.getResourceBundle("standardPhrases", locale);
    }

    @JDAEvent
    @JDAButtonInteractionEvent(startWith = "modMailCreateTicket")
    public void modMailTicketCreation(@NotNull ButtonInteractionEvent event) {
        event.deferReply(true).queue();
        Guild guild = event.getGuild();
        User user = event.getUser();
        if (guild == null) return;

        ResourceBundle guildBundle = modMailBundle(guild.getLocale());
        ResourceBundle userBundle = modMailBundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        List<Long> blacklistUserIds = controller.getGuildModMailBlacklistedUsers(guild);
        if (blacklistUserIds.contains(user.getIdLong())) {
            event.getHook().editOriginal(userBundle.getString("modMailTicketCreation.userIsOnBlacklist")).queue();
            return;
        }

        long categoryId = controller.getGuildModMailCategory(guild);

        Category category = guild.getCategoryById(categoryId);
        if (category == null) return;

        EnumSet<Permission> userAllowed = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.CREATE_PRIVATE_THREADS, Permission.CREATE_PUBLIC_THREADS);
        EnumSet<Permission> publicRoleDenied = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
        EnumSet<Permission> modRoleAllowed = EnumSet.of(Permission.VIEW_CHANNEL);
        EnumSet<Permission> modRoleDenied = EnumSet.of(Permission.MANAGE_CHANNEL, Permission.MESSAGE_SEND, Permission.MANAGE_PERMISSIONS, Permission.CREATE_PRIVATE_THREADS, Permission.CREATE_PUBLIC_THREADS);


        ChannelAction<TextChannel> channelAction = category.createTextChannel(user.getName())
                .addMemberPermissionOverride(user.getIdLong(), userAllowed, null)
                .addRolePermissionOverride(guild.getPublicRole().getIdLong(), null, publicRoleDenied);

        StringBuilder modPingMessage = new StringBuilder();
        List<Long> pingRoles = controller.getGuildModMailPingRoles(guild);
        for (long roleId : pingRoles) {
            Role role = guild.getRoleById(roleId);
            if (role != null) {
                modPingMessage.append(role.getAsMention()).append(" ");
                channelAction = channelAction.addRolePermissionOverride(roleId, modRoleAllowed, modRoleDenied);
            }
        }

        channelAction.flatMap(x -> event.getHook().editOriginal(String.format(userBundle.getString("modMailTicketCreation.modMailChannelCreated"), x.getAsMention()))
                        .flatMap(y -> !modPingMessage.isEmpty(), y -> x.sendMessage(modPingMessage))
                        .map(y -> controller.getGuildModMailLogChannel(guild))
                        .map(guild::getTextChannelById)
                        .flatMap(Objects::nonNull, y -> {
                            EmbedBuilder modMailLogEmbed = new EmbedBuilder();
                            modMailLogEmbed.setTitle(guildBundle.getString("modMailTicketCreation.modMailCreateEmbed.title"));
                            modMailLogEmbed.setAuthor(user.getName(), null, user.getEffectiveAvatarUrl());
                            modMailLogEmbed.setFooter(TimeUtil.getDateTimeString(event.getTimeCreated()), guild.getIconUrl());
                            modMailLogEmbed.setColor(new Color(255, 0, 0));
                            Button closeButton = Button.of(ButtonStyle.DANGER,
                                    String.format("modMailChannelClose;%s", x.getId()),
                                    standardPhrases.getString("buttons.close"));
                            return y.sendMessageEmbeds(modMailLogEmbed.build()).addActionRow(closeButton);
                        })
                        .flatMap(y -> x.createThreadChannel("moderatorMessages", true)
                                .flatMap(z -> z.sendMessage(modPingMessage)))
                        .flatMap(y -> x.getManager().setTopic(user.getId())))
                .queue();
    }

    @JDAEvent
    @JDAButtonInteractionEvent(startWith = "modMailChannelClose")
    public void modMailChannelCloseButtonAction(@NotNull ButtonInteractionEvent event) {
        event.deferReply(true).queue();
        Guild guild = event.getGuild();
        if (guild == null) return;

        ResourceBundle userBundle = modMailBundle(event.getUserLocale());
        ResourceBundle guildBundle = modMailBundle(event.getGuildLocale());

        String componentId = event.getComponentId();
        String[] splitComponentId = componentId.split(";");

        TextChannel modMailChannel = guild.getTextChannelById(splitComponentId[1]);
        if (modMailChannel == null) {
            event.getHook()
                    .editOriginal(userBundle.getString("modMailChannelCloseButtonAction.modMailChannelIsNull"))
                    .queue();
            return;
        }

        String userChannelId = modMailChannel.getTopic();

        List<ThreadChannel> threadChannels = modMailChannel.getThreadChannels();
        if (threadChannels.isEmpty()) {
            event.getHook()
                    .editOriginal(userBundle.getString("modMailChannelCloseButtonAction.threadsAreEmpty"))
                    .queue();
            return;
        }

        ThreadChannel threadChannel = threadChannels.getFirst();

        int messageCount = threadChannel.getMessageCount();

        if (messageCount < 100) {
            threadChannel.getHistoryFromBeginning(100)
                    .map(MessageHistory::getRetrievedHistory)
                    .flatMap(messageHistoryList ->
                            modMailChannel.delete()
                                    .map(x -> controller.getGuildModMailArchiveChannel(guild))
                                    .map(guild::getTextChannelById)
                                    .flatMap(Objects::nonNull,
                                            archiveChannel -> archiveChannel.createThreadChannel(modMailChannel.getName()
                                                            + "||"
                                                            + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date(System.currentTimeMillis())))
                                                    .onSuccess(archiveThread -> {
                                                        List<RestAction<Message>> messageActions = new ArrayList<>();
                                                        int attachmentMessageCount = 0;
                                                        List<CompletableFuture<File>> attachmentFiles = new ArrayList<>();
                                                        List<CompletableFuture<Void>> messageFuture = new ArrayList<>();
                                                        HashMap<String, byte[]> attachmentData = new HashMap<>();
                                                        for (Message m : messageHistoryList.reversed()) {
                                                            User author = m.getAuthor();
                                                            EmbedBuilder targetMessageEmbedBuilder = new EmbedBuilder();
                                                            targetMessageEmbedBuilder.setAuthor(author.getName(), null, author.getEffectiveAvatarUrl());

                                                            if (!m.getContentDisplay().isEmpty()) {
                                                                targetMessageEmbedBuilder.setDescription(m.getContentDisplay());
                                                            }


                                                            if (author.getId().equals(userChannelId)) {
                                                                targetMessageEmbedBuilder.setTitle(guildBundle.getString("modMailChannelCloseButtonAction.targetMessageEmbedBuilder.title.received"));
                                                            } else {
                                                                targetMessageEmbedBuilder.setTitle(guildBundle.getString("modMailChannelCloseButtonAction.targetMessageEmbedBuilder.title.send"));
                                                            }
                                                            if (m.getAttachments().isEmpty()) {
                                                                messageActions.add(archiveThread.sendMessageEmbeds(targetMessageEmbedBuilder.build()));
                                                            } else {
                                                                attachmentMessageCount++;
                                                                MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
                                                                messageBuilder.addEmbeds(targetMessageEmbedBuilder.build());
                                                                File directory = FileHandler.getDirectoryInUserDirectory("modMailAttachments/close" + m.getId());
                                                                for (Message.Attachment attachment : m.getAttachments()) {
                                                                    File attachmentFile = FileHandler.getFileInDirectory(directory, attachment.getFileName());
                                                                    attachmentFiles.add(attachment.getProxy().downloadToFile(attachmentFile).thenApply(file -> {
                                                                        try {
                                                                            attachmentData.put(attachment.getFileName(), Files.readAllBytes(file.toPath()));
                                                                        } catch (IOException e) {
                                                                            throw new RuntimeException(e);
                                                                        }
                                                                        if (!file.delete())
                                                                            throw new RuntimeException("ModMail-Attachment-File was not deleted in close action");
                                                                        return null;
                                                                    }));
                                                                }
                                                                messageFuture.add(CompletableFuture.allOf(attachmentFiles.toArray(new CompletableFuture[0]))
                                                                        .thenAccept(x -> {
                                                                            Util.logger.info("message added");
                                                                            for (String s : attachmentData.keySet()) {
                                                                                messageBuilder.addFiles(FileUpload.fromData(attachmentData.get(s), s));
                                                                            }
                                                                            messageActions.add(archiveThread
                                                                                    .sendMessage(messageBuilder.build())
                                                                                    .map(y -> {
                                                                                        if (!directory.delete())
                                                                                            throw new RuntimeException("ModMail-Attachment-Directory (close/%s) was not deleted".formatted(m.getId()));
                                                                                        return null;
                                                                                    }));
                                                                        }));

                                                            }
                                                        }
                                                        if (attachmentMessageCount == 0) {
                                                            RestAction.allOf(messageActions).mapToResult()
                                                                    .flatMap(x -> archiveThread.getManager().setLocked(true))
                                                                    .queue();
                                                        } else {
                                                            CompletableFuture.allOf(messageFuture.toArray(new CompletableFuture[0]))
                                                                    .thenAccept(x -> {
                                                                        Util.logger.info("messages build");
                                                                        RestAction.allOf(messageActions).mapToResult()
                                                                                .flatMap(y -> archiveThread.getManager().setLocked(true))
                                                                                .queue(y -> Util.logger.info("message sent"));
                                                                    });
                                                        }
                                                    }))
                    )
                    .flatMap(x -> event.getMessage().editMessageComponents()
                            .flatMap(y -> event.getHook().editOriginal("Closed")))
                    .queue();
        }
    }

    @JDAEvent
    public void modMailMessageHandler(@NotNull MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        User user = event.getAuthor();

        long categoryId = controller.getGuildModMailCategory(guild);
        Category databaseCategory = guild.getCategoryById(categoryId);
        Category modMailCategory = event.getMessage().getCategory();

        if ((modMailCategory == null || databaseCategory == null) && !Objects.equals(databaseCategory, modMailCategory))
            return;
        if (user.isBot() || user.isSystem()) return;

        long logChannelId = controller.getGuildModMailLogChannel(guild);
        long archiveCategory = controller.getGuildModMailArchiveChannel(guild);
        long channelId = event.getChannel().getIdLong();

        if (channelId == logChannelId || channelId == archiveCategory) return;

        List<Message.Attachment> attachments = event.getMessage().getAttachments();

        ChannelType channelType = event.getChannelType();

        if (attachments.isEmpty()) {
            if (channelType == ChannelType.TEXT) {
                List<ThreadChannel> threadChannels = event.getGuildChannel().asTextChannel().getThreadChannels();
                if (threadChannels.isEmpty()) return;
                threadChannels.getFirst().sendMessage(MessageCreateData.fromMessage(event.getMessage())).queue();
            } else if (channelType == ChannelType.GUILD_PRIVATE_THREAD) {
                TextChannel parentChannel = event.getGuildChannel().asThreadChannel().getParentChannel().asTextChannel();
                parentChannel.sendMessage(MessageCreateData.fromMessage(event.getMessage())).queue();
            }
        } else {
            if (channelType != ChannelType.TEXT && channelType != ChannelType.GUILD_PRIVATE_THREAD) return;
            MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
            messageBuilder.applyMessage(event.getMessage());
            List<CompletableFuture<File>> completableFuturesAttachmentList = new ArrayList<>();
            HashMap<String, byte[]> attachmentData = new HashMap<>();
            File directory = FileHandler.getDirectoryInUserDirectory("modMailAttachments/" + event.getMessageId());
            for (Message.Attachment attachment : attachments) {
                File attachmentFile = FileHandler.getFileInDirectory(directory, attachment.getFileName());
                completableFuturesAttachmentList.add(attachment.getProxy().downloadToFile(attachmentFile).thenApply(file -> {
                    try {
                        attachmentData.put(attachment.getFileName(), Files.readAllBytes(file.toPath()));
                        if (!file.delete()) throw new RuntimeException("ModMail-Attachment-File was not deleted");
                        return null;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }

            CompletableFuture.allOf(completableFuturesAttachmentList.toArray(new CompletableFuture[0]))
                    .thenAccept(x -> {
                        for (String set : attachmentData.keySet()) {
                            messageBuilder.addFiles(FileUpload.fromData(attachmentData.get(set), set));
                        }
                        if (channelType == ChannelType.TEXT) {
                            List<ThreadChannel> threadChannels = event.getGuildChannel().asTextChannel().getThreadChannels();
                            if (threadChannels.isEmpty()) return;
                            threadChannels.getFirst()
                                    .sendMessage(messageBuilder.build())
                                    .map(y -> {
                                        if (!directory.delete())
                                            throw new RuntimeException("ModMail-Attachment-Directory (%s) was not deleted".formatted(event.getMessageId()));
                                        return null;
                                    })
                                    .queue();
                        } else {
                            TextChannel parentChannel = event.getGuildChannel().asThreadChannel().getParentChannel().asTextChannel();
                            parentChannel
                                    .sendMessage(messageBuilder.build())
                                    .map(y -> {
                                        if (!directory.delete())
                                            throw new RuntimeException("ModMail-Attachment-Directory (%s) was not deleted".formatted(event.getMessageId()));
                                        return null;
                                    })
                                    .queue();
                        }
                    });
        }
    }

    @Override
    public Class<?> getEventClass() {
        return this.getClass();
    }
}
