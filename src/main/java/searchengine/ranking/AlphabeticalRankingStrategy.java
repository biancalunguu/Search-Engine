package searchengine.ranking;

import searchengine.model.SearchResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AlphabeticalRankingStrategy implements RankingStrategy {

    @Override
    public List<SearchResult> rank(List<SearchResult> results) {
        List<SearchResult> ranked = new ArrayList<>(results);

        ranked.sort(
                Comparator
                        .comparing(
                                (SearchResult r) -> r.getFile().getFileName(),
                                String.CASE_INSENSITIVE_ORDER
                        )
                        .thenComparing(
                                r -> r.getFile().getFilePath(),
                                String.CASE_INSENSITIVE_ORDER
                        )
        );

        return ranked;
    }

    @Override
    public String getName() {
        return "alpha";
    }
}