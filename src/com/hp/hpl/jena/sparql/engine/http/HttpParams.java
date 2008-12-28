/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.http;

/** Constants related to SPARQL over HTTP  
 * 
 * @author Andy Seaborne
 */

public class HttpParams
{
    /** Parameter for the SPARQL query string */
    public static final String pQuery               = "query" ;
    
//    /** Parameter for a URI pointing to a SPARQL query in a document */
//    public static final String pQueryUri       = "query-uri" ;
    
    /** Parameter for a URI identifying the graph (or one of the graphs) */
    public static final String pDefaultGraph        = "default-graph-uri" ;
    
    /** Parameter for a URI identifying the graph (or one of the graphs) */
    public static final String pNamedGraph          = "named-graph-uri" ;
    
    // ----------------- Non-SPARQL parameters

    public static final String pStylesheet      = "stylesheet" ;

    /** Parameter for query language URI */
    public static final String pQueryLang      = "lang" ;

    // -------- Constants
    
    public static final String contentTypeAppN3        = "application/n3" ;
    public static final String contentTypeTurtle       = "application/turtle" ;
    public static final String contentTypeRDFXML       = "application/rdf+xml" ;
    public static final String contentTypeNTriples     = "application/n-triples" ;
    public static final String contentTypeXML          = "application/xml" ;
    public static final String contentTypeResultsXML   = "application/sparql-results+xml" ;

    public static final String contentTypeTextPlain    = "text/plain" ;
    public static final String contentTypeTextN3       = "text/n3" ;
    public static final String contentTypeForText      = contentTypeTextPlain ;
    
    public static final String charsetUTF8             = "utf-8" ;

    
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