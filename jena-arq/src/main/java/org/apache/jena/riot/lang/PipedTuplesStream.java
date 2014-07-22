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

package org.apache.jena.riot.lang ;

import org.apache.jena.atlas.lib.Tuple ;
import org.apache.jena.riot.system.StreamRDF ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/**
 * Implementation of a producer class that sends @{code Tuple<Node>}; must be connected to a {@code PipedRDFIterator<Tuple<Node>}. 
 */
public class PipedTuplesStream extends PipedRDFStream<Tuple<Node>> implements StreamRDF
{
    /**
     * Creates a piped tuples stream connected to the specified piped 
     * RDF iterator.  Tuples written to this stream will then be 
     * available as input from <code>sink</code>.
     *
     * @param sink The piped RDF iterator to connect to.
     */
    public PipedTuplesStream(PipedRDFIterator<Tuple<Node>> sink)
    {
        super(sink) ;
    }

    @Override
    public void triple(Triple triple)
    {
        // Triples are discarded
    }

    @Override
    public void quad(Quad quad)
    {
        // Quads are discarded
    }
}
