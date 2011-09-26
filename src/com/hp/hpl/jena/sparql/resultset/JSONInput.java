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

import static com.hp.hpl.jena.sparql.resultset.JSONResults.dfBNode ;
import static com.hp.hpl.jena.sparql.resultset.JSONResults.dfBindings ;
import static com.hp.hpl.jena.sparql.resultset.JSONResults.dfBoolean ;
import static com.hp.hpl.jena.sparql.resultset.JSONResults.dfDatatype ;
import static com.hp.hpl.jena.sparql.resultset.JSONResults.dfHead ;
import static com.hp.hpl.jena.sparql.resultset.JSONResults.dfLang ;
import static com.hp.hpl.jena.sparql.resultset.JSONResults.dfLink ;
import static com.hp.hpl.jena.sparql.resultset.JSONResults.dfLiteral ;
import static com.hp.hpl.jena.sparql.resultset.JSONResults.dfResults ;
import static com.hp.hpl.jena.sparql.resultset.JSONResults.dfType ;
import static com.hp.hpl.jena.sparql.resultset.JSONResults.dfTypedLiteral ;
import static com.hp.hpl.jena.sparql.resultset.JSONResults.dfURI ;
import static com.hp.hpl.jena.sparql.resultset.JSONResults.dfValue ;
import static com.hp.hpl.jena.sparql.resultset.JSONResults.dfVars ;

import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.NoSuchElementException ;

import org.openjena.atlas.json.JSON ;
import org.openjena.atlas.json.JsonArray ;
import org.openjena.atlas.json.JsonException ;
import org.openjena.atlas.json.JsonObject ;
import org.openjena.atlas.json.JsonValue ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.ResultBinding ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.util.LabelToNodeMap ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;

/**
 * Code that reads a JSON Result Set and builds the ARQ structure for the same.
 * Originally from Elias Torres &lt;<a href="mailto:elias@torrez.us">elias@torrez.us</a>&gt;
 * Updated to not use org.json code : Andy Seaborne (2010)
 */
public class JSONInput extends SPARQLResult
{
    public JSONInput(InputStream in)
    {
        this(in, null) ;
    }

    public JSONInput(InputStream in, Model model)
    {
        if ( model == null )
            model = GraphFactory.makeJenaDefaultModel() ;
        JsonObject obj = toJSON(in) ;
        JSONResultSet r = new JSONResultSet(obj, model) ;
        if ( r.isResultSet() )
            set(r) ;
        else
            set(r.askResult) ;
    }
    
    public static ResultSet fromJSON(InputStream in)
    {
        return fromJSON(in, null) ;
    }

    public static ResultSet fromJSON(InputStream in, Model model)
    {
        JSONInput jin = new JSONInput(in, model) ;
        if(jin.isResultSet() )
            return jin.getResultSet() ;
        
        throw new ResultSetException("Not a result set") ;
    }

    public static boolean booleanFromXML(InputStream in)
    {
        {
            JSONInput jin = new JSONInput(in, null) ;
            if(!jin.isResultSet() )
                return jin.getBooleanResult() ;
            throw new ResultSetException("Not a boolean result") ;
        }
    }
    
    public static SPARQLResult make(InputStream in , Model model)
    {
        return new JSONInput(in, model) ;
    }

    private static JsonObject toJSON(InputStream in)
    {
        try { 
            JsonObject json = JSON.parse(in) ;
            return json;
        }
        catch (JsonException e)
        { throw new ResultSetException(e.getMessage(), e); }
    }
    
    public static class JSONResultSet implements ResultSet
    {
        // ResultSet variables
        QuerySolution current = null;
        List<String> variables = new ArrayList<String>();
        Binding binding = null; // Current binding
        boolean inputGraphLabels = ARQ.isTrue(ARQ.inputGraphBNodeLabels) ;

        LabelToNodeMap bNodes = LabelToNodeMap.createBNodeMap() ;

        // Type
        boolean isResultSet = false;

        // Result set
        boolean ordered = false;
        boolean distinct = false;
        boolean finished = false;

        Model model = null;
        int row = 0;

        // boolean
        boolean askResult = false;

        JsonObject json = null;
        
        JSONResultSet(JsonObject json) {
            this(json, null) ;
        }

        JSONResultSet(JsonObject json, Model model) {
            this.json = json;
            this.model = model;
            init();
        }
        
        public boolean isResultSet()
        {
            return isResultSet ;
        }

        public boolean getBooleanResult()
        {
            if ( isResultSet() )
                throw new ResultSetException("Not a boolean result") ;
            return askResult ;
        }       

        private void init() {
            processHead();

            // Next should be a <result>, <boolean> element or </results>
            // Need to decide what sort of thing we are reading.
            if (json.hasKey(dfResults)) {
                isResultSet = true;
                processResults();
            }

            if (json.hasKey(dfBoolean)) {
                isResultSet = false;
                processBoolean();
            }
        }

        public boolean hasNext() {
            if (!isResultSet)
                throw new ResultSetException("Not an XML result set");

            if (finished)
                return false;
            if (binding == null)
            {
                binding = getOneSolution();
                row++;
            }
            return binding != null;
        }

        public QuerySolution next() {
            return nextSolution();
        }

        public QuerySolution nextSolution()
        {
            return new ResultBinding(model, nextBinding()) ;
        }
        
        public Binding nextBinding() {
            if (finished)
                throw new NoSuchElementException("End of JSON Results");
            if (!hasNext())
                throw new NoSuchElementException("End of JSON Results");

            Binding r = binding;
            binding = null;
            return r ;
        }

        public int getRowNumber() {
            return row;
        }

        public List<String> getResultVars() { return variables; }

        public boolean isOrdered() { return ordered; }

        public boolean isDistinct() { return distinct; }

        // No model - it was from a stream
        public Model getResourceModel() { return null ; }
        
        public void remove() {
            throw new UnsupportedOperationException(JSONResultSet.class
                    .getName());
        }

        // -------- Boolean stuff

        private void processBoolean()
        {
            try {
                askResult = json.get(dfBoolean).getAsBoolean().value();
            } catch (JsonException e) {
                throw new ResultSetException(e.getMessage(), e) ;
            }
        }

        private void processHead() {
            try {
                // We don't have to a head because we could have boolean results
                if (!json.hasKey(dfHead))
                    return;

                // Get the "head" object
                JsonObject head = json.get(dfHead).getAsObject();

                if (head.hasKey(dfVars)) {
                    JsonArray vars = head.get(dfVars).getAsArray();
                    for (int i = 0; i < vars.size(); i++)
                        variables.add(vars.get(i).getAsString().value()) ;
                }

                if (head.hasKey(dfLink)) {
                    // We're being lazy for now.
                }
            } catch (JsonException e) {
                throw new ResultSetException(e.getMessage(), e) ;
            }
        }

        // -------- Result Set

        private void processResults() {
            try {
                JsonObject results = json.get(dfResults).getAsObject() ;
//                ordered = results.getAsBoolean(dfOrdered) ;
//                distinct = results.getBoolean(dfDistinct) ;
            } catch (JsonException e) {
                throw new ResultSetException(e.getMessage(), e) ;
            }
        }

        private Binding getOneSolution() {        
            try {
                                
                JsonObject jresults = json.get(dfResults).getAsObject() ;
                JsonArray jbindings = jresults.get(dfBindings).getAsArray() ;
                
                if (row < 0 || row >= jbindings.size())
                    return null;
                
                BindingMap binding = BindingFactory.create() ;
                JsonObject jsolution = jbindings.get(row).getAsObject() ;
                
                for ( String varName : jsolution.keys() )  
                {
                    JsonObject jbinding = jsolution.get(varName).getAsObject() ;
                    
                    if ( !jbinding.hasKey(dfType) )
                        throw new ResultSetException("Binding is missing 'type'.") ;
                    
                    String type = jbinding.get(dfType).getAsString().getAsString().value() ;
                    
                    if ( type.equals(dfURI) )
                    {
                         String uri = jbinding.get(dfValue).getAsString().value() ;
                         Node node = Node.createURI(uri) ;
                         addBinding(binding, Var.alloc(varName), node) ;   
                    }
                    
                    if ( type.equals(dfBNode) )
                    {
                        String label = jbinding.get(dfValue).getAsString().value() ;
                        Node node = null ;
                        if ( inputGraphLabels )
                            node = Node.createAnon(new AnonId(label)) ;
                        else
                            node = bNodes.asNode(label) ;
                        addBinding(binding, Var.alloc(varName), node) ;
                    }
                    
                    if ( type.equals(dfLiteral) || type.equals(dfTypedLiteral) )
                    {
                        String lex = jbinding.get(dfValue).getAsString().value() ;
                        String lang = null ;
                        String dtype = null ;
                        
                        JsonValue x1 = jbinding.get(dfLang) ;
                        if ( x1 != null )
                            lang = jbinding.get(dfLang).getAsString().value() ;
                        
                        JsonValue x2 = jbinding.get(dfDatatype) ;
                        if ( x2 != null )
                            dtype = jbinding.get(dfDatatype).getAsString().value() ;
                        Node n = NodeFactory.createLiteralNode(lex, lang, dtype) ;
                        addBinding(binding, Var.alloc(varName), n) ;
                    }
                }
                
                return binding ;
                
            } catch( JsonException e) {
                throw new ResultSetException(e.getMessage(), e) ;
            }
        }
    }
}
