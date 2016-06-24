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
 * FXML Controller class
 *
 * @author dustinhoman
 */
public class SalesforceSettingsController implements Initializable {

    public SalesforceSettingsController(){
        this.prefs = Preferences.userNodeForPackage(MySQLSettingsController.class);
    }
    
    
    interface Callback
    {
        void callback();
    }
    
    Scene scene, mainScene;
    Preferences prefs;
    
    @FXML private TextField environmentField;
    @FXML private TextField idField;
    @FXML private PasswordField secretField;
    @FXML private Text actiontarget;
    
    
    @FXML
    private void handleSubmitButtonAction(ActionEvent event) {
        if(environmentField.getText().equals("") || idField.getText().equals("") || secretField.getText().equals("")){
            actiontarget.setText("All fields are required!");
            return;
        }
        prefs.put("environment", environmentField.getText());
        prefs.put("id", idField.getText());
        prefs.put("secret", secretField.getText());
        
        actiontarget.setText("Settings Saved!");
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        environmentField.setText(prefs.get("environment", "https://salesforce.com"));
        idField.setText(prefs.get("id", ""));
        secretField.setText(prefs.get("secret", ""));
    }    
    
}
