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
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.OntModelControls;
import org.apache.jena.ontapi.common.EnhNodeFactory;
import org.apache.jena.ontapi.common.EnhNodeFilter;
import org.apache.jena.ontapi.common.EnhNodeFinder;
import org.apache.jena.ontapi.common.EnhNodeProducer;
import org.apache.jena.ontapi.common.OntConfig;
import org.apache.jena.ontapi.common.OntEnhGraph;
import org.apache.jena.ontapi.common.OntEnhNodeFactories;
import org.apache.jena.ontapi.impl.objects.OntDisjointImpl;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;

import java.util.function.BiFunction;

final class OntDisjoints {
    public static final EnhNodeFinder PROPERTIES_FINDER = new EnhNodeFinder.ByType(OWL2.AllDisjointProperties);
    public static final EnhNodeFinder DISJOINT_FINDER = OntEnhNodeFactories.createFinder(OWL2.AllDisjointClasses,
            OWL2.AllDifferent, OWL2.AllDisjointProperties);

    public static EnhNodeFactory createDifferentIndividualsFactory(OntConfig config) {
        boolean useDistinctMembers = config.getBoolean(OntModelControls.USE_OWL1_DISTINCT_MEMBERS_PREDICATE_FEATURE);
        boolean compatible = config.getBoolean(OntModelControls.USE_OWL2_DEPRECATED_VOCABULARY_FEATURE);
        Property[] predicates;
        if (useDistinctMembers) {
            predicates = new Property[]{OWL2.distinctMembers};
        } else if (compatible) {
            predicates = new Property[]{OWL2.members, OWL2.distinctMembers};
        } else {
            predicates = new Property[]{OWL2.members};
        }
        return createFactory(
                OntDisjointImpl.IndividualsImpl.class,
                (n, g) -> new OntDisjointImpl.IndividualsImpl(n, g, !compatible, useDistinctMembers),
                OWL2.AllDifferent,
                OntIndividual.class,
                true,
                predicates
        );
    }

    public static EnhNodeFactory createFactory(
            Class<? extends OntDisjointImpl<?>> impl,
            BiFunction<Node, EnhGraph, EnhNode> producer,
            Resource type,
            Class<? extends RDFNode> view,
            boolean allowEmptyList,
            Property... predicates) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, type, producer);
        EnhNodeFinder finder = new EnhNodeFinder.ByType(type);
        EnhNodeFilter filter = EnhNodeFilter.ANON.and(new EnhNodeFilter.HasType(type));
        return OntEnhNodeFactories.createCommon(maker, finder, filter
                .and(getHasPredicatesFilter(predicates))
                .and(getHasMembersOfFilter(view, allowEmptyList, predicates)));
    }

    public static EnhNodeFactory createQLRLOntDisjointFactory() {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(
                OntDisjointImpl.QLRLClassesImpl.class,
                OWL2.AllDisjointClasses,
                OntDisjointImpl.QLRLClassesImpl::new
        );
        EnhNodeFinder finder = new EnhNodeFinder.ByType(OWL2.AllDisjointClasses);
        EnhNodeFilter filter = EnhNodeFilter.ANON.and(new EnhNodeFilter.HasType(OWL2.AllDisjointClasses)).and((n, g) -> {
            ExtendedIterator<Triple> res = g.asGraph().find(n, OWL2.members.asNode(), Node.ANY);
            try {
                while (res.hasNext()) {
                    Node listNode = res.next().getObject();
                    if (!STDObjectFactories.RDF_LIST.canWrap(listNode, g)) {
                        return false;
                    }
                    RDFList list = (RDFList) STDObjectFactories.RDF_LIST.wrap(listNode, g);
                    if (Iterators.hasAtLeast(
                            list.iterator()
                                    .mapWith(it ->
                                            OntEnhGraph.asPersonalityModel(g).findNodeAs(it.asNode(), OntClass.class)
                                    )
                                    .filterKeep(it -> it != null && it.canAsDisjointClass()), 2)) {
                        return true;
                    }
                }
            } finally {
                res.close();
            }
            return false;
        });
        return OntEnhNodeFactories.createCommon(maker, finder, filter);
    }

    private static EnhNodeFilter getHasPredicatesFilter(Property... predicates) {
        if (predicates.length == 0) {
            throw new IllegalArgumentException();
        }
        EnhNodeFilter res = new EnhNodeFilter.HasPredicate(predicates[0]);
        for (int i = 1; i < predicates.length; i++) {
            res = res.or(new EnhNodeFilter.HasPredicate(predicates[i]));
        }
        return res;
    }

    private static EnhNodeFilter getHasMembersOfFilter(Class<? extends RDFNode> view,
                                                       boolean allowEmptyList,
                                                       Property... predicates) {
        return (node, eg) -> {
            ExtendedIterator<Node> res = listRoots(node, eg.asGraph(), predicates);
            try {
                while (res.hasNext()) {
                    if (testList(res.next(), eg, view, allowEmptyList)) return true;
                }
            } finally {
                res.close();
            }
            return false;
        };
    }

    private static ExtendedIterator<Node> listRoots(Node node, Graph graph, Property... predicates) {
        return Iterators.flatMap(Iterators.of(predicates),
                p -> graph.find(node, p.asNode(), Node.ANY).mapWith(Triple::getObject));
    }

    private static boolean testList(Node node, EnhGraph graph, Class<? extends RDFNode> view, boolean allowEmptyList) {
        if (!STDObjectFactories.RDF_LIST.canWrap(node, graph)) {
            return false;
        }
        if (view == null) return true;
        RDFList list = (RDFList) STDObjectFactories.RDF_LIST.wrap(node, graph);
        return (list.isEmpty() && allowEmptyList) ||
                Iterators.anyMatch(list.iterator().mapWith(RDFNode::asNode), n -> OntEnhGraph.canAs(view, n, graph));
    }
}
