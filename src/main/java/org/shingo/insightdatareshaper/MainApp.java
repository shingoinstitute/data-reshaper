package org.shingo.insightdatareshaper;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import static javafx.application.Application.launch;
import javafx.scene.image.Image;


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
        
        final Menu menu1 = new Menu("Options");
        final MenuItem item1 = new MenuItem("MySQL");
        item1.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                try {
                    showMySQLSettings();
                } catch (IOException ex) {
                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        
        });
        final MenuItem item2 = new MenuItem("Salesforce");
        item2.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                try {
                    showSalesforceSettings();
                } catch (IOException ex) {
                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        menu1.getItems().addAll(item1, item2);
        
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(menu1);
        menuBar.setUseSystemMenuBar(true);        
 
 
        ((GridPane) scene.getRoot()).getChildren().addAll(menuBar);
        
        stage.setTitle("Insight Data-Reshaper");
        stage.setScene(scene);
        try{
            stage.getIcons().add(new Image(getClass().getResourceAsStream( "/images/shingo_flame.png" )));
        } catch(Exception ex){
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        stage.show();
    }
    
    private void showMySQLSettings() throws IOException {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MySQLSettings.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("MySQL Settings");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(scene.getWindow() );
        stage.show();
    }
    
    private void showSalesforceSettings() throws IOException {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/SalesforceSettings.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("Salesforce Settings");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(scene.getWindow() );
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
