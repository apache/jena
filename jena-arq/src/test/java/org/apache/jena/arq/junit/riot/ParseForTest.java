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

import org.apache.jena.riot.*;
import org.apache.jena.riot.system.*;

public class ParseForTest {

    private static ErrorHandler errorHandlerTestStrict = ErrorHandlerFactory.errorHandlerStrictSilent();

    public static void parse(StreamRDF destination, String uri, Lang lang) {
        parse(destination, uri, uri, lang);
    }

    public static Map<Lang, ReaderRIOTFactory> alternativeReaderFactories = new ConcurrentHashMap<>();
    
    public static void parse(StreamRDF destination, String uri, String base, Lang lang) {
        if ( alternativeReaderFactories.containsKey(lang) ) {
            ReaderRIOTFactory factoryForTest = alternativeReaderFactories.get(lang);
            InputStream in = RDFDataMgr.open(uri);
            ParserProfile profile = RiotLib.profile(lang, base, errorHandlerTestStrict);
            factoryForTest.create(lang, profile).read(in, base, null, destination, RIOT.getContext());
            return ;
        }
        // Otherwise use the RDFParser builder.
        RDFParser.create()
            .errorHandler(errorHandlerTestStrict)
            .strict(true)
            .forceLang(lang)
            .source(uri)
            .base(base)
            .build()
            .parse(destination);
    }
}
