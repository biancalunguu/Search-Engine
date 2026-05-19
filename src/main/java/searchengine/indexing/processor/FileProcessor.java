package searchengine.indexing.processor;

import searchengine.model.FileRecord;

import java.io.IOException;
import java.nio.file.Path;

public interface FileProcessor {

    boolean supports(FileRecord record);
    void process(Path filePath, FileRecord record) throws IOException;
}
