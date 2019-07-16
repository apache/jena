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

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

// Vocabulary for http://www.w3.org/ns/shacl#
// schemagen then converted to Node level.

public class SHACL {
    private static Node createResource(String uri) { return NodeFactory.createURI(uri); }
    private static Node createProperty(String uri) { return NodeFactory.createURI(uri); }

    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.w3.org/ns/shacl#";

    /** <p>The namespace of the vocabulary as a string</p>
     * @return namespace as String
     * @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a Node</p> */
    public static final Node NAMESPACE = createResource( NS );

    /** <p>The (single) value of this property must be a list of path elements, representing
     *  the elements of alternative paths.</p>
     */
    public static final Node alternativePath = createProperty( "http://www.w3.org/ns/shacl#alternativePath" );

    /** <p>RDF list of shapes to validate the value nodes against.</p> */
    public static final Node and = createProperty( "http://www.w3.org/ns/shacl#and" );

    /** <p>The annotation property that shall be set.</p> */
    public static final Node annotationProperty = createProperty( "http://www.w3.org/ns/shacl#annotationProperty" );

    /** <p>The (default) values of the annotation property.</p> */
    public static final Node annotationValue = createProperty( "http://www.w3.org/ns/shacl#annotationValue" );

    /** <p>The name of the SPARQL variable from the SELECT clause that shall be used
     *  for the values.</p>
     */
    public static final Node annotationVarName = createProperty( "http://www.w3.org/ns/shacl#annotationVarName" );

    /** <p>The SPARQL ASK query to execute.</p> */
    public static final Node ask = createProperty( "http://www.w3.org/ns/shacl#ask" );

    /** <p>The type that all value nodes must have.</p> */
    public static final Node class_ = createProperty( "http://www.w3.org/ns/shacl#class" );

    /** <p>If set to true then the shape is closed.</p> */
    public static final Node closed = createProperty( "http://www.w3.org/ns/shacl#closed" );

    /** <p>The shapes that the focus nodes need to conform to before a rule is executed
     *  on them.</p>
     */
    public static final Node condition = createProperty( "http://www.w3.org/ns/shacl#condition" );

    /** <p>True if the validation did not produce any validation results, and false otherwise.</p> */
    public static final Node conforms = createProperty( "http://www.w3.org/ns/shacl#conforms" );

    /** <p>The SPARQL CONSTRUCT query to execute.</p> */
    public static final Node construct = createProperty( "http://www.w3.org/ns/shacl#construct" );

    /** <p>Specifies an RDF datatype that all value nodes must have.</p> */
    public static final Node datatype = createProperty( "http://www.w3.org/ns/shacl#datatype" );

    /** <p>If set to true then all nodes conform to this.</p> */
    public static final Node deactivated = createProperty( "http://www.w3.org/ns/shacl#deactivated" );

    /** <p>Links a resource with its namespace prefix declarations.</p> */
    public static final Node declare = createProperty( "http://www.w3.org/ns/shacl#declare" );

    /** <p>A default value for a property, for example for user interface tools to pre-populate
     *  input fields.</p>
     */
    public static final Node defaultValue = createProperty( "http://www.w3.org/ns/shacl#defaultValue" );

    /** <p>Human-readable descriptions for the property in the context of the surrounding
     *  shape.</p>
     */
    public static final Node description = createProperty( "http://www.w3.org/ns/shacl#description" );

    /** <p>Links a result with other results that provide more details, for example to
     *  describe violations against nested shapes.</p>
     */
    public static final Node detail = createProperty( "http://www.w3.org/ns/shacl#detail" );

    /** <p>Specifies a property where the set of values must be disjoint with the value
     *  nodes.</p>
     */
    public static final Node disjoint = createProperty( "http://www.w3.org/ns/shacl#disjoint" );

    /** <p>An entailment regime that indicates what kind of inferencing is required by
     *  a shapes graph.</p>
     */
    public static final Node entailment = createProperty( "http://www.w3.org/ns/shacl#entailment" );

    /** <p>Specifies a property that must have the same values as the value nodes.</p> */
    public static final Node equals = createProperty( "http://www.w3.org/ns/shacl#equals" );

    /** <p>The node expression that must return true for the value nodes.</p> */
    public static final Node expression = createProperty( "http://www.w3.org/ns/shacl#expression" );

    /** <p>The shape that all input nodes of the expression need to conform to.</p> */
    public static final Node filterShape = createProperty( "http://www.w3.org/ns/shacl#filterShape" );

    /** <p>An optional flag to be used with regular expression pattern matching.</p> */
    public static final Node flags = createProperty( "http://www.w3.org/ns/shacl#flags" );

    /** <p>The focus node that was validated when the result was produced.</p> */
    public static final Node focusNode = createProperty( "http://www.w3.org/ns/shacl#focusNode" );

    /** <p>Can be used to link to a property group to indicate that a property shape
     *  belongs to a group of related property shapes.</p>
     */
    public static final Node group = createProperty( "http://www.w3.org/ns/shacl#group" );

    /** <p>Specifies a value that must be among the value nodes.</p> */
    public static final Node hasValue = createProperty( "http://www.w3.org/ns/shacl#hasValue" );

    /** <p>An optional RDF list of properties that are also permitted in addition to
     *  those explicitly enumerated via sh:property/sh:path.</p>
     */
    public static final Node ignoredProperties = createProperty( "http://www.w3.org/ns/shacl#ignoredProperties" );

    /** <p>Specifies a list of allowed values so that each value node must be among the
     *  members of the given list.</p>
     */
    public static final Node in = createProperty( "http://www.w3.org/ns/shacl#in" );

    /** <p>A list of node expressions that shall be intersected.</p> */
    public static final Node intersection = createProperty( "http://www.w3.org/ns/shacl#intersection" );

    /** <p>The (single) value of this property represents an inverse path (object to
     *  subject).</p>
     */
    public static final Node inversePath = createProperty( "http://www.w3.org/ns/shacl#inversePath" );

    /** <p>Constraints expressed in JavaScript.</p> */
    public static final Node js = createProperty( "http://www.w3.org/ns/shacl#js" );

    /** <p>The name of the JavaScript function to execute.</p> */
    public static final Node jsFunctionName = createProperty( "http://www.w3.org/ns/shacl#jsFunctionName" );

    /** <p>Declares which JavaScript libraries are needed to execute this.</p> */
    public static final Node jsLibrary = createProperty( "http://www.w3.org/ns/shacl#jsLibrary" );

    /** <p>Declares the URLs of a JavaScript library. This should be the absolute URL
     *  of a JavaScript file. Implementations may redirect those to local files.</p>
     */
    public static final Node jsLibraryURL = createProperty( "http://www.w3.org/ns/shacl#jsLibraryURL" );

    /** <p>Outlines how human-readable labels of instances of the associated Parameterizable
     *  shall be produced. The values can contain {?paramName} as placeholders for
     *  the actual values of the given parameter.</p>
     */
    public static final Node labelTemplate = createProperty( "http://www.w3.org/ns/shacl#labelTemplate" );

    /** <p>Specifies a list of language tags that all value nodes must have.</p> */
    public static final Node languageIn = createProperty( "http://www.w3.org/ns/shacl#languageIn" );

    /** <p>Specifies a property that must have smaller values than the value nodes.</p> */
    public static final Node lessThan = createProperty( "http://www.w3.org/ns/shacl#lessThan" );

    /** <p>Specifies a property that must have smaller or equal values than the value
     *  nodes.</p>
     */
    public static final Node lessThanOrEquals = createProperty( "http://www.w3.org/ns/shacl#lessThanOrEquals" );

    /** <p>Specifies the maximum number of values in the set of value nodes.</p> */
    public static final Node maxCount = createProperty( "http://www.w3.org/ns/shacl#maxCount" );

    /** <p>Specifies the maximum exclusive value of each value node.</p> */
    public static final Node maxExclusive = createProperty( "http://www.w3.org/ns/shacl#maxExclusive" );

    /** <p>Specifies the maximum inclusive value of each value node.</p> */
    public static final Node maxInclusive = createProperty( "http://www.w3.org/ns/shacl#maxInclusive" );

    /** <p>Specifies the maximum string length of each value node.</p> */
    public static final Node maxLength = createProperty( "http://www.w3.org/ns/shacl#maxLength" );

    /** <p>A human-readable message (possibly with placeholders for variables) explaining
     *  the cause of the result.</p>
     */
    public static final Node message = createProperty( "http://www.w3.org/ns/shacl#message" );

    /** <p>Specifies the minimum number of values in the set of value nodes.</p> */
    public static final Node minCount = createProperty( "http://www.w3.org/ns/shacl#minCount" );

    /** <p>Specifies the minimum exclusive value of each value node.</p> */
    public static final Node minExclusive = createProperty( "http://www.w3.org/ns/shacl#minExclusive" );

    /** <p>Specifies the minimum inclusive value of each value node.</p> */
    public static final Node minInclusive = createProperty( "http://www.w3.org/ns/shacl#minInclusive" );

    /** <p>Specifies the minimum string length of each value node.</p> */
    public static final Node minLength = createProperty( "http://www.w3.org/ns/shacl#minLength" );

    /** <p>Human-readable labels for the property in the context of the surrounding shape.</p> */
    public static final Node name = createProperty( "http://www.w3.org/ns/shacl#name" );

    /** <p>The namespace associated with a prefix in a prefix declaration.</p> */
    public static final Node namespace = createProperty( "http://www.w3.org/ns/shacl#namespace" );

    /** <p>Specifies the node shape that all value nodes must conform to.</p> */
    public static final Node node = createProperty( "http://www.w3.org/ns/shacl#node" );

    /** <p>Specifies the node kind (e.g. IRI or literal) each value node.</p> */
    public static final Node nodeKind = createProperty( "http://www.w3.org/ns/shacl#nodeKind" );

    /** <p>The validator(s) used to evaluate a constraint in the context of a node shape.</p> */
    public static final Node nodeValidator = createProperty( "http://www.w3.org/ns/shacl#nodeValidator" );

    /** <p>The node expression producing the input nodes of a filter shape expression.</p> */
    public static final Node nodes = createProperty( "http://www.w3.org/ns/shacl#nodes" );

    /** <p>Specifies a shape that the value nodes must not conform to.</p> */
    public static final Node not = createProperty( "http://www.w3.org/ns/shacl#not" );

    /** <p>An expression producing the nodes that shall be inferred as objects.</p> */
    public static final Node object = createProperty( "http://www.w3.org/ns/shacl#object" );

    /** <p>The (single) value of this property represents a path that is matched one
     *  or more times.</p>
     */
    public static final Node oneOrMorePath = createProperty( "http://www.w3.org/ns/shacl#oneOrMorePath" );

    /** <p>Indicates whether a parameter is optional.</p> */
    public static final Node optional = createProperty( "http://www.w3.org/ns/shacl#optional" );

    /** <p>Specifies a list of shapes so that the value nodes must conform to at least
     *  one of the shapes.</p>
     */
    public static final Node or = createProperty( "http://www.w3.org/ns/shacl#or" );

    /** <p>Specifies the relative order of this compared to its siblings. For example
     *  use 0 for the first, 1 for the second.</p>
     */
    public static final Node order = createProperty( "http://www.w3.org/ns/shacl#order" );

    /** <p>The parameters of a function or constraint component.</p> */
    public static final Node parameter = createProperty( "http://www.w3.org/ns/shacl#parameter" );

    /** <p>Specifies the property path of a property shape.</p> */
    public static final Node path = createProperty( "http://www.w3.org/ns/shacl#path" );

    /** <p>Specifies a regular expression pattern that the string representations of
     *  the value nodes must match.</p>
     */
    public static final Node pattern = createProperty( "http://www.w3.org/ns/shacl#pattern" );

    /** <p>An expression producing the properties that shall be inferred as predicates.</p> */
    public static final Node predicate = createProperty( "http://www.w3.org/ns/shacl#predicate" );

    /** <p>The prefix of a prefix declaration.</p> */
    public static final Node prefix = createProperty( "http://www.w3.org/ns/shacl#prefix" );

    /** <p>The prefixes that shall be applied before parsing the associated SPARQL query.</p> */
    public static final Node prefixes = createProperty( "http://www.w3.org/ns/shacl#prefixes" );

    /** <p>Links a shape to its property shapes.</p> */
    public static final Node property = createProperty( "http://www.w3.org/ns/shacl#property" );

    /** <p>The validator(s) used to evaluate a constraint in the context of a property
     *  shape.</p>
     */
    public static final Node propertyValidator = createProperty( "http://www.w3.org/ns/shacl#propertyValidator" );

    /** <p>The maximum number of value nodes that can conform to the shape.</p> */
    public static final Node qualifiedMaxCount = createProperty( "http://www.w3.org/ns/shacl#qualifiedMaxCount" );

    /** <p>The minimum number of value nodes that must conform to the shape.</p> */
    public static final Node qualifiedMinCount = createProperty( "http://www.w3.org/ns/shacl#qualifiedMinCount" );

    /** <p>The shape that a specified number of values must conform to.</p> */
    public static final Node qualifiedValueShape = createProperty( "http://www.w3.org/ns/shacl#qualifiedValueShape" );

    /** <p>Can be used to mark the qualified value shape to be disjoint with its sibling
     *  shapes.</p>
     */
    public static final Node qualifiedValueShapesDisjoint = createProperty( "http://www.w3.org/ns/shacl#qualifiedValueShapesDisjoint" );

    /** <p>The validation results contained in a validation report.</p> */
    public static final Node result = createProperty( "http://www.w3.org/ns/shacl#result" );

    /** <p>Links a SPARQL validator with zero or more sh:ResultAnnotation instances,
     *  defining how to derive additional result properties based on the variables
     *  of the SELECT query.</p>
     */
    public static final Node resultAnnotation = createProperty( "http://www.w3.org/ns/shacl#resultAnnotation" );

    /** <p>Human-readable messages explaining the cause of the result.</p> */
    public static final Node resultMessage = createProperty( "http://www.w3.org/ns/shacl#resultMessage" );

    /** <p>The path of a validation result, based on the path of the validated property
     *  shape.</p>
     */
    public static final Node resultPath = createProperty( "http://www.w3.org/ns/shacl#resultPath" );

    /** <p>The severity of the result, e.g. warning.</p> */
    public static final Node resultSeverity = createProperty( "http://www.w3.org/ns/shacl#resultSeverity" );

    /** <p>The expected type of values returned by the associated function.</p> */
    public static final Node returnType = createProperty( "http://www.w3.org/ns/shacl#returnType" );

    /** <p>The rules linked to a shape.</p> */
    public static final Node rule = createProperty( "http://www.w3.org/ns/shacl#rule" );

    /** <p>The SPARQL SELECT query to execute.</p> */
    public static final Node select = createProperty( "http://www.w3.org/ns/shacl#select" );

    /** <p>Defines the severity that validation results produced by a shape must have.
     *  Defaults to sh:Violation.</p>
     */
    public static final Node severity = createProperty( "http://www.w3.org/ns/shacl#severity" );

    /** <p>Shapes graphs that should be used when validating this data graph.</p> */
    public static final Node shapesGraph = createProperty( "http://www.w3.org/ns/shacl#shapesGraph" );

    /** <p>If true then the validation engine was certain that the shapes graph has passed
     *  all SHACL syntax requirements during the validation process.</p>
     */
    public static final Node shapesGraphWellFormed = createProperty( "http://www.w3.org/ns/shacl#shapesGraphWellFormed" );

    /** <p>The constraint that was validated when the result was produced.</p> */
    public static final Node sourceConstraint = createProperty( "http://www.w3.org/ns/shacl#sourceConstraint" );

    /** <p>The constraint component that is the source of the result.</p> */
    public static final Node sourceConstraintComponent = createProperty( "http://www.w3.org/ns/shacl#sourceConstraintComponent" );

    /** <p>The shape that is was validated when the result was produced.</p> */
    public static final Node sourceShape = createProperty( "http://www.w3.org/ns/shacl#sourceShape" );

    /** <p>Links a shape with SPARQL constraints.</p> */
    public static final Node sparql = createProperty( "http://www.w3.org/ns/shacl#sparql" );

    /** <p>An expression producing the resources that shall be inferred as subjects.</p> */
    public static final Node subject = createProperty( "http://www.w3.org/ns/shacl#subject" );

    /** <p>Suggested shapes graphs for this ontology. The values of this property may
     *  be used in the absence of specific sh:shapesGraph statements.</p>
     */
    public static final Node suggestedShapesGraph = createProperty( "http://www.w3.org/ns/shacl#suggestedShapesGraph" );

    /** <p>Links a shape to a target specified by an extension language, for example
     *  instances of sh:SPARQLTarget.</p>
     */
    public static final Node target = createProperty( "http://www.w3.org/ns/shacl#target" );

    /** <p>Links a shape to a class, indicating that all instances of the class must
     *  conform to the shape.</p>
     */
    public static final Node targetClass = createProperty( "http://www.w3.org/ns/shacl#targetClass" );

    /** <p>Links a shape to individual nodes, indicating that these nodes must conform
     *  to the shape.</p>
     */
    public static final Node targetNode = createProperty( "http://www.w3.org/ns/shacl#targetNode" );

    /** <p>Links a shape to a property, indicating that all all objects of triples that
     *  have the given property as their predicate must conform to the shape.</p>
     */
    public static final Node targetObjectsOf = createProperty( "http://www.w3.org/ns/shacl#targetObjectsOf" );

    /** <p>Links a shape to a property, indicating that all subjects of triples that
     *  have the given property as their predicate must conform to the shape.</p>
     */
    public static final Node targetSubjectsOf = createProperty( "http://www.w3.org/ns/shacl#targetSubjectsOf" );

    /** <p>A list of node expressions that shall be used together.</p> */
    public static final Node union = createProperty( "http://www.w3.org/ns/shacl#union" );

    /** <p>Specifies whether all node values must have a unique (or no) language tag.</p> */
    public static final Node uniqueLang = createProperty( "http://www.w3.org/ns/shacl#uniqueLang" );

    /** <p>The SPARQL UPDATE to execute.</p> */
    public static final Node update = createProperty( "http://www.w3.org/ns/shacl#update" );

    /** <p>The validator(s) used to evaluate constraints of either node or property shapes.</p> */
    public static final Node validator = createProperty( "http://www.w3.org/ns/shacl#validator" );

    /** <p>An RDF node that has caused the result.</p> */
    public static final Node value = createProperty( "http://www.w3.org/ns/shacl#value" );

    /** <p>Specifies a list of shapes so that the value nodes must conform to exactly
     *  one of the shapes.</p>
     */
    public static final Node xone = createProperty( "http://www.w3.org/ns/shacl#xone" );

    /** <p>The (single) value of this property represents a path that is matched zero
     *  or more times.</p>
     */
    public static final Node zeroOrMorePath = createProperty( "http://www.w3.org/ns/shacl#zeroOrMorePath" );

    /** <p>The (single) value of this property represents a path that is matched zero
     *  or one times.</p>
     */
    public static final Node zeroOrOnePath = createProperty( "http://www.w3.org/ns/shacl#zeroOrOnePath" );

    /** <p>The base class of validation results, typically not instantiated directly.</p> */
    public static final Node AbstractResult = createResource( "http://www.w3.org/ns/shacl#AbstractResult" );

    /** <p>A constraint component that can be used to test whether a value node conforms
     *  to all members of a provided list of shapes.</p>
     */
    public static final Node AndConstraintComponent = createResource( "http://www.w3.org/ns/shacl#AndConstraintComponent" );

    public static final Node AndConstraintComponent_and = createResource( "http://www.w3.org/ns/shacl#AndConstraintComponent-and" );

    /** <p>The node kind of all blank nodes.</p> */
    public static final Node BlankNode = createResource( "http://www.w3.org/ns/shacl#BlankNode" );

    /** <p>The node kind of all blank nodes or IRIs.</p> */
    public static final Node BlankNodeOrIRI = createResource( "http://www.w3.org/ns/shacl#BlankNodeOrIRI" );

    /** <p>The node kind of all blank nodes or literals.</p> */
    public static final Node BlankNodeOrLiteral = createResource( "http://www.w3.org/ns/shacl#BlankNodeOrLiteral" );

    /** <p>A constraint component that can be used to verify that each value node is
     *  an instance of a given type.</p>
     */
    public static final Node ClassConstraintComponent = createResource( "http://www.w3.org/ns/shacl#ClassConstraintComponent" );

    public static final Node ClassConstraintComponent_class = createResource( "http://www.w3.org/ns/shacl#ClassConstraintComponent-class" );

    /** <p>A constraint component that can be used to indicate that focus nodes must
     *  only have values for those properties that have been explicitly enumerated
     *  via sh:property/sh:path.</p>
     */
    public static final Node ClosedConstraintComponent = createResource( "http://www.w3.org/ns/shacl#ClosedConstraintComponent" );

    public static final Node ClosedConstraintComponent_closed = createResource( "http://www.w3.org/ns/shacl#ClosedConstraintComponent-closed" );

    public static final Node ClosedConstraintComponent_ignoredProperties = createResource( "http://www.w3.org/ns/shacl#ClosedConstraintComponent-ignoredProperties" );

    /** <p>The class of constraint components.</p> */
    public static final Node ConstraintComponent = createResource( "http://www.w3.org/ns/shacl#ConstraintComponent" );

    /** <p>A constraint component that can be used to restrict the datatype of all value
     *  nodes.</p>
     */
    public static final Node DatatypeConstraintComponent = createResource( "http://www.w3.org/ns/shacl#DatatypeConstraintComponent" );

    public static final Node DatatypeConstraintComponent_datatype = createResource( "http://www.w3.org/ns/shacl#DatatypeConstraintComponent-datatype" );

    /** <p>A constraint component that can be used to verify that the set of value nodes
     *  is disjoint with the the set of nodes that have the focus node as subject
     *  and the value of a given property as predicate.</p>
     */
    public static final Node DisjointConstraintComponent = createResource( "http://www.w3.org/ns/shacl#DisjointConstraintComponent" );

    public static final Node DisjointConstraintComponent_disjoint = createResource( "http://www.w3.org/ns/shacl#DisjointConstraintComponent-disjoint" );

    /** <p>A constraint component that can be used to verify that the set of value nodes
     *  is equal to the set of nodes that have the focus node as subject and the value
     *  of a given property as predicate.</p>
     */
    public static final Node EqualsConstraintComponent = createResource( "http://www.w3.org/ns/shacl#EqualsConstraintComponent" );

    public static final Node EqualsConstraintComponent_equals = createResource( "http://www.w3.org/ns/shacl#EqualsConstraintComponent-equals" );

    /** <p>A constraint component that can be used to verify that a given node expression
     *  produces true for all value nodes.</p>
     */
    public static final Node ExpressionConstraintComponent = createResource( "http://www.w3.org/ns/shacl#ExpressionConstraintComponent" );

    public static final Node ExpressionConstraintComponent_expression = createResource( "http://www.w3.org/ns/shacl#ExpressionConstraintComponent-expression" );

    /** <p>The class of SHACL functions.</p> */
    public static final Node Function = createResource( "http://www.w3.org/ns/shacl#Function" );

    /** <p>A constraint component that can be used to verify that one of the value nodes
     *  is a given RDF node.</p>
     */
    public static final Node HasValueConstraintComponent = createResource( "http://www.w3.org/ns/shacl#HasValueConstraintComponent" );

    public static final Node HasValueConstraintComponent_hasValue = createResource( "http://www.w3.org/ns/shacl#HasValueConstraintComponent-hasValue" );

    /** <p>The node kind of all IRIs.</p> */
    public static final Node IRI = createResource( "http://www.w3.org/ns/shacl#IRI" );

    /** <p>The node kind of all IRIs or literals.</p> */
    public static final Node IRIOrLiteral = createResource( "http://www.w3.org/ns/shacl#IRIOrLiteral" );

    /** <p>A constraint component that can be used to exclusively enumerate the permitted
     *  value nodes.</p>
     */
    public static final Node InConstraintComponent = createResource( "http://www.w3.org/ns/shacl#InConstraintComponent" );

    public static final Node InConstraintComponent_in = createResource( "http://www.w3.org/ns/shacl#InConstraintComponent-in" );

    /** <p>The severity for an informational validation result.</p> */
    public static final Node Info = createResource( "http://www.w3.org/ns/shacl#Info" );

    /** <p>The class of constraints backed by a JavaScript function.</p> */
    public static final Node JSConstraint = createResource( "http://www.w3.org/ns/shacl#JSConstraint" );

    public static final Node JSConstraint_js = createResource( "http://www.w3.org/ns/shacl#JSConstraint-js" );

    /** <p>A constraint component with the parameter sh:js linking to a sh:JSConstraint
     *  containing a sh:script.</p>
     */
    public static final Node JSConstraintComponent = createResource( "http://www.w3.org/ns/shacl#JSConstraintComponent" );

    /** <p>Abstract base class of resources that declare an executable JavaScript.</p> */
    public static final Node JSExecutable = createResource( "http://www.w3.org/ns/shacl#JSExecutable" );

    /** <p>The class of SHACL functions that execute a JavaScript function when called.</p> */
    public static final Node JSFunction = createResource( "http://www.w3.org/ns/shacl#JSFunction" );

    /** <p>Represents a JavaScript library, typically identified by one or more URLs
     *  of files to include.</p>
     */
    public static final Node JSLibrary = createResource( "http://www.w3.org/ns/shacl#JSLibrary" );

    /** <p>The class of SHACL rules expressed using JavaScript.</p> */
    public static final Node JSRule = createResource( "http://www.w3.org/ns/shacl#JSRule" );

    /** <p>The class of targets that are based on JavaScript functions.</p> */
    public static final Node JSTarget = createResource( "http://www.w3.org/ns/shacl#JSTarget" );

    /** <p>The (meta) class for parameterizable targets that are based on JavaScript
     *  functions.</p>
     */
    public static final Node JSTargetType = createResource( "http://www.w3.org/ns/shacl#JSTargetType" );

    /** <p>A SHACL validator based on JavaScript. This can be used to declare SHACL constraint
     *  components that perform JavaScript-based validation when used.</p>
     */
    public static final Node JSValidator = createResource( "http://www.w3.org/ns/shacl#JSValidator" );

    /** <p>A constraint component that can be used to enumerate language tags that all
     *  value nodes must have.</p>
     */
    public static final Node LanguageInConstraintComponent = createResource( "http://www.w3.org/ns/shacl#LanguageInConstraintComponent" );

    public static final Node LanguageInConstraintComponent_languageIn = createResource( "http://www.w3.org/ns/shacl#LanguageInConstraintComponent-languageIn" );

    /** <p>A constraint component that can be used to verify that each value node is
     *  smaller than all the nodes that have the focus node as subject and the value
     *  of a given property as predicate.</p>
     */
    public static final Node LessThanConstraintComponent = createResource( "http://www.w3.org/ns/shacl#LessThanConstraintComponent" );

    public static final Node LessThanConstraintComponent_lessThan = createResource( "http://www.w3.org/ns/shacl#LessThanConstraintComponent-lessThan" );

    /** <p>A constraint component that can be used to verify that every value node is
     *  smaller than all the nodes that have the focus node as subject and the value
     *  of a given property as predicate.</p>
     */
    public static final Node LessThanOrEqualsConstraintComponent = createResource( "http://www.w3.org/ns/shacl#LessThanOrEqualsConstraintComponent" );

    public static final Node LessThanOrEqualsConstraintComponent_lessThanOrEquals = createResource( "http://www.w3.org/ns/shacl#LessThanOrEqualsConstraintComponent-lessThanOrEquals" );

    /** <p>The node kind of all literals.</p> */
    public static final Node Literal = createResource( "http://www.w3.org/ns/shacl#Literal" );

    /** <p>A constraint component that can be used to restrict the maximum number of
     *  value nodes.</p>
     */
    public static final Node MaxCountConstraintComponent = createResource( "http://www.w3.org/ns/shacl#MaxCountConstraintComponent" );

    public static final Node MaxCountConstraintComponent_maxCount = createResource( "http://www.w3.org/ns/shacl#MaxCountConstraintComponent-maxCount" );

    /** <p>A constraint component that can be used to restrict the range of value nodes
     *  with a maximum exclusive value.</p>
     */
    public static final Node MaxExclusiveConstraintComponent = createResource( "http://www.w3.org/ns/shacl#MaxExclusiveConstraintComponent" );

    public static final Node MaxExclusiveConstraintComponent_maxExclusive = createResource( "http://www.w3.org/ns/shacl#MaxExclusiveConstraintComponent-maxExclusive" );

    /** <p>A constraint component that can be used to restrict the range of value nodes
     *  with a maximum inclusive value.</p>
     */
    public static final Node MaxInclusiveConstraintComponent = createResource( "http://www.w3.org/ns/shacl#MaxInclusiveConstraintComponent" );

    public static final Node MaxInclusiveConstraintComponent_maxInclusive = createResource( "http://www.w3.org/ns/shacl#MaxInclusiveConstraintComponent-maxInclusive" );

    /** <p>A constraint component that can be used to restrict the maximum string length
     *  of value nodes.</p>
     */
    public static final Node MaxLengthConstraintComponent = createResource( "http://www.w3.org/ns/shacl#MaxLengthConstraintComponent" );

    public static final Node MaxLengthConstraintComponent_maxLength = createResource( "http://www.w3.org/ns/shacl#MaxLengthConstraintComponent-maxLength" );

    /** <p>A constraint component that can be used to restrict the minimum number of
     *  value nodes.</p>
     */
    public static final Node MinCountConstraintComponent = createResource( "http://www.w3.org/ns/shacl#MinCountConstraintComponent" );

    public static final Node MinCountConstraintComponent_minCount = createResource( "http://www.w3.org/ns/shacl#MinCountConstraintComponent-minCount" );

    /** <p>A constraint component that can be used to restrict the range of value nodes
     *  with a minimum exclusive value.</p>
     */
    public static final Node MinExclusiveConstraintComponent = createResource( "http://www.w3.org/ns/shacl#MinExclusiveConstraintComponent" );

    public static final Node MinExclusiveConstraintComponent_minExclusive = createResource( "http://www.w3.org/ns/shacl#MinExclusiveConstraintComponent-minExclusive" );

    /** <p>A constraint component that can be used to restrict the range of value nodes
     *  with a minimum inclusive value.</p>
     */
    public static final Node MinInclusiveConstraintComponent = createResource( "http://www.w3.org/ns/shacl#MinInclusiveConstraintComponent" );

    public static final Node MinInclusiveConstraintComponent_minInclusive = createResource( "http://www.w3.org/ns/shacl#MinInclusiveConstraintComponent-minInclusive" );

    /** <p>A constraint component that can be used to restrict the minimum string length
     *  of value nodes.</p>
     */
    public static final Node MinLengthConstraintComponent = createResource( "http://www.w3.org/ns/shacl#MinLengthConstraintComponent" );

    public static final Node MinLengthConstraintComponent_minLength = createResource( "http://www.w3.org/ns/shacl#MinLengthConstraintComponent-minLength" );

    /** <p>A constraint component that can be used to verify that all value nodes conform
     *  to the given node shape.</p>
     */
    public static final Node NodeConstraintComponent = createResource( "http://www.w3.org/ns/shacl#NodeConstraintComponent" );

    public static final Node NodeConstraintComponent_node = createResource( "http://www.w3.org/ns/shacl#NodeConstraintComponent-node" );

    /** <p>The class of all node kinds, including sh:BlankNode, sh:IRI, sh:Literal or
     *  the combinations of these: sh:BlankNodeOrIRI, sh:BlankNodeOrLiteral, sh:IRIOrLiteral.</p>
     */
    public static final Node NodeKind = createResource( "http://www.w3.org/ns/shacl#NodeKind" );

    /** <p>A constraint component that can be used to restrict the RDF node kind of each
     *  value node.</p>
     */
    public static final Node NodeKindConstraintComponent = createResource( "http://www.w3.org/ns/shacl#NodeKindConstraintComponent" );

    public static final Node NodeKindConstraintComponent_nodeKind = createResource( "http://www.w3.org/ns/shacl#NodeKindConstraintComponent-nodeKind" );

    /** <p>A node shape is a shape that specifies constraint that need to be met with
     *  respect to focus nodes.</p>
     */
    public static final Node NodeShape = createResource( "http://www.w3.org/ns/shacl#NodeShape" );

    /** <p>A constraint component that can be used to verify that value nodes do not
     *  conform to a given shape.</p>
     */
    public static final Node NotConstraintComponent = createResource( "http://www.w3.org/ns/shacl#NotConstraintComponent" );

    public static final Node NotConstraintComponent_not = createResource( "http://www.w3.org/ns/shacl#NotConstraintComponent-not" );

    /** <p>A constraint component that can be used to restrict the value nodes so that
     *  they conform to at least one out of several provided shapes.</p>
     */
    public static final Node OrConstraintComponent = createResource( "http://www.w3.org/ns/shacl#OrConstraintComponent" );

    public static final Node OrConstraintComponent_or = createResource( "http://www.w3.org/ns/shacl#OrConstraintComponent-or" );

    /** <p>The class of parameter declarations, consisting of a path predicate and (possibly)
     *  information about allowed value type, cardinality and other characteristics.</p>
     */
    public static final Node Parameter = createResource( "http://www.w3.org/ns/shacl#Parameter" );

    /** <p>Superclass of components that can take parameters, especially functions and
     *  constraint components.</p>
     */
    public static final Node Parameterizable = createResource( "http://www.w3.org/ns/shacl#Parameterizable" );

    /** <p>A constraint component that can be used to verify that every value node matches
     *  a given regular expression.</p>
     */
    public static final Node PatternConstraintComponent = createResource( "http://www.w3.org/ns/shacl#PatternConstraintComponent" );

    public static final Node PatternConstraintComponent_flags = createResource( "http://www.w3.org/ns/shacl#PatternConstraintComponent-flags" );

    public static final Node PatternConstraintComponent_pattern = createResource( "http://www.w3.org/ns/shacl#PatternConstraintComponent-pattern" );

    /** <p>The class of prefix declarations, consisting of pairs of a prefix with a namespace.</p> */
    public static final Node PrefixDeclaration = createResource( "http://www.w3.org/ns/shacl#PrefixDeclaration" );

    /** <p>A constraint component that can be used to verify that all value nodes conform
     *  to the given property shape.</p>
     */
    public static final Node PropertyConstraintComponent = createResource( "http://www.w3.org/ns/shacl#PropertyConstraintComponent" );

    public static final Node PropertyConstraintComponent_property = createResource( "http://www.w3.org/ns/shacl#PropertyConstraintComponent-property" );

    /** <p>Instances of this class represent groups of property shapes that belong together.</p> */
    public static final Node PropertyGroup = createResource( "http://www.w3.org/ns/shacl#PropertyGroup" );

    /** <p>A property shape is a shape that specifies constraints on the values of a
     *  focus node for a given property or path.</p>
     */
    public static final Node PropertyShape = createResource( "http://www.w3.org/ns/shacl#PropertyShape" );

    /** <p>A constraint component that can be used to verify that a specified maximum
     *  number of value nodes conforms to a given shape.</p>
     */
    public static final Node QualifiedMaxCountConstraintComponent = createResource( "http://www.w3.org/ns/shacl#QualifiedMaxCountConstraintComponent" );

    public static final Node QualifiedMaxCountConstraintComponent_qualifiedMaxCount = createResource( "http://www.w3.org/ns/shacl#QualifiedMaxCountConstraintComponent-qualifiedMaxCount" );

    public static final Node QualifiedMaxCountConstraintComponent_qualifiedValueShape = createResource( "http://www.w3.org/ns/shacl#QualifiedMaxCountConstraintComponent-qualifiedValueShape" );

    public static final Node QualifiedMaxCountConstraintComponent_qualifiedValueShapesDisjoint = createResource( "http://www.w3.org/ns/shacl#QualifiedMaxCountConstraintComponent-qualifiedValueShapesDisjoint" );

    /** <p>A constraint component that can be used to verify that a specified minimum
     *  number of value nodes conforms to a given shape.</p>
     */
    public static final Node QualifiedMinCountConstraintComponent = createResource( "http://www.w3.org/ns/shacl#QualifiedMinCountConstraintComponent" );

    public static final Node QualifiedMinCountConstraintComponent_qualifiedMinCount = createResource( "http://www.w3.org/ns/shacl#QualifiedMinCountConstraintComponent-qualifiedMinCount" );

    public static final Node QualifiedMinCountConstraintComponent_qualifiedValueShape = createResource( "http://www.w3.org/ns/shacl#QualifiedMinCountConstraintComponent-qualifiedValueShape" );

    public static final Node QualifiedMinCountConstraintComponent_qualifiedValueShapesDisjoint = createResource( "http://www.w3.org/ns/shacl#QualifiedMinCountConstraintComponent-qualifiedValueShapesDisjoint" );

    /** <p>A class of result annotations, which define the rules to derive the values
     *  of a given annotation property as extra values for a validation result.</p>
     */
    public static final Node ResultAnnotation = createResource( "http://www.w3.org/ns/shacl#ResultAnnotation" );

    /** <p>The class of SHACL rules. Never instantiated directly.</p> */
    public static final Node Rule = createResource( "http://www.w3.org/ns/shacl#Rule" );

    /** <p>The class of SPARQL executables that are based on an ASK query.</p> */
    public static final Node SPARQLAskExecutable = createResource( "http://www.w3.org/ns/shacl#SPARQLAskExecutable" );

    /** <p>The class of validators based on SPARQL ASK queries. The queries are evaluated
     *  for each value node and are supposed to return true if the given node conforms.</p>
     */
    public static final Node SPARQLAskValidator = createResource( "http://www.w3.org/ns/shacl#SPARQLAskValidator" );

    /** <p>The class of constraints based on SPARQL SELECT queries.</p> */
    public static final Node SPARQLConstraint = createResource( "http://www.w3.org/ns/shacl#SPARQLConstraint" );

    /** <p>A constraint component that can be used to define constraints based on SPARQL
     *  queries.</p>
     */
    public static final Node SPARQLConstraintComponent = createResource( "http://www.w3.org/ns/shacl#SPARQLConstraintComponent" );

    public static final Node SPARQLConstraintComponent_sparql = createResource( "http://www.w3.org/ns/shacl#SPARQLConstraintComponent-sparql" );

    /** <p>The class of SPARQL executables that are based on a CONSTRUCT query.</p> */
    public static final Node SPARQLConstructExecutable = createResource( "http://www.w3.org/ns/shacl#SPARQLConstructExecutable" );

    /** <p>The class of resources that encapsulate a SPARQL query.</p> */
    public static final Node SPARQLExecutable = createResource( "http://www.w3.org/ns/shacl#SPARQLExecutable" );

    /** <p>A function backed by a SPARQL query - either ASK or SELECT.</p> */
    public static final Node SPARQLFunction = createResource( "http://www.w3.org/ns/shacl#SPARQLFunction" );

    /** <p>The class of SHACL rules based on SPARQL CONSTRUCT queries.</p> */
    public static final Node SPARQLRule = createResource( "http://www.w3.org/ns/shacl#SPARQLRule" );

    /** <p>The class of SPARQL executables based on a SELECT query.</p> */
    public static final Node SPARQLSelectExecutable = createResource( "http://www.w3.org/ns/shacl#SPARQLSelectExecutable" );

    /** <p>The class of validators based on SPARQL SELECT queries. The queries are evaluated
     *  for each focus node and are supposed to produce bindings for all focus nodes
     *  that do not conform.</p>
     */
    public static final Node SPARQLSelectValidator = createResource( "http://www.w3.org/ns/shacl#SPARQLSelectValidator" );

    /** <p>The class of targets that are based on SPARQL queries.</p> */
    public static final Node SPARQLTarget = createResource( "http://www.w3.org/ns/shacl#SPARQLTarget" );

    /** <p>The (meta) class for parameterizable targets that are based on SPARQL queries.</p> */
    public static final Node SPARQLTargetType = createResource( "http://www.w3.org/ns/shacl#SPARQLTargetType" );

    /** <p>The class of SPARQL executables based on a SPARQL UPDATE.</p> */
    public static final Node SPARQLUpdateExecutable = createResource( "http://www.w3.org/ns/shacl#SPARQLUpdateExecutable" );

    /** <p>The class of validation result severity levels, including violation and warning
     *  levels.</p>
     */
    public static final Node Severity = createResource( "http://www.w3.org/ns/shacl#Severity" );

    /** <p>A shape is a collection of constraints that may be targeted for certain nodes.</p> */
    public static final Node Shape = createResource( "http://www.w3.org/ns/shacl#Shape" );

    /** <p>The base class of targets such as those based on SPARQL queries.</p> */
    public static final Node Target = createResource( "http://www.w3.org/ns/shacl#Target" );

    /** <p>The (meta) class for parameterizable targets. Instances of this are instantiated
     *  as values of the sh:target property.</p>
     */
    public static final Node TargetType = createResource( "http://www.w3.org/ns/shacl#TargetType" );

    public static final Node TripleRule = createResource( "http://www.w3.org/ns/shacl#TripleRule" );

    /** <p>A constraint component that can be used to specify that no pair of value nodes
     *  may use the same language tag.</p>
     */
    public static final Node UniqueLangConstraintComponent = createResource( "http://www.w3.org/ns/shacl#UniqueLangConstraintComponent" );

    public static final Node UniqueLangConstraintComponent_uniqueLang = createResource( "http://www.w3.org/ns/shacl#UniqueLangConstraintComponent-uniqueLang" );

    /** <p>The class of SHACL validation reports.</p> */
    public static final Node ValidationReport = createResource( "http://www.w3.org/ns/shacl#ValidationReport" );

    /** <p>The class of validation results.</p> */
    public static final Node ValidationResult = createResource( "http://www.w3.org/ns/shacl#ValidationResult" );

    /** <p>The class of validators, which provide instructions on how to process a constraint
     *  definition. This class serves as base class for the SPARQL-based validators
     *  and other possible implementations.</p>
     */
    public static final Node Validator = createResource( "http://www.w3.org/ns/shacl#Validator" );

    /** <p>The severity for a violation validation result.</p> */
    public static final Node Violation = createResource( "http://www.w3.org/ns/shacl#Violation" );

    /** <p>The severity for a warning validation result.</p> */
    public static final Node Warning = createResource( "http://www.w3.org/ns/shacl#Warning" );

    /** <p>A constraint component that can be used to restrict the value nodes so that
     *  they conform to exactly one out of several provided shapes.</p>
     */
    public static final Node XoneConstraintComponent = createResource( "http://www.w3.org/ns/shacl#XoneConstraintComponent" );

    public static final Node XoneConstraintComponent_xone = createResource( "http://www.w3.org/ns/shacl#XoneConstraintComponent-xone" );
}
