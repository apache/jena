/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.sparql.util.TranslationTable;

public class ResultsFormat extends Symbol
{
    // c.f ResultSetFormat.
    /* Select formats - full names */
    
    public final static ResultsFormat FMT_RS_RDF   = new ResultsFormat("rs/graph") ;  /** Format of result set is an RDF graph */
    public final static ResultsFormat FMT_RS_XML   = new ResultsFormat("rs/xml") ;    /** Format of result set as XML */
    public final static ResultsFormat FMT_RS_JSON  = new ResultsFormat("rs/json") ;   /** Format of result set as JSON */
    public final static ResultsFormat FMT_RS_SSE   = new ResultsFormat("rs/sse") ;    /** Format of result set as SSE */ 
    public final static ResultsFormat FMT_RS_CSV   = new ResultsFormat("rs/csv") ;
    public final static ResultsFormat FMT_RS_TSV   = new ResultsFormat("rs/tsv") ;

    public final static ResultsFormat FMT_NONE     = new ResultsFormat("none") ;       /** No output */
    public final static ResultsFormat FMT_TEXT     = new ResultsFormat("rs/text") ;   /** Any kind of text */
    
    public final static ResultsFormat FMT_TUPLES   = new ResultsFormat("tuples") ;     /** Format of results as dump of bindings */ 

    public final static ResultsFormat FMT_COUNT    = new ResultsFormat("count") ;      /** Output the number of rows of a SELECT query */
    
    /* Construct and describe formats as well */ 
    public final static ResultsFormat FMT_RDF_XML  = new ResultsFormat("RDF/XML") ;
    public final static ResultsFormat FMT_RDF_N3   = new ResultsFormat("N3") ;
    public final static ResultsFormat FMT_RDF_TTL  = new ResultsFormat("Turtle") ;
    public final static ResultsFormat FMT_RDF_NT   = new ResultsFormat("N-TRIPLES") ;


    
    public final static ResultsFormat FMT_UNKNOWN   = new ResultsFormat("unknown") ;
    
    static TranslationTable resultFormats =  new TranslationTable(true) ;
    
    static {
        // Mapping of names to symbols.
        // Common names.
        
        resultFormats.put("rs/text",   ResultsFormat.FMT_TEXT ) ;
        resultFormats.put("text" ,     ResultsFormat.FMT_TEXT ) ;
        resultFormats.put("rs/raw",    ResultsFormat.FMT_TEXT ) ;

        resultFormats.put("none" ,     ResultsFormat.FMT_NONE ) ;
        resultFormats.put("count" ,    ResultsFormat.FMT_COUNT ) ;
        
        // result set - XML result
        resultFormats.put("rs/xml",    ResultsFormat.FMT_RS_XML ) ;
        resultFormats.put("rs",        ResultsFormat.FMT_RS_XML ) ;
        resultFormats.put("srx",       ResultsFormat.FMT_RS_XML ) ;
        resultFormats.put("xml" ,      ResultsFormat.FMT_RS_XML ) ;
        
        // result set - JSON tables
        resultFormats.put("rs/json",   ResultsFormat.FMT_RS_JSON ) ;
        resultFormats.put("srj",       ResultsFormat.FMT_RS_JSON ) ;
        resultFormats.put("json",      ResultsFormat.FMT_RS_JSON ) ;

        // result set - SSE tables
        resultFormats.put("rs/sse",    ResultsFormat.FMT_RS_SSE ) ;
        resultFormats.put("sse",       ResultsFormat.FMT_RS_SSE ) ;

        resultFormats.put("rs/csv",    ResultsFormat.FMT_RS_CSV ) ;
        resultFormats.put("csv",       ResultsFormat.FMT_RS_CSV ) ;

        resultFormats.put("rs/tsv",    ResultsFormat.FMT_RS_TSV ) ;
        resultFormats.put("tsv",       ResultsFormat.FMT_RS_TSV ) ;

        
        // Dump!
        resultFormats.put("tuples",    ResultsFormat.FMT_TUPLES ) ;
        resultFormats.put("dump",      ResultsFormat.FMT_TUPLES ) ;
        resultFormats.put("debug",     ResultsFormat.FMT_TUPLES ) ;

        // result set - graph
        resultFormats.put("rs/graph",  ResultsFormat.FMT_RS_RDF ) ;
        resultFormats.put("rs/rdf",    ResultsFormat.FMT_RS_RDF ) ;
        resultFormats.put("graph",     ResultsFormat.FMT_RS_RDF ) ;
        resultFormats.put("rs/n3",     ResultsFormat.FMT_RDF_N3 ) ;

        resultFormats.put("rdf",       ResultsFormat.FMT_RDF_XML ) ;
        resultFormats.put("rdf/xml",   ResultsFormat.FMT_RDF_XML ) ;
        resultFormats.put("n3",        ResultsFormat.FMT_RDF_N3 ) ;
        resultFormats.put("ttl",       ResultsFormat.FMT_RDF_TTL ) ;
        resultFormats.put("n-triples", ResultsFormat.FMT_RDF_NT ) ;
    }
    
    /** Short name to proper name (symbol)
     * 
     */
    public static ResultsFormat lookup(String s)
    {
        return (ResultsFormat)resultFormats.lookup(s) ;
    }
    
    protected ResultsFormat(String s) { super(s) ; }
    protected ResultsFormat(ResultsFormat s) { super(s) ; }

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