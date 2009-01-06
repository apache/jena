/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import java.io.OutputStream;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

/** XML Output (ASK format)
 * 
 * @author Andy Seaborne
 */


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
/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
