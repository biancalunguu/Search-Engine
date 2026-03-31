package searchengine.indexing;

import searchengine.config.ConfigurationManager;
import searchengine.model.FileRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * In charge of the full indexing pipeline:
 *
 *   1. FileCrawler - discover all files on disk
 *   2. DataExtractor - read metadata + content from each file
 *   3. DatabaseSynchronizer - compare with DB, insert/update/delete as needed
 *   4. TelemetryReporter - print summary
 */
public class IndexingService {

    private final ConfigurationManager config;

    public IndexingService() {
        this.config = ConfigurationManager.getInstance();
    }

    public void run() {
        String rootDir = config.getRootDirectory();
        System.out.println("[INFO] Root directory: " + rootDir);

        TelemetryReporter reporter = new TelemetryReporter();
        reporter.start();

        // step 1
        FileCrawler crawler        = new FileCrawler();
        List<Path>  discoveredPaths = crawler.crawl(Paths.get(rootDir));

        reporter.setTotalDiscovered(discoveredPaths.size());
        reporter.setPermissionErrors(crawler.getPermissionErrors());

        // step 2
        DataExtractor    extractor = new DataExtractor();
        List<FileRecord> records   = new ArrayList<>();

        System.out.println("[INFO] Extracting file data...");
        for (int i = 0; i < discoveredPaths.size(); i++) {
            if ((i + 1) % 500 == 0) {
                System.out.printf("[INFO] Processed %d / %d files...%n", i + 1, discoveredPaths.size());
            }

            try {
                records.add(extractor.extract(discoveredPaths.get(i)));
            } catch (IOException e) {
                System.err.println("[WARN] Could not extract: " + discoveredPaths.get(i) + " — " + e.getMessage());
            }
        }

        // step 3
        System.out.println("[INFO] Syncing to database...");
        try {
            DatabaseSynchronizer sync    = new DatabaseSynchronizer();
            Map<String, LocalDateTime> indexed = sync.getIndexedFiles();
            sync.sync(records, indexed);
            reporter.printReport(sync);
        } catch (SQLException e) {
            System.err.println("[ERROR] Database error during sync: " + e.getMessage());
        }
    }
}
