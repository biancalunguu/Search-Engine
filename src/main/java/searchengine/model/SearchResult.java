package searchengine.model;

/**
 * Wraps a FileRecord with a contextual text snippet that highlights
 * where the search terms were found inside the file.
 */
public class SearchResult {

    private final FileRecord file;
    private final String snippet; // may be null if file has no text content

    public SearchResult(FileRecord file, String snippet) {
        this.file = file;
        this.snippet = snippet;
    }

    public FileRecord getFile() { return file; }
    public String getSnippet() { return snippet; }
}
