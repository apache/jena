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

package com.hp.hpl.jena.query;

import java.io.InputStream ;
import java.util.List ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.ResultSetMgr ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.shared.NotFoundException ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.resultset.* ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderTable ;
import com.hp.hpl.jena.util.FileManager ;

/** ResultSetFactory - make result sets from places other than a query. */

public class ResultSetFactory {
    // See also ResultSetMgr -- this post-dates this code.
    // Ideally, read operations here should call ResultSetMgr.
    
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
        InputStream in = IO.openFile(filenameOrURI) ;
        return load(in, format) ;
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
            format = ResultsFormat.FMT_RS_XML;
        }
        
        // Old World - new world
        Lang lang = ResultsFormat.convert(format) ;
        if ( lang != null )
            return ResultSetMgr.read(input, lang) ;

        if (format.equals(ResultsFormat.FMT_RS_JSON))
            return JSONInput.fromJSON(input);

        if (format.equals(ResultsFormat.FMT_RS_TSV))
            return TSVInput.fromTSV(input);

        if (format.equals(ResultsFormat.FMT_RS_CSV))
            return CSVInput.fromCSV(input);

        if (format.equals(ResultsFormat.FMT_RS_BIO))
            return BIOInput.fromBIO(input);

        if (format.equals(ResultsFormat.FMT_RS_XML))
            return ResultSetFactory.fromXML(input);

        if (format.equals(ResultsFormat.FMT_TEXT)) {
            Log.warn(ResultSet.class, "Can't read a text result set");
            throw new ResultSetException("Can't read a text result set");
        }

        if (format.equals(ResultsFormat.FMT_RDF_XML)) {
            Model m = ModelFactory.createDefaultModel();
            m.read(input, null);
            return ResultSetFactory.fromRDF(m);
        }

        if (format.equals(ResultsFormat.FMT_RDF_TTL)) {
            Model m = ModelFactory.createDefaultModel();
            m.read(input, null, "TURTLE");
            return ResultSetFactory.fromRDF(m);
        }

        if (format.equals(ResultsFormat.FMT_RDF_N3)) {
            Model m = ModelFactory.createDefaultModel();
            m.read(input, null, "N3");
            return ResultSetFactory.fromRDF(m);
        }

        if (format.equals(ResultsFormat.FMT_RDF_NT)) {
            Model m = ModelFactory.createDefaultModel();
            m.read(input, null, "N-TRIPLES");
            return ResultSetFactory.fromRDF(m);
        }

        Log.warn(ResultSet.class, "Unknown result set syntax: " + format);
        return null;

    }

    /**
     * Load a result set (or any other model) from file or URL
     * 
     * @param filenameOrURI
     * @return Model
     */

    public static Model loadAsModel(String filenameOrURI) {
        return loadAsModel(null, filenameOrURI, null);
    }

    /**
     * Load a result set (or any other model) from file or URL
     * 
     * @param model
     *            Load into this model (returned)
     * @param filenameOrURI
     * @return Model
     */
    public static Model loadAsModel(Model model, String filenameOrURI) {
        return loadAsModel(model, filenameOrURI, null);
    }

    /**
     * Load a result set (or any other model) from file or URL
     * 
     * @param filenameOrURI
     * @param format
     * @return Model
     */

    public static Model loadAsModel(String filenameOrURI, ResultsFormat format) {
        return loadAsModel(null, filenameOrURI, format);
    }

    /**
     * Load a result set (or any other model) from file or URL. Does not have to
     * be a result set (e.g. CONSTRUCt results) but it does interpret the
     * ResultSetFormat possibilities.
     * 
     * @param model
     *            Load into this model (returned)
     * @param filenameOrURI
     * @param format
     * @return Model
     */

    public static Model loadAsModel(Model model, String filenameOrURI, ResultsFormat format) {
        if (model == null)
            model = GraphFactory.makeDefaultModel();

        if (format == null)
            format = ResultsFormat.guessSyntax(filenameOrURI);

        if (format == null) {
            Log.warn(ResultSet.class, "Null format - defaulting to XML");
            format = ResultsFormat.FMT_RS_XML;
        }

        if (format.equals(ResultsFormat.FMT_TEXT)) {
            Log.fatal(ResultSet.class, "Can't read a text result set");
            throw new ResultSetException("Can't read a text result set");
        }

        if (format.equals(ResultsFormat.FMT_RS_XML) || format.equals(ResultsFormat.FMT_RS_JSON)) {
            InputStream in = null;
            try {
                in = FileManager.get().open(filenameOrURI);
                if (in == null)
                    throw new NotFoundException(filenameOrURI);
            } catch (NotFoundException ex) {
                throw new NotFoundException("File not found: " + filenameOrURI);
            }

            SPARQLResult x = null;

            if (format.equals(ResultsFormat.FMT_RS_JSON))
                x = JSONInput.make(in, GraphFactory.makeDefaultModel());
            else
                x = XMLInput.make(in, GraphFactory.makeDefaultModel());

            if (x.isResultSet())
                ResultSetFormatter.asRDF(model, x.getResultSet());
            else
                ResultSetFormatter.asRDF(model, x.getBooleanResult());

            return model;
        }

        if (ResultsFormat.isRDFGraphSyntax(format))
            return FileManager.get().readModel(model, filenameOrURI);

        Log.fatal(ResultSet.class, "Unknown result set syntax: " + format);
        return null;
    }

    /**
     * Read in any kind of result kind (result set, boolean, graph) Guess the
     * syntax based on filename/URL extension.
     */
    public static SPARQLResult result(String filenameOrURI) {
        return result(filenameOrURI, null);
    }

    /**
     * Read in any kind of result kind (result set, boolean, graph)
     */

    public static SPARQLResult result(String filenameOrURI, ResultsFormat format) {
        if (format == null)
            format = ResultsFormat.guessSyntax(filenameOrURI);

        if (format == null) {
            Log.warn(ResultSet.class, "Null format - defaulting to XML");
            format = ResultsFormat.FMT_RS_XML;
        }

        if (format.equals(ResultsFormat.FMT_TEXT)) {
            Log.fatal(ResultSet.class, "Can't read a text result set");
            throw new ResultSetException("Can't read a text result set");
        }

        if (format.equals(ResultsFormat.FMT_RS_XML) || format.equals(ResultsFormat.FMT_RS_JSON)
            || format.equals(ResultsFormat.FMT_RS_TSV) || format.equals(ResultsFormat.FMT_RS_CSV)) {
            InputStream in = null;
            try {
                in = FileManager.get().open(filenameOrURI);
                if (in == null)
                    throw new NotFoundException(filenameOrURI);
            } catch (NotFoundException ex) {
                throw new NotFoundException("File not found: " + filenameOrURI);
            }

            SPARQLResult x = null;

            if (format.equals(ResultsFormat.FMT_RS_JSON))
                return JSONInput.make(in, GraphFactory.makeDefaultModel());
            else if (format.equals(ResultsFormat.FMT_RS_XML))
                return XMLInput.make(in, GraphFactory.makeDefaultModel());
            else if (format.equals(ResultsFormat.FMT_RS_TSV)) {
                ResultSet rs = TSVInput.fromTSV(in);
                return new SPARQLResult(rs);
            } else if (format.equals(ResultsFormat.FMT_RS_CSV)) {
                ResultSet rs = CSVInput.fromCSV(in);
                return new SPARQLResult(rs);
            } else if (format.equals(ResultsFormat.FMT_RS_BIO)) {
                ResultSet rs = BIOInput.fromBIO(in);
                return new SPARQLResult(rs);
            }
        }

        if (ResultsFormat.isRDFGraphSyntax(format)) {
            Model model = FileManager.get().loadModel(filenameOrURI);
            return new SPARQLResult(model);
        }

        Log.fatal(ResultSet.class, "Unknown result set syntax: " + format);
        return null;
    }

    /**
     * Read XML which is the format of the SPARQL result set format.
     * 
     * @param in
     *            InputStream
     * @return ResultSet
     */
    public static ResultSet fromXML(InputStream in) {
        return XMLInput.fromXML(in);
    }

    /**
     * Read XML which is the format of the SPARQL result set format.
     * 
     * @param str
     *            String to process
     * @return ResultSet
     */
    public static ResultSet fromXML(String str) {
        return XMLInput.fromXML(str);
    }

    /**
     * Turns an RDF model, with properties and classses from the result set
     * vocabulary, into a SPARQL result set. The result set formed is a copy in
     * memory.
     * 
     * @param model
     * @return ResultSet
     */
    public static ResultSet fromRDF(Model model) {
        return new RDFInput(model);
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
        return JSONInput.fromJSON(in);
    }

    /**
     * Read from an input stream which is the format of the SPARQL result set
     * format in TSV.
     * 
     * @param in
     *            InputStream
     * @return ResultSet
     */
    public static ResultSet fromTSV(InputStream in) {
        return TSVInput.fromTSV(in);
    }

    /**
     * Read from an input stream which is the format of the SPARQL result set
     * format in SSE.
     * 
     * @param in
     *            InputStream
     * @return ResultSet
     */
    public static ResultSet fromSSE(InputStream in) {
        Item item = SSE.parse(in);
        Log.warn(ResultSet.class, "Reading SSE result set not full implemented");
        // See SPARQLResult. Have a level of ResultSetFactory that does
        // "get SPARQLResult".
        // Or just boolean/result set because those are both srx. etc.

        BuilderTable.build(item);
        return null;
    }

    /**
     * Turns an RDF model, with properties and classses from the result set
     * vocabulary, into a SPARQL result set. The result set formed is a copy in
     * memory.
     * 
     * @param model
     * @return ResultSet
     */
    static public ResultSet makeResults(Model model) {
        return new RDFInput(model);
    }

    /**
     * Turns an RDF model, with properties and classses from the result set
     * vocabulary, into a SPARQL result set which is rewindable (has a
     * .reset()operation). The result set formed is a copy in memory.
     * 
     * @param model
     * @return ResultSetRewindable
     */
    static public ResultSetRewindable makeRewindable(Model model) {
        return new RDFInput(model);
    }

    /**
     * Turn an existing result set into a rewindable one.
     * May take a copy but this is not guarantted
     * Uses up the result set passed in which is no longer valid as a ResultSet.
     * 
     * @param resultSet
     * @return ResultSetRewindable
     */
    static public ResultSetRewindable makeRewindable(ResultSet resultSet) {
        if ( resultSet instanceof ResultSetRewindable )
        {
            ResultSetRewindable rsw = (ResultSetRewindable)resultSet ;
            rsw.reset() ;
            return rsw ;
        }
        return new ResultSetMem(resultSet);
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
     * </p> Note that rewindable results may typically also be peekable so may
     * be more broadly applicable if you can afford the cost of loading all the
     * results into memory. </p>
     * 
     * @param resultSet
     *            Result set to wrap
     * @return Peekable results
     */
    static public ResultSetPeekable makePeekable(ResultSet resultSet) {
        return new ResultSetPeeking(resultSet);
    }

    /**
     * Take a copy of a result set - the result set returns is an in-memory
     * copy. It is not attached to the original query execution object which can
     * be closed.
     * 
     * @param results
     * @return ResultSet
     */
    static public ResultSetRewindable copyResults(ResultSet results) {
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
    static public ResultSet create(QueryIterator queryIterator, List<String> vars) {
        return new ResultSetStream(vars, null, queryIterator);
    }
}
