/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.apache.jena.rdfs;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdfs.engine.ApplyRDFS;
import org.apache.jena.rdfs.engine.Mappers;
import org.apache.jena.rdfs.engine.Output;
import org.apache.jena.rdfs.setup.ConfigRDFS;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.sparql.core.Quad;

/**
 * A {@link StreamRDF} that applies RDFS to its inputs.
 * <p>
 * It receives triples and quads (incoming because this is a {@link StreamRDF}),
 * applies RDFS,
 * and outputs to the StreamRDF provided.
 * The output stream may include duplicates.
 */
public class InfStreamRDFS extends StreamRDFWrapper {
    private final ConfigRDFS<Node>     rdfsSetup;
    private final ApplyRDFS<Node, Triple> rdfs;
    private final Output<Node> outputTriple;
    private final boolean includeInput = true;

    private Node currentGraph;
    private Output<Node> currentGraphOutput;

    public InfStreamRDFS(final StreamRDF output, ConfigRDFS<Node> rdfsSetup) {
        super(output);
        this.rdfsSetup = rdfsSetup;
        outputTriple = (s,p,o)->output.triple(Triple.create(s,p,o));
        this.rdfs = new ApplyRDFS<>(rdfsSetup, Mappers.mapperTriple());
    }

    @Override
    public void triple(Triple triple) {
        if ( includeInput )
            super.triple(triple);
        rdfs.infer(triple.getSubject(), triple.getPredicate(), triple.getObject(), outputTriple);
    }

    @Override
    public void quad(Quad quad) {
        if ( includeInput )
            super.quad(quad);
        if ( currentGraph != quad.getGraph() ) {
            currentGraph = quad.getGraph();
            currentGraphOutput = (s,p,o)->Quad.create(currentGraph, s, p, o);
        }
        rdfs.infer(quad.getSubject(), quad.getPredicate(), quad.getObject(), currentGraphOutput);
    }
}
