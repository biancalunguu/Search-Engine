package searchengine.history;

import java.time.LocalDateTime;

public class SearchEvent {

    private final String queryText;
    private final int resultCount;
    private final LocalDateTime searchedAt;

    public SearchEvent(String queryText, int resultCount) {
        this.queryText = queryText;
        this.resultCount = resultCount;
        this.searchedAt = LocalDateTime.now();
    }

    public String getQueryText() {
        return queryText;
    }

    public int getResultCount() {
        return resultCount;
    }

    public LocalDateTime getSearchedAt() {
        return searchedAt;
    }
}