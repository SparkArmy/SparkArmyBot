package de.SparkArmy.tasks.runnables;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.Postgres;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeleteOldMessageAttachments implements Runnable {

    private final ConfigController controller;

    public DeleteOldMessageAttachments(ConfigController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        Postgres db = controller.getMain().getPostgres();
        List<Long> msaIds = new ArrayList<>();

        for (Object o : db.getMessageAttachmentsByMessageIDs(db.getMessageDataBeforeTimestamp(LocalDateTime.now().minusDays(21)))) {
            JSONObject jsonObject = (JSONObject) o;
            msaIds.add(jsonObject.getLong("msaId"));
        }
        db.deleteMessageAttachments(msaIds);
    }
}
