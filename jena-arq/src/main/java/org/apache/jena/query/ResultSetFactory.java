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

package org.apache.jena.query;

import java.io.IOException;
import java.io.InputStream ;
import java.util.List ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.ReadAnything;
import org.apache.jena.riot.ResultSetMgr ;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetOnClose;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.ResultSetStream ;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.resultset.* ;

/** ResultSetFactory - make result sets from places other than a query. */

public class ResultSetFactory {
    // See also ResultSetMgr - which post-dates this code.
    // Ideally, read operations here should call ResultSetMgr.
    // The exception is XML from a string and the archaic RDF to ResultSet forms.

    /**
     * Load a result set from file or URL into a result set (memory backed).
     *
     * @param filenameOrURI
     * @return ResultSet
     */
    public static ResultSet load(String filenameOrURI) {
        return load(filenameOrURI, null);
    }

    /**
     * Load a result set from file or URL into a result set (memory backed).
     *
     * @param filenameOrURI
     * @param format
     * @return ResultSet
     */
    public static ResultSet load(String filenameOrURI, ResultsFormat format) {
        if (format == null)
            format = ResultsFormat.guessSyntax(filenameOrURI);
        // Result sets are iterators.
        // The input stream isn't finished with when the result set is created.
        // Close input stream when the result set is finished with
        // (exhausted or closed).
        InputStream in = IO.openFile(filenameOrURI) ;
        ResultSet rs = load(in, format) ;
        Runnable onClose = ()-> {
            try { in.close(); }
            catch (IOException ex) { throw IOX.exception(ex); }
        };
        ResultSet rs2 = new ResultSetOnClose(rs, onClose);
        return rs;
    }

    /**
     * Load a result set from input stream into a result set (memory backed).
     *
     * @param input
     * @param format
     * @return ResultSet
     */
    public static ResultSet load(InputStream input, ResultsFormat format) {
        if (format == null) {
            Log.warn(ResultSet.class, "Null format - defaulting to XML");
            format = ResultsFormat.XML;
        }

        if ( format == ResultsFormat.TEXT ) {
            Log.warn(ResultSet.class, "Can't read a text result set");
            throw new ResultSetException("Can't read a text result set");
        }

        Lang lang = format.resultSetLang();

        if ( lang != null )
            return ResultSetMgr.read(input, lang) ;

        Log.warn(ResultSet.class, "Unknown result set syntax: " + format);
        return null;
    }

    /**
     * Read in any kind of result kind (result set, boolean, graph) Guess the
     * syntax based on filename/URL extension.
     */
    public static SPARQLResult result(String filenameOrURI) {
        return ReadAnything.read(filenameOrURI);
    }

    /**
     * Read XML which is the format of the SPARQL result set format.
     *
     * @param in
     *            InputStream
     * @return ResultSet
     */
    public static ResultSet fromXML(InputStream in) {
        return ResultSetMgr.read(in, ResultSetLang.RS_XML);
    }

    /**
     * Read from an input stream which is the format of the SPARQL result set
     * format in JSON.
     *
     * @param in
     *            InputStream
     * @return ResultSet
     */
    public static ResultSet fromJSON(InputStream in) {
        return ResultSetMgr.read(in, ResultSetLang.RS_JSON);
    }

    /**
     * Turns an RDF model, with properties and classes from the result set
     * vocabulary, into a SPARQL result set. The result set formed is a copy in
     * memory.
     *
     * @param model
     * @return ResultSet
     */
    public static ResultSet makeResults(Model model) {
        return new RDFInput(model);
    }

    /**
     * Turns an RDF model, with properties and classes from the result set
     * vocabulary, into a SPARQL result set which is rewindable (has a
     * .reset()operation). The result set formed is a copy in memory.
     *
     * @param model
     * @return ResultSetRewindable
     */
    public static ResultSetRewindable makeRewindable(Model model) {
        return new RDFInput(model);
    }

    /**
     * Turn an existing result set into a rewindable one.
     * May take a copy but this is not guaranteed
     * Uses up the result set passed in which is no longer valid as a ResultSet.
     *
     * @param resultSet
     * @return ResultSetRewindable
     */
    public static ResultSetRewindable makeRewindable(ResultSet resultSet) {
        if ( resultSet instanceof ResultSetRewindable ) {
            ResultSetRewindable rsw = (ResultSetRewindable)resultSet;
            rsw.reset();
            return rsw;
        }
        return new ResultSetMem(resultSet);
    }

    /**
     * Turn a row set into a rewindable ResultSet.
     * Uses up the result set passed in which is no longer valid as a RowSet.
     *
     * @param rowSet
     * @return ResultSetRewindable
     */
    public static ResultSetRewindable makeRewindable(RowSet rowSet) {
        return makeRewindable(ResultSet.adapt(rowSet));
    }

    /**
     * Turns an existing result set into one with peeking capabilities
     * <p>
     * Using the returned result set consumes the result set passed in, the
     * underlying result set must be at the start in order to be made peeking.
     * If you create such a result set you should avoid accessing the underlying
     * result set directly as this may cause results to be missed or put the
     * returned peekable result set into an invalid state.
     * </p>
     * <p> Note that rewindable results may typically also be peekable so may
     * be more broadly applicable if you can afford the cost of loading all the
     * results into memory. </p>
     *
     * @param resultSet
     *            Result set to wrap
     * @return Peekable results
     */
    public static ResultSetPeekable makePeekable(ResultSet resultSet) {
        return new ResultSetPeeking(resultSet);
    }

    /** Return a closable resultset for a {@link QueryExecution}.
     * The {@link QueryExecution} must be for a {@code SELECT} query.
     * <p>
     * Example:
     * <pre>
     *   QueryExecution qExec = QueryExecutionFactory.create(...);
     *   try (ResultSetCloseable rs = ResultSetFactory.closableResultSet(qExec) ) {
     *       ...
     * }
     * </pre>
     *
     * @param queryExecution {@code QueryExecution} must be for a {@code SELECT} query.
     * @return ResultSetCloseable
     */
    public static ResultSetCloseable closeableResultSet(QueryExecution queryExecution) {
        return ResultSetCloseable.closeableResultSet(queryExecution);
    }

    /**
     * Take a copy of a result set - the result set returns is an in-memory
     * copy. It is not attached to the original query execution object which can
     * be closed.
     *
     * @param results
     * @return ResultSet
     */
    public static ResultSetRewindable copyResults(ResultSet results) {
        return new ResultSetMem(results);
    }

    /**
     * Build a result set from one of ARQ's lower level query iterator.
     *
     * @param queryIterator
     * @param vars
     *            List of variables, by name, for the result set
     * @return ResultSet
     */
    public static ResultSet create(QueryIterator queryIterator, List<String> vars) {
        return ResultSetStream.create(Var.varList(vars), queryIterator);
    }
}
