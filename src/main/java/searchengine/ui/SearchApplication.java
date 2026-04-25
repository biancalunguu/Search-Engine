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

    private void handleRankingCommand(String command) {
        String[] parts = command.split("\\s+");

        if (parts.length < 2) {
            System.out.println("Current ranking: " + engine.getCurrentRankingStrategy());
            System.out.println("Available rankings: " + engine.getAvailableRankingStrategies());
            return;
        }

        String requestedStrategy = parts[1];

        if (engine.setRankingStrategy(requestedStrategy)) {
            System.out.println("Ranking changed to: " + engine.getCurrentRankingStrategy());
        } else {
            System.out.println("Unknown ranking strategy: " + requestedStrategy);
            System.out.println("Available rankings: " + engine.getAvailableRankingStrategies());
        }
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

            if (query.startsWith(":rank")) {
                handleRankingCommand(query);
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
