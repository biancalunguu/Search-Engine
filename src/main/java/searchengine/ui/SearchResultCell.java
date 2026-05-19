package searchengine.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import searchengine.model.FileRecord;
import searchengine.model.SearchResult;

/**
 * Custom cell used to display a search result in the JavaFX list.
 */
public class SearchResultCell extends ListCell<SearchResult> {

    @Override
    protected void updateItem(SearchResult result, boolean empty) {
        super.updateItem(result, empty);

        if (empty || result == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        FileRecord file = result.getFile();

        Label nameLabel = new Label(safe(file.getFileName()));
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label pathLabel = new Label(safe(file.getFilePath()));
        pathLabel.setWrapText(true);
        pathLabel.setStyle("-fx-text-fill: #555555;");

        String metadata = "Score: " + String.format("%.2f", file.getPathScore())
                + " | Extension: " + safe(file.getExtension())
                + " | Size: " + file.getSizeBytes() + " bytes";

        if (file.isImageFile()) {
            metadata += " | Image color: " + safe(file.getDominantColor());
        }

        Label metadataLabel = new Label(metadata);
        metadataLabel.setStyle("-fx-text-fill: #777777;");

        Label snippetLabel = new Label(safe(result.getSnippet()));
        snippetLabel.setWrapText(true);

        VBox container = new VBox(4, nameLabel, pathLabel, metadataLabel, snippetLabel);
        container.setPadding(new Insets(8));

        setText(null);
        setGraphic(container);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
