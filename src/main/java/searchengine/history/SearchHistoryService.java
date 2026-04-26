package searchengine.history;

import searchengine.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
}