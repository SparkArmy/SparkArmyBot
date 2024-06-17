package de.sparkarmy.db;

import de.sparkarmy.utils.ErrorCodes;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public record DBMember(long mbrId, long userId, long guildId, boolean onServer, boolean isModerator) {

    public static long getDatabaseId(long userId, long guildId) throws SQLException {
        Connection conn = DatabaseSource.connection();
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "mbrId" FROM guilddata."tblMember" WHERE "fk_mbrUserId" = ? AND "fk_mbrGuildId" = ?;
                """);
        prepStmt.setLong(1, userId);
        prepStmt.setLong(2, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return ErrorCodes.SQL_QUERY_SELECT_HAS_NO_ROW.getId();
        long result = rs.getLong("mbrId");
        conn.close();
        return result;
    }

    public static @Nullable DBMember getDBMemberByDatabaseId(long memberDatabaseId) {
        try {
            Connection conn = DatabaseSource.connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT * FROM guilddata."tblMember" WHERE "mbrId" = ?;
                    """);
            prepStmt.setLong(1, memberDatabaseId);
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) return null;
            return new DBMember(
                    memberDatabaseId,
                    rs.getLong("fk_mbrUserId"),
                    rs.getLong("fk_mbrGuildId"),
                    rs.getBoolean("mbrOnServer"),
                    rs.getBoolean("mbrIsModerator"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    public long insertOrUpdateNewDatabaseMember() {
        try {
            Connection conn = DatabaseSource.connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    INSERT INTO guilddata."tblMember" ("fk_mbrUserId", "fk_mbrGuildId", "mbrOnServer", "mbrIsModerator")
                    VALUES (?,?,?,?) on conflict do update SET excluded."mbrOnServer" = "mbrOnServer",excluded."mbrIsModerator" = "mbrIsModerator";
                    """);
            prepStmt.setLong(1, this.userId);
            prepStmt.setLong(2, this.guildId);
            prepStmt.setBoolean(3, this.onServer);
            prepStmt.setBoolean(4, this.isModerator);
            long updateCount = prepStmt.executeUpdate();
            conn.close();
            return updateCount;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
