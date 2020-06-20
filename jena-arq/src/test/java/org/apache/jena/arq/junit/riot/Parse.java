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

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.StreamRDF;

public class Parse {

    private static ErrorHandler errorHandlerTestStrict = ErrorHandlerFactory.errorHandlerStrictSilent();

    public static void parse(StreamRDF destination, String uri, Lang lang) {
        parse(destination, uri, uri, lang);
    }

    public static void parse(StreamRDF destination, String uri, String base, Lang lang) {
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
