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

import java.util.Map ;
import java.util.Objects ;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.resultset.rw.*;

/** Registry for ResultSetWriter factories. */
public class ResultSetWriterRegistry {

    private static Map<Lang, ResultSetWriterFactory> registry = new ConcurrentHashMap<>() ;

    /** Lookup a {@link Lang} to get the registered {@link ResultSetReaderFactory} (or null) */
    public static ResultSetWriterFactory getFactory(Lang lang) {
        Objects.requireNonNull(lang) ;
        return registry.get(lang) ;
    }

    public static boolean isRegistered(Lang lang) {
        Objects.requireNonNull(lang) ;
        return registry.containsKey(lang) ;
    }

    /** Register a {@link ResultSetWriterFactory} for a {@link Lang} */
    public static void register(Lang lang, ResultSetWriterFactory factory) {
        Objects.requireNonNull(lang) ;
        Objects.requireNonNull(factory) ;
        registry.put(lang, factory) ;
    }

    private static boolean initialized = false ;
    public static void init() {
        if ( initialized )
            return ;
        initialized = true ;

        register(RS_XML,    ResultSetWriterXML.factory) ;
        register(RS_JSON,   ResultSetWriterJSON.factory) ;
        register(RS_Thrift, ResultSetWriterThrift.factory) ;
        register(RS_CSV,    ResultSetWriterCSV.factory) ;
        register(RS_TSV,    ResultSetWriterTSV.factory) ;
        // Build-in std factory (below).
        register(RS_Text,   ResultSetWriterText.factory) ;
        register(RS_None,   ResultSetWriterNone.factory) ;
    }
}

