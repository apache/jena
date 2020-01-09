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

package org.apache.jena.shacl.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

// Vocabulary for http://www.w3.org/ns/shacl#
// schemagen

public class SHACLM {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static final Model M_MODEL = ModelFactory.createDefaultModel();

    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.w3.org/ns/shacl#";

    /** <p>The namespace of the vocabulary as a string</p>
     * @return namespace as String
     * @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = M_MODEL.createResource( NS );

    /** <p>The (single) value of this property must be a list of path elements, representing
     *  the elements of alternative paths.</p>
     */
    public static final Property alternativePath = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#alternativePath" );

    /** <p>RDF list of shapes to validate the value nodes against.</p> */
    public static final Property and = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#and" );

    /** <p>The annotation property that shall be set.</p> */
    public static final Property annotationProperty = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#annotationProperty" );

    /** <p>The (default) values of the annotation property.</p> */
    public static final Property annotationValue = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#annotationValue" );

    /** <p>The name of the SPARQL variable from the SELECT clause that shall be used
     *  for the values.</p>
     */
    public static final Property annotationVarName = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#annotationVarName" );

    /** <p>The SPARQL ASK query to execute.</p> */
    public static final Property ask = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#ask" );

    /** <p>The type that all value nodes must have.</p> */
    public static final Property class_ = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#class" );

    /** <p>If set to true then the shape is closed.</p> */
    public static final Property closed = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#closed" );

    /** <p>The shapes that the focus nodes need to conform to before a rule is executed
     *  on them.</p>
     */
    public static final Property condition = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#condition" );

    /** <p>True if the validation did not produce any validation results, and false otherwise.</p> */
    public static final Property conforms = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#conforms" );

    /** <p>The SPARQL CONSTRUCT query to execute.</p> */
    public static final Property construct = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#construct" );

    /** <p>Specifies an RDF datatype that all value nodes must have.</p> */
    public static final Property datatype = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#datatype" );

    /** <p>If set to true then all nodes conform to this.</p> */
    public static final Property deactivated = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#deactivated" );

    /** <p>Links a resource with its namespace prefix declarations.</p> */
    public static final Property declare = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#declare" );

    /** <p>A default value for a property, for example for user interface tools to pre-populate
     *  input fields.</p>
     */
    public static final Property defaultValue = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#defaultValue" );

    /** <p>Human-readable descriptions for the property in the context of the surrounding
     *  shape.</p>
     */
    public static final Property description = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#description" );

    /** <p>Links a result with other results that provide more details, for example to
     *  describe violations against nested shapes.</p>
     */
    public static final Property detail = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#detail" );

    /** <p>Specifies a property where the set of values must be disjoint with the value
     *  nodes.</p>
     */
    public static final Property disjoint = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#disjoint" );

    /** <p>An entailment regime that indicates what kind of inferencing is required by
     *  a shapes graph.</p>
     */
    public static final Property entailment = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#entailment" );

    /** <p>Specifies a property that must have the same values as the value nodes.</p> */
    public static final Property equals = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#equals" );

    /** <p>The node expression that must return true for the value nodes.</p> */
    public static final Property expression = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#expression" );

    /** <p>The shape that all input nodes of the expression need to conform to.</p> */
    public static final Property filterShape = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#filterShape" );

    /** <p>An optional flag to be used with regular expression pattern matching.</p> */
    public static final Property flags = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#flags" );

    /** <p>The focus node that was validated when the result was produced.</p> */
    public static final Property focusNode = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#focusNode" );

    /** <p>Can be used to link to a property group to indicate that a property shape
     *  belongs to a group of related property shapes.</p>
     */
    public static final Property group = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#group" );

    /** <p>Specifies a value that must be among the value nodes.</p> */
    public static final Property hasValue = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#hasValue" );

    /** <p>An optional RDF list of properties that are also permitted in addition to
     *  those explicitly enumerated via sh:property/sh:path.</p>
     */
    public static final Property ignoredProperties = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#ignoredProperties" );

    /** <p>Specifies a list of allowed values so that each value node must be among the
     *  members of the given list.</p>
     */
    public static final Property in = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#in" );

    /** <p>A list of node expressions that shall be intersected.</p> */
    public static final Property intersection = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#intersection" );

    /** <p>The (single) value of this property represents an inverse path (object to
     *  subject).</p>
     */
    public static final Property inversePath = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#inversePath" );

    /** <p>Constraints expressed in JavaScript.</p> */
    public static final Property js = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#js" );

    /** <p>The name of the JavaScript function to execute.</p> */
    public static final Property jsFunctionName = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#jsFunctionName" );

    /** <p>Declares which JavaScript libraries are needed to execute this.</p> */
    public static final Property jsLibrary = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#jsLibrary" );

    /** <p>Declares the URLs of a JavaScript library. This should be the absolute URL
     *  of a JavaScript file. Implementations may redirect those to local files.</p>
     */
    public static final Property jsLibraryURL = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#jsLibraryURL" );

    /** <p>Outlines how human-readable labels of instances of the associated Parameterizable
     *  shall be produced. The values can contain {?paramName} as placeholders for
     *  the actual values of the given parameter.</p>
     */
    public static final Property labelTemplate = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#labelTemplate" );

    /** <p>Specifies a list of language tags that all value nodes must have.</p> */
    public static final Property languageIn = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#languageIn" );

    /** <p>Specifies a property that must have smaller values than the value nodes.</p> */
    public static final Property lessThan = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#lessThan" );

    /** <p>Specifies a property that must have smaller or equal values than the value
     *  nodes.</p>
     */
    public static final Property lessThanOrEquals = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#lessThanOrEquals" );

    /** <p>Specifies the maximum number of values in the set of value nodes.</p> */
    public static final Property maxCount = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#maxCount" );

    /** <p>Specifies the maximum exclusive value of each value node.</p> */
    public static final Property maxExclusive = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#maxExclusive" );

    /** <p>Specifies the maximum inclusive value of each value node.</p> */
    public static final Property maxInclusive = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#maxInclusive" );

    /** <p>Specifies the maximum string length of each value node.</p> */
    public static final Property maxLength = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#maxLength" );

    /** <p>A human-readable message (possibly with placeholders for variables) explaining
     *  the cause of the result.</p>
     */
    public static final Property message = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#message" );

    /** <p>Specifies the minimum number of values in the set of value nodes.</p> */
    public static final Property minCount = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#minCount" );

    /** <p>Specifies the minimum exclusive value of each value node.</p> */
    public static final Property minExclusive = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#minExclusive" );

    /** <p>Specifies the minimum inclusive value of each value node.</p> */
    public static final Property minInclusive = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#minInclusive" );

    /** <p>Specifies the minimum string length of each value node.</p> */
    public static final Property minLength = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#minLength" );

    /** <p>Human-readable labels for the property in the context of the surrounding shape.</p> */
    public static final Property name = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#name" );

    /** <p>The namespace associated with a prefix in a prefix declaration.</p> */
    public static final Property namespace = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#namespace" );

    /** <p>Specifies the node shape that all value nodes must conform to.</p> */
    public static final Property node = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#node" );

    /** <p>Specifies the node kind (e.g. IRI or literal) each value node.</p> */
    public static final Property nodeKind = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#nodeKind" );

    /** <p>The validator(s) used to evaluate a constraint in the context of a node shape.</p> */
    public static final Property nodeValidator = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#nodeValidator" );

    /** <p>The node expression producing the input nodes of a filter shape expression.</p> */
    public static final Property nodes = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#nodes" );

    /** <p>Specifies a shape that the value nodes must not conform to.</p> */
    public static final Property not = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#not" );

    /** <p>An expression producing the nodes that shall be inferred as objects.</p> */
    public static final Property object = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#object" );

    /** <p>The (single) value of this property represents a path that is matched one
     *  or more times.</p>
     */
    public static final Property oneOrMorePath = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#oneOrMorePath" );

    /** <p>Indicates whether a parameter is optional.</p> */
    public static final Property optional = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#optional" );

    /** <p>Specifies a list of shapes so that the value nodes must conform to at least
     *  one of the shapes.</p>
     */
    public static final Property or = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#or" );

    /** <p>Specifies the relative order of this compared to its siblings. For example
     *  use 0 for the first, 1 for the second.</p>
     */
    public static final Property order = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#order" );

    /** <p>The parameters of a function or constraint component.</p> */
    public static final Property parameter = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#parameter" );

    /** <p>Specifies the property path of a property shape.</p> */
    public static final Property path = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#path" );

    /** <p>Specifies a regular expression pattern that the string representations of
     *  the value nodes must match.</p>
     */
    public static final Property pattern = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#pattern" );

    /** <p>An expression producing the properties that shall be inferred as predicates.</p> */
    public static final Property predicate = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#predicate" );

    /** <p>The prefix of a prefix declaration.</p> */
    public static final Property prefix = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#prefix" );

    /** <p>The prefixes that shall be applied before parsing the associated SPARQL query.</p> */
    public static final Property prefixes = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#prefixes" );

    /** <p>Links a shape to its property shapes.</p> */
    public static final Property property = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#property" );

    /** <p>The validator(s) used to evaluate a constraint in the context of a property
     *  shape.</p>
     */
    public static final Property propertyValidator = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#propertyValidator" );

    /** <p>The maximum number of value nodes that can conform to the shape.</p> */
    public static final Property qualifiedMaxCount = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#qualifiedMaxCount" );

    /** <p>The minimum number of value nodes that must conform to the shape.</p> */
    public static final Property qualifiedMinCount = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#qualifiedMinCount" );

    /** <p>The shape that a specified number of values must conform to.</p> */
    public static final Property qualifiedValueShape = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#qualifiedValueShape" );

    /** <p>Can be used to mark the qualified value shape to be disjoint with its sibling
     *  shapes.</p>
     */
    public static final Property qualifiedValueShapesDisjoint = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#qualifiedValueShapesDisjoint" );

    /** <p>The validation results contained in a validation report.</p> */
    public static final Property result = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#result" );

    /** <p>Links a SPARQL validator with zero or more sh:ResultAnnotation instances,
     *  defining how to derive additional result properties based on the variables
     *  of the SELECT query.</p>
     */
    public static final Property resultAnnotation = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#resultAnnotation" );

    /** <p>Human-readable messages explaining the cause of the result.</p> */
    public static final Property resultMessage = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#resultMessage" );

    /** <p>The path of a validation result, based on the path of the validated property
     *  shape.</p>
     */
    public static final Property resultPath = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#resultPath" );

    /** <p>The severity of the result, e.g. warning.</p> */
    public static final Property resultSeverity = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#resultSeverity" );

    /** <p>The expected type of values returned by the associated function.</p> */
    public static final Property returnType = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#returnType" );

    /** <p>The rules linked to a shape.</p> */
    public static final Property rule = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#rule" );

    /** <p>The SPARQL SELECT query to execute.</p> */
    public static final Property select = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#select" );

    /** <p>Defines the severity that validation results produced by a shape must have.
     *  Defaults to sh:Violation.</p>
     */
    public static final Property severity = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#severity" );

    /** <p>Shapes graphs that should be used when validating this data graph.</p> */
    public static final Property shapesGraph = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#shapesGraph" );

    /** <p>If true then the validation engine was certain that the shapes graph has passed
     *  all SHACL syntax requirements during the validation process.</p>
     */
    public static final Property shapesGraphWellFormed = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#shapesGraphWellFormed" );

    /** <p>The constraint that was validated when the result was produced.</p> */
    public static final Property sourceConstraint = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#sourceConstraint" );

    /** <p>The constraint component that is the source of the result.</p> */
    public static final Property sourceConstraintComponent = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#sourceConstraintComponent" );

    /** <p>The shape that is was validated when the result was produced.</p> */
    public static final Property sourceShape = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#sourceShape" );

    /** <p>Links a shape with SPARQL constraints.</p> */
    public static final Property sparql = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#sparql" );

    /** <p>An expression producing the resources that shall be inferred as subjects.</p> */
    public static final Property subject = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#subject" );

    /** <p>Suggested shapes graphs for this ontology. The values of this property may
     *  be used in the absence of specific sh:shapesGraph statements.</p>
     */
    public static final Property suggestedShapesGraph = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#suggestedShapesGraph" );

    /** <p>Links a shape to a target specified by an extension language, for example
     *  instances of sh:SPARQLTarget.</p>
     */
    public static final Property target = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#target" );

    /** <p>Links a shape to a class, indicating that all instances of the class must
     *  conform to the shape.</p>
     */
    public static final Property targetClass = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#targetClass" );

    /** <p>Links a shape to individual nodes, indicating that these nodes must conform
     *  to the shape.</p>
     */
    public static final Property targetNode = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#targetNode" );

    /** <p>Links a shape to a property, indicating that all all objects of triples that
     *  have the given property as their predicate must conform to the shape.</p>
     */
    public static final Property targetObjectsOf = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#targetObjectsOf" );

    /** <p>Links a shape to a property, indicating that all subjects of triples that
     *  have the given property as their predicate must conform to the shape.</p>
     */
    public static final Property targetSubjectsOf = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#targetSubjectsOf" );

    /** <p>A list of node expressions that shall be used together.</p> */
    public static final Property union = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#union" );

    /** <p>Specifies whether all node values must have a unique (or no) language tag.</p> */
    public static final Property uniqueLang = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#uniqueLang" );

    /** <p>The SPARQL UPDATE to execute.</p> */
    public static final Property update = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#update" );

    /** <p>The validator(s) used to evaluate constraints of either node or property shapes.</p> */
    public static final Property validator = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#validator" );

    /** <p>An RDF node that has caused the result.</p> */
    public static final Property value = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#value" );

    /** <p>Specifies a list of shapes so that the value nodes must conform to exactly
     *  one of the shapes.</p>
     */
    public static final Property xone = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#xone" );

    /** <p>The (single) value of this property represents a path that is matched zero
     *  or more times.</p>
     */
    public static final Property zeroOrMorePath = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#zeroOrMorePath" );

    /** <p>The (single) value of this property represents a path that is matched zero
     *  or one times.</p>
     */
    public static final Property zeroOrOnePath = M_MODEL.createProperty( "http://www.w3.org/ns/shacl#zeroOrOnePath" );

    /** <p>The base class of validation results, typically not instantiated directly.</p> */
    public static final Resource AbstractResult = M_MODEL.createResource( "http://www.w3.org/ns/shacl#AbstractResult" );

    /** <p>A constraint component that can be used to test whether a value node conforms
     *  to all members of a provided list of shapes.</p>
     */
    public static final Resource AndConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#AndConstraintComponent" );

    public static final Resource AndConstraintComponent_and = M_MODEL.createResource( "http://www.w3.org/ns/shacl#AndConstraintComponent-and" );

    /** <p>The node kind of all blank nodes.</p> */
    public static final Resource BlankNode = M_MODEL.createResource( "http://www.w3.org/ns/shacl#BlankNode" );

    /** <p>The node kind of all blank nodes or IRIs.</p> */
    public static final Resource BlankNodeOrIRI = M_MODEL.createResource( "http://www.w3.org/ns/shacl#BlankNodeOrIRI" );

    /** <p>The node kind of all blank nodes or literals.</p> */
    public static final Resource BlankNodeOrLiteral = M_MODEL.createResource( "http://www.w3.org/ns/shacl#BlankNodeOrLiteral" );

    /** <p>A constraint component that can be used to verify that each value node is
     *  an instance of a given type.</p>
     */
    public static final Resource ClassConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#ClassConstraintComponent" );

    public static final Resource ClassConstraintComponent_class = M_MODEL.createResource( "http://www.w3.org/ns/shacl#ClassConstraintComponent-class" );

    /** <p>A constraint component that can be used to indicate that focus nodes must
     *  only have values for those properties that have been explicitly enumerated
     *  via sh:property/sh:path.</p>
     */
    public static final Resource ClosedConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#ClosedConstraintComponent" );

    public static final Resource ClosedConstraintComponent_closed = M_MODEL.createResource( "http://www.w3.org/ns/shacl#ClosedConstraintComponent-closed" );

    public static final Resource ClosedConstraintComponent_ignoredProperties = M_MODEL.createResource( "http://www.w3.org/ns/shacl#ClosedConstraintComponent-ignoredProperties" );

    /** <p>The class of constraint components.</p> */
    public static final Resource ConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#ConstraintComponent" );

    /** <p>A constraint component that can be used to restrict the datatype of all value
     *  nodes.</p>
     */
    public static final Resource DatatypeConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#DatatypeConstraintComponent" );

    public static final Resource DatatypeConstraintComponent_datatype = M_MODEL.createResource( "http://www.w3.org/ns/shacl#DatatypeConstraintComponent-datatype" );

    /** <p>A constraint component that can be used to verify that the set of value nodes
     *  is disjoint with the the set of nodes that have the focus node as subject
     *  and the value of a given property as predicate.</p>
     */
    public static final Resource DisjointConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#DisjointConstraintComponent" );

    public static final Resource DisjointConstraintComponent_disjoint = M_MODEL.createResource( "http://www.w3.org/ns/shacl#DisjointConstraintComponent-disjoint" );

    /** <p>A constraint component that can be used to verify that the set of value nodes
     *  is equal to the set of nodes that have the focus node as subject and the value
     *  of a given property as predicate.</p>
     */
    public static final Resource EqualsConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#EqualsConstraintComponent" );

    public static final Resource EqualsConstraintComponent_equals = M_MODEL.createResource( "http://www.w3.org/ns/shacl#EqualsConstraintComponent-equals" );

    /** <p>A constraint component that can be used to verify that a given node expression
     *  produces true for all value nodes.</p>
     */
    public static final Resource ExpressionConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#ExpressionConstraintComponent" );

    public static final Resource ExpressionConstraintComponent_expression = M_MODEL.createResource( "http://www.w3.org/ns/shacl#ExpressionConstraintComponent-expression" );

    /** <p>The class of SHACL functions.</p> */
    public static final Resource Function = M_MODEL.createResource( "http://www.w3.org/ns/shacl#Function" );

    /** <p>A constraint component that can be used to verify that one of the value nodes
     *  is a given RDF node.</p>
     */
    public static final Resource HasValueConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#HasValueConstraintComponent" );

    public static final Resource HasValueConstraintComponent_hasValue = M_MODEL.createResource( "http://www.w3.org/ns/shacl#HasValueConstraintComponent-hasValue" );

    /** <p>The node kind of all IRIs.</p> */
    public static final Resource IRI = M_MODEL.createResource( "http://www.w3.org/ns/shacl#IRI" );

    /** <p>The node kind of all IRIs or literals.</p> */
    public static final Resource IRIOrLiteral = M_MODEL.createResource( "http://www.w3.org/ns/shacl#IRIOrLiteral" );

    /** <p>A constraint component that can be used to exclusively enumerate the permitted
     *  value nodes.</p>
     */
    public static final Resource InConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#InConstraintComponent" );

    public static final Resource InConstraintComponent_in = M_MODEL.createResource( "http://www.w3.org/ns/shacl#InConstraintComponent-in" );

    /** <p>The severity for an informational validation result.</p> */
    public static final Resource Info = M_MODEL.createResource( "http://www.w3.org/ns/shacl#Info" );

    /** <p>The class of constraints backed by a JavaScript function.</p> */
    public static final Resource JSConstraint = M_MODEL.createResource( "http://www.w3.org/ns/shacl#JSConstraint" );

    public static final Resource JSConstraint_js = M_MODEL.createResource( "http://www.w3.org/ns/shacl#JSConstraint-js" );

    /** <p>A constraint component with the parameter sh:js linking to a sh:JSConstraint
     *  containing a sh:script.</p>
     */
    public static final Resource JSConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#JSConstraintComponent" );

    /** <p>Abstract base class of resources that declare an executable JavaScript.</p> */
    public static final Resource JSExecutable = M_MODEL.createResource( "http://www.w3.org/ns/shacl#JSExecutable" );

    /** <p>The class of SHACL functions that execute a JavaScript function when called.</p> */
    public static final Resource JSFunction = M_MODEL.createResource( "http://www.w3.org/ns/shacl#JSFunction" );

    /** <p>Represents a JavaScript library, typically identified by one or more URLs
     *  of files to include.</p>
     */
    public static final Resource JSLibrary = M_MODEL.createResource( "http://www.w3.org/ns/shacl#JSLibrary" );

    /** <p>The class of SHACL rules expressed using JavaScript.</p> */
    public static final Resource JSRule = M_MODEL.createResource( "http://www.w3.org/ns/shacl#JSRule" );

    /** <p>The class of targets that are based on JavaScript functions.</p> */
    public static final Resource JSTarget = M_MODEL.createResource( "http://www.w3.org/ns/shacl#JSTarget" );

    /** <p>The (meta) class for parameterizable targets that are based on JavaScript
     *  functions.</p>
     */
    public static final Resource JSTargetType = M_MODEL.createResource( "http://www.w3.org/ns/shacl#JSTargetType" );

    /** <p>A SHACL validator based on JavaScript. This can be used to declare SHACL constraint
     *  components that perform JavaScript-based validation when used.</p>
     */
    public static final Resource JSValidator = M_MODEL.createResource( "http://www.w3.org/ns/shacl#JSValidator" );

    /** <p>A constraint component that can be used to enumerate language tags that all
     *  value nodes must have.</p>
     */
    public static final Resource LanguageInConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#LanguageInConstraintComponent" );

    public static final Resource LanguageInConstraintComponent_languageIn = M_MODEL.createResource( "http://www.w3.org/ns/shacl#LanguageInConstraintComponent-languageIn" );

    /** <p>A constraint component that can be used to verify that each value node is
     *  smaller than all the nodes that have the focus node as subject and the value
     *  of a given property as predicate.</p>
     */
    public static final Resource LessThanConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#LessThanConstraintComponent" );

    public static final Resource LessThanConstraintComponent_lessThan = M_MODEL.createResource( "http://www.w3.org/ns/shacl#LessThanConstraintComponent-lessThan" );

    /** <p>A constraint component that can be used to verify that every value node is
     *  smaller than all the nodes that have the focus node as subject and the value
     *  of a given property as predicate.</p>
     */
    public static final Resource LessThanOrEqualsConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#LessThanOrEqualsConstraintComponent" );

    public static final Resource LessThanOrEqualsConstraintComponent_lessThanOrEquals = M_MODEL.createResource( "http://www.w3.org/ns/shacl#LessThanOrEqualsConstraintComponent-lessThanOrEquals" );

    /** <p>The node kind of all literals.</p> */
    public static final Resource Literal = M_MODEL.createResource( "http://www.w3.org/ns/shacl#Literal" );

    /** <p>A constraint component that can be used to restrict the maximum number of
     *  value nodes.</p>
     */
    public static final Resource MaxCountConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#MaxCountConstraintComponent" );

    public static final Resource MaxCountConstraintComponent_maxCount = M_MODEL.createResource( "http://www.w3.org/ns/shacl#MaxCountConstraintComponent-maxCount" );

    /** <p>A constraint component that can be used to restrict the range of value nodes
     *  with a maximum exclusive value.</p>
     */
    public static final Resource MaxExclusiveConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#MaxExclusiveConstraintComponent" );

    public static final Resource MaxExclusiveConstraintComponent_maxExclusive = M_MODEL.createResource( "http://www.w3.org/ns/shacl#MaxExclusiveConstraintComponent-maxExclusive" );

    /** <p>A constraint component that can be used to restrict the range of value nodes
     *  with a maximum inclusive value.</p>
     */
    public static final Resource MaxInclusiveConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#MaxInclusiveConstraintComponent" );

    public static final Resource MaxInclusiveConstraintComponent_maxInclusive = M_MODEL.createResource( "http://www.w3.org/ns/shacl#MaxInclusiveConstraintComponent-maxInclusive" );

    /** <p>A constraint component that can be used to restrict the maximum string length
     *  of value nodes.</p>
     */
    public static final Resource MaxLengthConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#MaxLengthConstraintComponent" );

    public static final Resource MaxLengthConstraintComponent_maxLength = M_MODEL.createResource( "http://www.w3.org/ns/shacl#MaxLengthConstraintComponent-maxLength" );

    /** <p>A constraint component that can be used to restrict the minimum number of
     *  value nodes.</p>
     */
    public static final Resource MinCountConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#MinCountConstraintComponent" );

    public static final Resource MinCountConstraintComponent_minCount = M_MODEL.createResource( "http://www.w3.org/ns/shacl#MinCountConstraintComponent-minCount" );

    /** <p>A constraint component that can be used to restrict the range of value nodes
     *  with a minimum exclusive value.</p>
     */
    public static final Resource MinExclusiveConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#MinExclusiveConstraintComponent" );

    public static final Resource MinExclusiveConstraintComponent_minExclusive = M_MODEL.createResource( "http://www.w3.org/ns/shacl#MinExclusiveConstraintComponent-minExclusive" );

    /** <p>A constraint component that can be used to restrict the range of value nodes
     *  with a minimum inclusive value.</p>
     */
    public static final Resource MinInclusiveConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#MinInclusiveConstraintComponent" );

    public static final Resource MinInclusiveConstraintComponent_minInclusive = M_MODEL.createResource( "http://www.w3.org/ns/shacl#MinInclusiveConstraintComponent-minInclusive" );

    /** <p>A constraint component that can be used to restrict the minimum string length
     *  of value nodes.</p>
     */
    public static final Resource MinLengthConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#MinLengthConstraintComponent" );

    public static final Resource MinLengthConstraintComponent_minLength = M_MODEL.createResource( "http://www.w3.org/ns/shacl#MinLengthConstraintComponent-minLength" );

    /** <p>A constraint component that can be used to verify that all value nodes conform
     *  to the given node shape.</p>
     */
    public static final Resource NodeConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#NodeConstraintComponent" );

    public static final Resource NodeConstraintComponent_node = M_MODEL.createResource( "http://www.w3.org/ns/shacl#NodeConstraintComponent-node" );

    /** <p>The class of all node kinds, including sh:BlankNode, sh:IRI, sh:Literal or
     *  the combinations of these: sh:BlankNodeOrIRI, sh:BlankNodeOrLiteral, sh:IRIOrLiteral.</p>
     */
    public static final Resource NodeKind = M_MODEL.createResource( "http://www.w3.org/ns/shacl#NodeKind" );

    /** <p>A constraint component that can be used to restrict the RDF node kind of each
     *  value node.</p>
     */
    public static final Resource NodeKindConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#NodeKindConstraintComponent" );

    public static final Resource NodeKindConstraintComponent_nodeKind = M_MODEL.createResource( "http://www.w3.org/ns/shacl#NodeKindConstraintComponent-nodeKind" );

    /** <p>A node shape is a shape that specifies constraint that need to be met with
     *  respect to focus nodes.</p>
     */
    public static final Resource NodeShape = M_MODEL.createResource( "http://www.w3.org/ns/shacl#NodeShape" );

    /** <p>A constraint component that can be used to verify that value nodes do not
     *  conform to a given shape.</p>
     */
    public static final Resource NotConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#NotConstraintComponent" );

    public static final Resource NotConstraintComponent_not = M_MODEL.createResource( "http://www.w3.org/ns/shacl#NotConstraintComponent-not" );

    /** <p>A constraint component that can be used to restrict the value nodes so that
     *  they conform to at least one out of several provided shapes.</p>
     */
    public static final Resource OrConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#OrConstraintComponent" );

    public static final Resource OrConstraintComponent_or = M_MODEL.createResource( "http://www.w3.org/ns/shacl#OrConstraintComponent-or" );

    /** <p>The class of parameter declarations, consisting of a path predicate and (possibly)
     *  information about allowed value type, cardinality and other characteristics.</p>
     */
    public static final Resource Parameter = M_MODEL.createResource( "http://www.w3.org/ns/shacl#Parameter" );

    /** <p>Superclass of components that can take parameters, especially functions and
     *  constraint components.</p>
     */
    public static final Resource Parameterizable = M_MODEL.createResource( "http://www.w3.org/ns/shacl#Parameterizable" );

    /** <p>A constraint component that can be used to verify that every value node matches
     *  a given regular expression.</p>
     */
    public static final Resource PatternConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#PatternConstraintComponent" );

    public static final Resource PatternConstraintComponent_flags = M_MODEL.createResource( "http://www.w3.org/ns/shacl#PatternConstraintComponent-flags" );

    public static final Resource PatternConstraintComponent_pattern = M_MODEL.createResource( "http://www.w3.org/ns/shacl#PatternConstraintComponent-pattern" );

    /** <p>The class of prefix declarations, consisting of pairs of a prefix with a namespace.</p> */
    public static final Resource PrefixDeclaration = M_MODEL.createResource( "http://www.w3.org/ns/shacl#PrefixDeclaration" );

    /** <p>A constraint component that can be used to verify that all value nodes conform
     *  to the given property shape.</p>
     */
    public static final Resource PropertyConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#PropertyConstraintComponent" );

    public static final Resource PropertyConstraintComponent_property = M_MODEL.createResource( "http://www.w3.org/ns/shacl#PropertyConstraintComponent-property" );

    /** <p>Instances of this class represent groups of property shapes that belong together.</p> */
    public static final Resource PropertyGroup = M_MODEL.createResource( "http://www.w3.org/ns/shacl#PropertyGroup" );

    /** <p>A property shape is a shape that specifies constraints on the values of a
     *  focus node for a given property or path.</p>
     */
    public static final Resource PropertyShape = M_MODEL.createResource( "http://www.w3.org/ns/shacl#PropertyShape" );

    /** <p>A constraint component that can be used to verify that a specified maximum
     *  number of value nodes conforms to a given shape.</p>
     */
    public static final Resource QualifiedMaxCountConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#QualifiedMaxCountConstraintComponent" );

    public static final Resource QualifiedMaxCountConstraintComponent_qualifiedMaxCount = M_MODEL.createResource( "http://www.w3.org/ns/shacl#QualifiedMaxCountConstraintComponent-qualifiedMaxCount" );

    public static final Resource QualifiedMaxCountConstraintComponent_qualifiedValueShape = M_MODEL.createResource( "http://www.w3.org/ns/shacl#QualifiedMaxCountConstraintComponent-qualifiedValueShape" );

    public static final Resource QualifiedMaxCountConstraintComponent_qualifiedValueShapesDisjoint = M_MODEL.createResource( "http://www.w3.org/ns/shacl#QualifiedMaxCountConstraintComponent-qualifiedValueShapesDisjoint" );

    /** <p>A constraint component that can be used to verify that a specified minimum
     *  number of value nodes conforms to a given shape.</p>
     */
    public static final Resource QualifiedMinCountConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#QualifiedMinCountConstraintComponent" );

    public static final Resource QualifiedMinCountConstraintComponent_qualifiedMinCount = M_MODEL.createResource( "http://www.w3.org/ns/shacl#QualifiedMinCountConstraintComponent-qualifiedMinCount" );

    public static final Resource QualifiedMinCountConstraintComponent_qualifiedValueShape = M_MODEL.createResource( "http://www.w3.org/ns/shacl#QualifiedMinCountConstraintComponent-qualifiedValueShape" );

    public static final Resource QualifiedMinCountConstraintComponent_qualifiedValueShapesDisjoint = M_MODEL.createResource( "http://www.w3.org/ns/shacl#QualifiedMinCountConstraintComponent-qualifiedValueShapesDisjoint" );

    /** <p>A class of result annotations, which define the rules to derive the values
     *  of a given annotation property as extra values for a validation result.</p>
     */
    public static final Resource ResultAnnotation = M_MODEL.createResource( "http://www.w3.org/ns/shacl#ResultAnnotation" );

    /** <p>The class of SHACL rules. Never instantiated directly.</p> */
    public static final Resource Rule = M_MODEL.createResource( "http://www.w3.org/ns/shacl#Rule" );

    /** <p>The class of SPARQL executables that are based on an ASK query.</p> */
    public static final Resource SPARQLAskExecutable = M_MODEL.createResource( "http://www.w3.org/ns/shacl#SPARQLAskExecutable" );

    /** <p>The class of validators based on SPARQL ASK queries. The queries are evaluated
     *  for each value node and are supposed to return true if the given node conforms.</p>
     */
    public static final Resource SPARQLAskValidator = M_MODEL.createResource( "http://www.w3.org/ns/shacl#SPARQLAskValidator" );

    /** <p>The class of constraints based on SPARQL SELECT queries.</p> */
    public static final Resource SPARQLConstraint = M_MODEL.createResource( "http://www.w3.org/ns/shacl#SPARQLConstraint" );

    /** <p>A constraint component that can be used to define constraints based on SPARQL
     *  queries.</p>
     */
    public static final Resource SPARQLConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#SPARQLConstraintComponent" );

    public static final Resource SPARQLConstraintComponent_sparql = M_MODEL.createResource( "http://www.w3.org/ns/shacl#SPARQLConstraintComponent-sparql" );

    /** <p>The class of SPARQL executables that are based on a CONSTRUCT query.</p> */
    public static final Resource SPARQLConstructExecutable = M_MODEL.createResource( "http://www.w3.org/ns/shacl#SPARQLConstructExecutable" );

    /** <p>The class of resources that encapsulate a SPARQL query.</p> */
    public static final Resource SPARQLExecutable = M_MODEL.createResource( "http://www.w3.org/ns/shacl#SPARQLExecutable" );

    /** <p>A function backed by a SPARQL query - either ASK or SELECT.</p> */
    public static final Resource SPARQLFunction = M_MODEL.createResource( "http://www.w3.org/ns/shacl#SPARQLFunction" );

    /** <p>The class of SHACL rules based on SPARQL CONSTRUCT queries.</p> */
    public static final Resource SPARQLRule = M_MODEL.createResource( "http://www.w3.org/ns/shacl#SPARQLRule" );

    /** <p>The class of SPARQL executables based on a SELECT query.</p> */
    public static final Resource SPARQLSelectExecutable = M_MODEL.createResource( "http://www.w3.org/ns/shacl#SPARQLSelectExecutable" );

    /** <p>The class of validators based on SPARQL SELECT queries. The queries are evaluated
     *  for each focus node and are supposed to produce bindings for all focus nodes
     *  that do not conform.</p>
     */
    public static final Resource SPARQLSelectValidator = M_MODEL.createResource( "http://www.w3.org/ns/shacl#SPARQLSelectValidator" );

    /** <p>The class of targets that are based on SPARQL queries.</p> */
    public static final Resource SPARQLTarget = M_MODEL.createResource( "http://www.w3.org/ns/shacl#SPARQLTarget" );

    /** <p>The (meta) class for parameterizable targets that are based on SPARQL queries.</p> */
    public static final Resource SPARQLTargetType = M_MODEL.createResource( "http://www.w3.org/ns/shacl#SPARQLTargetType" );

    /** <p>The class of SPARQL executables based on a SPARQL UPDATE.</p> */
    public static final Resource SPARQLUpdateExecutable = M_MODEL.createResource( "http://www.w3.org/ns/shacl#SPARQLUpdateExecutable" );

    /** <p>The class of validation result severity levels, including violation and warning
     *  levels.</p>
     */
    public static final Resource Severity = M_MODEL.createResource( "http://www.w3.org/ns/shacl#Severity" );

    /** <p>A shape is a collection of constraints that may be targeted for certain nodes.</p> */
    public static final Resource Shape = M_MODEL.createResource( "http://www.w3.org/ns/shacl#Shape" );

    /** <p>The base class of targets such as those based on SPARQL queries.</p> */
    public static final Resource Target = M_MODEL.createResource( "http://www.w3.org/ns/shacl#Target" );

    /** <p>The (meta) class for parameterizable targets. Instances of this are instantiated
     *  as values of the sh:target property.</p>
     */
    public static final Resource TargetType = M_MODEL.createResource( "http://www.w3.org/ns/shacl#TargetType" );

    public static final Resource TripleRule = M_MODEL.createResource( "http://www.w3.org/ns/shacl#TripleRule" );

    /** <p>A constraint component that can be used to specify that no pair of value nodes
     *  may use the same language tag.</p>
     */
    public static final Resource UniqueLangConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#UniqueLangConstraintComponent" );

    public static final Resource UniqueLangConstraintComponent_uniqueLang = M_MODEL.createResource( "http://www.w3.org/ns/shacl#UniqueLangConstraintComponent-uniqueLang" );

    /** <p>The class of SHACL validation reports.</p> */
    public static final Resource ValidationReport = M_MODEL.createResource( "http://www.w3.org/ns/shacl#ValidationReport" );

    /** <p>The class of validation results.</p> */
    public static final Resource ValidationResult = M_MODEL.createResource( "http://www.w3.org/ns/shacl#ValidationResult" );

    /** <p>The class of validators, which provide instructions on how to process a constraint
     *  definition. This class serves as base class for the SPARQL-based validators
     *  and other possible implementations.</p>
     */
    public static final Resource Validator = M_MODEL.createResource( "http://www.w3.org/ns/shacl#Validator" );

    /** <p>The severity for a violation validation result.</p> */
    public static final Resource Violation = M_MODEL.createResource( "http://www.w3.org/ns/shacl#Violation" );

    /** <p>The severity for a warning validation result.</p> */
    public static final Resource Warning = M_MODEL.createResource( "http://www.w3.org/ns/shacl#Warning" );

    /** <p>A constraint component that can be used to restrict the value nodes so that
     *  they conform to exactly one out of several provided shapes.</p>
     */
    public static final Resource XoneConstraintComponent = M_MODEL.createResource( "http://www.w3.org/ns/shacl#XoneConstraintComponent" );

    public static final Resource XoneConstraintComponent_xone = M_MODEL.createResource( "http://www.w3.org/ns/shacl#XoneConstraintComponent-xone" );
}
