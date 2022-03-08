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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.rowset.rw.*;

public class RowSetReaderRegistry {

    /** Lookup a {@link Lang} to get the registered {@link RowSetReaderFactory} (or null) */
    public static RowSetReaderFactory getFactory(Lang lang) {
        Objects.requireNonNull(lang);
        return registry.get(lang);
    }

    /** Register a {@link RowSetReaderFactory} for a {@link Lang} */
    public static void register(Lang lang, RowSetReaderFactory factory) {
        Objects.requireNonNull(lang);
        Objects.requireNonNull(factory);
        registry.put(lang, factory);
    }

    /** Test whether {@link Lang} is registered as a result set syntax. */
    public static boolean isRegistered(Lang lang) {
        Objects.requireNonNull(lang);
        return registry.containsKey(lang);
    }

    private static Map<Lang, RowSetReaderFactory> registry = new HashMap<>();

    private static boolean initialized = false;
    public static void init() {
        if ( initialized )
            return;
        initialized = true;

        register(RS_XML,        RowSetReaderXML.factory);
        // register(RS_JSON,       RowSetReaderJSON.factory);
        register(RS_JSON,       RowSetReaderJSONStreaming.factory); // Experimental! JENA-2302

        register(RS_CSV,        RowSetReaderCSV.factory);
        register(RS_TSV,        RowSetReaderTSV.factory);

        register(RS_Thrift,     RowSetReaderThrift.factory);
        register(RS_Protobuf,   RowSetReaderProtobuf.factory);

        register(RS_None,       RowSetReaderNone.factory);
    };
}
