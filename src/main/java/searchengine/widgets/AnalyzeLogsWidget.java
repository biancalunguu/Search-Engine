package searchengine.widgets;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import searchengine.model.FileRecord;
import searchengine.model.SearchResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class AnalyzeLogsWidget implements SearchWidget {

    private static final int MAX_LINES_PER_FILE = 500;

    @Override
    public boolean supports(SearchContext context) {
        return context.hasResults()
                && (
                context.resultsArePredominantlyLogs()
                        || context.hasManyLogs()
                        || context.queryLooksLogRelated()
        )
                && context.countByExtension("log") > 0;
    }

    @Override
    public Node createNode(SearchContext context) {
        Button button = new Button("Analyze Logs");

        button.setTooltip(new javafx.scene.control.Tooltip(context.describeLogActivationReason()));

        button.setOnAction(event -> openLogAnalysisWindow(context));

        return button;
    }

    private void openLogAnalysisWindow(SearchContext context) {
        List<FileRecord> logFiles = context.getResults()
                .stream()
                .map(SearchResult::getFile)
                .filter(file -> {
                    String extension = file.getExtension();
                    return extension != null && extension.equalsIgnoreCase("log");
                })
                .toList();

        LogAnalysis analysis = analyzeLogs(logFiles);

        Stage stage = new Stage();
        stage.setTitle("Analyze Logs");

        Label title = new Label("Log Analysis");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label reason = new Label(context.describeLogActivationReason());
        reason.setStyle("-fx-text-fill: #555555;");

        Label summary = new Label(
                "Analyzed " + logFiles.size()
                        + " .log file(s) out of "
                        + context.getResultCount()
                        + " total result(s)."
                        + "\nERROR: " + analysis.errorCount
                        + " | WARN: " + analysis.warnCount
                        + " | INFO: " + analysis.infoCount
                        + " | DEBUG: " + analysis.debugCount
        );

        TextArea details = new TextArea(analysis.details.toString());
        details.setEditable(false);
        details.setWrapText(false);

        VBox root = new VBox(10);
        root.setPadding(new Insets(16));
        root.getChildren().addAll(title, reason, summary, details);

        Scene scene = new Scene(root, 750, 500);
        stage.setScene(scene);
        stage.show();
    }

    private LogAnalysis analyzeLogs(List<FileRecord> logFiles) {
        LogAnalysis analysis = new LogAnalysis();

        for (FileRecord file : logFiles) {
            analysis.details.append("File: ")
                    .append(file.getFileName())
                    .append("\n");

            try {
                List<String> lines = Files.readAllLines(Path.of(file.getFilePath()))
                        .stream()
                        .limit(MAX_LINES_PER_FILE)
                        .toList();

                for (String line : lines) {
                    String normalized = line.toLowerCase(Locale.ROOT);

                    if (normalized.contains("error")) {
                        analysis.errorCount++;
                        analysis.details.append("  [ERROR] ").append(line).append("\n");
                    } else if (normalized.contains("warn")) {
                        analysis.warnCount++;
                        analysis.details.append("  [WARN] ").append(line).append("\n");
                    } else if (normalized.contains("info")) {
                        analysis.infoCount++;
                    } else if (normalized.contains("debug")) {
                        analysis.debugCount++;
                    }
                }

                analysis.details.append("\n");

            } catch (IOException e) {
                analysis.details.append("Could not read log file: ")
                        .append(e.getMessage())
                        .append("\n\n");
            }
        }

        if (analysis.details.toString().isBlank()) {
            analysis.details.append("No log details available.");
        }

        return analysis;
    }

    private static class LogAnalysis {
        private int errorCount;
        private int warnCount;
        private int infoCount;
        private int debugCount;
        private final StringBuilder details = new StringBuilder();
    }
}