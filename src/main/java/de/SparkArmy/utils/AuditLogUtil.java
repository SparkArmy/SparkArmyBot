package de.SparkArmy.utils;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.TargetType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AuditLogUtil {

    private static List<AuditLogEntry> getAuditLogEntryList(@NotNull List<AuditLogEntry> auditLogEntries, ActionType actionType){
       return auditLogEntries.stream().filter(x->x.getType().equals(actionType)).toList();
    }

    private static @NotNull List<AuditLogEntry> getAuditLogEntryList(@NotNull List<AuditLogEntry> auditLogEntries, @NotNull List<ActionType> actionTypes){
        List<AuditLogEntry> entries = new ArrayList<>();
        actionTypes.forEach(x-> entries.addAll(getAuditLogEntryList(auditLogEntries,x)));
        MainUtil.logger.info(entries.toString());
        return entries;
    }

    private static List<AuditLogEntry> getLogEntries(@NotNull Guild guild){
        return guild.retrieveAuditLogs().complete();
    }

    public static @Nullable AuditLogEntry getAuditLogEntryByUser(User user, ActionType actionType,@NotNull List<AuditLogEntry> auditLogEntries){
       List<AuditLogEntry> entries = getAuditLogEntryList(auditLogEntries,actionType);
       List<AuditLogEntry> filteredEntries = new ArrayList<>();
       entries.forEach(x->{
           String userId = x.getTargetType().equals(TargetType.MEMBER) ? x.getTargetId() : null;
           if (userId != null){
               if (userId.equals(user.getId())){
                   filteredEntries.add(x);
               }
           }
       });
       if (filteredEntries.isEmpty()) return null;
       return filteredEntries.get(0);
    }

    @SuppressWarnings("unused")
    public static @Nullable AuditLogEntry getAuditLogEntryByUser(User user, List<ActionType> actionTypes, @NotNull List<AuditLogEntry> auditLogEntries){
        List<AuditLogEntry> entries = getAuditLogEntryList(auditLogEntries,actionTypes);
        List<AuditLogEntry> filteredEntries = new ArrayList<>();
        entries.forEach(x->{
            String userId = x.getTargetType().equals(TargetType.MEMBER) ? x.getTargetId() : null;
            if (userId != null){
                if (userId.equals(user.getId())){
                    filteredEntries.add(x);
                }
            }
        });
        if (filteredEntries.isEmpty()) return null;
        return filteredEntries.get(0);
    }

    @SuppressWarnings("unused")
    public static @Nullable AuditLogEntry getAuditLogEntryByUser(User user, List<ActionType> actionTypes, Guild guild){
        List<AuditLogEntry> auditLogEntries = getLogEntries(guild);
        List<AuditLogEntry> entries = getAuditLogEntryList(auditLogEntries,actionTypes);
        List<AuditLogEntry> filteredEntries = new ArrayList<>();
        entries.forEach(x->{
            String userId = x.getTargetType().equals(TargetType.MEMBER) ? x.getTargetId() : null;
            if (userId != null){
                if (userId.equals(user.getId())){
                    filteredEntries.add(x);
                }
            }
        });
        if (filteredEntries.isEmpty()) return null;
        return filteredEntries.get(0);
    }

    public static @Nullable AuditLogEntry getAuditLogEntryByUser(User user, ActionType actionType,Guild guild){
        List<AuditLogEntry> auditLogEntries = getLogEntries(guild);
        List<AuditLogEntry> entries = getAuditLogEntryList(auditLogEntries,actionType);
        List<AuditLogEntry> filteredEntries = new ArrayList<>();
        entries.forEach(x->{
            String userId = x.getTargetType().equals(TargetType.MEMBER) ? x.getTargetId() : null;
            if (userId != null){
                if (userId.equals(user.getId())){
                    filteredEntries.add(x);
                }
            }
        });
        if (filteredEntries.isEmpty()) return null;
        return filteredEntries.get(0);
    }
}
