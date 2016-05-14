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

package org.apache.jena.hadoop.rdf.io.input.readers;

import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.io.input.util.TrackableInputStream;
import org.apache.jena.hadoop.rdf.io.input.util.TrackedPipedRDFStream;
import org.apache.jena.hadoop.rdf.io.input.util.TrackedPipedTriplesStream;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.lang.PipedRDFIterator;

/**
 * An abstract record reader for whole file triple formats
 * 
 * 
 * 
 */
public abstract class AbstractBlockBasedTripleReader extends AbstractBlockBasedNodeTupleReader<Triple, TripleWritable> {

    @Override
    protected PipedRDFIterator<Triple> getPipedIterator() {
        return new PipedRDFIterator<Triple>();
    }

    @Override
    protected TrackedPipedRDFStream<Triple> getPipedStream(PipedRDFIterator<Triple> iterator, TrackableInputStream input) {
        return new TrackedPipedTriplesStream(iterator, input);
    }

    @Override
    protected TripleWritable createInstance(Triple tuple) {
        return new TripleWritable(tuple);
    }
}
