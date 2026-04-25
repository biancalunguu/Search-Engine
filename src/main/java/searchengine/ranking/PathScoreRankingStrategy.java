package searchengine.ranking;

import searchengine.model.SearchResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PathScoreRankingStrategy implements RankingStrategy {

    @Override
    public List<SearchResult> rank(List<SearchResult> results) {
        List<SearchResult> ranked = new ArrayList<>(results);

        ranked.sort(
                Comparator.comparingDouble((SearchResult r) -> r.getFile().getPathScore())
                        .reversed()
                        .thenComparing(r -> r.getFile().getFileName(), String.CASE_INSENSITIVE_ORDER)
        );

        return ranked;
    }

    @Override
    public String getName() {
        return "path";
    }
}