package org.shingo.insightdatareshaper;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApp extends Application {
    Scene scene, mainScene;
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginScene.fxml"));
        Parent main = FXMLLoader.load(getClass().getResource("/fxml/MainScene.fxml"));
        
        scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        
        mainScene = new Scene(main);
        mainScene.getStylesheets().add("/styles/Styles.css");
        
        stage.setTitle("Insight Data-Reshaper");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
