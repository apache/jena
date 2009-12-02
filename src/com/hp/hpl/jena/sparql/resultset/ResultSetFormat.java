/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.sparql.util.TranslationTable;

public class ResultSetFormat extends Symbol
{
    public static final ResultSetFormat syntaxXML          = new ResultSetFormat("RS_XML") ;
    
    // general RDF
    public static final ResultSetFormat syntaxRDF_XML      = new ResultSetFormat("RS_RDF") ;
    
    // Specific RDF serialization
    public static final ResultSetFormat syntaxRDF_N3       = new ResultSetFormat("RS_RDF/N3") ;
//    public static final ResultSetFormat syntaxRDF_TTL      = new ResultSetFormat("RS_RDF/TTL") ;
    public static final ResultSetFormat syntaxRDF_TURTLE   = new ResultSetFormat("RS_RDF/TTL") ;
    public static final ResultSetFormat syntaxRDF_NT       = new ResultSetFormat("RS_RDF/NT") ;
    
    public static final ResultSetFormat syntaxText         = new ResultSetFormat("RS_TEXT") ;
    public static final ResultSetFormat syntaxJSON         = new ResultSetFormat("RS_JSON") ;
    
    public static final ResultSetFormat syntaxSSE          = new ResultSetFormat("RS_SSE") ;
    public static final ResultSetFormat syntaxCSV          = new ResultSetFormat("RS_CSV") ;
    public static final ResultSetFormat syntaxTSV          = new ResultSetFormat("RS_TSV") ;

    // Common names to symbol (used by arq.rset)
    protected static TranslationTable syntaxNames = new TranslationTable(true) ;
    static {
        syntaxNames.put("srx",     ResultSetFormat.syntaxXML) ;
        syntaxNames.put("xml",     ResultSetFormat.syntaxXML) ;
        syntaxNames.put("rdf",     ResultSetFormat.syntaxRDF_XML) ; 
        syntaxNames.put("rdf/n3",  ResultSetFormat.syntaxRDF_N3) ;
        syntaxNames.put("rdf/xml", ResultSetFormat.syntaxRDF_XML) ;
        syntaxNames.put("n3",      ResultSetFormat.syntaxRDF_N3) ;
        syntaxNames.put("ttl",     ResultSetFormat.syntaxRDF_TURTLE) ;
        syntaxNames.put("turtle",  ResultSetFormat.syntaxRDF_TURTLE) ;
        syntaxNames.put("text",    ResultSetFormat.syntaxText) ;
        syntaxNames.put("json",    ResultSetFormat.syntaxJSON) ;
        syntaxNames.put("yaml",    ResultSetFormat.syntaxJSON) ;    // The JSON format is a subset of YAML
        syntaxNames.put("sse",     ResultSetFormat.syntaxSSE) ;
        syntaxNames.put("csv",     ResultSetFormat.syntaxCSV) ;
        syntaxNames.put("tsv",     ResultSetFormat.syntaxTSV) ;
    }

    protected ResultSetFormat(String symbol) { super(symbol) ; }
    protected ResultSetFormat(ResultSetFormat fmt) { super(fmt) ; }
    
    
    public static ResultSetFormat guessSyntax(String url) 
    {
        return guessSyntax(url, ResultSetFormat.syntaxXML) ;
    }
    
    public boolean isCompatibleWith(ResultSetFormat other)
    {
        if ( equals(other) )
            return true ;
        if ( other.equals(syntaxRDF_XML) &&
             ( equals(syntaxRDF_N3) || equals(syntaxRDF_TURTLE) || equals(syntaxRDF_NT) ) )
             return true ;
        return false ;
    }
    
    public static ResultSetFormat guessSyntax(String url, ResultSetFormat defaultFormat)
    {
        // -- XML
        if ( url.endsWith(".srx") )
            return ResultSetFormat.syntaxXML ;
        if ( url.endsWith(".xml") )
            return ResultSetFormat.syntaxXML ;
        
        // -- Some kind of RDF
        if ( url.endsWith(".rdf") )
            return ResultSetFormat.syntaxRDF_XML ;
        if ( url.endsWith(".n3") )
            return ResultSetFormat.syntaxRDF_N3 ;
        if ( url.endsWith(".ttl") )
            return ResultSetFormat.syntaxRDF_N3 ;
        
        // -- JSON
        if ( url.endsWith(".srj") )
            return ResultSetFormat.syntaxJSON ;
        if ( url.endsWith(".json") )
            return ResultSetFormat.syntaxJSON ;
        if ( url.endsWith(".yml") )
            return ResultSetFormat.syntaxJSON ;
        
        // -- SSE : http://jena.hpl.hp.com/wiki/SSE
        if ( url.endsWith(".sse") )
            return ResultSetFormat.syntaxSSE ;

        // Likelyto be something completely different!
        if ( url.endsWith(".csv") )
            return ResultSetFormat.syntaxCSV ;
        if ( url.endsWith(".tsv") )
            return ResultSetFormat.syntaxTSV ;
        
        return defaultFormat ;
    }
    
    
    /** Look up a short name for a result set syntax
     * 
     * @param s  Short name
     * @return  ResultSetFormat
     */
 
    public static ResultSetFormat lookup(String s)
    {
        return (ResultSetFormat)syntaxNames.lookup(s) ;
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