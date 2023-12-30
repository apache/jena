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

import java.util.function.Function;

import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.Quad;

/**
 * {@link StreamRDF} that expects triples not quads.
 * Runs an action the first time a quad is seen.
 * Quads that are the default graph or no graph are redirected to {@link StreamRDF#triple}.
 */
public class StreamTriplesOnly extends StreamRDFWrapper {

    public static enum QuadPolicy { CALL, IGNORE }

    /** Exception on non-delfault graph quad */
    private static Function<Quad, QuadPolicy> actionException = (q) -> {
        throw new RiotException("Quad in Triple output: "+NodeFmtLib.str(q));
    };

    /** Replace an existing wrapper with a new policy for quads */
    public static StreamRDF setActionIfQuads(StreamRDF stream, Function<Quad, QuadPolicy> action) {
        // Strip existing layers.
        while ( stream instanceof StreamTriplesOnly stream2 ) {
            stream = stream2.get();
        }
        return addActionIfQuads(stream, action);
    }

    /** Add a new policy layer for quads */
    public static StreamRDF addActionIfQuads(StreamRDF stream, Function<Quad, QuadPolicy> action) {
        return new StreamTriplesOnly(stream, action);
    }

    /** Throw a {@link RiotException} if a non-default graph quad is seen. */
    public static StreamRDF exceptionOnQuads(StreamRDF stream) {
        return new StreamTriplesOnly(stream, actionException);
    }

    private QuadPolicy quadAction = QuadPolicy.CALL;
    // The policy when seeing a quad that isn't in the default graph.
    // Return true for "continue calling"
    // Return false for "don't call again, ignore from now on"
    private final Function<Quad, QuadPolicy> action;

    private StreamTriplesOnly(StreamRDF sink, Function<Quad, QuadPolicy> action) {
        super(sink) ;
        this.action = action;
    }

    @Override
    public void quad(Quad quad) {
        if ( quad.getGraph() == null || quad.isTriple() || quad.isDefaultGraph() ) {
            triple(quad.asTriple()) ;
            return;
        }
        if ( quadAction == QuadPolicy.CALL ) {
            quadAction = action.apply(quad);
        }
    }

//        @Override public void triple(Triple triple)
//        { other.triple(triple) ; }
}

