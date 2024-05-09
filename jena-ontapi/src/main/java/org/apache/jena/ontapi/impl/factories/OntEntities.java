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
import org.apache.jena.graph.FrontsNode;
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
import org.apache.jena.ontapi.common.OntPersonality;
import org.apache.jena.ontapi.impl.objects.OntAnnotationPropertyImpl;
import org.apache.jena.ontapi.impl.objects.OntDataPropertyImpl;
import org.apache.jena.ontapi.impl.objects.OntIndividualImpl;
import org.apache.jena.ontapi.impl.objects.OntNamedDataRangeImpl;
import org.apache.jena.ontapi.impl.objects.OntObjectImpl;
import org.apache.jena.ontapi.impl.objects.OntObjectPropertyImpl;
import org.apache.jena.ontapi.impl.objects.OntSimpleClassImpl;
import org.apache.jena.ontapi.model.OntAnnotationProperty;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntEntity;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.utils.Graphs;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is an enumeration of all entities (configurable-)factories.
 *
 * @see OntEntity
 */
final class OntEntities {

    public static EnhNodeFactory createNamedObjectPropertyFactory(OntConfig config) {
        Set<Resource> objectPropertyTypes = new LinkedHashSet<>();
        objectPropertyTypes.add(OWL2.ObjectProperty);
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_INVERSE_FUNCTIONAL_FEATURE)) {
            objectPropertyTypes.add(OWL2.InverseFunctionalProperty);
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_REFLEXIVE_FEATURE)) {
            objectPropertyTypes.add(OWL2.ReflexiveProperty);
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_IRREFLEXIVE_FEATURE)) {
            objectPropertyTypes.add(OWL2.IrreflexiveProperty);
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_SYMMETRIC_FEATURE)) {
            objectPropertyTypes.add(OWL2.SymmetricProperty);
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_ASYMMETRIC_FEATURE)) {
            objectPropertyTypes.add(OWL2.AsymmetricProperty);
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_TRANSITIVE_FEATURE)) {
            objectPropertyTypes.add(OWL2.TransitiveProperty);
        }
        return createOntEntityFactory(
                OntObjectProperty.Named.class,
                OntObjectPropertyImpl.NamedImpl.class,
                OntObjectPropertyImpl.NamedImpl::new,
                OntPersonality.Builtins::getObjectProperties,
                OntPersonality.Punnings::getObjectProperties,
                OWL2.ObjectProperty,
                objectPropertyTypes.toArray(new Resource[0])
        );
    }

    public static EnhNodeFactory createDataPropertyFactory() {
        return createOntEntityFactory(
                OntDataProperty.class,
                OntDataPropertyImpl.class,
                OntDataPropertyImpl::new,
                OntPersonality.Builtins::getDatatypeProperties,
                OntPersonality.Punnings::getDatatypeProperties,
                OWL2.DatatypeProperty
        );
    }

    public static EnhNodeFactory createAnnotationPropertyFactory() {
        return createOntEntityFactory(
                OntAnnotationProperty.class,
                OntAnnotationPropertyImpl.class,
                OntAnnotationPropertyImpl::new,
                OntPersonality.Builtins::getAnnotationProperties,
                OntPersonality.Punnings::getAnnotationProperties,
                OWL2.AnnotationProperty
        );
    }

    public static EnhNodeFactory createOWL2NamedClassFactory() {
        return createOntEntityFactory(
                OntClass.Named.class,
                OntSimpleClassImpl.NamedImpl.class,
                OntSimpleClassImpl.NamedImpl::new,
                OntPersonality.Builtins::getNamedClasses,
                OntPersonality.Punnings::getNamedClasses,
                OWL2.Class
        );
    }

    public static EnhNodeFactory createOWL2RLNamedClassFactory() {
        return createOntEntityFactory(
                OntClass.Named.class,
                OntSimpleClassImpl.RLNamedImpl.class,
                OntSimpleClassImpl.RLNamedImpl::new,
                OntPersonality.Builtins::getNamedClasses,
                OntPersonality.Punnings::getNamedClasses,
                OWL2.Class
        );
    }

    public static Function<OntConfig, EnhNodeFactory> createOWL1NamedClassFactory() {
        Set<Node> compatibleTypes = Stream.of(OWL2.Class, RDFS.Class, RDFS.Datatype)
                .map(FrontsNode::asNode).collect(Collectors.toUnmodifiableSet());
        return config -> {
            Function<OntPersonality.Punnings, Set<Node>> punnings = OntPersonality.Punnings::getNamedClasses;
            Function<OntPersonality.Builtins, Set<Node>> builtins = OntPersonality.Builtins::getNamedClasses;
            boolean useLegacyClassTesting = config.getBoolean(OntModelControls.USE_LEGACY_COMPATIBLE_NAMED_CLASS_FACTORY);
            EnhNodeFinder finder = new EnhNodeFinder.ByType(OWL2.Class);
            EnhNodeFilter filter = (n, g) -> OntClasses.canBeNamedClass(n, g, useLegacyClassTesting);
            EnhNodeProducer maker =
                    new EnhNodeProducer.WithType(OntSimpleClassImpl.NamedImpl.class, OWL2.Class, OntSimpleClassImpl.NamedImpl::new)
                            .restrict(createIllegalPunningsFilter(punnings));
            return OntEnhNodeFactories.createCommon(OntClass.Named.class, maker, finder, filter);
        };
    }

    public static EnhNodeFactory createOWL2NamedDataRangeFactory() {
        return createOntEntityFactory(
                OntDataRange.Named.class,
                OntNamedDataRangeImpl.class,
                OntNamedDataRangeImpl::new,
                OntPersonality.Builtins::getDatatypes,
                OntPersonality.Punnings::getDatatypes,
                RDFS.Datatype
        );
    }

    public static EnhNodeFactory createOWL1NamedDataRangeFactory() {
        // In OWL1 only builtins
        return OntEnhNodeFactories.createCommon(OntDataRange.class,
                new EnhNodeProducer.Default(OntNamedDataRangeImpl.class, OntNamedDataRangeImpl::new),
                EnhNodeFinder.NOTHING,
                EnhNodeFilter.URI.and(createBuiltinsFilter(OntPersonality.Builtins::getDatatypes))
        );
    }

    public static EnhNodeFactory createNamedIndividualFactory() {
        EnhNodeFinder finder = new EnhNodeFinder.ByType(OWL2.NamedIndividual);
        EnhNodeFilter filter = (n, g) -> n.isURI() && testNamedIndividualType(n, g);
        EnhNodeProducer maker = new EnhNodeProducer.WithType(
                OntIndividualImpl.NamedImpl.class, OWL2.NamedIndividual, OntIndividualImpl.NamedImpl::new
        ).restrict(createIllegalPunningsFilter(OntPersonality.Punnings::getNamedClasses));
        return OntEnhNodeFactories.createCommon(OntIndividual.Named.class, maker, finder, filter);
    }

    private static boolean testNamedIndividualType(Node n, EnhGraph g) {
        OntPersonality personality = OntEnhGraph.asPersonalityModel(g).getOntPersonality();
        if (personality.getBuiltins().getNamedIndividuals().contains(n)) { // just in case
            return true;
        }
        Set<Node> forbidden = personality.getPunnings().getNamedIndividuals();
        List<Node> candidates = new ArrayList<>();
        boolean hasDeclaration = false;
        ExtendedIterator<Triple> it = g.asGraph().find(n, RDF.Nodes.type, Node.ANY);
        try {
            while (it.hasNext()) {
                Node type = it.next().getObject();
                if (forbidden.contains(type)) {
                    return false;
                }
                if (OWL2.NamedIndividual.asNode().equals(type)) {
                    hasDeclaration = true;
                } else {
                    candidates.add(type);
                }
            }
        } finally {
            it.close();
        }
        if (hasDeclaration) {
            return true;
        }
        // In general, owl:NamedIndividual declaration is optional
        for (Node c : candidates) {
            if (OntEnhGraph.canAs(OntClass.class, c, g)) return true;
        }
        return false;
    }

    public static EnhNodeFactory createOntEntityFactory(Class<? extends OntEntity> classType,
                                                        Class<? extends OntObjectImpl> impl,
                                                        BiFunction<Node, EnhGraph, EnhNode> producer,
                                                        Function<OntPersonality.Builtins, Set<Node>> builtins,
                                                        Function<OntPersonality.Punnings, Set<Node>> punnings,
                                                        Resource resourceType,
                                                        Resource... alternativeResourceTypes) {
        Set<Resource> resourceTypes = Stream.concat(Stream.of(resourceType), Stream.of(alternativeResourceTypes))
                .collect(Collectors.toUnmodifiableSet());
        EnhNodeFinder finder = new EnhNodeFinder.ByTypes(resourceTypes);
        EnhNodeFilter filter = createPrimaryEntityFilter(resourceTypes, builtins, punnings);
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, resourceType, producer)
                .restrict(createIllegalPunningsFilter(punnings));
        return OntEnhNodeFactories.createCommon(classType, maker, finder, filter);
    }

    static EnhNodeFilter createPrimaryEntityFilter(
            Set<Resource> types,
            Function<OntPersonality.Builtins, Set<Node>> builtins,
            Function<OntPersonality.Punnings, Set<Node>> punnings
    ) {
        Set<Node> whiteList = types.stream().map(FrontsNode::asNode).collect(Collectors.toUnmodifiableSet());
        return (n, eg) -> {
            if (!n.isURI()) {
                return false;
            }
            OntPersonality personality = OntEnhGraph.asPersonalityModel(eg).getOntPersonality();
            if (builtins.apply(personality.getBuiltins()).contains(n)) {
                return true;
            }
            Set<Node> blackList = punnings.apply(personality.getPunnings());
            return Graphs.testTypes(n, eg.asGraph(), whiteList, blackList);
        };
    }

    static EnhNodeFilter createIllegalPunningsFilter(Function<OntPersonality.Punnings, Set<Node>> punnings) {
        return (n, eg) -> !hasIllegalPunnings(n, eg, punnings);
    }

    static EnhNodeFilter createBuiltinsFilter(Function<OntPersonality.Builtins, Set<Node>> extractNodeSet) {
        return (n, g) -> isBuiltIn(n, g, extractNodeSet);
    }

    static boolean hasIllegalPunnings(Node n, EnhGraph eg, Function<OntPersonality.Punnings, Set<Node>> extractNodeSet) {
        Set<Node> punnings = extractNodeSet.apply(OntEnhGraph.asPersonalityModel(eg).getOntPersonality().getPunnings());
        return Graphs.hasOneOfType(n, eg.asGraph(), punnings);
    }

    static boolean isBuiltIn(Node n, EnhGraph eg, Function<OntPersonality.Builtins, Set<Node>> extractNodeSet) {
        return extractNodeSet.apply(OntEnhGraph.asPersonalityModel(eg).getOntPersonality().getBuiltins()).contains(n);
    }
}
