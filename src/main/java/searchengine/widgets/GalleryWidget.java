package searchengine.widgets;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import searchengine.model.FileRecord;
import searchengine.model.SearchResult;

import java.nio.file.Path;
import java.util.List;

public class GalleryWidget implements SearchWidget {

    private static final int THUMBNAIL_SIZE = 150;
    private static final int COLUMNS = 3;

    @Override
    public boolean supports(SearchContext context) {
        return context.hasResults()
                && (
                context.resultsArePredominantlyImages()
                        || context.hasManyImages()
                        || context.queryLooksImageRelated()
        )
                && context.countImages() > 0;
    }

    @Override
    public Node createNode(SearchContext context) {
        Button button = new Button("View as Gallery");

        button.setTooltip(new javafx.scene.control.Tooltip(context.describeImageActivationReason()));

        button.setOnAction(event -> openGalleryWindow(context));

        return button;
    }

    private void openGalleryWindow(SearchContext context) {
        List<FileRecord> imageFiles = context.getResults()
                .stream()
                .map(SearchResult::getFile)
                .filter(FileRecord::isImageFile)
                .toList();

        Stage stage = new Stage();
        stage.setTitle("Image Gallery");

        Label title = new Label("Image Gallery");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label reason = new Label(context.describeImageActivationReason());
        reason.setStyle("-fx-text-fill: #555555;");

        Label summary = new Label(
                "Showing " + imageFiles.size()
                        + " image result(s) out of "
                        + context.getResultCount()
                        + " total result(s)."
        );

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(12));

        int index = 0;

        for (FileRecord file : imageFiles) {
            VBox card = createImageCard(file);

            int row = index / COLUMNS;
            int column = index % COLUMNS;

            grid.add(card, column, row);
            index++;
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);

        VBox root = new VBox(10);
        root.setPadding(new Insets(16));
        root.getChildren().addAll(title, reason, summary, scrollPane);

        Scene scene = new Scene(root, 650, 500);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createImageCard(FileRecord file) {
        ImageView imageView = new ImageView();

        try {
            String uri = Path.of(file.getFilePath()).toUri().toString();
            Image image = new Image(uri, THUMBNAIL_SIZE, THUMBNAIL_SIZE, true, true);

            imageView.setImage(image);
            imageView.setFitWidth(THUMBNAIL_SIZE);
            imageView.setFitHeight(THUMBNAIL_SIZE);
            imageView.setPreserveRatio(true);

        } catch (Exception e) {
            imageView.setFitWidth(THUMBNAIL_SIZE);
            imageView.setFitHeight(THUMBNAIL_SIZE);
        }

        String color = file.getDominantColor();

        if (color == null || color.isBlank()) {
            color = "unknown";
        }

        Label fileName = new Label(file.getFileName());
        fileName.setWrapText(true);
        fileName.setMaxWidth(THUMBNAIL_SIZE);

        Label colorLabel = new Label("Dominant color: " + color);
        colorLabel.setStyle("-fx-text-fill: #555555;");

        VBox card = new VBox(6);
        card.setPadding(new Insets(8));
        card.setStyle(
                "-fx-border-color: #dddddd;"
                        + "-fx-border-radius: 6;"
                        + "-fx-background-radius: 6;"
                        + "-fx-background-color: #fafafa;"
        );

        card.getChildren().addAll(imageView, fileName, colorLabel);

        return card;
    }
}