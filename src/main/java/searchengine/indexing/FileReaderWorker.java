package searchengine.indexing;

import searchengine.model.FileRecord;
import searchengine.ranking.PathScorer;

import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;

public class FileReaderWorker implements Runnable {

    private static final FileRecord POISON_PILL = new FileRecord();

    private final BlockingQueue<FileRecord> queue;
    private final Path filePath;
    private final DataExtractor extractor;
    private final PathScorer pathScorer;

    public FileReaderWorker(BlockingQueue<FileRecord> queue, Path filePath,
                            DataExtractor extractor, PathScorer pathScorer) {
        this.queue = queue;
        this.filePath = filePath;
        this.extractor = extractor;
        this.pathScorer = pathScorer;
    }

    @Override
    public void run() {
        try {
            FileRecord record = extractor.extract(filePath);
            record.setPathScore(pathScorer.score(record));
            queue.put(record);
        } catch (Exception e) {
            System.err.println("[WARN] Reader failed for: " + filePath + " - " + e.getMessage());
        } finally {
            try {
                queue.put(POISON_PILL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static FileRecord getPoisonPill() {
        return POISON_PILL;
    }
}
