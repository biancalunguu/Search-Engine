package searchengine.ui;

import searchengine.model.FileRecord;
import searchengine.model.SearchResult;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Formats search results and prints them to the console.
 * Each result shows the filename, full path, size, last-modified date,
 * and a contextual text snippet (preview).
 */
public class ResultRenderer {

    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String            SEPARATOR = "─".repeat(62);

    public void render(List<SearchResult> results, String query) {
        if (results.isEmpty()) {
            System.out.println("\n  No results found for: \"" + query + "\"");
            return;
        }

        System.out.println("\n  Found " + results.size() + " result(s) for: \"" + query + "\"");
        System.out.println(SEPARATOR);

        for (int i = 0; i < results.size(); i++) {
            SearchResult  result = results.get(i);
            FileRecord    file   = result.getFile();

            System.out.printf("[%d] %s%n", i + 1, file.getFileName());
            System.out.println("    Path     : " + file.getFilePath());
            System.out.printf ("    Size     : %s  |  Modified: %s%n",
                    formatSize(file.getSizeBytes()),
                    file.getLastModified() != null ? file.getLastModified().format(DATE_FMT) : "N/A");

            if (result.getSnippet() != null && !result.getSnippet().isBlank()) {
                System.out.println("    Preview  :");
                for (String line : result.getSnippet().split("\n")) {
                    String trimmed = line.stripTrailing();
                    if (!trimmed.isEmpty()) {
                        System.out.println("      " + trimmed);
                    }
                }
            }

            System.out.println(SEPARATOR);
        }
    }

    private String formatSize(long bytes) {
        if (bytes < 1_024)           return bytes + " B";
        if (bytes < 1_048_576)       return String.format("%.1f KB", bytes / 1_024.0);
        if (bytes < 1_073_741_824)   return String.format("%.1f MB", bytes / 1_048_576.0);
        return                              String.format("%.2f GB", bytes / 1_073_741_824.0);
    }
}
