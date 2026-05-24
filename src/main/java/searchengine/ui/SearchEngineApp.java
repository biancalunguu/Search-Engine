package searchengine.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import searchengine.indexing.IndexingService;
import searchengine.model.SearchResult;
import searchengine.query.QueryEngine;
import searchengine.widgets.SearchContext;
import searchengine.widgets.WidgetFactory;

import java.sql.SQLException;
import java.util.List;

public class SearchEngineApp {

    private final BorderPane root = new BorderPane();

    private final TextField searchField = new TextField();
    private final Button searchButton = new Button("Search");
    private final Button indexButton = new Button("Index Files");

    private final ComboBox<String> rankingComboBox =
            new ComboBox<>(FXCollections.observableArrayList("path", "alpha", "history"));

    private final Label autocompleteLabel = new Label("Autocomplete: none");
    private final Label autocompleteHintLabel = new Label("Press TAB to accept autocomplete");
    private String currentAutocomplete = null;

    private final ListView<String> suggestionsList = new ListView<>();
    private final ListView<SearchResult> resultsList = new ListView<>();
    private final Label statusLabel = new Label("Ready.");

    private final Label widgetsTitle = new Label("Context-aware widgets");
    private final HBox widgetsBox = new HBox(8);

    private final QueryEngine queryEngine;
    private final WidgetFactory widgetFactory = new WidgetFactory();

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
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        searchField.setPromptText("Example: path:src content:java color:red");
        searchField.setPrefWidth(520);

        rankingComboBox.setValue("path");

        HBox searchRow = new HBox(10);
        searchRow.getChildren().addAll(
                new Label("Search:"),
                searchField,
                searchButton,
                new Label("Ranking:"),
                rankingComboBox,
                indexButton
        );

        autocompleteLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");
        autocompleteHintLabel.setStyle("-fx-text-fill: #777777;");

        VBox autocompleteBox = new VBox(3);
        autocompleteBox.getChildren().addAll(autocompleteLabel, autocompleteHintLabel);

        Label suggestionsTitle = new Label("Related previous searches");
        suggestionsTitle.setStyle("-fx-font-weight: bold;");

        suggestionsList.setMaxHeight(95);
        suggestionsList.setVisible(false);
        suggestionsList.setManaged(false);

        VBox suggestionsBox = new VBox(5);
        suggestionsBox.getChildren().addAll(suggestionsTitle, suggestionsList);

        widgetsTitle.setStyle("-fx-font-weight: bold;");
        widgetsTitle.setVisible(false);
        widgetsTitle.setManaged(false);

        widgetsBox.setVisible(false);
        widgetsBox.setManaged(false);

        VBox widgetsSection = new VBox(5);
        widgetsSection.getChildren().addAll(widgetsTitle, widgetsBox);

        VBox top = new VBox(12);
        top.getChildren().addAll(title, searchRow, autocompleteBox, suggestionsBox, widgetsSection);

        root.setTop(top);

        resultsList.setCellFactory(listView -> new SearchResultCell());

        VBox resultsBox = new VBox(8);
        resultsBox.getChildren().addAll(new Label("Results"), resultsList);
        VBox.setVgrow(resultsList, Priority.ALWAYS);

        root.setCenter(resultsBox);

        statusLabel.setPadding(new Insets(10, 0, 0, 0));
        root.setBottom(statusLabel);
    }

    private void connectActions() {
        searchButton.setOnAction(event -> runSearch());
        searchField.setOnAction(event -> runSearch());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateSuggestionsAndAutocomplete(newValue);
        });

        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB) {
                acceptAutocomplete();
                event.consume();
            }

            if (event.getCode() == KeyCode.ESCAPE) {
                clearSuggestions();
                event.consume();
            }
        });

        suggestionsList.setOnMouseClicked(event -> {
            String selected = suggestionsList.getSelectionModel().getSelectedItem();

            if (selected != null) {
                searchField.setText(selected);
                searchField.positionCaret(selected.length());
                clearSuggestions();
                searchField.requestFocus();
            }
        });

        rankingComboBox.setOnAction(event -> {
            String selected = rankingComboBox.getValue();

            if (selected != null && queryEngine.setRankingStrategy(selected)) {
                statusLabel.setText("Ranking changed to: " + selected);
            }
        });

        indexButton.setOnAction(event -> runIndexing());
    }

    private void updateSuggestionsAndAutocomplete(String input) {
        if (input == null || input.isBlank()) {
            currentAutocomplete = null;
            autocompleteLabel.setText("Autocomplete: none");
            clearSuggestions();
            return;
        }

        List<String> suggestions = queryEngine.suggestQueriesFuzzy(input)
                .stream()
                .limit(3)
                .toList();

        updateAutocomplete(input, suggestions);
        updateSuggestionList(suggestions);
    }

    private void updateAutocomplete(String input, List<String> suggestions) {
        currentAutocomplete = null;

        if (suggestions.isEmpty()) {
            autocompleteLabel.setText("Autocomplete: none");
            return;
        }

        String best = suggestions.get(0);

        if (best.equalsIgnoreCase(input)) {
            autocompleteLabel.setText("Autocomplete: already complete");
            return;
        }

        currentAutocomplete = best;
        autocompleteLabel.setText("Autocomplete: " + best);
    }

    private void updateSuggestionList(List<String> suggestions) {
        if (suggestions.isEmpty()) {
            clearSuggestions();
            return;
        }

        suggestionsList.getItems().setAll(suggestions);
        suggestionsList.setVisible(true);
        suggestionsList.setManaged(true);
    }

    private void acceptAutocomplete() {
        if (currentAutocomplete == null || currentAutocomplete.isBlank()) {
            return;
        }

        String acceptedAutocomplete = currentAutocomplete;

        searchField.setText(acceptedAutocomplete);
        searchField.positionCaret(acceptedAutocomplete.length());

        currentAutocomplete = null;
        clearSuggestions();
        statusLabel.setText("Autocomplete accepted.");
    }
    private void clearSuggestions() {
        suggestionsList.getItems().clear();
        suggestionsList.setVisible(false);
        suggestionsList.setManaged(false);
    }

    private void runSearch() {
        String query = searchField.getText().trim();

        if (query.isBlank()) {
            statusLabel.setText("Type a query first.");
            clearWidgets();
            return;
        }

        try {
            List<SearchResult> results = queryEngine.query(query);
            resultsList.getItems().setAll(results);

            updateContextAwareWidgets(query, results);
            clearSuggestions();

            statusLabel.setText(
                    "Found " + results.size()
                            + " result(s) using ranking: "
                            + queryEngine.getCurrentRankingStrategy()
            );

        } catch (SQLException e) {
            statusLabel.setText("Search failed: " + e.getMessage());
            clearWidgets();
        }
    }

    private void updateContextAwareWidgets(String query, List<SearchResult> results) {
        SearchContext context = new SearchContext(query, results);
        List<Node> activeWidgets = widgetFactory.createWidgets(context);

        widgetsBox.getChildren().setAll(activeWidgets);

        boolean hasWidgets = !activeWidgets.isEmpty();
        widgetsTitle.setVisible(hasWidgets);
        widgetsTitle.setManaged(hasWidgets);
        widgetsBox.setVisible(hasWidgets);
        widgetsBox.setManaged(hasWidgets);
    }

    private void clearWidgets() {
        widgetsBox.getChildren().clear();
        widgetsTitle.setVisible(false);
        widgetsTitle.setManaged(false);
        widgetsBox.setVisible(false);
        widgetsBox.setManaged(false);
    }

    private void runIndexing() {
        indexButton.setDisable(true);
        statusLabel.setText("Indexing files... please wait.");

        Thread indexingThread = new Thread(() -> {
            try {
                IndexingService indexingService = new IndexingService();
                indexingService.run();

                Platform.runLater(() -> {
                    indexButton.setDisable(false);
                    statusLabel.setText("Indexing finished.");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    indexButton.setDisable(false);
                    statusLabel.setText("Indexing failed: " + e.getMessage());
                });
            }
        });

        indexingThread.setDaemon(true);
        indexingThread.start();
    }
}