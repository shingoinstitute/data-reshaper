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
public class RespondentSurvey {
    private String name;
    private String id;
    private String url;
    
    public RespondentSurvey(){}
    
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
    
    public void setUrl(String url){
        this.url = url;
    }
    
    public String getUrl(){
        return this.url;
    }
    
    @Override
    public String toString(){
        return "Name: " + this.name;
    }
    
    public String toDebugString(){
        return "Name: " + this.name + "\nID: " + this.id + "\nURL: " + this.url + "\n";
    }
}
