package searchengine.indexing;

import searchengine.model.FileRecord;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class DatabaseWriterWorker implements Runnable {

    private final BlockingQueue<FileRecord> queue;
    private final DatabaseSynchronizer sync;
    private final Map<String, LocalDateTime> indexed;
    private final int totalFiles;

    public DatabaseWriterWorker(BlockingQueue<FileRecord> queue,
                                DatabaseSynchronizer sync,
                                Map<String, LocalDateTime> indexed,
                                int totalFiles) {
        this.queue = queue;
        this.sync = sync;
        this.indexed = indexed;
        this.totalFiles = totalFiles;
    }

    @Override
    public void run() {
        int poisonPillsReceived = 0;
        try {
            while (poisonPillsReceived < totalFiles) {
                FileRecord record = queue.take();

                if (record == FileReaderWorker.getPoisonPill()) {
                    poisonPillsReceived++;
                    continue;
                }

                String path = record.getFilePath();
                LocalDateTime dbModified = indexed.get(path);

                try {
                    sync.syncOne(record, dbModified);
                } catch (SQLException e) {
                    System.err.println("[ERROR] Writer failed for: " + path + " - " + e.getMessage());
                    sync.incrementErrors();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
