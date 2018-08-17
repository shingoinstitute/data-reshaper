package org.shingo.insightdatareshaper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
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
        this.prefs = Preferences.userNodeForPackage(FXMLController.class);
        this.sf_prefs = Preferences.userNodeForPackage(SalesforceSettingsController.class);
    }

    // TODO: Set up Salesforce app info
    private static String CLIENT_ID;
    private static String CLIENT_SECRET;
    private static String ACCESS_TOKEN;
    private List<InsightOrg> orgList = new ArrayList<>();
    private List<RespondentSurvey> respondentSurveys = new ArrayList<>();
    private static InsightOrg selectedOrg;
    private MySQLHelper db;
    private int totalSize = 0;
    private int completedSurveys = 0;
    private ProgressIndicator pi;
    private Preferences prefs;
    private Preferences sf_prefs;
    private String PREF_NAME="datareshaper_username";
    private SalesforceConnector sf;

    @FXML private TextField userField;
    @FXML private PasswordField passwordField;
    @FXML private Text actiontarget;
    @FXML private ListView insightorgs;
    @FXML private HBox infobox;
    @FXML private Text size;
    @FXML private CheckBox rememberMe;

    private String login() throws IOException {
        String username = userField.getText();
        String password = passwordField.getText();

        if (rememberMe.isSelected()) {
            actiontarget.setText("Credentials not saved!");
            rememberMe(username, password);
        } else {
            if(prefs.getBoolean("REMEMBER_ME", false)){
                prefs.putBoolean("REMEMBER_ME", false);
            }
        }

        return this.sf.connect(CLIENT_ID, CLIENT_SECRET, username, password);
    }

    private String buildSOQLQuery(String sobject, String[] fields, String clause) throws UnsupportedEncodingException {
        String query = "SELECT " + String.join(", ", fields) + " FROM " + sobject +
                (clause != null ? " WHERE " + clause : "");
        return "/services/data/v32.0/query?q=" + URLEncoder.encode(query, "UTF-8");
    }

    private InsightOrg createInsightOrg(JSONObject jorg) {
        InsightOrg org = new InsightOrg();
        org.setName(jorg.getString("Name"));
        String objUrl = jorg.getJSONObject("attributes").getString("url");
        org.setUrl(objUrl);
        String[] split = objUrl.split("/");
        org.setId(split[split.length - 1]);

        return org;
    }

    private List<InsightOrg> getInsightOrgs() throws IOException {
        String[] fields = {
                "Name",
                "Status__c"
        };
        String sobject = "Insight_Organization__c";
        String clause = "Status__c='Survey Complete, Waiting Report'";
        String urlString = buildSOQLQuery(sobject, fields, clause);
        JSONObject result = sf.get(urlString);

        JSONArray records = result.getJSONArray("records");
        for(int i = 0; i < records.length(); i++){
            JSONObject jorg = records.getJSONObject(i);
            orgList.add(createInsightOrg(jorg));
        }

        return orgList;
    }

    private RespondentSurvey createRespondentSurvey(JSONObject jorg) {
        RespondentSurvey survey = new RespondentSurvey();
        survey.setName(jorg.getString("Name"));
        String objUrl = jorg.getJSONObject("attributes").getString("url");
        survey.setUrl(objUrl);
        String[] split = objUrl.split("/");
        survey.setId(split[split.length - 1]);

        return survey;
    }

    private List<RespondentSurvey> getRespondentSurveys() throws IOException {
        String[] fields = {
                "Name"
        };
        String sobject = "Insight_Respondent_Survey__c";
        String clause = "Insight_Organization__c = '" + selectedOrg.getId() + "'";
        String urlString = this.buildSOQLQuery(sobject, fields, clause);
        JSONObject result = sf.get(urlString);

        System.out.println("Reshape: " + result.toString());
        JSONArray records = result.getJSONArray("records");
        for(int i = 0; i < records.length(); i++){
            JSONObject jorg = records.getJSONObject(i);
            respondentSurveys.add(createRespondentSurvey(jorg));
        }

        totalSize = result.getInt("totalSize");

        return respondentSurveys;
    }

    @FXML
    private void handleSubmitButtonAction(ActionEvent event) {
        try {
            ACCESS_TOKEN = this.login();
            List<InsightOrg> orgs = this.getInsightOrgs();

            if (!ACCESS_TOKEN.equals("")) {
                ObservableList<InsightOrg> data = FXCollections.observableArrayList(orgs);
                Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
                Parent main = null;
                try {
                    main = FXMLLoader.load(getClass().getResource("/fxml/MainScene.fxml"));
                } catch (IOException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }

                Scene mainScene = new Scene(main);
                mainScene.getStylesheets().add("/styles/Styles.css");
                insightorgs = (ListView) mainScene.lookup("#insightorgs");
                size = (Text) mainScene.lookup("#size");
                insightorgs.setItems(data);
                size.setText(data.size() + " Organizations");
                insightorgs.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
                        selectedOrg = (InsightOrg)newValue;
                        System.out.println("Selected item: " + selectedOrg.getId());
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
    
    private void rememberMe(String username, String password) {
        prefs.putBoolean("REMEMBER_ME",true);
        if(!prefs.get(PREF_NAME, "").equals(username)){
            prefs.put(PREF_NAME, username);
        }
        String pass = prefs.get("pass", username);
        if(!pass.equals("")){
            if(!pass.equals(password)){
                prefs.put("pass", password);
            }
            return;
        }
        prefs.put("pass", password);
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

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        
        stage.setScene(scene);
    }
    
    @FXML
    private void handleReshapeButtonAction(ActionEvent event) {
        if (selectedOrg != NULL){
            try {
                // we moved to a new view, so the sf object was recreated
                sf.setAccessToken(ACCESS_TOKEN);
                List<RespondentSurvey> surveys = getRespondentSurveys();

                actiontarget.setText("Processing " + totalSize + " respondentSurveys...");
                pi = new ProgressIndicator(0);
                pi.setMinWidth(50);
                pi.setMinHeight(50);
                pi.setStyle(" -fx-progress-color: green;");
                if(infobox.getChildren().contains(pi)) infobox.getChildren().remove(pi);
                infobox.getChildren().add(pi);

                new Thread(() -> {
                    reshapeData(surveys, sf);
                    actiontarget.setText("");
                }).start();

            } catch (IOException ex) {
                actiontarget.setText("Oooops!!!!");
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void reshapeData(List<RespondentSurvey> surveys, SalesforceConnector connector) {
        try {
            List<ResponseSet> responseSet = new ArrayList<>();
            db = new MySQLHelper("Insight_Organization");
            String orgUrlString = "/" + selectedOrg.getUrl().substring(1);

            try {
                JSONObject result = connector.get(orgUrlString);
                db.insertOrg(result);
            } catch (IOException ex) {
                actiontarget.setText("Oooops!!!!");
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }

            db.dropTable("Respondent");

            for (int i = 0; i < surveys.size(); i++) {
                String surveyId = surveys.get(i).getId();
                String urlString = "/services/data/v32.0/sobjects/Insight_Respondent_Survey__c/" + surveyId;
                try {
                    JSONObject result = connector.get(urlString);

                    System.out.println("reshapeData[" + i + "]: " + result.toString());

                    db.insertRespondent(result);
                    Iterator<?> keys = result.keys();

                    List<String> filter = Arrays.asList(
                            "Name",
                            "LastModifiedById",
                            "IsDeleted","Id",
                            "CreatedById",
                            "Insight_Organization__c",
                            "Gender__c",
                            "Age__c",
                            "Years_with_Employer__c",
                            "Years_in_Current_Position__c",
                            "Native_Language__c",
                            "Skill_in_English__c",
                            "Level_of_Education__c",
                            "Scope__c",
                            "Role__c",
                            "Department_or_Job_Function__c",
                            "Position__c"
                    );

                    responseSet.clear();

                    while (keys.hasNext()) {
                        String key = (String)keys.next();
                        if (!(result.get(key) instanceof JSONObject || key.contains("Date") || key.contains("System") || filter.contains(key))) {
                            if (key.contains("SocD")) {
                                boolean bool = (result.get(key).equals("1") || result.get(key).equals("true") || result.get(key).equals("True") || result.get(key).equals("TRUE"));
                                responseSet.add(new ResponseSet(key, String.valueOf(bool), surveyId));
                            } else {
                                responseSet.add(new ResponseSet(key, result.get(key).toString(), surveyId));
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

    private void initSalesforce() {
        String environment = sf_prefs.get("environment","https://salesforce.com");
        this.sf = new SalesforceConnector(environment);
        CLIENT_ID = sf_prefs.get("id", "");
        CLIENT_SECRET = sf_prefs.get("secret", "");
    }

    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initSalesforce();
        if(!url.getPath().contains("Login")) return;
        if(prefs.getBoolean("REMEMBER_ME", false)){
            String username = prefs.get(PREF_NAME, "");
            String password = prefs.get("pass", username);

            userField.setText(username);
            passwordField.setText(password);
            rememberMe.setSelected(true);
        }
    }
}
