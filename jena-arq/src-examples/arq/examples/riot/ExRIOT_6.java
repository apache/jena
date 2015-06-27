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

import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;

import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.lang.PipedRDFIterator ;
import org.apache.jena.riot.lang.PipedRDFStream ;
import org.apache.jena.riot.lang.PipedTriplesStream ;

/**
 * Example of using RIOT : iterate over output of parser run using a
 * {@link PipedRDFStream} and a {@link PipedRDFIterator}
 * 
 */
public class ExRIOT_6 {

    public static void main(String... argv) {
        final String filename = "data.ttl";

        // Create a PipedRDFStream to accept input and a PipedRDFIterator to
        // consume it
        // You can optionally supply a buffer size here for the
        // PipedRDFIterator, see the documentation for details about recommended
        // buffer sizes
        PipedRDFIterator<Triple> iter = new PipedRDFIterator<Triple>();
        final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(iter);

        // PipedRDFStream and PipedRDFIterator need to be on different threads
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Create a runnable for our parser thread
        Runnable parser = new Runnable() {

            @Override
            public void run() {
                // Call the parsing process.
                RDFDataMgr.parse(inputStream, filename);
            }
        };

        // Start the parser on another thread
        executor.submit(parser);

        // We will consume the input on the main thread here

        // We can now iterate over data as it is parsed, parsing only runs as
        // far ahead of our consumption as the buffer size allows
        while (iter.hasNext()) {
            Triple next = iter.next();
            // Do something with each triple
        }
    }

}
