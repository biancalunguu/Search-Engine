package searchengine.query;

import java.util.ArrayList;
import java.util.List;

public class LogicDecorator extends QueryBuilderDecorator {
    public LogicDecorator(QueryBuilder decoratedBuilder) {
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
            } else if (token.endsWith("*")) {
                result.add(token);
            } else {
                result.add(token + "*");
            }
        }
        return String.join(" ", result);
    }
}
