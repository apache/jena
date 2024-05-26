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
import org.apache.jena.ontapi.OntJenaException;
import org.apache.jena.ontapi.OntModelControls;
import org.apache.jena.ontapi.common.BaseEnhNodeFactoryImpl;
import org.apache.jena.ontapi.common.EnhNodeFactory;
import org.apache.jena.ontapi.common.EnhNodeFilter;
import org.apache.jena.ontapi.common.EnhNodeFinder;
import org.apache.jena.ontapi.common.EnhNodeProducer;
import org.apache.jena.ontapi.common.OntConfig;
import org.apache.jena.ontapi.common.OntEnhNodeFactories;
import org.apache.jena.ontapi.common.WrappedEnhNodeFactory;
import org.apache.jena.ontapi.impl.objects.OntDataRangeImpl;
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.impl.RDFListImpl;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class OntDataRanges {
    private static final EnhNodeFinder DR_FINDER_OWL1 = new EnhNodeFinder.ByType(OWL2.DataRange);
    private static final EnhNodeFilter DR_FILTER_OWL1 = new EnhNodeFilter.HasType(OWL2.DataRange);

    private static final EnhNodeFinder DR_FINDER_OWL2 = new EnhNodeFinder.ByType(RDFS.Datatype);
    private static final EnhNodeFilter DR_FILTER_OWL2 = EnhNodeFilter.ANON.and(new EnhNodeFilter.HasType(RDFS.Datatype));

    private static final EnhNodeFinder DR_FINDER_OWL2_COMPATIBLE = new EnhNodeFinder.ByTypes(Set.of(RDFS.Datatype, OWL2.DataRange));
    private static final EnhNodeFilter DR_FILTER_OWL2_COMPATIBLE = DR_FILTER_OWL2.or(DR_FILTER_OWL1);

    public static EnhNodeFinder makeFacetRestrictionFinder(Property predicate) {
        return new EnhNodeFinder.ByPredicate(predicate);
    }

    public static EnhNodeFilter makeFacetRestrictionFilter(Property predicate) {
        return EnhNodeFilter.ANON.and(
                (n, g) -> Iterators.anyMatch(g.asGraph().find(n, predicate.asNode(), Node.ANY)
                        .mapWith(Triple::getObject), Node::isLiteral)
        );
    }

    public static EnhNodeFilter makeOWLFilter(OntConfig config) {
        if (config.getBoolean(OntModelControls.USE_OWL1_DATARANGE_DECLARATION_FEATURE)) {
            return DR_FILTER_OWL1;
        }
        if (config.getBoolean(OntModelControls.USE_OWL2_DEPRECATED_VOCABULARY_FEATURE)) {
            return DR_FILTER_OWL2_COMPATIBLE;
        } else {
            return DR_FILTER_OWL2;
        }
    }

    public static EnhNodeFinder makeOWLFinder(OntConfig config) {
        if (config.getBoolean(OntModelControls.USE_OWL1_DATARANGE_DECLARATION_FEATURE)) {
            return DR_FINDER_OWL1;
        }
        if (config.getBoolean(OntModelControls.USE_OWL2_DEPRECATED_VOCABULARY_FEATURE)) {
            return DR_FINDER_OWL2_COMPATIBLE;
        } else {
            return DR_FINDER_OWL2;
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public static EnhNodeFactory createOWL2ELOneOfEnumerationFactory(OntConfig config) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(
                OntDataRangeImpl.OneOfImpl.class, RDFS.Datatype, OntDataRangeImpl.OneOfImpl::new
        );
        EnhNodeFilter filter = makeOWLFilter(config)
                .and((n, g) -> {
                    RDFList list = Iterators.findFirst(g.asGraph().find(n, OWL2.oneOf.asNode(), Node.ANY).filterKeep(
                            it -> STDObjectFactories.RDF_LIST.canWrap(it.getObject(), g)
                    ).mapWith(it -> new RDFListImpl(it.getObject(), g))).orElse(null);
                    if (list == null) {
                        return false;
                    }
                    return Iterators.hasExactly(list.iterator(), 1);
                });
        EnhNodeFinder finder = makeOWLFinder(config);
        return OntEnhNodeFactories.createCommon(maker, finder, filter);
    }

    public static EnhNodeFactory createDataRangeFactory(Type... types) {
        Set<EnhNodeFactory> factories = new LinkedHashSet<>();
        for (Type t : types) {
            if (t == Type.COMPLEMENT_OF) {
                factories.add(Factory.COMPLEMENT_OF_FACTORY);
            }
            if (t == Type.RESTRICTION) {
                factories.add(Factory.RESTRICTION_FACTORY);
            }
            if (t == Type.ONE_OF) {
                factories.add(Factory.ONE_OF_FACTORY);
            }
            if (t == Type.INTERSECTION_OF) {
                factories.add(Factory.INTERSECTION_OF_FACTORY);
            }
            if (t == Type.UNION_OF) {
                factories.add(Factory.UNION_OF_FACTORY);
            }
        }
        return new Factory(factories.stream().toList());
    }

    /**
     * A factory to produce {@link OntDataRange}s.
     * <p>
     * Although it would be straightforward to produce this factory
     * using {@link OntEnhNodeFactories#createFrom(EnhNodeFinder, Class, Class[])},
     * this variant with explicit methods must be a little faster,
     * since there is a reduction of number of some possible repetition calls.
     * Also, everything here is under control.
     */
    public static class Factory extends BaseEnhNodeFactoryImpl {
        private static final Node TYPE = RDF.Nodes.type;
        private static final Node ANY = Node.ANY;
        private static final Node PRIMARY_DATATYPE_TYPE = RDFS.Datatype.asNode();
        // owl:DataRange is deprecated in OWL 2, replaced by rdfs:Datatype, but for compatibility needs to handle it
        private static final Node SECONDARY_DATATYPE_TYPE = OWL2.DataRange.asNode();

        private static final EnhNodeFactory NAMED_FACTORY = WrappedEnhNodeFactory.of(OntDataRange.Named.class);
        private static final EnhNodeFactory ONE_OF_FACTORY = WrappedEnhNodeFactory.of(OntDataRange.OneOf.class);
        private static final EnhNodeFactory COMPLEMENT_OF_FACTORY = WrappedEnhNodeFactory.of(OntDataRange.ComplementOf.class);
        private static final EnhNodeFactory UNION_OF_FACTORY = WrappedEnhNodeFactory.of(OntDataRange.UnionOf.class);
        private static final EnhNodeFactory INTERSECTION_OF_FACTORY = WrappedEnhNodeFactory.of(OntDataRange.IntersectionOf.class);
        private static final EnhNodeFactory RESTRICTION_FACTORY = WrappedEnhNodeFactory.of(OntDataRange.Restriction.class);
        private final List<EnhNodeFactory> anonymousDatarangeFactories;

        private Factory(List<EnhNodeFactory> anonymousDatarangeFactories) {
            this.anonymousDatarangeFactories = Objects.requireNonNull(anonymousDatarangeFactories);
        }

        @Override
        public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
            return eg.asGraph().find(ANY, TYPE, PRIMARY_DATATYPE_TYPE)
                    .mapWith(t -> t.getSubject().isURI() ?
                            safeWrap(t.getSubject(), eg, NAMED_FACTORY) :
                            safeWrap(t.getSubject(), eg, anonymousDatarangeFactories))
                    .andThen(
                            eg.asGraph().find(ANY, TYPE, SECONDARY_DATATYPE_TYPE)
                                    .mapWith(t -> t.getSubject().isURI() ? null : safeWrap(t.getSubject(), eg, ONE_OF_FACTORY))
                    )
                    .filterDrop(Objects::isNull);
        }

        @Override
        public EnhNode createInstance(Node node, EnhGraph eg) {
            if (node.isURI()) {
                return safeWrap(node, eg, NAMED_FACTORY);
            }
            if (!node.isBlank()) {
                return null;
            }
            if (!eg.asGraph().contains(node, TYPE, PRIMARY_DATATYPE_TYPE)
                    && !eg.asGraph().contains(node, TYPE, SECONDARY_DATATYPE_TYPE)) {
                return null;
            }
            return safeWrap(node, eg, anonymousDatarangeFactories);
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg) {
            if (node.isURI()) {
                return NAMED_FACTORY.canWrap(node, eg);
            }
            if (!node.isBlank()) {
                return false;
            }
            if (eg.asGraph().contains(node, TYPE, PRIMARY_DATATYPE_TYPE)) {
                return canWrap(node, eg, anonymousDatarangeFactories);
            }
            return canWrap(node, eg, ONE_OF_FACTORY);
        }

        @Override
        public EnhNode wrap(Node node, EnhGraph eg) {
            if (node.isURI()) {
                return NAMED_FACTORY.wrap(node, eg);
            }
            OntJenaException.Conversion ex = new OntJenaException.Conversion("Can't convert node " + node +
                    " to Data Range Expression.");
            if (!node.isBlank()) {
                throw ex;
            }
            if (!eg.asGraph().contains(node, TYPE, PRIMARY_DATATYPE_TYPE)
                    && !eg.asGraph().contains(node, TYPE, SECONDARY_DATATYPE_TYPE)) {
                throw ex;
            }
            return wrap(node, eg, ex, anonymousDatarangeFactories);
        }
    }

    public enum Type {
        ONE_OF,
        UNION_OF,
        INTERSECTION_OF,
        COMPLEMENT_OF,
        RESTRICTION,
    }
}
