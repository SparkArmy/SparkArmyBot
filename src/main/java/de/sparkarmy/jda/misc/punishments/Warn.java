//package de.sparkarmy.jda.misc.punishments;
//
//import de.sparkarmy.data.value.RoleValue;
//import de.sparkarmy.data.value.RoleValueUtil;
//import net.dv8tion.jda.api.entities.Member;
//import net.dv8tion.jda.api.entities.Role;
//import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
//import net.dv8tion.jda.api.interactions.InteractionHook;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.List;
//import java.util.Objects;
//
//public class Warn extends AbstractPunishment {
//    private final InteractionHook hook;
//
//    public Warn(@NotNull SlashCommandInteractionEvent event) {
//        super(event);
//        event.deferReply(true).queue();
//        this.hook = event.getHook();
//    }
//
//    public void createWarn() {
//        if (checkPrecondition(this.hook)) return;
//        checkMemberConditions(this.hook)
//                .onErrorFlatMap(Objects::isNull,
//                        (error) -> hook.editOriginal(this.userPunishmentBundle.getString("checkPrecondition.unknownUser")))
//                .flatMap(Objects::nonNull, memberAsObject -> {
//                    long punishmentEntryCreated = createPunishmentEntry(PunishmentType.WARN);
//                    if (punishmentEntryCreated < 0) {
//                        return hook.editOriginal(String.format(standardPhrases.getString("replies.dbErrorReply"), punishmentEntryCreated));
//                    } else if (punishmentEntryCreated == 0) {
//                        return hook.editOriginal(this.userPunishmentBundle.getString("createPunishment.noDataWasInserted"));
//                    }
//
//                    List<Role> warnRoleList = getWarnRole();
//                    if (warnRoleList == null) {
//                        return createLogMessageRestAction(PunishmentType.WARN)
//                                .flatMap(x -> createUserLogRestAction(x, PunishmentType.WARN))
//                                .flatMap(x -> hook.editOriginal(this.userPunishmentBundle.getString("checkPrecondition.noPunishmentRoleSet")));
//                    }
//
//                    return this.guild.modifyMemberRoles((Member) memberAsObject, warnRoleList)
//                            .reason(this.reason)
//                            .flatMap(x -> createLogMessageRestAction(PunishmentType.WARN))
//                            .flatMap(x -> createUserLogRestAction(x, PunishmentType.WARN))
//                            .flatMap(x -> hook.editOriginal(this.userPunishmentBundle.getString("createPunishment.successfully")));
//                })
//                .queue();
//    }
//
//    private @Nullable List<Role> getWarnRole() {
//        List<Role> roleList = this.guild.getRoles().stream()
//                .filter(x -> RoleValueUtil.checkIsApplied(DBRole.getDBRoleByRole(x).getRoleValuesRaw(), RoleValue.WARN_ROLE))
//                .toList();
//        if (roleList.isEmpty()) return null;
//        return roleList;
//    }
//}
