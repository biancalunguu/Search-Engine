package searchengine.indexing;

import searchengine.config.ConfigurationManager;
import searchengine.model.FileRecord;
import searchengine.ranking.PathScorer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class ParallelIndexingService {

    private static final int READER_THREADS = 4;
    private static final int QUEUE_CAPACITY  = 500;

    private final ConfigurationManager config;

    public ParallelIndexingService() {
        this.config = ConfigurationManager.getInstance();
    }

    public void run() {
        String rootDir = config.getRootDirectory();
        System.out.println("[INFO] Root directory: " + rootDir);

        TelemetryReporter reporter = new TelemetryReporter();
        reporter.start();

        // step 1 – discover files
        FileCrawler crawler = new FileCrawler();
        List<Path> paths = crawler.crawl(Paths.get(rootDir));
        reporter.setTotalDiscovered(paths.size());
        reporter.setPermissionErrors(crawler.getPermissionErrors());

        if (paths.isEmpty()) {
            System.out.println("[INFO] Nothing to index.");
            return;
        }

        // step 2 – load current DB state (single-threaded, before any reader starts)
        DatabaseSynchronizer sync;
        Map<String, LocalDateTime> indexed;
        try {
            sync    = new DatabaseSynchronizer();
            indexed = sync.getIndexedFiles();
        } catch (SQLException e) {
            System.err.println("[ERROR] Cannot connect to database: " + e.getMessage());
            return;
        }

        // step 3 – shared queue; each reader produces one record then one poison pill
        BlockingQueue<FileRecord> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

        // step 4 – single writer thread waits on queue; stops after paths.size() poison pills
        Thread writerThread = new Thread(
                new DatabaseWriterWorker(queue, sync, indexed, paths.size()),
                "db-writer"
        );
        writerThread.start();

        // step 5 – reader pool processes every file in parallel
        // each worker gets its own extractor to avoid shared state between threads
        ExecutorService readerPool = Executors.newFixedThreadPool(READER_THREADS);

        System.out.println("[INFO] Parsing files with " + READER_THREADS + " reader threads...");
        for (Path path : paths) {
            readerPool.submit(new FileReaderWorker(queue, path, new DataExtractor(), new PathScorer()));
        }

        // step 6 – shut down pool (no new tasks) and wait for all readers to finish
        readerPool.shutdown();
        try {
            readerPool.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // step 7 – wait for writer to drain the queue and exit
        try {
            writerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // step 8 – delete DB records that no longer exist on disk (sequential, safe after writer exits)
        Set<String> seenPaths = new HashSet<>();
        for (Path p : paths) {
            seenPaths.add(p.toAbsolutePath().toString());
        }
        for (String path : indexed.keySet()) {
            if (!seenPaths.contains(path)) {
                try {
                    sync.deleteOne(path);
                } catch (SQLException e) {
                    System.err.println("[ERROR] Could not delete: " + path + " - " + e.getMessage());
                    sync.incrementErrors();
                }
            }
        }

        reporter.printReport(sync);
    }
}
