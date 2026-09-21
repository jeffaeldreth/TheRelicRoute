package libraryCatalog;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Scene.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("The Relic Route");
            stage.show();

            // Once the window is up, connect to the database (asking for the details on first run).
            Controller controller = loader.getController();
            Platform.runLater(controller::onWindowShown);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        DatabaseConnection.disconnect();
    }

    public static void main(String[] args) {

        launch(args);
    }
}
