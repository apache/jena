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

package org.apache.jena.arq.junit4.riot;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.riot.*;
import org.apache.jena.riot.system.*;

/**
 * Manage parsers used for tests, separate from the overall system setup.
 */
public class ParseForTest {

    public static void parse(StreamRDF destination, String uri, Lang lang, boolean ignoreWarnings) {
        parse(destination, uri, uri, lang, ignoreWarnings);
    }

    /**
     * Map of {@link Lang} to {@link ReaderRIOTFactory} that is consulted before
     * defaulting to the standard system parser.
     */
    private static Map<Lang, ReaderRIOTFactory> alternativeReaderFactories = new ConcurrentHashMap<>();

    /**
     * Add an alternative language implementation to
     * {@link #alternativeReaderFactories} map. This map of {@link Lang} to
     * {@link ReaderRIOTFactory} is consulted before defaulting to the standard
     * system parser.
     */
    public static void registerAlternative(Lang lang, ReaderRIOTFactory factory) {
        alternativeReaderFactories.put(lang, factory);
    }

    /**
     * Remove an registration of an alternative for {@link Lang}.
     */
    public static void unregisterAlternative(Lang lang) {
        alternativeReaderFactories.remove(lang);
    }

    public static void parse(StreamRDF destination, String uri, String base, Lang lang, boolean ignoreWarnings) {

        ErrorHandler errorHandlerTest = ignoreWarnings
                ? ErrorHandlerFactory.errorHandlerIgnoreWarnings(null)
                : ErrorHandlerFactory.errorHandlerStrictSilent();

        if ( alternativeReaderFactories.containsKey(lang) ) {
            ReaderRIOTFactory factoryForTest = alternativeReaderFactories.get(lang);
            InputStream in = RDFDataMgr.open(uri);
            ParserProfile profile = RiotLib.profile(lang, base, errorHandlerTest);
            factoryForTest.create(lang, profile).read(in, base, null, destination, RIOT.getContext());
            return ;
        }
        // Otherwise use the RDFParser builder.
        RDFParser.create()
            .errorHandler(errorHandlerTest)
            .strict(true)
            .forceLang(lang)
            .source(uri)
            .base(base)
            .build()
            .parse(destination);
    }
}
