package searchengine.query;

public class SanitizationDecorator extends QueryBuilderDecorator {
    public SanitizationDecorator(QueryBuilder decoratedBuilder) {
        super(decoratedBuilder);
    }

    @Override
    public String buildQuery(String rawInput) {
        String query = super.buildQuery(rawInput);
        if (query == null || query.isBlank()) {
            return query;
        }
        String sanitized = query.replaceAll("[\\+\\-\\*~\\<\\>\\(\\)\\\"@]", " ");
        return sanitized.replaceAll("\\s+", " ").trim();
    }
}
