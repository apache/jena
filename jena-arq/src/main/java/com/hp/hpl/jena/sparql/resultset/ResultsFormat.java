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

import static org.apache.jena.riot.WebContent.* ;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.resultset.ResultSetLang ;

import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.sparql.util.TranslationTable ;

// Better ....
//public enum ResultFormat
//{ 
//    // Merge with our system wide naming (WebContent?)
//    FMT_RS_XML , FMT_RS_JSON , FMT_RS_CSV , FMT_RS_TSV , FMT_RS_SSE , FMT_RS_BIO ,
//    FMT_NONE , FMT_TEXT , FMT_TUPLES , FMT_COUNT ,
//    FMT_RS_RDF , FMT_RDF_XML , FMT_RDF_N3 , FMT_RDF_TTL , FMT_RDF_TURTLE , FMT_RDF_NT ,
//    FMT_UNKNOWN ;
    

// Old world.  Remove in Jena3
public class ResultsFormat extends Symbol
{ 
    // ---- Compatibility (this started pre java 1.5)
    private ResultsFormat(String symbol)
    {
        super(symbol) ;
    }

    static public ResultsFormat FMT_RS_XML       = new ResultsFormat(contentTypeResultsXML) ;
    static public ResultsFormat FMT_RS_JSON      = new ResultsFormat(contentTypeResultsJSON) ;
    static public ResultsFormat FMT_RS_THRIFT    = new ResultsFormat(contentTypeResultsThrift) ;
    static public ResultsFormat FMT_RS_CSV       = new ResultsFormat(contentTypeTextCSV) ;
    static public ResultsFormat FMT_RS_TSV       = new ResultsFormat(contentTypeTextTSV) ;
    static public ResultsFormat FMT_RS_SSE       = new ResultsFormat(contentTypeSSE) ;
    static public ResultsFormat FMT_RS_BIO       = new ResultsFormat(contentTypeResultsBIO) ;
    static public ResultsFormat FMT_NONE         = new ResultsFormat("none") ;
    static public ResultsFormat FMT_TEXT         = new ResultsFormat("text") ;
    static public ResultsFormat FMT_TUPLES       = new ResultsFormat("tuples") ;
    static public ResultsFormat FMT_COUNT        = new ResultsFormat("count") ;
    static public ResultsFormat FMT_RDF_XML      = new ResultsFormat(contentTypeRDFXML) ;
    static public ResultsFormat FMT_RDF_N3       = new ResultsFormat(contentTypeN3) ;
    static public ResultsFormat FMT_RDF_TTL      = new ResultsFormat(contentTypeTurtle) ;
    static public ResultsFormat FMT_RDF_TURTLE   = new ResultsFormat(contentTypeTurtle) ;
    static public ResultsFormat FMT_RDF_NT       = new ResultsFormat(contentTypeNTriples) ;
    static public ResultsFormat FMT_UNKNOWN      = new ResultsFormat("unknown") ;
    // ---- Compatibility
    
    // Common names to symbol (used by arq.rset)
    private static TranslationTable<ResultsFormat> names = new TranslationTable<>(true) ;
    static {
        names.put("srx",         FMT_RS_XML) ;
        names.put("xml",         FMT_RS_XML) ;
        
        names.put("json",        FMT_RS_JSON) ;
        names.put("srj",         FMT_RS_JSON) ;
        names.put("srt",         FMT_RS_THRIFT) ;
        names.put("thrift",      FMT_RS_THRIFT) ;
        
        names.put("sse",         FMT_RS_SSE) ;
        names.put("csv",         FMT_RS_CSV) ;
        names.put("tsv",         FMT_RS_TSV) ;
        names.put("srb",         FMT_RS_BIO) ;
        names.put("text",        FMT_TEXT) ;
        names.put("count",       FMT_COUNT) ;
        names.put("tuples",      FMT_TUPLES) ;
        names.put("none",        FMT_NONE) ;
        
        names.put("rdf",         FMT_RDF_XML) ; 
        names.put("rdf/n3",      FMT_RDF_N3) ;
        names.put("rdf/xml",     FMT_RDF_XML) ;
        names.put("n3",          FMT_RDF_N3) ;
        names.put("ttl",         FMT_RDF_TTL) ;
        names.put("turtle",      FMT_RDF_TTL) ;
        names.put("graph",       FMT_RDF_TTL) ;
        names.put("nt",          FMT_RDF_NT) ;
        names.put("n-triples",   FMT_RDF_NT) ;

    }

    public static ResultsFormat guessSyntax(String url) 
    {
        return guessSyntax(url, FMT_RS_XML) ;
    }
    
    public static boolean isRDFGraphSyntax(ResultsFormat fmt)
    {
        if ( FMT_RDF_N3.equals(fmt) ) return true ;
        if ( FMT_RDF_TURTLE.equals(fmt) ) return true ;
        if ( FMT_RDF_XML.equals(fmt) ) return true ;
        if ( FMT_RDF_NT.equals(fmt) ) return true ;
        return false ;
    }
    
    public static ResultsFormat guessSyntax(String url, ResultsFormat defaultFormat)
    {
        // -- XML
        if ( url.endsWith(".srx") )
            return FMT_RS_XML ;
        if ( url.endsWith(".xml") )
            return FMT_RS_XML ;
        
        // -- Some kind of RDF
        if ( url.endsWith(".rdf") )
            return FMT_RDF_XML ;
        if ( url.endsWith(".n3") )
            return FMT_RDF_N3 ;
        if ( url.endsWith(".ttl") )
            return FMT_RDF_TURTLE ;
        
        // -- JSON
        if ( url.endsWith(".srj") )
            return FMT_RS_JSON ;
        if ( url.endsWith(".json") )
            return FMT_RS_JSON ;
        if ( url.endsWith(".yml") )
            return FMT_RS_JSON ;
        
        // -- Thrift
        if ( url.endsWith(".srt") )
            return FMT_RS_THRIFT ;
        
        // -- SSE : http://jena.apache.org/documentation/notes/sse.html
        if ( url.endsWith(".sse") )
            return FMT_RS_SSE ;

        if ( url.endsWith(".srb") ) // BindingsIO format.
            return FMT_RS_BIO ;

        // Likely to be something completely different!
        if ( url.endsWith(".csv") )
            return FMT_RS_CSV ;
        if ( url.endsWith(".tsv") )
            return FMT_RS_TSV ;
        
        return defaultFormat ;
    }
    
    
    /** Look up a short name for a result set FMT_
     * 
     * @param s  Short name
     * @return  ResultSetFormat
     */
 
    public static ResultsFormat lookup(String s)
    {
        return names.lookup(s) ;
    }

    static Map<ResultsFormat, Lang> mapResultsFormatToLang = new HashMap<>() ;
    static {
        mapResultsFormatToLang.put(ResultsFormat.FMT_RS_CSV, ResultSetLang.SPARQLResultSetCSV) ;
        mapResultsFormatToLang.put(ResultsFormat.FMT_RS_TSV, ResultSetLang.SPARQLResultSetTSV) ;
        mapResultsFormatToLang.put(ResultsFormat.FMT_RS_XML, ResultSetLang.SPARQLResultSetXML) ;
        mapResultsFormatToLang.put(ResultsFormat.FMT_RS_JSON, ResultSetLang.SPARQLResultSetJSON) ;
        mapResultsFormatToLang.put(ResultsFormat.FMT_RS_THRIFT, ResultSetLang.SPARQLResultSetThrift) ;
    }

    public static Lang convert(ResultsFormat fmt) {
        return mapResultsFormatToLang.get(fmt) ;
    }
}
