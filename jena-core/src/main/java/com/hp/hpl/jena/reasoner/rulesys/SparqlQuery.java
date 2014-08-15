/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Rule.Parser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mba
 */
public class SparqlQuery implements ClauseEntry {

    private String SparqlCmd;
    List<String> resultVars;
    List<Node_RuleVariable> nodeVars;
    Parser parser;
    Query query;
    TriplePattern [] head;
    
    public SparqlQuery(String pSparqlCmd) {
        SparqlCmd = pSparqlCmd;
        
        getVars();
    }
    
    
    public SparqlQuery(String pSparqlCmd, Query pquery, Parser pparser) {
        parser = pparser;
        SparqlCmd = pSparqlCmd;
        query = pquery;
        
        getVars();

        nodeVars = new ArrayList<Node_RuleVariable>();
        
        for(String n1 : resultVars) {
            Node_RuleVariable n2 = parser.getNodeVar("?" + n1);
            nodeVars.add(n2);
        }

    }
    
    private void getVars() {
        resultVars = query.getResultVars();
    }
    
    public Query getQuery() {
        return query;
    }
    
    public List<String> getResultVars() {
        return resultVars;
    }
     
    public List<Node_RuleVariable> getNodeVars() {
        return nodeVars;
    }
    
    public void setNodeList(List<Node_RuleVariable> pNodeVars) {
        nodeVars = pNodeVars;
    }
    
    public String getSparqlCmd() {
        return SparqlCmd;
    }
    
    public Map<String, String> getRulesPrefixes() {
        return parser.getPrefixMap();
    }

    @Override
    public boolean sameAs(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    public void setHead(TriplePattern [] phead){
        head = phead;
    }


    
    public TriplePattern [] getHead() {
        return head;
    }
}
