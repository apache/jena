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

package org.apache.jena.ontapi.common;

import org.apache.jena.enhanced.Personality;
import org.apache.jena.graph.Node;
import org.apache.jena.ontapi.impl.factories.OWL1ObjectFactories;
import org.apache.jena.ontapi.impl.factories.OWL2ObjectFactories;
import org.apache.jena.ontapi.impl.factories.RDFSObjectFactories;
import org.apache.jena.ontapi.impl.factories.STDObjectFactories;
import org.apache.jena.ontapi.impl.factories.SWRLObjectFactories;
import org.apache.jena.ontapi.model.OntAnnotation;
import org.apache.jena.ontapi.model.OntAnnotationProperty;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntDisjoint;
import org.apache.jena.ontapi.model.OntEntity;
import org.apache.jena.ontapi.model.OntFacetRestriction;
import org.apache.jena.ontapi.model.OntID;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntNamedProperty;
import org.apache.jena.ontapi.model.OntNegativeAssertion;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontapi.model.OntRelationalProperty;
import org.apache.jena.ontapi.model.OntSWRL;
import org.apache.jena.ontapi.utils.StdModels;
import org.apache.jena.rdf.model.Alt;
import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Container;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.vocabulary.OWL2;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Settings and personalities that are used for constructing {@link OntModel}.
 * An access point to several predefined {@link OntPersonality Ontology Personality} constants.
 */
@SuppressWarnings("WeakerAccess")
public class OntPersonalities {

    public static final OntPersonality.Builtins RDFS_BUILTINS = createBuiltinsVocabulary(OntVocabulary.RDFS);
    public static final OntPersonality.Builtins OWL2_FULL_BUILTINS = createBuiltinsVocabulary(
            OntVocabulary.OWL2_FULL.and(OntVocabulary.DC).and(OntVocabulary.SKOS).and(OntVocabulary.SWRL));
    public static final OntPersonality.Builtins OWL2_EL_BUILTINS = createBuiltinsVocabulary(OntVocabulary.OWL2_EL);
    public static final OntPersonality.Builtins OWL2_QL_BUILTINS = createBuiltinsVocabulary(OntVocabulary.OWL2_QL);
    public static final OntPersonality.Builtins OWL2_RL_BUILTINS = createBuiltinsVocabulary(OntVocabulary.OWL2_RL);
    public static final OntPersonality.Builtins OWL1_FULL_BUILTINS = createBuiltinsVocabulary(OntVocabulary.OWL1_FULL);
    public static final OntPersonality.Builtins OWL1_LITE_BUILTINS = createBuiltinsVocabulary(OntVocabulary.OWL1_LITE);

    public static final OntPersonality.Reserved RDFS_RESERVED = createReservedVocabulary(OntVocabulary.RDFS);
    public static final OntPersonality.Reserved OWL2_RESERVED = createReservedVocabulary(
            OntVocabulary.OWL2_FULL.and(OntVocabulary.DC).and(OntVocabulary.SKOS).and(OntVocabulary.SWRL));
    public static final OntPersonality.Reserved OWL1_RESERVED = createReservedVocabulary(OntVocabulary.OWL1_FULL);

    public static final OntPersonality.Punnings OWL_DL1_PUNNINGS = createPunningsVocabulary(PunningsMode.DL1_COMPATIBLE);
    public static final OntPersonality.Punnings OWL_DL2_PUNNINGS = createPunningsVocabulary(PunningsMode.DL2);
    public static final OntPersonality.Punnings OWL_DL_WEAK_PUNNINGS = createPunningsVocabulary(PunningsMode.DL_WEAK);
    public static final OntPersonality.Punnings OWL_NO_PUNNINGS = createPunningsVocabulary(PunningsMode.FULL);
    public static final OntPersonality.Punnings RDFS_NO_PUNNINGS = createPunningsVocabulary(PunningsMode.FULL);

    /**
     * Standard resources. Private access since this constant is mutable.
     *
     * @see org.apache.jena.enhanced.BuiltinPersonalities#model
     */
    public static final Personality<RDFNode> STANDARD_PERSONALITY = new Personality<RDFNode>()
            .add(RDFNode.class, STDObjectFactories.NODE)
            .add(Resource.class, STDObjectFactories.RESOURCE)
            .add(Property.class, STDObjectFactories.PROPERTY)
            .add(Literal.class, STDObjectFactories.LITERAL)
            .add(Container.class, STDObjectFactories.RESOURCE)
            .add(Alt.class, STDObjectFactories.ALT)
            .add(Bag.class, STDObjectFactories.BAG)
            .add(Seq.class, STDObjectFactories.SEQ)
            .add(RDFList.class, STDObjectFactories.RDF_LIST);

    /**
     * For RDFS Ontologies, limited functionality.
     *
     * @see <a href="https://www.w3.org/TR/rdf12-schema/">RDF 1.2 Schema</a>
     */
    private static final OntObjectPersonalityBuilder RDFS_OBJECT_FACTORIES = templatePersonalityBuilder()
            .setName("RDFS")
            .addPersonality(STANDARD_PERSONALITY)
            .add(OntObject.class, RDFSObjectFactories.ANY_OBJECT)
            .add(OntEntity.class, RDFSObjectFactories.ANY_ENTITY)
            .add(OntIndividual.Named.class, RDFSObjectFactories.NAMED_INDIVIDUAL)
            .add(OntIndividual.Anonymous.class, RDFSObjectFactories.ANONYMOUS_INDIVIDUAL)
            .add(OntIndividual.class, RDFSObjectFactories.ANY_INDIVIDUAL)
            .add(OntProperty.class, RDFSObjectFactories.PROPERTY)
            .add(OntAnnotationProperty.class, RDFSObjectFactories.ANNOTATION_PROPERTY)
            .add(OntClass.class, RDFSObjectFactories.ANY_CLASS)
            .add(OntClass.Named.class, RDFSObjectFactories.NAMED_CLASS);

    /**
     * Default personality builder for SWRL. Private access since this constant is mutable.
     */
    private static final OntObjectPersonalityBuilder SWRL_OBJECT_FACTORIES = templatePersonalityBuilder()
            .add(OntSWRL.Variable.class, SWRLObjectFactories.VARIABLE_SWRL)
            .add(OntSWRL.Builtin.class, SWRLObjectFactories.BUILTIN_SWRL)
            .add(OntSWRL.IArg.class, SWRLObjectFactories.IARG_SWRL)
            .add(OntSWRL.DArg.class, SWRLObjectFactories.DARG_SWRL)
            .add(OntSWRL.Arg.class, SWRLObjectFactories.ANY_ARG_SWRL)
            .add(OntSWRL.Atom.WithBuiltin.class, SWRLObjectFactories.BUILT_IN_ATOM_SWRL)
            .add(OntSWRL.Atom.WithClass.class, SWRLObjectFactories.CLASS_ATOM_SWRL)
            .add(OntSWRL.Atom.WithDataRange.class, SWRLObjectFactories.DATA_RANGE_ATOM_SWRL)
            .add(OntSWRL.Atom.WithObjectProperty.class, SWRLObjectFactories.INDIVIDUAL_ATOM_SWRL)
            .add(OntSWRL.Atom.WithDataProperty.class, SWRLObjectFactories.DATA_VALUED_ATOM_SWRL)
            .add(OntSWRL.Atom.WithDifferentIndividuals.class, SWRLObjectFactories.DIFFERENT_INDIVIDUALS_ATOM_SWRL)
            .add(OntSWRL.Atom.WithSameIndividuals.class, SWRLObjectFactories.SAME_INDIVIDUALS_ATOM_SWRL)
            .add(OntSWRL.Atom.Unary.class, SWRLObjectFactories.ANY_UNARY_ATOM_SWRL)
            .add(OntSWRL.Atom.Binary.class, SWRLObjectFactories.ANY_BINARY_ATOM_SWRL)
            .add(OntSWRL.Atom.class, SWRLObjectFactories.ANY_ATOM_SWRL)
            .add(OntSWRL.Imp.class, SWRLObjectFactories.IMPL_SWRL)
            .add(OntSWRL.class, SWRLObjectFactories.ANY_OBJECT_SWRL);

    /**
     * Default personality builder for OWL2 (FULL + DL) and SWRL. Private access since this constant is mutable.
     */
    private static final OntObjectPersonalityBuilder OWL2_OBJECT_FACTORIES = templatePersonalityBuilder()
            .setName("OWL2")
            .addPersonality(STANDARD_PERSONALITY)
            // the base ontology object:
            .add(OntObject.class, OWL2ObjectFactories.ANY_OBJECT)

            // ont-id:
            .add(OntID.class, OWL2ObjectFactories.ID)

            // annotation object:
            .add(OntAnnotation.class, OWL2ObjectFactories.ANNOTATION)

            // entities:
            .add(OntClass.Named.class, OWL2ObjectFactories.NAMED_CLASS)
            .add(OntDataRange.Named.class, OWL2ObjectFactories.NAMED_DATARANGE)
            .add(OntIndividual.Named.class, OWL2ObjectFactories.NAMED_INDIVIDUAL)
            .add(OntObjectProperty.Named.class, OWL2ObjectFactories.NAMED_OBJECT_PROPERTY)
            .add(OntDataProperty.class, OWL2ObjectFactories.DATATYPE_PROPERTY)
            .add(OntAnnotationProperty.class, OWL2ObjectFactories.ANNOTATION_PROPERTY)
            .add(OntEntity.class, OWL2ObjectFactories.ANY_ENTITY)

            // individuals:
            .add(OntIndividual.Anonymous.class, OWL2ObjectFactories.ANONYMOUS_INDIVIDUAL)
            .add(OntIndividual.class, OWL2ObjectFactories.ANY_INDIVIDUAL)

            // property expressions:
            .add(OntObjectProperty.Inverse.class, OWL2ObjectFactories.INVERSE_OBJECT_PROPERTY)
            .add(OntObjectProperty.class, OWL2ObjectFactories.OBJECT_PROPERTY)
            .add(OntRelationalProperty.class, OWL2ObjectFactories.ANY_DATA_OR_OBJECT_PROPERTY)
            .add(OntNamedProperty.class, OWL2ObjectFactories.ANY_NAMED_PROPERTY)
            .add(OntProperty.class, OWL2ObjectFactories.ANY_PROPERTY)

            // class expressions:
            .add(OntClass.ObjectSomeValuesFrom.class, OWL2ObjectFactories.OBJECT_SOME_VALUES_FROM_CLASS)
            .add(OntClass.DataSomeValuesFrom.class, OWL2ObjectFactories.DATA_SOME_VALUES_FROM_CLASS)
            .add(OntClass.ObjectAllValuesFrom.class, OWL2ObjectFactories.OBJECT_ALL_VALUES_FROM_CLASS)
            .add(OntClass.DataAllValuesFrom.class, OWL2ObjectFactories.DATA_ALL_VALUES_FROM_CLASS)
            .add(OntClass.ObjectHasValue.class, OWL2ObjectFactories.OBJECT_HAS_VALUE_CLASS)
            .add(OntClass.DataHasValue.class, OWL2ObjectFactories.DATA_HAS_VALUE_CLASS)
            .add(OntClass.ObjectMinCardinality.class, OWL2ObjectFactories.OBJECT_MIN_CARDINALITY_CLASS)
            .add(OntClass.DataMinCardinality.class, OWL2ObjectFactories.DATA_MIN_CARDINALITY_CLASS)
            .add(OntClass.ObjectMaxCardinality.class, OWL2ObjectFactories.OBJECT_MAX_CARDINALITY_CLASS)
            .add(OntClass.DataMaxCardinality.class, OWL2ObjectFactories.DATA_MAX_CARDINALITY_CLASS)
            .add(OntClass.ObjectCardinality.class, OWL2ObjectFactories.OBJECT_CARDINALITY_CLASS)
            .add(OntClass.DataCardinality.class, OWL2ObjectFactories.DATA_CARDINALITY_CLASS)
            .add(OntClass.HasSelf.class, OWL2ObjectFactories.HAS_SELF_CLASS)
            .add(OntClass.UnionOf.class, OWL2ObjectFactories.UNION_OF_CLASS)
            .add(OntClass.OneOf.class, OWL2ObjectFactories.ONE_OF_CLASS)
            .add(OntClass.IntersectionOf.class, OWL2ObjectFactories.INTERSECTION_OF_CLASS)
            .add(OntClass.ComplementOf.class, OWL2ObjectFactories.COMPLEMENT_OF_CLASS)
            .add(OntClass.NaryDataAllValuesFrom.class, OWL2ObjectFactories.NARY_DATA_ALL_VALUES_FROM_CLASS)
            .add(OntClass.NaryDataSomeValuesFrom.class, OWL2ObjectFactories.NARY_DATA_SOME_VALUES_FROM_CLASS)
            .add(OntClass.LogicalExpression.class, OWL2ObjectFactories.ANY_LOGICAL_CLASS)
            .add(OntClass.CollectionOf.class, OWL2ObjectFactories.ANY_COLLECTION_OF_CLASS)
            .add(OntClass.ValueRestriction.class, OWL2ObjectFactories.ANY_VALUE_RESTRICTION_CLASS)
            .add(OntClass.CardinalityRestriction.class, OWL2ObjectFactories.ANY_CARDINALITY_RESTRICTION_CLASS)
            .add(OntClass.ComponentRestriction.class, OWL2ObjectFactories.ANY_COMPONENT_RESTRICTION_CLASS)
            .add(OntClass.UnaryRestriction.class, OWL2ObjectFactories.ANY_UNARY_RESTRICTION_CLASS)
            .add(OntClass.Restriction.class, OWL2ObjectFactories.ANY_RESTRICTION_CLASS)
            .add(OntClass.class, OWL2ObjectFactories.ANY_CLASS)

            // data ranges:
            .add(OntDataRange.OneOf.class, OWL2ObjectFactories.ONE_OF_DATARANGE)
            .add(OntDataRange.Restriction.class, OWL2ObjectFactories.RESTRICTION_DATARANGE)
            .add(OntDataRange.ComplementOf.class, OWL2ObjectFactories.COMPLEMENT_OF_DATARANGE)
            .add(OntDataRange.UnionOf.class, OWL2ObjectFactories.UNION_OF_DATARANGE)
            .add(OntDataRange.IntersectionOf.class, OWL2ObjectFactories.INTERSECTION_OF_DATARANGE)
            .add(OntDataRange.Combination.class, OWL2ObjectFactories.ANY_COMPONENTS_DATARANGE)
            .add(OntDataRange.class, OWL2ObjectFactories.ANY_DATARANGE)

            // facet restrictions:
            .add(OntFacetRestriction.Length.class, OWL2ObjectFactories.LENGTH_FACET_RESTRICTION)
            .add(OntFacetRestriction.MinLength.class, OWL2ObjectFactories.MIN_LENGTH_FACET_RESTRICTION)
            .add(OntFacetRestriction.MaxLength.class, OWL2ObjectFactories.MAX_LENGTH_FACET_RESTRICTION)
            .add(OntFacetRestriction.MinInclusive.class, OWL2ObjectFactories.MIN_INCLUSIVE_FACET_RESTRICTION)
            .add(OntFacetRestriction.MaxInclusive.class, OWL2ObjectFactories.MAX_INCLUSIVE_FACET_RESTRICTION)
            .add(OntFacetRestriction.MinExclusive.class, OWL2ObjectFactories.MIN_EXCLUSIVE_FACET_RESTRICTION)
            .add(OntFacetRestriction.MaxExclusive.class, OWL2ObjectFactories.MAX_EXCLUSIVE_FACET_RESTRICTION)
            .add(OntFacetRestriction.Pattern.class, OWL2ObjectFactories.PATTERN_FACET_RESTRICTION)
            .add(OntFacetRestriction.TotalDigits.class, OWL2ObjectFactories.TOTAL_DIGITS_FACET_RESTRICTION)
            .add(OntFacetRestriction.FractionDigits.class, OWL2ObjectFactories.FRACTION_DIGITS_FACET_RESTRICTION)
            .add(OntFacetRestriction.LangRange.class, OWL2ObjectFactories.LANG_RANGE_FACET_RESTRICTION)
            .add(OntFacetRestriction.class, OWL2ObjectFactories.ANY_FACET_RESTRICTION)

            // negative property assertions:
            .add(OntNegativeAssertion.WithObjectProperty.class, OWL2ObjectFactories.OBJECT_NEGATIVE_PROPERTY_ASSERTION)
            .add(OntNegativeAssertion.WithDataProperty.class, OWL2ObjectFactories.DATA_NEGATIVE_PROPERTY_ASSERTION)
            .add(OntNegativeAssertion.class, OWL2ObjectFactories.ANY_NEGATIVE_PROPERTY_ASSERTION)

            // disjoint anonymous collections:
            .add(OntDisjoint.Classes.class, OWL2ObjectFactories.CLASSES_DISJOINT)
            .add(OntDisjoint.Individuals.class, OWL2ObjectFactories.DIFFERENT_INDIVIDUALS_DISJOINT)
            .add(OntDisjoint.ObjectProperties.class, OWL2ObjectFactories.OBJECT_PROPERTIES_DISJOINT)
            .add(OntDisjoint.DataProperties.class, OWL2ObjectFactories.DATA_PROPERTIES_DISJOINT)
            .add(OntDisjoint.Properties.class, OWL2ObjectFactories.ANY_PROPERTIES_DISJOINT)
            .add(OntDisjoint.class, OWL2ObjectFactories.ANY_DISJOINT)

            // SWRL objects:
            .add(SWRL_OBJECT_FACTORIES);

    /**
     * Default personality builder for OWL2 EL. Private access since this constant is mutable.
     *
     * @see <a href="https://www.w3.org/TR/owl2-profiles/#OWL_2_EL">OWL 2 EL</a>
     */
    private static final OntObjectPersonalityBuilder OWL2_EL_OBJECT_FACTORIES = templatePersonalityBuilder()
            .setName("OWL2-EL")
            .addPersonality(STANDARD_PERSONALITY)
            // the base ontology object:
            .add(OntObject.class, OWL2ObjectFactories.ANY_OBJECT)

            // ont-id:
            .add(OntID.class, OWL2ObjectFactories.ID)

            // annotation object:
            .add(OntAnnotation.class, OWL2ObjectFactories.ANNOTATION)

            // entities:
            .add(OntClass.Named.class, OWL2ObjectFactories.NAMED_CLASS)
            .add(OntDataRange.Named.class, OWL2ObjectFactories.NAMED_DATARANGE)
            .add(OntIndividual.Named.class, OWL2ObjectFactories.NAMED_INDIVIDUAL)
            .add(OntObjectProperty.Named.class, OWL2ObjectFactories.NAMED_OBJECT_PROPERTY)
            .add(OntDataProperty.class, OWL2ObjectFactories.DATATYPE_PROPERTY)
            .add(OntAnnotationProperty.class, OWL2ObjectFactories.ANNOTATION_PROPERTY)
            .add(OntEntity.class, OWL2ObjectFactories.ANY_ENTITY)

            // individuals:
            .add(OntIndividual.class, OWL2ObjectFactories.EL_ANY_INDIVIDUAL)

            // property expressions:
            .add(OntObjectProperty.class, OWL2ObjectFactories.EL_OBJECT_PROPERTY)
            .add(OntRelationalProperty.class, OWL2ObjectFactories.EL_ANY_DATA_OR_OBJECT_PROPERTY)
            .add(OntNamedProperty.class, OWL2ObjectFactories.ANY_NAMED_PROPERTY)
            .add(OntProperty.class, OWL2ObjectFactories.EL_ANY_PROPERTY)

            // class expressions:
            .add(OntClass.ObjectSomeValuesFrom.class, OWL2ObjectFactories.OBJECT_SOME_VALUES_FROM_CLASS)
            .add(OntClass.DataSomeValuesFrom.class, OWL2ObjectFactories.DATA_SOME_VALUES_FROM_CLASS)
            .add(OntClass.ObjectHasValue.class, OWL2ObjectFactories.OBJECT_HAS_VALUE_CLASS)
            .add(OntClass.DataHasValue.class, OWL2ObjectFactories.DATA_HAS_VALUE_CLASS)
            .add(OntClass.HasSelf.class, OWL2ObjectFactories.HAS_SELF_CLASS)
            .add(OntClass.OneOf.class, OWL2ObjectFactories.EL_ONE_OF_CLASS)
            .add(OntClass.IntersectionOf.class, OWL2ObjectFactories.INTERSECTION_OF_CLASS)
            .add(OntClass.LogicalExpression.class, OWL2ObjectFactories.EL_ANY_LOGICAL_CLASS)
            .add(OntClass.CollectionOf.class, OWL2ObjectFactories.EL_ANY_COLLECTION_OF_CLASS)
            .add(OntClass.ValueRestriction.class, OWL2ObjectFactories.EL_ANY_VALUE_RESTRICTION_CLASS)
            .add(OntClass.ComponentRestriction.class, OWL2ObjectFactories.EL_ANY_COMPONENT_RESTRICTION_CLASS)
            .add(OntClass.UnaryRestriction.class, OWL2ObjectFactories.EL_ANY_UNARY_RESTRICTION_CLASS)
            .add(OntClass.Restriction.class, OWL2ObjectFactories.EL_ANY_RESTRICTION_CLASS)
            .add(OntClass.class, OWL2ObjectFactories.EL_ANY_CLASS)

            // data ranges:
            .add(OntDataRange.OneOf.class, OWL2ObjectFactories.EL_ONE_OF_DATARANGE)
            .add(OntDataRange.IntersectionOf.class, OWL2ObjectFactories.INTERSECTION_OF_DATARANGE)
            .add(OntDataRange.Combination.class, OWL2ObjectFactories.EL_ANY_COMPONENTS_DATARANGE)
            .add(OntDataRange.class, OWL2ObjectFactories.EL_ANY_DATARANGE)

            // negative property assertions:
            .add(OntNegativeAssertion.WithObjectProperty.class, OWL2ObjectFactories.OBJECT_NEGATIVE_PROPERTY_ASSERTION)
            .add(OntNegativeAssertion.WithDataProperty.class, OWL2ObjectFactories.DATA_NEGATIVE_PROPERTY_ASSERTION)
            .add(OntNegativeAssertion.class, OWL2ObjectFactories.ANY_NEGATIVE_PROPERTY_ASSERTION)

            // disjoint anonymous collections:
            .add(OntDisjoint.Classes.class, OWL2ObjectFactories.EL_CLASSES_DISJOINT)
            .add(OntDisjoint.Individuals.class, OWL2ObjectFactories.EL_QL_RL_DIFFERENT_INDIVIDUALS_DISJOINT)
            .add(OntDisjoint.class, OWL2ObjectFactories.EL_ANY_DISJOINT);

    /**
     * Default personality builder for OWL2 QL. Private access since this constant is mutable.
     */
    private static final OntObjectPersonalityBuilder OWL2_QL_OBJECT_FACTORIES = templatePersonalityBuilder()
            .setName("OWL2-QL")
            .addPersonality(STANDARD_PERSONALITY)
            // the base ontology object:
            .add(OntObject.class, OWL2ObjectFactories.ANY_OBJECT)

            // ont-id:
            .add(OntID.class, OWL2ObjectFactories.ID)

            // annotation object:
            .add(OntAnnotation.class, OWL2ObjectFactories.ANNOTATION)

            // entities:
            .add(OntClass.Named.class, OWL2ObjectFactories.NAMED_CLASS)
            .add(OntDataRange.Named.class, OWL2ObjectFactories.NAMED_DATARANGE)
            .add(OntIndividual.Named.class, OWL2ObjectFactories.NAMED_INDIVIDUAL)
            .add(OntObjectProperty.Named.class, OWL2ObjectFactories.NAMED_OBJECT_PROPERTY)
            .add(OntDataProperty.class, OWL2ObjectFactories.DATATYPE_PROPERTY)
            .add(OntAnnotationProperty.class, OWL2ObjectFactories.ANNOTATION_PROPERTY)
            .add(OntEntity.class, OWL2ObjectFactories.ANY_ENTITY)

            // individuals:
            .add(OntIndividual.class, OWL2ObjectFactories.QL_ANY_INDIVIDUAL)

            // property expressions:
            .add(OntObjectProperty.Inverse.class, OWL2ObjectFactories.INVERSE_OBJECT_PROPERTY)
            .add(OntObjectProperty.class, OWL2ObjectFactories.OBJECT_PROPERTY)
            .add(OntRelationalProperty.class, OWL2ObjectFactories.ANY_DATA_OR_OBJECT_PROPERTY)
            .add(OntNamedProperty.class, OWL2ObjectFactories.ANY_NAMED_PROPERTY)
            .add(OntProperty.class, OWL2ObjectFactories.ANY_PROPERTY)

            // class expressions:
            .add(OntClass.ObjectSomeValuesFrom.class, OWL2ObjectFactories.QL_OBJECT_SOME_VALUES_FROM_CLASS)
            .add(OntClass.DataSomeValuesFrom.class, OWL2ObjectFactories.QL_DATA_SOME_VALUES_FROM_CLASS)
            .add(OntClass.IntersectionOf.class, OWL2ObjectFactories.QL_INTERSECTION_OF_CLASS)
            .add(OntClass.ComplementOf.class, OWL2ObjectFactories.QL_COMPLEMENT_OF_CLASS)
            .add(OntClass.NaryDataAllValuesFrom.class, OWL2ObjectFactories.NARY_DATA_ALL_VALUES_FROM_CLASS)
            .add(OntClass.NaryDataSomeValuesFrom.class, OWL2ObjectFactories.NARY_DATA_SOME_VALUES_FROM_CLASS)
            .add(OntClass.LogicalExpression.class, OWL2ObjectFactories.QL_ANY_LOGICAL_CLASS)
            .add(OntClass.CollectionOf.class, OWL2ObjectFactories.QL_ANY_COLLECTION_OF_CLASS)
            .add(OntClass.ValueRestriction.class, OWL2ObjectFactories.QL_ANY_VALUE_RESTRICTION_CLASS)
            .add(OntClass.ComponentRestriction.class, OWL2ObjectFactories.QL_ANY_COMPONENT_RESTRICTION_CLASS)
            .add(OntClass.UnaryRestriction.class, OWL2ObjectFactories.QL_ANY_UNARY_RESTRICTION_CLASS)
            .add(OntClass.Restriction.class, OWL2ObjectFactories.QL_ANY_RESTRICTION_CLASS)
            .add(OntClass.class, OWL2ObjectFactories.QL_ANY_CLASS)

            // data ranges:
            .add(OntDataRange.IntersectionOf.class, OWL2ObjectFactories.INTERSECTION_OF_DATARANGE)
            .add(OntDataRange.Combination.class, OWL2ObjectFactories.QL_ANY_COMPONENTS_DATARANGE)
            .add(OntDataRange.class, OWL2ObjectFactories.QL_ANY_DATARANGE)

            // disjoint anonymous collections:
            .add(OntDisjoint.Classes.class, OWL2ObjectFactories.QL_RL_CLASSES_DISJOINT)
            .add(OntDisjoint.Individuals.class, OWL2ObjectFactories.EL_QL_RL_DIFFERENT_INDIVIDUALS_DISJOINT)
            .add(OntDisjoint.ObjectProperties.class, OWL2ObjectFactories.QL_RL_OBJECT_PROPERTIES_DISJOINT)
            .add(OntDisjoint.DataProperties.class, OWL2ObjectFactories.QL_RL_DATA_PROPERTIES_DISJOINT)
            .add(OntDisjoint.Properties.class, OWL2ObjectFactories.ANY_PROPERTIES_DISJOINT)
            .add(OntDisjoint.class, OWL2ObjectFactories.ANY_DISJOINT);

    /**
     * Default personality builder for OWL2 RL. Private access since this constant is mutable.
     */
    private static final OntObjectPersonalityBuilder OWL2_RL_OBJECT_FACTORIES = templatePersonalityBuilder()
            .setName("OWL2-RL")
            .addPersonality(STANDARD_PERSONALITY)
            // the base ontology object:
            .add(OntObject.class, OWL2ObjectFactories.ANY_OBJECT)

            // ont-id:
            .add(OntID.class, OWL2ObjectFactories.ID)

            // annotation object:
            .add(OntAnnotation.class, OWL2ObjectFactories.ANNOTATION)

            // entities:
            .add(OntClass.Named.class, OWL2ObjectFactories.RL_NAMED_CLASS)
            .add(OntDataRange.Named.class, OWL2ObjectFactories.NAMED_DATARANGE)
            .add(OntIndividual.Named.class, OWL2ObjectFactories.NAMED_INDIVIDUAL)
            .add(OntObjectProperty.Named.class, OWL2ObjectFactories.NAMED_OBJECT_PROPERTY)
            .add(OntDataProperty.class, OWL2ObjectFactories.DATATYPE_PROPERTY)
            .add(OntAnnotationProperty.class, OWL2ObjectFactories.ANNOTATION_PROPERTY)
            .add(OntEntity.class, OWL2ObjectFactories.RL_ANY_ENTITY)

            // individuals:
            .add(OntIndividual.Anonymous.class, OWL2ObjectFactories.ANONYMOUS_INDIVIDUAL)
            .add(OntIndividual.class, OWL2ObjectFactories.ANY_INDIVIDUAL)

            // property expressions:
            .add(OntObjectProperty.Inverse.class, OWL2ObjectFactories.INVERSE_OBJECT_PROPERTY)
            .add(OntObjectProperty.class, OWL2ObjectFactories.OBJECT_PROPERTY)
            .add(OntRelationalProperty.class, OWL2ObjectFactories.ANY_DATA_OR_OBJECT_PROPERTY)
            .add(OntNamedProperty.class, OWL2ObjectFactories.ANY_NAMED_PROPERTY)
            .add(OntProperty.class, OWL2ObjectFactories.ANY_PROPERTY)

            // class expressions:
            .add(OntClass.ObjectSomeValuesFrom.class, OWL2ObjectFactories.RL_OBJECT_SOME_VALUES_FROM_CLASS)
            .add(OntClass.DataSomeValuesFrom.class, OWL2ObjectFactories.RL_DATA_SOME_VALUES_FROM_CLASS)
            .add(OntClass.ObjectAllValuesFrom.class, OWL2ObjectFactories.RL_OBJECT_ALL_VALUES_FROM_CLASS)
            .add(OntClass.DataAllValuesFrom.class, OWL2ObjectFactories.RL_DATA_ALL_VALUES_FROM_CLASS)
            .add(OntClass.ObjectHasValue.class, OWL2ObjectFactories.OBJECT_HAS_VALUE_CLASS)
            .add(OntClass.DataHasValue.class, OWL2ObjectFactories.DATA_HAS_VALUE_CLASS)
            .add(OntClass.ObjectMaxCardinality.class, OWL2ObjectFactories.RL_OBJECT_MAX_CARDINALITY_CLASS)
            .add(OntClass.DataMaxCardinality.class, OWL2ObjectFactories.RL_DATA_MAX_CARDINALITY_CLASS)
            .add(OntClass.UnionOf.class, OWL2ObjectFactories.RL_UNION_OF_CLASS)
            .add(OntClass.OneOf.class, OWL2ObjectFactories.RL_ONE_OF_CLASS)
            .add(OntClass.IntersectionOf.class, OWL2ObjectFactories.RL_INTERSECTION_OF_CLASS)
            .add(OntClass.ComplementOf.class, OWL2ObjectFactories.RL_COMPLEMENT_OF_CLASS)
            .add(OntClass.LogicalExpression.class, OWL2ObjectFactories.RL_ANY_LOGICAL_CLASS)
            .add(OntClass.CollectionOf.class, OWL2ObjectFactories.RL_ANY_COLLECTION_OF_CLASS)
            .add(OntClass.ValueRestriction.class, OWL2ObjectFactories.RL_ANY_VALUE_RESTRICTION_CLASS)
            .add(OntClass.CardinalityRestriction.class, OWL2ObjectFactories.RL_ANY_CARDINALITY_RESTRICTION_CLASS)
            .add(OntClass.ComponentRestriction.class, OWL2ObjectFactories.RL_ANY_COMPONENT_RESTRICTION_CLASS)
            .add(OntClass.UnaryRestriction.class, OWL2ObjectFactories.RL_ANY_UNARY_RESTRICTION_CLASS)
            .add(OntClass.Restriction.class, OWL2ObjectFactories.RL_ANY_RESTRICTION_CLASS)
            .add(OntClass.class, OWL2ObjectFactories.RL_ANY_CLASS)

            // data ranges:
            .add(OntDataRange.IntersectionOf.class, OWL2ObjectFactories.INTERSECTION_OF_DATARANGE)
            .add(OntDataRange.Combination.class, OWL2ObjectFactories.RL_ANY_COMPONENTS_DATARANGE)
            .add(OntDataRange.class, OWL2ObjectFactories.RL_ANY_DATARANGE)

            // negative property assertions:
            .add(OntNegativeAssertion.WithObjectProperty.class, OWL2ObjectFactories.OBJECT_NEGATIVE_PROPERTY_ASSERTION)
            .add(OntNegativeAssertion.WithDataProperty.class, OWL2ObjectFactories.DATA_NEGATIVE_PROPERTY_ASSERTION)
            .add(OntNegativeAssertion.class, OWL2ObjectFactories.ANY_NEGATIVE_PROPERTY_ASSERTION)

            // disjoint anonymous collections:
            .add(OntDisjoint.Classes.class, OWL2ObjectFactories.QL_RL_CLASSES_DISJOINT)
            .add(OntDisjoint.Individuals.class, OWL2ObjectFactories.EL_QL_RL_DIFFERENT_INDIVIDUALS_DISJOINT)
            .add(OntDisjoint.ObjectProperties.class, OWL2ObjectFactories.QL_RL_OBJECT_PROPERTIES_DISJOINT)
            .add(OntDisjoint.DataProperties.class, OWL2ObjectFactories.QL_RL_DATA_PROPERTIES_DISJOINT)
            .add(OntDisjoint.Properties.class, OWL2ObjectFactories.ANY_PROPERTIES_DISJOINT)
            .add(OntDisjoint.class, OWL2ObjectFactories.ANY_DISJOINT);
    /**
     * For OWL1.1 Ontologies, limited functionality.
     */
    private static final OntObjectPersonalityBuilder OWL1_OBJECT_FACTORIES = templatePersonalityBuilder()
            .setName("OWL1")
            .addPersonality(STANDARD_PERSONALITY)
            // the base ontology object:
            .add(OntObject.class, OWL1ObjectFactories.ANY_OBJECT)

            // ont-id:
            .add(OntID.class, OWL1ObjectFactories.ID)

            // entities:
            .add(OntClass.Named.class, OWL1ObjectFactories.NAMED_CLASS)
            .add(OntDataRange.Named.class, OWL1ObjectFactories.NAMED_DATARANGE)
            .add(OntIndividual.Named.class, OWL1ObjectFactories.NAMED_INDIVIDUAL)
            .add(OntObjectProperty.Named.class, OWL1ObjectFactories.NAMED_OBJECT_PROPERTY)
            .add(OntDataProperty.class, OWL1ObjectFactories.DATATYPE_PROPERTY)
            .add(OntAnnotationProperty.class, OWL1ObjectFactories.ANNOTATION_PROPERTY)
            .add(OntEntity.class, OWL1ObjectFactories.ANY_ENTITY)

            // individuals:
            .add(OntIndividual.Anonymous.class, OWL1ObjectFactories.ANONYMOUS_INDIVIDUAL)
            .add(OntIndividual.class, OWL1ObjectFactories.ANY_INDIVIDUAL)

            // properties:
            .add(OntObjectProperty.class, OWL1ObjectFactories.OBJECT_PROPERTY)
            .add(OntRelationalProperty.class, OWL1ObjectFactories.ANY_DATA_OR_OBJECT_PROPERTY)
            .add(OntNamedProperty.class, OWL1ObjectFactories.ANY_NAMED_PROPERTY)
            .add(OntProperty.class, OWL1ObjectFactories.ANY_PROPERTY)

            // class expressions:
            .add(OntClass.ObjectSomeValuesFrom.class, OWL1ObjectFactories.OBJECT_SOME_VALUES_FROM_CLASS)
            .add(OntClass.DataSomeValuesFrom.class, OWL1ObjectFactories.DATA_SOME_VALUES_FROM_CLASS)
            .add(OntClass.ObjectAllValuesFrom.class, OWL1ObjectFactories.OBJECT_ALL_VALUES_FROM_CLASS)
            .add(OntClass.DataAllValuesFrom.class, OWL1ObjectFactories.DATA_ALL_VALUES_FROM_CLASS)
            .add(OntClass.ObjectHasValue.class, OWL1ObjectFactories.OBJECT_HAS_VALUE_CLASS)
            .add(OntClass.DataHasValue.class, OWL1ObjectFactories.DATA_HAS_VALUE_CLASS)
            .add(OntClass.ObjectMinCardinality.class, OWL1ObjectFactories.OBJECT_MIN_CARDINALITY_CLASS)
            .add(OntClass.DataMinCardinality.class, OWL1ObjectFactories.DATA_MIN_CARDINALITY_CLASS)
            .add(OntClass.ObjectMaxCardinality.class, OWL1ObjectFactories.OBJECT_MAX_CARDINALITY_CLASS)
            .add(OntClass.DataMaxCardinality.class, OWL1ObjectFactories.DATA_MAX_CARDINALITY_CLASS)
            .add(OntClass.ObjectCardinality.class, OWL1ObjectFactories.OBJECT_CARDINALITY_CLASS)
            .add(OntClass.DataCardinality.class, OWL1ObjectFactories.DATA_CARDINALITY_CLASS)
            .add(OntClass.UnionOf.class, OWL1ObjectFactories.UNION_OF_CLASS)
            .add(OntClass.OneOf.class, OWL1ObjectFactories.ONE_OF_CLASS)
            .add(OntClass.IntersectionOf.class, OWL1ObjectFactories.INTERSECTION_OF_CLASS)
            .add(OntClass.ComplementOf.class, OWL1ObjectFactories.COMPLEMENT_OF_CLASS)
            .add(OntClass.LogicalExpression.class, OWL1ObjectFactories.ANY_LOGICAL_CLASS_FULL)
            .add(OntClass.CollectionOf.class, OWL1ObjectFactories.ANY_COLLECTION_OF_CLASS_FULL)
            .add(OntClass.ValueRestriction.class, OWL1ObjectFactories.ANY_VALUE_RESTRICTION_CLASS_FULL)
            .add(OntClass.CardinalityRestriction.class, OWL1ObjectFactories.ANY_CARDINALITY_RESTRICTION_CLASS)
            .add(OntClass.ComponentRestriction.class, OWL1ObjectFactories.ANY_COMPONENT_RESTRICTION_CLASS)
            .add(OntClass.UnaryRestriction.class, OWL1ObjectFactories.ANY_UNARY_RESTRICTION_CLASS)
            .add(OntClass.Restriction.class, OWL1ObjectFactories.ANY_RESTRICTION_CLASS_FULL)
            .add(OntClass.class, OWL1ObjectFactories.ANY_CLASS_FULL)

            // data ranges:
            .add(OntDataRange.OneOf.class, OWL1ObjectFactories.ONE_OF_DATARANGE)
            .add(OntDataRange.Combination.class, OWL1ObjectFactories.ANY_COMPONENTS_DATARANGE)
            .add(OntDataRange.class, OWL1ObjectFactories.ANY_DATARANGE)

            .add(OntDisjoint.Individuals.class, OWL1ObjectFactories.DIFFERENT_INDIVIDUALS_DISJOINT)
            .add(OntDisjoint.class, OWL1ObjectFactories.ANY_DISJOINT);

    /**
     * For OWL1.1 Lite Ontologies, limited functionality.
     *
     * @see <a href="https://www.w3.org/TR/2004/REC-owl-features-20040210/#s3">Language Description of OWL Lite</a>
     */
    private static final OntObjectPersonalityBuilder OWL1_LITE_OBJECT_FACTORIES = templatePersonalityBuilder()
            .setName("OWL1-LITE")
            .addPersonality(STANDARD_PERSONALITY)
            // the base ontology object:
            .add(OntObject.class, OWL1ObjectFactories.ANY_OBJECT)

            // ont-id:
            .add(OntID.class, OWL1ObjectFactories.ID)

            // entities:
            .add(OntClass.Named.class, OWL1ObjectFactories.NAMED_CLASS)
            .add(OntDataRange.Named.class, OWL1ObjectFactories.NAMED_DATARANGE)
            .add(OntIndividual.Named.class, OWL1ObjectFactories.NAMED_INDIVIDUAL)
            .add(OntObjectProperty.Named.class, OWL1ObjectFactories.NAMED_OBJECT_PROPERTY)
            .add(OntDataProperty.class, OWL1ObjectFactories.DATATYPE_PROPERTY)
            .add(OntAnnotationProperty.class, OWL1ObjectFactories.ANNOTATION_PROPERTY)
            .add(OntEntity.class, OWL1ObjectFactories.ANY_ENTITY)

            // individuals:
            .add(OntIndividual.Anonymous.class, OWL1ObjectFactories.ANONYMOUS_INDIVIDUAL)
            .add(OntIndividual.class, OWL1ObjectFactories.ANY_INDIVIDUAL)

            // properties:
            .add(OntObjectProperty.class, OWL1ObjectFactories.OBJECT_PROPERTY)
            .add(OntRelationalProperty.class, OWL1ObjectFactories.ANY_DATA_OR_OBJECT_PROPERTY)
            .add(OntNamedProperty.class, OWL1ObjectFactories.ANY_NAMED_PROPERTY)
            .add(OntProperty.class, OWL1ObjectFactories.ANY_PROPERTY)

            // class expressions:
            .add(OntClass.ObjectSomeValuesFrom.class, OWL1ObjectFactories.OBJECT_SOME_VALUES_FROM_CLASS)
            .add(OntClass.DataSomeValuesFrom.class, OWL1ObjectFactories.DATA_SOME_VALUES_FROM_CLASS)
            .add(OntClass.ObjectAllValuesFrom.class, OWL1ObjectFactories.OBJECT_ALL_VALUES_FROM_CLASS)
            .add(OntClass.DataAllValuesFrom.class, OWL1ObjectFactories.DATA_ALL_VALUES_FROM_CLASS)
            .add(OntClass.ObjectMinCardinality.class, OWL1ObjectFactories.OBJECT_MIN_CARDINALITY_CLASS)
            .add(OntClass.DataMinCardinality.class, OWL1ObjectFactories.DATA_MIN_CARDINALITY_CLASS)
            .add(OntClass.ObjectMaxCardinality.class, OWL1ObjectFactories.OBJECT_MAX_CARDINALITY_CLASS)
            .add(OntClass.DataMaxCardinality.class, OWL1ObjectFactories.DATA_MAX_CARDINALITY_CLASS)
            .add(OntClass.ObjectCardinality.class, OWL1ObjectFactories.OBJECT_CARDINALITY_CLASS)
            .add(OntClass.DataCardinality.class, OWL1ObjectFactories.DATA_CARDINALITY_CLASS)
            .add(OntClass.IntersectionOf.class, OWL1ObjectFactories.INTERSECTION_OF_CLASS)
            .add(OntClass.LogicalExpression.class, OWL1ObjectFactories.ANY_LOGICAL_CLASS_LITE)
            .add(OntClass.CollectionOf.class, OWL1ObjectFactories.ANY_COLLECTION_OF_CLASS_LITE)
            .add(OntClass.ValueRestriction.class, OWL1ObjectFactories.ANY_VALUE_RESTRICTION_CLASS_LITE)
            .add(OntClass.CardinalityRestriction.class, OWL1ObjectFactories.ANY_CARDINALITY_RESTRICTION_CLASS)
            .add(OntClass.ComponentRestriction.class, OWL1ObjectFactories.ANY_COMPONENT_RESTRICTION_CLASS)
            .add(OntClass.UnaryRestriction.class, OWL1ObjectFactories.ANY_UNARY_RESTRICTION_CLASS)
            .add(OntClass.Restriction.class, OWL1ObjectFactories.ANY_RESTRICTION_CLASS_LITE)
            .add(OntClass.class, OWL1ObjectFactories.ANY_CLASS_LITE)

            .add(OntDisjoint.Individuals.class, OWL1ObjectFactories.DIFFERENT_INDIVIDUALS_DISJOINT)
            .add(OntDisjoint.class, OWL1ObjectFactories.ANY_DISJOINT);

    /**
     * Creates (mutable) {@link OntObjectPersonalityBuilder} for RDFS Ontologies.
     *
     * @return {@link OntObjectPersonalityBuilder}
     */
    public static OntObjectPersonalityBuilder RDFS_ONT_PERSONALITY() {
        return RDFS_OBJECT_FACTORIES.copy()
                .setBuiltins(OntPersonalities.RDFS_BUILTINS)
                .setReserved(OntPersonalities.RDFS_RESERVED)
                .setPunnings(OntPersonalities.RDFS_NO_PUNNINGS)
                .setConfig(OntConfigs.RDFS_CONFIG);
    }

    /**
     * Creates (mutable) {@link OntObjectPersonalityBuilder} for OWL2 Ontologies.
     *
     * @return {@link OntObjectPersonalityBuilder}
     */
    public static OntObjectPersonalityBuilder OWL2_ONT_PERSONALITY() {
        return OWL2_OBJECT_FACTORIES
                .copy()
                .setBuiltins(OntPersonalities.OWL2_FULL_BUILTINS)
                .setReserved(OntPersonalities.OWL2_RESERVED)
                .setPunnings(OntPersonalities.OWL_NO_PUNNINGS)
                .setConfig(OntConfigs.OWL2_CONFIG);
    }

    /**
     * Creates (mutable) {@link OntObjectPersonalityBuilder} for OWL2 EL Ontologies.
     *
     * @return {@link OntObjectPersonalityBuilder}
     */
    public static OntObjectPersonalityBuilder OWL2_EL_ONT_PERSONALITY() {
        return OWL2_EL_OBJECT_FACTORIES
                .copy()
                .setBuiltins(OntPersonalities.OWL2_EL_BUILTINS)
                .setReserved(OntPersonalities.OWL2_RESERVED)
                .setPunnings(OntPersonalities.OWL_DL2_PUNNINGS)
                .setConfig(OntConfigs.OWL2_EL_CONFIG);
    }

    /**
     * Creates (mutable) {@link OntObjectPersonalityBuilder} for OWL2 QL Ontologies.
     *
     * @return {@link OntObjectPersonalityBuilder}
     */
    public static OntObjectPersonalityBuilder OWL2_QL_ONT_PERSONALITY() {
        return OWL2_QL_OBJECT_FACTORIES
                .copy()
                .setBuiltins(OntPersonalities.OWL2_QL_BUILTINS)
                .setReserved(OntPersonalities.OWL2_RESERVED)
                .setPunnings(OntPersonalities.OWL_DL2_PUNNINGS)
                .setConfig(OntConfigs.OWL2_QL_CONFIG);
    }

    /**
     * Creates (mutable) {@link OntObjectPersonalityBuilder} for OWL2 RL Ontologies.
     *
     * @return {@link OntObjectPersonalityBuilder}
     */
    public static OntObjectPersonalityBuilder OWL2_RL_ONT_PERSONALITY() {
        return OWL2_RL_OBJECT_FACTORIES
                .copy()
                .setBuiltins(OntPersonalities.OWL2_RL_BUILTINS)
                .setReserved(OntPersonalities.OWL2_RESERVED)
                .setPunnings(OntPersonalities.OWL_DL2_PUNNINGS)
                .setConfig(OntConfigs.OWL2_RL_CONFIG);
    }

    /**
     * Creates (mutable) {@link OntObjectPersonalityBuilder} for OWL1 Ontologies.
     *
     * @return {@link OntObjectPersonalityBuilder}
     */
    public static OntObjectPersonalityBuilder OWL1_ONT_PERSONALITY() {
        return OWL1_OBJECT_FACTORIES.copy()
                .setBuiltins(OntPersonalities.OWL2_FULL_BUILTINS)
                .setReserved(OntPersonalities.OWL2_RESERVED)
                .setPunnings(OntPersonalities.OWL_NO_PUNNINGS)
                .setConfig(OntConfigs.OWL1_CONFIG);
    }

    /**
     * Creates (mutable) {@link OntObjectPersonalityBuilder} for OWL1 LITE Ontologies.
     *
     * @return {@link OntObjectPersonalityBuilder}
     */
    public static OntObjectPersonalityBuilder OWL1_LITE_ONT_PERSONALITY() {
        return OWL1_LITE_OBJECT_FACTORIES.copy()
                .setBuiltins(OntPersonalities.OWL2_FULL_BUILTINS)
                .setReserved(OntPersonalities.OWL2_RESERVED)
                .setPunnings(OntPersonalities.OWL_DL1_PUNNINGS)
                .setConfig(OntConfigs.OWL1_CONFIG);
    }

    private static OntObjectPersonalityBuilder templatePersonalityBuilder() {
        return new OntObjectPersonalityBuilder() {

            @Override
            public OntPersonality build() throws IllegalStateException {
                throw new IllegalStateException("Should not be call");
            }
        };
    }

    /**
     * @param personality {@link OntPersonality}
     * @return {@code true} if the specified {@link OntPersonality} has name "RDFS"
     */
    public static boolean isRDFS(OntPersonality personality) {
        return personality.getName() != null && personality.getName().startsWith("RDFS");
    }

    /**
     * @param personality {@link OntPersonality}
     * @return {@code true} if the specified {@link OntPersonality} supports {@code owl:Thing}
     */
    public static boolean supportsOWLThing(OntPersonality personality) {
        return personality.getBuiltins().getNamedClasses().contains(OWL2.Thing.asNode());
    }

    /**
     * Creates a {@link OntPersonality.Builtins builtins personality vocabulary}
     * from the given {@link OntVocabulary system vocabulary}.
     *
     * @param voc {@link OntVocabulary}, not {@code null}
     * @return {@link OntPersonality.Builtins}
     */
    public static OntPersonality.Builtins createBuiltinsVocabulary(OntVocabulary voc) {
        Objects.requireNonNull(voc);
        Map<Class<? extends OntObject>, Set<Node>> res = new HashMap<>();
        res.put(OntAnnotationProperty.class, StdModels.asUnmodifiableNodeSet(voc.getBuiltinAnnotationProperties()));
        res.put(OntDataProperty.class, StdModels.asUnmodifiableNodeSet(voc.getBuiltinDatatypeProperties()));
        res.put(OntObjectProperty.Named.class, StdModels.asUnmodifiableNodeSet(voc.getBuiltinObjectProperties()));
        res.put(OntDataRange.Named.class, StdModels.asUnmodifiableNodeSet(voc.getBuiltinDatatypes()));
        res.put(OntClass.Named.class, StdModels.asUnmodifiableNodeSet(voc.getBuiltinClasses()));
        res.put(OntSWRL.Builtin.class, StdModels.asUnmodifiableNodeSet(voc.getBuiltinSWRLs()));
        res.put(OntIndividual.Named.class, Collections.emptySet());
        Set<Property> ontProperties = new HashSet<>();
        ontProperties.addAll(voc.getBuiltinAnnotationProperties());
        ontProperties.addAll(voc.getBuiltinDatatypeProperties());
        ontProperties.addAll(voc.getBuiltinObjectProperties());
        Set<Resource> ontEntities = new HashSet<>();
        ontEntities.addAll(ontProperties);
        ontEntities.addAll(voc.getBuiltinClasses());
        ontEntities.addAll(voc.getBuiltinDatatypes());
        res.put(OntProperty.class, StdModels.asUnmodifiableNodeSet(ontProperties));
        res.put(OntEntity.class, StdModels.asUnmodifiableNodeSet(ontEntities));
        return new ResourceVocabularyImpl.BuiltinsImpl(res);
    }

    /**
     * Creates a {@link OntPersonality.Reserved reserved personality vocabulary}
     * from the given {@link OntVocabulary system vocabulary}.
     *
     * @param voc {@link OntVocabulary}, not {@code null}
     * @return {@link OntPersonality.Reserved}
     */
    public static OntPersonality.Reserved createReservedVocabulary(OntVocabulary voc) {
        Objects.requireNonNull(voc);
        Map<Class<? extends Resource>, Set<Node>> res = new HashMap<>();
        res.put(Resource.class, StdModels.asUnmodifiableNodeSet(voc.getSystemResources()));
        res.put(Property.class, StdModels.asUnmodifiableNodeSet(voc.getSystemProperties()));
        return new ResourceVocabularyImpl.ReservedIml(Map.copyOf(res));
    }

    /**
     * Creates a {@link OntPersonality.Punnings punnings personality vocabulary} according to {@link PunningsMode}.
     *
     * @param mode {@link PunningsMode}, not {@code null}
     * @return {@link OntPersonality.Punnings}
     */
    private static OntPersonality.Punnings createPunningsVocabulary(PunningsMode mode) {
        return new ResourceVocabularyImpl.PunningsImpl(PunningsMode.toMap(mode));
    }

}
