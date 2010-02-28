/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import static com.hp.hpl.jena.sparql.resultset.JSONResults.* ;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;
import java.util.NoSuchElementException ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.ResultBinding ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.lib.org.json.JSONArray ;
import com.hp.hpl.jena.sparql.lib.org.json.JSONException ;
import com.hp.hpl.jena.sparql.lib.org.json.JSONObject ;
import com.hp.hpl.jena.sparql.util.LabelToNodeMap ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;
import com.hp.hpl.jena.util.FileUtils ;

/**
 * Code that reads a JSON Result Set and builds the ARQ structure for the same.
 * Originally from Elias Torres &lt;<a href="mailto:elias@torrez.us">elias@torrez.us</a>&gt;
 * 
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
        JSONObject obj = toJSON(in) ;
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

    private static JSONObject toJSON(InputStream in)
    {
        try { 
            String s = FileUtils.readWholeFileAsUTF8(in);
            JSONObject json = new JSONObject(s);
            return json;
        }
        catch (JSONException e) { throw new ResultSetException(e.getMessage(), e); }
        catch (IOException e) { throw new ResultSetException(e.getMessage(), e); }
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

        // JSON
        JSONObject json = null;
        
        JSONResultSet(JSONObject json) {
            this(json, null) ;
        }

        JSONResultSet(JSONObject json, Model model) {
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
            if (json.has(dfResults)) {
                isResultSet = true;
                processResults();
            }

            if (json.has(dfBoolean)) {
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

        private void processBoolean() {
            try {
                askResult = json.getBoolean(dfBoolean);
            } catch (JSONException e) {
                throw new ResultSetException("Unknown boolean value.");
            }
        }

        private void processHead() {
            try {
                // We don't have to a head because we could have boolean results
                if (!json.has(dfHead))
                    return;

                // Get the "head" object
                JSONObject head = json.getJSONObject(dfHead);

                if (head.has(dfVars)) {
                    JSONArray vars = head.getJSONArray(dfVars);

                    for (int i = 0; i < vars.length(); i++) {
                        variables.add(vars.getString(i));
                    }
                }

                if (head.has(dfLink)) {
                    // We're being lazy for now.
                }
            } catch (JSONException e) {
                throw new ResultSetException(e.getMessage(), e) ;
            }
        }

        // -------- Result Set

        private void processResults() {
//            try {
//                JSONObject results = json.getJSONObject(dfResults) ;
//                ordered = results.getBoolean(dfOrdered) ;
//                distinct = results.getBoolean(dfDistinct) ;
//            } catch (JSONException e) {
//                throw new ResultSetException(e.getMessage(), e) ;
//            }
        }

        private Binding getOneSolution() {        
            try {
                                
                JSONObject jresults = json.getJSONObject(dfResults) ;
                JSONArray jbindings = jresults.getJSONArray(dfBindings) ;
                
                if (row < 0 || row >= jbindings.length())
                    return null;
                
                Binding binding = new BindingMap() ;
                JSONObject jsolution = jbindings.getJSONObject(row) ;
                
                for ( Iterator<String> it = jsolution.keys(); it.hasNext() ; ) 
                {
                    String varName = it.next() ;
                    JSONObject jbinding = jsolution.getJSONObject(varName) ;
                    
                    if ( !jbinding.has(dfType) )
                        throw new ResultSetException("Binding is missing 'type'.") ;
                    
                    if ( jbinding.getString(dfType).equals(dfURI) )
                    {
                         String uri = jbinding.getString(dfValue) ;
                         Node node = Node.createURI(uri) ;
                         binding.add(Var.alloc(varName), node) ;   
                    }
                    
                    if ( jbinding.getString(dfType).equals(dfBNode) )
                    {
                        String label = jbinding.getString(dfValue) ;
                        Node node = null ;
                        if ( inputGraphLabels )
                            node = Node.createAnon(new AnonId(label)) ;
                        else
                            node = bNodes.asNode(label) ;
                        binding.add(Var.alloc(varName), node) ;
                    }
                    
                    if ( jbinding.getString(dfType).equals(dfLiteral) ||
                            jbinding.getString(dfType).equals(dfTypedLiteral) )
                    {
                        String lex = jbinding.getString(dfValue) ;
                        String lang = jbinding.optString(dfLang) ;
                        String dtype = jbinding.optString(dfDatatype) ;
                        Node n = NodeFactory.createLiteralNode(lex, lang, dtype) ;
                        binding.add(Var.alloc(varName), n) ;
                    }
                }
                
                return binding ;
                
            } catch( JSONException e) {
                throw new ResultSetException(e.getMessage(), e) ;
            }
        }
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