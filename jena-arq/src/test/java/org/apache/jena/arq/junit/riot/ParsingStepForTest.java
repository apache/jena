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

package org.apache.jena.arq.junit.riot;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.jena.riot.*;
import org.apache.jena.riot.system.*;

/**
 * Manage the setup of parsing used for tests.
 */
public class ParsingStepForTest {
    /*
     * Ensure that the parser used in a test is decided early, not at the point of
     * running the test. The environment can be set in BeforeAll/AfterAll and tests
     * created by {@code TestFactory} so the environment choice is not delayed until the
     * test is run.
     */

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

    /**
     * Create a function that builds and runs the parser.
     */
    public static Consumer<StreamRDF> parse(String uri, Lang lang, boolean ignoreWarnings) {
        return parse(uri, uri, lang, ignoreWarnings);
    }

    /**
     * Create a function that builds and runs the parser.
     */
    public static Consumer<StreamRDF> parse(String source, String baseURI, Lang lang, boolean silentWarnings) {
        ErrorHandler errorHandlerTest = silentWarnings
                ? ErrorHandlerFactory.errorHandlerIgnoreWarnings(null)
                : ErrorHandlerFactory.errorHandlerStrictSilent();

        if ( alternativeReaderFactories.containsKey(lang) ) {
            ReaderRIOTFactory factoryForTest = alternativeReaderFactories.get(lang);
            InputStream in = RDFDataMgr.open(source);
            ParserProfile profile = RiotLib.profile(lang, baseURI, errorHandlerTest);
            // Function that uses the registered alternative parser factory.
            return (StreamRDF destination)->
                    factoryForTest.create(lang, profile).read(in, baseURI, null, destination, RIOT.getContext());
        }

        // Otherwise use the normal RDFParser builder.
        return (StreamRDF destination)->
                        RDFParser.create()
                            .errorHandler(errorHandlerTest)
                            .strict(true)
                            .forceLang(lang)
                            .source(source)
                            .base(baseURI)
                            .build()
                            .parse(destination);
    }
}
