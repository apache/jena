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

package org.apache.jena.rdfs.engine;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * Constants relating to RDFS inference.
 */
public class ConstRDFS {

    public static final Node ANY               = Node.ANY;
    public static final Node rdfType           = RDF.Nodes.type;
    public static final Node rdfsRange         = RDFS.Nodes.range;
    public static final Node rdfsDomain        = RDFS.Nodes.domain;
    public static final Node rdfsSubClassOf    = RDFS.Nodes.subClassOf;
    public static final Node rdfsSubPropertyOf = RDFS.Nodes.subPropertyOf;

    private static Set<Node> vocabTerms        = new HashSet<>();
    static {
        vocabTerms.add(rdfsRange);
        vocabTerms.add(rdfsDomain);
        vocabTerms.add(rdfsSubClassOf);
        vocabTerms.add(rdfsSubPropertyOf);
    }

    /** Predicate that accepts rdfs:domain, rdfs:range, rdfs:subClassOf and rdfs:subPropertyOf */
    public static Predicate<Triple> filterRDFS =
        triple -> vocabTerms.contains(triple.getPredicate());

    /** Predicate that rejects rdfs:domain, rdfs:range, rdfs:subClassOf and rdfs:subPropertyOf */
    public static Predicate<Triple> filterNotRDFS = filterRDFS.negate();
}
