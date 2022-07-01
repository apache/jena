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

package arq.examples.riot;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.*;
import org.apache.jena.riot.adapters.RDFReaderRIOT;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.sse.Item;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.sse.builders.BuilderGraph;
import org.apache.jena.sparql.util.Context;

/** Example of using RIOT : register a new input language */
public class ExRIOT6_AddNewReader
{
    static { LogCtl.setLogging(); }

    public static void main(String... argv) {
        Lang lang = LangBuilder.create("SSE", "text/x-sse").addFileExtensions("rsse").build();
        // This just registers the name, not the parser.
        RDFLanguages.register(lang);

        // Register the parser factory.
        ReaderRIOTFactory factory = new SSEReaderFactory();
        RDFParserRegistry.registerLangTriples(lang, factory);

        // use it ... inline data
        String x = "(graph (<s> <p1> 123) (<s> <p2> 456) )";
        // Model.read( , "SSE")
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new StringReader(x), "http://example/", lang);
        // print results.
        RDFDataMgr.write(System.out, model, Lang.TTL);
    }

    static class SSEReaderFactory implements ReaderRIOTFactory
    {
        @Override
        public ReaderRIOT create(Lang language, ParserProfile profile) {
            return new SSEReader();
        }
    }

    static class SSEReader implements ReaderRIOT
    {
        // This is just an example - it reads a graph in
        // http://jena.apache.org/documentation/notes/sse.html
        // format. It is not a streaming parser; it creates some triples,
        // then send them to the output. This style might be useful for creating
        // triples from a converter process or program.

        @Override
        public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
            Item item = SSE.parse(in);
            read(item, baseURI, ct, output, context);
        }

        @Override
        public void read(Reader in, String baseURI, ContentType ct, StreamRDF output, Context context) {
            Item item = SSE.parse(in);
            read(item, baseURI, ct, output, context);
        }

        private void read(Item item, String baseURI, ContentType ct, StreamRDF output, Context context) {
            Graph graph = BuilderGraph.buildGraph(item);
            Iterator<Triple> iter = graph.find(null, null, null);
            for (; iter.hasNext(); )
                output.triple(iter.next());
        }
    }

    // Model.read adapter - must be public.
    public static class RDFReaderSSE extends RDFReaderRIOT
    {
        public RDFReaderSSE() {
            super("SSE");
        }
    }
}