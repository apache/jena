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

package org.apache.jena.ontapi.model;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.vocabulary.OWL2;

import java.util.Arrays;
import java.util.Collection;

/**
 * A technical interface to generate {@link OntClass Class Expression}s.
 */
interface CreateClasses {

    /**
     * Creates an Existential Quantification Object Property Restriction.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty P .
     * _:x owl:someValuesFrom C .
     * }</pre>
     *
     * @param property {@link OntObjectProperty object property expression}, not {@code null}
     * @param ce       {@link OntClass class expression}, not {@code null}
     * @return {@link OntClass.ObjectSomeValuesFrom}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Existential_Quantification">8.2.1 Existential Quantification</a>
     */
    OntClass.ObjectSomeValuesFrom createObjectSomeValuesFrom(OntObjectProperty property, OntClass ce);

    /**
     * Creates an Existential Quantification Data Property Restriction.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty R .
     * _:x owl:someValuesFrom D .
     * }</pre>
     *
     * @param property {@link OntDataProperty data property}, not {@code null}
     * @param dr       {@link OntDataRange data range}, not {@code null}
     * @return {@link OntClass.DataSomeValuesFrom}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Existential_Quantification_2">8.4.1 Existential Quantification</a>
     */
    OntClass.DataSomeValuesFrom createDataSomeValuesFrom(OntDataProperty property, OntDataRange dr);

    /**
     * Creates a Universal Quantification Object Property Restriction.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty P .
     * _:x owl:allValuesFrom C .
     * }</pre>
     *
     * @param property {@link OntObjectProperty object property expression}, not {@code null}
     * @param ce       {@link OntClass class expression}, not {@code null}
     * @return {@link OntClass.ObjectAllValuesFrom}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Universal_Quantification">8.2.2 Universal Quantification</a>
     */
    OntClass.ObjectAllValuesFrom createObjectAllValuesFrom(OntObjectProperty property, OntClass ce);

    /**
     * Creates a Universal Quantification Data Property Restriction.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty R .
     * _:x owl:allValuesFrom D .
     * }</pre>
     *
     * @param property {@link OntDataProperty data property}, not {@code null}
     * @param dr       {@link OntDataRange data range}, not {@code null}
     * @return {@link OntClass.DataAllValuesFrom}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Universal_Quantification_2">8.4.2 Universal Quantification</a>
     */
    OntClass.DataAllValuesFrom createDataAllValuesFrom(OntDataProperty property, OntDataRange dr);

    /**
     * Creates an Individual Value Restriction.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty P .
     * _:x owl:hasValue a .
     * }</pre>
     *
     * @param property   {@link OntObjectProperty object property expression}, not {@code null}
     * @param individual {@link OntIndividual}, not {@code null}
     * @return {@link OntClass.ObjectHasValue}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Individual_Value_Restriction">8.2.3 Individual Value Restriction</a>
     */
    OntClass.ObjectHasValue createObjectHasValue(OntObjectProperty property, OntIndividual individual);

    /**
     * Creates a Literal Value Restriction.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty R .
     * _:x owl:hasValue v .
     * }</pre>
     *
     * @param property {@link OntDataProperty data property}, not {@code null}
     * @param literal  {@link Literal}, not {@code null}
     * @return {@link OntClass.DataHasValue}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Literal_Value_Restriction">8.4.3 Literal Value Restriction</a>
     */
    OntClass.DataHasValue createDataHasValue(OntDataProperty property, Literal literal);

    /**
     * Creates an Object Minimum Cardinality Restriction, possible Qualified.
     * If {@code ce} is {@code null}, it is taken to be {@link OWL2#Thing owl:Thing}.
     * In that case the return restriction is unqualified.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty P .
     * _:x owl:minCardinality n .
     * }</pre> or
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty P .
     * _:x owl:minQualifiedCardinality n .
     * _:x owl:onClass C .
     * }</pre>
     *
     * @param property    {@link OntObjectProperty object property expression}, not {@code null}
     * @param cardinality {@code int}, non-negative number
     * @param ce          {@link OntClass class expression} or {@code null}
     * @return {@link OntClass.ObjectMinCardinality}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Minimum_Cardinality">8.3.1 Minimum Cardinality</a>
     */
    OntClass.ObjectMinCardinality createObjectMinCardinality(OntObjectProperty property, int cardinality, OntClass ce);

    /**
     * Creates a Data Minimum Cardinality Restriction, possible Qualified.
     * If {@code dr} is {@code null}, it is taken to be {@link org.apache.jena.vocabulary.RDFS#Literal rdfs:Literal}.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty R .
     * _:x owl:minCardinality n .
     * }</pre> or
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty R .
     * _:x owl:minQualifiedCardinality n .
     * _:x owl:onDataRange D .
     * }</pre>
     *
     * @param property    {@link OntDataProperty data property}, not {@code null}
     * @param cardinality {@code int}, non-negative number
     * @param dr          {@link OntDataRange data range} or {@code null}
     * @return {@link OntClass.DataMinCardinality}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Minimum_Cardinality_2">8.5.1 Minimum Cardinality</a>
     */
    OntClass.DataMinCardinality createDataMinCardinality(OntDataProperty property, int cardinality, OntDataRange dr);

    /**
     * Creates an Object Maximum Cardinality Restriction, possible Qualified.
     * If {@code ce} is {@code null}, it is taken to be {@link OWL2#Thing owl:Thing}.
     * In that case the return restriction is unqualified.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty P .
     * _:x owl:maxCardinality n .
     * }</pre> or
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty P .
     * _:x owl:maxQualifiedCardinality n .
     * _:x owl:onClass C .
     * }</pre>
     *
     * @param property     {@link OntObjectProperty object property expression}, not {@code null}
     * @param cardinality  {@code int}, non-negative number
     * @param ce           {@link OntClass class expression} or {@code null}
     * @return {@link OntClass.ObjectMaxCardinality}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Maximum_Cardinality">8.3.2 Maximum Cardinality</a>
     */
    OntClass.ObjectMaxCardinality createObjectMaxCardinality(OntObjectProperty property, int cardinality, OntClass ce);

    /**
     * Creates a Data Maximum Cardinality Restriction, possible Qualified.
     * If {@code dr} is {@code null}, it is taken to be {@link org.apache.jena.vocabulary.RDFS#Literal rdfs:Literal}.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty R .
     * _:x owl:maxCardinality n .
     * }</pre> or
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty R .
     * _:x owl:maxQualifiedCardinality n .
     * _:x owl:onDataRange D .
     * }</pre>
     *
     * @param property    {@link OntDataProperty data property}, not {@code null}
     * @param cardinality {@code int}, non-negative number
     * @param dr          {@link OntDataRange data range} or {@code null}
     * @return {@link OntClass.DataMaxCardinality}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Maximum_Cardinality_2">8.5.2 Maximum Cardinality</a>
     */
    OntClass.DataMaxCardinality createDataMaxCardinality(OntDataProperty property, int cardinality, OntDataRange dr);

    /**
     * Creates an Object Exact Cardinality Restriction, possible Qualified.
     * If {@code ce} is {@code null}, it is taken to be {@link OWL2#Thing owl:Thing}.
     * In that case the return restriction is unqualified.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty P .
     * _:x owl:cardinality n .
     * }</pre> or
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty P .
     * _:x owl:qualifiedCardinality n .
     * _:x owl:onClass C .
     * }</pre>
     *
     * @param property     {@link OntObjectProperty object property expression}, not {@code null}
     * @param cardinality  {@code int}, non-negative number
     * @param ce           {@link OntClass class expression} or {@code null}
     * @return {@link OntClass.ObjectCardinality}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Exact_Cardinality">8.3.3 Exact Cardinality</a>
     */
    OntClass.ObjectCardinality createObjectCardinality(OntObjectProperty property, int cardinality, OntClass ce);

    /**
     * Creates a Data Exact Cardinality Restriction, possible Qualified.
     * If {@code dr} is {@code null}, it is taken to be {@link org.apache.jena.vocabulary.RDFS#Literal rdfs:Literal}.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty R .
     * _:x owl:cardinality n .
     * }</pre> or
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty R .
     * _:x owl:qualifiedCardinality n .
     * _:x owl:onDataRange D .
     * }</pre>
     *
     * @param property    {@link OntDataProperty data property}, not {@code null}
     * @param cardinality {@code int}, non-negative number
     * @param dr          {@link OntDataRange data range} or {@code null}
     * @return {@link OntClass.DataCardinality}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Exact_Cardinality_2">8.5.3 Exact Cardinality</a>
     */
    OntClass.DataCardinality createDataCardinality(OntDataProperty property, int cardinality, OntDataRange dr);

    /**
     * Creates a Local Reflexivity Class Expression (Self-Restriction).
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperty P .
     * _:x owl:hasSelf "true"^^xsd:boolean .
     * }</pre>
     *
     * @param property {@link OntObjectProperty object property expression}, not {@code null}
     * @return {@link OntClass.HasSelf}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Self-Restriction">8.2.4 Self-Restriction</a>
     */
    OntClass.HasSelf createHasSelf(OntObjectProperty property);

    /**
     * Creates a Union of Class Expressions.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Class .
     * _:x owl:unionOf ( C1 ... Cn ) .
     * }</pre>
     *
     * @param classes {@code Collection} of {@link OntClass class expression}s without {@code null}s
     * @return {@link OntClass.UnionOf}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Union_of_Class_Expressions">8.1.2 Union of Class Expressions</a>
     */
    OntClass.UnionOf createObjectUnionOf(Collection<OntClass> classes);

    /**
     * Creates an Intersection of Class Expressions.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Class .
     * _:x owl:intersectionOf ( C1 ... Cn ) .
     * }</pre>
     *
     * @param classes {@code Collection} of {@link OntClass class expression}s without {@code null}s
     * @return {@link OntClass.IntersectionOf}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Intersection_of_Class_Expressions">8.1.1 Intersection of Class Expressions</a>
     */
    OntClass.IntersectionOf createObjectIntersectionOf(Collection<OntClass> classes);

    /**
     * Creates an Enumeration of Individuals.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Class .
     * _:x owl:oneOf ( a1 ... an ).
     * }</pre>
     *
     * @param individuals {@code Collection} of {@link OntIndividual individual}s without {@code null}s
     * @return {@link OntClass.OneOf}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Enumeration_of_Individuals">8.1.4 Enumeration of Individuals</a>
     */
    OntClass.OneOf createObjectOneOf(Collection<OntIndividual> individuals);

    /**
     * Create a Complement of Class Expressions.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Class .
     * _:x owl:complementOf C .
     * }</pre>
     *
     * @param ce {@link OntClass class expression} or {@code null}
     * @return {@link OntClass.ComplementOf}
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Complement_of_Class_Expressions">8.1.3 Complement of Class Expressions</a>
     */
    OntClass.ComplementOf createObjectComplementOf(OntClass ce);

    /**
     * Creates an N-Ary Data Universal Quantification N-Ary Restriction.
     * Note: currently a Unary Restriction is preferable since in OWL2 data-range arity is always {@code 1}.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperties ( R1 ... Rn ) .
     * _:x owl:allValuesFrom Dn .
     * }</pre>
     *
     * @param properties {@code Collection} of {@link OntDataRange data range}s without {@code null}s
     * @param dr         {@link OntDataRange data range}, not {@code null}
     * @return {@link OntClass.NaryDataAllValuesFrom}
     * @see OntDataRange#arity()
     * @see #createDataAllValuesFrom(OntDataProperty, OntDataRange)
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Universal_Quantification_2">8.4.2 Universal Quantification</a>
     */
    OntClass.NaryDataAllValuesFrom createDataAllValuesFrom(Collection<OntDataProperty> properties, OntDataRange dr);

    /**
     * Creates an N-Ary Data Existential Quantification N-Ary Restriction.
     * Note: currently a Unary Restriction is preferable since in OWL2 data-range arity is always {@code 1}.
     * The RDF structure:
     * <pre>{@code
     * _:x rdf:type owl:Restriction .
     * _:x owl:onProperties ( R1 ... Rn ) .
     * _:x owl:someValuesFrom Dn .
     * }</pre>
     *
     * @param properties {@code Collection} of {@link OntDataRange data range}s without {@code null}s
     * @param dr         {@link OntDataRange data range}, not {@code null}
     * @return {@link OntClass.NaryDataAllValuesFrom}
     * @see OntDataRange#arity()
     * @see #createDataSomeValuesFrom(OntDataProperty, OntDataRange)
     * @see <a href="https://www.w3.org/TR/owl-syntax/#Existential_Quantification_2">8.4.1 Existential Quantification</a>
     */
    OntClass.NaryDataSomeValuesFrom createDataSomeValuesFrom(Collection<OntDataProperty> properties, OntDataRange dr);

    /**
     * Creates an Intersection of Class Expressions.
     *
     * @param classes Array of {@link OntClass class expression}s without {@code null}s
     * @return {@link OntClass.IntersectionOf}
     * @see #createObjectIntersectionOf(Collection)
     */
    default OntClass.IntersectionOf createObjectIntersectionOf(OntClass... classes) {
        return createObjectIntersectionOf(Arrays.asList(classes));
    }

    /**
     * Creates a Union of Class Expressions.
     *
     * @param classes Array of {@link OntClass class expression}s without {@code null}s
     * @return {@link OntClass.UnionOf}
     * @see #createObjectUnionOf(Collection)
     */
    default OntClass.UnionOf createObjectUnionOf(OntClass... classes) {
        return createObjectUnionOf(Arrays.asList(classes));
    }

    /**
     * Creates an Enumeration of Individuals.
     *
     * @param individuals Array of {@link OntIndividual individual}s without {@code null}s
     * @return {@link OntClass.OneOf}
     * @see #createObjectOneOf(Collection)
     */
    default OntClass.OneOf createObjectOneOf(OntIndividual... individuals) {
        return createObjectOneOf(Arrays.asList(individuals));
    }
}
