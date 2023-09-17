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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.lang.IteratorParsers;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;

/**
 * Example of using RIOT for asynchronous parsing and for a pull parser.
 */
public class ExRIOT9_AsyncParser {

    public static void main(String... argv) {
        final String filename = "data.ttl";

        // ---- Parser a file on another thread.
        // Something to process the parser output.
        StreamRDF stream = StreamRDFLib.print(System.out);
        AsyncParser.asyncParse(filename, stream);

        // -- Alternative way to handle the results of parsing in a pull-parser style.
        // Get an iterator that yields triples.
        Iterator<Triple> iter = AsyncParser.asyncParseTriples(filename);
        iter.forEachRemaining(triple->{
            // Do something with triple
        });

        // For N-Triples and N-Quads only, there is a same-thread pull parser.
        try ( InputStream input = IO.openFileBuffered(filename) ) {
            IteratorParsers.createIteratorNTriples(input);
        } catch (IOException ex) { throw IOX.exception(ex); }
    }
}
