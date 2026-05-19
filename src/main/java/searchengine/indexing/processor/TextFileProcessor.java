package searchengine.indexing.processor;

import searchengine.model.FileRecord;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Strategy used for text files. It reads UTF-8 content and creates a small preview.
 */
public class TextFileProcessor implements FileProcessor {

    private final List<String> textExtensions;
    private final long maxFileSizeBytes;
    private final int previewLines;

    public TextFileProcessor(List<String> textExtensions, long maxFileSizeBytes, int previewLines) {
        this.textExtensions = textExtensions;
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.previewLines = previewLines;
    }

    @Override
    public boolean supports(FileRecord record) {
        return record.getExtension() != null
                && textExtensions.contains(record.getExtension().toLowerCase());
    }

    @Override
    public void process(Path filePath, FileRecord record) throws IOException {
        record.setTextFile(true);

        long fileSize = Files.size(filePath);
        if (fileSize <= 0 || fileSize > maxFileSizeBytes) {
            return;
        }

        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            record.setContent(content);
            record.setPreview(generatePreview(content));
        } catch (MalformedInputException e) {
            record.setTextFile(false);
            record.setContent(null);
            record.setPreview(null);
        }
    }

    private String generatePreview(String content) {
        String[] lines = content.split("\n", previewLines + 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(lines.length, previewLines); i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines[i]);
        }
        return sb.toString();
    }
}
