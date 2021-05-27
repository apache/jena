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

package org.apache.jena.rdfs.setup;

import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;

/**
 * Inference setup for RDFS over some space of 3-tuples of type {@code <T>}.
 * {@code <T>} maybe {@link Node} but it may be some storage idea of a RDF term,
 * such as TDB2's {@code NodeId}.
 */
public interface ConfigRDFS<X> {

    /** Return the sub-class hierarchy - map from class to its subclasses (transitive). */
    public Map<X, Set<X>> getSubClassHierarchy();

    /** Return the sub-property hierarchy - map from property to its sub-properties (transitive). */
    public Map<X, Set<X>> getSubPropertyHierarchy();

    /** Return the mapping property to range(s). */
    public Map<X, Set<X>> getPropertyRanges();

    /** Return the mapping property to domains(s). */
    public Map<X, Set<X>> getPropertyDomains();

    /** All super-types of an element.
     * Does not include the element unless there is a cycle of length &gt; 1.
     * Returns an empty set of there are no declared superclasses.
     */
    public Set<X> getSuperClasses(X elt);

    /** All super-types of an element, including the element itself. */
    public Set<X> getSuperClassesInc(X elt);

    /** All sub-types of an element.  Does not include the element unless there is a cycle of length &gt; 1 */
    public Set<X> getSubClasses(X elt);

    /** All sub-types of an element, including the element itself */
    public Set<X> getSubClassesInc(X elt);

    /** All super-properties.  Does not include the property itself unless there is a cycle of length &gt; 1. */
    public Set<X> getSuperProperties(X elt);

    /** All super-properties including the property itself. */
    public Set<X> getSuperPropertiesInc(X elt);

    /** All sub-properties.  Does not include the property itself unless there is a cycle of length &gt; 1. */
    public Set<X> getSubProperties(X elt);

    /** All sub-properties including the property itself. */
    public Set<X> getSubPropertiesInc(X elt);

    /** Get the range(s) of a property - only includes mentioned range types, not supertypes. */
    public Set<X> getRange(X elt);

    /** Get the domain(s) of a property - only includes mentioned domain types, not supertypes. */
    public Set<X> getDomain(X elt);

    /** Get the properties that directly mention 'type' as their range. */
    public Set<X> getPropertiesByRange(X elt);

    /** Get the properties that directly mention 'type' as their domain. */
    public Set<X> getPropertiesByDomain(X elt);

    /** Does this setup have any class/subclass declarations? */
    public boolean hasClassDeclarations();

    /** Does this setup have any property/subproperty declarations? */
    public boolean hasPropertyDeclarations();

    /**
     * Does this setup have any property/subproperty declarations
     * and no range, domain or subClass (which means no rdf:type work needed).
     */
    public boolean hasOnlyPropertyDeclarations();

    /** Does this setup have any range declarations? */
    public boolean hasRangeDeclarations();

    /** Does this setup have any domain declarations? */
    public boolean hasDomainDeclarations();

    /** Does this setup have any RDFS? */
    public boolean hasRDFS();

}

