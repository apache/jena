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

import static com.hp.hpl.jena.sparql.resultset.JSONResultsKW.* ;

import java.io.OutputStream ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.json.io.JSWriter ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;

/**
 * A JSON writer for SPARQL Result Sets.  Uses Jena Atlas JSON support. 
 * 
 * Format: <a href="http://www.w3.org/TR/sparql11-results-json/">SPARQL 1.1 Query Results JSON Format</a> 
 */ 

public class JSONOutputResultSet implements ResultSetProcessor
{
    static boolean multiLineValues = false ;
    static boolean multiLineVarNames = false ;
    
    private boolean outputGraphBNodeLabels = false ;
    private IndentedWriter out ;
    private int bNodeCounter = 0 ;
    private Map<Resource, String> bNodeMap = new HashMap<>() ;
    
    JSONOutputResultSet(OutputStream outStream)
    { this(new IndentedWriter(outStream)) ; }
    
    JSONOutputResultSet(IndentedWriter indentedOut)
    {   out = indentedOut ;
        outputGraphBNodeLabels = ARQ.isTrue(ARQ.outputGraphBNodeLabels) ;
    }
    
    @Override
    public void start(ResultSet rs)
    {
        out.println("{") ;
        out.incIndent() ;
        doHead(rs) ;
        out.println(quoteName(kResults)+": {") ;
        out.incIndent() ;
        out.println(quoteName(kBindings)+": [") ;
        out.incIndent() ;
        firstSolution = true ;
    }

    @Override
    public void finish(ResultSet rs)
    {
        // Close last binding.
        out.println() ;
        
        out.decIndent() ;       // bindings
        out.println("]") ;
        out.decIndent() ;
        out.println("}") ;      // results
        out.decIndent() ;
        out.println("}") ;      // top level {}
        out.flush() ;
    }

    private void doHead(ResultSet rs)
    {
        out.println(quoteName(kHead)+": {") ;
        out.incIndent() ;
        doLink(rs) ;
        doVars(rs) ;
        out.decIndent() ;
        out.println("} ,") ;
    }
    
    private void doLink(ResultSet rs)
    {
        // ---- link
        //out.println("\"link\": []") ;
    }
    
    private void doVars(ResultSet rs)
    {
        // On one line.
        out.print(quoteName(kVars)+": [ ") ;
        if ( multiLineVarNames ) out.println() ;
        out.incIndent() ;
        for (Iterator<String> iter = rs.getResultVars().iterator() ; iter.hasNext() ; )
        {
            String varname = iter.next() ;
            out.print("\""+varname+"\"") ;
            if ( multiLineVarNames ) out.println() ;
            if ( iter.hasNext() )
                out.print(" , ") ;
        }
        out.println(" ]") ;
        out.decIndent() ;
    }

    boolean firstSolution = true ;
    boolean firstBindingInSolution = true ;
    
    // NB assumes are on end of previous line.
    @Override
    public void start(QuerySolution qs)
    {
        if ( ! firstSolution )
            out.println(" ,") ;
        firstSolution = false ;
        out.println("{") ;
        out.incIndent() ;
        firstBindingInSolution = true ;
    }

    @Override
    public void finish(QuerySolution qs)
    {
        out.println() ;     // Finish last binding
        out.decIndent() ;
        out.print("}") ;    // NB No newline
    }

    @Override
    public void binding(String varName, RDFNode value)
    {
        if ( value == null )
            return ;
        
        if ( !firstBindingInSolution )
            out.println(" ,") ;
        firstBindingInSolution = false ;

        // Do not use quoteName - varName may not be JSON-safe as a bare name.
        out.print(quote(varName)+": { ") ;
        if ( multiLineValues ) out.println() ;
        
        out.incIndent() ;
        // Old, explicit unbound
//        if ( value == null )
//            printUnbound() ;
//        else
      	if ( value.isLiteral() )
            printLiteral((Literal)value) ;
        else if ( value.isResource() )
            printResource((Resource)value) ;
        else 
            Log.warn(this, "Unknown RDFNode type in result set: "+value.getClass()) ;
        out.decIndent() ;
        
        if ( !multiLineValues ) out.print(" ") ; 
        out.print("}") ;        // NB No newline
    }
    
//    private void printUnbound()
//    {
//        out.print(quoteName(kType)+ ": "+quote(kUnbound)+" , ") ;
//        if ( multiLineValues ) out.println() ;
//        out.print(quoteName(kValue)+": null") ;
//        if ( multiLineValues ) out.println() ;
//    }

    private void printLiteral(Literal literal)
    {
        String datatype = literal.getDatatypeURI() ;
        String lang = literal.getLanguage() ;
        
        if ( datatype != null )
        {
            out.print(quoteName(kDatatype)+": "+quote(datatype)+" , ") ;
            if ( multiLineValues ) out.println() ;
            
            out.print(quoteName(kType)+": "+quote(kTypedLiteral)+" , ") ;
            if ( multiLineValues ) out.println() ;
        }
        else
        {
            out.print(quoteName(kType)+": "+quote(kLiteral)+" , ") ;
            if ( multiLineValues ) out.println() ;
            
            if ( lang != null && !lang.equals("") )
            {
                out.print(quoteName(kXmlLang)+": "+quote(lang)+" , ") ;
                if ( multiLineValues ) out.println() ;
            }
        }
            
        out.print(quoteName(kValue)+": "+quote(literal.getLexicalForm())) ;
        if ( multiLineValues ) out.println() ;
    }

    private void printResource(Resource resource)
    {
        if ( resource.isAnon() )
        {
            String label ; 
            if ( outputGraphBNodeLabels )
                label = resource.getId().getLabelString() ;
            else
            {
                if ( ! bNodeMap.containsKey(resource))
                    bNodeMap.put(resource, "b"+(bNodeCounter++)) ;
                label = bNodeMap.get(resource) ;
            }
            
            out.print(quoteName(kType)+": "+quote(kBnode)+" , ") ;
            if ( multiLineValues ) out.println() ;
            
            out.print(quoteName(kValue)+": "+quote(label)) ;
            
            if ( multiLineValues ) out.println() ;
        }
        else
        {
            out.print(quoteName(kType)+": "+quote(kUri)+" , ") ;
            if ( multiLineValues ) out.println() ;
            out.print(quoteName(kValue)+": "+quote(resource.getURI())) ;
            if ( multiLineValues ) out.println() ;
            return ;
        }
    }
    
    private static String quote(String string)
    {
        return JSWriter.outputQuotedString(string) ;
    }
    
    // Quote a name (known to be JSON-safe)
    // Never the RHS of a member entry (for example "false")
    // Some (the Java JSON code for one) JSON parsers accept an unquoted
    // string as a name of a name/value pair.
    
    private static String quoteName(String string)
    {
        // Safest to quote anyway.
        return quote(string) ;
        
        // Assumes only called with safe names
        //return string ;
        
        // Better would be:
        // starts a-z, constains a-z,0-9, not a keyword(true, false, null)
//        if ( string.contains(something not in a-z0-9)
//        and         
//        //return "\""+string+"\"" ;
//        return JSONObject.quote(string) ;
    }

}
