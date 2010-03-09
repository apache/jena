/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;


public class WebContent
{
    // Names for things.
    
    public static final String contentTypeN3              = "text/rdf+n3" ;
    public static final String contentTypeN3Alt1          = "application/n3" ;
    public static final String contentTypeN3Alt2          = "text/n3" ;

    public static final String contentTypeTurtle1         = "application/turtle" ;      // Swapped in Joseki. 
    public static final String contentTypeTurtle2         = "text/turtle" ; 
    public static final String contentTypeTurtle3         = "application/x-turtle" ;
    
    public static final String contentTypeRDFXML          = "application/rdf+xml" ;
    
    public static final String contentTypeTextPlain       = "text/plain" ;
    // MIME type for N-triple is text/plain (!!!)
    public static final String contentTypeNTriples        = contentTypeTextPlain ;
    public static final String contentTypeNTriplesAlt     = "application/n-triples" ;
    
    public static final String contentTypeXML             = "application/xml" ;
    
    public static final String contentTypeResultsXML      = "application/sparql-results+xml" ;
    public static final String contentTypeResultsJSON     = "application/sparql-results+json" ;

    public static final String contentTypeTriG            = "application/x-trig" ;
    public static final String contentTypeNQuads          = "text/x-nquads" ;

    
    // There is no MIME for a SPARQL query.
    // Either it is a GET or it is a "x-www-form-urlencoded"
    
    //public static final String contentSPARQL_X            = "application/x-sparql-query" ;
    public static final String contentSPARQLUpdate_X      = "application/x-sparql-update" ;

    //public static final String contentSPARQL              = "application/sparql-query" ;
    public static final String contentSPARQLUpdate        = "application/sparql-update" ;
    public static final String charsetUTF8                = "utf-8" ;
    
    // Names used in Jena for the parsers
    public static final String langRDFXML           = "RDF/XML" ;
    public static final String langRDFXMLAbbrev     = "RDF/XML-ABBREV" ;
    public static final String langNTriple          = "N-TRIPLE" ;
    public static final String langNTriples         = "N-TRIPLES" ;
    public static final String langN3               = "N3" ;
    public static final String langTurtle           = "TURTLE" ;

    public static final String langNQuads           = "NQUADS" ;
    public static final String langTriG             = "TRIG" ;

    
    /** Java name for UTF-8 encoding */
    public static final String encodingUTF8         = "utf-8" ;
    
}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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