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

package com.hp.hpl.jena.sparql.resultset;

import static com.hp.hpl.jena.sparql.resultset.JSONResultsKW.kBindings ;
import static com.hp.hpl.jena.sparql.resultset.JSONResultsKW.kBnode ;
import static com.hp.hpl.jena.sparql.resultset.JSONResultsKW.kBoolean ;
import static com.hp.hpl.jena.sparql.resultset.JSONResultsKW.kDatatype ;
import static com.hp.hpl.jena.sparql.resultset.JSONResultsKW.kHead ;
import static com.hp.hpl.jena.sparql.resultset.JSONResultsKW.kLink ;
import static com.hp.hpl.jena.sparql.resultset.JSONResultsKW.kLiteral ;
import static com.hp.hpl.jena.sparql.resultset.JSONResultsKW.kResults ;
import static com.hp.hpl.jena.sparql.resultset.JSONResultsKW.kType ;
import static com.hp.hpl.jena.sparql.resultset.JSONResultsKW.kTypedLiteral ;
import static com.hp.hpl.jena.sparql.resultset.JSONResultsKW.kUri ;
import static com.hp.hpl.jena.sparql.resultset.JSONResultsKW.kValue ;
import static com.hp.hpl.jena.sparql.resultset.JSONResultsKW.kVars ;
import static com.hp.hpl.jena.sparql.resultset.JSONResultsKW.kXmlLang ;

import java.io.InputStream ;
import java.util.* ;

import org.openjena.atlas.json.JSON ;
import org.openjena.atlas.json.JsonArray ;
import org.openjena.atlas.json.JsonObject ;
import org.openjena.atlas.json.JsonValue ;
import org.openjena.atlas.logging.Log ;
import org.openjena.riot.lang.LabelToNode ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;

public class JSONInput extends SPARQLResult
{
    public static ResultSet fromJSON(InputStream input)
    {
        SPARQLResult r = new JSONInput().process(input, null) ;
        return r.getResultSet() ; 
    }
    
    public static boolean booleanFromJSON(InputStream input)
    {
        SPARQLResult r = new JSONInput().process(input, null) ;
        return r.getBooleanResult() ; 
    }
    
    public static SPARQLResult make(InputStream input)
    {
        return make(input, null) ;
    }
    
    public static SPARQLResult make(InputStream input, Model model)
    {
        return new JSONInput().process(input, model) ;
    }

    public JSONInput() {}
    
    public JSONInput(InputStream in)
    {
        this(in, null) ;
    }

    // See also XMLInputSAX for design structure.
    public JSONInput(InputStream in, Model model)
    {
        if ( model == null )
            model = GraphFactory.makeJenaDefaultModel() ;
        process(in, model) ;
    }
    
    Boolean booleanResult = null ;        // Valid if rows is null. 
    List<Binding> rows = null ;
    List<Var> vars = null ;

    // TODO Streaming version of JSON Result set processing
    
    private SPARQLResult process(InputStream in, Model model)
    {
        parse(in) ;
        if ( model == null )
            model = GraphFactory.makeJenaDefaultModel() ;
        if ( rows != null )
        {
            QueryIterator qIter = new QueryIterPlainWrapper(rows.iterator()) ;
            ResultSet rs = new ResultSetStream(Var.varNames(vars), model, qIter) ;
            super.set(rs) ;
        }
        else
            super.set(booleanResult) ;
        return this ;
    }
    
    private void parse(InputStream in)
    {
        JsonObject obj = JSON.parse(in) ;
        
        if ( obj.hasKey(kBoolean) )
        {
            checkContains(obj, kHead, kBoolean) ;
            // TODO check kHead is empty.
            booleanResult = obj.get(kBoolean).getAsBoolean().value() ;
            rows = null ;
            return ;
        }
        
        rows = new ArrayList<Binding>(1000) ;
        
        checkContains(obj, kHead, kResults) ;

        if ( ! obj.hasKey(kHead) )    throw new ResultSetException("No 'head' for results") ;
        if ( ! obj.hasKey(kResults) ) throw new ResultSetException("No 'results' for results") ;
        
        // process head
        if ( ! obj.get(kHead).isObject() )
            throw new ResultSetException("Key 'head' must have a JSON object as value: found: "+obj.get(kHead)) ;
        JsonObject head = obj.get(kHead).getAsObject() ;
        
        // ---- Head
        // -- Link - array.
        if ( head.hasKey(kLink) )
        {
            List<String> links = new ArrayList<String>() ; 

            if ( head.get(kLink).isString() )
            {
                Log.warn(this, "Link field is a string, should be an array of strings") ;
                links.add(head.get(kLink).getAsString().value()) ;
            }
            else
            {
                if ( ! head.get(kLink).isArray() )
                    throw new ResultSetException("Key 'link' must have be an array: found: "+obj.get(kLink)) ;

                for ( JsonValue v : head.get(kLink).getAsArray() )
                {
                    if ( ! v.isString() )
                        throw new ResultSetException("Key 'link' must have be an array of strings: found: "+v) ;
                    links.add(v.getAsString().value()) ;
                }
            }
        }
        // -- Vars
        vars = parseVars(head) ;

        // ---- Results
        JsonObject results = obj.get(kResults).getAsObject() ;
        if ( ! results.get(kBindings).isArray() )
            throw new ResultSetException("'bindings' must be an array") ;
        JsonArray array = results.get(kBindings).getAsArray() ;
        Iterator<JsonValue> iter = array.iterator() ;
        
        for ( ; iter.hasNext() ; )
        {
            BindingMap b = BindingFactory.create() ;
            JsonValue v = iter.next() ;
            if ( ! v.isObject() )
                throw new ResultSetException("Entry in 'bindings' array must be an object {}")  ;
            JsonObject x = v.getAsObject() ;
            Set<String> varNames = x.keys() ;
            for ( String vn : varNames )
            {
                if ( ! vars.contains(vn) )
                    ; // Warning
                JsonValue vt = x.get(vn) ;
                if ( ! vt.isObject() )
                    throw new ResultSetException("Binding for variable '"+vn+"' is not a JSON object: "+vt)  ;
                Node n = parseOneTerm(vt.getAsObject()) ;
                b.add(Var.alloc(vn), n) ;
            }
            rows.add(b) ;
        }
    }
    
    private List<Var> parseVars(JsonObject obj)
    {
        if ( ! obj.get(kVars).isArray() )
            throw new ResultSetException("Key 'vars' must be a JSON array") ;
        JsonArray a = obj.get(kVars).getAsArray() ;
        Iterator<JsonValue> iter = a.iterator() ;
        List<Var> vars = new ArrayList<Var>() ;
        for ( ; iter.hasNext() ; )
        {
            JsonValue v = iter.next() ;
            if ( ! v.isString() )
                throw new ResultSetException("Entries in vars array must be strings") ;
            Var var = Var.alloc(v.getAsString().value()) ;
            vars.add(var) ;
        }
        return vars ;
    }

    LabelToNode labelMap = LabelToNode.createScopeByDocument() ;
    private Node parseOneTerm(JsonObject term)
    {
        checkContains(term, kType, kValue, kXmlLang, kDatatype) ;
        
        String type = stringOrNull(term, kType) ;
        String v = stringOrNull(term, kValue) ;
        
        if ( kUri.equals(type) )
        {
            checkContains(term, kType, kValue) ;
            String uri = v ;
            Node n = Node.createURI(v) ;
            return n ;
        }
        
        if ( kLiteral.equals(type) || kTypedLiteral.equals(type) )
        {
            String lang = stringOrNull(term, kXmlLang) ;
            String dtStr = stringOrNull(term, kDatatype) ;
            if ( lang != null && dtStr != null )
                throw new ResultSetException("Both language and datatype defined: "+term) ;
            RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(dtStr) ;
            return Node.createLiteral(v, lang, dt)  ;
        }
        
        if ( kBnode.equals(type) )
            return labelMap.get(null, v) ;
        
        throw new ResultSetException("Object key not recognized as valid for an RDF term: "+term) ;
    }
    
    private static String stringOrNull(JsonObject obj, String key)
    {
        JsonValue v = obj.get(key) ;
        if ( v == null ) return null ;
        if ( ! v.isString() )
            throw new ResultSetException("Not a string: key: "+key) ;
        return v.getAsString().value();
        
    }
    
    private static void checkContains(JsonObject term, String...keys)
    {
        List<String> x = Arrays.asList(keys) ;
        for ( String k : term.keys() )
        {
            if ( !x.contains(k) )
                throw new ResultSetException("Expected only object keys "+Arrays.asList(keys)+" but encountered '"+k+"'") ; 
        }
    }
}

