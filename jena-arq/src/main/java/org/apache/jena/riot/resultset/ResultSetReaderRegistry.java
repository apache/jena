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

import static org.apache.jena.riot.resultset.ResultSetLang.*;

import java.io.InputStream ;
import java.io.Reader ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.Objects ;

import org.apache.jena.query.ResultSet ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.resultset.rw.*;
import org.apache.jena.sparql.resultset.SPARQLResult;
import org.apache.jena.sparql.util.Context ;

public class ResultSetReaderRegistry {

    /** Lookup a {@link Lang} to get the registered {@link ResultSetReaderFactory} (or null) */
    public static ResultSetReaderFactory getFactory(Lang lang) {
        Objects.requireNonNull(lang) ;
        return registry.get(lang) ;
    }

    /** Register a {@link ResultSetReaderFactory} for a {@link Lang} */
    public static void register(Lang lang, ResultSetReaderFactory factory) {
        Objects.requireNonNull(lang) ;
        Objects.requireNonNull(factory) ;
        registry.put(lang, factory) ;
    }

    /** Test whether {@link Lang} is registered as a result set syntax. */
    public static boolean isRegistered(Lang lang) {
        Objects.requireNonNull(lang) ;
        return registry.containsKey(lang);
    }

    private static Map<Lang, ResultSetReaderFactory> registry = new HashMap<>() ;

    private static boolean initialized = false ;
    public static void init() {
        if ( initialized )
            return ;
        initialized = true ;

        register(RS_XML,    ResultSetReaderXML.factory) ;
        register(RS_JSON,   ResultSetReaderJSON.factory) ;
        register(RS_Thrift, ResultSetReaderThrift.factory) ;
        register(RS_CSV,    ResultSetReaderCSV.factory) ;
        register(RS_TSV,    ResultSetReaderTSV.factory) ;
    }

    private static class ResultSetReaderNone implements ResultSetReader {
        @Override public ResultSet read(InputStream in, Context context)       { return null ; }
        @Override public ResultSet read(Reader in, Context context)            { return null ; }
        @Override public SPARQLResult readAny(InputStream in, Context context) { return null ; }
    } ;
}
