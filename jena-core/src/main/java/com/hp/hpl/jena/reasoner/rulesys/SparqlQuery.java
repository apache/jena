/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Rule.Parser;
import static com.hp.hpl.jena.reasoner.rulesys.SparqlInRulesGenericFunctions.getAllVars;
import static com.hp.hpl.jena.reasoner.rulesys.SparqlInRulesGenericFunctions.validCharFirst;
import static com.hp.hpl.jena.reasoner.rulesys.SparqlInRulesGenericFunctions.validCharOthers;
import com.hp.hpl.jena.sparql.lang.SPARQLParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class SparqlQuery implements ClauseEntry {

    private String SparqlCmd;
    private String originalSparqlCmd;
    List<String> resultVars;
    List<Node_RuleVariable> nodeVars;
    Parser parser;
    Query query;
    TriplePattern [] head;
    String internalKey;
    String outerVarChar = "&";
    int internal_i = 0;
    HashMap<String, String> ov_map1 = new HashMap<String, String>();
    HashMap<String, String> ov_map2 = new HashMap<String, String>();
    /*
    public SparqlQuery(String pSparqlCmd) {
        SparqlCmd = pSparqlCmd;
        
        getVars();
    }
    */
    
    public SparqlQuery(SparqlQuery sq) {
        clone(sq);
    }
    
    
    public SparqlQuery(String pSparqlCmd, Parser pparser) {
        initSparqlQuery(pSparqlCmd, pparser);
    }
    
    public void initSparqlQuery(String pSparqlCmd, Parser pparser) {
        this.originalSparqlCmd = pSparqlCmd;
        this.internalKey = "OV_"+generatekey(8);
        
        while(pSparqlCmd.contains(this.internalKey)) {
            this.internalKey = "OV_"+generatekey(8);
        }
        
        this.parser = pparser;
        
        String sparqlCmd = replaceInternalRepresentation(pSparqlCmd);
        init(sparqlCmd);
    }

    
    public SparqlQuery(String pSparqlCmd) {
        this.internalKey = "OV_"+generatekey(8);
        
        while(pSparqlCmd.contains(this.internalKey)) {
            this.internalKey = "OV_"+generatekey(8);
        }
        
        String sparqlCmd = replaceInternalRepresentation(pSparqlCmd);
        this.SparqlCmd = sparqlCmd;
        //init(sparqlCmd);
    }
    
    private String replaceInternalRepresentation(String pSparqlCmd) {
        ArrayList<Integer> procsNum = new ArrayList<Integer>();
        String sparqlCmd = replaceInternalKey(pSparqlCmd);
        ArrayList<String> vgetAllVars = getAllVarsStr(sparqlCmd);
        for(String v : vgetAllVars) {
            if(v.startsWith(this.internalKey)) {
                int varNum = Integer.valueOf(v.substring(this.internalKey.length()+1));
                procsNum.add(varNum);
            }
        }
        
        for(int i=1; i<= this.internal_i; i++) {
            String intKey = this.internalKey +"_" + i;
            if(!procsNum.contains(i)) {
                sparqlCmd = sparqlCmd.replace("?" + intKey, outerVarChar + ov_map2.get(intKey));
            }
        }
        
        return sparqlCmd;
    }
    
    
    
    private String replaceInternalKey(String pSparqlCmd) {
        String retSparqlCmd = "";
        
        int i=0;
        while(i<pSparqlCmd.length()) {
            if(pSparqlCmd.substring(i).startsWith(outerVarChar)) {
                String varName = getVarName(pSparqlCmd.substring(i + outerVarChar.length()));
                if(varName.length()>0) {
                    String internalKeyVar;
                    if(ov_map1.containsKey(varName)) {
                        internalKeyVar = ov_map1.get(varName);
                    }
                    else {
                        internalKeyVar = internalKey + "_" + (++internal_i);
                        ov_map1.put(varName, internalKeyVar);
                        ov_map2.put(internalKeyVar, varName);
                    }
                    retSparqlCmd += "?" + internalKeyVar;
                    i+= outerVarChar.length() + varName.length();
                }  
                else {
                    retSparqlCmd += pSparqlCmd.charAt(i);
                    i++;
                }
            }
            else {
                retSparqlCmd += pSparqlCmd.charAt(i);
                i++;
            }
        }

        return retSparqlCmd;
    }
    
    private String getVarName(String cmd) {
        String retVar = "";
        
        if(validCharFirst(cmd.charAt(0))) {
            retVar += cmd.charAt(0);
            
            int i=1;
            
            while(validCharOthers(cmd.charAt(i))){
                retVar += cmd.charAt(i);
                i++;
            }
        }

        return retVar;
    }
    
    private void init(String pSparqlCmd) {
        
        setSparqlQuery(pSparqlCmd);
        
        getVars();

    }
    
    /*
    private void init(String pSparqlCmd, Parser pparser) {
        parser = pparser;
        
        setSparqlQuery(pSparqlCmd);
        
        getVars();

        fillNodeVars();
    }
    */
    
    private void fillNodeVars() {
        nodeVars = new ArrayList<Node_RuleVariable>();
        
        for(String n1 : resultVars) {
            Node_RuleVariable n2 = parser.getNodeVar("?" + n1);
            nodeVars.add(n2);
        }
        
    }
    
    public Set<String> tmp_ov_map1_keySet() {
        return ov_map1.keySet();
    }
    
    
    public boolean existOuterVariable(String v) {
        return ov_map1.containsKey(v);
    }
    
    public String getOuterVariable(String v) {
        return ov_map1.get(v);
    }
    
    public void setSparqlQuery(String pSparqlCmd) {
        SparqlCmd = pSparqlCmd;
        SPARQLParser parser2 = SPARQLParser.createParser(Syntax.defaultQuerySyntax) ;
        query = new Query();
        
        addRulePrefixes(query);

        parser2.parse(query, pSparqlCmd);

    }
    
    private void addRulePrefixes(Query q) {
        Map<String, String> rulePrefixes = getRulesPrefixes();
        if(rulePrefixes != null) {
            Set keys = rulePrefixes.keySet();
            for (Iterator i = keys.iterator(); i.hasNext();) 
            {
                String prefix = (String) i.next();
                String localname = (String) getRulesPrefixes().get(prefix);
                q.setPrefix(prefix, localname);
            }
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
     
    /*
    public List<Node_RuleVariable> getNodeVars() {
        return nodeVars;
    }
    */
    
    public void setNodeList(List<Node_RuleVariable> pNodeVars) {
        nodeVars = pNodeVars;
    }
    
    public String getSparqlCmd() {
        return SparqlCmd;
    }
    
    public String getOriginalSparqlCmd() {
        return originalSparqlCmd;
    }
    
    public Map<String, String> getRulesPrefixes() {
        return parser != null ? parser.getPrefixMap() : null;
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
    
    
    public void clone(SparqlQuery sq) {
        initSparqlQuery(sq.getOriginalSparqlCmd(), sq.parser);
    }
    
    public static String generatekey(int lenKey) {
        String keygenerated = "";
        Random generator = new Random();
        
        char [] ch = {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm', 'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X', 'C', 'V', 'B', 'N', 'M', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        
        
        for(int i=0; i<lenKey;i++) 
            keygenerated += ch[generator.nextInt(ch.length)];
        
        return keygenerated;        
    }
    
    
    private ArrayList<String> getAllVarsStr(String sparqlCmd) {
        SPARQLParser parser2 = SPARQLParser.createParser(Syntax.defaultQuerySyntax) ;
        Query q = new Query();
        
        addRulePrefixes(q);

        parser2.parse(q, sparqlCmd);
        ArrayList<String> retVars = getAllVars(q);
        
        return retVars;
    }
    
    
}
