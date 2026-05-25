package searchengine.query;

public abstract class QueryBuilderDecorator implements QueryBuilder {
    protected final QueryBuilder decoratedBuilder;

    public QueryBuilderDecorator(QueryBuilder decoratedBuilder) {
        this.decoratedBuilder = decoratedBuilder;
    }

    @Override
    public String buildQuery(String rawInput) {
        return decoratedBuilder.buildQuery(rawInput);
    }
}
