package de.SparkArmy.jda.events.customEvents.otherEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.annotations.events.messageEvents.*;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MessageEvents {

    private final ConfigController controller;

    public MessageEvents(@NotNull EventDispatcher dispatcher) {
        this.controller = dispatcher.getController();
    }

    @JDAMessageBulkDeleteEvent
    public void messageBulkDeleteEvent(@NotNull MessageBulkDeleteEvent event) {
        List<Long> msgIdsLong = event.getMessageIds().stream().map(Long::parseLong).toList();
        controller.getMain().getPostgres().deleteMessagesFromMessageTable(msgIdsLong);
    }

    @JDAMessageDeleteEvent
    public void messageDeleteEvent(@NotNull MessageDeleteEvent event) {
        List<Long> ids = new ArrayList<>();
        ids.add(event.getMessageIdLong());
        controller.getMain().getPostgres().deleteMessagesFromMessageTable(ids);
      }

    @JDAMessageReactionRemoveAllEvent
    public void messageReactionReactionRemoveAllEvent(MessageReactionRemoveAllEvent event) {
    }

    @JDAMessageReactionRemoveEmojiEvent
    public void messageReactionRemoveEmojiEvent(MessageReactionRemoveEmojiEvent event) {
    }

    @JDAMessageReceivedEvent
    public void messageReceivedEvent(@NotNull MessageReceivedEvent event) {
        controller.getMain().getPostgres().putMessageDataAndAttachmentsInTables(event.getMessage());
    }

    @JDAMessageUpdateEvent
    public void messageUpdateEvent(@NotNull MessageUpdateEvent event) {
        controller.getMain().getPostgres().updateMessageDataInMessageTable(event.getMessage());
    }


