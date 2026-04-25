package searchengine.indexing;

import searchengine.config.ConfigurationManager;
import searchengine.model.FileRecord;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Reads a file from disk and builds a FileRecord containing:
 *   - Metadata (name, extension, size, last-modified time)
 *   - Text content + preview (for recognised text file types under the size limit)
 */
public class DataExtractor {

    private final List<String> textExtensions;
    private final long maxFileSizeBytes;
    private final int previewLines;

    public DataExtractor() {
        ConfigurationManager cfg = ConfigurationManager.getInstance();
        this.textExtensions = cfg.getTextExtensions();
        this.maxFileSizeBytes = cfg.getMaxFileSizeBytes();
        this.previewLines = cfg.getPreviewLines();
    }

    /**
     * Extracts all information from the given file path and returns a FileRecord.
     */
    public FileRecord extract(Path filePath) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);

        FileRecord record = new FileRecord();
        record.setFilePath(filePath.toAbsolutePath().toString());
        record.setFileName(filePath.getFileName().toString());
        record.setExtension(getExtension(record.getFileName()));
        record.setSizeBytes(attrs.size());
        record.setLastModified(toLocalDateTime(attrs.lastModifiedTime().toInstant()));

        boolean isText = textExtensions.contains(record.getExtension().toLowerCase());
        record.setTextFile(isText);

        if (isText && attrs.size() > 0 && attrs.size() <= maxFileSizeBytes) {
            try {
                String content = Files.readString(filePath, StandardCharsets.UTF_8);
                record.setContent(content);
                record.setPreview(generatePreview(content));
            } catch (MalformedInputException e) {
                record.setTextFile(false);
            }
        }

        return record;
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot >= 0 && dot < fileName.length() - 1) {
            return fileName.substring(dot + 1);
        }
        return "";
    }

    /** Returns the first N lines of the content as a preview string. */
    private String generatePreview(String content) {
        String[] lines = content.split("\n", previewLines + 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(lines.length, previewLines); i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines[i]);
        }
        return sb.toString();
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
