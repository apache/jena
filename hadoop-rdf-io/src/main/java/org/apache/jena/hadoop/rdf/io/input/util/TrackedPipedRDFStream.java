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

package org.apache.jena.hadoop.rdf.io.input.util;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;

/**
 * A tracked piped RDF stream
 * 
 * 
 * 
 * @param <T>
 *            Type corresponding to a supported RDF primitive
 */
public abstract class TrackedPipedRDFStream<T> extends PipedRDFStream<T> {

    private TrackableInputStream input;
    private Queue<Long> positions = new LinkedList<Long>();

    protected TrackedPipedRDFStream(PipedRDFIterator<T> sink, TrackableInputStream input) {
        super(sink);
        this.input = input;
    }

    @Override
    protected void receive(T t) {
        // Track positions the input stream is at as we receive inputs
        synchronized (this.positions) {
            this.positions.add(this.input.getBytesRead());
        }
        super.receive(t);
    }

    /**
     * Gets the next position
     * 
     * @return Position
     */
    public Long getPosition() {
        synchronized (this.positions) {
            return this.positions.poll();
        }
    }
}
