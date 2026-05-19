package searchengine.indexing;

import searchengine.indexing.processor.FileProcessorFactory;
import searchengine.model.FileRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Reads a file from disk and builds a FileRecord containing common metadata.
 * File-type-specific enrichment is delegated to FileProcessor strategies.
 */
public class DataExtractor {

    private final FileProcessorFactory processorFactory;

    public DataExtractor() {
        this.processorFactory = new FileProcessorFactory();
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

        processorFactory.findProcessor(record)
                .ifPresent(processor -> processFile(processor, filePath, record));

        return record;
    }

    private void processFile(searchengine.indexing.processor.FileProcessor processor,
                             Path filePath,
                             FileRecord record) {
        try {
            processor.process(filePath, record);
        } catch (IOException e) {
            System.err.println("[WARN] Could not process file content: " + filePath + " - " + e.getMessage());
        }
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot >= 0 && dot < fileName.length() - 1) {
            return fileName.substring(dot + 1).toLowerCase();
        }
        return "";
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
