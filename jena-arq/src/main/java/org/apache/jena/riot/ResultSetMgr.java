/**
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

package org.apache.jena.riot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream ;
import java.io.OutputStream ;
import java.util.Objects;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.query.ResultSetFactory ;
import org.apache.jena.query.ResultSetFormatter ;
import org.apache.jena.riot.resultset.ResultSetReaderRegistry;
import org.apache.jena.riot.resultset.rw.ResultsReader;
import org.apache.jena.riot.resultset.rw.ResultsWriter;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.resultset.SPARQLResult;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sys.JenaSystem;

/** 
 * Reading and writing of Result Sets.
 * {@link ResultSetFormatter} provides output to text.
 * @see ResultSetFactory
 * @see ResultSetFormatter 
 */
public class ResultSetMgr {
    static { JenaSystem.init(); }
    
    /**
     * Read from a {@code URL} (including filenames) and produce a {@link ResultSet}.
     * Note that returned result set may stream and so the input stream be read
     * while the ResultSet is used.
     * <p>
     * See {@link ResultSetFactory#copyResults(ResultSet)}
     * for a ResultSet that is detached from the {@code InputStream}.
     * 
     * @param urlOrFilename
     * @return ResultSet
     */
    public static ResultSet read(String urlOrFilename) {
        ResultSet rs = readAny(urlOrFilename).getResultSet();
        if ( rs == null )
            throw new ResultSetException("Not a result set"); 
        return rs;
    }


    /**
     * Read from a {@code URL} (including filenames) and produce a {@link ResultSet};
     * the stream is expect to use syntax {@code lang}.  Note that returned
     * result set may stream and so the input stream be read while the ResultSet is used.
     * See {@link ResultSetFactory#copyResults(ResultSet)}
     * for a ResultSet that is detached from the {@code InputStream}.
     * 
     * @param urlOrFilename
     * @param lang
     * @return ResultSet
     */
    public static ResultSet read(String urlOrFilename, Lang lang) {
        ResultSet rs = readAny(urlOrFilename, lang).getResultSet();
        if ( rs == null )
            throw new ResultSetException("Not a result set"); 
        return rs;
    }

    /**
     * Read from a {@code URL} (including filenames) and produce a {@link ResultSet}.
     * Note that returned result set may stream and so the input stream be read
     * while the ResultSet is used.
     * <p>
     * See {@link ResultSetFactory#copyResults(ResultSet)}
     * for a ResultSet that is detached from the {@code InputStream}.
     * 
     * @param input
     * @return ResultSet
     */
    public static ResultSet read(InputStream input) {
        ResultSet rs = readAny(input).getResultSet();
        if ( rs == null )
            throw new ResultSetException("Not a result set"); 
        return rs;
    }

    /**
     * Read from an {@code InputStream} and produce a {@link ResultSet};
     * the stream is expect to use syntax {@code lang}.  Note that returned
     * result set may stream and so the input stream be read while the ResultSet is used.
     * See {@link ResultSetFactory#copyResults(ResultSet)}
     * for a ResultSet that is detached from the {@code InputStream}.
     * 
     * @param input
     * @param lang
     * @return ResultSet
     */
    public static ResultSet read(InputStream input, Lang lang) {
        ResultSet rs = readAny(input, lang).getResultSet();
        if ( rs == null )
            throw new ResultSetException("Not a result set"); 
        return rs;
    }

    private static void checkLang(Lang lang) {
        Objects.requireNonNull(lang);
        if ( ! ResultSetReaderRegistry.isRegistered(lang) ) {
            throw new ResultSetException("Not a result set syntax: "+lang);
        }
    }
    
    /** Read a boolean result from the URI
     * 
     * @param urlOrFilename
     * @return boolean
     */
    public static boolean readBoolean(String urlOrFilename) {
        Boolean b = readAny(urlOrFilename).getBooleanResult();
        return b;
    }
    
    /** Read a boolean result from the URI;
     * the input is expect to use syntax {@code lang}
     * 
     * @param urlOrFilename
     * @param lang
     * @return boolean
     */
    public static boolean readBoolean(String urlOrFilename, Lang lang) {
        Boolean b = readAny(urlOrFilename, lang).getBooleanResult();
        return b;
    }
    
    /** Read a boolean result from the URI
     * 
     * @param input
     * @return boolean
     */
    public static boolean readBoolean(InputStream input) {
        Boolean b = readAny(input).getBooleanResult();
        return b;
    }
    
    /** Read a boolean result from the URI;
     * the input is expect to use syntax {@code lang}
     * 
     * @param input
     * @param lang
     * @return boolean
     */
    public static boolean readBoolean(InputStream input, Lang lang) {
        Boolean b = readAny(input, lang).getBooleanResult();
        return b;
    }

    private static SPARQLResult readAny(String url) {
        return ResultsReader.create().build().readAny(url);
    }

    private static SPARQLResult readAny(String url, Lang lang) {
        checkLang(lang);
        return ResultsReader.create()
            .lang(lang)
            .build()
            .readAny(url);
    }
    
    private static SPARQLResult readAny(InputStream input) {
        return ResultsReader.create().build().readAny(input);
    }

    private static SPARQLResult readAny(InputStream input, Lang lang) {
        checkLang(lang);
        return ResultsReader.create()
            .lang(lang)
            .build()
            .readAny(input);
    }
    // -------------------------------
    
    /** Read ResultSet.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
     */
    public static ResultSet parse(String uri, Lang hintLang, Context context) {
        ResultSet rs = ResultsReader.create().lang(hintLang).context(context).read(uri);
        if ( rs == null )
            throw new ResultSetException("Not a result set"); 
        return rs;
    }

    // -------------------------------
    
    /** Write a SPARQL result set to the output stream in the specified language/syntax.
     * @param output
     * @param resultSet
     * @param lang
     */
    public static void write(OutputStream output, ResultSet resultSet, Lang lang) {
        Objects.requireNonNull(lang);
        ResultsWriter.create()
            .lang(lang)
            .write(output, resultSet);
    }

    /** Write a SPARQL boolean result to the output stream in the specified language/syntax.
     * @param output
     * @param result
     * @param lang
     */
    public static void write(OutputStream output, boolean result, Lang lang) {
        Objects.requireNonNull(lang);
        ResultsWriter.create()
            .lang(lang)
            .build()
            .write(output, result);
    }
    
    /** Generate a string in the specified language/syntax for a SPARQL result set.
     * @param resultSet
     * @param lang
     */
    public static String asString(ResultSet resultSet, Lang lang) {
        Objects.requireNonNull(lang);
        ByteArrayOutputStream output = new ByteArrayOutputStream(1000); 
        ResultsWriter.create()
            .lang(lang)
            .write(output, resultSet);
        return StrUtils.fromUTF8bytes(output.toByteArray());
    }

    /** Generate a string in the specified language/syntax for a SPARQL boolean result.
     * @param result
     * @param lang
     */
    public static String asString(boolean result, Lang lang) {
        Objects.requireNonNull(lang);
        ByteArrayOutputStream output = new ByteArrayOutputStream(1000); 
        ResultsWriter.create()
            .lang(lang)
            .build()
            .write(output, result);
        return StrUtils.fromUTF8bytes(output.toByteArray());
    }

}

