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

import org.apache.jena.ontapi.common.EnhNodeFactory;
import org.apache.jena.ontapi.common.EnhNodeFilter;
import org.apache.jena.ontapi.common.EnhNodeFinder;
import org.apache.jena.ontapi.common.OntConfig;
import org.apache.jena.ontapi.common.OntEnhNodeFactories;
import org.apache.jena.ontapi.common.OntPersonality;
import org.apache.jena.ontapi.impl.objects.OntAnnotationImpl;
import org.apache.jena.ontapi.impl.objects.OntClassImpl;
import org.apache.jena.ontapi.impl.objects.OntDataRangeImpl;
import org.apache.jena.ontapi.impl.objects.OntFacetRestrictionImpl;
import org.apache.jena.ontapi.impl.objects.OntIDImpl;
import org.apache.jena.ontapi.impl.objects.OntIndividualImpl;
import org.apache.jena.ontapi.impl.objects.OntNegativePropertyAssertionImpl;
import org.apache.jena.ontapi.impl.objects.OntObjectImpl;
import org.apache.jena.ontapi.impl.objects.OntSimpleClassImpl;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntDisjoint;
import org.apache.jena.ontapi.model.OntFacetRestriction;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntNegativeAssertion;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;

import java.util.List;
import java.util.function.Function;

/**
 * A helper-factory to produce (OWL2) {@link EnhNodeFactory} factories;
 * for {@link OntPersonality ont-personalities}
 */
public final class OWL2ObjectFactories {

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
    public static final EnhNodeFactory ANNOTATION = OntEnhNodeFactories.createCommon(
            OntAnnotationImpl.class,
            OntAnnotations::listRootAnnotations,
            OntAnnotations::testAnnotation
    );

    public static final EnhNodeFactory NAMED_CLASS = OntEntities.createOWL2NamedClassFactory();
    public static final EnhNodeFactory RL_NAMED_CLASS = OntEntities.createOWL2RLNamedClassFactory();
    public static final EnhNodeFactory NAMED_DATARANGE = OntEntities.createOWL2NamedDataRangeFactory();
    public static final EnhNodeFactory ANNOTATION_PROPERTY = OntEntities.createAnnotationPropertyFactory();
    public static final EnhNodeFactory DATATYPE_PROPERTY = OntEntities.createDataPropertyFactory();
    public static final Function<OntConfig, EnhNodeFactory> NAMED_OBJECT_PROPERTY = OntEntities::createNamedObjectPropertyFactory;
    public static final EnhNodeFactory NAMED_INDIVIDUAL = OntEntities.createNamedIndividualFactory();

    public static final Function<OntConfig, EnhNodeFactory> ANY_ENTITY = config -> OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_TYPED,
            NAMED_CLASS,
            NAMED_DATARANGE,
            NAMED_INDIVIDUAL,
            ANNOTATION_PROPERTY,
            DATATYPE_PROPERTY,
            NAMED_OBJECT_PROPERTY.apply(config)
    );
    public static final Function<OntConfig, EnhNodeFactory> RL_ANY_ENTITY = config -> OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_TYPED,
            RL_NAMED_CLASS,
            NAMED_DATARANGE,
            NAMED_INDIVIDUAL,
            ANNOTATION_PROPERTY,
            DATATYPE_PROPERTY,
            NAMED_OBJECT_PROPERTY.apply(config)
    );

    public static final EnhNodeFactory ANONYMOUS_INDIVIDUAL = OntEnhNodeFactories.createCommon(
            OntIndividualImpl.AnonymousImpl.class,
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntIndividuals::testAnonymousIndividual
    );
    public static final EnhNodeFactory ANY_INDIVIDUAL = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_SUBJECT_AND_OBJECT,
            OntIndividual.Named.class,
            OntIndividual.Anonymous.class
    );
    public static final EnhNodeFactory EL_ANY_INDIVIDUAL = NAMED_INDIVIDUAL;
    public static final EnhNodeFactory QL_ANY_INDIVIDUAL = NAMED_INDIVIDUAL;

    public static final EnhNodeFactory INVERSE_OBJECT_PROPERTY = new OntProperties.AnonymousObjectPropertyFactory();

    public static final Function<OntConfig, EnhNodeFactory> ANY_NAMED_PROPERTY = config -> OntEnhNodeFactories.createFrom(
            NAMED_OBJECT_PROPERTY.apply(config),
            DATATYPE_PROPERTY,
            ANNOTATION_PROPERTY
    );
    public static final EnhNodeFactory OBJECT_PROPERTY = new OntProperties.ObjectPropertyExpressionFactory();
    public static final Function<OntConfig, EnhNodeFactory> EL_OBJECT_PROPERTY = NAMED_OBJECT_PROPERTY;

    public static final EnhNodeFactory ANY_DATA_OR_OBJECT_PROPERTY = OntEnhNodeFactories.createFrom(
            DATATYPE_PROPERTY,
            OBJECT_PROPERTY
    );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_DATA_OR_OBJECT_PROPERTY = config ->
            OntEnhNodeFactories.createFrom(
                    DATATYPE_PROPERTY,
                    EL_OBJECT_PROPERTY.apply(config)
            );
    public static final Function<OntConfig, EnhNodeFactory> ANY_PROPERTY = config -> OntProperties.createFactory(config, true);
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_PROPERTY = config -> OntProperties.createFactory(config, false);

    // Class Expressions
    public static final Function<OntConfig, EnhNodeFactory> UNION_OF_CLASS =
            config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
                    OntClassImpl.UnionOfImpl.class,
                    OWL2.unionOf,
                    RDFList.class,
                    OntClassImpl.UnionOfImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> RL_UNION_OF_CLASS =
            OntClasses::createOWL2RLUnionOfFactory;
    public static final Function<OntConfig, EnhNodeFactory> INTERSECTION_OF_CLASS =
            config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
                    OntClassImpl.IntersectionOfImpl.class,
                    OWL2.intersectionOf,
                    RDFList.class,
                    OntClassImpl.IntersectionOfImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> QL_INTERSECTION_OF_CLASS =
            OntClasses::createOWL2QLIntersectionOfFactory;
    public static final Function<OntConfig, EnhNodeFactory> RL_INTERSECTION_OF_CLASS =
            OntClasses::createOWL2RLIntersectionOfFactory;
    public static final Function<OntConfig, EnhNodeFactory> ONE_OF_CLASS =
            config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
                    OntClassImpl.OneOfImpl.class,
                    OWL2.oneOf,
                    RDFList.class,
                    OntClassImpl.OneOfImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> RL_ONE_OF_CLASS =
            config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
                    OntClassImpl.RLOneOfImpl.class,
                    OWL2.oneOf,
                    RDFList.class,
                    OntClassImpl.RLOneOfImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> EL_ONE_OF_CLASS = OntClasses::createOWL2ELObjectOneOfFactory;
    public static final Function<OntConfig, EnhNodeFactory> COMPLEMENT_OF_CLASS =
            config -> OntClasses.createBooleanConnectivesAndIndividualEnumerationFactory(
                    OntClassImpl.ComplementOfImpl.class,
                    OWL2.complementOf,
                    OntClass.class,
                    OntClassImpl.ComplementOfImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> QL_COMPLEMENT_OF_CLASS = OntClasses::createOWL2QLComplementOfFactory;
    public static final Function<OntConfig, EnhNodeFactory> RL_COMPLEMENT_OF_CLASS = OntClasses::createOWL2RLComplementOfFactory;

    public static final Function<OntConfig, EnhNodeFactory> OBJECT_SOME_VALUES_FROM_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.ObjectSomeValuesFromImpl.class,
                    OntClasses.RestrictionType.OBJECT,
                    OntClasses.ObjectRestrictionType.CLASS,
                    OWL2.someValuesFrom,
                    OntClassImpl.ObjectSomeValuesFromImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> QL_OBJECT_SOME_VALUES_FROM_CLASS =
            OntClasses::createOWL2QLObjectSomeValuesFromFactory;
    public static final Function<OntConfig, EnhNodeFactory> RL_OBJECT_SOME_VALUES_FROM_CLASS =
            OntClasses::createOWL2RLObjectSomeValuesFromFactory;
    public static final Function<OntConfig, EnhNodeFactory> DATA_SOME_VALUES_FROM_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.DataSomeValuesFromImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.DATA_RANGE,
                    OWL2.someValuesFrom,
                    OntClassImpl.DataSomeValuesFromImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> RL_DATA_SOME_VALUES_FROM_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.RLDataSomeValuesFromImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.DATA_RANGE,
                    OWL2.someValuesFrom,
                    OntClassImpl.RLDataSomeValuesFromImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> QL_DATA_SOME_VALUES_FROM_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.QLDataSomeValuesFromImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.DATA_RANGE,
                    OWL2.someValuesFrom,
                    OntClassImpl.QLDataSomeValuesFromImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_ALL_VALUES_FROM_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.ObjectAllValuesFromImpl.class,
                    OntClasses.RestrictionType.OBJECT,
                    OntClasses.ObjectRestrictionType.CLASS,
                    OWL2.allValuesFrom,
                    OntClassImpl.ObjectAllValuesFromImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> RL_OBJECT_ALL_VALUES_FROM_CLASS =
            OntClasses::createOWL2RLObjectAllValuesFromFactory;
    public static final Function<OntConfig, EnhNodeFactory> DATA_ALL_VALUES_FROM_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.DataAllValuesFromImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.DATA_RANGE,
                    OWL2.allValuesFrom,
                    OntClassImpl.DataAllValuesFromImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> RL_DATA_ALL_VALUES_FROM_CLASS =
            config -> OntClasses.createComponentRestrictionFactory(
                    OntClassImpl.RLDataAllValuesFromImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.DATA_RANGE,
                    OWL2.allValuesFrom,
                    OntClassImpl.RLDataAllValuesFromImpl::new,
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
    public static final Function<OntConfig, EnhNodeFactory> RL_OBJECT_MAX_CARDINALITY_CLASS =
            OntClasses::createOWL2RLObjectMaxCardinalityFactory;
    public static final Function<OntConfig, EnhNodeFactory> DATA_CARDINALITY_CLASS =
            config -> OntClasses.createCardinalityRestrictionFactory(
                    OntClassImpl.DataCardinalityImpl.class,
                    OntClasses.RestrictionType.DATA,
                    OntClasses.ObjectRestrictionType.DATA_RANGE,
                    OntClassImpl.CardinalityType.EXACTLY,
                    OntClassImpl.DataCardinalityImpl::new,
                    config);
    public static final Function<OntConfig, EnhNodeFactory> RL_DATA_MAX_CARDINALITY_CLASS =
            OntClasses::createOWL2RLDataMaxCardinalityFactory;
    public static final Function<OntConfig, EnhNodeFactory> OBJECT_CARDINALITY_CLASS =
            config -> OntClasses.createCardinalityRestrictionFactory(
                    OntClassImpl.ObjectCardinalityImpl.class,
                    OntClasses.RestrictionType.OBJECT,
                    OntClasses.ObjectRestrictionType.CLASS,
                    OntClassImpl.CardinalityType.EXACTLY,
                    OntClassImpl.ObjectCardinalityImpl::new,
                    config);
    public static final EnhNodeFactory HAS_SELF_CLASS = OntEnhNodeFactories.createCommon(
            new OntClasses.HasSelfMaker(),
            OntClasses.RESTRICTION_FINDER,
            EnhNodeFilter.ANON.and(new OntClasses.HasSelfFilter())
    );
    // see <a href="https://www.w3.org/TR/owl2-quick-reference/#Class_Expressions">Restrictions Using n-ary Data Range</a>
    public static final EnhNodeFactory NARY_DATA_ALL_VALUES_FROM_CLASS = OntClasses.createNaryRestrictionFactory(
            OntClassImpl.NaryDataAllValuesFromImpl.class,
            OWL2.allValuesFrom
    );
    public static final EnhNodeFactory NARY_DATA_SOME_VALUES_FROM_CLASS = OntClasses.createNaryRestrictionFactory(
            OntClassImpl.NaryDataSomeValuesFromImpl.class,
            OWL2.someValuesFrom
    );
    // Boolean Connectives and Enumeration of Individuals (with except of ComplementOf):
    public static final Function<OntConfig, EnhNodeFactory> ANY_COLLECTION_OF_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.UNION_OF,
                    OntClasses.Type.INTERSECTION_OF,
                    OntClasses.Type.ONE_OF
            );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_COLLECTION_OF_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    false,
                    List.of(OntClasses.Type.INTERSECTION_OF, OntClasses.Type.ONE_OF),
                    List.of(OntClasses.Type.ONE_OF)
            );
    public static final Function<OntConfig, EnhNodeFactory> QL_ANY_COLLECTION_OF_CLASS = QL_INTERSECTION_OF_CLASS;
    public static final Function<OntConfig, EnhNodeFactory> RL_ANY_COLLECTION_OF_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    false,
                    List.of(OntClasses.Type.UNION_OF,
                            OntClasses.Type.INTERSECTION_OF,
                            OntClasses.Type.ONE_OF),
                    List.of(OntClasses.Type.INTERSECTION_OF,
                            OntClasses.Type.UNION_OF)
            );
    // Boolean Connectives and Enumeration of Individuals + ComplementOf):
    public static final Function<OntConfig, EnhNodeFactory> ANY_LOGICAL_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.UNION_OF,
                    OntClasses.Type.INTERSECTION_OF,
                    OntClasses.Type.ONE_OF,
                    OntClasses.Type.COMPLEMENT_OF
            );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_LOGICAL_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    false,
                    List.of(OntClasses.Type.INTERSECTION_OF, OntClasses.Type.ONE_OF),
                    List.of(OntClasses.Type.ONE_OF)
            );
    public static final Function<OntConfig, EnhNodeFactory> QL_ANY_LOGICAL_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    false,
                    List.of(OntClasses.Type.INTERSECTION_OF,
                            OntClasses.Type.COMPLEMENT_OF),
                    List.of(OntClasses.Type.INTERSECTION_OF,
                            OntClasses.Type.COMPLEMENT_OF)
            );
    public static final Function<OntConfig, EnhNodeFactory> RL_ANY_LOGICAL_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    false,
                    List.of(
                            OntClasses.Type.UNION_OF,
                            OntClasses.Type.INTERSECTION_OF,
                            OntClasses.Type.ONE_OF,
                            OntClasses.Type.COMPLEMENT_OF),
                    List.of(OntClasses.Type.INTERSECTION_OF,
                            OntClasses.Type.UNION_OF,
                            OntClasses.Type.COMPLEMENT_OF)
            );
    // Value Restrictions:
    public static final Function<OntConfig, EnhNodeFactory> ANY_VALUE_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_ALL_VALUES_FROM,
                    OntClasses.Type.DATA_HAS_VALUE
            );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_VALUE_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_HAS_VALUE
            );
    public static final Function<OntConfig, EnhNodeFactory> QL_ANY_VALUE_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    false,
                    List.of(OntClasses.Type.OBJECT_SOME_VALUES_FROM, OntClasses.Type.DATA_SOME_VALUES_FROM),
                    List.of(OntClasses.Type.OBJECT_SOME_VALUES_FROM)
            );
    public static final Function<OntConfig, EnhNodeFactory> RL_ANY_VALUE_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    false,
                    List.of(OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                            OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                            OntClasses.Type.OBJECT_HAS_VALUE,
                            OntClasses.Type.DATA_SOME_VALUES_FROM,
                            OntClasses.Type.DATA_ALL_VALUES_FROM,
                            OntClasses.Type.DATA_HAS_VALUE),
                    List.of(OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                            OntClasses.Type.OBJECT_ALL_VALUES_FROM)
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
    public static final Function<OntConfig, EnhNodeFactory> RL_ANY_CARDINALITY_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    false,
                    List.of(
                            OntClasses.Type.OBJECT_MAX_CARDINALITY,
                            OntClasses.Type.DATA_MAX_CARDINALITY),
                    List.of(OntClasses.Type.OBJECT_MAX_CARDINALITY,
                            OntClasses.Type.DATA_MAX_CARDINALITY)
            );
    // Cardinality + Existential/Universal Restrictions + Value Restrictions:
    public static final Function<OntConfig, EnhNodeFactory> ANY_COMPONENT_RESTRICTION_CLASS =
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
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_COMPONENT_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_HAS_VALUE
            );
    public static final Function<OntConfig, EnhNodeFactory> QL_ANY_COMPONENT_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    false,
                    List.of(OntClasses.Type.OBJECT_SOME_VALUES_FROM, OntClasses.Type.DATA_SOME_VALUES_FROM),
                    List.of(OntClasses.Type.OBJECT_SOME_VALUES_FROM)
            );
    public static final Function<OntConfig, EnhNodeFactory> RL_ANY_COMPONENT_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    false,
                    List.of(
                            OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                            OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                            OntClasses.Type.OBJECT_MAX_CARDINALITY,
                            OntClasses.Type.OBJECT_HAS_VALUE,
                            OntClasses.Type.DATA_SOME_VALUES_FROM,
                            OntClasses.Type.DATA_ALL_VALUES_FROM,
                            OntClasses.Type.DATA_MAX_CARDINALITY,
                            OntClasses.Type.DATA_HAS_VALUE
                    ),
                    List.of(OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                            OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                            OntClasses.Type.OBJECT_MAX_CARDINALITY,
                            OntClasses.Type.DATA_MAX_CARDINALITY)
            );
    // Cardinality + Existential/Universal Restrictions + Local reflexivity (hasSelf) + Value Restrictions
    // (all them have owl:onProperty):
    public static final Function<OntConfig, EnhNodeFactory> ANY_UNARY_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                    OntClasses.Type.OBJECT_MIN_CARDINALITY,
                    OntClasses.Type.OBJECT_MAX_CARDINALITY,
                    OntClasses.Type.OBJECT_EXACT_CARDINALITY,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.OBJECT_HAS_SELF,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_ALL_VALUES_FROM,
                    OntClasses.Type.DATA_MIN_CARDINALITY,
                    OntClasses.Type.DATA_MAX_CARDINALITY,
                    OntClasses.Type.DATA_EXACT_CARDINALITY,
                    OntClasses.Type.DATA_HAS_VALUE
            );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_UNARY_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.OBJECT_HAS_SELF,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_HAS_VALUE
            );
    public static final Function<OntConfig, EnhNodeFactory> QL_ANY_UNARY_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    false,
                    List.of(OntClasses.Type.OBJECT_SOME_VALUES_FROM, OntClasses.Type.DATA_SOME_VALUES_FROM),
                    List.of(OntClasses.Type.OBJECT_SOME_VALUES_FROM)
            );
    public static final Function<OntConfig, EnhNodeFactory> RL_ANY_UNARY_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    false,
                    List.of(OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                            OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                            OntClasses.Type.OBJECT_MAX_CARDINALITY,
                            OntClasses.Type.OBJECT_HAS_VALUE,
                            OntClasses.Type.DATA_SOME_VALUES_FROM,
                            OntClasses.Type.DATA_ALL_VALUES_FROM,
                            OntClasses.Type.DATA_MAX_CARDINALITY,
                            OntClasses.Type.DATA_HAS_VALUE),
                    List.of(OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                            OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                            OntClasses.Type.OBJECT_MAX_CARDINALITY,
                            OntClasses.Type.DATA_MAX_CARDINALITY)
            );
    // Cardinality + Existential/Universal Restrictions + N-ary existential/universal +
    // Local reflexivity (hasSelf) + Value Restrictions:
    public static final Function<OntConfig, EnhNodeFactory> ANY_RESTRICTION_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                    OntClasses.Type.OBJECT_MIN_CARDINALITY,
                    OntClasses.Type.OBJECT_MAX_CARDINALITY,
                    OntClasses.Type.OBJECT_EXACT_CARDINALITY,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.OBJECT_HAS_SELF,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_ALL_VALUES_FROM,
                    OntClasses.Type.DATA_MIN_CARDINALITY,
                    OntClasses.Type.DATA_MAX_CARDINALITY,
                    OntClasses.Type.DATA_EXACT_CARDINALITY,
                    OntClasses.Type.DATA_HAS_VALUE,
                    OntClasses.Type.DATA_NARY_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_NARY_ALL_VALUES_FROM
            );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_RESTRICTION_CLASS = EL_ANY_UNARY_RESTRICTION_CLASS;
    public static final Function<OntConfig, EnhNodeFactory> QL_ANY_RESTRICTION_CLASS = QL_ANY_UNARY_RESTRICTION_CLASS;
    public static final Function<OntConfig, EnhNodeFactory> RL_ANY_RESTRICTION_CLASS = RL_ANY_UNARY_RESTRICTION_CLASS;

    // All Class Expressions:
    public static final Function<OntConfig, EnhNodeFactory> ANY_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    true,
                    OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                    OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                    OntClasses.Type.OBJECT_MIN_CARDINALITY,
                    OntClasses.Type.OBJECT_MAX_CARDINALITY,
                    OntClasses.Type.OBJECT_EXACT_CARDINALITY,
                    OntClasses.Type.OBJECT_HAS_VALUE,
                    OntClasses.Type.OBJECT_HAS_SELF,
                    OntClasses.Type.DATA_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_ALL_VALUES_FROM,
                    OntClasses.Type.DATA_MIN_CARDINALITY,
                    OntClasses.Type.DATA_MAX_CARDINALITY,
                    OntClasses.Type.DATA_EXACT_CARDINALITY,
                    OntClasses.Type.DATA_HAS_VALUE,
                    OntClasses.Type.DATA_NARY_SOME_VALUES_FROM,
                    OntClasses.Type.DATA_NARY_ALL_VALUES_FROM,
                    OntClasses.Type.UNION_OF,
                    OntClasses.Type.INTERSECTION_OF,
                    OntClasses.Type.ONE_OF,
                    OntClasses.Type.COMPLEMENT_OF
            );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    true,
                    List.of(OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                            OntClasses.Type.OBJECT_HAS_VALUE,
                            OntClasses.Type.OBJECT_HAS_SELF,
                            OntClasses.Type.DATA_SOME_VALUES_FROM,
                            OntClasses.Type.DATA_HAS_VALUE,
                            OntClasses.Type.DATA_NARY_SOME_VALUES_FROM,
                            OntClasses.Type.DATA_NARY_ALL_VALUES_FROM,
                            OntClasses.Type.INTERSECTION_OF,
                            OntClasses.Type.ONE_OF),
                    List.of(OntClasses.Type.ONE_OF)
            );
    public static final Function<OntConfig, EnhNodeFactory> QL_ANY_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    true,
                    List.of(OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                            OntClasses.Type.DATA_SOME_VALUES_FROM,
                            OntClasses.Type.INTERSECTION_OF,
                            OntClasses.Type.COMPLEMENT_OF),
                    List.of(OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                            OntClasses.Type.INTERSECTION_OF,
                            OntClasses.Type.COMPLEMENT_OF)
            );

    // All Class Expressions:
    public static final Function<OntConfig, EnhNodeFactory> RL_ANY_CLASS =
            config -> OntClasses.createClassExpressionFactory(
                    config,
                    OntSimpleClassImpl.RLNamedImpl::new,
                    List.of(OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                            OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                            OntClasses.Type.OBJECT_MAX_CARDINALITY,
                            OntClasses.Type.OBJECT_HAS_VALUE,
                            OntClasses.Type.DATA_SOME_VALUES_FROM,
                            OntClasses.Type.DATA_ALL_VALUES_FROM,
                            OntClasses.Type.DATA_MAX_CARDINALITY,
                            OntClasses.Type.DATA_HAS_VALUE,
                            OntClasses.Type.UNION_OF,
                            OntClasses.Type.INTERSECTION_OF,
                            OntClasses.Type.ONE_OF,
                            OntClasses.Type.COMPLEMENT_OF),
                    List.of(OntClasses.Type.OBJECT_SOME_VALUES_FROM,
                            OntClasses.Type.OBJECT_ALL_VALUES_FROM,
                            OntClasses.Type.OBJECT_MAX_CARDINALITY,
                            OntClasses.Type.DATA_MAX_CARDINALITY,
                            OntClasses.Type.UNION_OF,
                            OntClasses.Type.INTERSECTION_OF,
                            OntClasses.Type.COMPLEMENT_OF)
            );

    // Data Range Expressions
    public static final Function<OntConfig, EnhNodeFactory> RESTRICTION_DATARANGE = config -> OntEnhNodeFactories.createCommon(
            OntDataRangeImpl.RestrictionImpl.class,
            OntDataRanges.makeOWLFinder(config),
            OntDataRanges.makeOWLFilter(config)
                    .and(new EnhNodeFilter.HasPredicate(OWL2.onDatatype))
                    .and(new EnhNodeFilter.HasPredicate(OWL2.withRestrictions))
    );
    public static final Function<OntConfig, EnhNodeFactory> COMPLEMENT_OF_DATARANGE =
            config -> OntEnhNodeFactories.createCommon(
                    OntDataRangeImpl.ComplementOfImpl.class,
                    OntDataRanges.makeOWLFinder(config),
                    OntDataRanges.makeOWLFilter(config).and(new EnhNodeFilter.HasPredicate(OWL2.datatypeComplementOf))
            );
    public static final Function<OntConfig, EnhNodeFactory> ONE_OF_DATARANGE =
            config -> OntEnhNodeFactories.createCommon(
                    OntDataRangeImpl.OneOfImpl.class,
                    OntDataRanges.makeOWLFinder(config),
                    OntDataRanges.makeOWLFilter(config).and(new EnhNodeFilter.HasPredicate(OWL2.oneOf)));
    public static final Function<OntConfig, EnhNodeFactory> EL_ONE_OF_DATARANGE = OntDataRanges::createOWL2ELOneOfEnumerationFactory;
    public static final Function<OntConfig, EnhNodeFactory> UNION_OF_DATARANGE =
            config -> OntEnhNodeFactories.createCommon(
                    OntDataRangeImpl.UnionOfImpl.class,
                    OntDataRanges.makeOWLFinder(config),
                    OntDataRanges.makeOWLFilter(config).and(new EnhNodeFilter.HasPredicate(OWL2.unionOf))
            );
    public static final Function<OntConfig, EnhNodeFactory> INTERSECTION_OF_DATARANGE = config -> OntEnhNodeFactories.createCommon(
            OntDataRangeImpl.IntersectionOfImpl.class,
            OntDataRanges.makeOWLFinder(config),
            OntDataRanges.makeOWLFilter(config).and(new EnhNodeFilter.HasPredicate(OWL2.intersectionOf))
    );
    public static final Function<OntConfig, EnhNodeFactory> ANY_COMPONENTS_DATARANGE = config -> OntEnhNodeFactories.createFrom(
            OntDataRanges.makeOWLFinder(config),
            OntDataRange.OneOf.class,
            OntDataRange.Restriction.class,
            OntDataRange.UnionOf.class,
            OntDataRange.IntersectionOf.class
    );
    public static final Function<OntConfig, EnhNodeFactory> EL_ANY_COMPONENTS_DATARANGE = config -> OntEnhNodeFactories.createFrom(
            OntDataRanges.makeOWLFinder(config),
            OntDataRange.OneOf.class,
            OntDataRange.IntersectionOf.class
    );
    public static final Function<OntConfig, EnhNodeFactory> QL_ANY_COMPONENTS_DATARANGE = INTERSECTION_OF_DATARANGE;
    public static final Function<OntConfig, EnhNodeFactory> RL_ANY_COMPONENTS_DATARANGE = INTERSECTION_OF_DATARANGE;
    public static final EnhNodeFactory ANY_DATARANGE = OntDataRanges.createDataRangeFactory(
            OntDataRanges.Type.RESTRICTION,
            OntDataRanges.Type.COMPLEMENT_OF,
            OntDataRanges.Type.ONE_OF,
            OntDataRanges.Type.UNION_OF,
            OntDataRanges.Type.INTERSECTION_OF
    );
    public static final EnhNodeFactory EL_ANY_DATARANGE = OntDataRanges.createDataRangeFactory(
            OntDataRanges.Type.ONE_OF,
            OntDataRanges.Type.INTERSECTION_OF
    );
    public static final EnhNodeFactory QL_ANY_DATARANGE = OntDataRanges.createDataRangeFactory(
            OntDataRanges.Type.INTERSECTION_OF
    );
    public static final EnhNodeFactory RL_ANY_DATARANGE = OntDataRanges.createDataRangeFactory(
            OntDataRanges.Type.INTERSECTION_OF
    );

    public static final EnhNodeFactory LENGTH_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.LengthImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.length),
            OntDataRanges.makeFacetRestrictionFilter(XSD.length)
    );
    public static final EnhNodeFactory MIN_LENGTH_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.MinLengthImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.minLength),
            OntDataRanges.makeFacetRestrictionFilter(XSD.minLength)
    );
    public static final EnhNodeFactory MAX_LENGTH_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.MaxLengthImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.maxLength),
            OntDataRanges.makeFacetRestrictionFilter(XSD.maxLength)
    );
    public static final EnhNodeFactory MIN_INCLUSIVE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.MinInclusiveImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.minInclusive),
            OntDataRanges.makeFacetRestrictionFilter(XSD.minInclusive)
    );
    public static final EnhNodeFactory MAX_INCLUSIVE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.MaxInclusiveImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.maxInclusive),
            OntDataRanges.makeFacetRestrictionFilter(XSD.maxInclusive)
    );
    public static final EnhNodeFactory MIN_EXCLUSIVE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.MinExclusiveImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.minExclusive),
            OntDataRanges.makeFacetRestrictionFilter(XSD.minExclusive)
    );
    public static final EnhNodeFactory MAX_EXCLUSIVE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.MaxExclusiveImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.maxExclusive),
            OntDataRanges.makeFacetRestrictionFilter(XSD.maxExclusive)
    );
    public static final EnhNodeFactory TOTAL_DIGITS_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.TotalDigitsImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.totalDigits),
            OntDataRanges.makeFacetRestrictionFilter(XSD.totalDigits)
    );
    public static final EnhNodeFactory FRACTION_DIGITS_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.FractionDigitsImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.fractionDigits),
            OntDataRanges.makeFacetRestrictionFilter(XSD.fractionDigits)
    );
    public static final EnhNodeFactory PATTERN_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.PatternImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(XSD.pattern),
            OntDataRanges.makeFacetRestrictionFilter(XSD.pattern)
    );
    public static final EnhNodeFactory LANG_RANGE_FACET_RESTRICTION = OntEnhNodeFactories.createCommon(
            OntFacetRestrictionImpl.LangRangeImpl.class,
            OntDataRanges.makeFacetRestrictionFinder(RDF.langRange),
            OntDataRanges.makeFacetRestrictionFilter(RDF.langRange)
    );
    public static final EnhNodeFactory ANY_FACET_RESTRICTION = OntEnhNodeFactories.createFrom(
            EnhNodeFinder.ANY_BLANK_SUBJECT,
            OntFacetRestriction.Length.class,
            OntFacetRestriction.MinLength.class,
            OntFacetRestriction.MaxLength.class,
            OntFacetRestriction.MinInclusive.class,
            OntFacetRestriction.MaxInclusive.class,
            OntFacetRestriction.MinExclusive.class,
            OntFacetRestriction.MaxExclusive.class,
            OntFacetRestriction.TotalDigits.class,
            OntFacetRestriction.FractionDigits.class,
            OntFacetRestriction.Pattern.class,
            OntFacetRestriction.LangRange.class
    );

    public static final EnhNodeFactory CLASSES_DISJOINT = OntDisjoints.createDisjointClassesFactory(1);
    public static final EnhNodeFactory EL_CLASSES_DISJOINT = OntDisjoints.createDisjointClassesFactory(2);
    public static final EnhNodeFactory QL_RL_CLASSES_DISJOINT = OntDisjoints.createQLRLDisjointClassesFactory();
    public static final Function<OntConfig, EnhNodeFactory> DIFFERENT_INDIVIDUALS_DISJOINT =
            OntDisjoints::createDLFullDifferentIndividualsFactory;
    public static final EnhNodeFactory EL_QL_RL_DIFFERENT_INDIVIDUALS_DISJOINT =
            OntDisjoints.createELQLRLDifferentIndividualsFactory();
    public static final EnhNodeFactory OBJECT_PROPERTIES_DISJOINT = OntDisjoints.createDisjointObjectPropertiesFactory(1);
    public static final EnhNodeFactory QL_RL_OBJECT_PROPERTIES_DISJOINT = OntDisjoints.createDisjointObjectPropertiesFactory(2);
    public static final EnhNodeFactory DATA_PROPERTIES_DISJOINT = OntDisjoints.createDisjointDataPropertiesFactory(1);
    public static final EnhNodeFactory QL_RL_DATA_PROPERTIES_DISJOINT = OntDisjoints.createDisjointDataPropertiesFactory(2);

    public static final EnhNodeFactory ANY_PROPERTIES_DISJOINT = OntEnhNodeFactories.createFrom(
            OntDisjoints.PROPERTIES_FINDER,
            OntDisjoint.ObjectProperties.class,
            OntDisjoint.DataProperties.class
    );
    public static final EnhNodeFactory ANY_DISJOINT = OntEnhNodeFactories.createFrom(
            OntDisjoints.DISJOINT_FINDER,
            OntDisjoint.ObjectProperties.class,
            OntDisjoint.DataProperties.class,
            OntDisjoint.Classes.class,
            OntDisjoint.Individuals.class
    );
    public static final EnhNodeFactory EL_ANY_DISJOINT = OntEnhNodeFactories.createFrom(
            OntDisjoints.DISJOINT_FINDER,
            OntDisjoint.Classes.class,
            OntDisjoint.Individuals.class
    );

    public static final EnhNodeFactory OBJECT_NEGATIVE_PROPERTY_ASSERTION = OntEnhNodeFactories.createCommon(
            OntNegativePropertyAssertionImpl.ObjectAssertionImpl.class,
            OntProperties.NEGATIVE_PROPERTY_ASSERTION_FINDER,
            OntProperties.NEGATIVE_PROPERTY_ASSERTION_FILTER,
            new EnhNodeFilter.HasPredicate(OWL2.targetIndividual)
    );
    public static final EnhNodeFactory DATA_NEGATIVE_PROPERTY_ASSERTION = OntEnhNodeFactories.createCommon(
            OntNegativePropertyAssertionImpl.DataAssertionImpl.class,
            OntProperties.NEGATIVE_PROPERTY_ASSERTION_FINDER,
            OntProperties.NEGATIVE_PROPERTY_ASSERTION_FILTER,
            new EnhNodeFilter.HasPredicate(OWL2.targetValue)
    );
    public static final EnhNodeFactory ANY_NEGATIVE_PROPERTY_ASSERTION = OntEnhNodeFactories.createFrom(
            OntProperties.NEGATIVE_PROPERTY_ASSERTION_FINDER,
            OntNegativeAssertion.WithObjectProperty.class,
            OntNegativeAssertion.WithDataProperty.class
    );

}
