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

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.OntJenaException;
import org.apache.jena.ontapi.OntModelControls;
import org.apache.jena.ontapi.common.BaseEnhNodeFactoryImpl;
import org.apache.jena.ontapi.common.EnhNodeFactory;
import org.apache.jena.ontapi.common.EnhNodeFilter;
import org.apache.jena.ontapi.common.EnhNodeFinder;
import org.apache.jena.ontapi.common.EnhNodeProducer;
import org.apache.jena.ontapi.common.OntConfig;
import org.apache.jena.ontapi.common.OntEnhGraph;
import org.apache.jena.ontapi.common.OntEnhNodeFactories;
import org.apache.jena.ontapi.common.WrappedEnhNodeFactory;
import org.apache.jena.ontapi.impl.objects.OntClassImpl;
import org.apache.jena.ontapi.impl.objects.OntSimpleClassImpl;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontapi.utils.Graphs;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.ontapi.utils.StdModels;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.RDFListImpl;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class OntClasses {
    public static final EnhNodeFinder CLASS_FINDER = new EnhNodeFinder.ByType(OWL2.Class);
    public static final EnhNodeFinder RESTRICTION_FINDER = new EnhNodeFinder.ByType(OWL2.Restriction);

    // legacy Jena's OntModel allows these types
    private static final Set<Node> COMPATIBLE_TYPES = Stream.of(OWL2.Class, RDFS.Class, RDFS.Datatype)
            .map(FrontsNode::asNode).collect(Collectors.toUnmodifiableSet());

    public static boolean canBeNamedClass(Node n, EnhGraph eg, boolean useLegacyCheck) {
        if (!n.isURI()) {
            return false;
        }
        if (OntEnhGraph.asPersonalityModel(eg).getOntPersonality().getBuiltins().getNamedClasses().contains(n)) {
            return true;
        }
        Graph g = eg.asGraph();
        Set<Node> punnings = OntEnhGraph.asPersonalityModel(eg).getOntPersonality().getPunnings().getNamedClasses();
        if (!useLegacyCheck) {
            return g.contains(n, RDF.type.asNode(), OWL2.Class.asNode()) && !Graphs.hasOneOfType(n, g, punnings);
        }
        if (Graphs.hasOneOfType(n, g, punnings)) {
            return false;
        }
        return canBeClass(n, g);
    }

    /**
     * This is for compatibility with legacy jena's OntModel.
     */
    public static boolean canBeClass(Node n, Graph g) {
        if (Graphs.hasOneOfType(n, g, COMPATIBLE_TYPES)) {
            return true;
        }
        return g.contains(Node.ANY, RDFS.domain.asNode(), n) || g.contains(Node.ANY, RDFS.range.asNode(), n);
    }

    public static EnhNodeFactory createClassExpressionFactory(OntConfig config, Type... filters) {
        return createClassExpressionFactory(config, false, filters);
    }

    public static EnhNodeFactory createClassExpressionFactory(OntConfig config,
                                                              boolean anyClass,
                                                              Type... filters) {
        return createClassExpressionFactory(config, anyClass, Arrays.asList(filters), List.of());
    }

    public static EnhNodeFactory createClassExpressionFactory(OntConfig config,
                                                              boolean anyClass,
                                                              List<Type> filters,
                                                              List<Type> strict) {
        return createClassExpressionFactory(config, anyClass ? OntSimpleClassImpl.NamedImpl::new : null, filters, strict);
    }

    public static EnhNodeFactory createClassExpressionFactory(OntConfig config,
                                                              BiFunction<Node, EnhGraph, EnhNode> namedClassProducer,
                                                              List<Type> filters,
                                                              List<Type> strict) {
        // namedClassFactory should be specified only for the super type OntClass.
        boolean useLegacyClassTest = config.getBoolean(OntModelControls.USE_LEGACY_COMPATIBLE_NAMED_CLASS_FACTORY);
        BiPredicate<Node, EnhGraph> namedClassFilter = namedClassProducer != null ?
                (node, graph) -> canBeNamedClass(node, graph, useLegacyClassTest) : null;
        BiPredicate<Node, EnhGraph> genericClassFilter =
                namedClassProducer != null && config.getBoolean(OntModelControls.ALLOW_GENERIC_CLASS_EXPRESSIONS) ?
                        (node, graph) -> canBeClass(node, graph.asGraph()) : null;
        return new Factory(
                /*namedClassFilter*/ namedClassFilter,
                /*genericClassFilter*/ genericClassFilter,
                /*namedClassProducer*/ namedClassProducer,
                /*allowNamedClassExpressions*/ config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS),
                /*allowQualifiedCardinalityRestrictions*/ config.getBoolean(OntModelControls.USE_OWL2_QUALIFIED_CARDINALITY_RESTRICTION_FEATURE),
                /*primary filter types*/ filters,
                /*additional filter types*/ strict);
    }

    // Boolean Connectives and Enumeration of Individuals
    public static EnhNodeFactory createBooleanConnectivesAndIndividualEnumerationFactory(
            Class<? extends OntClassImpl> impl,
            Property predicate,
            Class<? extends RDFNode> view,
            BiFunction<Node, EnhGraph, EnhNode> producer,
            OntConfig config) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, OWL2.Class, producer);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL2.Class))
                .and((n, g) -> {
                    ExtendedIterator<Triple> res = g.asGraph().find(n, predicate.asNode(), Node.ANY);
                    try {
                        while (res.hasNext()) {
                            if (OntEnhGraph.canAs(view, res.next().getObject(), g)) {
                                return true;
                            }
                        }
                    } finally {
                        res.close();
                    }
                    return false;
                });
        return OntEnhNodeFactories.createCommon(maker, CLASS_FINDER, filter);
    }

    public static EnhNodeFactory createCardinalityRestrictionFactory(
            Class<? extends OntClassImpl.CardinalityRestrictionImpl<?, ?, ?>> impl,
            RestrictionType restrictionType,
            ObjectRestrictionType objectType,
            OntClassImpl.CardinalityType cardinalityType,
            BiFunction<Node, EnhGraph, EnhNode> producer,
            OntConfig config) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, OWL2.Restriction, producer);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL2.Restriction))
                .and(getCardinalityFilter(cardinalityType,
                        objectType.view(),
                        config.getBoolean(OntModelControls.USE_OWL2_QUALIFIED_CARDINALITY_RESTRICTION_FEATURE)))
                .and(restrictionType.getFilter());
        return OntEnhNodeFactories.createCommon(maker, RESTRICTION_FINDER, filter);
    }

    public static EnhNodeFactory createComponentRestrictionFactory(
            Class<? extends OntClassImpl.ComponentRestrictionImpl<?, ?, ?>> impl,
            RestrictionType propertyType,
            ObjectRestrictionType objectType,
            Property predicate,
            BiFunction<Node, EnhGraph, EnhNode> producer,
            OntConfig config) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, OWL2.Restriction, producer);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL2.Restriction))
                .and(propertyType.getFilter())
                .and(objectType.getFilter(predicate));
        return OntEnhNodeFactories.createCommon(maker, RESTRICTION_FINDER, filter);
    }

    public static EnhNodeFactory createNaryRestrictionFactory(
            Class<? extends OntClassImpl.NaryRestrictionImpl<?, ?, ?>> impl,
            Property predicate) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, OWL2.Restriction);
        EnhNodeFilter filter = EnhNodeFilter.ANON.and(new EnhNodeFilter.HasType(OWL2.Restriction))
                .and(new EnhNodeFilter.HasPredicate(OWL2.onProperties))
                .and(new EnhNodeFilter.HasPredicate(predicate));
        return OntEnhNodeFactories.createCommon(maker, RESTRICTION_FINDER, filter);
    }

    private static EnhNodeFilter getCardinalityFilter(
            OntClassImpl.CardinalityType type,
            Class<? extends RDFNode> objectType,
            boolean qualifiedCardinalityAllowed) {
        return (n, g) -> type.isNonQualified(n, g) || (qualifiedCardinalityAllowed && type.isQualified(n, g, objectType));
    }

    @SuppressWarnings("DuplicatedCode")
    public static EnhNodeFactory createOWL2ELObjectOneOfFactory(
            OntConfig config) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(OntClassImpl.OneOfImpl.class, OWL2.Class, OntClassImpl.OneOfImpl::new);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL2.Class))
                .and((n, g) -> {
                    RDFList list = Iterators.findFirst(g.asGraph().find(n, OWL2.oneOf.asNode(), Node.ANY).filterKeep(
                            it -> STDObjectFactories.RDF_LIST.canWrap(it.getObject(), g)
                    ).mapWith(it -> new RDFListImpl(it.getObject(), g))).orElse(null);
                    if (list == null) {
                        return false;
                    }
                    return Iterators.hasExactly(list.iterator(), 1);
                });
        return OntEnhNodeFactories.createCommon(maker, CLASS_FINDER, filter);
    }

    public static EnhNodeFactory createOWL2QLObjectSomeValuesFromFactory(OntConfig config) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(OntClassImpl.QLObjectSomeValuesFromImpl.class, OWL2.Restriction,
                OntClassImpl.QLObjectSomeValuesFromImpl::new);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL2.Restriction))
                .and(RestrictionType.OBJECT.getFilter())
                // either owl:Thing or named class
                .and(ObjectRestrictionType.NAMED_CLASS.getFilter(OWL2.someValuesFrom));
        return OntEnhNodeFactories.createCommon(maker, RESTRICTION_FINDER, filter);
    }

    public static EnhNodeFactory createOWL2RLObjectSomeValuesFromFactory(OntConfig config) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(OntClassImpl.RLObjectSomeValuesFromImpl.class, OWL2.Restriction,
                OntClassImpl.RLObjectSomeValuesFromImpl::new);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL2.Restriction))
                .and(RestrictionType.OBJECT.getFilter())
                .and((n, g) -> {
                    ExtendedIterator<Triple> res = g.asGraph().find(n, OWL2.someValuesFrom.asNode(), Node.ANY);
                    try {
                        while (res.hasNext()) {
                            Node node = res.next().getObject();
                            OntClass clazz = OntEnhGraph.asPersonalityModel(g).safeFindNodeAs(node, OntClass.class);
                            if (clazz == null) {
                                continue;
                            }
                            if (OWL2.Thing.equals(clazz) || clazz.canAsSubClass()) {
                                return true;
                            }
                        }
                    } finally {
                        res.close();
                    }
                    return false;
                });
        return OntEnhNodeFactories.createCommon(maker, RESTRICTION_FINDER, filter);
    }

    public static EnhNodeFactory createOWL2RLObjectAllValuesFromFactory(OntConfig config) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(OntClassImpl.RLObjectAllValuesFromImpl.class, OWL2.Restriction,
                OntClassImpl.RLObjectAllValuesFromImpl::new);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL2.Restriction))
                .and(RestrictionType.OBJECT.getFilter())
                .and((n, g) -> {
                    ExtendedIterator<Triple> res = g.asGraph().find(n, OWL2.allValuesFrom.asNode(), Node.ANY);
                    try {
                        while (res.hasNext()) {
                            Node node = res.next().getObject();
                            OntClass clazz = OntEnhGraph.asPersonalityModel(g).safeFindNodeAs(node, OntClass.class);
                            if (clazz != null && clazz.canAsSuperClass()) {
                                return true;
                            }
                        }
                    } finally {
                        res.close();
                    }
                    return false;
                });
        return OntEnhNodeFactories.createCommon(maker, RESTRICTION_FINDER, filter);
    }

    public static EnhNodeFactory createOWL2QLIntersectionOfFactory(OntConfig config) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(OntClassImpl.QLIntersectionOfImpl.class, OWL2.Class,
                OntClassImpl.QLIntersectionOfImpl::new);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL2.Class))
                .and((n, g) -> {
                    ExtendedIterator<Triple> res = g.asGraph().find(n, OWL2.intersectionOf.asNode(), Node.ANY);
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
                                                    OntEnhGraph.asPersonalityModel(g).safeFindNodeAs(it.asNode(), OntClass.class)
                                            )
                                            .filterKeep(it -> it != null && it.canAsSuperClass()), 2)) {
                                return true;
                            }
                        }
                    } finally {
                        res.close();
                    }
                    return false;
                });
        return OntEnhNodeFactories.createCommon(maker, CLASS_FINDER, filter);
    }

    public static EnhNodeFactory createOWL2RLIntersectionOfFactory(OntConfig config) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(OntClassImpl.RLIntersectionOfImpl.class, OWL2.Class,
                OntClassImpl.RLIntersectionOfImpl::new);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL2.Class))
                .and((n, g) -> {
                    ExtendedIterator<Triple> res = g.asGraph().find(n, OWL2.intersectionOf.asNode(), Node.ANY);
                    try {
                        while (res.hasNext()) {
                            Node listNode = res.next().getObject();
                            if (!STDObjectFactories.RDF_LIST.canWrap(listNode, g)) {
                                return false;
                            }
                            RDFList list = (RDFList) STDObjectFactories.RDF_LIST.wrap(listNode, g);
                            ExtendedIterator<RDFNode> members = list.iterator();
                            int numSub = 0;
                            int numSup = 0;
                            int numEqv = 0;
                            try {
                                while (members.hasNext()) {
                                    RDFNode e = members.next();
                                    OntClass clazz = OntEnhGraph.asPersonalityModel(g).safeFindNodeAs(e.asNode(), OntClass.class);
                                    if (clazz == null) {
                                        continue;
                                    }
                                    if (clazz.canAsSubClass()) {
                                        numSub++;
                                    }
                                    if (clazz.canAsSuperClass()) {
                                        numSup++;
                                    }
                                    if (clazz.canAsEquivalentClass()) {
                                        numEqv++;
                                    }
                                    if (numSub > 1 || numSup > 1 || numEqv > 1) {
                                        return true;
                                    }
                                }
                            } finally {
                                members.close();
                            }
                        }
                    } finally {
                        res.close();
                    }
                    return false;
                });
        return OntEnhNodeFactories.createCommon(maker, CLASS_FINDER, filter);
    }

    public static EnhNodeFactory createOWL2RLComplementOfFactory(OntConfig config) {
        return createOWL2RLQLComplementOfFactory(config, OntClassImpl.RLComplementOfImpl.class, OntClassImpl.RLComplementOfImpl::new);
    }

    public static EnhNodeFactory createOWL2QLComplementOfFactory(OntConfig config) {
        return createOWL2RLQLComplementOfFactory(config, OntClassImpl.QLComplementOfImpl.class, OntClassImpl.QLComplementOfImpl::new);
    }

    private static EnhNodeFactory createOWL2RLQLComplementOfFactory(OntConfig config,
                                                                    Class<? extends OntClassImpl> implType,
                                                                    BiFunction<Node, EnhGraph, EnhNode> producer) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(implType, OWL2.Class, producer);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL2.Class))
                .and((n, g) -> {
                    ExtendedIterator<Triple> res = g.asGraph().find(n, OWL2.complementOf.asNode(), Node.ANY);
                    try {
                        while (res.hasNext()) {
                            Node node = res.next().getObject();
                            OntClass clazz = OntEnhGraph.asPersonalityModel(g).safeFindNodeAs(node, OntClass.class);
                            if (clazz == null) {
                                return false;
                            }
                            if (clazz.canAsSubClass()) {
                                return true;
                            }
                        }
                    } finally {
                        res.close();
                    }
                    return false;
                });
        return OntEnhNodeFactories.createCommon(maker, CLASS_FINDER, filter);
    }

    public static EnhNodeFactory createOWL2RLUnionOfFactory(OntConfig config) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(OntClassImpl.RLUnionOfImpl.class, OWL2.Class,
                OntClassImpl.RLUnionOfImpl::new);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL2.Class))
                .and((n, g) -> {
                    ExtendedIterator<Triple> res = g.asGraph().find(n, OWL2.unionOf.asNode(), Node.ANY);
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
                                                    OntEnhGraph.asPersonalityModel(g).safeFindNodeAs(it.asNode(), OntClass.class)
                                            )
                                            .filterKeep(it -> it != null && it.canAsSubClass()), 2)) {
                                return true;
                            }
                        }
                    } finally {
                        res.close();
                    }
                    return false;
                });
        return OntEnhNodeFactories.createCommon(maker, CLASS_FINDER, filter);
    }

    public static EnhNodeFactory createOWL2RLObjectMaxCardinalityFactory(OntConfig config) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(OntClassImpl.RLObjectMaxCardinalityImpl.class, OWL2.Restriction,
                OntClassImpl.RLObjectMaxCardinalityImpl::new);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL2.Restriction))
                .and(RestrictionType.OBJECT.getFilter())
                .and((n, g) -> {
                    ExtendedIterator<Triple> byMaxQualifiedCardinality = g.asGraph().find(n, OWL2.maxQualifiedCardinality.asNode(), Node.ANY);
                    try {
                        while (byMaxQualifiedCardinality.hasNext()) {
                            Node cardinality = byMaxQualifiedCardinality.next().getObject();
                            if (!isZeroOrOneNonNegativeInteger(cardinality)) {
                                continue;
                            }
                            if (Iterators.anyMatch(
                                    g.asGraph().find(n, OWL2.onClass.asNode(), Node.ANY),
                                    it -> OntEnhGraph.asPersonalityModel(g)
                                            .safeFindNodeAs(it.getObject(), OntClass.class)
                                            .canAsSubClass())) {
                                return true;
                            }
                        }
                    } finally {
                        byMaxQualifiedCardinality.close();
                    }
                    return Iterators.anyMatch(
                            g.asGraph().find(n, OWL2.maxCardinality.asNode(), Node.ANY).mapWith(Triple::getObject),
                            OntClasses::isZeroOrOneNonNegativeInteger);
                });
        return OntEnhNodeFactories.createCommon(maker, RESTRICTION_FINDER, filter);
    }

    public static EnhNodeFactory createOWL2RLDataMaxCardinalityFactory(OntConfig config) {
        EnhNodeProducer maker = new EnhNodeProducer.WithType(OntClassImpl.RLDataMaxCardinalityImpl.class, OWL2.Restriction,
                OntClassImpl.RLDataMaxCardinalityImpl::new);
        EnhNodeFilter primary = config.getBoolean(OntModelControls.ALLOW_NAMED_CLASS_EXPRESSIONS) ? EnhNodeFilter.TRUE : EnhNodeFilter.ANON;
        EnhNodeFilter filter = primary.and(new EnhNodeFilter.HasType(OWL2.Restriction))
                .and(RestrictionType.DATA.getFilter())
                .and((n, g) -> {
                    ExtendedIterator<Triple> byMaxQualifiedCardinality = g.asGraph().find(n, OWL2.maxQualifiedCardinality.asNode(), Node.ANY);
                    try {
                        while (byMaxQualifiedCardinality.hasNext()) {
                            Node cardinality = byMaxQualifiedCardinality.next().getObject();
                            if (!isZeroOrOneNonNegativeInteger(cardinality)) {
                                continue;
                            }
                            if (Iterators.anyMatch(
                                    g.asGraph().find(n, OWL2.onDataRange.asNode(), Node.ANY)
                                            .mapWith(it -> OntEnhGraph.asPersonalityModel(g)
                                                    .findNodeAs(it.getObject(), OntDataRange.class)
                                            ), Objects::nonNull)) {
                                return true;
                            }
                        }
                    } finally {
                        byMaxQualifiedCardinality.close();
                    }
                    return Iterators.anyMatch(
                            g.asGraph().find(n, OWL2.maxCardinality.asNode(), Node.ANY).mapWith(Triple::getObject),
                            OntClasses::isZeroOrOneNonNegativeInteger);
                });
        return OntEnhNodeFactories.createCommon(maker, RESTRICTION_FINDER, filter);
    }

    private static boolean isZeroOrOneNonNegativeInteger(Node n) {
        if (!n.isLiteral() || !n.getLiteral().getDatatypeURI().equals(XSD.nonNegativeInteger.getURI())) {
            return false;
        }
        String value = n.getLiteral().getValue().toString();
        return "0".equals(value) || "1".equals(value);
    }

    public enum ObjectRestrictionType implements PredicateFilterProvider {
        NAMED_CLASS {
            @Override
            public Class<OntClass.Named> view() {
                return OntClass.Named.class;
            }
        },
        CLASS {
            @Override
            public Class<OntClass> view() {
                return OntClass.class;
            }
        },
        DATA_RANGE {
            @Override
            public Class<OntDataRange> view() {
                return OntDataRange.class;
            }
        },
        INDIVIDUAL {
            @Override
            public Class<OntIndividual> view() {
                return OntIndividual.class;
            }
        },
        LITERAL {
            @Override
            public Class<Literal> view() {
                return Literal.class;
            }

            @Override
            public boolean testObject(Node node, EnhGraph graph) {
                return node.isLiteral();
            }
        },
    }

    public enum RestrictionType implements PredicateFilterProvider {
        DATA(OntDataProperty.class),
        OBJECT(OntObjectProperty.class),
        ;
        private final Class<? extends OntProperty> type;
        private final EnhNodeFactory propertyFactory;

        RestrictionType(Class<? extends OntProperty> type) {
            this.type = type;
            this.propertyFactory = WrappedEnhNodeFactory.of(type);
        }

        @Override
        public Class<? extends OntProperty> view() {
            return type;
        }

        public EnhNodeFilter getFilter() {
            return getFilter(OWL2.onProperty);
        }

        @Override
        public boolean testObject(Node node, EnhGraph graph) {
            return propertyFactory.canWrap(node, graph);
        }
    }

    /**
     * Technical interface to make predicate filter for restrictions
     */
    private interface PredicateFilterProvider {

        Class<? extends RDFNode> view();

        default EnhNodeFilter getFilter(Property predicate) {
            return (node, graph) -> testObjects(predicate, node, graph);
        }

        default boolean testObjects(Property predicate, Node node, EnhGraph graph) {
            return Iterators.anyMatch(graph.asGraph().find(node, predicate.asNode(), Node.ANY),
                    t -> testObject(t.getObject(), graph));
        }

        default boolean testObject(Node node, EnhGraph graph) {
            return OntEnhGraph.canAs(view(), node, graph);
        }
    }

    public static class HasSelfFilter implements EnhNodeFilter {
        @Override
        public boolean test(Node n, EnhGraph g) {
            return g.asGraph().contains(n, OWL2.hasSelf.asNode(), StdModels.TRUE.asNode());
        }
    }

    public static class HasSelfMaker extends EnhNodeProducer.WithType {
        public HasSelfMaker() {
            super(OntClassImpl.HasSelfImpl.class, OWL2.Restriction);
        }

        @Override
        public void doInsert(Node node, EnhGraph eg) {
            super.doInsert(node, eg);
            eg.asGraph().add(Triple.create(node, OWL2.hasSelf.asNode(), StdModels.TRUE.asNode()));
        }
    }

    /**
     * A factory to produce {@link OntClass}s of any types.
     */
    @SuppressWarnings("WeakerAccess")
    static class Factory extends BaseEnhNodeFactoryImpl {

        private static final Implementation LIST_FACTORY = STDObjectFactories.RDF_LIST;
        private static final Node ANY = Node.ANY;
        private static final Node TYPE = RDF.Nodes.type;
        private static final Node CLASS = OWL2.Class.asNode();
        private static final Node RESTRICTION = OWL2.Restriction.asNode();
        private static final Node ON_PROPERTY = OWL2.onProperty.asNode();
        private static final Node HAS_VALUE = OWL2.hasValue.asNode();
        private static final Node QUALIFIED_CARDINALITY = OWL2.qualifiedCardinality.asNode();
        private static final Node CARDINALITY = OWL2.cardinality.asNode();
        private static final Node MIN_QUALIFIED_CARDINALITY = OWL2.minQualifiedCardinality.asNode();
        private static final Node MIN_CARDINALITY = OWL2.minCardinality.asNode();
        private static final Node MAX_QUALIFIED_CARDINALITY = OWL2.maxQualifiedCardinality.asNode();
        private static final Node MAX_CARDINALITY = OWL2.maxCardinality.asNode();
        private static final Node SOME_VALUES_FROM = OWL2.someValuesFrom.asNode();
        private static final Node ALL_VALUES_FROM = OWL2.allValuesFrom.asNode();
        private static final Node ON_CLASS = OWL2.onClass.asNode();
        private static final Node ON_DATA_RANGE = OWL2.onDataRange.asNode();
        private static final Node HAS_SELF = OWL2.hasSelf.asNode();
        private static final Node ON_PROPERTIES = OWL2.onProperties.asNode();
        private static final Node INTERSECTION_OF = OWL2.intersectionOf.asNode();
        private static final Node UNION_OF = OWL2.unionOf.asNode();
        private static final Node ONE_OF = OWL2.oneOf.asNode();
        private static final Node COMPLEMENT_OF = OWL2.complementOf.asNode();
        private static final Node TRUE = NodeFactory.createLiteralByValue(Boolean.TRUE, XSDDatatype.XSDboolean);
        private static final String NON_NEGATIVE_INTEGER_URI = XSD.nonNegativeInteger.getURI();


        private static final BiFunction<Node, EnhGraph, EnhNode> GENERIC_CLASS_PRODUCER = OntSimpleClassImpl::new;
        private static final BiFunction<Node, EnhGraph, EnhNode> GENERIC_RESTRICTION_PRODUCER = OntClassImpl.RestrictionImpl::new;

        protected final EnhNodeFactory objectPropertyFactory = WrappedEnhNodeFactory.of(OntObjectProperty.class);
        protected final EnhNodeFactory dataPropertyFactory = WrappedEnhNodeFactory.of(OntDataProperty.class);

        private final boolean allowNamedClassExpressions;
        private final boolean allowQualifiedCardinalityRestrictions;

        private final Set<Type> filters;
        private final Set<Type> strict;
        private final BiFunction<Node, EnhGraph, EnhNode> namedClassProducer;
        private final BiPredicate<Node, EnhGraph> namedClassFilter;
        private final BiPredicate<Node, EnhGraph> genericClassFilter;

        private Factory(
                BiPredicate<Node, EnhGraph> namedClassFilter,
                BiPredicate<Node, EnhGraph> genericClassFilter,
                BiFunction<Node, EnhGraph, EnhNode> namedClassProducer,
                boolean allowNamedClassExpressions,
                boolean allowQualifiedCardinalityRestrictions,
                List<Type> filters,
                List<Type> additionalFilters) {
            if (genericClassFilter != null && namedClassFilter == null) {
                throw new IllegalArgumentException();
            }
            this.namedClassFilter = namedClassFilter;
            this.genericClassFilter = genericClassFilter;
            this.namedClassProducer = namedClassProducer;
            this.allowNamedClassExpressions = allowNamedClassExpressions;
            this.allowQualifiedCardinalityRestrictions = allowQualifiedCardinalityRestrictions;
            this.filters = EnumSet.copyOf(filters);
            this.strict = additionalFilters.isEmpty() ? Set.of() : EnumSet.copyOf(additionalFilters);
        }

        private boolean isDataCardinality(Node n,
                                          EnhGraph eg,
                                          Node p,
                                          Node qp) {
            return isCardinality(n, eg, p) || (allowQualifiedCardinalityRestrictions
                    && isQualifiedCardinality(n, eg, qp, ON_DATA_RANGE, OntDataRange.class));
        }

        private boolean isObjectCardinality(Node n,
                                            EnhGraph eg,
                                            Node p,
                                            Node qp) {
            return isCardinality(n, eg, p) || (allowQualifiedCardinalityRestrictions
                    && isQualifiedCardinality(n, eg, qp, ON_CLASS, OntClass.class));
        }

        private static boolean isQualifiedCardinality(Node n,
                                                      EnhGraph eg,
                                                      Node p,
                                                      Node o,
                                                      Class<? extends OntObject> t) {
            return isCardinality(n, eg, p) && isObjectOfType(n, eg, o, t);
        }

        private static boolean isCardinality(Node n, EnhGraph eg, Node p) {
            return Iterators.findFirst(listObjects(n, eg, p)
                    .filterKeep(x -> isLiteral(x.getObject(), NON_NEGATIVE_INTEGER_URI))).isPresent();
        }

        private static boolean isList(Node n, EnhGraph eg, Node p) {
            return Iterators.findFirst(listObjects(n, eg, p)
                    .filterKeep(x -> LIST_FACTORY.canWrap(x.getObject(), eg))).isPresent();
        }

        @SuppressWarnings("SameParameterValue")
        private static boolean isLiteral(Node n, String dt) {
            return n.isLiteral() && dt.equals(n.getLiteralDatatypeURI());
        }

        private static boolean isObjectOfType(Node n, EnhGraph eg, Node p, Class<? extends OntObject> t) {
            return Iterators.findFirst(listObjects(n, eg, p).filterKeep(x -> hasType(x.getObject(), eg, t))).isPresent();
        }

        private static boolean hasType(Node n, EnhGraph eg, Class<? extends OntObject> type) {
            return OntEnhGraph.canAs(type, n, eg);
        }

        private static ExtendedIterator<Triple> listObjects(Node n, EnhGraph eg, Node p) {
            return eg.asGraph().find(n, p, ANY);
        }

        @Override
        public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
            ExtendedIterator<EnhNode> byOWLClass = null;
            if (filterDeclaredClassExpressions()) {
                byOWLClass = eg.asGraph().find(ANY, RDF.Nodes.type, CLASS)
                        .mapWith(t -> {
                            Node n = t.getSubject();
                            if (namedClassFilter != null && n.isURI()) {
                                return namedClassFilter.test(n, eg) ? namedClassProducer.apply(n, eg) : null;
                            }
                            if (genericClassFilter != null) {
                                return GENERIC_CLASS_PRODUCER.apply(n, eg);
                            }
                            BiFunction<Node, EnhGraph, EnhNode> res = null;
                            if ((!n.isURI() || allowNamedClassExpressions) && filterLogicalExpressions()) {
                                res = logicalExpressionFactory(n, eg);
                            }
                            if (res != null) {
                                return res.apply(n, eg);
                            }
                            return null;
                        })
                        .filterKeep(Objects::nonNull);
            }
            ExtendedIterator<EnhNode> byOWLRestriction = null;
            if (filterRestrictions()) {
                byOWLRestriction = eg.asGraph().find(ANY, RDF.Nodes.type, RESTRICTION)
                        .mapWith(t -> {
                            Node n = t.getSubject();
                            if (n.isURI() && !allowNamedClassExpressions) {
                                return null;
                            }
                            if (genericClassFilter != null) {
                                return GENERIC_RESTRICTION_PRODUCER.apply(n, eg);
                            }
                            BiFunction<Node, EnhGraph, EnhNode> res = restrictionFactory(n, eg);
                            if (res != null) {
                                return res.apply(n, eg);
                            }
                            return null;
                        })
                        .filterKeep(Objects::nonNull);
            }
            if (byOWLClass == null && byOWLRestriction == null) {
                return NullIterator.instance();
            }
            if (byOWLClass != null && byOWLRestriction != null) {
                return byOWLClass.andThen(byOWLRestriction);
            }
            if (byOWLClass == null) {
                return byOWLRestriction;
            }
            return byOWLClass;
        }

        @Override
        public EnhNode createInstance(Node node, EnhGraph eg) {
            BiFunction<Node, EnhGraph, EnhNode> f = map(node, eg);
            if (f == null) return null;
            return f.apply(node, eg);
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg) {
            return map(node, eg) != null;
        }

        @Override
        public EnhNode wrap(Node node, EnhGraph eg) throws OntJenaException {
            BiFunction<Node, EnhGraph, EnhNode> f = map(node, eg);
            if (f == null) {
                throw new OntJenaException.Conversion("Can't convert node " + node + " to Class Expression.");
            }
            EnhNode res = f.apply(node, eg);
            if (res == null) {
                throw new OntJenaException.IllegalState("Can't create Class Expression for node " + node);
            }
            return res;
        }

        private BiFunction<Node, EnhGraph, EnhNode> map(Node n, EnhGraph eg) {
            if (n.isLiteral()) {
                return null;
            }
            if (namedClassFilter != null && n.isURI()) {
                // fast check for entity-class - the most common case in OWL2
                return namedClassFilter.test(n, eg) ? namedClassProducer : null;
            }
            Graph g = eg.asGraph();
            if (filterRestrictions() && g.contains(n, TYPE, RESTRICTION)) {
                if (n.isURI() && !allowNamedClassExpressions) {
                    return null;
                }
                return restrictionFactory(n, eg);
            }
            if (filterLogicalExpressions() && g.contains(n, TYPE, CLASS)) {
                BiFunction<Node, EnhGraph, EnhNode> res = null;
                if ((!n.isURI() || allowNamedClassExpressions) && filterLogicalExpressions()) {
                    res = logicalExpressionFactory(n, eg);
                }
                if (res != null) {
                    return res;
                }
                if (genericClassFilter != null) {
                    // can’t recognize what kind of class this is,
                    // for compatibility reasons (jena OntModel) we return a “generic” factory
                    return GENERIC_CLASS_PRODUCER;
                }
                return null;
            }
            if (genericClassFilter != null) {
                return genericClassFilter.test(n, eg) ? GENERIC_CLASS_PRODUCER : null;
            }
            return null;
        }

        private BiFunction<Node, EnhGraph, EnhNode> restrictionFactory(Node n, EnhGraph eg) {
            if (filterUnaryRestrictions()) {
                BiFunction<Node, EnhGraph, EnhNode> res = unaryRestrictionFactory(n, eg);
                if (res != null) {
                    return res;
                }
            }
            if (filterNaryRestrictions() && eg.asGraph().contains(n, ON_PROPERTIES, ANY)) {
                // very simplified factories for nary-restrictions:
                if (filters.contains(Type.DATA_NARY_SOME_VALUES_FROM)
                        && Iterators.findFirst(listObjects(n, eg, SOME_VALUES_FROM)).isPresent()) {
                    return Type.DATA_NARY_SOME_VALUES_FROM;
                }
                if (filters.contains(Type.DATA_NARY_ALL_VALUES_FROM)
                        && Iterators.findFirst(listObjects(n, eg, ALL_VALUES_FROM)).isPresent()) {
                    return Type.DATA_NARY_ALL_VALUES_FROM;
                }
            }
            if (genericClassFilter != null) {
                // can’t recognize what kind of Restriction this is,
                // for compatibility reasons (jena OntModel) we return a “generic” factory
                return GENERIC_RESTRICTION_PRODUCER;
            }
            return null;
        }

        private BiFunction<Node, EnhGraph, EnhNode> unaryRestrictionFactory(Node n, EnhGraph eg) {
            ExtendedIterator<Node> props = listObjects(n, eg, ON_PROPERTY).mapWith(Triple::getObject);
            try {
                boolean onPropertyFound = false;
                while (props.hasNext()) {
                    onPropertyFound = true;
                    Node p = props.next();
                    if (filterObjectUnaryRestrictions() && objectPropertyFactory.canWrap(p, eg)) {
                        if (filters.contains(Type.OBJECT_SOME_VALUES_FROM)
                                && isObjectOfType(n, eg, SOME_VALUES_FROM, OntClass.class)) {
                            return strictFilter(n, eg, Type.OBJECT_SOME_VALUES_FROM);
                        }
                        if (filters.contains(Type.OBJECT_ALL_VALUES_FROM)
                                && isObjectOfType(n, eg, ALL_VALUES_FROM, OntClass.class)) {
                            return strictFilter(n, eg, Type.OBJECT_ALL_VALUES_FROM);
                        }
                        if (filters.contains(Type.OBJECT_HAS_VALUE)
                                && isObjectOfType(n, eg, HAS_VALUE, OntIndividual.class)) {
                            return Type.OBJECT_HAS_VALUE;
                        }
                        if (filters.contains(Type.OBJECT_MIN_CARDINALITY)
                                && isObjectCardinality(n, eg, MIN_CARDINALITY, MIN_QUALIFIED_CARDINALITY)) {
                            return Type.OBJECT_MIN_CARDINALITY;
                        }
                        if (filters.contains(Type.OBJECT_MAX_CARDINALITY)
                                && isObjectCardinality(n, eg, MAX_CARDINALITY, MAX_QUALIFIED_CARDINALITY)) {
                            return strictFilter(n, eg, Type.OBJECT_MAX_CARDINALITY);
                        }
                        if (filters.contains(Type.OBJECT_EXACT_CARDINALITY)
                                && isObjectCardinality(n, eg, CARDINALITY, QUALIFIED_CARDINALITY)) {
                            return Type.OBJECT_EXACT_CARDINALITY;
                        }
                        if (filters.contains(Type.OBJECT_HAS_SELF)
                                && Iterators.findFirst(listObjects(n, eg, HAS_SELF)
                                .filterKeep(x -> TRUE.equals(x.getObject()))).isPresent()) {
                            return Type.OBJECT_HAS_SELF;
                        }
                    }
                    if (filterDataUnaryRestrictions() && dataPropertyFactory.canWrap(p, eg)) {
                        if (filters.contains(Type.DATA_SOME_VALUES_FROM)
                                && isObjectOfType(n, eg, SOME_VALUES_FROM, OntDataRange.class)) {
                            return Type.DATA_SOME_VALUES_FROM;
                        }
                        if (filters.contains(Type.DATA_ALL_VALUES_FROM)
                                && isObjectOfType(n, eg, ALL_VALUES_FROM, OntDataRange.class)) {
                            return Type.DATA_ALL_VALUES_FROM;
                        }
                        if (filters.contains(Type.DATA_HAS_VALUE)
                                && Iterators.findFirst(listObjects(n, eg, HAS_VALUE)
                                .filterKeep(x -> x.getObject().isLiteral())).isPresent()) {
                            return Type.DATA_HAS_VALUE;
                        }
                        if (filters.contains(Type.DATA_MIN_CARDINALITY)
                                && isDataCardinality(n, eg, MIN_CARDINALITY, MIN_QUALIFIED_CARDINALITY)) {
                            return Type.DATA_MIN_CARDINALITY;
                        }
                        if (filters.contains(Type.DATA_MAX_CARDINALITY)
                                && isDataCardinality(n, eg, MAX_CARDINALITY, MAX_QUALIFIED_CARDINALITY)) {
                            return strictFilter(n, eg, Type.DATA_MAX_CARDINALITY);
                        }
                        if (filters.contains(Type.DATA_EXACT_CARDINALITY)
                                && isDataCardinality(n, eg, CARDINALITY, QUALIFIED_CARDINALITY)) {
                            return Type.DATA_EXACT_CARDINALITY;
                        }
                    }
                }
                if (onPropertyFound && genericClassFilter != null) {
                    // can’t recognize what kind of unary Restriction this is,
                    // for compatibility reasons (jena OntModel) we return a “generic” factory
                    return GENERIC_RESTRICTION_PRODUCER;
                }
            } finally {
                props.close();
            }
            return null;
        }

        private BiFunction<Node, EnhGraph, EnhNode> logicalExpressionFactory(Node n, EnhGraph eg) {
            // first check owl:complementOf, since it is more accurately defined
            if (filters.contains(Type.COMPLEMENT_OF) && isObjectOfType(n, eg, COMPLEMENT_OF, OntClass.class)) {
                return strictFilter(n, eg, Type.COMPLEMENT_OF);
            }
            if (filters.contains(Type.INTERSECTION_OF) && isList(n, eg, INTERSECTION_OF)) {
                return strictFilter(n, eg, Type.INTERSECTION_OF);
            }
            if (filters.contains(Type.UNION_OF) && isList(n, eg, UNION_OF)) {
                return strictFilter(n, eg, Type.UNION_OF);
            }
            if (filters.contains(Type.ONE_OF) && isList(n, eg, ONE_OF)) {
                return strictFilter(n, eg, Type.ONE_OF);
            }
            return null;
        }

        private Type strictFilter(Node n, EnhGraph eg, Type filter) {
            return strict.contains(filter) ? filter.factory.canWrap(n, eg) ? filter : null : filter;
        }

        private boolean filterDeclaredClassExpressions() {
            return namedClassFilter != null || filterLogicalExpressions();
        }

        private boolean filterLogicalExpressions() {
            return filters.contains(Type.COMPLEMENT_OF) || filterCollectionOfExpressions();
        }

        private boolean filterCollectionOfExpressions() {
            return filters.contains(Type.UNION_OF) || filters.contains(Type.INTERSECTION_OF) || filters.contains(Type.ONE_OF);
        }

        private boolean filterRestrictions() {
            return filterUnaryRestrictions() || filterNaryRestrictions();
        }

        private boolean filterUnaryRestrictions() {
            return filterObjectUnaryRestrictions() || filterDataUnaryRestrictions() || filters.contains(Type.OBJECT_HAS_SELF);
        }

        private boolean filterObjectUnaryRestrictions() {
            return filters.contains(Type.OBJECT_ALL_VALUES_FROM)
                    || filters.contains(Type.OBJECT_SOME_VALUES_FROM)
                    || filters.contains(Type.OBJECT_HAS_VALUE)
                    || filters.contains(Type.OBJECT_MIN_CARDINALITY)
                    || filters.contains(Type.OBJECT_EXACT_CARDINALITY)
                    || filters.contains(Type.OBJECT_MAX_CARDINALITY)
                    || filters.contains(Type.OBJECT_HAS_SELF);
        }

        private boolean filterDataUnaryRestrictions() {
            return filters.contains(Type.DATA_ALL_VALUES_FROM)
                    || filters.contains(Type.DATA_SOME_VALUES_FROM)
                    || filters.contains(Type.DATA_HAS_VALUE)
                    || filters.contains(Type.DATA_MIN_CARDINALITY)
                    || filters.contains(Type.DATA_EXACT_CARDINALITY)
                    || filters.contains(Type.DATA_MAX_CARDINALITY);
        }

        private boolean filterNaryRestrictions() {
            return filters.contains(Type.DATA_NARY_ALL_VALUES_FROM) || filters.contains(Type.DATA_SOME_VALUES_FROM);
        }
    }

    public enum Type implements BiFunction<Node, EnhGraph, EnhNode> {
        OBJECT_SOME_VALUES_FROM(OntClass.ObjectSomeValuesFrom.class),
        OBJECT_ALL_VALUES_FROM(OntClass.ObjectAllValuesFrom.class),
        OBJECT_MIN_CARDINALITY(OntClass.ObjectMinCardinality.class),
        OBJECT_MAX_CARDINALITY(OntClass.ObjectMaxCardinality.class),
        OBJECT_EXACT_CARDINALITY(OntClass.ObjectCardinality.class),
        OBJECT_HAS_VALUE(OntClass.ObjectHasValue.class),
        OBJECT_HAS_SELF(OntClass.HasSelf.class),

        DATA_SOME_VALUES_FROM(OntClass.DataSomeValuesFrom.class),
        DATA_ALL_VALUES_FROM(OntClass.DataAllValuesFrom.class),
        DATA_MIN_CARDINALITY(OntClass.DataMinCardinality.class),
        DATA_MAX_CARDINALITY(OntClass.DataMaxCardinality.class),
        DATA_EXACT_CARDINALITY(OntClass.DataCardinality.class),
        DATA_HAS_VALUE(OntClass.DataHasValue.class),
        DATA_NARY_SOME_VALUES_FROM(OntClass.NaryDataSomeValuesFrom.class),
        DATA_NARY_ALL_VALUES_FROM(OntClass.NaryDataAllValuesFrom.class),

        UNION_OF(OntClass.UnionOf.class),
        INTERSECTION_OF(OntClass.IntersectionOf.class),
        ONE_OF(OntClass.OneOf.class),
        COMPLEMENT_OF(OntClass.ComplementOf.class),
        ;

        private final EnhNodeFactory factory;

        Type(Class<? extends OntObject> type) {
            this.factory = WrappedEnhNodeFactory.of(type);
        }

        @Override
        public EnhNode apply(Node node, EnhGraph enhGraph) {
            return factory.createInstance(node, enhGraph);
        }
    }
}
