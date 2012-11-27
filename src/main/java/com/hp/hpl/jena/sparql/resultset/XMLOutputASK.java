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

import org.apache.jena.atlas.io.IndentedWriter ;

/** XML Output (ASK format) */


public class XMLOutputASK implements XMLResults
{
    String stylesheetURL = null ;
    IndentedWriter  out ;
    int bNodeCounter = 0 ;
    boolean xmlInst = true ;
    
    public XMLOutputASK(OutputStream outStream)
    { this(outStream, null) ; }
    
    public XMLOutputASK(OutputStream outStream, String stylesheetURL)
    {
        this(new IndentedWriter(outStream), stylesheetURL) ;
    }
    
    public XMLOutputASK(IndentedWriter indentedOut, String stylesheetURL)
    {
        out = indentedOut ;
        this.stylesheetURL = stylesheetURL ;
    }
    
    public void exec(boolean result)
    {
        if ( xmlInst)
            out.println("<?xml version=\"1.0\"?>") ;
        
        if ( stylesheetURL != null )
            out.println("<?xml-stylesheet type=\"text/xsl\" href=\""+stylesheetURL+"\"?>") ;
        
        out.println("<"+dfRootTag+" xmlns=\""+dfNamespace+"\">") ;
        out.incIndent(INDENT) ;
        
        // Head
        out.println("<"+dfHead+">") ;
        out.incIndent(INDENT) ;
        if ( false )
        {
            String link = "UNSET" ;
            out.println("<link href=\""+link+"\"/>") ;
        }
        out.decIndent(INDENT) ;
        out.println("</"+dfHead+">") ;
        
//        // Results
//        out.println("<"+dfResults+">") ;
//        out.incIndent(INDENT) ;
        if ( result )
            out.println("<boolean>true</boolean>") ;
        else
            out.println("<boolean>false</boolean>") ;
//        out.decIndent(INDENT) ;
//        out.println("</"+dfResults+">") ;
        out.decIndent(INDENT) ;
        out.println("</"+dfRootTag+">") ;
        out.flush() ;
    }
}
