package de.sparkarmy.jda.misc.punishments;

import de.sparkarmy.db.DBRole;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class Mute extends Punishment {
    private final InteractionHook hook;

    public Mute(@NotNull SlashCommandInteractionEvent event) {
        super(event);
        event.deferReply(true).queue();
        this.hook = event.getHook();
    }

    public void createMute() {
        if (checkPrecondition(this.hook)) return;
        checkMemberConditions(this.hook)
                .onErrorFlatMap(Objects::isNull,
                        (error) -> hook.editOriginal(this.userPunishmentBundle.getString("checkPrecondition.unknownUser")))
                .flatMap(Objects::nonNull, memberAsObject -> {
                    long punishmentEntryCreated = createPunishmentEntry(PunishmentType.MUTE);
                    if (punishmentEntryCreated < 0) {
                        return hook.editOriginal(String.format(standardPhrases.getString("replies.dbErrorReply"), punishmentEntryCreated));
                    } else if (punishmentEntryCreated == 0) {
                        return hook.editOriginal(this.userPunishmentBundle.getString("createPunishment.noDataWasInserted"));
                    }

                    Role muteRole = getMuteRole();
                    if (muteRole == null) {
                        return createLogMessageRestAction(PunishmentType.MUTE)
                                .flatMap(x -> createUserLogRestAction(x, PunishmentType.MUTE))
                                .flatMap(x -> hook.editOriginal(this.userPunishmentBundle.getString("checkPrecondition.noPunishmentRoleSet")));
                    }

                    return this.guild.addRoleToMember(this.target, muteRole)
                            .reason(this.reason)
                            .flatMap(x -> createLogMessageRestAction(PunishmentType.MUTE))
                            .flatMap(x -> createUserLogRestAction(x, PunishmentType.MUTE))
                            .flatMap(x -> hook.editOriginal(this.userPunishmentBundle.getString("createPunishment.successfully")));
                })
                .queue();
    }

    private @Nullable Role getMuteRole() {
        List<Role> roleList = this.guild.getRoles().stream()
                .filter(x -> DBRole.getMuteRolesFromGuild(this.guild)
                        .stream()
                        .map(DBRole::roleId)
                        .toList()
                        .contains(x.getIdLong())).toList();
        if (roleList.isEmpty()) return null;
        return roleList.getFirst();
    }
}
