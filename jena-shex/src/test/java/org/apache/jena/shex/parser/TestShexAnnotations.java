/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.shex.parser;

import java.io.StringReader;
import java.util.List;

import junit.framework.TestCase;

import org.apache.jena.shex.ShexShape;
import org.apache.jena.shex.ShexValidator;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.shex.ShexReport;
import org.apache.jena.shex.ShexSchema;
import org.apache.jena.shex.expressions.*;
import org.apache.jena.sparql.graph.GraphFactory;

public class TestShexAnnotations extends TestCase {

    private static final String BASE = "http://example.org/schema";

    public void testShapeGroupAndConstraintAnnotations() {
        String SCHEMA_SHAPEGROUP = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:PersonShape {
            (
                ex:name xsd:string
                    // rdfs:comment "The person's name" ;
                ex:age xsd:integer
                    // rdfs:comment "The person's age"
            )
            // rdfs:comment "Person properties"
        }
            // rdfs:comment "Describes a person"
            // rdfs:label "Person"
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_SHAPEGROUP), BASE);
        assertEquals(1, schema.getShapes().size());
        ShexShape shexShape = schema.getShapes().get(0);

        // Shape-level annotations
        ShapeExprTripleExpr shapeExpr = (ShapeExprTripleExpr) shexShape.getShapeExpression();
        assertEquals(2, shapeExpr.getAnnotations().size());
        assertEquals(
            "Describes a person",
            shapeExpr.getAnnotations().get(0).getValue().getLiteralLexicalForm()
        );
        assertEquals(
            "Person",
            shapeExpr.getAnnotations().get(1).getValue().getLiteralLexicalForm()
        );

        // Bracketed expression annotation
        TripleExpression tripleExpr = shapeExpr.getTripleExpr();
        assertTrue(tripleExpr instanceof TripleExprEachOf);
        TripleExprEachOf eachOf = (TripleExprEachOf) tripleExpr;
        assertEquals(1, eachOf.getAnnotations().size());
        assertEquals(
            "Person properties",
            eachOf.getAnnotations().get(0).getValue().getLiteralLexicalForm()
        );

        // Individual triple constraints
        assertEquals(2, eachOf.expressions().size());
        TripleConstraint nameConstraint = (TripleConstraint) eachOf.expressions().get(0);
        TripleConstraint ageConstraint = (TripleConstraint) eachOf.expressions().get(1);
        assertEquals("http://example.org/name",
                     nameConstraint.getPredicate().getURI());
        assertEquals(1, nameConstraint.getAnnotations().size());
        assertEquals(
            "The person's name",
            nameConstraint.getAnnotations().get(0).getValue().getLiteralLexicalForm()
        );
        assertEquals("http://example.org/age",
                     ageConstraint.getPredicate().getURI());
        assertEquals(1, ageConstraint.getAnnotations().size());
        assertEquals(
            "The person's age",
            ageConstraint.getAnnotations().get(0).getValue().getLiteralLexicalForm()
        );
    }

    public void testOneOfAnnotations() {
        String SCHEMA_ONEOF = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:ContactShape {
            (
                ex:email xsd:string
                    // rdfs:comment "Email address" |
                ex:phone xsd:string
                    // rdfs:comment "Phone number"
            )
            // rdfs:comment "At least one contact method"
        }
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_ONEOF), BASE);
        ShexShape shexShape = schema.getShapes().get(0);
        ShapeExprTripleExpr shapeExpr = (ShapeExprTripleExpr) shexShape.getShapeExpression();
        TripleExpression tripleExpr = shapeExpr.getTripleExpr();

        // Verify OneOf
        assertTrue(tripleExpr instanceof TripleExprOneOf);
        TripleExprOneOf oneOf = (TripleExprOneOf) tripleExpr;

        // Annotation on the OneOf group
        assertEquals(1, oneOf.getAnnotations().size());
        assertEquals(
            "At least one contact method",
            oneOf.getAnnotations().get(0).getValue().getLiteralLexicalForm()
        );

        // Verify the individual alternatives
        assertEquals(2, oneOf.expressions().size());
        TripleConstraint email = (TripleConstraint) oneOf.expressions().get(0);
        TripleConstraint phone = (TripleConstraint) oneOf.expressions().get(1);
        assertEquals(
            "http://example.org/email",
            email.getPredicate().getURI());
        assertEquals(
            "http://example.org/phone",
            phone.getPredicate().getURI());
        assertEquals(1, email.getAnnotations().size());
        assertEquals(1, phone.getAnnotations().size());
        assertEquals(
            "Email address",
            email.getAnnotations().get(0).getValue().getLiteralLexicalForm()
        );
        assertEquals(
            "Phone number",
            phone.getAnnotations().get(0).getValue().getLiteralLexicalForm()
        );
    }

    public void testBracketedExpressionCardinalityAnnotations() {
        String SCHEMA_BRACKETCARDINALITY = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:TagsShape {
            (
                ex:tag xsd:string ;
                ex:category xsd:string
            )+
            // rdfs:comment "One or more tag/category pairs"
        }
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_BRACKETCARDINALITY), BASE);
        ShexShape shexShape = schema.getShapes().get(0);
        ShapeExprTripleExpr shapeExpr = (ShapeExprTripleExpr) shexShape.getShapeExpression();
        TripleExpression tripleExpr = shapeExpr.getTripleExpr();

        // Verify cardinality wrapper
        assertTrue(tripleExpr instanceof TripleExprCardinality);
        TripleExprCardinality cardinality = (TripleExprCardinality) tripleExpr;

        // Annotation belongs to the wrapper
        assertEquals(1, cardinality.getAnnotations().size());
        assertEquals(
            "One or more tag/category pairs",
            cardinality.getAnnotations().get(0).getValue().getLiteralLexicalForm()
        );

        // Verify the wrapped expression
        assertTrue(cardinality.target() instanceof TripleExprEachOf);
        TripleExprEachOf eachOf = (TripleExprEachOf) cardinality.target();
        assertTrue(eachOf.getAnnotations().isEmpty());
    }

    public void testAnnotationValueTypes() {
        String SCHEMA_VALUETYPES = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:PersonShape {
            ex:name xsd:string
                // rdfs:comment "Person name"@en
                // rdfs:seeAlso ex:NameDocumentation
                // ex:priority "7"^^xsd:integer
        }
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_VALUETYPES), BASE);
        ShexShape shexShape = schema.getShapes().get(0);
        ShapeExprTripleExpr shapeExpr = (ShapeExprTripleExpr) shexShape.getShapeExpression();
        TripleConstraint constraint = (TripleConstraint) shapeExpr.getTripleExpr();

        // Number of annotations
        List<ShexAnnotation> annotations = constraint.getAnnotations();
        assertEquals(3, annotations.size());

        // Language-tagged literal
        Node comment = annotations.get(0).getValue();
        assertTrue(comment.isLiteral());
        assertEquals("Person name", comment.getLiteralLexicalForm());
        assertEquals("en", comment.getLiteralLanguage());

        // IRI-valued annotation
        Node seeAlso = annotations.get(1).getValue();
        assertTrue(seeAlso.isURI());
        assertEquals("http://example.org/NameDocumentation", seeAlso.getURI());

        // Typed literal
        Node priority = annotations.get(2).getValue();
        assertTrue(priority.isLiteral());
        assertEquals("7", priority.getLiteralLexicalForm());
        assertEquals(
            "http://www.w3.org/2001/XMLSchema#integer",
            priority.getLiteralDatatypeURI()
        );
    }

    public void testSingleChildBracketedExpressionAnnotations() {
        String SCHEMA_CHILDBRACKET = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:PersonShape {
            (
                ex:name xsd:string
                    // rdfs:comment "Constraint annotation"
            )
            // rdfs:comment "Bracket annotation"
        }
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_CHILDBRACKET), BASE);
        ShexShape shexShape = schema.getShapes().get(0);
        ShapeExprTripleExpr shapeExpr = (ShapeExprTripleExpr) shexShape.getShapeExpression();
        TripleConstraint constraint = (TripleConstraint) shapeExpr.getTripleExpr();

        // Both annotations must survive the expression collapse.
        assertEquals(2, constraint.getAnnotations().size());
        assertEquals(
            "Constraint annotation",
            constraint.getAnnotations().get(0).getValue().getLiteralLexicalForm()
        );
        assertEquals(
            "Bracket annotation",
            constraint.getAnnotations().get(1).getValue().getLiteralLexicalForm()
        );
    }

    public void testEmptyShapeWithAnnotation() {
        String SCHEMA_EMPTYSHAPE = """
        PREFIX ex: <http://example.org/>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:EmptyShape {
        }
            // rdfs:comment "An intentionally empty shape"
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_EMPTYSHAPE), BASE);
        ShexShape shexShape = schema.getShapes().get(0);
        ShapeExprTripleExpr shapeExpr = (ShapeExprTripleExpr) shexShape.getShapeExpression();

        assertEquals(1, shapeExpr.getAnnotations().size());
        assertEquals(
            "An intentionally empty shape",
            shapeExpr.getAnnotations().get(0).getValue().getLiteralLexicalForm()
        );

        // An empty shape must be valid for a node with no outgoing triples.
        Graph data = GraphFactory.createDefaultGraph();
        Node focus = NodeFactory.createURI("http://example.org/alice");
        Node shapeRef = NodeFactory.createURI("http://example.org/EmptyShape");
        ShexReport report = ShexValidator.get().validate(
            data, schema, shapeRef, focus
        );

        assertTrue(report.conforms());
        assertTrue(report.getEntries().isEmpty());
    }

    public void testOrdinaryCommentsAreIgnored() {
        String SCHEMA_COMMENT = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        # This is an ordinary comment.
        ex:PersonShape {
            ex:name xsd:string
            # This is another ordinary comment.
        }
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_COMMENT), BASE);
        ShexShape shexShape = schema.getShapes().get(0);
        ShapeExprTripleExpr shapeExpr = (ShapeExprTripleExpr) shexShape.getShapeExpression();
        TripleConstraint constraint = (TripleConstraint) shapeExpr.getTripleExpr();

        assertTrue(shapeExpr.getAnnotations().isEmpty());
        assertTrue(constraint.getAnnotations().isEmpty());
    }
}