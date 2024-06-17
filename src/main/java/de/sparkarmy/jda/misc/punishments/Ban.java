package de.sparkarmy.jda.misc.punishments;

import de.sparkarmy.db.DBPunishment;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Ban extends Punishment {

    private final int days;

    private final InteractionHook hook;

    public Ban(@NotNull SlashCommandInteractionEvent event) {
        super(event);
        event.deferReply(true).queue();
        this.hook = event.getHook();
        this.days = event.getOption("days", 0, OptionMapping::getAsInt);
    }

    public void createBan() {
        if (checkPrecondition(this.hook)) return;
        checkMemberConditions(this.hook)
                .onErrorFlatMap(Objects::isNull,
                        (error) -> hook.editOriginal(this.userPunishmentBundle.getString("checkPrecondition.unknownUser")))
                .flatMap(Objects::nonNull, memberAsObject -> {
                    long punishmentEntryCreated = createPunishmentEntry(PunishmentType.BAN);
                    if (punishmentEntryCreated < 0) {
                        return hook.editOriginal(String.format(standardPhrases.getString("replies.dbErrorReply"), punishmentEntryCreated));
                    } else if (punishmentEntryCreated == 0) {
                        return hook.editOriginal(this.userPunishmentBundle.getString("createPunishment.noDataWasInserted"));
                    }
                    Member member = (Member) memberAsObject;

                    return member.ban(this.days, TimeUnit.DAYS)
                            .reason(this.reason)
                            .flatMap(x -> createLogMessageRestAction(PunishmentType.BAN))
                            .flatMap(x -> createUserLogRestAction(x, PunishmentType.BAN))
                            .flatMap(x -> hook.editOriginal(this.userPunishmentBundle.getString("createPunishment.successfully")));
                })
                .queue();
    }

    @SuppressWarnings("unused")
    public static List<DBPunishment> getBansFromGuild(Guild guild) {
        return DBPunishment.getPunishmentsByGuild(guild).stream().filter(x -> x.type().equals(PunishmentType.BAN)).toList();

    }


}
