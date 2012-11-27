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

package org.openjena.riot;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.atlas.web.MediaType ;

public class WebContent
{
    // Names for things.
    
    // contentType => ctStr
    
    // Rename as: 
    // TEXT_TURTLE etc etc.
    
    public static final String contentTypeN3                = "text/rdf+n3" ;
    public static final String contentTypeN3Alt1            = "application/n3" ;
    public static final String contentTypeN3Alt2            = "text/n3" ;
    
    public static final String contentTypeTurtle            = "text/turtle" ;
    public static final String contentTypeTurtleAlt1        = "application/turtle" ;
    public static final String contentTypeTurtleAlt2        = "application/x-turtle" ;

    public static final String contentTypeRDFXML            = "application/rdf+xml" ;
    public static final String contentTypeRDFJSON           = "application/rdf+json" ;
    
    // MIME type for N-triple is text/plain (!!!)
    public static final String contentTypeTextPlain         = "text/plain" ;
    public static final String contentTypeNTriples          = "application/n-triples" ;
    public static final String contentTypeNTriplesAlt       = contentTypeTextPlain ;
    
    public static final String contentTypeXML               = "application/xml" ;
    public static final String contentTypeXMLAlt            = "text/xml" ;

    public static final String contentTypeTriG              = "text/trig" ;
    public static final String contentTypeNQuads            = "application/n-quads" ;
    
    public static final String contentTypeTriGAlt1          = "application/x-trig" ;
    public static final String contentTypeTriGAlt2          = "application/trig" ;
    public static final String contentTypeNQuadsAlt1        = "text/n-quads" ;
    public static final String contentTypeNQuadsAlt2        = "text/nquads" ;

    public static final String contentTypeTriX              = "application/trix+xml" ;
    public static final String contentTypeOctets            = "application/octet-stream" ;
    public static final String contentTypeMultiMixed        = "multipart/mixed" ;
    public static final String contentTypeMultiFormData     = "multipart/form-data" ;
    public static final String contentTypeMultiAlt          = "multipart/alternative" ;

    public static final String contentTypeRdfJson			= "application/rdf+json" ;
    
    public static final String contentTypeResultsXML        = "application/sparql-results+xml" ;
    public static final String contentTypeResultsJSON       = "application/sparql-results+json" ;
    public static final String contentTypeJSON              = "application/json" ;
    // Unofficial
    public static final String contentTypeResultsBIO        = "application/sparql-results+bio" ;
    
    public static final String contentTypeSPARQLQuery       = "application/sparql-query" ;
    public static final String contentTypeSPARQLUpdate      = "application/sparql-update" ;
    public static final String contentTypeForm              = "application/x-www-form-urlencoded" ;
    public static final String contentTypeTextCSV           = "text/csv" ;
    public static final String contentTypeTextTSV           = "text/tab-separated-values" ;
    
    public static final String contentTypeSSE               = "text/sse" ;
    
    public static final String charsetUTF8                  = "utf-8" ;
    public static final String charsetASCII                 = "ascii" ;

    // Names used in Jena for the parsers
    // See also Lang enum (preferred).
    public static final String langRDFXML           = "RDF/XML" ;
    public static final String langRDFXMLAbbrev     = "RDF/XML-ABBREV" ;
    public static final String langNTriple          = "N-TRIPLE" ;
    public static final String langNTriples         = "N-TRIPLES" ;
    public static final String langN3               = "N3" ;
    public static final String langTurtle           = "TURTLE" ;
    public static final String langTTL              = "TTL" ;
    public static final String langRdfJson			= "RDF/JSON" ;

    public static final String langNQuads           = "NQUADS" ;
    public static final String langTriG             = "TRIG" ;
    
    /** Java name for UTF-8 encoding */
    public static final String encodingUTF8         = "utf-8" ;
    
    private static Map<String, Lang> mapContentTypeToLang = new HashMap<String, Lang>() ;
    static {
        // Or is code preferrable?
        mapContentTypeToLang.put(contentTypeRDFXML,         Lang.RDFXML) ;
        mapContentTypeToLang.put(contentTypeN3,             Lang.N3);
        mapContentTypeToLang.put(contentTypeN3Alt1,         Lang.N3);
        mapContentTypeToLang.put(contentTypeN3Alt2,         Lang.N3);
        mapContentTypeToLang.put(contentTypeTurtle,         Lang.TURTLE) ;
        mapContentTypeToLang.put(contentTypeTurtleAlt1,     Lang.TURTLE) ;
        mapContentTypeToLang.put(contentTypeTurtleAlt2,     Lang.TURTLE) ;
        mapContentTypeToLang.put(contentTypeNTriples,       Lang.NTRIPLES) ;   // text/plain
        mapContentTypeToLang.put(contentTypeNTriplesAlt,    Lang.NTRIPLES) ;
        mapContentTypeToLang.put(contentTypeRdfJson,		Lang.RDFJSON) ;

        mapContentTypeToLang.put(contentTypeNQuads,         Lang.NQUADS) ;
        mapContentTypeToLang.put(contentTypeNQuadsAlt1,     Lang.NQUADS) ;
        mapContentTypeToLang.put(contentTypeNQuadsAlt2,     Lang.NQUADS) ;
        mapContentTypeToLang.put(contentTypeTriG,           Lang.TRIG) ;
        mapContentTypeToLang.put(contentTypeTriGAlt1,       Lang.TRIG) ;
        mapContentTypeToLang.put(contentTypeTriGAlt2,       Lang.TRIG) ;
        
    }
    
    /** Return our "canonical" name for a Content Type.
     * This should be the standard one, no X-* 
     */
    public static String contentTypeCanonical(String contentType)
    { 
        Lang lang = contentTypeToLang(contentType) ;
        if ( lang == null )
            return null ;
        return mapLangToContentType.get(lang) ;
    }
    
    public static Lang contentTypeToLang(String contentType) { return mapContentTypeToLang.get(contentType) ; }

    /** Canonical names */
    private static Map<Lang, String> mapLangToContentType =  new HashMap<Lang, String>() ;
    static {
        mapLangToContentType.put(Lang.N3,           contentTypeN3) ;
        mapLangToContentType.put(Lang.TURTLE,       contentTypeTurtle) ;
        mapLangToContentType.put(Lang.NTRIPLES,     contentTypeNTriples) ;
        mapLangToContentType.put(Lang.RDFXML,       contentTypeRDFXML) ;
        mapLangToContentType.put(Lang.RDFJSON,		contentTypeRdfJson) ;
        
        mapLangToContentType.put(Lang.NQUADS,       contentTypeNQuads) ;
        mapLangToContentType.put(Lang.TRIG,         contentTypeTriG) ;
    }
    public static String mapLangToContentType(Lang lang) { return mapLangToContentType.get(lang) ; }
    
    public static String getCharsetForContentType(String contentType)
    {
        MediaType ct = MediaType.create(contentType) ;
        if ( ct.getCharset() != null )
            return ct.getCharset() ;
        
        String mt = ct.getContentType() ;
        if ( contentTypeNTriples.equals(mt) )       return charsetUTF8 ;
        if ( contentTypeNTriplesAlt.equals(mt) )    return charsetASCII ;
        if ( contentTypeNQuads.equals(mt) )         return charsetUTF8 ;
        if ( contentTypeNQuadsAlt1.equals(mt) )      return charsetASCII ;
        if ( contentTypeNQuadsAlt2.equals(mt) )      return charsetASCII ;
        return charsetUTF8 ;
    }

//    public static ContentType contentTypeForFilename(String filename)
//    {
//    }
    

}
