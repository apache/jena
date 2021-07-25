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

package org.apache.jena.sparql.exec;

import java.io.OutputStream ;
import java.util.Iterator ;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.riot.ResultSetMgr ;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.rw.ResultsWriter;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.Prologue ;

/** RowSetFormatter - Convenience ways to call the various output formatters.
 *  in various formats.
 *  @see ResultSetMgr
 */

public class RowSetFormatter {

    private RowSetFormatter() {}

    /**
     * This operation faithfully walks the rowSet but does nothing with the rows.
     */
    public static void consume(RowSet rowSet)
    { count(rowSet); }

    /**
     * Count the rows in the RowSet (from the current point of RowSet).
     * This operation consumes the RowSet.
     */
    public static long count(RowSet rowSet)
    { return rowSet.rewindable().size(); }

    /**
     * Output a result set in a text format.  The result set is consumed.
     * Use @see{ResultSetFactory.makeRewindable(ResultSet)} for a rewindable one.
     * <p>
     *  This caches the entire results in memory in order to determine the appropriate
     *  column widths and therefore may exhaust memory for large results
     *  </p>
     * @param rowSet   result set
     */
    public static void out(RowSet rowSet)
    { out(System.out, rowSet) ; }

    /**
     * Output a result set in a text format.
     * <p>
     *  This caches the entire results in memory in order to determine the appropriate
     *  column widths and therefore may exhaust memory for large results
     *  </p>
     * @param out        OutputStream
     * @param rowSet   result set
     */
    public static void out(OutputStream out, RowSet rowSet)
    { out(out, rowSet, (PrefixMap)null) ; }

    /**
     * Output a result set in a text format.
     * <p>
     *  This caches the entire results in memory in order to determine the appropriate
     *  column widths and therefore may exhaust memory for large results
     *  </p>
     * @param out        OutputStream
     * @param resultSet  Result set
     * @param pmap       Prefix mapping for abbreviating URIs.
     */
    public static void out(OutputStream out, RowSet resultSet, PrefixMap pmap) {
        PrefixMapping prefixMapping = (pmap == null) ? null : Prefixes.adapt(pmap);
        Prologue prologue = new Prologue(prefixMapping);
        out(out, resultSet, prologue);
    }

    /**
     * Output a result set in a text format.  The result set is consumed.
     * Use @see{ResultSetFactory.makeRewindable(ResultSet)} for a rewindable one.
     * <p>
     *  This caches the entire results in memory in order to determine the appropriate
     *  column widths and therefore may exhaust memory for large results
     *  </p>
     * @param out        OutputStream
     * @param rowSet  result set
     * @param prologue   Prologue, used to abbreviate IRIs
     */
    public static void out(OutputStream out, RowSet rowSet, Prologue prologue) {
        ResultsWriter.create()
            .lang(ResultSetLang.RS_Text)
            // [QExec] ResultSetWriter with prefixes.
            .set(ARQConstants.symPrologue, prologue)
            // [QExec]
            .write(out, ResultSet.adapt(rowSet));
    }

    /**
     * Output an ASK answer
     * @param answer    The boolean answer
     */
    public static void out(boolean answer)
    { out(System.out, answer) ; }

    /**
     * Output an ASK answer
     * @param out       OutputStream
     * @param answer    The boolean answer
     */
    public static void out(OutputStream out, boolean answer) {
        ResultsWriter.create().lang(ResultSetLang.RS_Text).write(out, answer);
    }

    /** Touch every var/value */
    private static void materialize(QuerySolution qs) {
        for ( Iterator<String> iter = qs.varNames() ; iter.hasNext() ; ) {
            String vn = iter.next();
            RDFNode n = qs.get(vn) ;
        }
    }

//    public static long count(RowSet rowSet) {
//        return Iter.count(rowSet);
//    }

    // ---- SSE


    // ---- CSV

}
