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

package org.apache.jena.shacl.compact;

import java.io.InputStream;
import java.io.Reader;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.sparql.util.Context;

/** SHACL Compact Syntax setup */
public class SHACLC {

    public static Lang langShacl = LangBuilder.create("SHACLC", "text/shaclc")
        .addAltNames("shaclc")
        .addFileExtensions("shaclc", "shc")
        .build();

    public static void init() {
         RDFLanguages.register(langShacl);
         ReaderRIOTFactory factory = (Lang language, ParserProfile profile)->new ReaderRIOTShaclc();
         RDFParserRegistry.registerLangTriples(langShacl, factory);
    }

    static class ReaderRIOTShaclc implements ReaderRIOT {
        @Override
        public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
            Graph g = ShaclcParser.parseSHACLC(reader, baseURI);
            StreamRDFOps.sendGraphToStream(g, output);
        }

        @Override
        public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
            Graph g = ShaclcParser.parseSHACLC(in, baseURI);
            StreamRDFOps.sendGraphToStream(g, output);
        }
    }

}

