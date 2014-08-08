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

package org.apache.jena.riot.resultset;

import java.io.InputStream ;
import java.io.OutputStream ;

import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RiotException ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.sparql.util.Context ;

/** 
 * Reading and writing of Result Sets 
 */
public class ResultSetMgr {
    static { 
        
        ResultSetReaderRegistry.init() ;
        ResultSetWriterRegistry.init() ;
    }
    
    // Register with RDFLanguages.
    
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
        ResultSetReaderFactory f = ResultSetReaderRegistry.lookup(lang) ;
        if ( f == null )
            throw new RiotException("No result set reader for "+lang) ;
        ResultSetReader rsr = f.create(lang) ;
        return rsr.read(in, ARQ.getContext()) ;
    }
    
    public static ResultSet read(String uri) {
        return read(uri, null) ;
    }
    
    public static ResultSet read(String uri, Lang lang) {
        return parse(uri, lang, null) ;
    }

    /** Read ResultSet.
     * @param uri       URI to read from (includes file: and a plain file name).
     * @param hintLang  Hint for the syntax
     * @param context   Content object to control reading process.
     */
    public static ResultSet parse(String uri, Lang hintLang, Context context)
    {
        if ( uri == null )
            throw new IllegalArgumentException("URI to read from is null") ;
        if ( hintLang == null )
            hintLang = RDFLanguages.filenameToLang(uri) ;
        TypedInputStream in = RDFDataMgr.open(uri, context) ;
        if ( in == null )
            throw new RiotException("Not found: "+uri) ;
        //ct -> lang
        return process(in, hintLang) ;
    }
    // Read from URL.

    private static ResultSet process(InputStream in, Lang lang) {
        ResultSetReaderFactory f = ResultSetReaderRegistry.lookup(lang) ;
        if ( f == null )
            throw new RiotException("No result set reader for "+lang) ;
        ResultSetReader rsr = f.create(lang) ;
        return rsr.read(in, ARQ.getContext()) ;
    }
 
    // ----
    
    public static void write(OutputStream out, ResultSet resultSet, Lang lang) { 
        ResultSetWriterFactory f = ResultSetWriterRegistry.lookup(lang) ;
        f.create(lang).write(out, resultSet, null) ;
    }
}

