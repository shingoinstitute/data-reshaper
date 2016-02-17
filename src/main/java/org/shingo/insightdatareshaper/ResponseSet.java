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
public class ResponseSet {
    private String question;
    private String response;
    private String surveyId;
    
    public ResponseSet(String question, String response, String surveyId){
        this.question = question;
        this.response = response;
        this.surveyId = surveyId;
    }
    
    public String getQuestion(){
        return this.question;
    }
    
    public String getReponse(){
        return this.response;
    }
    
    public String getSurveyId(){
        return this.surveyId;
    }
    
    @Override
    public String toString(){
        return this.question + ": " + this.response + ": " + this.surveyId;
    }
}
