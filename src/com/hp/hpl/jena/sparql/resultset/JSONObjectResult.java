/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.sparql.lib.org.json.*;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.sparql.util.ALog;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import static com.hp.hpl.jena.sparql.resultset.JSONResults.* ;

/**
 * JSON Output as a JSON object
 * (Converted from a JSONStringer output writer by Elias Torres (<a href="mailto:elias@torrez.us">elias@torrez.us</a>)

 * @author Andy Seaborne 
 */

public class JSONObjectResult implements ResultSetProcessor
{
    static boolean outputExplicitUnbound = false;

    boolean outputGraphBNodeLabels = ARQ.isTrue(ARQ.outputGraphBNodeLabels);
    int bNodeCounter = 0;
    Map<Resource, String> bNodeMap = new HashMap<Resource, String>();
    
    JSONObject json ;
    JSONArray solutions ;
    JSONObject currentSolution ;

    static JSONObject booleanResult(boolean result)
    {
        try {
            JSONObject json = new JSONObject() ;
            json.put(dfHead, new JSONObject()) ;
            json.put(dfBoolean, result);
            return json ;
        } catch(Exception ex) {
            throw new ResultSetException(ex.getMessage(), ex);
        }
    }
    
    static JSONObject resultSet(ResultSet resultSet)
    {
        JSONObjectResult xOut =  new JSONObjectResult() ;
        ResultSetApply a = new ResultSetApply(resultSet, xOut) ;
        a.apply() ;
        return xOut.json ;
    }
    
    
    private JSONObjectResult() { }

    public void start(ResultSet rs)
    {
        json = new JSONObject() ;
        try {
            // ---- Header
            JSONObject head = new JSONObject() ;
            json.put(dfHead, head) ;
            
            JSONArray vars = new JSONArray() ;
            for (String string : rs.getResultVars())
                vars.put(string) ;
            head.put(dfVars, vars) ;
            // ---- results
            JSONObject results = new JSONObject() ;
            json.put(dfResults, results) ;
            //results.put(dfOrdered, rs.isOrdered()) ;
            //results.put(dfDistinct,rs.isDistinct());
            solutions = new JSONArray() ;
            results.put(dfBindings, solutions) ;
        } catch (JSONException ex) {
            throw new ResultSetException(ex.getMessage(), ex);
        }
    }
    public void finish(ResultSet rs)
    {}
    
    public void start(QuerySolution qs)
    {
        currentSolution = new JSONObject() ;
        solutions.put(currentSolution) ;
    }

    public void finish(QuerySolution qs) { currentSolution = null ; }

    public void binding(String varName, RDFNode node)
    {
        if (node == null && !outputExplicitUnbound)
            return;
        try {
            JSONObject val = valueAsJSON(node) ;
            currentSolution.put(varName, val) ;
        } catch (JSONException ex) { throw new ResultSetException(ex.getMessage(), ex); }
    }
    
    private JSONObject valueAsJSON(RDFNode node) throws JSONException
    {
        JSONObject jsonValue = new JSONObject() ;
        if (node == null)
        {
            // Unbound
            jsonValue.put(dfType, dfUnbound);
            jsonValue.put(dfValue, JSONObject.NULL) ;
            return jsonValue ;
        }
        
        if (node instanceof Literal)
            return literalAsJSON((Literal)node) ;
        
        if (node instanceof Resource)
            return resourceAsJSON((Resource) node);
        
        ALog.warn(this, "Unknown RDFNode type in result set: " + node.getClass());
        return jsonValue ;
    }
    
    private JSONObject resourceAsJSON(Resource resource) throws JSONException
    {
        JSONObject jsonValue = new JSONObject() ;
        
        if (resource.isAnon())
        {
            String label;
            
            if (outputGraphBNodeLabels)
                label = resource.asNode().getBlankNodeId().getLabelString();
            else {
                if (!bNodeMap.containsKey(resource))
                    bNodeMap.put(resource, "b" + (bNodeCounter++));
                label = bNodeMap.get(resource);
            }
            jsonValue.put(dfType, dfBNode);
            jsonValue.put(dfValue, label) ;
        } else {
            jsonValue.put(dfType, dfURI);
            jsonValue.put(dfValue, resource.getURI()) ;
        }
        return jsonValue ;
    }
    
    private JSONObject literalAsJSON(Literal literal) throws JSONException
    {
        JSONObject jsonValue = new JSONObject() ;
        String datatype = literal.getDatatypeURI();
        String lang = literal.getLanguage();

        if (datatype != null && !datatype.equals("")) {
            jsonValue.put(dfType, dfTypedLiteral);
            jsonValue.put(dfDatatype, datatype) ;
        } else {
            jsonValue.put(dfType, dfLiteral) ;
        }
        
        if (lang != null && !(lang.length() == 0))
            jsonValue.put(dfLang,lang);
        
        jsonValue.put(dfValue, literal.getLexicalForm());
        return jsonValue ;
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */