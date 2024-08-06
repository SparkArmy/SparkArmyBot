package de.sparkarmy.data.db.enties;

import de.sparkarmy.data.db.DatabaseSource;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public record DBRole(
        long roleId,
        long guildId,
        boolean isMuteRole,
        boolean isWarnRole,
        boolean isTicketPing) {
    public static List<DBRole> getRolesFromGuild(long guildId) {
        List<DBRole> roles = new ArrayList<>();
        try {
            Connection conn = DatabaseSource.connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT * FROM bot."table_roles" WHERE "fk_rolguildid" = ?;
                    """);
            prepStmt.setLong(1, guildId);

            ResultSet rs = prepStmt.executeQuery();
            while (rs.next()) {
                roles.add(new DBRole(
                        rs.getLong("pk_rolid"),
                        rs.getLong("fk_rolguildid"),
                        rs.getBoolean("rolismute"),
                        rs.getBoolean("roliswarn"),
                        rs.getBoolean("rolisticketping")
                ));
            }
            conn.close();
            return roles;
        } catch (SQLException e) {
            return roles;
        }
    }

    public static List<DBRole> getWarnRolesFromGuild(long guildId) {
        return getRolesFromGuild(guildId).stream().filter(DBRole::isWarnRole).toList();
    }

    public static List<DBRole> getWarnRolesFromGuild(@NotNull Guild guild) {
        return getWarnRolesFromGuild(guild.getIdLong());
    }

    public static List<DBRole> getMuteRolesFromGuild(long guildId) {
        return getRolesFromGuild(guildId).stream().filter(DBRole::isMuteRole).toList();
    }

    public static List<DBRole> getMuteRolesFromGuild(@NotNull Guild guild) {
        return getMuteRolesFromGuild(guild.getIdLong());
    }

    // TODO add getter for ticketPing
    // TODO add create&UpdateEntry Method
    // TODO add deleteEntry Method
}
