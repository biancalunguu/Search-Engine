package searchengine.query;

import java.util.List;

/**
 * Finds where a search term appears inside a file's content and extracts a
 * short surrounding text block (snippet) to display alongside the result.
 *
 * This is inspired by how Google's contextual snippets work — instead of always
 * showing the beginning of the file, we show the part that actually matched.
 */
public class SnippetGenerator {

    private static final int CONTEXT_RADIUS = 120;

    /**
     * Generates a contextual snippet for the given content and search terms.
     * Returns null if content is empty or no match is found.
     */
    public String generate(String content, List<String> searchTerms) {
        if (content == null || content.isBlank() || searchTerms == null || searchTerms.isEmpty()) {
            return null;
        }

        String lowerContent = content.toLowerCase();

        int matchIndex = -1;
        for (String term : searchTerms) {
            int idx = lowerContent.indexOf(term.toLowerCase());
            if (idx >= 0) {
                matchIndex = idx;
                break;
            }
        }

        if (matchIndex < 0) {
            int end = Math.min(content.length(), CONTEXT_RADIUS * 2);
            return content.substring(0, end).trim() + (content.length() > end ? "..." : "");
        }

        int start = Math.max(0, matchIndex - CONTEXT_RADIUS);
        int end   = Math.min(content.length(), matchIndex + CONTEXT_RADIUS);

        StringBuilder snippet = new StringBuilder();
        if (start > 0) snippet.append("...");
        snippet.append(content, start, end);
        if (end < content.length()) snippet.append("...");

        return snippet.toString().trim();
    }
}
