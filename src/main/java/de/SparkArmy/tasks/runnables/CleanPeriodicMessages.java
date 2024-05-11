package de.SparkArmy.tasks.runnables;

import de.SparkArmy.db.DatabaseSource;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CleanPeriodicMessages implements Runnable {

    Logger logger = Util.logger;

    @Override
    public void run() {
        ShardManager shardManager = Util.controller.getMain().getJdaApi().getShardManager();
        for (Long id : getChannelIds()) {
            TextChannel textChannel = shardManager.getTextChannelById(id);
            if (textChannel == null) continue;
            textChannel.getHistory().retrievePast(100)
                    .flatMap(textChannel::deleteMessages)
                    .queue();
        }
    }


    private List<Long> getChannelIds() {
        List<Long> values = new ArrayList<>();
        try {
            Connection conn = DatabaseSource.connection();
            ResultSet rs = conn.prepareStatement("""
                            SELECT "fk_pcdChannelId" FROM globaldata."tblPeriodicCleanData" WHERE "pcdActive" = true;
                            """)
                    .executeQuery();
            while (rs.next()) {
                values.add(rs.getLong(1));
            }
            conn.close();
            return values;

        } catch (SQLException e) {
            logger.error("SQL error: ", e);
            return values;
        }
    }
}
