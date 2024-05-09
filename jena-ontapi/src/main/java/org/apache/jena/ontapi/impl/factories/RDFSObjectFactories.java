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
import org.apache.jena.ontapi.common.OntPersonality;
import org.apache.jena.ontapi.impl.objects.OntAnnotationPropertyImpl;
import org.apache.jena.ontapi.impl.objects.OntIndividualImpl;
import org.apache.jena.ontapi.impl.objects.OntObjectImpl;
import org.apache.jena.ontapi.impl.objects.OntSimpleClassImpl;
import org.apache.jena.ontapi.impl.objects.OntSimplePropertyImpl;
import org.apache.jena.ontapi.model.OntAnnotationProperty;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontapi.utils.Graphs;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A helper-factory to produce (OWL1) {@link EnhNodeFactory} factories;
 * for {@link OntPersonality ont-personalities}
 */
public final class RDFSObjectFactories {

    private static final Set<Node> CLASS_TYPES = Stream.of(RDFS.Class, RDFS.Datatype)
            .map(FrontsNode::asNode)
            .collect(Collectors.toUnmodifiableSet());

    public static final EnhNodeFactory ANY_OBJECT = OntEnhNodeFactories.createCommon(
            OntObjectImpl.class,
            EnhNodeFinder.ANY_SUBJECT,
            EnhNodeFilter.URI.or(EnhNodeFilter.ANON)
    );

    public static final Function<OntConfig, EnhNodeFactory> NAMED_CLASS = config -> createFactory(
            OntSimpleClassImpl.NamedImpl.class,
            OntClass.Named.class,
            RDFS.Class,
            OntSimpleClassImpl.NamedImpl::new,
            (n, eg) -> isNamedClass(n, eg, config)
    );

    public static final Function<OntConfig, EnhNodeFactory> NAMED_INDIVIDUAL = config -> OntEnhNodeFactories.createCommon(
            OntIndividualImpl.NamedImpl.class,
            OntIndividualImpl.NamedImpl::new,
            eg -> findIndividuals(eg, config).filterKeep(Node::isURI),
            (n, g) -> isNamedIndividual(n, g, config)
    );

    public static final Function<OntConfig, EnhNodeFactory> ANONYMOUS_INDIVIDUAL = config -> OntEnhNodeFactories.createCommon(
            OntIndividualImpl.AnonymousImpl.class,
            OntIndividualImpl.AnonymousImpl::new,
            eg -> findIndividuals(eg, config).filterKeep(Node::isBlank),
            (n, g) -> isAnonymousIndividual(n, g, config)
    );

    public static final EnhNodeFactory PROPERTY = createFactory(
            OntSimplePropertyImpl.class,
            OntProperty.class,
            RDF.Property,
            OntSimplePropertyImpl::new,
            RDFSObjectFactories::isAnyProperty
    );

    public static final EnhNodeFactory ANNOTATION_PROPERTY = OntEnhNodeFactories.createCommon(OntAnnotationProperty.class,
            new EnhNodeProducer.Default(OntAnnotationPropertyImpl.class, OntAnnotationPropertyImpl::new),
            EnhNodeFinder.NOTHING,
            (n, g) -> n.isURI() &&
                    OntEnhGraph.asPersonalityModel(g).getOntPersonality().getBuiltins().getAnnotationProperties().contains(n)
    );

    public static final Function<OntConfig, EnhNodeFactory> ANY_CLASS = config -> createFactory(
            OntSimpleClassImpl.class,
            OntClass.class,
            RDFS.Class,
            OntSimpleClassImpl::new,
            (n, eg) -> isAnyClass(n, eg, config)
    );

    public static final Function<OntConfig, EnhNodeFactory> ANY_ENTITY = config -> OntEnhNodeFactories.createFrom(
            OntEnhNodeFactories.createFinder(RDF.Property, RDFS.Class), NAMED_CLASS.apply(config), NAMED_INDIVIDUAL.apply(config)
    );

    public static final Function<OntConfig, EnhNodeFactory> ANY_INDIVIDUAL = config -> OntEnhNodeFactories.createFrom(
            eg -> findIndividuals(eg, config),
            OntIndividual.Named.class,
            OntIndividual.Anonymous.class
    );

    private static boolean isNamedClass(Node n, EnhGraph eg, OntConfig config) {
        return n.isURI() && isAnyClass(n, eg, config);
    }

    private static boolean isAnyClass(Node n, EnhGraph eg, OntConfig config) {
        if (OntEnhGraph.asPersonalityModel(eg).getOntPersonality().getBuiltins().getNamedClasses().contains(n)) {
            return true;
        }
        Graph g = eg.asGraph();
        if (!config.getBoolean(OntModelControls.USE_LEGACY_COMPATIBLE_NAMED_CLASS_FACTORY)) {
            return g.contains(n, RDF.type.asNode(), RDFS.Class.asNode());
        }
        return Graphs.hasOneOfType(n, g, CLASS_TYPES) ||
                g.contains(Node.ANY, RDFS.domain.asNode(), n) || g.contains(Node.ANY, RDFS.range.asNode(), n);
    }

    private static boolean isAnyProperty(Node n, EnhGraph eg) {
        if (OntEnhGraph.asPersonalityModel(eg).getOntPersonality().getBuiltins().getOntProperties().contains(n)) {
            return true;
        }
        return eg.asGraph().contains(n, RDF.type.asNode(), RDF.Property.asNode());
    }

    private static boolean isNamedIndividual(Node n, EnhGraph eg, OntConfig config) {
        return n.isURI() && isIndividual(n, eg, config);
    }

    private static boolean isAnonymousIndividual(Node n, EnhGraph eg, OntConfig config) {
        return !n.isURI() && isIndividual(n, eg, config);
    }

    private static boolean isIndividual(Node n, EnhGraph eg, OntConfig config) {
        return Iterators.anyMatch(
                eg.asGraph().find(n, RDF.type.asNode(), Node.ANY)
                        .mapWith(Triple::getObject),
                it -> isAnyClass(it, eg, config)
        );
    }

    private static ExtendedIterator<Node> findIndividuals(EnhGraph eg, OntConfig config) {
        Graph g = eg.asGraph();
        return g.find(Node.ANY, RDF.type.asNode(), Node.ANY)
                .filterKeep(t -> isAnyClass(t.getObject(), eg, config))
                .mapWith(Triple::getSubject);
    }

    private static EnhNodeFactory createFactory(
            Class<? extends OntObjectImpl> impl,
            Class<? extends OntObject> classType,
            Resource resourceType,
            BiFunction<Node, EnhGraph, EnhNode> producer,
            EnhNodeFilter filter) {
        EnhNodeFinder finder = new EnhNodeFinder.ByType(resourceType);
        EnhNodeProducer maker = new EnhNodeProducer.WithType(impl, resourceType, producer);
        return OntEnhNodeFactories.createCommon(classType, maker, finder, filter);
    }
}
