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
 *
 * @author dustinhoman
 */
public class MySQLSettingsController implements Initializable {
    
    public MySQLSettingsController() {
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
        String password = "";
        try {
            password = keychain.findGenericPassword("Data Reshaper MySQL", userField.getText());
        } catch (OSXKeychainException ex) {
            // Do nothing as the password was not found
        }
        if(!password.equals("")){
            if(!password.equals(passwordField.getText())){
                try {
                    keychain.modifyGenericPassword("Data Reshaper MySQL", userField.getText(), passwordField.getText());
                } catch (OSXKeychainException ex) {
                    actiontarget.setText("Couldn't save password!");
                    Logger.getLogger(MySQLSettingsController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            try {
                keychain.addGenericPassword("Data Reshaper MySQL", userField.getText(), passwordField.getText());
            } catch (OSXKeychainException ex) {
                actiontarget.setText("Couldn't save password!");
                Logger.getLogger(MySQLSettingsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        actiontarget.setText("Settings Saved!");
    }
    
    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hostField.setText(prefs.get("host", "localhost"));
        userField.setText(prefs.get("username", "root"));
        if(!userField.getText().equals("")){
            try {
                passwordField.setText(keychain.findGenericPassword("Data Reshaper MySQL", userField.getText()));
            } catch (OSXKeychainException ex) {
                Logger.getLogger(MySQLSettingsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
