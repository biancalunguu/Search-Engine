package searchengine.query;

import searchengine.model.FileRecord;
import searchengine.model.SearchResult;
import searchengine.ranking.RankingManager;
import searchengine.history.SearchEvent;
import searchengine.history.SearchHistoryService;
import searchengine.history.SearchSubject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//query parser generates snippets but also applies the selected ranking strategy
public class QueryEngine {

    private final QueryParser parser;
    private final SearchExecutor executor;
    private final SnippetGenerator snippetGenerator;
    private final RankingManager rankingManager;
    private final SearchSubject searchSubject;
    private final SearchHistoryService searchHistoryService;
    private QueryBuilder queryBuilder;

    public QueryEngine() throws SQLException {
        this.parser = new QueryParser();
        this.executor = new SearchExecutor();
        this.snippetGenerator = new SnippetGenerator();
        this.searchSubject = new SearchSubject();
        this.searchHistoryService = new SearchHistoryService();
        this.searchSubject.addObserver(searchHistoryService);
        this.rankingManager = new RankingManager(searchHistoryService);
        this.queryBuilder = new BaseQueryBuilder();
    }

    public List<SearchResult> query(String rawInput) throws SQLException {
        String processedQuery = queryBuilder.buildQuery(rawInput);
        QueryParser.ParsedQuery parsed = parser.parse(processedQuery);
        if (parsed == null) return new ArrayList<>();

        List<FileRecord> files = executor.search(parsed);
        List<SearchResult> results = new ArrayList<>();

        for (FileRecord file : files) {
            String snippet;

            if (file.isTextFile() && file.getContent() != null) {
                snippet = snippetGenerator.generate(file.getContent(), parsed.getTerms());
            } else if (file.isImageFile() && file.getDominantColor() != null) {
                snippet = "Image file - dominant color: " + file.getDominantColor();
            } else if (file.getPreview() != null) {
                snippet = file.getPreview();
            } else {
                snippet = null;
            }

            results.add(new SearchResult(file, snippet));
        }

        List<SearchResult> rankedResults = rankingManager.rank(results);

        searchSubject.notifyObservers(
                new SearchEvent(rawInput, rankedResults.size())
        );

        return rankedResults;
    }

    public boolean setRankingStrategy(String strategyName) {
        return rankingManager.useStrategy(strategyName);
    }

    public String getCurrentRankingStrategy() {
        return rankingManager.getCurrentStrategyName();
    }

    public String getAvailableRankingStrategies() {
        return rankingManager.getAvailableStrategies();
    }

    public List<String> suggestQueries(String partialQuery) {
        return searchHistoryService.suggestQueries(partialQuery);
    }

    public List<String> suggestQueriesFuzzy(String partialQuery) {
        return searchHistoryService.suggestQueriesFuzzy(partialQuery);
    }

    public void setQueryBuilder(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public QueryBuilder getQueryBuilder() {
        return this.queryBuilder;
    }

}
