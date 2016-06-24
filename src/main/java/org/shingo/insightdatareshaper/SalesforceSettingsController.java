/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shingo.insightdatareshaper;

import com.mcdermottroe.apple.OSXKeychain;
import com.mcdermottroe.apple.OSXKeychainException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        try {
            this.keychain = OSXKeychain.getInstance();
        } catch (OSXKeychainException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    interface Callback
    {
        void callback();
    }
    
    Scene scene, mainScene;
    OSXKeychain keychain;
    Preferences prefs;
    String SERVICE_NAME = "Data Reshaper Salesforce API";
    
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
        String password = "";
        try {
            password = keychain.findGenericPassword(SERVICE_NAME, idField.getText());
        } catch (OSXKeychainException ex) {
            // Do nothing as the password was not found
        }
        if(!password.equals("")){
            if(!password.equals(secretField.getText())){
                try {
                    keychain.modifyGenericPassword(SERVICE_NAME, idField.getText(), secretField.getText());
                } catch (OSXKeychainException ex) {
                    actiontarget.setText("Couldn't save password!");
                    Logger.getLogger(MySQLSettingsController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            try {
                keychain.addGenericPassword(SERVICE_NAME, idField.getText(), secretField.getText());
            } catch (OSXKeychainException ex) {
                actiontarget.setText("Couldn't save password!");
                Logger.getLogger(MySQLSettingsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        actiontarget.setText("Settings Saved!");
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        environmentField.setText(prefs.get("environment", "https://salesforce.com"));
        idField.setText(prefs.get("id", ""));
        try {
            secretField.setText(keychain.findGenericPassword(SERVICE_NAME, idField.getText()));
        } catch (OSXKeychainException ex) {
            Logger.getLogger(SalesforceSettingsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
}
