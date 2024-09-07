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
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.common.EnhNodeFactory;
import org.apache.jena.ontapi.common.EnhNodeFilter;
import org.apache.jena.ontapi.common.EnhNodeFinder;
import org.apache.jena.ontapi.common.OntConfig;
import org.apache.jena.ontapi.common.OntEnhGraph;
import org.apache.jena.ontapi.common.OntEnhNodeFactories;
import org.apache.jena.ontapi.impl.objects.OntClassImpl;
import org.apache.jena.ontapi.impl.objects.OntDataRangeImpl;
import org.apache.jena.ontapi.impl.objects.OntIDImpl;
import org.apache.jena.ontapi.impl.objects.OntIndividualImpl;
import org.apache.jena.ontapi.impl.objects.OntObjectImpl;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

import java.util.function.Function;

public class OWL1ObjectFactories {

    public static final EnhNodeFactory ANY_OBJECT = OntEnhNodeFactories.createCommon(
            OntObjectImpl.class,
            EnhNodeFinder.ANY_SUBJECT,
            EnhNodeFilter.URI.or(EnhNodeFilter.ANON)
    );
    public static final EnhNodeFactory ID = OntEnhNodeFactories.createCommon(
            OntIDImpl.class,
            new EnhNodeFinder.ByType(OWL2.Ontology),
            new EnhNodeFilter.HasType(OWL2.Ontology)
    );

    public static final Function<OntConfig, EnhNodeFactory> NAMED_CLASS = OntEntities.createOWL1NamedClassFactory();
    public static final EnhNodeFactory NAMED_DATARANGE = OntEntities.createOWL1NamedDataRangeFactory();
    public static final EnhNodeFactory ANNOTATION_PROPERTY = OntEntities.createAnnotationPropertyFactory();
    public static final EnhNodeFactory DATATYPE_PROPERTY = OntEntities.createDataPropertyFactory();
    public static final Function<OntConfig, EnhNodeFactory> NAMED_OBJECT_PROPERTY = OntEntities::createNamedObjectPropertyFactory;
    public static final EnhNodeFactory NAMED_INDIVIDUAL = OntEnhNodeFactories.createCommon(
            OntIndividualImpl.NamedImpl.class,
            OntIndividualImpl.NamedImpl::new,
            eg -> findIndividuals(eg).filterKeep(Node::isURI),
            OWL1ObjectFactories::isNamedIndividual
    );
    public static final EnhNodeFactory ANONYMOUS_INDIVIDUAL = OntEnhNodeFactories.createCommon(
            OntIndividualImpl.AnonymousImpl.class,
            OntIndividualImpl.AnonymousImpl::new,
            eg -> findIndividuals(eg).filterKeep(Node::isBlank),
            OWL1ObjectFactories::isAnonymousIndividual
    );

    public static final Function<OntConfig, EnhNodeFactory> ANY_ENTITY = config -> OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_TYPED,
            NAMED_CLASS.apply(config),
            NAMED_DATARANGE,
            NAMED_INDIVIDUAL,
            ANNOTATION_PROPERTY,
            DATATYPE_PROPERTY,
            NAMED_OBJECT_PROPERTY.apply(config)
    );

    public static final EnhNodeFactory ANY_INDIVIDUAL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntIndividual.Named.class,
            OntIndividual.Anonymous.class
    );

    public static final Function<OntConfig, EnhNodeFactory> ANY_NAMED_PROPERTY = config -> OntEnhNodeFactories.createFrom(
            NAMED_OBJECT_PROPERTY.apply(config),
            DATATYPE_PROPERTY,
            ANNOTATION_PROPERTY
    );
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_PROPERTY = NAMED_OBJECT_PROPERTY;
    public static final Function<OntConfig, EnhNodeFactory> ANY_DATA_OR_OBJECT_PROPERTY = config -> OntEnhNodeFactories.createFrom(
            NAMED_OBJECT_PROPERTY.apply(config),
            DATATYPE_PROPERTY
    );
    public static final Function<OntConfig, EnhNodeFactory> ANY_PROPERTY = config -> OntProperties.createFactory(config, false);

    // Class Expressions (Boolean Connectives and Enumeration of Individuals):
    public static final Function<OntConfig, EnhNodeFactory> UNION_OF_CLASS =
            config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
                    OntClassImpl.UnionOfImpl.class,
                    OWL2.unionOf,
                    RDFList.class,
                    OntClassImpl.UnionOfImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> INTERSECTION_OF_CLASS =
            config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
                    OntClassImpl.IntersectionOfImpl.class,
                    OWL2.intersectionOf,
                    RDFList.class,
                    OntClassImpl.IntersectionOfImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> ONE_OF_CLASS =
            config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
                    OntClassImpl.OneOfImpl.class,
                    OWL2.oneOf,
                    RDFList.class,
                    OntClassImpl.OneOfImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> COMPLEMENT_OF_CLASS =
            config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
                    OntClassImpl.ComplementOfImpl.class,
                    OWL2.complementOf,
                    OntClass.class,
                    OntClassImpl.ComplementOfImpl::new,
                    config);
    // Class Expressions (Restrictions):
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_SOME_VALUES_FROM_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.ObjectSomeValuesFromImpl.class,
                    OntClasses.RestrictionType.OBJECT,
                    OntClasses.ObjectRestrictionType.CLASS,
                    OWL2.someValuesFrom,
                    OntClassImpl.ObjectSomeValuesFromImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_SOME_VALUES_FROM_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.DataSomeValuesFromImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.DATA_RANGE,
                    OWL2.someValuesFrom,
                    OntClassImpl.DataSomeValuesFromImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_ALL_VALUES_FROM_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.ObjectAllValuesFromImpl.class,
                    OntClasses.RestrictionType.OBJECT,
                    OntClasses.ObjectRestrictionType.CLASS,
                    OWL2.allValuesFrom,
                    OntClassImpl.ObjectAllValuesFromImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_ALL_VALUES_FROM_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.DataAllValuesFromImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.DATA_RANGE,
                    OWL2.allValuesFrom,
                    OntClassImpl.DataAllValuesFromImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_HAS_VALUE_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.ObjectHasValueImpl.class,
                    OntClasses.RestrictionType.OBJECT,
                    OntClasses.ObjectRestrictionType.INDIVIDUAL,
                    OWL2.hasValue,
                    OntClassImpl.ObjectHasValueImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_HAS_VALUE_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.DataHasValueImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.LITERAL,
                    OWL2.hasValue,
                    OntClassImpl.DataHasValueImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_MIN_CARDINALITY_CLASS =
            config -> OntClasses.createCardinalityRestrictionFactory(
                    OntClassImpl.DataMinCardinalityImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.DATA_RANGE,
                    OntClassImpl.CardinalityType.MIN,
                    OntClassImpl.DataMinCardinalityImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_MIN_CARDINALITY_CLASS =
            config -> OntClasses.createCardinalityRestrictionFactory(
                    OntClassImpl.ObjectMinCardinalityImpl.class,
                    OntClasses.RestrictionType.OBJECT,
                    OntClasses.ObjectRestrictionType.CLASS,
                    OntClassImpl.CardinalityType.MIN,
                    OntClassImpl.ObjectMinCardinalityImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_MAX_CARDINALITY_CLASS =
            config -> OntClasses.createCardinalityRestrictionFactory(
                    OntClassImpl.DataMaxCardinalityImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.DATA_RANGE,
                    OntClassImpl.CardinalityType.MAX,
                    OntClassImpl.DataMaxCardinalityImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_MAX_CARDINALITY_CLASS =
            config -> OntClasses.createCardinalityRestrictionFactory(
                    OntClassImpl.ObjectMaxCardinalityImpl.class,
                    OntClasses.RestrictionType.OBJECT,
                    OntClasses.ObjectRestrictionType.CLASS,
                    OntClassImpl.CardinalityType.MAX,
                    OntClassImpl.ObjectMaxCardinalityImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> DATA_CARDINALITY_CLASS =
            config -> OntClasses.createCardinalityRestrictionFactory(
                    OntClassImpl.DataCardinalityImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.DATA_RANGE,
                    OntClassImpl.CardinalityType.EXACTLY,
                    OntClassImpl.DataCardinalityImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_CARDINALITY_CLASS =
            config -> OntClasses.createCardinalityRestrictionFactory(
                    OntClassImpl.ObjectCardinalityImpl.class,
                    OntClasses.RestrictionType.OBJECT,
                    OntClasses.ObjectRestrictionType.CLASS,
                    OntClassImpl.CardinalityType.EXACTLY,
                    OntClassImpl.ObjectCardinalityImpl::new,
                    config);
    // Boolean Connectives and Enumeration of Individuals (with except of ComplementOf):
    public static final Function<OntConfig, EnhNodeFactory> ANY_COLLECTION_OF_CLASS_FULL =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.UNION_OF,
                    OntClasses.Type.INTERSECTION_OF,
                    OntClasses.Type.ONE_OF
            );
    public static final Function<OntConfig, EnhNodeFactory> ANY_COLLECTION_OF_CLASS_LITE =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.INTERSECTION_OF
            );
    // Boolean Connectives and Enumeration of Individuals + ComplementOf):
    public static final Function<OntConfig, EnhNodeFactory> ANY_LOGICAL_CLASS_FULL =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.UNION_OF,
                    OntClasses.Type.INTERSECTION_OF,
                    OntClasses.Type.ONE_OF,
                    OntClasses.Type.COMPLEMENT_OF
            );
    public static final Function<OntConfig, EnhNodeFactory> ANY_LOGICAL_CLASS_LITE =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.INTERSECTION_OF
            );
    // Value Restrictions:
    public static final Function<OntConfig, EnhNodeFactory> ANY_VALUE_RESTRICTION_CLASS_FULL =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_ALL_VALUES_FROM,
                    OntClasses.Type.DATA_HAS_VALUE
            );
    public static final Function<OntConfig, EnhNodeFactory> ANY_VALUE_RESTRICTION_CLASS_LITE =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_ALL_VALUES_FROM
            );
    // Cardinality Restrictions:
    public static final Function<OntConfig, EnhNodeFactory> ANY_CARDINALITY_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_MIN_CARDINALITY,
                    OntClasses.Type.OBJECT_MAX_CARDINALITY,
                    OntClasses.Type.OBJECT_EXACT_CARDINALITY,
                    OntClasses.Type.DATA_MIN_CARDINALITY,
                    OntClasses.Type.DATA_MAX_CARDINALITY,
                    OntClasses.Type.DATA_EXACT_CARDINALITY
            );
    // Cardinality + Existential/Universal Restrictions + Value Restrictions:
    public static final Function<OntConfig, EnhNodeFactory> ANY_RESTRICTION_CLASS_FULL =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                    OntClasses.Type.OBJECT_MIN_CARDINALITY,
                    OntClasses.Type.OBJECT_MAX_CARDINALITY,
                    OntClasses.Type.OBJECT_EXACT_CARDINALITY,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_ALL_VALUES_FROM,
                    OntClasses.Type.DATA_MIN_CARDINALITY,
                    OntClasses.Type.DATA_MAX_CARDINALITY,
                    OntClasses.Type.DATA_EXACT_CARDINALITY,
                    OntClasses.Type.DATA_HAS_VALUE
            );
    public static final Function<OntConfig, EnhNodeFactory> ANY_RESTRICTION_CLASS_LITE =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                    OntClasses.Type.OBJECT_MIN_CARDINALITY,
                    OntClasses.Type.OBJECT_MAX_CARDINALITY,
                    OntClasses.Type.OBJECT_EXACT_CARDINALITY,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_ALL_VALUES_FROM,
                    OntClasses.Type.DATA_MIN_CARDINALITY,
                    OntClasses.Type.DATA_MAX_CARDINALITY,
                    OntClasses.Type.DATA_EXACT_CARDINALITY
            );
    public static final Function<OntConfig, EnhNodeFactory> ANY_UNARY_RESTRICTION_CLASS = ANY_RESTRICTION_CLASS_FULL;
    public static final Function<OntConfig, EnhNodeFactory> ANY_COMPONENT_RESTRICTION_CLASS = ANY_RESTRICTION_CLASS_FULL;
    // All Class Expressions:
    public static final Function<OntConfig, EnhNodeFactory> ANY_CLASS_FULL =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    true,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                    OntClasses.Type.OBJECT_MIN_CARDINALITY,
                    OntClasses.Type.OBJECT_MAX_CARDINALITY,
                    OntClasses.Type.OBJECT_EXACT_CARDINALITY,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_ALL_VALUES_FROM,
                    OntClasses.Type.DATA_MIN_CARDINALITY,
                    OntClasses.Type.DATA_MAX_CARDINALITY,
                    OntClasses.Type.DATA_EXACT_CARDINALITY,
                    OntClasses.Type.DATA_HAS_VALUE,
                    OntClasses.Type.UNION_OF,
                    OntClasses.Type.INTERSECTION_OF,
                    OntClasses.Type.ONE_OF,
                    OntClasses.Type.COMPLEMENT_OF
            );
    public static final Function<OntConfig, EnhNodeFactory> ANY_CLASS_LITE =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    true,
                    OntClasses.Type.INTERSECTION_OF,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                    OntClasses.Type.OBJECT_MIN_CARDINALITY,
                    OntClasses.Type.OBJECT_MAX_CARDINALITY,
                    OntClasses.Type.OBJECT_EXACT_CARDINALITY,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_ALL_VALUES_FROM,
                    OntClasses.Type.DATA_MIN_CARDINALITY,
                    OntClasses.Type.DATA_MAX_CARDINALITY,
                    OntClasses.Type.DATA_EXACT_CARDINALITY
            );

    // Data Range Expressions
    public static final Function<OntConfig, EnhNodeFactory> ONE_OF_DATARANGE = config -> OntEnhNodeFactories.createCommon(
            OntDataRangeImpl.OneOfImpl.class,
            OntDataRanges.makeOWLFinder(config),
            OntDataRanges.makeOWLFilter(config).and(new EnhNodeFilter.HasPredicate(OWL2.oneOf))
    );
    public static final Function<OntConfig, EnhNodeFactory> ANY_COMPONENTS_DATARANGE = ONE_OF_DATARANGE;
    public static final Function<OntConfig, EnhNodeFactory> ANY_DATARANGE = config -> OntEnhNodeFactories.createFrom(
            ONE_OF_DATARANGE.apply(config),
            NAMED_DATARANGE
    );

    public static final Function<OntConfig, EnhNodeFactory> DIFFERENT_INDIVIDUALS_DISJOINT =
            OntDisjoints::createDLFullDifferentIndividualsFactory;
    public static final Function<OntConfig, EnhNodeFactory> ANY_DISJOINT = DIFFERENT_INDIVIDUALS_DISJOINT;

    private static boolean isNamedIndividual(Node n, EnhGraph eg) {
        return n.isURI() && isIndividual(n, eg);
    }

    private static boolean isAnonymousIndividual(Node n, EnhGraph eg) {
        return !n.isURI() && isIndividual(n, eg);
    }

    private static boolean isIndividual(Node n, EnhGraph eg) {
        EnhNodeFactory factory = ANY_CLASS_FULL.apply(OntEnhGraph.config(eg));
        return Iterators.anyMatch(
                eg.asGraph().find(n, RDF.type.asNode(), Node.ANY)
                        .mapWith(Triple::getObject),
                it -> factory.canWrap(it, eg)
        );
    }

    private static ExtendedIterator<Node> findIndividuals(EnhGraph eg) {
        EnhNodeFactory factory = ANY_CLASS_FULL.apply(OntEnhGraph.config(eg));
        return eg.asGraph().find(Node.ANY, RDF.type.asNode(), Node.ANY)
                .filterKeep(t -> factory.canWrap(t.getObject(), eg))
                .mapWith(Triple::getSubject);
    }
}
