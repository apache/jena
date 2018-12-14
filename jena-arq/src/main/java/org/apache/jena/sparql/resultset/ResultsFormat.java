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

package org.apache.jena.sparql.resultset;

import static org.apache.jena.riot.WebContent.* ;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.resultset.ResultSetLang ;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.sparql.util.Symbol ;
import org.apache.jena.sparql.util.TranslationTable ;

// Better ....
//public enum ResultFormat
//{ 
//    // Merge with our system wide naming (WebContent?)
//    FMT_RS_XML , FMT_RS_JSON , FMT_RS_CSV , FMT_RS_TSV , FMT_RS_SSE , FMT_RS_BIO ,
//    FMT_NONE , FMT_TEXT , FMT_TUPLES , FMT_COUNT ,
//    FMT_RS_RDF , FMT_RDF_XML , FMT_RDF_N3 , FMT_RDF_TTL , FMT_RDF_TURTLE , FMT_RDF_NT ,
//    FMT_UNKNOWN ;
    

// Old world.
public class ResultsFormat extends Symbol
{ 
    private ResultsFormat(String symbol) {
        super(symbol);
    }

    static public ResultsFormat FMT_RS_XML       = new ResultsFormat(contentTypeResultsXML) ;
    static public ResultsFormat FMT_RS_JSON      = new ResultsFormat(contentTypeResultsJSON) ;
    static public ResultsFormat FMT_RS_THRIFT    = new ResultsFormat(contentTypeResultsThrift) ;
    static public ResultsFormat FMT_RS_CSV       = new ResultsFormat(contentTypeTextCSV) ;
    static public ResultsFormat FMT_RS_TSV       = new ResultsFormat(contentTypeTextTSV) ;
    static public ResultsFormat FMT_RS_SSE       = new ResultsFormat(contentTypeSSE) ;
    static public ResultsFormat FMT_NONE         = new ResultsFormat("none") ;
    static public ResultsFormat FMT_TEXT         = new ResultsFormat("text") ;
    static public ResultsFormat FMT_TUPLES       = new ResultsFormat("tuples") ;
    static public ResultsFormat FMT_COUNT        = new ResultsFormat("count") ;
    // Also used for output of result sets as RDF.
    static public ResultsFormat FMT_RDF_XML      = new ResultsFormat(contentTypeRDFXML) ;
    static public ResultsFormat FMT_RDF_N3       = new ResultsFormat(contentTypeN3) ;
    static public ResultsFormat FMT_RDF_TTL      = new ResultsFormat(contentTypeTurtle) ;
    static public ResultsFormat FMT_RDF_TURTLE   = new ResultsFormat(contentTypeTurtle) ;
    static public ResultsFormat FMT_RDF_NT       = new ResultsFormat(contentTypeNTriples) ;
    static public ResultsFormat FMT_RDF_TRIG     = new ResultsFormat(contentTypeTriG) ;
    static public ResultsFormat FMT_RDF_NQ       = new ResultsFormat(contentTypeNQuads) ;
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
        names.put("ntriples",    FMT_RDF_NT) ;
        
        names.put("nq",          FMT_RDF_NQ) ;
        names.put("nquads",      FMT_RDF_NQ) ;
        names.put("n-quads",     FMT_RDF_NQ) ;
        names.put("trig",        FMT_RDF_TRIG) ;

    }

    public static ResultsFormat guessSyntax(String url) {
        return guessSyntax(url, FMT_RS_XML);
    }

    public static boolean isRDFGraphSyntax(ResultsFormat fmt) {
        if ( FMT_RDF_N3.equals(fmt) )
            return true;
        if ( FMT_RDF_TURTLE.equals(fmt) )
            return true;
        if ( FMT_RDF_XML.equals(fmt) )
            return true;
        if ( FMT_RDF_NT.equals(fmt) )
            return true;
        return false;
    }

    public static boolean isDatasetSyntax(ResultsFormat fmt) {
        if ( FMT_RDF_TRIG.equals(fmt) )
            return true;
        if ( FMT_RDF_NQ.equals(fmt) )
            return true;
        return false;
    }

    public static ResultsFormat guessSyntax(String url, ResultsFormat defaultFormat) {
        // -- XML
        if ( url.endsWith(".srx") )
            return FMT_RS_XML;
        if ( url.endsWith(".xml") )
            return FMT_RS_XML;

        // -- Some kind of RDF
        if ( url.endsWith(".rdf") )
            return FMT_RDF_XML;
        if ( url.endsWith(".n3") )
            return FMT_RDF_N3;
        if ( url.endsWith(".ttl") )
            return FMT_RDF_TURTLE;

        // -- JSON
        if ( url.endsWith(".srj") )
            return FMT_RS_JSON;
        if ( url.endsWith(".json") )
            return FMT_RS_JSON;
        if ( url.endsWith(".yml") )
            return FMT_RS_JSON;

        // -- Thrift
        if ( url.endsWith(".srt") )
            return FMT_RS_THRIFT;

        // -- SSE : http://jena.apache.org/documentation/notes/sse.html
        if ( url.endsWith(".sse") )
            return FMT_RS_SSE;

        // Likely to be something completely different!
        if ( url.endsWith(".csv") )
            return FMT_RS_CSV;
        if ( url.endsWith(".tsv") )
            return FMT_RS_TSV;

        // -- Dataset
        if ( url.endsWith(".trig") )
            return FMT_RDF_TRIG;
        if ( url.endsWith(".nq") )
            return FMT_RDF_NQ;

        return defaultFormat;
    }

    /**
     * Look up a short name for a result set FMT_
     * 
     * @param s
     *            Short name
     * @return ResultSetFormat
     */
    public static ResultsFormat lookup(String s) {
        return names.lookup(s);
    }

    /**
     * Mapping from old-style {@link ResultsFormat} to {@link ResultSetLang} or other
     * {@link Lang}. See also {@link QueryExecUtils#outputResultSet} for dispatch of some old,
     * specialized types such as results encoded in RDF.
     */ 
    static Map<ResultsFormat, Lang> mapResultsFormatToLang = new HashMap<>() ;
    static {
        mapResultsFormatToLang.put(ResultsFormat.FMT_NONE,      ResultSetLang.SPARQLResultSetNone) ;
        mapResultsFormatToLang.put(ResultsFormat.FMT_RS_CSV,    ResultSetLang.SPARQLResultSetCSV) ;
        mapResultsFormatToLang.put(ResultsFormat.FMT_RS_TSV,    ResultSetLang.SPARQLResultSetTSV) ;
        mapResultsFormatToLang.put(ResultsFormat.FMT_RS_XML,    ResultSetLang.SPARQLResultSetXML) ;
        mapResultsFormatToLang.put(ResultsFormat.FMT_RS_JSON,   ResultSetLang.SPARQLResultSetJSON) ;
        mapResultsFormatToLang.put(ResultsFormat.FMT_RS_THRIFT, ResultSetLang.SPARQLResultSetThrift) ;
        mapResultsFormatToLang.put(ResultsFormat.FMT_TEXT,      ResultSetLang.SPARQLResultSetText);
    }

    public static Lang convert(ResultsFormat fmt) {
        return mapResultsFormatToLang.get(fmt) ;
    }
    
    /** Write a {@link ResultSet} in various old style formats no longer recommended.
     * Return true if the format was handled else false.
     */ 
    public static boolean oldWrite(OutputStream out, ResultsFormat outputFormat, Prologue prologue, ResultSet resultSet) {
        if ( outputFormat.equals(ResultsFormat.FMT_COUNT) ) {
            int count = ResultSetFormatter.consume(resultSet) ;
            PrintStream pOut = new PrintStream(out);
            pOut.println("Count = " + count) ;
            return true ;
        }
        
        if ( outputFormat.equals(ResultsFormat.FMT_RDF_XML) ) {
            RDFOutput.outputAsRDF(out, "RDF/XML-ABBREV", resultSet) ;
            return true;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_TTL) ) {
            RDFOutput.outputAsRDF(out, "TTL", resultSet) ;
            return true;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_NT) ) {
            RDFOutput.outputAsRDF(out, "N-TRIPLES", resultSet) ;
            return true;
        }
        
        if ( outputFormat.equals(ResultsFormat.FMT_TUPLES) ) {
            PlainFormat pFmt = new PlainFormat(out, prologue) ;
            ResultSetApply a = new ResultSetApply(resultSet, pFmt) ;
            a.apply() ;
            return true;
        }

        return false;
    }
}
