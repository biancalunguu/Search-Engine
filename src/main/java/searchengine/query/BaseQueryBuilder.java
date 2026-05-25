package searchengine.query;

public class BaseQueryBuilder implements QueryBuilder {
    @Override
    public String buildQuery(String rawInput) {
        if (rawInput == null) {
            return "";
        }
        return rawInput.trim();
    }
}
