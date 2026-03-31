package searchengine.query;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Analyzes raw user input and turns it into a ParsedQuery that the
 * SearchExecutor can translate into SQL.
 */
public class QueryParser {

    /**
     * Parses the raw query string.
     * @return a ParsedQuery, or null if the input is blank
     */
    public ParsedQuery parse(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return null;
        }

        String cleaned = rawQuery.trim().toLowerCase();

        List<String> terms = Arrays.stream(cleaned.split("\\s+"))
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toList());

        return new ParsedQuery(rawQuery.trim(), terms);
    }


    /** Holds the result of parsing a raw query string. */
    public static class ParsedQuery {

        private final String       original; // untouched user input
        private final List<String> terms;    // cleaned, lowercased tokens

        public ParsedQuery(String original, List<String> terms) {
            this.original = original;
            this.terms    = terms;
        }

        public String       getOriginal() { return original; }
        public List<String> getTerms()    { return terms; }
        public boolean      isMultiWord() { return terms.size() > 1; }

        public String toFullTextQuery() {
            return terms.stream()
                    .map(t -> "+" + t + "*")
                    .collect(Collectors.joining(" "));
        }
    }
}
