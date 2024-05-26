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

package org.apache.jena.riot.lang;

import org.apache.jena.riot.ErrorHandlerTestLib.ErrorHandlerEx ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.system.StreamRDFLib ;

/** Helper code for RIOT language parsing tests. */
class ParserTests {

    // Must end in / for easy use in tests
    static String BASE = "http://base/";

    /** Setup parser for the tests: base "http://base/" and ErrorHandlerEx. */
    static RDFParserBuilder parser() {
        String baseIRI = BASE;
        return RDFParser.create()
                .base(baseIRI)
                .errorHandler(new ErrorHandlerEx());
    }

    /** Parse for a language - convert errors.wranigns to ErrorHandlerEx */
    static long parseCount(Lang lang, String string) {
        StreamRDFCounting dest = StreamRDFLib.count() ;
        parser().fromString(string).lang(lang).parse(dest);
        return dest.count() ;
    }
}

