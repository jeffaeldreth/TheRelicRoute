package webView;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        showBrowserStage(stage, null);
    }

    // Opens the browser in a new window from inside an already-running JavaFX app
    // (e.g. libraryCatalog), since Application.launch() can only be called once per program.
    // Pass a search term/URL to load immediately, or null to just show the homepage.
    public static void openWithSearch(String query) {
        showBrowserStage(new Stage(), query);
    }

    private static void showBrowserStage(Stage stage, String initialQuery) {
        try {
            File cookieFile = new File(System.getProperty("user.home"), ".insomniac-browser-cookies.dat");
            PersistentCookieStore cookieStore = new PersistentCookieStore(cookieFile);
            CookieHandler.setDefault(new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL));

            FXMLLoader loader = new FXMLLoader(Main.class.getResource("Scene.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("The Relic Route - Web Search");

            stage.setOnCloseRequest(event -> cookieStore.save());

            stage.setMaximized(true);

            stage.show();

            if (initialQuery != null && !initialQuery.isBlank()) {
                Controller controller = loader.getController();
                controller.search(initialQuery);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}