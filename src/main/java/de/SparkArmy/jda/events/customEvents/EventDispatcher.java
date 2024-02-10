package de.SparkArmy.jda.events.customEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.JdaApi;
import de.SparkArmy.jda.events.annotations.events.channelEvents.*;
import de.SparkArmy.jda.events.annotations.events.guildEvents.*;
import de.SparkArmy.jda.events.annotations.events.messageEvents.*;
import de.SparkArmy.jda.events.annotations.events.roleEvents.*;
import de.SparkArmy.jda.events.annotations.events.userEvents.*;
import de.SparkArmy.jda.events.annotations.interactions.*;
import de.SparkArmy.jda.events.customEvents.commandEvents.*;
import de.SparkArmy.jda.events.customEvents.otherEvents.MessageEvents;
import de.SparkArmy.jda.events.customEvents.otherEvents.ModMailEvents;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.GenericChannelEvent;
import net.dv8tion.jda.api.events.channel.update.*;
import net.dv8tion.jda.api.events.guild.*;
import net.dv8tion.jda.api.events.guild.invite.GenericGuildInviteEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.events.guild.member.update.*;
import net.dv8tion.jda.api.events.guild.override.GenericPermissionOverrideEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideCreateEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideDeleteEvent;
import net.dv8tion.jda.api.events.guild.override.PermissionOverrideUpdateEvent;
import net.dv8tion.jda.api.events.guild.scheduledevent.*;
import net.dv8tion.jda.api.events.guild.scheduledevent.update.*;
import net.dv8tion.jda.api.events.guild.update.*;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.role.GenericRoleEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.*;
import net.dv8tion.jda.api.events.stage.GenericStageInstanceEvent;
import net.dv8tion.jda.api.events.stage.StageInstanceCreateEvent;
import net.dv8tion.jda.api.events.stage.StageInstanceDeleteEvent;
import net.dv8tion.jda.api.events.stage.update.StageInstanceUpdatePrivacyLevelEvent;
import net.dv8tion.jda.api.events.stage.update.StageInstanceUpdateTopicEvent;
import net.dv8tion.jda.api.events.user.GenericUserEvent;
import net.dv8tion.jda.api.events.user.update.GenericUserUpdateEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventDispatcher {

    private final ShardManager shardManager;
    private final ConfigController controller;
    private final Logger logger;
    private final JdaApi api;

    private final Set<Object> events = ConcurrentHashMap.newKeySet();

    private @NotNull Constructor<?> constructor(@NotNull Object o) throws NoSuchMethodException {
        return o.getClass().getConstructor(this.getClass());
    }


    public EventDispatcher(@NotNull JdaApi api) {
        this.api = api;
        this.controller = api.getController();
        this.logger = api.getLogger();
        this.shardManager = api.getShardManager();
        awaitJdaLoading();
        registerEvents();
    }

    private void awaitJdaLoading() {
        for (int i = 0; i < shardManager.getShardsTotal(); i++) {
            try {
                JDA jda = shardManager.getShardById(i);
                if (jda == null) {
                    logger.error("Shard %d is null".formatted(i));
                    controller.getMain().systemExit(1);
                    return;
                }
                jda.awaitReady();
                logger.info("Shard %d is ready".formatted(i));
            } catch (InterruptedException e) {
                logger.error("Error in shard loading", e);
                controller.getMain().systemExit(1);
            }
        }
    }

    @SubscribeEvent
    public void dispatchEvent(GenericEvent event) {
        if (event instanceof GenericInteractionCreateEvent) {
            new InteractionEvent((GenericInteractionCreateEvent) event, this);
        } else if (event instanceof GenericMessageEvent) {
            new MessageEvent((GenericMessageEvent) event, this);
        } else if (event instanceof GenericUserEvent) {
            new UserEvent((GenericUserEvent) event, this);
        } else if (event instanceof GenericChannelEvent) {
            new ChannelEvent((GenericChannelEvent) event, this);
        } else if (event instanceof GenericGuildEvent) {
            new GuildEvent((GenericGuildEvent) event, this);
        } else if (event instanceof GenericRoleEvent) {
            new RoleEvent((GenericRoleEvent) event, this);
        } else if (event instanceof Event) {
            new OtherEvents((Event) event, this);
        }
    }

    private void registerEvents() {
        registerEvent(new ArchiveSlashCommandEvents(this));
        registerEvent(new GeneralCommandEvents(this));
        registerEvent(new NoteSlashCommandEvents(this));
        registerEvent(new PunishmentCommandEvents(this));
        registerEvent(new NotificationSlashCommandEvents(this));
        registerEvent(new CleanSlashCommandEvents(this));
        registerEvent(new MessageEvents(this));
        registerEvent(new ConfigureSlashCommandEvents(this));
        registerEvent(new FeedbackSlashCommandEvents(this));
        registerEvent(new ModMailEvents(this));
    }

    private void registerEvent(Object o) {
        if (events.contains(o)) {
            logger.error("Event: \" " + o.getClass().getName() + " \" already registered");
            return;
        }
        events.add(o);
    }

    public ConfigController getController() {
        return controller;
    }

    public Logger getLogger() {
        return logger;
    }

    public JdaApi getApi() {
        return api;
    }


    private class InteractionEvent {
        public InteractionEvent(GenericInteractionCreateEvent event, Object clazz) {
            for (Object o : events) {
                for (Method m : o.getClass().getMethods()) {
                    try {
                        if (event instanceof ButtonInteractionEvent) {
                            JDAButton annotation = m.getAnnotation(JDAButton.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                String componentId = ((ButtonInteractionEvent) event).getComponentId();
                                if (componentId.startsWith(annotation.startWith())) {
                                    m.invoke(constructor(o).newInstance(clazz), event);
                                } else if (componentId.equals(annotation.name())) {
                                    m.invoke(constructor(o).newInstance(clazz), event);
                                }
                            }
                        } else if (event instanceof CommandAutoCompleteInteractionEvent) {
                            JDAAutoComplete annotation = m.getAnnotation(JDAAutoComplete.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                if (((CommandAutoCompleteInteractionEvent) event).getName().equals(annotation.commandName())) {
                                    m.invoke(constructor(o).newInstance(clazz), event);
                                }
                            }
                        } else if (event instanceof EntitySelectInteractionEvent) {
                            JDAEntityMenu annotation = m.getAnnotation(JDAEntityMenu.class);
                            String componentId = ((EntitySelectInteractionEvent) event).getComponentId();
                            if (annotation != null && m.getParameterCount() == 1) {
                                if (componentId.startsWith(annotation.startWith())) {
                                    m.invoke(constructor(o).newInstance(clazz), event);
                                } else if (componentId.equals(annotation.name())) {
                                    m.invoke(constructor(o).newInstance(clazz), event);
                                }
                            }
                        } else if (event instanceof MessageContextInteractionEvent) {
                            JDAMessageCommand annotation = m.getAnnotation(JDAMessageCommand.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                if (((MessageContextInteractionEvent) event).getName().equals(annotation.name())) {
                                    m.invoke(constructor(o).newInstance(clazz), event);
                                }
                            }
                        } else if (event instanceof ModalInteractionEvent) {
                            JDAModal annotation = m.getAnnotation(JDAModal.class);
                            String componentId = ((ModalInteractionEvent) event).getModalId();
                            if (annotation != null && m.getParameterCount() == 1) {
                                if (componentId.startsWith(annotation.startWith())) {
                                    m.invoke(constructor(o).newInstance(clazz), event);
                                } else if (componentId.equals(annotation.name())) {
                                    m.invoke(constructor(o).newInstance(clazz), event);
                                }
                            }
                        } else if (event instanceof SlashCommandInteractionEvent) {
                            JDASlashCommand annotation = m.getAnnotation(JDASlashCommand.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                if (((SlashCommandInteractionEvent) event).getName().equals(annotation.name())) {
                                    m.invoke(constructor(o).newInstance(clazz), event);
                                }
                            }
                        } else if (event instanceof StringSelectInteractionEvent) {
                            JDAStringMenu annotation = m.getAnnotation(JDAStringMenu.class);
                            String componentId = ((StringSelectInteractionEvent) event).getComponentId();
                            if (annotation != null && m.getParameterCount() == 1) {
                                if (componentId.startsWith(annotation.startWith())) {
                                    m.invoke(constructor(o).newInstance(clazz), event);
                                } else if (componentId.equals(annotation.name())) {
                                    m.invoke(constructor(o).newInstance(clazz), event);
                                }
                            }
                        } else if (event instanceof UserContextInteractionEvent) {
                            JDAUserCommand annotation = m.getAnnotation(JDAUserCommand.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                if (((UserContextInteractionEvent) event).getName().equals(annotation.name())) {
                                    m.invoke(constructor(o).newInstance(clazz), event);
                                }
                            }
                        }
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                             InstantiationException e) {
                        logger.error("Error to dispatch interactionEvent in %s | %s".formatted(o.getClass().getName(), m.getName()), e);
                    }
                }
            }
        }
    }

    private class MessageEvent {
        public MessageEvent(GenericMessageEvent event, Object clazz) {
            for (Object o : events) {
                for (Method m : o.getClass().getMethods()) {
                    try {
                        if (event instanceof MessageDeleteEvent) {
                            JDAMessageDeleteEvent annotation = m.getAnnotation(JDAMessageDeleteEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        } else if (event instanceof MessageEmbedEvent) {
                            JDAMessageEmbedEvent annotation = m.getAnnotation(JDAMessageEmbedEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        } else if (event instanceof MessageReactionAddEvent) {
                            JDAMessageReactionAddEvent annotation = m.getAnnotation(JDAMessageReactionAddEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        } else if (event instanceof MessageReactionRemoveAllEvent) {
                            JDAMessageReactionRemoveAllEvent annotation = m.getAnnotation(JDAMessageReactionRemoveAllEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        } else if (event instanceof MessageReactionRemoveEmojiEvent) {
                            JDAMessageReactionRemoveEmojiEvent annotation = m.getAnnotation(JDAMessageReactionRemoveEmojiEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        } else if (event instanceof MessageReactionRemoveEvent) {
                            JDAMessageReactionRemoveEvent annotation = m.getAnnotation(JDAMessageReactionRemoveEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        } else if (event instanceof MessageReceivedEvent) {
                            JDAMessageReceivedEvent annotation = m.getAnnotation(JDAMessageReceivedEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        } else if (event instanceof MessageUpdateEvent) {
                            JDAMessageUpdateEvent annotation = m.getAnnotation(JDAMessageUpdateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                             InstantiationException e) {
                        logger.error("Error to dispatch messageEvent in %s | %s".formatted(o.getClass().getName(), m.getName()), e);
                    }
                }
            }
        }
    }

    private class UserEvent {
        public UserEvent(GenericUserEvent event, Object clazz) {
            for (Object o : events) {
                for (Method m : o.getClass().getMethods()) {
                    try {
                        if (event instanceof GenericUserUpdateEvent<?>) {
                            JDAGenericUserUpdateEvent annotation = m.getAnnotation(JDAGenericUserUpdateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericUserUpdateEvent<?>) {
                            JDAUserActivityEndEvent annotation = m.getAnnotation(JDAUserActivityEndEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericUserUpdateEvent<?>) {
                            JDAUserActivityStartEvent annotation = m.getAnnotation(JDAUserActivityStartEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericUserUpdateEvent<?>) {
                            JDAUserTypingEvent annotation = m.getAnnotation(JDAUserTypingEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericUserUpdateEvent<?>) {
                            JDAUserUpdateActivitiesEvent annotation = m.getAnnotation(JDAUserUpdateActivitiesEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericUserUpdateEvent<?>) {
                            JDAUserUpdateActivitiesOrderEvent annotation = m.getAnnotation(JDAUserUpdateActivitiesOrderEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericUserUpdateEvent<?>) {
                            JDAUserUpdateAvatarEvent annotation = m.getAnnotation(JDAUserUpdateAvatarEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericUserUpdateEvent<?>) {
                            JDAUserUpdateFlagsEvent annotation = m.getAnnotation(JDAUserUpdateFlagsEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericUserUpdateEvent<?>) {
                            JDAUserUpdateGlobalNameEvent annotation = m.getAnnotation(JDAUserUpdateGlobalNameEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericUserUpdateEvent<?>) {
                            JDAUserUpdateNameEvent annotation = m.getAnnotation(JDAUserUpdateNameEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericUserUpdateEvent<?>) {
                            JDAUserUpdateOnlineStatusEvent annotation = m.getAnnotation(JDAUserUpdateOnlineStatusEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                    } catch (InvocationTargetException | IllegalAccessException | InstantiationException |
                             NoSuchMethodException e) {
                        logger.error("Error to dispatch userEvent in %s | %s".formatted(o.getClass().getName(), m.getName()), e);

                    }
                }
            }
        }
    }

    private class ChannelEvent {
        public ChannelEvent(GenericChannelEvent event, Object clazz) {
            for (Object o : events) {
                for (Method m : o.getClass().getMethods()) {
                    try {
                        if (event instanceof ChannelCreateEvent) {
                            JDAChannelCreateEvent annotation = m.getAnnotation(JDAChannelCreateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelDeleteEvent) {
                            JDAChannelDeleteEvent annotation = m.getAnnotation(JDAChannelDeleteEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateAppliedTagsEvent) {
                            JDAChannelUpdateAppliedTagsEvent annotation = m.getAnnotation(JDAChannelUpdateAppliedTagsEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateArchivedEvent) {
                            JDAChannelUpdateArchiveEvent annotation = m.getAnnotation(JDAChannelUpdateArchiveEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateArchiveTimestampEvent) {
                            JDAChannelUpdateArchiveTimestampEvent annotation = m.getAnnotation(JDAChannelUpdateArchiveTimestampEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateAutoArchiveDurationEvent) {
                            JDAChannelUpdateAutoArchiveDurationEvent annotation = m.getAnnotation(JDAChannelUpdateAutoArchiveDurationEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateBitrateEvent) {
                            JDAChannelUpdateBitrateEvent annotation = m.getAnnotation(JDAChannelUpdateBitrateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateDefaultLayoutEvent) {
                            JDAChannelUpdateDefaultLayoutEvent annotation = m.getAnnotation(JDAChannelUpdateDefaultLayoutEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateDefaultReactionEvent) {
                            JDAChannelUpdateDefaultReactionEvent annotation = m.getAnnotation(JDAChannelUpdateDefaultReactionEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateDefaultSortOrderEvent) {
                            JDAChannelUpdateDefaultSortOrderEvent annotation = m.getAnnotation(JDAChannelUpdateDefaultSortOrderEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateDefaultThreadSlowmodeEvent) {
                            JDAChannelUpdateDefaultThreadSlowmodeEvent annotation = m.getAnnotation(JDAChannelUpdateDefaultThreadSlowmodeEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateFlagsEvent) {
                            JDAChannelUpdateFlagsEvent annotation = m.getAnnotation(JDAChannelUpdateFlagsEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateInvitableEvent) {
                            JDAChannelUpdateInvitableEvent annotation = m.getAnnotation(JDAChannelUpdateInvitableEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateLockedEvent) {
                            JDAChannelUpdateLockedEvent annotation = m.getAnnotation(JDAChannelUpdateLockedEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateNameEvent) {
                            JDAChannelUpdateNameEvent annotation = m.getAnnotation(JDAChannelUpdateNameEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateNSFWEvent) {
                            JDAChannelUpdateNSFWEvent annotation = m.getAnnotation(JDAChannelUpdateNSFWEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateParentEvent) {
                            JDAChannelUpdateParentEvent annotation = m.getAnnotation(JDAChannelUpdateParentEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdatePositionEvent) {
                            JDAChannelUpdatePositionEvent annotation = m.getAnnotation(JDAChannelUpdatePositionEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateRegionEvent) {
                            JDAChannelUpdateRegionEvent annotation = m.getAnnotation(JDAChannelUpdateRegionEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateSlowmodeEvent) {
                            JDAChannelUpdateSlowmodeEvent annotation = m.getAnnotation(JDAChannelUpdateSlowmodeEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateTopicEvent) {
                            JDAChannelUpdateTopicEvent annotation = m.getAnnotation(JDAChannelUpdateTopicEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateTypeEvent) {
                            JDAChannelUpdateTypeEvent annotation = m.getAnnotation(JDAChannelUpdateTypeEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ChannelUpdateUserLimitEvent) {
                            JDAChannelUpdateUserLimitEvent annotation = m.getAnnotation(JDAChannelUpdateUserLimitEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericChannelUpdateEvent<?>) {
                            JDAGenericChannelUpdateEvent annotation = m.getAnnotation(JDAGenericChannelUpdateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                             InstantiationException e) {
                        logger.error("Error to dispatch channelEvent in %s | %s".formatted(o.getClass().getName(), m.getName()), e);
                    }
                }
            }
        }
    }

    private class GuildEvent {
        public GuildEvent(GenericGuildEvent event, Object clazz) {
            for (Object o : events) {
                for (Method m : o.getClass().getMethods()) {
                    try {
                        if (event instanceof ApplicationCommandUpdatePrivilegesEvent) {
                            JDAApplicationCommandUpdatePrivilegesEvent annotation = m.getAnnotation(JDAApplicationCommandUpdatePrivilegesEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ApplicationUpdatePrivilegesEvent) {
                            JDAApplicationUpdatePrivilegesEvent annotation = m.getAnnotation(JDAApplicationUpdatePrivilegesEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericGuildInviteEvent) {
                            JDAGenericGuildInviteEvent annotation = m.getAnnotation(JDAGenericGuildInviteEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericGuildMemberEvent) {
                            JDAGenericGuildMemberEvent annotation = m.getAnnotation(JDAGenericGuildMemberEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericGuildMemberUpdateEvent<?>) {
                            JDAGenericGuildMemberUpdateEvent annotation = m.getAnnotation(JDAGenericGuildMemberUpdateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericGuildUpdateEvent<?>) {
                            JDAGenericGuildUpdateEvent annotation = m.getAnnotation(JDAGenericGuildUpdateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericGuildVoiceEvent) {
                            JDAGenericGuildVoiceEvent annotation = m.getAnnotation(JDAGenericGuildVoiceEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericPermissionOverrideEvent) {
                            JDAGenericPermissionOverrideEvent annotation = m.getAnnotation(JDAGenericPermissionOverrideEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericPrivilegeUpdateEvent) {
                            JDAGenericPrivilegeUpdateEvent annotation = m.getAnnotation(JDAGenericPrivilegeUpdateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericScheduledEventGatewayEvent) {
                            JDAGenericScheduledEventGatewayEvent annotation = m.getAnnotation(JDAGenericScheduledEventGatewayEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericScheduledEventUpdateEvent) {
                            JDAGenericScheduledEventUpdateEvent annotation = m.getAnnotation(JDAGenericScheduledEventUpdateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericScheduledEventUserEvent) {
                            JDAGenericScheduledEventUserEvent annotation = m.getAnnotation(JDAGenericScheduledEventUserEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GenericStageInstanceEvent) {
                            JDAGenericStageInstanceEvent annotation = m.getAnnotation(JDAGenericStageInstanceEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof JDAGenericStageInstanceUpdateEvent) {
                            JDAGenericStageInstanceUpdateEvent annotation = m.getAnnotation(JDAGenericStageInstanceUpdateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildAuditLogEntryCreateEvent) {
                            JDAGuildAuditLogEntryCreateEvent annotation = m.getAnnotation(JDAGuildAuditLogEntryCreateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildAvailableEvent) {
                            JDAGuildAvailableEvent annotation = m.getAnnotation(JDAGuildAvailableEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildBanEvent) {
                            JDAGuildBanEvent annotation = m.getAnnotation(JDAGuildBanEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildInviteCreateEvent) {
                            JDAGuildInviteCreateEvent annotation = m.getAnnotation(JDAGuildInviteCreateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildInviteDeleteEvent) {
                            JDAGuildInviteDeleteEvent annotation = m.getAnnotation(JDAGuildInviteDeleteEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildJoinEvent) {
                            JDAGuildJoinEvent annotation = m.getAnnotation(JDAGuildJoinEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildLeaveEvent) {
                            JDAGuildLeaveEvent annotation = m.getAnnotation(JDAGuildLeaveEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildMemberJoinEvent) {
                            JDAGuildMemberJoinEvent annotation = m.getAnnotation(JDAGuildMemberJoinEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildMemberRemoveEvent) {
                            JDAGuildMemberRemoveEvent annotation = m.getAnnotation(JDAGuildMemberRemoveEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildMemberRoleAddEvent) {
                            JDAGuildMemberRoleAddEvent annotation = m.getAnnotation(JDAGuildMemberRoleAddEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildMemberRoleRemoveEvent) {
                            JDAGuildMemberRoleRemove annotation = m.getAnnotation(JDAGuildMemberRoleRemove.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildMemberUpdateAvatarEvent) {
                            JDAGuildMemberUpdateAvatarEvent annotation = m.getAnnotation(JDAGuildMemberUpdateAvatarEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildMemberUpdateBoostTimeEvent) {
                            JDAGuildMemberUpdateBoostTimeEvent annotation = m.getAnnotation(JDAGuildMemberUpdateBoostTimeEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildMemberUpdateEvent) {
                            JDAGuildMemberUpdateEvent annotation = m.getAnnotation(JDAGuildMemberUpdateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildMemberUpdateFlagsEvent) {
                            JDAGuildMemberUpdateFlagsEvent annotation = m.getAnnotation(JDAGuildMemberUpdateFlagsEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildMemberUpdateNicknameEvent) {
                            JDAGuildMemberUpdateNicknameEvent annotation = m.getAnnotation(JDAGuildMemberUpdateNicknameEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildMemberUpdatePendingEvent) {
                            JDAGuildMemberUpdatePendingEvent annotation = m.getAnnotation(JDAGuildMemberUpdatePendingEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildMemberUpdateTimeOutEvent) {
                            JDAGuildMemberUpdateTimeOutEvent annotation = m.getAnnotation(JDAGuildMemberUpdateTimeOutEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildReadyEvent) {
                            JDAGuildReadyEvent annotation = m.getAnnotation(JDAGuildReadyEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUnavailableEvent) {
                            JDAGuildUnavailableEvent annotation = m.getAnnotation(JDAGuildUnavailableEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUnbanEvent) {
                            JDAGuildUnbanEvent annotation = m.getAnnotation(JDAGuildUnbanEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateAfkChannelEvent) {
                            JDAGuildUpdateAfkChannelEvent annotation = m.getAnnotation(JDAGuildUpdateAfkChannelEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateAfkTimeoutEvent) {
                            JDAGuildUpdateAfkTimeoutEvent annotation = m.getAnnotation(JDAGuildUpdateAfkTimeoutEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateBannerEvent) {
                            JDAGuildUpdateBannerEvent annotation = m.getAnnotation(JDAGuildUpdateBannerEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateBoostCountEvent) {
                            JDAGuildUpdateBoostCountEvent annotation = m.getAnnotation(JDAGuildUpdateBoostCountEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateBoostTierEvent) {
                            JDAGuildUpdateBoostTierEvent annotation = m.getAnnotation(JDAGuildUpdateBoostTierEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateCommunityUpdatesChannelEvent) {
                            JDAGuildUpdateCommunityUpdatesChannelEvent annotation = m.getAnnotation(JDAGuildUpdateCommunityUpdatesChannelEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateDescriptionEvent) {
                            JDAGuildUpdateDescriptionEvent annotation = m.getAnnotation(JDAGuildUpdateDescriptionEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateExplicitContentLevelEvent) {
                            JDAGuildUpdateExplicitContentLevelEvent annotation = m.getAnnotation(JDAGuildUpdateExplicitContentLevelEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(0).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateFeaturesEvent) {
                            JDAGuildUpdateFeaturesEvent annotation = m.getAnnotation(JDAGuildUpdateFeaturesEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateIconEvent) {
                            JDAGuildUpdateIconEvent annotation = m.getAnnotation(JDAGuildUpdateIconEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateLocaleEvent) {
                            JDAGuildUpdateLocaleEvent annotation = m.getAnnotation(JDAGuildUpdateLocaleEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateMaxMembersEvent) {
                            JDAGuildUpdateMaxMembersEvent annotation = m.getAnnotation(JDAGuildUpdateMaxMembersEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateMaxPresencesEvent) {
                            JDAGuildUpdateMaxPresencesEvent annotation = m.getAnnotation(JDAGuildUpdateMaxPresencesEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateMFALevelEvent) {
                            JDAGuildUpdateMFALevelEvent annotation = m.getAnnotation(JDAGuildUpdateMFALevelEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateNameEvent) {
                            JDAGuildUpdateNameEvent annotation = m.getAnnotation(JDAGuildUpdateNameEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateNotificationLevelEvent) {
                            JDAGuildUpdateNotificationLevelEvent annotation = m.getAnnotation(JDAGuildUpdateNotificationLevelEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateNSFWLevelEvent) {
                            JDAGuildUpdateNSFWLevelEvent annotation = m.getAnnotation(JDAGuildUpdateNSFWLevelEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateOwnerEvent) {
                            JDAGuildUpdateOwnerEvent annotation = m.getAnnotation(JDAGuildUpdateOwnerEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateRulesChannelEvent) {
                            JDAGuildUpdateRulesChannelEvent annotation = m.getAnnotation(JDAGuildUpdateRulesChannelEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateSplashEvent) {
                            JDAGuildUpdateSplashEvent annotation = m.getAnnotation(JDAGuildUpdateSplashEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateSystemChannelEvent) {
                            JDAGuildUpdateSystemChannelEvent annotation = m.getAnnotation(JDAGuildUpdateSystemChannelEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateVanityCodeEvent) {
                            JDAGuildUpdateVanityCodeEvent annotation = m.getAnnotation(JDAGuildUpdateVanityCodeEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildUpdateVerificationLevelEvent) {
                            JDAGuildUpdateVerificationLevelEvent annotation = m.getAnnotation(JDAGuildUpdateVerificationLevelEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildVoiceDeafenEvent) {
                            JDAGuildVoiceDeafenEvent annotation = m.getAnnotation(JDAGuildVoiceDeafenEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildVoiceGuildDeafenEvent) {
                            JDAGuildVoiceGuildDeafenEvent annotation = m.getAnnotation(JDAGuildVoiceGuildDeafenEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildVoiceGuildMuteEvent) {
                            JDAGuildVoiceGuildMuteEvent annotation = m.getAnnotation(JDAGuildVoiceGuildMuteEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildVoiceMuteEvent) {
                            JDAGuildVoiceMuteEvent annotation = m.getAnnotation(JDAGuildVoiceMuteEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildVoiceRequestToSpeakEvent) {
                            JDAGuildVoiceRequestToSpeakEvent annotation = m.getAnnotation(JDAGuildVoiceRequestToSpeakEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildVoiceSelfDeafenEvent) {
                            JDAGuildVoiceSelfDeafenEvent annotation = m.getAnnotation(JDAGuildVoiceSelfDeafenEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildVoiceSelfMuteEvent) {
                            JDAGuildVoiceSelfMuteEvent annotation = m.getAnnotation(JDAGuildVoiceSelfMuteEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildVoiceStreamEvent) {
                            JDAGuildVoiceStreamEvent annotation = m.getAnnotation(JDAGuildVoiceStreamEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildVoiceSuppressEvent) {
                            JDAGuildVoiceSuppressEvent annotation = m.getAnnotation(JDAGuildVoiceSuppressEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildVoiceUpdateEvent) {
                            JDAGuildVoiceUpdateEvent annotation = m.getAnnotation(JDAGuildVoiceUpdateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof GuildVoiceVideoEvent) {
                            JDAGuildVoiceVideoEvent annotation = m.getAnnotation(JDAGuildVoiceVideoEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof PermissionOverrideCreateEvent) {
                            JDAPermissionOverrideCreateEvent annotation = m.getAnnotation(JDAPermissionOverrideCreateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof PermissionOverrideDeleteEvent) {
                            JDAPermissionOverrideDeleteEvent annotation = m.getAnnotation(JDAPermissionOverrideDeleteEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof PermissionOverrideUpdateEvent) {
                            JDAPermissionOverrideUpdateEvent annotation = m.getAnnotation(JDAPermissionOverrideUpdateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ScheduledEventCreateEvent) {
                            JDAScheduledEventCreateEvent annotation = m.getAnnotation(JDAScheduledEventCreateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ScheduledEventDeleteEvent) {
                            JDAScheduledEventDeleteEvent annotation = m.getAnnotation(JDAScheduledEventDeleteEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ScheduledEventUpdateDescriptionEvent) {
                            JDAScheduledEventUpdateDescriptionEvent annotation = m.getAnnotation(JDAScheduledEventUpdateDescriptionEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ScheduledEventUpdateEndTimeEvent) {
                            JDAScheduledEventUpdateEndTimeEvent annotation = m.getAnnotation(JDAScheduledEventUpdateEndTimeEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ScheduledEventUpdateImageEvent) {
                            JDAScheduledEventUpdateImageEvent annotation = m.getAnnotation(JDAScheduledEventUpdateImageEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ScheduledEventUpdateLocationEvent) {
                            JDAScheduledEventUpdateLocationEvent annotation = m.getAnnotation(JDAScheduledEventUpdateLocationEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ScheduledEventUpdateNameEvent) {
                            JDAScheduledEventUpdateNameEvent annotation = m.getAnnotation(JDAScheduledEventUpdateNameEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ScheduledEventUpdateStartTimeEvent) {
                            JDAScheduledEventUpdateStartTimeEvent annotation = m.getAnnotation(JDAScheduledEventUpdateStartTimeEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ScheduledEventUpdateStatusEvent) {
                            JDAScheduledEventUpdateStatusEvent annotation = m.getAnnotation(JDAScheduledEventUpdateStatusEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ScheduledEventUserAddEvent) {
                            JDAScheduledEventUserAddEvent annotation = m.getAnnotation(JDAScheduledEventUserAddEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof ScheduledEventUserRemoveEvent) {
                            JDAScheduledEventUserRemoveEvent annotation = m.getAnnotation(JDAScheduledEventUserRemoveEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof StageInstanceCreateEvent) {
                            JDAStageInstanceCreateEvent annotation = m.getAnnotation(JDAStageInstanceCreateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof StageInstanceDeleteEvent) {
                            JDAStageInstanceDeleteEvent annotation = m.getAnnotation(JDAStageInstanceDeleteEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof StageInstanceUpdatePrivacyLevelEvent) {
                            JDAStageInstanceUpdatePrivacyLevelEvent annotation = m.getAnnotation(JDAStageInstanceUpdatePrivacyLevelEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof StageInstanceUpdateTopicEvent) {
                            JDAStageInstanceUpdateTopicEvent annotation = m.getAnnotation(JDAStageInstanceUpdateTopicEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                             InstantiationException e) {
                        logger.error("Error to dispatch guildEvent in %s | %s".formatted(o.getClass().getName(), m.getName()), e);
                    }
                }
            }
        }
    }

    private class RoleEvent {
        public RoleEvent(GenericRoleEvent event, Object clazz) {
            for (Object o : events) {
                for (Method m : o.getClass().getMethods()) {
                    try {
                        if (event instanceof GenericRoleUpdateEvent<?>) {
                            JDAGenericRoleUpdateEvent annotation = m.getAnnotation(JDAGenericRoleUpdateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof RoleCreateEvent) {
                            JDARoleCreateEvent annotation = m.getAnnotation(JDARoleCreateEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof RoleDeleteEvent) {
                            JDARoleDeleteEvent annotation = m.getAnnotation(JDARoleDeleteEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof RoleUpdateColorEvent) {
                            JDARoleUpdateColorEvent annotation = m.getAnnotation(JDARoleUpdateColorEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof RoleUpdateHoistedEvent) {
                            JDARoleUpdateHoistedEvent annotation = m.getAnnotation(JDARoleUpdateHoistedEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof RoleUpdateIconEvent) {
                            JDARoleUpdateIconEvent annotation = m.getAnnotation(JDARoleUpdateIconEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof RoleUpdateMentionableEvent) {
                            JDARoleUpdateMentionableEvent annotation = m.getAnnotation(JDARoleUpdateMentionableEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof RoleUpdateNameEvent) {
                            JDARoleUpdateNameEvent annotation = m.getAnnotation(JDARoleUpdateNameEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof RoleUpdatePermissionsEvent) {
                            JDARoleUpdatePermissionsEvent annotation = m.getAnnotation(JDARoleUpdatePermissionsEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                        if (event instanceof RoleUpdatePermissionsEvent) {
                            JDARoleUpdatePositionEvent annotation = m.getAnnotation(JDARoleUpdatePositionEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                    } catch (InvocationTargetException | IllegalAccessException | InstantiationException |
                             NoSuchMethodException e) {
                        logger.error("Error to dispatch roleEvents in %s | %s".formatted(o.getClass().getName(), m.getName()), e);
                    }
                }
            }
        }
    }

    private class OtherEvents {
        public OtherEvents(Event event, Object clazz) {
            for (Object o : events) {
                for (Method m : o.getClass().getMethods()) {
                    try {
                        if (event instanceof MessageBulkDeleteEvent) {
                            JDAMessageBulkDeleteEvent annotation = m.getAnnotation(JDAMessageBulkDeleteEvent.class);
                            if (annotation != null && m.getParameterCount() == 1) {
                                m.invoke(constructor(o).newInstance(clazz), event);
                            }
                        }
                    } catch (InvocationTargetException | IllegalAccessException | InstantiationException |
                             NoSuchMethodException e) {
                        logger.error("Error to dispatch otherEvents in %s | %s".formatted(o.getClass().getName(), m.getName()), e);

                    }
                }
            }
        }
    }
}
