package de.sparkarmy.jda.misc.punishments;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Softban extends Punishment {
    private final InteractionHook hook;

    public Softban(@NotNull SlashCommandInteractionEvent event) {
        super(event);
        event.deferReply(true).queue();
        this.hook = event.getHook();
    }

    public void createSoftban() {
        if (checkPrecondition(this.hook)) return;
        checkMemberConditions(this.hook)
                .onErrorFlatMap(Objects::isNull,
                        (error) -> hook.editOriginal(this.userPunishmentBundle.getString("checkPrecondition.unknownUser")))
                .flatMap(Objects::nonNull, memberAsObject -> {
                    long punishmentEntryCreated = createPunishmentEntry(PunishmentType.SOFTBAN);
                    if (punishmentEntryCreated < 0) {
                        return hook.editOriginal(String.format(standardPhrases.getString("replies.dbErrorReply"), punishmentEntryCreated));
                    } else if (punishmentEntryCreated == 0) {
                        return hook.editOriginal(this.userPunishmentBundle.getString("createPunishment.noDataWasInserted"));
                    }
                    Member member = (Member) memberAsObject;

                    return member.ban(1, TimeUnit.DAYS)
                            .reason(this.reason)
                            .delay(3, TimeUnit.SECONDS)
                            .flatMap(x -> this.guild.unban(this.target).reason("SOFTBAN"))
                            .flatMap(x -> createLogMessageRestAction(PunishmentType.SOFTBAN))
                            .flatMap(x -> createUserLogRestAction(x, PunishmentType.SOFTBAN))
                            .flatMap(x -> hook.editOriginal(this.userPunishmentBundle.getString("createPunishment.successfully")));
                })
                .queue();
    }
}
