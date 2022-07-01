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

package org.apache.jena.riot.lang.extra;

import org.apache.jena.riot.*;

public class TurtleJCC {

    // Must be a different content type.
    // Must be a different file extension.
    public static Lang TTLJCC = LangBuilder.create("TurtleJavaCC", "text/turtle-jcc")
                                        .addAltNames("ttljcc")
                                        .addFileExtensions("ttljcc")
                                        .build();
    public static ReaderRIOTFactory factory = (lang, profile) -> new TurtleJavaccReaderRIOT(profile) ;

    public static void register() {
        RDFLanguages.register(TTLJCC);
        RDFParserRegistry.registerLangTriples(TTLJCC, factory);
    }

    public static void unregister() {
        RDFParserRegistry.removeRegistration(TTLJCC);
        RDFLanguages.unregister(TTLJCC);
    }
}
