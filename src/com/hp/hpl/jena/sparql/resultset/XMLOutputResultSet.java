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

import java.io.OutputStream ;
import java.util.HashMap ;
import java.util.Map ;

import org.openjena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import org.openjena.atlas.logging.Log ;

/** XML Output (ResultSet format) */


public class XMLOutputResultSet
    implements ResultSetProcessor, XMLResults
{
    static boolean outputExplicitUnbound = false ;
    
    boolean outputGraphBNodeLabels = ARQ.isTrue(ARQ.outputGraphBNodeLabels) ;

    int index = 0 ;                     // First index is 1 
    String stylesheetURL = null ;
    boolean xmlInst = true ;

    IndentedWriter  out ;
    int bNodeCounter = 0 ;
    Map<Resource, String> bNodeMap = new HashMap<Resource, String>() ;
    
    XMLOutputResultSet(OutputStream outStream)
    {
        this(new IndentedWriter(outStream)) ;
    }
    
    XMLOutputResultSet(IndentedWriter indentedOut)
    {
        out = indentedOut ;
    }
    
    public void start(ResultSet rs)
    {
        if ( xmlInst )
            out.println("<?xml version=\"1.0\"?>") ;
        
        if ( stylesheetURL != null )
            out.println("<?xml-stylesheet type=\"text/xsl\" href=\""+stylesheetURL+"\"?>") ;
        
        // ---- Root
        out.print("<"+dfRootTag) ;
        out.print(" ") ;
        out.println("xmlns=\""+dfNamespace+"\">") ;

        // Remove this next you see it.
//       out.incIndent(INDENT) ;
//       out.incIndent(INDENT) ;
//       out.println("xmlns:rdf=\""+ARQConstants.rdfPrefix+"\"") ;
//       out.println("xmlns:xs=\""+ARQConstants.XML_SCHEMA_NS+"\"") ;
//       out.println("xmlns=\""+dfNamespace+"\" >") ;
//       out.decIndent(INDENT) ;
//       out.decIndent(INDENT) ;
        // ---- Header

        out.incIndent(INDENT) ;
        out.println("<"+dfHead+">") ;
        
        if ( false )
        {
            String link = "UNSET" ;
            out.println("<link href=\""+link+"\"/>") ;
        }
        
        for (String n : rs.getResultVars())
        {
            out.incIndent(INDENT) ;
            out.print("<") ;
            out.print(dfVariable) ;
            out.print(" "+dfAttrVarName+"=\""+n+"\"") ;
            out.println("/>") ;
            out.decIndent(INDENT) ;
        }
        out.println("</"+dfHead+">") ;
        out.decIndent(INDENT) ;
        
        // Start results proper
        out.incIndent(INDENT) ;
        out.println("<"+dfResults+">") ;
        out.incIndent(INDENT) ;
    }

    public void finish(ResultSet rs)
    {
        out.decIndent(INDENT) ;
        out.println("</"+dfResults+">") ;
        out.decIndent(INDENT) ;
        out.println("</"+dfRootTag+">") ;
        out.flush() ;
    }

    public void start(QuerySolution qs)
    {
        out.println("<"+dfSolution+">") ;
        index ++ ;
        out.incIndent(INDENT) ;
    }

    public void finish(QuerySolution qs)
    {
        out.decIndent(INDENT) ;
        out.println("</"+dfSolution+">") ;
    }

    public void binding(String varName, RDFNode node)
    {
        if ( node == null && ! outputExplicitUnbound )
            return ;
        
        out.print("<") ; 
        out.print(dfBinding) ;
        out.println(" name=\""+varName+"\">") ;
        out.incIndent(INDENT) ;
        printBindingValue(node) ;
        out.decIndent(INDENT) ;
        out.println("</"+dfBinding+">") ;
    }
        
    void printBindingValue(RDFNode node)
    {
        if ( node == null )
        {
            // Unbound
            out.println("<"+dfUnbound+"/>") ;
            return ;
        }
        
        if ( node instanceof Literal )
        {
            printLiteral((Literal)node) ;
            return ;
        }
        
        if ( node instanceof Resource )
        {
            printResource((Resource)node) ;
            return ;
        }
        
        Log.warn(this,"Unknown RDFNode type in result set: "+node.getClass()) ;
    }
    
    void printLiteral(Literal literal)
    {
        String datatype = literal.getDatatypeURI() ;
        String lang = literal.getLanguage() ;
        
        out.print("<"+dfLiteral) ;
        
        if ( lang != null && !(lang.length()==0) )
            out.print(" xml:lang=\""+lang+"\"") ;
            
        if ( datatype != null && ! datatype.equals(""))
        {
//            if ( datatype.startsWith(xsBaseURI) )
//            {
//                String r = datatype.substring(xsBaseURI.length()) ;
//                out.print(" xsi:type=\"xsi:"+r+"\"") ;
//            }
            out.print(" "+dfAttrDatatype+"=\""+datatype+"\"") ;
        }
            
        out.print(">") ;
        out.print(xml_escape(literal.getLexicalForm())) ;
        out.println("</"+dfLiteral+">") ;
    }
    
    void printResource(Resource r)
    {
        if ( r.isAnon() ) 
        {
            String label ;
            
            if ( outputGraphBNodeLabels )
                label = r.asNode().getBlankNodeId().getLabelString() ;
            else
            {
                if ( ! bNodeMap.containsKey(r))
                    bNodeMap.put(r, "b"+(bNodeCounter++)) ;
                label = bNodeMap.get(r) ;
            }
            out.println("<"+dfBNode+">"+label+"</"+dfBNode+">") ;
        }
        else
        {
            out.println("<"+dfURI+">"+xml_escape(r.getURI())+"</"+dfURI+">") ;
        }
    }

    private static String xml_escape(String string)
    {
        String s = string ;
        s = s.replaceAll("&", "&amp;") ;
        s = s.replaceAll("<", "&lt;") ;
        s = s.replaceAll(">", "&gt;") ;
        s = s.replaceAll("\r", "&#x0D;") ;
        s = s.replaceAll("\n", "&#x0A;") ;  // Safe - excessively safe
        return s ;
    }

    /** @return Returns the stylesheetURL. */
    public String getStylesheetURL()
    { return stylesheetURL ; }

    /** @param stylesheetURL The stylesheetURL to set. */
    public void setStylesheetURL(String stylesheetURL)
    { this.stylesheetURL = stylesheetURL ; }

    /** @return Returns the xmlInst. */
    public boolean getXmlInst()
    { return xmlInst ; }

    /** @param xmlInst The xmlInst to set. */
    public void setXmlInst(boolean xmlInst)
    { this.xmlInst = xmlInst ; }
}
