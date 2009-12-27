/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.sparql.lib.org.json.*;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import static com.hp.hpl.jena.sparql.resultset.JSONResults.* ;
/**
 * A JSON writer for SPARQL Result Sets
 * 
 * Format: <a href="http://www.w3.org/2001/sw/DataAccess/json-sparql/">Serializing SPARQL Query Results in JSON</a> 
 * 
 * JSON: <a href="http://json.org">http://json.org/</a>
 * 
 * @author Andy Seaborne
 */

public class JSONOutputResultSet implements ResultSetProcessor
{
    static boolean multiLineValues = false ;
    static boolean multiLineVarNames = false ;
    
    private boolean outputGraphBNodeLabels = false ;
    private IndentedWriter out ;
    private int bNodeCounter = 0 ;
    private Map<Resource, String> bNodeMap = new HashMap<Resource, String>() ;
    
    JSONOutputResultSet(OutputStream outStream)
    { this(new IndentedWriter(outStream)) ; }
    
    JSONOutputResultSet(IndentedWriter indentedOut)
    {   out = indentedOut ;
        outputGraphBNodeLabels = ARQ.isTrue(ARQ.outputGraphBNodeLabels) ;
    }
    
    public void start(ResultSet rs)
    {
        out.println("{") ;
        out.incIndent() ;
        doHead(rs) ;
        out.println(quoteName(dfResults)+": {") ;
        out.incIndent() ;
//        out.println(quoteName(dfDistinct)+": "+(rs.isDistinct()?"true":"false")+" ,") ;
//        out.println(quoteName(dfOrdered)+": " +(rs.isOrdered() ?"true":"false")+" ,") ;
        out.println(quoteName(dfBindings)+": [") ;
        out.incIndent() ;
        firstSolution = true ;
    }

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
        out.println(quoteName(dfHead)+": {") ;
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
        out.print(quoteName(dfVars)+": [ ") ;
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
    public void start(QuerySolution qs)
    {
        if ( ! firstSolution )
            out.println(" ,") ;
        firstSolution = false ;
        out.println("{") ;
        out.incIndent() ;
        firstBindingInSolution = true ;
    }

    public void finish(QuerySolution qs)
    {
        out.println() ;     // Finish last binding
        out.decIndent() ;
        out.print("}") ;    // NB No newline
    }

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
            ALog.warn(this, "Unknown RDFNode type in result set: "+value.getClass()) ;
        out.decIndent() ;
        
        if ( !multiLineValues ) out.print(" ") ; 
        out.print("}") ;        // NB No newline
    }
    
    private void printUnbound()
    {
        out.print(quoteName(dfType)+ ": "+quote(dfUnbound)+" , ") ;
        if ( multiLineValues ) out.println() ;
        out.print(quoteName(dfValue)+": null") ;
        if ( multiLineValues ) out.println() ;
    }

    private void printLiteral(Literal literal)
    {
        String datatype = literal.getDatatypeURI() ;
        String lang = literal.getLanguage() ;
        
        if ( datatype != null )
        {
            out.print(quoteName(dfDatatype)+": "+quote(datatype)+" , ") ;
            if ( multiLineValues ) out.println() ;
            
            out.print(quoteName(dfType)+": "+quote(dfTypedLiteral)+" , ") ;
            if ( multiLineValues ) out.println() ;
        }
        else
        {
            out.print(quoteName(dfType)+": "+quote(dfLiteral)+" , ") ;
            if ( multiLineValues ) out.println() ;
            
            if ( lang != null && !lang.equals("") )
            {
                out.print(quoteName(dfLang)+": "+quote(lang)+" , ") ;
                if ( multiLineValues ) out.println() ;
            }
        }
            
        out.print(quoteName(dfValue)+": "+quote(literal.getLexicalForm())) ;
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
            
            out.print(quoteName(dfType)+": "+quote(dfBNode)+" , ") ;
            if ( multiLineValues ) out.println() ;
            
            out.print(quoteName(dfValue)+": "+quote(label)) ;
            
            if ( multiLineValues ) out.println() ;
        }
        else
        {
            out.print(quoteName(dfType)+": "+quote(dfURI)+" , ") ;
            if ( multiLineValues ) out.println() ;
            out.print(quoteName(dfValue)+": "+quote(resource.getURI())) ;
            if ( multiLineValues ) out.println() ;
            return ;
        }
    }
    
    private static String quote(String string)
    {
        return JSONObject.quote(string) ;
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