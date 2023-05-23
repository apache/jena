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

package org.apache.jena.riot.system;

import org.apache.jena.sparql.core.Quad;

/**
 * {@link StreamRDF} that expects triples not quads.
 * Runs an action the first time a quad is seen.
 * Quads that are the default graph or no graph are redirected to {@link StreamRDF#triple}.
 */
public class StreamTriplesOnly extends StreamRDFWrapper {

    public static StreamRDF actionIfQuads(StreamRDF stream, Runnable action) {
        return new StreamTriplesOnly(stream, action);
    }

    private boolean seenQuads = false;
    private final Runnable action;

    private StreamTriplesOnly(StreamRDF sink, Runnable action) {
        super(sink) ;
        this.action = action;
    }

    @Override
    public void quad(Quad quad) {
        if ( quad.isTriple() || quad.isDefaultGraph() || quad.isUnionGraph() ) {
            triple(quad.asTriple()) ;
            return;
        }
        if ( ! seenQuads ) {
            action.run();
            seenQuads = true;
        }
    }

//        @Override public void triple(Triple triple)
//        { other.triple(triple) ; }
}

