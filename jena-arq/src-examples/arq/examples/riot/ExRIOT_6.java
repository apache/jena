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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.StreamedTriplesIterator;
import org.apache.jena.riot.out.SinkTripleOutput;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.riot.system.SyntaxLabels;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

/** Example of using RIOT : iterate over output of parser run */
public class ExRIOT_5 {

    public static void main(String... argv) {
        // Not needed here as we are using RIOT itself via RDFDataMgr, not
        // indirectly.
        // RIOT.init() ;

        String filename = "data.ttl";

        // Create a StreamedTriplesIterator, this doubles as both a StreamRDF
        // for parser output and an iterator for our consumption
        // You can optionally supply a buffer size here, see the documentation
        // for details about recommended buffer sizes
        StreamedTriplesIterator iter = new StreamedTriplesIterator();

        // The classes derived from StreamedRDFIterator such as this are
        // fully thread safe so the parser and the consumer of the iterator
        // may be on different threads
        // Generally speaking the parser and the consumer must be on different
        // threads as otherwise your consumer code will never start and
        // everything will deadlock

        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Create a runnable for our parser thread
        Runnable parser = new Runnable() {

            @Override
            public void run() {
                // Call the parsing process.
                RDFDataMgr.parse(iter, filename);
            }
        };

        // Start the parser
        executor.submit(parser);

        // We will consume the input on the main thread here

        // We can now iterate over data as it is parsed, parsing only runs as
        // far
        // ahead of our consumption as the buffer size allows
        while (iter.hasNext()) {
            Triple next = iter.next();
            // Do something with each triple
        }
    }

}
