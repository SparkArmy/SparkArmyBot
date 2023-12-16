package de.SparkArmy.tasks.runnables;

import de.SparkArmy.db.DatabaseAction;

import java.time.LocalDateTime;

public class DeleteOldMessageAttachments implements Runnable {

    public DeleteOldMessageAttachments() {
    }

    @Override
    public void run() {
        DatabaseAction db = new DatabaseAction();
        db.deleteMessageAttachmentsBeforeSpecificTimestamp(LocalDateTime.now().minusDays(21));
    }
}
