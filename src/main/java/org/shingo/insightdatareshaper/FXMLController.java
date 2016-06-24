package org.shingo.insightdatareshaper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import org.json.JSONArray;
import org.json.JSONObject;
import static org.json.JSONObject.NULL;


public class FXMLController implements Initializable {

    public FXMLController() {
//        try {
//            this.keychain = OSXKeychain.getInstance();
//        } catch (OSXKeychainException ex) {
//            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    interface Callback
    {
        void callback();
    }
    
    Scene scene, mainScene;
    // TODO: Set up Salesforce app info
    static String ENVIRONMENT = "YOUR_ENVIRONMENT_URL";
    static String CLIENT_ID = "YOUR_CLIENT_ID";
    static String CLIENT_SECRET = "YOUR_CLIENT_SECRET";
    static String ACCESS_TOKEN;
    List insightOrgs = new ArrayList<>();
    List surveys = new ArrayList<>();
    static InsightOrg selectedOrg;
    MySQLHelper db;
    int totalSize = 0;
    int completedSurveys = 0;
    ProgressIndicator pi;
//    OSXKeychain keychain;
    
    @FXML private TextField userField;
    @FXML private PasswordField passwordField;
    @FXML private Text actiontarget;
    @FXML private ListView insightorgs;
    @FXML private HBox infobox;
    @FXML private Text size;
    @FXML private CheckBox rememberMe;
    
    
    @FXML
    private void handleSubmitButtonAction(ActionEvent event) {
        String username = userField.getText();
        String password = passwordField.getText();
        /*if(rememberMe.isSelected()){
           keychain.addGenericPassword("Data Reshaper", username, password);
        }*/
        System.out.println("Username: " + username + "; Password: " + password);;
        String urlString = ENVIRONMENT + "services/oauth2/token?grant_type=password&client_id=" + 
                CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&username=" + 
                username + "&password=" + password;
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8")); 
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);
            
            System.out.println(responseStrBuilder.toString());
            JSONObject result = new JSONObject(responseStrBuilder.toString());
            ACCESS_TOKEN = result.getString("access_token");
            if(!ACCESS_TOKEN.equals(""))
            {
                urlString = ENVIRONMENT + "services/data/v32.0/query?q=SELECT+Name,+Status__c+from+Insight_Organization__c+WHERE+Status__c%3D%27Survey%20Complete%2C%20Waiting%20Report%27";
                url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
                connection.setRequestProperty("Content-Type", "application/json");
                streamReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                responseStrBuilder = new StringBuilder();

                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);
                
                System.out.println(responseStrBuilder.toString());
                result = new JSONObject(responseStrBuilder.toString());
                JSONArray records = result.getJSONArray("records");
                for(int i = 0; i < records.length(); i++){
                    JSONObject jorg = records.getJSONObject(i);
                    InsightOrg org = new InsightOrg();
                    org.setName(jorg.getString("Name"));
                    String objUrl = jorg.getJSONObject("attributes").getString("url");
                    org.setUrl(objUrl);
                    String[] split = objUrl.split("/");
                    org.setId(split[split.length - 1]);
                    insightOrgs.add(org);
                    System.out.println(org.toDebugString());
                    
                }
                ObservableList<String> data = FXCollections.observableArrayList(insightOrgs);
                Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
                Parent main = null;
                try {
                    main = FXMLLoader.load(getClass().getResource("/fxml/MainScene.fxml"));                    
                } catch (IOException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
                 
                mainScene = new Scene(main);
                mainScene.getStylesheets().add("/styles/Styles.css");
                insightorgs = (ListView) mainScene.lookup("#insightorgs");
                size = (Text) mainScene.lookup("#size");
                insightorgs.setItems(data);
                size.setText(data.size() + " Organizations");
                insightorgs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<InsightOrg>() {
                    @Override
                    public void changed(ObservableValue<? extends InsightOrg> observable, InsightOrg oldValue, InsightOrg newValue) {
                        // Your action here
                        selectedOrg = newValue;
                        System.out.println("Selected item: " + selectedOrg.getId());
                    }
                });
                stage.setScene(mainScene);
            }
        } catch (IOException ex) {
            if(ex.getMessage().contains("response code: 400")){
                actiontarget.setText("Bad username or password!");
            }
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void handleLogoutButtonAction(ActionEvent event) {
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/LoginScene.fxml"));
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        
        stage.setScene(scene);
    }
    
    @FXML
    private void handleReshapeButtonAction(ActionEvent event) {
        if(selectedOrg != NULL){
            String urlString = ENVIRONMENT + "services/data/v32.0/query?q=SELECT+name+from+Insight_Respondent_Survey__c+where+Insight_Organization__c+=+'" + selectedOrg.getId() + "'";
            URL url = null;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
                connection.setRequestProperty("Content-Type", "application/json");
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8")); 
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                System.out.println("Reshape: " + responseStrBuilder.toString());
                JSONObject result = new JSONObject(responseStrBuilder.toString());
                JSONArray records = result.getJSONArray("records");
                for(int i = 0; i < records.length(); i++){
                    JSONObject jorg = records.getJSONObject(i);
                    RespondentSurvey survey = new RespondentSurvey();
                    survey.setName(jorg.getString("Name"));
                    String objUrl = jorg.getJSONObject("attributes").getString("url");
                    survey.setUrl(objUrl);
                    String[] split = objUrl.split("/");
                    survey.setId(split[split.length - 1]);
                    surveys.add(survey);
                }
                totalSize = result.getInt("totalSize");
                actiontarget.setText("Processing " + totalSize + " surveys...");
                pi = new ProgressIndicator(0);
                pi.setMinWidth(50);
                pi.setMinHeight(50);
                pi.setStyle(" -fx-progress-color: green;");
                if(infobox.getChildren().contains(pi)) infobox.getChildren().remove(pi);
                infobox.getChildren().add(pi);
                
                class OneShotTask implements Runnable {
                    List list;
                    Callback c;
                    OneShotTask(List list, Callback c) { this.list = list; this.c = c; }
                    @Override
                    public void run(){
                        reshapeData(list);
                        this.c.callback();
                    }
                }
                Thread t = new Thread(new OneShotTask(surveys, new Callback(){
                    @Override
                    public void callback(){
                        actiontarget.setText("");
                    }
                }));
                t.start();
                
            } catch (IOException ex) {
                actiontarget.setText("Oooops!!!!");
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void reshapeData(List surveys){
        try {
            List<ResponseSet> responseSet = new ArrayList<>();
            db = new MySQLHelper("Insight_Organization");
            String orgurlstring = ENVIRONMENT + selectedOrg.getUrl().substring(1);
            URL orgurl = null;
            try {
                orgurl = new URL(orgurlstring);
            } catch (MalformedURLException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                HttpURLConnection connection = (HttpURLConnection) orgurl.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
                connection.setRequestProperty("Content-Type", "application/json");
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                JSONObject result = new JSONObject(responseStrBuilder.toString());
                db.insertOrg(result);
            } catch (IOException ex) {
                actiontarget.setText("Oooops!!!!");
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
            db.dropTable("Respondent");
            for(int i = 0; i < surveys.size(); i++){
                String surveyId = ((RespondentSurvey) surveys.get(i)).getId();
                String urlString = ENVIRONMENT + "services/data/v32.0/sobjects/Insight_Respondent_Survey__c/" + surveyId;
                URL url = null;
                try {
                    url = new URL(urlString);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
                    connection.setRequestProperty("Content-Type", "application/json");
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    StringBuilder responseStrBuilder = new StringBuilder();

                    String inputStr;
                    while ((inputStr = streamReader.readLine()) != null)
                        responseStrBuilder.append(inputStr);

                    System.out.println("reshapeData[" + i +"]: " + responseStrBuilder.toString());
                    JSONObject result = new JSONObject(responseStrBuilder.toString());
                    db.insertRespondent(result);
                    Iterator<?> keys = result.keys();
                    List<String> filter = Arrays.asList("Name", "LastModifiedById","IsDeleted","Id","CreatedById", "Insight_Organization__c", "Gender__c", "Age__c", "Years_with_Employer__c", "Years_in_Current_Position__c", "Native_Language__c", "Skill_in_English__c","Level_of_Education__c", "Scope__c", "Role__c","Department_or_Job_Function__c","Position__c");
                    responseSet.clear();
                    while(keys.hasNext()){
                        String key = (String)keys.next();
                        if(result.get(key) instanceof JSONObject || key.contains("Date") || key.contains("System") || filter.contains(key)) {}
                        else {
                            if(key.contains("SocD")){
                                boolean bool = (result.get(key).equals("1") || result.get(key).equals("true") || result.get(key).equals("True") || result.get(key).equals("TRUE"));
                                responseSet.add(new ResponseSet(key,String.valueOf(bool),surveyId));
                            }
                            else{
                                responseSet.add(new ResponseSet(key,result.get(key).toString(),surveyId));
                            }
                        }
                    }
                    insertResponseSet(responseSet);
                    completedSurveys += 1;
                    pi.setProgress(completedSurveys / (double) totalSize);
                } catch (IOException ex) {
                    actiontarget.setText("Oooops!!!!");
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void insertResponseSet(List<ResponseSet> data){
        try {            
            db.insertAll(data);
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
}
