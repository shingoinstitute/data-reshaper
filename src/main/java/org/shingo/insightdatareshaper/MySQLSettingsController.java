/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shingo.insightdatareshaper;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 *
 * @author dustinhoman
 */
public class MySQLSettingsController implements Initializable {
    
    public MySQLSettingsController() {
        this.prefs = Preferences.userNodeForPackage(MySQLSettingsController.class);
    }
    
    interface Callback
    {
        void callback();
    }
    Scene scene, mainScene;
    Preferences prefs;
    
    @FXML private TextField hostField;
    @FXML private TextField userField;
    @FXML private PasswordField passwordField;
    @FXML private Text actiontarget;
    
    
    @FXML
    private void handleSubmitButtonAction(ActionEvent event) {
        if(hostField.getText().equals("") || userField.getText().equals("") || passwordField.getText().equals("")){
            actiontarget.setText("All fields are required!");
            return;
        }
        prefs.put("host", hostField.getText());
        prefs.put("username", userField.getText());
        prefs.put("password", passwordField.getText());
                
        actiontarget.setText("Settings Saved!");
    }
    
    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hostField.setText(prefs.get("host", "localhost"));
        userField.setText(prefs.get("username", "root"));
        passwordField.setText(prefs.get("password", "password"));
    }
    
}
