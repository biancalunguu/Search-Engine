package searchengine.indexing;

import searchengine.db.DatabaseConnection;
import searchengine.model.FileRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Compares the list of files discovered on disk against what is already stored
 * in the database, and performs only the minimum required changes:
 *
 *   INSERT — file is new (not in the database yet)
 *   UPDATE — file exists but its last-modified timestamp changed
 *   DELETE — file was in the database but no longer exists on disk
 *   SKIP   — file is unchanged (same last-modified time)
 *
 * This is incremental indexing, which avoids rebuilding the entire index on every run
 */
public class DatabaseSynchronizer {

    private final Connection connection;

    private int inserted = 0;
    private int updated = 0;
    private int deleted = 0;
    private int skipped = 0;
    private int errors = 0;

    public DatabaseSynchronizer() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Loads all file paths and their last-modified timestamps from the database.
     * Used to compare against the current state of the file system.
     */
    public Map<String, LocalDateTime> getIndexedFiles() throws SQLException {
        Map<String, LocalDateTime> indexed = new HashMap<>();
        String sql = "SELECT file_path, last_modified FROM files";
        try (Statement stmt = connection.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("last_modified");
                indexed.put(rs.getString("file_path"),
                        ts != null ? ts.toLocalDateTime() : null);
            }
        }
        return indexed;
    }


    public void sync(List<FileRecord> records, Map<String, LocalDateTime> indexed) {
        Set<String> seenPaths = new HashSet<>();

        for (FileRecord record : records) {
            seenPaths.add(record.getFilePath());
            LocalDateTime dbModified = indexed.get(record.getFilePath());

            try {
                if (dbModified == null) {
                    insert(record);
                    inserted++;
                } else if (record.getLastModified() != null
                        && !record.getLastModified().equals(dbModified)) {
                    update(record);
                    updated++;
                } else {
                    skipped++;
                }
            } catch (SQLException e) {
                System.err.println("[ERROR] Could not sync: " + record.getFilePath() + " — " + e.getMessage());
                errors++;
            }
        }

        for (String path : indexed.keySet()) {
            if (!seenPaths.contains(path)) {
                try {
                    delete(path);
                    deleted++;
                } catch (SQLException e) {
                    System.err.println("[ERROR] Could not delete record: " + path + " — " + e.getMessage());
                    errors++;
                }
            }
        }
    }

    public synchronized void syncOne(FileRecord record, LocalDateTime dbModified) throws SQLException {
        String path = record.getFilePath();
        if (dbModified == null) {
            insert(record);
            inserted++;
        } else if (record.getLastModified() != null && !record.getLastModified().equals(dbModified)) {
            update(record);
            updated++;
        } else {
            skipped++;
        }
    }

    public synchronized void deleteOne(String path) throws SQLException {
        delete(path);
        deleted++;
    }

    public synchronized void incrementErrors() {
        errors++;
    }

    //I2 s2 added path score
    private void insert(FileRecord r) throws SQLException {
        String sql = """
                INSERT INTO files
                    (file_path, file_name, extension, size_bytes, last_modified,
                     is_text_file, is_image_file, dominant_color, content, preview, path_score)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, r.getFilePath());
            stmt.setString(2, r.getFileName());
            stmt.setString(3, r.getExtension());
            stmt.setLong(4, r.getSizeBytes());
            stmt.setTimestamp(5, toTimestamp(r.getLastModified()));
            stmt.setBoolean(6, r.isTextFile());
            stmt.setBoolean(7, r.isImageFile());
            stmt.setString(8, r.getDominantColor());
            stmt.setString(9, r.getContent());
            stmt.setString(10, r.getPreview());
            stmt.setDouble(11, r.getPathScore());
            stmt.executeUpdate();
        }
    }

    private void update(FileRecord r) throws SQLException {
        String sql = """
                UPDATE files
                SET file_name     = ?,
                    extension     = ?,
                    size_bytes    = ?,
                    last_modified = ?,
                    is_text_file  = ?,
                    is_image_file = ?,
                    dominant_color = ?,
                    content       = ?,
                    preview       = ?,
                    path_score    = ?,
                    indexed_at    = NOW()
                WHERE file_path = ?
                """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, r.getFileName());
            stmt.setString(2, r.getExtension());
            stmt.setLong(3, r.getSizeBytes());
            stmt.setTimestamp(4, toTimestamp(r.getLastModified()));
            stmt.setBoolean(5, r.isTextFile());
            stmt.setBoolean(6, r.isImageFile());
            stmt.setString(7, r.getDominantColor());
            stmt.setString(8, r.getContent());
            stmt.setString(9, r.getPreview());
            stmt.setDouble(10, r.getPathScore());
            stmt.setString(11, r.getFilePath());
            stmt.executeUpdate();
        }
    }

    private void delete(String filePath) throws SQLException {
        String sql = "DELETE FROM files WHERE file_path = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, filePath);
            stmt.executeUpdate();
        }
    }

    private Timestamp toTimestamp(LocalDateTime ldt) {
        return ldt != null ? Timestamp.valueOf(ldt) : null;
    }

    public int getInserted() { return inserted; }
    public int getUpdated() { return updated; }
    public int getDeleted() { return deleted; }
    public int getSkipped() { return skipped; }
    public int getErrors() { return errors; }
}
