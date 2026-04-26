package searchengine.ranking;

import searchengine.history.SearchHistoryService;
import searchengine.model.SearchResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingManager {

    private final Map<String, RankingStrategy> strategies = new HashMap<>();
    private RankingStrategy currentStrategy;

    public RankingManager(SearchHistoryService searchHistoryService) {
        register(new PathScoreRankingStrategy());
        register(new AlphabeticalRankingStrategy());
        register(new HistoryRankingStrategy(searchHistoryService));

        currentStrategy = strategies.get("path");
    }

    public void register(RankingStrategy strategy) {
        strategies.put(strategy.getName(), strategy);
    }

    public boolean useStrategy(String name) {
        RankingStrategy strategy = strategies.get(name.toLowerCase());

        if (strategy == null) {
            return false;
        }

        currentStrategy = strategy;
        return true;
    }

    public List<SearchResult> rank(List<SearchResult> results) {
        return currentStrategy.rank(results);
    }

    public String getCurrentStrategyName() {
        return currentStrategy.getName();
    }

    public String getAvailableStrategies() {
        return String.join(", ", strategies.keySet());
    }
}