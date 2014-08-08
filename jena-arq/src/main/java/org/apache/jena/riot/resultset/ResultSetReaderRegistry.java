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

import static org.apache.jena.riot.resultset.ResultSetLang.SPARQLResultSetCSV ;
import static org.apache.jena.riot.resultset.ResultSetLang.SPARQLResultSetJSON ;
import static org.apache.jena.riot.resultset.ResultSetLang.SPARQLResultSetTSV ;
import static org.apache.jena.riot.resultset.ResultSetLang.SPARQLResultSetThrift ;
import static org.apache.jena.riot.resultset.ResultSetLang.SPARQLResultSetXML ;

import java.io.InputStream ;
import java.io.Reader ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.Objects ;

import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RiotException ;

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.sparql.resultset.CSVInput ;
import com.hp.hpl.jena.sparql.resultset.JSONInput ;
import com.hp.hpl.jena.sparql.resultset.XMLInput ;
import com.hp.hpl.jena.sparql.util.Context ;

public class ResultSetReaderRegistry {
    
    /** Lookup a {@linkplain Lang} to get the registered {@linkplain ResultSetReaderFactory} (or null) */
    public static ResultSetReaderFactory lookup(Lang lang) {
        Objects.requireNonNull(lang) ;
        return registry.get(lang) ;
    }

    /** Register a {@linkplain ResultSetReaderFactory} for a {@linkplain Lang} */
    public static void register(Lang lang, ResultSetReaderFactory factory) {
        Objects.requireNonNull(lang) ;
        Objects.requireNonNull(factory) ;
        registry.put(lang, factory) ;
    }

    private static Map<Lang, ResultSetReaderFactory> registry = new HashMap<>() ;
    static { init(); }
    
    /*package*/ static void init() {
        ResultSetReaderFactory factory = new ResultSetReaderFactoryStd() ;
        register(SPARQLResultSetXML, factory) ;
        register(SPARQLResultSetJSON, factory) ;
        register(SPARQLResultSetCSV, factory) ;
        register(SPARQLResultSetTSV, factory) ;
        register(SPARQLResultSetThrift, factory) ;
    }
    
    static { init(); }

    private static class ResultSetReaderFactoryStd implements ResultSetReaderFactory {
        @Override
        public ResultSetReader create(Lang lang) {
            lang = Objects.requireNonNull(lang, "Language must not be null") ;
            if ( lang.equals(SPARQLResultSetXML) )      return readerXML ;
            if ( lang.equals(SPARQLResultSetJSON) )     return readerJSON ;
            if ( lang.equals(SPARQLResultSetCSV) )      return readerCSV ;
            if ( lang.equals(SPARQLResultSetTSV) )      return readerTSV ;
            throw new RiotException("Lang not registered (ResultSet reader)") ;
        }
    }
    
    private static ResultSetReader readerXML = new ResultSetReader() {
        @Override public ResultSet read(InputStream in, Context context)    { return XMLInput.fromXML(in); }
        @Override public ResultSet read(Reader in, Context context)         { return null ; } 
    } ;

    private static ResultSetReader readerJSON = new ResultSetReader() {
        @Override public ResultSet read(InputStream in, Context context)    { return JSONInput.fromJSON(in) ; }
        @Override public ResultSet read(Reader in, Context context)         { return null ; }
    } ;

    private static ResultSetReader readerCSV = new ResultSetReader() {
        @Override public ResultSet read(InputStream in, Context context)    { return CSVInput.fromCSV(in) ; }
        @Override public ResultSet read(Reader in, Context context)         { return null ; }
    } ;
    
    private static ResultSetReader readerTSV = new ResultSetReader() {
        @Override public ResultSet read(InputStream in, Context context)    { return ResultSetFactory.fromTSV(in) ; }
        @Override public ResultSet read(Reader in, Context context)         { return null ; }
    } ;

    private static ResultSetReader readerNo = new ResultSetReader() {
        @Override public ResultSet read(InputStream in, Context context)    { return null ; }
        @Override public ResultSet read(Reader in, Context context)         { return null ; }
    } ;
}
