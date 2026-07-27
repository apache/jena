/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.resultset;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.sparql.util.TranslationTable;

/**
 * The output formats for all query types. This allows the APi to given a results
 * choice before the kind of query is known.
 * <p>
 * This does not include results sets as RDF. They are provided for tests with
 * {@link RDFInput} and {@link RDFOutput}.
 * <p>
 * A {@code ResultsFormat} item has three parts, result format, triples format (e.g.
 * CONSTRUCT), and quads (for extended CONSTRUCT). These are stored in a lookup table
 * for string to {@code ResultsFormat}, which is used by commands.
 */

public enum ResultsFormat {
    // Does not cover boolean results.

    XML(ResultSetLang.RS_XML, RDFFormat.RDFXML_ABBREV, null),
    JSON(ResultSetLang.RS_JSON, RDFFormat.JSONLD, RDFFormat.JSONLD),
    TEXT(ResultSetLang.RS_Text, RDFFormat.TURTLE, RDFFormat.TRIG),

    CSV(ResultSetLang.RS_CSV, null, null),
    TSV(ResultSetLang.RS_TSV, null, null),

    THRIFT(ResultSetLang.RS_Thrift, RDFFormat.RDF_THRIFT, RDFFormat.RDF_THRIFT),
    PROTOBUF(ResultSetLang.RS_Protobuf, RDFFormat.RDF_PROTO, RDFFormat.RDF_PROTO),

    // result set as RDF is handled specially
    TTL(null, RDFFormat.TURTLE, null),
    NT(null, RDFFormat.NTRIPLES, null),

    TRIG(null, RDFFormat.TRIG, RDFFormat.TRIG),
    NQ(null, RDFFormat.NQUADS, RDFFormat.NQUADS),

    RDFXML(null, RDFFormat.RDFXML, null),
    JSONLD(null, RDFFormat.JSONLD, RDFFormat.JSONLD),

    // Special name.
    COUNT(null, null, null),

    NONE(ResultSetLang.RS_None, RDFFormat.RDFNULL, RDFFormat.RDFNULL),

    SSE(null, null, null)
   ;

    private final Lang resultSetLang;
    private final RDFFormat triplesFormat;
    private final RDFFormat quadsFormat;
    //private final boolean supportsBoolean;

    private ResultsFormat(Lang resultSetLang, RDFFormat triplesFormat, RDFFormat quadsFormat) {
        this.resultSetLang = resultSetLang;
        this.triplesFormat = triplesFormat;
        this.quadsFormat = quadsFormat;
        //this.supportsBoolean = supportsBoolean;
    }

    public Lang resultSetLang() {
        return resultSetLang;
    }

    public RDFFormat triplesFormat() {
        return triplesFormat;
    }

    public RDFFormat quadsFormat() {
        return quadsFormat;
    }

//    public boolean supportsBoolean() {
//        return supportsBoolean;
//    }

    public boolean isResultSet() {
        return ResultSetLang.isRegistered(resultSetLang);
    }

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
     * Look up a short name for an output format.
     *
     * @param shortname Short name
     * @return ResultSetFormat
     */
    public static ResultsFormat lookup(String shortname) {
        return names.lookup(shortname);
    }

    // Common names to symbol (used by arq.rset)
    private static TranslationTable<ResultsFormat> names = new TranslationTable<>(true);
    static {
        names.put("srx",         XML);
        names.put("srj",         JSON);
        names.put("srt",         THRIFT);
        names.put("srp",         PROTOBUF);
        names.put("srpb",        PROTOBUF);

        names.put("rt",          THRIFT);
        names.put("trdf",        THRIFT);

        names.put("rpb",         PROTOBUF);
        names.put("pbrdf",       PROTOBUF);

        names.put("rdfxml",      RDFXML);

        names.put("xml",         XML);
        names.put("json",        JSON);

        names.put("rdf",         TTL);
        names.put("ttl",         TTL);
        names.put("turtle",      TTL);

        names.put("trig",        TRIG);

        names.put("n-triples",   NT);
        names.put("ntriples",    NT);
        names.put("nt",          NT);

        names.put("n-quads",     NQ);
        names.put("nquads",      NQ);
        names.put("nq",          NQ);

        names.put("jsonld",      JSONLD);
        names.put("json-ld",     JSONLD);

        names.put("sse",         SSE);
        names.put("csv",         CSV);
        names.put("tsv",         TSV);

        names.put("text",        TEXT);
        names.put("txt",         TEXT);
        names.put("count",       COUNT);

        names.put("none",        NONE);
    }
}
