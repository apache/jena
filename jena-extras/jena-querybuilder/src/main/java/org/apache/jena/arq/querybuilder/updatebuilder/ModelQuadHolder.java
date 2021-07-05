/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder.updatebuilder;

import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * An QuadHolder that manages extracts all statements from a model as quads.
 *
 */
public class ModelQuadHolder implements QuadHolder {

    private final Model model;
    private final Node defaultGraphName;

    /**
     * Constructor.
     * 
     * @param graph the default graph name for the triples
     * @param model the model that is providing the triples.
     */
    public ModelQuadHolder(final Node graph, final Model model) {
        this.model = model;
        defaultGraphName = graph;
    }

    /**
     * Constructor. Uses Quad.defaultGraphNodeGenerated for the graph name.
     * 
     * @see Quad#defaultGraphNodeGenerated
     * @param model the model that is providing the triples.
     */
    public ModelQuadHolder(final Model model) {
        this(Quad.defaultGraphNodeGenerated, model);
    }

    @Override
    public ExtendedIterator<Quad> getQuads() {
        return model.listStatements().mapWith(stmt -> new Quad(defaultGraphName, stmt.asTriple()));
    }

    /**
     * This implementation does nothing.
     */
    @Override
    public QuadHolder setValues(final Map<Var, Node> values) {
        return this;
    }

}
