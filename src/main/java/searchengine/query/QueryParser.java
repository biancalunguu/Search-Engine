package searchengine.query;

import java.util.ArrayList;
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
    //I2 s1: add structured input and queries, the input is not taken as a whole, but broken down in parts
    public ParsedQuery parse(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return null;
        }

        List<String> generalTerms = new ArrayList<>();
        List<String> contentTerms = new ArrayList<>();
        List<String> pathTerms = new ArrayList<>();

        String[] tokens = rawQuery.trim().toLowerCase().split("\\s+"); // \\s+ means split on one or more whitespace characters
        for (String token : tokens) {
            if (token.isBlank()) continue;

            if (token.startsWith("content:") && token.length() > "content:".length()) {
                contentTerms.add(token.substring("content:".length()));
            } else if (token.startsWith("path:") && token.length() > "path:".length()) {
                pathTerms.add(token.substring("path:".length()));
            } else {
                generalTerms.add(token);
            }
        }

        return new ParsedQuery(rawQuery.trim(), generalTerms, contentTerms, pathTerms);
    }


    /** Holds the result of parsing a raw query string. */
    public static class ParsedQuery {

        private final String original;
        private final List<String> generalTerms;
        private final List<String> contentTerms;
        private final List<String> pathTerms;

        public ParsedQuery(String original, List<String> generalTerms, List<String> contentTerms, List<String> pathTerms) {
            this.original = original;
            this.generalTerms = generalTerms;
            this.contentTerms = contentTerms;
            this.pathTerms = pathTerms;
        }

        public String getOriginal() { return original; }
        public List<String> getGeneralTerms() { return generalTerms; }
        public List<String> getContentTerms() { return contentTerms; }
        public List<String> getPathTerms() { return pathTerms; }

        public List<String> getTerms() {
            List<String> all = new ArrayList<>();
            all.addAll(generalTerms);
            all.addAll(contentTerms);
            all.addAll(pathTerms);
            return all;
        }

        public boolean hasQualifiedTerms() {
            return !contentTerms.isEmpty() || !pathTerms.isEmpty();
        }

        public boolean isMultiWord() {
            return getTerms().size() > 1;
        }

        public String toFullTextQuery() {
            return termsForContentSearch().stream()
                    .map(t -> "+" + t + "*")
                    .collect(Collectors.joining(" "));
        }

        public List<String> termsForContentSearch() {
            List<String> terms = new ArrayList<>();
            terms.addAll(generalTerms);
            terms.addAll(contentTerms);
            return terms;
        }
    }
}
