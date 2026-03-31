package searchengine.query;

import searchengine.model.FileRecord;
import searchengine.model.SearchResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * In charge of the query pipeline:
 *
 *   1. QueryParser - parse and clean the raw user input
 *   2. SearchExecutor → run the SQL query and get matching FileRecords
 *   3. SnippetGenerator → attach a contextual snippet to each result
 */
public class QueryEngine {

    private final QueryParser parser;
    private final SearchExecutor executor;
    private final SnippetGenerator snippetGenerator;

    public QueryEngine() throws SQLException {
        this.parser = new QueryParser();
        this.executor = new SearchExecutor();
        this.snippetGenerator = new SnippetGenerator();
    }

    /**
     * Runs a full query and returns a list of results with contextual snippets.
     */
    public List<SearchResult> query(String rawInput) throws SQLException {
        QueryParser.ParsedQuery parsed = parser.parse(rawInput);
        if (parsed == null) return new ArrayList<>();

        List<FileRecord>    files   = executor.search(parsed);
        List<SearchResult>  results = new ArrayList<>();

        for (FileRecord file : files) {
            String snippet;

            if (file.isTextFile() && file.getContent() != null) {
                snippet = snippetGenerator.generate(file.getContent(), parsed.getTerms());
            } else if (file.getPreview() != null) {
                snippet = file.getPreview();
            } else {
                snippet = null;
            }

            results.add(new SearchResult(file, snippet));
        }

        return results;
    }
}
