package searchengine.history;

import searchengine.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

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
}