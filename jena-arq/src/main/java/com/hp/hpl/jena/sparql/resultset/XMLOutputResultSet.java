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

import java.io.OutputStream ;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;

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
    Map<Resource, String> bNodeMap = new HashMap<>() ;
    
    XMLOutputResultSet(OutputStream outStream)
    {
        this(new IndentedWriter(outStream)) ;
    }
    
    XMLOutputResultSet(IndentedWriter indentedOut)
    {
        out = indentedOut ;
    }
    
    @Override
    public void start(ResultSet rs)
    {
        if ( xmlInst )
            out.println("<?xml version=\"1.0\"?>") ;
        
        if ( stylesheetURL != null )
        {
            out.print("<?xml-stylesheet type=\"text/xsl\" href=\"") ;
            out.print(stylesheetURL) ;
            out.println("\"?>") ;
        }
        
        // ---- Root
        out.print("<") ;
        out.print(dfRootTag) ;
        out.print(" xmlns=\"") ;
        out.print(dfNamespace) ;
        out.println("\">") ;

        // ---- Header

        out.incIndent(INDENT) ;
        out.print("<") ;
        out.print(dfHead) ;
        out.println(">") ;
        
        if ( false )
        {
            String link = "UNSET" ;
            out.print("<link href=\"") ;
            out.print(link) ;
            out.println("\"/>") ;
        }
        
        for (String n : rs.getResultVars())
        {
            out.incIndent(INDENT) ;
            out.print("<") ;
            out.print(dfVariable) ;
            out.print(" ") ;
            out.print(dfAttrVarName) ;
            out.print("=\"") ;
            out.print(n) ;
            out.print("\"") ;
            out.println("/>") ;
            out.decIndent(INDENT) ;
        }
        out.print("</") ;
        out.print(dfHead) ;
        out.println(">") ;
        out.decIndent(INDENT) ;
        
        // Start results proper
        out.incIndent(INDENT) ;
        out.print("<") ;
        out.print(dfResults) ;
        out.println(">") ;
        out.incIndent(INDENT) ;
    }

    @Override
    public void finish(ResultSet rs)
    {
        out.decIndent(INDENT) ;
        out.print("</") ;
        out.print(dfResults) ;
        out.println(">") ;
        out.decIndent(INDENT) ;
        out.print("</") ;
        out.print(dfRootTag) ;
        out.println(">") ;
        out.flush() ;
    }

    @Override
    public void start(QuerySolution qs)
    {
        out.print("<") ;
        out.print(dfSolution) ;
        out.println(">") ;
        index ++ ;
        out.incIndent(INDENT) ;
    }

    @Override
    public void finish(QuerySolution qs)
    {
        out.decIndent(INDENT) ;
        out.print("</") ;
        out.print(dfSolution) ;
        out.println(">") ;
    }

    @Override
    public void binding(String varName, RDFNode node)
    {
        if ( node == null && ! outputExplicitUnbound )
            return ;
        
        out.print("<") ; 
        out.print(dfBinding) ;
        out.print(" name=\"") ;
        out.print(varName) ;
        out.println("\">") ;
        out.incIndent(INDENT) ;
        printBindingValue(node) ;
        out.decIndent(INDENT) ;
        out.print("</") ;
        out.print(dfBinding) ;
        out.println(">") ;
    }
        
    void printBindingValue(RDFNode node)
    {
        if ( node == null )
        {
            // Unbound
            out.print("<") ;
            out.print(dfUnbound) ;
            out.println("/>") ;
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
        
        out.print("<") ;
        out.print(dfLiteral) ;
        
        if ( lang != null && !(lang.length()==0) )
        {
            out.print(" xml:lang=\"") ;
            out.print(lang) ;
            out.print("\"") ;
        }
            
        if ( datatype != null && ! datatype.equals(""))
        {
//            if ( datatype.startsWith(xsBaseURI) )
//            {
//                String r = datatype.substring(xsBaseURI.length()) ;
//                out.print(" xsi:type=\"xsi:"+r+"\"") ;
//            }
            out.print(" ") ;
            out.print(dfAttrDatatype) ;
            out.print("=\"") ;
            out.print(datatype) ;
            out.print("\"") ;
        }
            
        out.print(">") ;
        out.print(xml_escape(literal.getLexicalForm())) ;
        out.print("</") ;
        out.print(dfLiteral) ;
        out.println(">") ;
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
            out.print("<") ;
            out.print(dfBNode) ;
            out.print(">") ;
            out.print(label) ;
            out.print("</") ;
            out.print(dfBNode) ;
            out.println(">") ;
        }
        else
        {
            out.print("<") ;
            out.print(dfURI) ;
            out.print(">") ;
            out.print(xml_escape(r.getURI())) ;
            out.print("</") ;
            out.print(dfURI) ;
            out.println(">") ;
        }
    }
    
    private static String xml_escape(String string)
    {
        final StringBuilder sb = new StringBuilder(string);
        
        int offset = 0;
        String replacement;
        char found;
        for (int i = 0; i < string.length(); i++) {
            found = string.charAt(i);
            
            switch (found) {
                case '&' : replacement = "&amp;"; break;
                case '<' : replacement = "&lt;"; break;
                case '>' : replacement = "&gt;"; break;
                case '\r': replacement = "&#x0D;"; break;
                case '\n': replacement = "&#x0A;"; break;
                default  : replacement = null;
            }
            
            if (replacement != null) {
                sb.replace(offset + i, offset + i + 1, replacement);
                offset += replacement.length() - 1; // account for added chars
            }
        }
        
        return sb.toString();
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
