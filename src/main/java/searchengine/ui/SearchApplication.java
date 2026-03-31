package searchengine.ui;

import searchengine.model.SearchResult;
import searchengine.query.QueryEngine;

import java.sql.SQLException;
import java.util.List;

/**
 * Runs the interactive search loop.
 *
 * The user types a query and presses Enter.
 * Results are displayed immediately.
 * Typing "exit" or "quit" returns to the main menu.
 */
public class SearchApplication {

    private final InputController input    = new InputController();
    private final ResultRenderer  renderer = new ResultRenderer();
    private final QueryEngine     engine;

    public SearchApplication() throws SQLException {
        this.engine = new QueryEngine();
    }

    public void run() {
        System.out.println("\n  ==== Search Mode ==== (type 'exit' to return to menu)");

        while (true) {
            System.out.println();
            String query = input.readQuery().trim();

            if (query.equalsIgnoreCase("exit") || query.equalsIgnoreCase("quit")) {
                break;
            }

            if (query.isBlank()) {
                continue;
            }

            try {
                List<SearchResult> results = engine.query(query);
                renderer.render(results, query);
            } catch (SQLException e) {
                System.err.println("[ERROR] Search failed: " + e.getMessage());
            }
        }
    }
}
