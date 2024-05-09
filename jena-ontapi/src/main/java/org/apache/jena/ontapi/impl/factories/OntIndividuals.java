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

package org.apache.jena.ontapi.impl.factories;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.common.OntEnhGraph;
import org.apache.jena.ontapi.common.OntPersonality;
import org.apache.jena.ontapi.impl.OntGraphModelImpl;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SWRL;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class OntIndividuals {
    private static final String FORBIDDEN_SUBJECTS = OntIndividual.Anonymous.class.getName() + ".InSubject";
    private static final String FORBIDDEN_OBJECTS = OntIndividual.Anonymous.class.getName() + ".InObject";
    // allowed predicates for a subject (the pattern '_:x p ANY'):
    private static final Set<Node> FOR_SUBJECT = Stream.of(OWL2.sameAs, OWL2.differentFrom)
            .map(FrontsNode::asNode).collect(Collectors.toUnmodifiableSet());
    // allowed predicates for an object (the pattern 'ANY p _:x'):
    private static final Set<Node> FOR_OBJECT = Stream.of(OWL2.sameAs, OWL2.differentFrom,
                    OWL2.sourceIndividual, OWL2.targetIndividual, OWL2.hasValue,
                    OWL2.annotatedSource, OWL2.annotatedTarget,
                    RDF.first, SWRL.argument1, SWRL.argument2)
            .map(FrontsNode::asNode).collect(Collectors.toUnmodifiableSet());

    public static boolean testAnonymousIndividual(Node node, EnhGraph eg) {
        if (!node.isBlank()) {
            return false;
        }
        boolean hasType = false;
        // class-assertion:
        ExtendedIterator<Node> types = eg.asGraph().find(node, RDF.Nodes.type, Node.ANY).mapWith(Triple::getObject);
        try {
            while (types.hasNext()) {
                if (OntEnhGraph.canAs(OntClass.class, types.next(), eg)) return true;
                hasType = true;
            }
        } finally {
            types.close();
        }
        // any other typed statement (builtin, such as owl:AllDifferent):
        if (hasType) {
            return false;
        }
        // all known predicates whose subject definitely cannot be an individual
        Set<Node> forbiddenSubjects = reserved(eg, FORBIDDEN_SUBJECTS, FOR_SUBJECT);
        // _:x @built-in-predicate @any:
        ExtendedIterator<Node> bySubject = eg.asGraph().find(node, Node.ANY, Node.ANY).mapWith(Triple::getPredicate);
        try {
            while (bySubject.hasNext()) {
                if (forbiddenSubjects.contains(bySubject.next()))
                    return false;
            }
        } finally {
            bySubject.close();
        }
        // all known predicates whose object definitely cannot be an individual
        Set<Node> forbiddenObjects = reserved(eg, FORBIDDEN_OBJECTS, FOR_OBJECT);
        // @any @built-in-predicate _:x
        ExtendedIterator<Node> byObject = eg.asGraph().find(Node.ANY, Node.ANY, node).mapWith(Triple::getPredicate);
        try {
            while (byObject.hasNext()) {
                if (forbiddenObjects.contains(byObject.next()))
                    return false;
            }
        } finally {
            byObject.close();
        }
        // tolerantly allow any other blank node to be treated as anonymous individual:
        return true;
    }

    @SuppressWarnings("unchecked")
    private static Set<Node> reserved(EnhGraph eg, String key, Set<Node> forbiddenProperties) {
        OntPersonality personality = OntEnhGraph.asPersonalityModel(eg).getOntPersonality();
        OntPersonality.Builtins builtins = personality.getBuiltins();
        OntPersonality.Reserved reserved = personality.getReserved();
        Set<Node> builtinProperties = builtins.getOntProperties();
        if (eg instanceof OntGraphModelImpl) {
            Map<String, Object> store = ((OntGraphModelImpl) eg).propertyStore;
            Object res = store.get(key);
            if (res != null) {
                return (Set<Node>) res;
            }
            Set<Node> forbidden = reserved.getProperties().stream()
                    .filter(n -> !builtinProperties.contains(n) && !forbiddenProperties.contains(n))
                    .collect(Collectors.toUnmodifiableSet());
            store.put(key, forbidden);
            return forbidden;
        }
        return reserved.getProperties().stream()
                .filter(n -> !builtinProperties.contains(n) && !forbiddenProperties.contains(n))
                .collect(Collectors.toUnmodifiableSet());
    }
}
