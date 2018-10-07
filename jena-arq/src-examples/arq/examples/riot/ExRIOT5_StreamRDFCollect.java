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

import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.RDFParser ;
import org.apache.jena.riot.lang.CollectorStreamBase;
import org.apache.jena.riot.lang.CollectorStreamTriples;

/**
 * Example of using RIOT for streaming RDF to be stored into a Collection.
 * 
 * Suitable for single-threaded parsing, for use with small data or distributed 
 * computing frameworks (e.g. Hadoop) where the overhead of creating many threads
 * is significant. 
 * 
 * @see CollectorStreamBase
 */
public class ExRIOT5_StreamRDFCollect {

    public static void main(String... argv) {
        final String filename = "data.ttl";
        
        CollectorStreamTriples inputStream = new CollectorStreamTriples();
        RDFParser.source(filename).parse(inputStream);

        for (Triple triple : inputStream.getCollected()) {
        	System.out.println(triple);
        }
    }

}
