package searchengine.query;

import java.util.ArrayList;
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
        List<String> colorTerms = new ArrayList<>();

        String[] tokens = rawQuery.trim().split("\\s+");
        List<String> processed = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.isBlank()) continue;

            if (token.equalsIgnoreCase("or") && !processed.isEmpty() && i + 1 < tokens.length) {
                String next = tokens[i + 1];
                if (!next.isBlank() && !next.equalsIgnoreCase("or")) {
                    String last = processed.remove(processed.size() - 1);
                    String grouped = last + "|" + next;
                    i++;
                    while (i + 1 < tokens.length && tokens[i + 1].equalsIgnoreCase("or") && i + 2 < tokens.length) {
                        String further = tokens[i + 2];
                        if (!further.isBlank() && !further.equalsIgnoreCase("or")) {
                            grouped = grouped + "|" + further;
                            i += 2;
                        } else {
                            break;
                        }
                    }
                    processed.add(grouped);
                } else {
                    processed.add(token);
                }
            } else {
                processed.add(token);
            }
        }

        for (String token : processed) {
            String lower = token.toLowerCase();
            if (lower.startsWith("content:") && token.length() > "content:".length()) {
                contentTerms.add(token.substring("content:".length()));
            } else if (lower.startsWith("path:") && token.length() > "path:".length()) {
                pathTerms.add(token.substring("path:".length()));
            } else if (lower.startsWith("color:") && token.length() > "color:".length()) {
                colorTerms.add(token.substring("color:".length()));
            } else {
                generalTerms.add(token);
            }
        }

        return new ParsedQuery(rawQuery.trim(), generalTerms, contentTerms, pathTerms, colorTerms);
    }


    /** Holds the result of parsing a raw query string. */
    public static class ParsedQuery {

        private final String original;
        private final List<String> generalTerms;
        private final List<String> contentTerms;
        private final List<String> pathTerms;
        private final List<String> colorTerms;

        public ParsedQuery(String original,
                           List<String> generalTerms,
                           List<String> contentTerms,
                           List<String> pathTerms,
                           List<String> colorTerms) {
            this.original = original;
            this.generalTerms = generalTerms;
            this.contentTerms = contentTerms;
            this.pathTerms = pathTerms;
            this.colorTerms = colorTerms;
        }

        public String getOriginal() { return original; }
        public List<String> getGeneralTerms() { return generalTerms; }
        public List<String> getContentTerms() { return contentTerms; }
        public List<String> getPathTerms() { return pathTerms; }
        public List<String> getColorTerms() { return colorTerms; }

        public List<String> getTerms() {
            List<String> all = new ArrayList<>();
            all.addAll(generalTerms);
            all.addAll(contentTerms);
            all.addAll(pathTerms);
            all.addAll(colorTerms);
            return all;
        }

        public boolean hasQualifiedTerms() {
            return !contentTerms.isEmpty() || !pathTerms.isEmpty() || !colorTerms.isEmpty();
        }

        public boolean isMultiWord() {
            return getTerms().size() > 1;
        }

        public String toFullTextQuery() {
            return termsForContentSearch().stream()
                    .map(t -> {
                        if (t.contains("|")) {
                            String[] parts = t.split("\\|");
                            StringBuilder sb = new StringBuilder("+(");
                            for (int i = 0; i < parts.length; i++) {
                                if (i > 0) sb.append(" ");
                                String term = parts[i].trim();
                                if (!term.endsWith("*")) {
                                    term = term + "*";
                                }
                                sb.append(term);
                            }
                            sb.append(")");
                            return sb.toString();
                        } else {
                            String term = t.trim();
                            if (!term.endsWith("*")) {
                                term = term + "*";
                            }
                            return "+" + term;
                        }
                    })
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
