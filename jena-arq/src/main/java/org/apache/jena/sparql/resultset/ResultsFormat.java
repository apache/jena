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

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.sparql.util.TranslationTable;

/**
 * The output formats for all query types.
 * Result sets, boolean graphs.
 * <p>
 * This does not include results sets as RDF is elsewhere which is provided for tests with {@link RDFInput} and {@link RDFOutput}.
 */

public enum ResultsFormat {
    // Results formats, by surface syntax.
    // Used by commands.

    XML(ResultSetLang.RS_XML, RDFFormat.RDFXML_ABBREV),
    JSON(ResultSetLang.RS_JSON, RDFFormat.JSONLD11),
    TEXT(ResultSetLang.RS_Text, RDFFormat.TURTLE),

    CSV(ResultSetLang.RS_CSV, null),
    TSV(ResultSetLang.RS_TSV, null),

    THRIFT(ResultSetLang.RS_Thrift, RDFFormat.RDF_THRIFT),
    PROTOBUF(ResultSetLang.RS_Protobuf, RDFFormat.RDF_PROTO),

    // result set as RDF is handled specially
    TTL(null, RDFFormat.TURTLE),
    NT(null, RDFFormat.NTRIPLES),
    RDFXML(null, RDFFormat.RDFXML),
    RDF_JSONLD(null, RDFFormat.JSONLD),

    // Special name.
    COUNT(null, null),

    NONE(ResultSetLang.RS_None, RDFFormat.RDFNULL),

    SSE(null, null),
    TUPLES(null, null)
    ;

    private final Lang resultSetLang;
    private final RDFFormat rdfFormat;
    //private final boolean supportsBoolean;

    ResultsFormat(Lang resultSetLang, RDFFormat rdfFormat) {
        this.resultSetLang = resultSetLang;
        this.rdfFormat = rdfFormat;

        //this.supportsBoolean = supportsBoolean;
    }

    public Lang resultSetLang() {
        return resultSetLang;
    }

    public RDFFormat rdfFormat() {
        return rdfFormat;
    }

//
//    public boolean supportsBoolean() {
//        return supportsBoolean;
//    }
//
//    public boolean isResultSet() {
//        return ResultSetLang.isRegistered(lang());
//    }

    /** Guess the syntax of a result set URL */
    public static ResultsFormat guessSyntax(String resultsFilename) {
        Lang rsLang = RDFLanguages.pathnameToLang(resultsFilename);
        if ( rsLang == null )
            return null;
//        if ( ! ResultSetLang.isRegistered(rsLang) )
//            return null;
        ResultsFormat[] enums = ResultsFormat.values();
        for ( ResultsFormat rsFmt : enums ) {
            if ( rsFmt.resultSetLang().equals(rsLang) )
                return rsFmt;
        }
        return null;
    }

    /**
     * Look up a short name for a result set FMT_
     *
     * @param shortname Short name
     * @return ResultSetFormat
     */
    public static ResultsFormat lookup(String shortname) {
        return names.lookup(shortname);
    }

    // Common names to symbol (used by arq.rset)
    private static TranslationTable<ResultsFormat> names = new TranslationTable<>(true) ;
    static {
        names.put("srx",         XML) ;
        names.put("xml",         XML) ;

        names.put("json",        JSON) ;
        names.put("srj",         JSON) ;

        names.put("srt",         THRIFT) ;
        names.put("srp",         PROTOBUF) ;

        names.put("rdfxml",      RDFXML) ;
        names.put("rdf",         TTL) ;
        names.put("ttl",         TTL);
        names.put("turtle",      TTL);

        names.put("n-triples",   NT);
        names.put("ntriples",    NT);
        names.put("nt",          NT);

        names.put("jsonld",      RDF_JSONLD) ;
        names.put("json-ld",     RDF_JSONLD) ;

        names.put("sse",         SSE) ;
        names.put("csv",         CSV) ;
        names.put("tsv",         TSV) ;
        names.put("text",        TEXT) ;
        names.put("count",       COUNT) ;
        names.put("tuples",      TUPLES) ;
        names.put("none",        NONE) ;

        //names.put("rdf",         ???) ;

//        names.put("rdf",         RDF_XML) ;
//        names.put("rdf/n3",      RDF_N3) ;
//        names.put("rdf/xml",     RDF_XML) ;
//        names.put("n3",          RDF_N3) ;
//        names.put("ttl",         RDF_TTL) ;
//        names.put("turtle",      RDF_TTL) ;
//        names.put("graph",       RDF_TTL) ;
//        names.put("nt",          RDF_NT) ;
//        names.put("n-triples",   RDF_NT) ;
//        names.put("ntriples",    RDF_NT) ;
//        names.put("jsonld",      RDF_JSONLD) ;
//        names.put("json-ld",     RDF_JSONLD) ;
//
//        names.put("nq",          RDF_NQ) ;
//        names.put("nquads",      RDF_NQ) ;
//        names.put("n-quads",     RDF_NQ) ;
//        names.put("trig",        RDF_TRIG) ;
    }
}
