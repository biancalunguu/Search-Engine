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
    private final int maxResults;

    public SearchExecutor() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.maxResults = ConfigurationManager.getInstance().getMaxResults();
    }

    public List<FileRecord> search(QueryParser.ParsedQuery query) throws SQLException {
        if (query.hasQualifiedTerms()) {
            return searchQualified(query);
        }

        List<FileRecord> results = searchFullText(query);

        if (results.isEmpty()) {
            results = searchLike(query);
        }

        return results;
    }

    private List<FileRecord> searchFullText(QueryParser.ParsedQuery query) throws SQLException {
        String sql = """
                SELECT id, file_path, file_name, extension, size_bytes, last_modified,
                       is_text_file, content, preview, indexed_at, path_score
                FROM files
                WHERE MATCH(file_name, content) AGAINST (? IN BOOLEAN MODE)
                ORDER BY file_name ASC
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

            where.append("""
                    (
                        file_name LIKE ?
                        OR content LIKE ?
                        OR REPLACE(file_path, '\\\\', '/') LIKE ?
                    )
                    """);
        }

        String sql = """
                SELECT id, file_path, file_name, extension, size_bytes, last_modified,
                       is_text_file, content, preview, indexed_at, path_score
                FROM files
                WHERE %s
                ORDER BY file_name ASC
                LIMIT ?
                """.formatted(where);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int index = 1;

            for (String term : query.getTerms()) {
                String like = "%" + normalizePathTerm(term) + "%";

                stmt.setString(index++, like);
                stmt.setString(index++, like);
                stmt.setString(index++, like);
            }

            stmt.setInt(index, maxResults);

            return mapResultSet(stmt.executeQuery());
        }
    }

    /**
     * Iteration 2 parser support.
     * - content:x checks file_name or content
     * - path:x checks normalized file_path
     * - unqualified words keep the old broad behavior
     * Every term is joined with AND, including duplicate qualifiers.
     */
    private List<FileRecord> searchQualified(QueryParser.ParsedQuery query) throws SQLException {
        StringBuilder where = new StringBuilder("1=1");

        for (int i = 0; i < query.getGeneralTerms().size(); i++) {
            where.append("""
                     AND (
                        file_name LIKE ?
                        OR content LIKE ?
                        OR REPLACE(file_path, '\\\\', '/') LIKE ?
                    )
                    """);
        }

        for (int i = 0; i < query.getContentTerms().size(); i++) {
            where.append("""
                     AND (
                        file_name LIKE ?
                        OR content LIKE ?
                    )
                    """);
        }

        for (int i = 0; i < query.getPathTerms().size(); i++) {
            where.append(" AND REPLACE(file_path, '\\\\', '/') LIKE ?");
        }

        String sql = """
                SELECT id, file_path, file_name, extension, size_bytes, last_modified,
                       is_text_file, content, preview, indexed_at, path_score
                FROM files
                WHERE %s
                ORDER BY file_name ASC
                LIMIT ?
                """.formatted(where);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int index = 1;

            for (String term : query.getGeneralTerms()) {
                String like = "%" + normalizePathTerm(term) + "%";

                stmt.setString(index++, like);
                stmt.setString(index++, like);
                stmt.setString(index++, like);
            }

            for (String term : query.getContentTerms()) {
                String like = "%" + term + "%";

                stmt.setString(index++, like);
                stmt.setString(index++, like);
            }

            for (String term : query.getPathTerms()) {
                String like = "%" + normalizePathTerm(term) + "%";
                stmt.setString(index++, like);
            }

            stmt.setInt(index, maxResults);

            return mapResultSet(stmt.executeQuery());
        }
    }

    private String normalizePathTerm(String term) {
        if (term == null) {
            return "";
        }

        return term.replace("\\", "/");
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
            r.setPathScore(rs.getDouble("path_score"));

            Timestamp lastMod = rs.getTimestamp("last_modified");
            if (lastMod != null) {
                r.setLastModified(lastMod.toLocalDateTime());
            }

            r.setTextFile(rs.getBoolean("is_text_file"));
            r.setContent(rs.getString("content"));
            r.setPreview(rs.getString("preview"));

            Timestamp indexedAt = rs.getTimestamp("indexed_at");
            if (indexedAt != null) {
                r.setIndexedAt(indexedAt.toLocalDateTime());
            }

            list.add(r);
        }

        return list;
    }
}