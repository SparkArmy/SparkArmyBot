//package de.sparkarmy.jda.misc.punishments;
//
//import net.dv8tion.jda.api.entities.Member;
//import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
//import net.dv8tion.jda.api.interactions.InteractionHook;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Objects;
//
//public class Kick extends AbstractPunishment {
//    private final InteractionHook hook;
//
//    public Kick(@NotNull SlashCommandInteractionEvent event) {
//        super(event);
//        event.deferReply(true).queue();
//        this.hook = event.getHook();
//    }
//
//    public void createKick() {
//        if (checkPrecondition(this.hook)) return;
//        checkMemberConditions(this.hook)
//                .onErrorFlatMap(Objects::isNull,
//                        (error) -> hook.editOriginal(this.userPunishmentBundle.getString("checkPrecondition.unknownUser")))
//                .flatMap(Objects::nonNull, memberAsObject -> {
//                    long punishmentEntryCreated = createPunishmentEntry(PunishmentType.KICK);
//                    if (punishmentEntryCreated < 0) {
//                        return hook.editOriginal(String.format(standardPhrases.getString("replies.dbErrorReply"), punishmentEntryCreated));
//                    } else if (punishmentEntryCreated == 0) {
//                        return hook.editOriginal(this.userPunishmentBundle.getString("createPunishment.noDataWasInserted"));
//                    }
//                    Member member = (Member) memberAsObject;
//
//                    return member.kick()
//                            .reason(this.reason)
//                            .flatMap(x -> createLogMessageRestAction(PunishmentType.KICK))
//                            .flatMap(x -> createUserLogRestAction(x, PunishmentType.KICK))
//                            .flatMap(x -> hook.editOriginal(this.userPunishmentBundle.getString("createPunishment.successfully")));
//                })
//                .queue();
//    }
//}
