package searchengine.history;

public class QuerySuggestion {

    private final String queryText;
    private final int distance;
    private final int frequency;

    public QuerySuggestion(String queryText, int distance, int frequency) {
        this.queryText = queryText;
        this.distance = distance;
        this.frequency = frequency;
    }

    public String getQueryText() {
        return queryText;
    }

    public int getDistance() {
        return distance;
    }

    public int getFrequency() {
        return frequency;
    }

    public double getScore() {
        return frequency * 2.0 - distance;
    }
}