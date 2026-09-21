package webView;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private TabPane myTabPane;
    @FXML
    private Button loadButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button zoomInButton;
    @FXML
    private Button zoomOutButton;
    @FXML
    private Button backButton;
    @FXML
    private Button forwardButton;
    @FXML
    private Button secondandcharlesButton;
    @FXML
    private Button poshmarkButton;
    @FXML
    private TextField urlTextField;

    private final String duckDuckGoSettings = "kl=us-en&kae=d&k8=eb6b11&kx=eb6b11";
    private final String homePage = "https://duckduckgo.com/?" + duckDuckGoSettings;

    // Small helper bundling everything one tab needs to track independently —
    // its own WebView, WebEngine, zoom level, and pin state. Each Tab's userData holds one of these.
    private static class BrowserTab {
        final WebView webView;
        final WebEngine engine;
        double zoom = 1.0;
        boolean pinned = false;
        String titleWithoutPin = "New Tab"; // remembers the real title, separate from the 📌 marker

        BrowserTab(WebView webView) {
            this.webView = webView;
            this.engine = webView.getEngine();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        newTab(); // start with one tab open, showing the homepage

        // Whenever the user switches tabs, reflect that tab's URL back into the address bar
        myTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                BrowserTab bt = (BrowserTab) newTab.getUserData();
                String currentUrl = bt.engine.getLocation();
                if (currentUrl != null && !currentUrl.isEmpty()) {
                    urlTextField.setText(currentUrl);
                }
            }
        });
    }

    // Returns whichever BrowserTab is currently selected/active
    private BrowserTab getActiveTab() {
        Tab selected = myTabPane.getSelectionModel().getSelectedItem();
        return (BrowserTab) selected.getUserData();
    }

    public void newTab() {
        WebView webView = new WebView();
        BrowserTab browserTab = new BrowserTab(webView);

        Tab tab = new Tab("New Tab");
        tab.setContent(webView);
        tab.setUserData(browserTab);

        // Update the tab's label to match the page title once it loads
        browserTab.engine.titleProperty().addListener((obs, oldTitle, newTitle) -> {
            if (newTitle != null && !newTitle.isEmpty()) {
                browserTab.titleWithoutPin = newTitle;
                refreshTabLabel(tab, browserTab);
            }
        });

        browserTab.engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                String currentUrl = browserTab.engine.getLocation();
                if (currentUrl != null && currentUrl.contains("duckduckgo.com")) {
                    browserTab.engine.executeScript(
                            "var style = document.createElement('style');" +
                                    "style.innerHTML = 'body { background-color: #181818 !important; color: #eaeaea !important; }" +
                                    " a { color: #adc2fc !important; }';" +
                                    "document.head.appendChild(style);"
                    );
                }
            }
        });

        // Right-click context menu: Pin/Unpin this tab
        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
        javafx.scene.control.MenuItem pinItem = new javafx.scene.control.MenuItem("Pin Tab");
        pinItem.setOnAction(e -> togglePin(tab, browserTab, pinItem));
        contextMenu.getItems().add(pinItem);
        tab.setContextMenu(contextMenu);

        myTabPane.getTabs().add(tab);
        myTabPane.getSelectionModel().select(tab);

        browserTab.engine.load(homePage);
        urlTextField.setText(homePage);
    }

    // Toggles a tab's pinned state: prevents closing, marks it visually,
    // and keeps pinned tabs grouped at the front of the tab bar.
    private void togglePin(Tab tab, BrowserTab browserTab, javafx.scene.control.MenuItem pinItem) {
        browserTab.pinned = !browserTab.pinned;
        tab.setClosable(!browserTab.pinned);
        pinItem.setText(browserTab.pinned ? "Unpin Tab" : "Pin Tab");
        refreshTabLabel(tab, browserTab);
        reorderPinnedTabsToFront();
    }

    private void refreshTabLabel(Tab tab, BrowserTab browserTab) {
        tab.setText((browserTab.pinned ? "📌 " : "") + browserTab.titleWithoutPin);
    }

    // Moves every pinned tab to the front of the tab bar, keeping their
    // relative order among themselves and among the unpinned tabs unchanged.
    private void reorderPinnedTabsToFront() {
        ObservableList<Tab> tabs = myTabPane.getTabs();
        Tab currentlySelected = myTabPane.getSelectionModel().getSelectedItem();

        java.util.List<Tab> pinned = new java.util.ArrayList<>();
        java.util.List<Tab> unpinned = new java.util.ArrayList<>();

        for (Tab t : tabs) {
            BrowserTab bt = (BrowserTab) t.getUserData();
            if (bt.pinned) {
                pinned.add(t);
            } else {
                unpinned.add(t);
            }
        }

        tabs.clear();
        tabs.addAll(pinned);
        tabs.addAll(unpinned);

        myTabPane.getSelectionModel().select(currentlySelected);
    }

    public void closeTab() {
        Tab selected = myTabPane.getSelectionModel().getSelectedItem();
        BrowserTab bt = selected != null ? (BrowserTab) selected.getUserData() : null;

        if (selected != null && bt != null && !bt.pinned && myTabPane.getTabs().size() > 1) {
            myTabPane.getTabs().remove(selected);
        }
        // Does nothing if it's pinned, or if it's the last tab — always keep at least one open
    }



    public void loadPage() {
        BrowserTab bt = getActiveTab();
        String input = urlTextField.getText().trim();

        if (input.isEmpty()) {
            return;
        }

        String urlToLoad;

        if (looksLikeUrl(input)) {
            if (!input.startsWith("http://") && !input.startsWith("https://")) {
                urlToLoad = "https://" + input;
            } else {
                urlToLoad = input;
            }
        } else {
            String encodedQuery = URLEncoder.encode(input, StandardCharsets.UTF_8);
            urlToLoad = "https://duckduckgo.com/?q=" + encodedQuery + "&" + duckDuckGoSettings;
        }

        bt.engine.load(urlToLoad);
        urlTextField.setText(urlToLoad);
    }

    private boolean looksLikeUrl(String input) {
        if (input.startsWith("http://") || input.startsWith("https://")) {
            return true;
        }
        return !input.contains(" ") && input.matches(".*\\.[a-zA-Z]{2,}.*");
    }

    public void refreshPage() {
        getActiveTab().engine.reload();
    }

    public void zoomIn() {
        BrowserTab bt = getActiveTab();
        bt.zoom += 0.25;
        bt.webView.setZoom(bt.zoom);
    }

    public void zoomOut() {
        BrowserTab bt = getActiveTab();
        bt.zoom -= 0.25;
        bt.webView.setZoom(bt.zoom);
    }

    public void displayHistory() {
        BrowserTab bt = getActiveTab();
        WebHistory history = bt.engine.getHistory();
        ObservableList<WebHistory.Entry> entries = history.getEntries();

        ListView<String> historyListView = new ListView<>();
        for (WebHistory.Entry entry : entries) {
            historyListView.getItems().add(entry.getUrl());
        }

        historyListView.setOnMouseClicked(event -> {
            String selectedUrl = historyListView.getSelectionModel().getSelectedItem();
            if (selectedUrl != null) {
                bt.engine.load(selectedUrl);
                urlTextField.setText(selectedUrl);
            }
        });

        Stage historyStage = new Stage();
        historyStage.setTitle("History");
        historyStage.setScene(new Scene(historyListView, 400, 300));
        historyStage.show();
    }

    public void back() {
        BrowserTab bt = getActiveTab();
        WebHistory history = bt.engine.getHistory();
        if (history.getCurrentIndex() > 0) {
            history.go(-1);
            ObservableList<WebHistory.Entry> entries = history.getEntries();
            urlTextField.setText(entries.get(history.getCurrentIndex()).getUrl());
        }
    }

    public void forward() {
        BrowserTab bt = getActiveTab();
        WebHistory history = bt.engine.getHistory();
        ObservableList<WebHistory.Entry> entries = history.getEntries();
        if (history.getCurrentIndex() < entries.size() - 1) {
            history.go(1);
            urlTextField.setText(entries.get(history.getCurrentIndex()).getUrl());
        }
    }

    public void secondandCharles() {
        getActiveTab().engine.executeScript("window.location =\"https://www.2ndandcharles.com\";");
    }

    public void poshmark() {
        getActiveTab().engine.executeScript("window.location =\"https://www.poshmark.com\";");
    }

    public void search(String query) {
        urlTextField.setText(query);
        loadPage();
    }
}