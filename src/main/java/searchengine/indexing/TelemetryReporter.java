package searchengine.indexing;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Monitors indexing progress and generates a summary report at the end.
 *
 * Usage:
 *   reporter.start();
 *   reporter.setTotalDiscovered(n);
 *   reporter.printReport(sync);
 */
public class TelemetryReporter {

    private LocalDateTime startTime;
    private int totalDiscovered;
    private int permissionErrors;

    public void start() {
        startTime = LocalDateTime.now();
        System.out.println("[INFO] Indexing started at " + startTime);
    }

    public void setTotalDiscovered(int count) {
        this.totalDiscovered = count;
        System.out.println("[INFO] Discovered " + count + " file(s) on disk.");
    }

    public void setPermissionErrors(int count) {
        this.permissionErrors = count;
    }

    /** Prints the final summary after the DatabaseSynchronizer has finished. */
    public void printReport(DatabaseSynchronizer sync) {
        LocalDateTime endTime  = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);

        System.out.println("\n=== Indexing Report ===");
        System.out.println("Duration: " + formatDuration(duration));
        System.out.println("Discovered: " + totalDiscovered);
        System.out.println("Inserted: " + sync.getInserted());
        System.out.println("Updated: " + sync.getUpdated());
        System.out.println("Deleted: " + sync.getDeleted());
        System.out.println("Skipped: " + sync.getSkipped());
        System.out.println("Errors: " + (sync.getErrors() + permissionErrors));
        System.out.println("========================");
    }

    private String formatDuration(Duration d) {
        long minutes = d.toMinutes();
        long seconds = d.toSecondsPart();
        long millis  = d.toMillisPart();
        if (minutes > 0) return minutes + "m " + seconds + "s";
        if (seconds > 0) return seconds + "s " + millis + "ms";
        return millis + "ms";
    }
}
