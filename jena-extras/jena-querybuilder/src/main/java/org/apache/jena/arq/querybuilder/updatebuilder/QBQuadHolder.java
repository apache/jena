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

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * An QuadHolder that manages AbstractQueryBuilder data.
 *
 */
public class QBQuadHolder implements QuadHolder {

    private final AbstractQueryBuilder<?> qb;
    private Node defaultGraphName;

    /**
     * Constructor.
     * 
     * @param graph the default graph name for the triples
     * @param qb the AbstractQueryBuilder that is providing the triples.
     */
    public QBQuadHolder(Node graph, AbstractQueryBuilder<?> qb) {
        this.qb = qb;
        this.defaultGraphName = graph;
    }

    /**
     * Constructor. Uses Quad.defaultGraphNodeGenerated for the graph name.
     * 
     * @see Quad#defaultGraphNodeGenerated
     * @param qb the AbstractQueryBuilder that is providing the triples.
     */
    public QBQuadHolder(AbstractQueryBuilder<?> qb) {
        this(Quad.defaultGraphNodeGenerated, qb);
    }

    @Override
    public ExtendedIterator<Quad> getQuads() {
        Query q = qb.build();
        QuadIteratorBuilder builder = new QuadIteratorBuilder(defaultGraphName);
        q.getQueryPattern().visit(builder);
        return builder.iter;
    }

    @Override
    public QuadHolder setValues(Map<Var, Node> values) {
        qb.clearValues();
        for (Map.Entry<Var, Node> entry : values.entrySet()) {
            qb.setVar(entry.getKey(), entry.getValue());
        }
        return this;
    }

}
