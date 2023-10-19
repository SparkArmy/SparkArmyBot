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

public class MessageEvents {

    private final ConfigController controller;

    public MessageEvents(@NotNull EventDispatcher dispatcher) {
        this.controller = dispatcher.getController();
    }

    @JDAMessageBulkDeleteEvent
    public void messageBulkDeleteEvent(MessageBulkDeleteEvent event) {

    }

    @JDAMessageDeleteEvent
    public void messageDeleteEvent(MessageDeleteEvent event) {

    }

    @JDAMessageReactionRemoveAllEvent
    public void messageReactionReactionRemoveAllEvent(MessageReactionRemoveAllEvent event) {
    }

    @JDAMessageReactionRemoveEmojiEvent
    public void messageReactionRemoveEmojiEvent(MessageReactionRemoveEmojiEvent event) {
    }

    @JDAMessageReceivedEvent
    public void messageReactionReceivedEvent(MessageReceivedEvent event) {
    }

    @JDAMessageUpdateEvent
    public void messageUpdateEvent(MessageUpdateEvent event) {
    }
}
