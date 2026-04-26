package searchengine.ranking;

import searchengine.history.SearchHistoryService;
import searchengine.model.SearchResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HistoryRankingStrategy implements RankingStrategy {

    private final SearchHistoryService searchHistoryService;

    public HistoryRankingStrategy(SearchHistoryService searchHistoryService) {
        this.searchHistoryService = searchHistoryService;
    }

    @Override
    public List<SearchResult> rank(List<SearchResult> results) {
        Map<String, Integer> frequentTerms = searchHistoryService.getFrequentSearchTerms();

        List<SearchResult> ranked = new ArrayList<>(results);

        ranked.sort(
                Comparator
                        .comparingDouble((SearchResult result) -> score(result, frequentTerms))
                        .reversed()
                        .thenComparing(
                                result -> result.getFile().getFileName(),
                                String.CASE_INSENSITIVE_ORDER
                        )
        );

        return ranked;
    }

    private double score(SearchResult result, Map<String, Integer> frequentTerms) {
        double score = result.getFile().getPathScore();

        String searchableText = (
                result.getFile().getFileName() + " " +
                        result.getFile().getFilePath() + " " +
                        safe(result.getSnippet())
        ).toLowerCase();

        for (Map.Entry<String, Integer> entry : frequentTerms.entrySet()) {
            String term = entry.getKey();
            int frequency = entry.getValue();

            if (searchableText.contains(term)) {
                score += Math.min(frequency, 5) * 0.5;
            }
        }

        return score;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public String getName() {
        return "history";
    }
}