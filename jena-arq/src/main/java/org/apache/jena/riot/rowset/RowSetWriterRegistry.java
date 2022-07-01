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

package org.apache.jena.riot.rowset;

import static org.apache.jena.riot.resultset.ResultSetLang.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.rowset.rw.*;

/** Registry for RowSetWriter factories. */
public class RowSetWriterRegistry {

    private static Map<Lang, RowSetWriterFactory> registry = new ConcurrentHashMap<>();

    /** Lookup a {@link Lang} to get the registered {@link RowSetReaderFactory} (or null) */
    public static RowSetWriterFactory getFactory(Lang lang) {
        Objects.requireNonNull(lang);
        return registry.get(lang);
    }

    public static boolean isRegistered(Lang lang) {
        Objects.requireNonNull(lang);
        return registry.containsKey(lang);
    }

    /** Register a {@link RowSetReaderFactory} for a {@link Lang} */
    public static void register(Lang lang, RowSetWriterFactory factory) {
        Objects.requireNonNull(lang);
        Objects.requireNonNull(factory);
        registry.put(lang, factory);
    }

    private static boolean initialized = false;
    public static void init() {
        if ( initialized )
            return;
        initialized = true;

        register(RS_XML,        RowSetWriterXML.factory);
        register(RS_JSON,       RowSetWriterJSON.factory);

        register(RS_CSV,        RowSetWriterCSV.factory);
        register(RS_TSV,        RowSetWriterTSV.factory);

        register(RS_Thrift,     RowSetWriterThrift.factory);
        register(RS_Protobuf,   RowSetWriterProtobuf.factory);

        register(RS_Text,       RowSetWriterText.factory);
        register(RS_None,       RowSetWriterNone.factory);
    }
}

