package searchengine.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import searchengine.indexing.IndexingService;
import searchengine.model.SearchResult;
import searchengine.query.QueryEngine;

import java.sql.SQLException;
import java.util.List;

/**
 * JavaFX user interface for the local search engine.
 *
 * This class only handles presentation and user actions. The actual search,
 * ranking, suggestions and indexing logic remains in the backend services.
 */
public class SearchEngineApp {

    private final BorderPane root = new BorderPane();

    private final TextField searchField = new TextField();
    private final Button searchButton = new Button("Search");
    private final Button indexButton = new Button("Index Files");

    private final ComboBox<String> rankingComboBox = new ComboBox<>(
            FXCollections.observableArrayList("path", "alpha", "history")
    );

    private final ListView<SearchResult> resultsList = new ListView<>();
    private final Label statusLabel = new Label("Ready.");

    private final QueryEngine queryEngine;

    public SearchEngineApp() throws SQLException {
        this.queryEngine = new QueryEngine();
        buildLayout();
        connectActions();
    }

    public Parent getRoot() {
        return root;
    }

    private void buildLayout() {
        root.setPadding(new Insets(16));

        Label title = new Label("Local Search Engine");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        searchField.setPromptText("Example: path:src content:java");
        searchField.setPrefWidth(420);

        rankingComboBox.setValue("path");
        rankingComboBox.setPrefWidth(120);

        HBox searchRow = new HBox(10);
        searchRow.getChildren().addAll(
                new Label("Search:"),
                searchField,
                searchButton,
                new Label("Ranking:"),
                rankingComboBox,
                indexButton
        );

        VBox top = new VBox(12);
        top.getChildren().addAll(title, searchRow);
        root.setTop(top);

        resultsList.setCellFactory(listView -> new SearchResultCell());

        Label resultsTitle = new Label("Results");
        resultsTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        VBox resultsBox = new VBox(8, resultsTitle, resultsList);
        VBox.setVgrow(resultsList, Priority.ALWAYS);

        Label suggestionsTitle = new Label("Suggestions");
        suggestionsTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        root.setCenter(resultsBox);

        statusLabel.setPadding(new Insets(10, 0, 0, 0));
        root.setBottom(statusLabel);
    }

    private void connectActions() {
        searchButton.setOnAction(event -> runSearch());
        searchField.setOnAction(event -> runSearch());
        indexButton.setOnAction(event -> runIndexing());

        rankingComboBox.setOnAction(event -> {
            String selectedStrategy = rankingComboBox.getValue();
            if (selectedStrategy != null && queryEngine.setRankingStrategy(selectedStrategy)) {
                statusLabel.setText("Ranking changed to: " + selectedStrategy);
            }
        });
    }

    private void runSearch() {
        String query = searchField.getText().trim();

        if (query.isBlank()) {
            statusLabel.setText("Type a query first.");
            return;
        }

        try {
            List<SearchResult> results = queryEngine.query(query);
            resultsList.getItems().setAll(results);
            statusLabel.setText("Found " + results.size() + " result(s) using "
                    + queryEngine.getCurrentRankingStrategy() + " ranking.");
        } catch (SQLException e) {
            statusLabel.setText("Search failed: " + e.getMessage());
        }
    }

    private void runIndexing() {
        indexButton.setDisable(true);
        statusLabel.setText("Indexing files...");

        Thread indexingThread = new Thread(() -> {
            IndexingService indexingService = new IndexingService();
            indexingService.run();

            Platform.runLater(() -> {
                indexButton.setDisable(false);
                statusLabel.setText("Indexing finished. You can now search.");
            });
        });

        indexingThread.setDaemon(true);
        indexingThread.start();
    }
}
