package de.sparkarmy.data.db.enties;

import de.sparkarmy.data.db.DatabaseSource;
import de.sparkarmy.utils.ErrorCodes;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings("unused")
public record DBMember(long mbrId, long userId, long guildId, boolean isModerator) {

    public static long getDatabaseId(long userId, long guildId) throws SQLException {
        Connection conn = DatabaseSource.connection();
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "pk_mbrid" FROM bot."table_member" WHERE "fk_mbruserid" = ? AND "fk_mbrguildid" = ?;
                """);
        prepStmt.setLong(1, userId);
        prepStmt.setLong(2, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return ErrorCodes.SQL_QUERY_SELECT_HAS_NO_ROW.getId();
        long result = rs.getLong("pk_mbrid");
        conn.close();
        return result;
    }

    public static @Nullable DBMember getDBMemberByDatabaseId(long memberDatabaseId) {
        try (Connection conn = DatabaseSource.connection()) {
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT * FROM bot."table_member" WHERE "pk_mbrid" = ?;
                    """);
            prepStmt.setLong(1, memberDatabaseId);
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) return null;
            return new DBMember(
                    memberDatabaseId,
                    rs.getLong("fk_mbruserid"),
                    rs.getLong("fk_mbrguildid"),
                    rs.getBoolean("mbrismod"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long insertOrUpdateNewDatabaseMember() {
        try (Connection conn = DatabaseSource.connection()) {
            PreparedStatement prepStmt = conn.prepareStatement("""
                    INSERT INTO bot."table_member" ("fk_mbruserid", "fk_mbrguildid", "mbrismod")
                    VALUES (?,?,?,?) on conflict do update SET excluded."mbrismod" = "mbrismod";
                    """);
            prepStmt.setLong(1, this.userId);
            prepStmt.setLong(2, this.guildId);
            prepStmt.setBoolean(4, this.isModerator);
            long updateCount = prepStmt.executeUpdate();
            conn.close();
            return updateCount;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
