package searchengine;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import searchengine.db.DatabaseConnection;
import searchengine.ui.SearchEngineApp;

import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            DatabaseConnection.getInstance().getConnection();
            DatabaseConnection.getInstance().initializeDatabase("sql/schema.sql");

            SearchEngineApp app = new SearchEngineApp();

            Scene scene = new Scene(app.getRoot(), 1000, 700);

            stage.setTitle("Local Search Engine");
            stage.setScene(scene);
            stage.show();

        } catch (SQLException e) {
            System.err.println("Could not connect to database: " + e.getMessage());
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        DatabaseConnection.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}