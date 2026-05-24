package searchengine.widgets;

import searchengine.model.FileRecord;
import searchengine.model.SearchResult;

import java.util.List;
import java.util.Locale;

public class SearchContext {

    private static final double PREDOMINANT_THRESHOLD = 0.5;

    private final String query;
    private final List<SearchResult> results;

    public SearchContext(String query, List<SearchResult> results) {
        this.query = query == null ? "" : query;
        this.results = results == null ? List.of() : results;
    }

    public String getQuery() {
        return query;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public int getResultCount() {
        return results.size();
    }

    public boolean hasResults() {
        return !results.isEmpty();
    }

    public boolean queryContains(String term) {
        if (term == null || term.isBlank()) {
            return false;
        }

        return query.toLowerCase(Locale.ROOT)
                .contains(term.toLowerCase(Locale.ROOT));
    }

    public long countByExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return 0;
        }

        return results.stream()
                .map(SearchResult::getFile)
                .map(FileRecord::getExtension)
                .filter(ext -> ext != null && ext.equalsIgnoreCase(extension))
                .count();
    }

    public long countImages() {
        return results.stream()
                .map(SearchResult::getFile)
                .filter(FileRecord::isImageFile)
                .count();
    }

    public double imageRatio() {
        if (results.isEmpty()) {
            return 0.0;
        }

        return (double) countImages() / results.size();
    }

    public double logRatio() {
        if (results.isEmpty()) {
            return 0.0;
        }

        return (double) countByExtension("log") / results.size();
    }

    public boolean resultsArePredominantlyImages() {
        return hasResults() && imageRatio() > PREDOMINANT_THRESHOLD;
    }

    public boolean resultsArePredominantlyLogs() {
        return hasResults() && logRatio() > PREDOMINANT_THRESHOLD;
    }

    public boolean hasManyImages() {
        return countImages() >= 2;
    }

    public boolean hasManyLogs() {
        return countByExtension("log") >= 2;
    }

    public boolean queryLooksImageRelated() {
        return queryContains("image")
                || queryContains("images")
                || queryContains("img")
                || queryContains("photo")
                || queryContains("picture")
                || queryContains("gallery")
                || queryContains("color:");
    }

    public boolean queryLooksLogRelated() {
        return queryContains("log")
                || queryContains("logs")
                || queryContains("error")
                || queryContains("exception")
                || queryContains("warning")
                || queryContains("warn")
                || queryContains("debug");
    }

    public String describeImageActivationReason() {
        if (resultsArePredominantlyImages()) {
            return "Most results are image files.";
        }

        if (hasManyImages()) {
            return "The result set contains multiple image files.";
        }

        if (queryLooksImageRelated()) {
            return "The query is image-related.";
        }

        return "Image widget is not relevant.";
    }

    public String describeLogActivationReason() {
        if (resultsArePredominantlyLogs()) {
            return "Most results are .log files.";
        }

        if (hasManyLogs()) {
            return "The result set contains multiple .log files.";
        }

        if (queryLooksLogRelated()) {
            return "The query is log-related.";
        }

        return "Log widget is not relevant.";
    }
}