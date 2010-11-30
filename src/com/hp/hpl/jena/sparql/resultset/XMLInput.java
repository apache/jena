/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import java.io.InputStream ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.util.RefBoolean ;

/** Code that reads an XML Result Set and builds the ARQ structure for the same. */


public class XMLInput
{
    static RefBoolean useSAX = new RefBoolean(ARQ.useSAX) ;
    
    public static ResultSet fromXML(InputStream in) 
    {
        return fromXML(in, null) ;
    }
    
    public static ResultSet fromXML(InputStream in, Model model) 
    {
        return make(in, model).getResultSet() ;
    }
    
    public static ResultSet fromXML(String str) 
    {
        return fromXML(str, null) ;
    }

    public static ResultSet fromXML(String str, Model model) 
    {
        return make(str, model).getResultSet() ;
    }

    public static boolean booleanFromXML(InputStream in)
    {
        return make(in, null).getBooleanResult() ;
    }

    public static boolean booleanFromXML(String str)
    {
        return make(str, null).getBooleanResult() ;
    }

    // Low level operations
    
    public static SPARQLResult make(InputStream in) { return make(in, null) ; }
    
    public static SPARQLResult make(InputStream in, Model model)
    {
        if ( useSAX.getValue() )
            return new XMLInputSAX(in, model) ;
        return new XMLInputStAX(in, model) ;
    }

    public static SPARQLResult make(String str) { return make(str, null) ; }
    
    public static SPARQLResult make(String str, Model model)
    {
        if ( useSAX.getValue() )
            return new XMLInputSAX(str, model) ;
        return new XMLInputStAX(str, model) ;
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