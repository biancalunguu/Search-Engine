package searchengine.query;

import searchengine.config.ConfigurationManager;
import searchengine.db.DatabaseConnection;
import searchengine.model.FileRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Translates a ParsedQuery into SQL and executes it against the database.
 * Both single-word and multi-word queries are handled by the same methods.
 */
public class SearchExecutor {

    private final Connection connection;
    private final int        maxResults;

    public SearchExecutor() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.maxResults = ConfigurationManager.getInstance().getMaxResults();
    }

    /**
     * Executes the search and returns up to maxResults matching FileRecords.
     */
    public List<FileRecord> search(QueryParser.ParsedQuery query) throws SQLException {
        List<FileRecord> results = searchFullText(query);

        if (results.isEmpty()) {
            results = searchLike(query);
        }

        return results;
    }


    private List<FileRecord> searchFullText(QueryParser.ParsedQuery query) throws SQLException {
        String sql = """
                SELECT id, file_path, file_name, extension, size_bytes, last_modified,
                       is_text_file, content, preview, content_hash, indexed_at
                FROM files
                WHERE MATCH(file_name, content) AGAINST (? IN BOOLEAN MODE)
                LIMIT ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, query.toFullTextQuery());
            stmt.setInt(2, maxResults);
            return mapResultSet(stmt.executeQuery());
        }
    }


    private List<FileRecord> searchLike(QueryParser.ParsedQuery query) throws SQLException {
        StringBuilder where = new StringBuilder();
        for (int i = 0; i < query.getTerms().size(); i++) {
            if (i > 0) where.append(" AND ");
            where.append("(file_name LIKE ? OR content LIKE ? OR file_path LIKE ?)");
        }

        String sql = "SELECT id, file_path, file_name, extension, size_bytes, last_modified, "
                + "is_text_file, content, preview, content_hash, indexed_at "
                + "FROM files WHERE " + where + " LIMIT " + maxResults;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int i = 1;
            for (String term : query.getTerms()) {
                String like = "%" + term + "%";
                stmt.setString(i++, like);
                stmt.setString(i++, like);
                stmt.setString(i++, like);
            }
            return mapResultSet(stmt.executeQuery());
        }
    }

    private List<FileRecord> mapResultSet(ResultSet rs) throws SQLException {
        List<FileRecord> list = new ArrayList<>();
        while (rs.next()) {
            FileRecord r = new FileRecord();
            r.setId(rs.getLong("id"));
            r.setFilePath(rs.getString("file_path"));
            r.setFileName(rs.getString("file_name"));
            r.setExtension(rs.getString("extension"));
            r.setSizeBytes(rs.getLong("size_bytes"));

            Timestamp lastMod = rs.getTimestamp("last_modified");
            if (lastMod != null) r.setLastModified(lastMod.toLocalDateTime());

            r.setTextFile(rs.getBoolean("is_text_file"));
            r.setContent(rs.getString("content"));
            r.setPreview(rs.getString("preview"));
            r.setContentHash(rs.getString("content_hash"));

            Timestamp indexedAt = rs.getTimestamp("indexed_at");
            if (indexedAt != null) r.setIndexedAt(indexedAt.toLocalDateTime());

            list.add(r);
        }
        return list;
    }
}
