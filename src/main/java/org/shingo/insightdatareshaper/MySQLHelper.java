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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

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

    private Connection getConnection(String host, String username, String password, String db) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        conn = DriverManager.getConnection("jdbc:mysql://" + host + "/" + (db != null ? db : ""), username, password);
        System.out.println("Connected to database successfully!");
        return conn;
    }

    private void createDatabase(String dbname) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        conn = getConnection(HOST, USERNAME, PASSWORD, null);
        conn.setAutoCommit(false);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbname);
        stmt.close();
        conn.commit();
        conn.setAutoCommit(true);
        conn.close();
    }

    public MySQLHelper(String dbname) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        getCredentialsFromSettings();
        this.dbname = dbname;
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        createDatabase(dbname);
    }

    public void dropTable(String name) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        conn = getConnection(HOST, USERNAME, PASSWORD, dbname);
        conn.setAutoCommit(false);
        Statement stmt = conn.createStatement();
        String sql = "DROP TABLE IF EXISTS " + name;
        stmt.execute(sql);

        stmt.close();
        conn.commit();
        conn.setAutoCommit(true);
        conn.close();
    }

    public void insertRespondent(JSONObject org) throws SQLException, ClassNotFoundException {
        try {
            StringBuilder str = new StringBuilder();
            StringBuilder val = new StringBuilder();
            Iterator<?> keys = org.keys();
            while(keys.hasNext()){
                String key = (String)keys.next();
                if (!(org.get(key) instanceof JSONObject || key.contains("Date") || key.contains("System"))) {
                    str.append(key);
                    str.append(", ");
                    val.append("'").append(org.get(key).toString().replace("'", "''")).append("', ");
                }
            }
            String columns = str.toString();
            // strip last comma and space
            columns = columns.substring(0, columns.length() - 2);
            String values = val.toString();
            values = values.substring(0, values.length() - 2);

            conn = getConnection(HOST, USERNAME, PASSWORD, dbname);
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            str = new StringBuilder();
            String[] cols = columns.split(", ");
            str.append("CREATE TABLE IF NOT EXISTS Respondent (_id MEDIUMINT NOT NULL AUTO_INCREMENT, ");
            for (String col : cols) {
                if (col.equals("Id")) {
                    str.append(col).append(" VARCHAR(727), ");
                } else {
                    str.append(col).append(" MEDIUMTEXT, ");
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
                if (!(org.get(key) instanceof JSONObject || key.contains("Date") || key.contains("System"))) {
                    str.append(key);
                    str.append(", ");
                    val.append("'").append(org.get(key).toString().replace("'", "''")).append("', ");
                }
            }
            String columns = str.toString();
            columns = columns.substring(0, columns.length() - 2);
            String values = val.toString();
            values = values.substring(0, values.length() - 2);

            conn = getConnection(HOST, USERNAME, PASSWORD, dbname);
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            stmt.execute("DROP TABLE IF EXISTS Insight_Org");
            str = new StringBuilder();
            String[] cols = columns.split(", ");
            str.append("CREATE TABLE Insight_Org (_id MEDIUMINT NOT NULL AUTO_INCREMENT, ");
            for (String col : cols) {
                str.append(col).append(" TEXT, ");
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
