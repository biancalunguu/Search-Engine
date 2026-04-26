package searchengine.history;

import searchengine.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

public class SearchHistoryService implements SearchObserver {

    private final DatabaseConnection databaseConnection;

    public SearchHistoryService() {
        this.databaseConnection = new DatabaseConnection();
    }

    @Override
    public void onSearch(SearchEvent event) {
        save(event);
    }

    private void save(SearchEvent event) {
        String sql = """
                INSERT INTO search_history (query_text, result_count, searched_at)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, event.getQueryText());
            statement.setInt(2, event.getResultCount());
            statement.setTimestamp(3, Timestamp.valueOf(event.getSearchedAt()));
            statement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[WARN] Could not save search activity: " + e.getMessage());
        }
    }

    public List<String> suggestQueries(String partialQuery) {
        List<String> suggestions = new ArrayList<>();

        String sql = """
                SELECT query_text, COUNT(*) AS frequency, MAX(searched_at) AS last_used
                FROM search_history
                WHERE LOWER(query_text) LIKE ?
                GROUP BY query_text
                ORDER BY frequency DESC, last_used DESC
                LIMIT 5
                """;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + partialQuery.toLowerCase() + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    suggestions.add(resultSet.getString("query_text"));
                }
            }

        } catch (SQLException e) {
            System.err.println("[WARN] Could not load query suggestions: " + e.getMessage());
        }

        return suggestions;
    }

    public Map<String, Integer> getFrequentSearchTerms() {
        Map<String, Integer> termFrequency = new HashMap<>();

        String sql = """
            SELECT query_text
            FROM search_history
            ORDER BY searched_at DESC
            LIMIT 50
            """;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String query = resultSet.getString("query_text");

                for (String term : extractTerms(query)) {
                    termFrequency.put(term, termFrequency.getOrDefault(term, 0) + 1);
                }
            }

        } catch (SQLException e) {
            System.err.println("[WARN] Could not load search history terms: " + e.getMessage());
        }

        return termFrequency;
    }

    private List<String> extractTerms(String query) {
        List<String> terms = new ArrayList<>();

        if (query == null || query.isBlank()) {
            return terms;
        }

        String[] tokens = query.toLowerCase().split("\\s+");

        for (String token : tokens) {
            String cleaned = token;

            if (cleaned.contains(":")) {
                cleaned = cleaned.substring(cleaned.indexOf(":") + 1);
            }

            cleaned = cleaned.replaceAll("[^a-z0-9._/-]", "");

            if (!cleaned.isBlank()) {
                terms.add(cleaned);
            }
        }

        return terms;
    }
}