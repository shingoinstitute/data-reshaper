/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shingo.insightdatareshaper;

/**
 *
 * @author dustinehoman
 */
public class InsightOrg {
    private String name;
    private String id;
    private String sobject = "Insight_Application__c";

    public InsightOrg(){
        
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getName(){
        return this.name;
    }
    
    public void setId(String id){
        this.id = id;
    }

    public String getId(){
        return this.id;
    }

    public void setSObject(String sobject) { this.sobject = sobject; }
    public String getSObject() { return this.sobject; }

    @Override
    public String toString(){
        return this.name;
    }
    
    public String toDebugString(){
        return "Name: " + this.name + "\nID: " + this.id + "\nsObject: " + this.sobject + "\n";
    }
}
