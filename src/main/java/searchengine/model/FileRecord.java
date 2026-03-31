package searchengine.model;

import java.time.LocalDateTime;

/**
 * Represents a single file entry stored in the database.
 * Contains metadata and optionally the full text content.
 */
public class FileRecord {

    private long id;
    private String filePath;
    private String fileName;
    private String extension;
    private long sizeBytes;
    private LocalDateTime lastModified;
    private boolean textFile;
    private String content;
    private String preview;
    private String contentHash;
    private LocalDateTime indexedAt;


    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }

    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }

    public boolean isTextFile() { return textFile; }
    public void setTextFile(boolean textFile) { this.textFile = textFile; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPreview() { return preview; }
    public void setPreview(String preview) { this.preview = preview; }

    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public LocalDateTime getIndexedAt() { return indexedAt; }
    public void setIndexedAt(LocalDateTime indexedAt) { this.indexedAt = indexedAt; }
}
