package searchengine.ranking;

import searchengine.model.SearchResult;

import java.util.List;

public interface RankingStrategy {

    List<SearchResult> rank(List<SearchResult> results);

    String getName();
}