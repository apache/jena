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
import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.mapreduce.filter.AbstractTripleFilterMapper;
import org.apache.jena.hadoop.rdf.types.TripleWritable;

/**
 * An abstract triple filter that filters triples based on different criteria
 * for each position (subject, predicate and object) within the triple.
 * <p>
 * By default this implementation eliminates all triples it sees, derived
 * implementations need to override one or more of the specific accept methods
 * in order to actually accept some triples. See
 * {@link TripleFilterByPredicateUriMapper} for an example implementation.
 * </p>
 * 
 * 
 * 
 * @param <TKey>
 *            Key type
 */
public abstract class AbstractTripleFilterByPositionMapper<TKey> extends AbstractTripleFilterMapper<TKey> {

    @Override
    protected final boolean accepts(Object key, TripleWritable tuple) {
        Triple t = tuple.get();
        if (!this.acceptsAllSubjects()) {
            Node s = t.getSubject();
            if (!this.acceptsSubject(s))
                return false;
        }
        if (!this.acceptsAllPredicates()) {
            Node p = t.getPredicate();
            if (!this.acceptsPredicate(p))
                return false;
        }
        if (!this.acceptsAllObjects()) {
            Node o = t.getObject();
            if (!this.acceptsObject(o))
                return false;
        }

        return true;
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
