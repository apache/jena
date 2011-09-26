/**
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

package com.hp.hpl.jena.sparql.resultset;

import static com.hp.hpl.jena.sparql.resultset.JSONResults.* ;

import java.util.HashMap ;
import java.util.Map ;

import org.openjena.atlas.json.JsonArray ;
import org.openjena.atlas.json.JsonException ;
import org.openjena.atlas.json.JsonNull ;
import org.openjena.atlas.json.JsonObject ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;

/**
 * JSON Output as a JSON object
 * (Converted from a JSONStringer output writer by Elias Torres (<a href="mailto:elias@torrez.us">elias@torrez.us</a>)
 * Upgraded to */

public class JSONObjectResult implements ResultSetProcessor
{
    // UNUSED
    static boolean outputExplicitUnbound = false;

    boolean outputGraphBNodeLabels = ARQ.isTrue(ARQ.outputGraphBNodeLabels);
    int bNodeCounter = 0;
    Map<Resource, String> bNodeMap = new HashMap<Resource, String>();
    
    JsonObject json ;
    JsonArray solutions ;
    JsonObject currentSolution ;

    static JsonObject booleanResult(boolean result)
    {
        try {
            JsonObject json = new JsonObject() ;
            json.put(dfHead, new JsonObject()) ;
            json.put(dfBoolean, result);
            return json ;
        } catch(Exception ex) {
            throw new ResultSetException(ex.getMessage(), ex);
        }
    }
    
    static JsonObject resultSet(ResultSet resultSet)
    {
        JSONObjectResult xOut =  new JSONObjectResult() ;
        ResultSetApply a = new ResultSetApply(resultSet, xOut) ;
        a.apply() ;
        return xOut.json ;
    }
    
    
    private JSONObjectResult() { }

    public void start(ResultSet rs)
    {
        json = new JsonObject() ;
        try {
            // ---- Header
            JsonObject head = new JsonObject() ;
            
            json.put(dfHead, head) ;
            JsonArray vars = new JsonArray() ;
            for (String string : rs.getResultVars())
                vars.add(string) ;
                
            head.put(dfVars, vars) ;
            // ---- results
            JsonObject results = new JsonObject() ;
            json.put(dfResults, results) ;
            //results.put(dfOrdered, rs.isOrdered()) ;
            //results.put(dfDistinct,rs.isDistinct());
            solutions = new JsonArray() ;
            results.put(dfBindings, solutions) ;
        } catch (JsonException ex) {
            throw new ResultSetException(ex.getMessage(), ex);
        }
    }
    public void finish(ResultSet rs)
    {}
    
    public void start(QuerySolution qs)
    {
        currentSolution = new JsonObject() ;
        solutions.add(currentSolution) ;
    }

    public void finish(QuerySolution qs) { currentSolution = null ; }

    public void binding(String varName, RDFNode node)
    {
        if (node == null && !outputExplicitUnbound)
            return;
        try {
            JsonObject val = valueAsJSON(node) ;
            currentSolution.put(varName, val) ;
        } catch (JsonException ex) { throw new ResultSetException(ex.getMessage(), ex); }
    }
    
    private JsonObject valueAsJSON(RDFNode node)
    {
        JsonObject jsonValue = new JsonObject() ;
        if (node == null)
        {
            // Unbound
            jsonValue.put(dfType, dfUnbound);
            jsonValue.put(dfValue, JsonNull.instance) ;
            return jsonValue ;
        }
        
        if (node instanceof Literal)
            return literalAsJSON((Literal)node) ;
        
        if (node instanceof Resource)
            return resourceAsJSON((Resource) node);
        
        Log.warn(this, "Unknown RDFNode type in result set: " + node.getClass());
        return jsonValue ;
    }
    
    private JsonObject resourceAsJSON(Resource resource)
    {
        JsonObject jsonValue = new JsonObject() ;
        
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
    
    private JsonObject literalAsJSON(Literal literal)
    {
        JsonObject jsonValue = new JsonObject() ;
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
