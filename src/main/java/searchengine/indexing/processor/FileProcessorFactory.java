package searchengine.indexing.processor;

import searchengine.config.ConfigurationManager;
import searchengine.indexing.image.ColorClassifier;
import searchengine.model.FileRecord;

import java.util.List;
import java.util.Optional;

/**
 * Factory that exposes the available file processing strategies.
 */
public class FileProcessorFactory {

    private final List<FileProcessor> processors;

    public FileProcessorFactory() {
        ConfigurationManager cfg = ConfigurationManager.getInstance();
        this.processors = List.of(
                new ImageFileProcessor(cfg.getImageExtensions(), new ColorClassifier()),
                new TextFileProcessor(cfg.getTextExtensions(), cfg.getMaxFileSizeBytes(), cfg.getPreviewLines())
        );
    }

    public Optional<FileProcessor> findProcessor(FileRecord record) {
        return processors.stream()
                .filter(processor -> processor.supports(record))
                .findFirst();
    }
}
