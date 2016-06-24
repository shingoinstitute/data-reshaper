/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shingo.insightdatareshaper;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONObject;


/**
 *
 * @author dustinehoman
 */
public class MySQLHelper {
    private Connection conn = null;
    private String dbname;    
    private String USERNAME;
    private String PASSWORD;
    private String HOST;
    private Preferences prefs;
    
    private void getCredentialsFromSettings(){
        this.prefs = Preferences.userNodeForPackage(MySQLSettingsController.class);
        USERNAME = prefs.get("username", "root");
        HOST = prefs.get("host", "localhost");
        PASSWORD = prefs.get("password", "password");
    }
    
    public MySQLHelper(String dbname) throws SQLException, ClassNotFoundException{
        getCredentialsFromSettings();
        try {
            this.dbname = dbname;
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            // TODO: Change login info
            conn = DriverManager.getConnection("jdbc:mysql://" + HOST + "/", USERNAME, "JoeSmith1820!!");
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbname);
            stmt.close();
            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
            
            // TODO: Change login info
            conn = DriverManager.getConnection("jdbc:mysql://" + HOST + "/" + dbname, USERNAME, "JoeSmith1820!!");           
            System.out.println("Connected to database successfully!");
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            stmt.executeUpdate("drop table if exists Response_Set");
            conn.commit();
            String sql = "CREATE TABLE Response_Set " +
                    "(id MEDIUMINT NOT NULL AUTO_INCREMENT, " +
                    "Question TEXT NOT NULL, " +
                    "Response TEXT NOT NULL, " +
                    "SurveyId TEXT NOT NULL, " +
                    "PRIMARY KEY (id));";
            stmt.execute(sql);
            stmt.close();
            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void insertAll(List<ResponseSet> list) throws SQLException, ClassNotFoundException{
        try {
            // TODO: Change login info
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://" + HOST + "/" + dbname, USERNAME, PASSWORD);           
            System.out.println("Connected to database successfully!");
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            for(ResponseSet res : list){
                String question = StringEscapeUtils.escapeSql(res.getQuestion());
                String response = StringEscapeUtils.escapeSql(res.getReponse());
                String sql = "INSERT INTO Response_Set (Question, Response, SurveyId) " +
                        "VALUES ( '" + question + "', '" + response + "', '" + res.getSurveyId() + "');";
                
                stmt.execute(sql);
            }
            
            stmt.close();
            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void dropTable(String name) throws SQLException, ClassNotFoundException{
                try {
            // TODO: Change login info
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://" + HOST + "/" + dbname, USERNAME, PASSWORD);           
            System.out.println("Connected to database successfully!");
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            String sql = "DROP TABLE IF EXISTS " + name;
            stmt.execute(sql);
            
            stmt.close();
            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void insertRespondent(JSONObject org) throws SQLException, ClassNotFoundException{
        try {
            StringBuilder str = new StringBuilder();
            StringBuilder val = new StringBuilder();            
            List<String> filter = Arrays.asList("Id","Insight_Organization__c", "Gender__c", "Age__c", "Years_with_Employer__c", "Years_in_Current_Position__c", "Native_Language__c", "Skill_in_English__c","Level_of_Education__c", "Scope__c", "Role__c","Department_or_Job_Function__c","Position__c");
            Iterator<?> keys = org.keys();
            while(keys.hasNext()){
                String key = (String)keys.next();
                if(org.get(key) instanceof JSONObject || key.contains("Date") || key.contains("System")) {}
                else {
                    if(filter.contains(key)){
                        str.append(key);
                        str.append(", ");
                        val.append("'").append(org.get(key).toString().replace("'", "''")).append("', ");
                    }
                }
            }
            String columns = str.toString();
            columns = columns.substring(0, columns.length() - 2);
            String values = val.toString();
            values = values.substring(0, values.length() - 2);
   
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://" + HOST + "/" + dbname, USERNAME, PASSWORD);           
            System.out.println("Connected to database successfully!");
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            str = new StringBuilder();
            String[] cols = columns.split(", ");
            str.append("CREATE TABLE IF NOT EXISTS Respondent (_id MEDIUMINT NOT NULL AUTO_INCREMENT, ");
            for (String col : cols) {
                if (filter.contains(col)) {
                    str.append(col).append(" VARCHAR(767), ");
                }            
            }
            str.append("PRIMARY KEY (_id), UNIQUE (Id));");
            System.out.println("CREATE TABLE STMT: " + str.toString());
            stmt.executeUpdate(str.toString());
            str = new StringBuilder();
            str.append("INSERT INTO Respondent (").append(columns).append(")")
                    .append(" VALUES ( ").append(values).append(");");
            System.out.println("INSERT INTO STMT: " + str.toString());
            try{
                stmt.executeUpdate(str.toString());
            } catch(MySQLIntegrityConstraintViolationException cve){
                System.out.println("Duplicate Respondent");
            }
            stmt.close();
            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void insertOrg(JSONObject org) throws SQLException, ClassNotFoundException{
        try {
            StringBuilder str = new StringBuilder();
            StringBuilder val = new StringBuilder();
            Iterator<?> keys = org.keys();
            while(keys.hasNext()){
                String key = (String)keys.next();
                if(org.get(key) instanceof JSONObject || key.contains("Date") || key.contains("System")) {}
                else {
                    str.append(key);
                    str.append(", ");
                    val.append("'").append(org.get(key).toString().replace("'", "''")).append("', ");
                }
            }
            String columns = str.toString();
            columns = columns.substring(0, columns.length() - 2);
            String values = val.toString();
            values = values.substring(0, values.length() - 2);
            
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://" + HOST + "/" + dbname, USERNAME, PASSWORD);           
            System.out.println("Connected to database successfully!");
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            stmt.execute("DROP TABLE IF EXISTS Insight_Org");
            str = new StringBuilder();
            String[] cols = columns.split(", ");
            str.append("CREATE TABLE Insight_Org (_id MEDIUMINT NOT NULL AUTO_INCREMENT, ");
            for(int i = 0; i < cols.length; i++){
                str.append(cols[i]).append(" TEXT, ");
            }            
            str.append("PRIMARY KEY (_id));");
            System.out.println("CREATE TABLE STMT: " + str.toString());
            stmt.executeUpdate(str.toString());
            str = new StringBuilder();
            str.append("INSERT INTO Insight_Org (").append(columns).append(")")
                    .append(" VALUES ( ").append(values).append(");");
            System.out.println("INSERT INTO STMT: " + str.toString());
            stmt.executeUpdate(str.toString());
            stmt.close();
            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
