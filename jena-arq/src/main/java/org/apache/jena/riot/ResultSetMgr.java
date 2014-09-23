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

import java.io.InputStream ;
import java.io.OutputStream ;

import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.resultset.* ;

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.sparql.util.Context ;

/** 
 * Reading and writing of Result Sets.
 * {@linkplain ResultSetFormatter} provides output to text.
 * @see ResultSetFactory
 * @see ResultSetFormatter 
 */
public class ResultSetMgr {
    static { 
        ResultSetLang.init() ;
    }
    
    /**
     * Read from an {@code InputStream} and produce a {@linkplain ResultSet};
     * the stream is expect to use syntax {@code lang}.  Note that returned
     * result set may stream and so the input stream be read while the ResultSet is used.
     * See {@linkplain ResultSetFactory#copyResults(ResultSet)}
     * for a ResultSet that is detached from the {@code InputStream}.
     * 
     * @param in
     * @param lang
     * @return ResultSet
     */
    public static ResultSet read(InputStream in, Lang lang) {
        return process(new TypedInputStream(in), null, lang, null) ;
    }
    
    /** Read a result set from the URI */
    public static ResultSet read(String uri) {
        return read(uri, null) ;
    }
    
    /** Read a result set from the URI, in the speficied syntax */ 
    public static ResultSet read(String uri, Lang lang) {
        return parse(uri, lang, null) ;
    }

//    /**
//     * Read from an {@code Reader} and produce a {@linkplain ResultSet};
//     * the stream is expect to use syntax {@code lang}.  
//     * Using InputStreams is better to ensure the character set
//     * of the input matches that of the syntax.  
//     * Note that returned
//     * result set may stream and so the input stream be read while the ResultSet is used.
//     * See {@linkplain ResultSetFactory#copyResults(ResultSet)}
//     * for a ResultSet that is detached from the {@code InputStream}.
//     * 
//     * @param in
//     * @param lang
//     * @return ResultSet
//     */
//    @Deprecated
//    public static ResultSet read(Reader in, Lang lang) {
//        ResultSetReaderFactory f = ResultSetReaderRegistry.getFactory(lang) ;
//        if ( f == null )
//            throw new RiotException("No result set reader for "+lang) ;
//        ResultSetReader rsr = f.create(lang) ;
//        return rsr.read(in, ARQ.getContext()) ;
//    }
//    
//    
//    /**
//     * Read from an {@code StringReader} and produce a {@linkplain ResultSet};
//     * the stream is expect to use syntax {@code lang}.  
//     * Note that returned
//     * result set may stream and so the input stream be read while the ResultSet is used.
//     * See {@linkplain ResultSetFactory#copyResults(ResultSet)}
//     * for a ResultSet that is detached from the {@code InputStream}.
//     * 
//     * @param in
//     * @param lang
//     * @return ResultSet
//     */
//    public static ResultSet read(StringReader in, Lang lang) {
//        ResultSetReaderFactory f = ResultSetReaderRegistry.getFactory(lang) ;
//        if ( f == null )
//            throw new RiotException("No result set reader for "+lang) ;
//        ResultSetReader rsr = f.create(lang) ;
//        return rsr.read(in, ARQ.getContext()) ;
//    }
    
    /** Read ResultSet.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
     */
    public static ResultSet parse(String uri, Lang hintLang, Context context)
    {
        // Conneg
        if ( uri == null )
            throw new IllegalArgumentException("URI to read from is null") ;
        if ( hintLang == null )
            hintLang = RDFLanguages.filenameToLang(uri) ;
        TypedInputStream in = RDFDataMgr.open(uri, context) ;
        if ( in == null )
            throw new RiotException("Not found: "+uri) ;
        return process(in, uri, hintLang, context) ;
    }
        
    private static ResultSet process(TypedInputStream in, String srcURI, Lang hintLang, Context context) {
        ContentType ct = WebContent.determineCT(in.getContentType(), hintLang, srcURI) ;
        if ( ct == null )
            throw new RiotException("Failed to determine the content type: (URI="+srcURI+" : stream="+in.getContentType()+" : hint="+hintLang+")") ;
        ResultSetReader reader = getReader(ct) ;
        if ( reader == null )
            throw new RiotException("No parser registered for content type: "+ct.getContentType()) ;
        return reader.read(in, context) ;
    }
    
    private static ResultSetReader getReader(ContentType ct)
    {
        Lang lang = RDFLanguages.contentTypeToLang(ct) ;
        if ( lang == null )
            return null ;
        ResultSetReaderFactory r = ResultSetReaderRegistry.getFactory(lang) ;
        if ( r == null )
            return null ;
        return r.create(lang) ;
    }
    
    // -------------------------------

    /** Write a SPARQL result set to the output stream in the speciifcied language/syntax.
     * @param out
     * @param resultSet
     * @param lang
     */
    public static void write(OutputStream out, ResultSet resultSet, Lang lang) { 
        ResultSetWriterFactory f = ResultSetWriterRegistry.lookup(lang) ;
        if ( f == null )
            throw new RiotException("No resultSet writer for "+lang) ;
        f.create(lang).write(out, resultSet, null) ;
    }
    
//    /** Write a SPARQL result set to the {@link java.io.Writer} in the speciifcied language/syntax.
//     * Using {@link OutputStream}s is better because the charcater encoding will match the
//     * requirements of the language.   
//     * @param out
//     * @param resultSet
//     * @param lang
//     */
//    @Deprecated
//    public static void write(Writer out, ResultSet resultSet, Lang lang) { 
//        ResultSetWriterFactory f = ResultSetWriterRegistry.lookup(lang) ;
//        if ( f == null )
//            throw new RiotException("No resultSet writer for "+lang) ;
//        f.create(lang).write(out, resultSet, null) ;
//    }
//
//    /** Write a SPARQL result set to the {@link java.io.Writer} in the speciifcied language/syntax.
//     * @param out
//     * @param resultSet
//     * @param lang
//     */
//    public static void write(StringWriter out, ResultSet resultSet, Lang lang) { 
//        ResultSetWriterFactory f = ResultSetWriterRegistry.lookup(lang) ;
//        if ( f == null )
//            throw new RiotException("No resultSet writer for "+lang) ;
//        f.create(lang).write(out, resultSet, null) ;
//    }

}

