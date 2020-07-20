package vk.botArt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import vk.botArt.models.AccountInfo;
import vk.botArt.models.Settings;

import java.time.LocalDate;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {
	
	public Properties props = System.getProperties();

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) throws Exception {
        launch(args);
    	    	
    }

    public void start(Stage stage) throws Exception {
    	
    	props.setProperty("java.util.logging.config.file", "logging.properties");
    	
        String fxmlFile = "/fxml/hello.fxml";
        log.debug("Loading FXML for main view from: {}", fxmlFile);
        FXMLLoader loader = new FXMLLoader();
        Parent rootNode = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));

        log.debug("Showing JFX scene");
        Scene scene = new Scene(rootNode, 550, 500);
        scene.getStylesheets().add("/styles/styles.css");
        stage.setResizable(false);

        stage.setTitle("Массфоловинг");
        stage.setScene(scene);
        stage.show();
       
    }
}
