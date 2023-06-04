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

package org.apache.jena.riot.system;

import java.util.Iterator ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;

/* TODO
 * org.apache.jena.riot.streamrdf?
 *
 */
/** Utilities for sending to StreamRDF.
 *  Unless otherwise stated, send* operations do not call stream.start()/stream.finish()
 *  whereas other operations do.
 */

public class StreamRDFOps {

    /** Send a dataset to a StreamRDF as prefixes, triples and quads, enclosed in stream.start()/stream.finish() */
    public static void datasetToStream(DatasetGraph datasetGraph, StreamRDF stream) {
        stream.start() ;
        sendDatasetToStream(datasetGraph, stream) ;
        stream.finish() ;
    }

    /** Send the triples of graph and it's prefix mapping to a StreamRDF, enclosed in stream.start()/stream.finish() */
    public static void graphToStream(Graph graph, StreamRDF stream) {
        stream.start();
        sendGraphToStream(graph, stream) ;
        stream.finish() ;
    }

    /** Send a PrefixMap to a stream */
    public static void sendPrefixesToStream(PrefixMap prefixMap, StreamRDF stream) {
        prefixMap.forEach((p,u) -> stream.prefix(p, u.toString())) ;
    }

    public static void sendPrefixesToStream(PrefixMapping prefixMap, StreamRDF stream) {
        prefixMap.getNsPrefixMap().forEach(stream::prefix);
    }

    /** Send a dataset graph to a stream with triples for the default graph
     * and quads for the named graphs without prefixes
     */
    public static void sendTriplesQuadsToStream(DatasetGraph datasetGraph, StreamRDF stream) {
        sendDatasetToStream(datasetGraph, stream, null, null) ;
    }

    /** Send a dataset to a StreamRDF as prefixes, triples and quads */
    public static void sendDatasetToStream(DatasetGraph datasetGraph, StreamRDF stream) {
        sendDatasetToStream(datasetGraph, stream, null, datasetGraph.prefixes()) ;
    }

    /** Send a dataset to a StreamRDF as triples and quads, using the explicitly given prefix map */
    public static void sendDatasetToStream(DatasetGraph datasetGraph, StreamRDF stream, String baseURI, PrefixMap prefixMap) {
        if ( baseURI != null )
            stream.base(baseURI);
        if ( prefixMap != null )
            sendPrefixesToStream(prefixMap, stream) ;

        // Default graph
        Iterator<Triple> iter1 = datasetGraph.getDefaultGraph().find(null, null, null) ;
        StreamRDFOps.sendTriplesToStream(iter1, stream) ;

        Iterator<Quad> iter2 = datasetGraph.findNG(null, null, null, null) ;
        StreamRDFOps.sendQuadsToStream(iter2, stream) ;
    }


    /**
     * Send the triples of graph and an explicitly given prefix mapping, to a StreamRDF.
     * This operation does not include start/finish nesting - see {@link #graphToStream}.
     */
    public static void sendGraphToStream(Graph graph, StreamRDF stream) {
        PrefixMap prefixMap = PrefixMapFactory.create(graph.getPrefixMapping()) ;
        sendGraphToStream(graph, stream, null, prefixMap) ;
    }

    /** Send the triples of graph, and an explicitly given prefix mapping, to a StreamRDF */
    public static void sendGraphToStream(Graph graph, StreamRDF stream, String baseURI, PrefixMap prefixMap) {
        if ( baseURI != null )
            stream.base(baseURI);
        if ( prefixMap != null )
            sendPrefixesToStream(prefixMap, stream) ;
        Iterator<Triple> iter = graph.find(null, null, null) ;
        StreamRDFOps.sendTriplesToStream(iter, stream) ;
    }

    /** Send the triples of graph to a StreamRDF (no prefix mapping) */
    public static void sendTriplesToStream(Graph graph, StreamRDF stream) {
        sendGraphToStream(graph, stream, null, null) ;
    }

    /** Set triples to a StreamRDF - does not call .start/.finish */
    public static void sendTriplesToStream(Iterator<Triple> iter, StreamRDF dest)
    {
        iter.forEachRemaining(dest::triple);
    }

    /** Send quads of a dataset (including default graph as quads) to a StreamRDF, without prefixes */
    public static void sendQuadsToStream(DatasetGraph datasetGraph, StreamRDF stream) {
        Iterator<Quad> iter2 = datasetGraph.find(null, null, null, null) ;
        StreamRDFOps.sendQuadsToStream(iter2, stream) ;
    }

    /** Set quads to a StreamRDF - does not call .start/.finish */
    public static void sendQuadsToStream(Iterator<Quad> iter, StreamRDF dest)
    {
        iter.forEachRemaining(dest::quad);
    }
}
