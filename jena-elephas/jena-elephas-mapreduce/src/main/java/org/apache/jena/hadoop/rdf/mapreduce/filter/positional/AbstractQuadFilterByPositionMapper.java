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

package org.apache.jena.hadoop.rdf.mapreduce.filter.positional;

import org.apache.jena.graph.Node ;
import org.apache.jena.hadoop.rdf.mapreduce.filter.AbstractQuadFilterMapper;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.sparql.core.Quad ;

/**
 * An abstract triple filter that filters quads based on different criteria for
 * each position (graph, subject, predicate and object) within the quad.
 * <p>
 * By default this implementation eliminates all quads it sees, derived
 * implementations need to override one or more of the specific accept methods
 * in order to actually accept some triples. See
 * {@link QuadFilterByPredicateMapper} for an example implementation.
 * </p>
 * 
 * 
 * 
 * @param <TKey>
 *            Key type
 */
public abstract class AbstractQuadFilterByPositionMapper<TKey> extends AbstractQuadFilterMapper<TKey> {

    @Override
    protected final boolean accepts(Object key, QuadWritable tuple) {
        Quad q = tuple.get();
        if (!this.acceptsAllGraphs()) {
            Node g = q.getGraph();
            if (!this.acceptsGraph(g))
                return false;
        }
        if (!this.acceptsAllSubjects()) {
            Node s = q.getSubject();
            if (!this.acceptsSubject(s))
                return false;
        }
        if (!this.acceptsAllPredicates()) {
            Node p = q.getPredicate();
            if (!this.acceptsPredicate(p))
                return false;
        }
        if (!this.acceptsAllObjects()) {
            Node o = q.getObject();
            if (!this.acceptsObject(o))
                return false;
        }

        return true;
    }

    /**
     * Gets whether this filter accepts all graphs, if false then the
     * {@link #acceptsGraph(Node)} method will be called to determine if a
     * specific graph is acceptable
     * <p>
     * Default behaviour if not overridden is to return {@code false}
     * </p>
     * 
     * @return True if all graphs are accepted, false otherwise
     */
    protected boolean acceptsAllGraphs() {
        return false;
    }

    /**
     * Gets whether a specific graph is acceptable
     * 
     * @param graph
     *            Graph
     * @return True if accepted, false otherwise
     */
    protected boolean acceptsGraph(Node graph) {
        return false;
    }

    /**
     * Gets whether this filter accepts all subjects, if false then the
     * {@link #acceptsSubject(Node)} method will be called to determine if a
     * specific subject is acceptable
     * <p>
     * Default behaviour if not overridden is to return {@code false}
     * </p>
     * 
     * @return True if all subjects are accepted, false otherwise
     */
    protected boolean acceptsAllSubjects() {
        return false;
    }

    /**
     * Gets whether a specific subject is acceptable
     * 
     * @param subject
     *            Subject
     * @return True if accepted, false otherwise
     */
    protected boolean acceptsSubject(Node subject) {
        return false;
    }

    /**
     * Gets whether this filter accepts all predicate, if false then the
     * {@link #acceptsPredicate(Node)} method will be called to determine if a
     * specific predicate is acceptable
     * <p>
     * Default behaviour if not overridden is to return {@code false}
     * </p>
     * 
     * @return True if all predicates are accepted, false otherwise
     */
    protected boolean acceptsAllPredicates() {
        return false;
    }

    /**
     * Gets whether a specific predicate is acceptable
     * 
     * @param predicate
     *            Predicate
     * @return True if accepted, false otherwise
     */
    protected boolean acceptsPredicate(Node predicate) {
        return false;
    }

    /**
     * Gets whether this filter accepts all objects, if false then the
     * {@link #acceptsObject(Node)} method will be called to determine if a
     * specific object is acceptable
     * <p>
     * Default behaviour if not overridden is to return {@code false}
     * </p>
     * 
     * @return True if all objects are accepted, false otherwise
     */
    protected boolean acceptsAllObjects() {
        return false;
    }

    /**
     * Gets whether a specific object is acceptable
     * 
     * @param object
     *            Object
     * @return True if accepted, false otherwise
     */
    protected boolean acceptsObject(Node object) {
        return false;
    }

}
