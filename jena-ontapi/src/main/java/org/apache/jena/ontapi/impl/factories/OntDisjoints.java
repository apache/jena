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
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;

import java.util.function.BiFunction;
import java.util.function.Predicate;

final class OntDisjoints {
    public static final EnhNodeFinder PROPERTIES_FINDER = new EnhNodeFinder.ByType(OWL2.AllDisjointProperties);
    public static final EnhNodeFinder DISJOINT_FINDER = OntEnhNodeFactories.createFinder(OWL2.AllDisjointClasses,
            OWL2.AllDifferent, OWL2.AllDisjointProperties);

    public static EnhNodeFactory createDLFullDifferentIndividualsFactory(OntConfig config) {
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
                it -> true,
                1,
                predicates
        );
    }

    public static EnhNodeFactory createELQLRLDifferentIndividualsFactory() {
        return createFactory(
                OntDisjointImpl.IndividualsImpl.class,
                (n, g) -> new OntDisjointImpl.IndividualsImpl(n, g, true, false),
                OWL2.AllDifferent,
                OntIndividual.class,
                it -> true,
                2,
                OWL2.members
        );
    }

    public static EnhNodeFactory createDisjointObjectPropertiesFactory(int atLeastN) {
        return createFactory(
                OntDisjointImpl.ObjectPropertiesImpl.class,
                OntDisjointImpl.ObjectPropertiesImpl::new,
                OWL2.AllDisjointProperties,
                OntObjectProperty.class,
                it -> true,
                atLeastN,
                OWL2.members
        );
    }

    public static EnhNodeFactory createDisjointDataPropertiesFactory(int atLeastN) {
        return createFactory(
                OntDisjointImpl.DataPropertiesImpl.class,
                OntDisjointImpl.DataPropertiesImpl::new,
                OWL2.AllDisjointProperties,
                OntDataProperty.class,
                it -> true,
                atLeastN,
                OWL2.members
        );
    }

    public static EnhNodeFactory createDisjointClassesFactory(int atLeastN) {
        return createFactory(
                OntDisjointImpl.QLRLClassesImpl.class,
                OntDisjointImpl.QLRLClassesImpl::new,
                OWL2.AllDisjointClasses,
                OntClass.class,
                it -> true,
                atLeastN,
                OWL2.members
        );
    }

    public static EnhNodeFactory createQLRLDisjointClassesFactory() {
        return createFactory(
                OntDisjointImpl.QLRLClassesImpl.class,
                OntDisjointImpl.QLRLClassesImpl::new,
                OWL2.AllDisjointClasses,
                OntClass.class,
                OntClass::canAsDisjointClass,
                2,
                OWL2.members
        );
    }

    private static <X extends RDFNode> EnhNodeFactory createFactory(
            Class<? extends OntDisjointImpl<?>> impl,
            BiFunction<Node, EnhGraph, EnhNode> producer,
            Resource rdfType,
            Class<X> memberType,
            Predicate<X> testMember,
            int atLeastN,
            Property... membersPredicates
    ) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, rdfType, producer);
        EnhNodeFinder finder = new EnhNodeFinder.ByType(rdfType);
        EnhNodeFilter filter = EnhNodeFilter.ANON.and(new EnhNodeFilter.HasType(rdfType)).and((n, g) -> {
            ExtendedIterator<Triple> res;
            if (membersPredicates.length == 1) {
                res = g.asGraph().find(n, membersPredicates[0].asNode(), Node.ANY);
            } else {
                res = Iterators.flatMap(Iterators.of(membersPredicates), it -> g.asGraph().find(n, it.asNode(), Node.ANY));
            }
            try {
                while (res.hasNext()) {
                    Node listNode = res.next().getObject();
                    if (!STDObjectFactories.RDF_LIST.canWrap(listNode, g)) {
                        return false;
                    }
                    if (atLeastN == 0) {
                        return true;
                    }
                    RDFList list = (RDFList) STDObjectFactories.RDF_LIST.wrap(listNode, g);
                    if (Iterators.hasAtLeast(
                            list.iterator()
                                    .mapWith(it ->
                                            OntEnhGraph.asPersonalityModel(g).findNodeAs(it.asNode(), memberType)
                                    )
                                    .filterKeep(it -> it != null && testMember.test(it)), atLeastN)) {
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
}
