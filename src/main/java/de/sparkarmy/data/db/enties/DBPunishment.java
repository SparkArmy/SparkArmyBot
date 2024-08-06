package de.sparkarmy.data.db.enties;

import de.sparkarmy.data.db.DatabaseSource;
import de.sparkarmy.jda.misc.punishments.PunishmentType;
import de.sparkarmy.utils.Util;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public record DBPunishment(Guild guild, User moderator, User target, PunishmentType type, String reason,
                           Timestamp timestamp) {

    private final static Logger logger = LoggerFactory.getLogger("DBPunishment");

    public static @NotNull List<DBPunishment> getPunishmentsByGuild(@NotNull Guild guild) {
        return getPunishmentsByGuildId(guild.getIdLong());
    }

    public static @NotNull List<DBPunishment> getPunishmentsByGuildId(long guildId) {
        List<DBPunishment> results = new ArrayList<>();

        ShardManager manager = Util.controller.main().getJdaApi().getShardManager();

        try {
            Connection conn = DatabaseSource.connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT * FROM bot."table_punishment" tP
                    INNER JOIN bot."table_member" tM ON tP."fk_psmtarget" = tM."pk_mbrid"
                    INNER JOIN bot."table_guild" tG ON tM."fk_mbrguildid" = tG."pk_gldid"
                    WHERE tG."pk_gldid" = ?;
                    """);
            prepStmt.setLong(1, guildId);
            ResultSet rs = prepStmt.executeQuery();

            while (rs.next()) {

                DBMember dbMemberTarget = DBMember.getDBMemberByDatabaseId(rs.getLong("fk_psmtarget"));
                if (dbMemberTarget == null) continue;
                User userTarget = manager.getUserById(dbMemberTarget.userId());
                DBMember dbMemberModerator = DBMember.getDBMemberByDatabaseId(rs.getLong("fk_psmexecuter"));
                if (dbMemberModerator == null) continue;
                User userModerator = manager.getUserById(dbMemberModerator.userId());
                Guild guild = manager.getGuildById(guildId);
                PunishmentType type = PunishmentType.getById(rs.getInt("fk_psmtype"));

                results.add(new DBPunishment(
                        guild,
                        userModerator,
                        userTarget,
                        type,
                        rs.getString("psmreason"),
                        rs.getTimestamp("psmtimestamp")
                ));
            }
            conn.close();
            return results;
        } catch (SQLException e) {
            return results;
        }
    }

    public static long getPunishmentCountForGuild(@NotNull Guild guild) {
        try {
            Connection conn = DatabaseSource.connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT COUNT(*) FROM bot."table_punishment" tP
                    INNER JOIN bot."table_member" tM ON tP."fk_psmexecuter" = tM."pk_mbrid"
                    INNER JOIN bot."table_guild" tG ON tM."fk_mbrguildid" = tG."pk_gldid"
                    WHERE tG."pk_gldid" = ?
                    """);
            prepStmt.setLong(1, guild.getIdLong());
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) {
                conn.close();
                logger.error("Can't get the punishment count: ", new IllegalStateException("SELECT COUNT() always have a first row"));
                return 0;
            }
            long count = rs.getLong(1);
            conn.close();
            return count;
        } catch (SQLException e) {
            logger.error("Can't get the punishment count: ", e);
            return 0;
        }
    }

    public long createPunishmentEntry() {
        try {
            Connection conn = DatabaseSource.connection();
            PreparedStatement insertData = conn.prepareStatement("""
                    INSERT INTO bot."table_punishment" (fk_psmexecuter, fk_psmtarget, fk_psmtype, psmreason)
                    VALUES (?,?,?,?,now())
                    """);
            long guildId = this.guild.getIdLong();
            insertData.setLong(1, DBMember.getDatabaseId(this.moderator.getIdLong(), guildId));
            insertData.setLong(2, DBMember.getDatabaseId(this.target.getIdLong(), guildId));
            insertData.setLong(3, this.type.getId());
            insertData.setString(4, this.reason);
            long updatedRows = insertData.executeUpdate();
            conn.close();
            return updatedRows;
        } catch (SQLException e) {
            logger.error("Can't create Punishment-Entry: ", e);
            return -200;
        }

    }
}
