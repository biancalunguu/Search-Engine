package searchengine.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SynonymDecorator extends QueryBuilderDecorator {
    private static final Map<String, List<String>> SYNONYMS = Map.of(
            "img", List.of("image", "photo"),
            "txt", List.of("text", "document"),
            "fn", List.of("function", "method"),
            "dir", List.of("directory", "folder")
    );

    public SynonymDecorator(QueryBuilder decoratedBuilder) {
        super(decoratedBuilder);
    }

    @Override
    public String buildQuery(String rawInput) {
        String query = super.buildQuery(rawInput);
        if (query == null || query.isBlank()) {
            return query;
        }
        String[] tokens = query.split("\\s+");
        List<String> result = new ArrayList<>();
        for (String token : tokens) {
            if (token.equalsIgnoreCase("or")) {
                result.add(token);
            } else {
                result.add(expandToken(token));
            }
        }
        return String.join(" ", result);
    }

    private String expandToken(String token) {
        String prefix = "";
        String value = token;

        if (token.startsWith("content:")) {
            prefix = "content:";
            value = token.substring("content:".length());
        } else if (token.startsWith("path:")) {
            prefix = "path:";
            value = token.substring("path:".length());
        } else if (token.startsWith("color:")) {
            prefix = "color:";
            value = token.substring("color:".length());
        }

        boolean endsWithWildcard = value.endsWith("*");
        String lookupKey = endsWithWildcard ? value.substring(0, value.length() - 1) : value;

        List<String> list = SYNONYMS.get(lookupKey.toLowerCase());
        if (list == null || list.isEmpty()) {
            return token;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(token);
        for (String syn : list) {
            sb.append(" OR ");
            sb.append(prefix);
            sb.append(syn);
            if (endsWithWildcard) {
                sb.append("*");
            }
        }
        return sb.toString();
    }
}
