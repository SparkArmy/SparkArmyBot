package de.SparkArmy.tasks.runnables;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.Postgres;

import java.time.LocalDateTime;
import java.util.List;

public class DeleteOldMessageAttachments implements Runnable {

    private final ConfigController controller;

    public DeleteOldMessageAttachments(ConfigController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        Postgres db = controller.getMain().getPostgres();
        List<Long> msaIds = db.getMessageAttachmentsIdsByMessageIDs(db.getMessageDataBeforeTimestamp(LocalDateTime.now().minusDays(21)));

        db.deleteMessageAttachments(msaIds);
    }
}
