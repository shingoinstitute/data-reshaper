/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shingo.insightdatareshaper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author dustinehoman
 */
public class MySQLHelper {
    private Connection conn = null;
    private String dbname;
    public MySQLHelper(String dbname) throws SQLException, ClassNotFoundException{
        try {
            this.dbname = dbname;
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            // TODO: Change login info
            conn = DriverManager.getConnection("jdbc:mysql://localhost/", "root", "MYPASSWORD");
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbname);
            stmt.close();
            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
            
            // TODO: Change login info
            conn = DriverManager.getConnection("jdbc:mysql://localhost/" + dbname, "root", "MYPASSWORD");           
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
                    "OrgId TEXT NOT NULL, PRIMARY KEY (id));";
            stmt.execute(sql);
            stmt.close();
            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
        } catch (InstantiationException ex) {
            Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void insertAll(List<ResponseSet> list) throws SQLException, ClassNotFoundException{
        try {
            // TODO: Change login info
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://localhost/" + dbname, "root", "MYPASSWORD");           
            System.out.println("Connected to database successfully!");
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            for(ResponseSet res : list){
                String sql = "INSERT INTO Response_Set (Question, Response, SurveyId, OrgId) " +
                        "VALUES ( '" + res.getQuestion() + "', '" + res.getReponse() + "', '" + res.getSurveyId() + "', '" + res.getOrgId() + "');";
                
                stmt.execute(sql);
            }
            
            stmt.close();
            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
        } catch (InstantiationException ex) {
            Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
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
            conn = DriverManager.getConnection("jdbc:mysql://localhost/" + dbname, "root", "Shingo");           
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
        } catch (InstantiationException ex) {
            Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MySQLHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
