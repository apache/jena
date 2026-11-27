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

package org.apache.jena.shex;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.shex.expressions.ShapeExprTripleExpr;
import org.apache.jena.shex.expressions.ShexAnnotated;
import org.apache.jena.shex.expressions.TripleConstraint;
import org.apache.jena.shex.expressions.TripleExprCardinality;
import org.apache.jena.shex.expressions.TripleExprEachOf;
import org.apache.jena.shex.expressions.TripleExprOneOf;
import org.apache.jena.shex.expressions.TripleExprRef;
import org.apache.jena.shex.parser.ShExC;
import org.apache.jena.shex.sys.ReportItem;
import org.apache.jena.shex.sys.ValidationContext;
import org.apache.jena.shex.sys.ValidationTrace;
import org.apache.jena.sparql.graph.GraphFactory;

/**
 * TestShexValidationAnnotation tests access to ShEx annotations through validation reports
 * and the correctness of validation-expression tracing.
 *
 * Test goals:
 *  - Annotation access and resolution
 *  - Validation trace construction
 *  - Failures without detailed report entries
 *  - Alternative and partition evaluation
 *  - Validation-context and report lifecycle
 *  - Immutability and missing provenance
 */
public class TestShexValidationAnnotation extends TestCase {

    private static final String BASE = "http://example.org/schema";

    public void testConstraintAnnotationsTakePrecedence() {
        ReportItem item = validateAgeFailure(
            "Constraint annotation",
            "Group annotation",
            "Shape annotation"
        );
        ValidationTrace trace = item.getValidationTrace();
        assertNotNull(trace);
        List<ShexAnnotated> path = trace.getExpressionPath();
        assertEquals(3, path.size());

        ShapeExprTripleExpr shape = (ShapeExprTripleExpr) path.get(0);
        TripleExprEachOf group = (TripleExprEachOf) path.get(1);
        TripleConstraint constraint = (TripleConstraint) path.get(2);

        // All three levels really have annotations.
        assertEquals(1, shape.getAnnotations().size());
        assertEquals(1, group.getAnnotations().size());
        assertEquals(1, constraint.getAnnotations().size());

        // The innermost annotation-bearing expression wins.
        assertSame(constraint, item.getAnnotationSource());
        assertEquals(
            List.of("Constraint annotation"),
            item.getAnnotations().stream()
                .map(a -> a.getValue().getLiteralLexicalForm())
                .toList()
        );
    }

    public void testGroupAnnotationsUsedWhenConstraintHasNone() {
        ReportItem item = validateAgeFailure(
            null,
            "Group annotation",
            "Shape annotation"
        );
        ValidationTrace trace = item.getValidationTrace();
        assertNotNull(trace);
        List<ShexAnnotated> path = trace.getExpressionPath();
        assertEquals(3, path.size());

        ShapeExprTripleExpr shape = (ShapeExprTripleExpr) path.get(0);
        TripleExprEachOf group = (TripleExprEachOf) path.get(1);
        TripleConstraint constraint = (TripleConstraint) path.get(2);

        assertTrue(constraint.getAnnotations().isEmpty());
        assertEquals(1, group.getAnnotations().size());
        assertEquals(1, shape.getAnnotations().size());
        assertSame(group, item.getAnnotationSource());
        assertEquals(
            List.of("Group annotation"),
            item.getAnnotations().stream()
                .map(a -> a.getValue().getLiteralLexicalForm())
                .toList()
        );
    }

    public void testShapeAnnotationsUsedWhenNestedExpressionsHaveNone() {
        ReportItem item = validateAgeFailure(
            null,
            null,
            "Shape annotation"
        );
        ValidationTrace trace = item.getValidationTrace();
        assertNotNull(trace);
        List<ShexAnnotated> path = trace.getExpressionPath();
        assertEquals(3, path.size());

        ShapeExprTripleExpr shape = (ShapeExprTripleExpr) path.get(0);
        TripleExprEachOf group = (TripleExprEachOf) path.get(1);
        TripleConstraint constraint = (TripleConstraint) path.get(2);

        assertTrue(constraint.getAnnotations().isEmpty());
        assertTrue(group.getAnnotations().isEmpty());
        assertEquals(1, shape.getAnnotations().size());
        assertSame(shape, item.getAnnotationSource());
        assertEquals(
            List.of("Shape annotation"),
            item.getAnnotations().stream()
                .map(a -> a.getValue().getLiteralLexicalForm())
                .toList()
        );
    }

    public void testNoAnnotationAvailableReturnsEmptyList() {
        ReportItem item = validateAgeFailure(null, null, null);

        assertNotNull(item.getValidationTrace());
        assertNull(item.getAnnotationSource());
        assertTrue(item.getAnnotations().isEmpty());
    }

    public void testValidationReportExposesAnnotationsViaValidationTrace() {
        String SCHEMA = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:PersonShape {
            ex:age xsd:integer
                // rdfs:comment "The person's age"
                // ex:validationMessage "Age must be an integer"
        }
        """;

        // Parse schema and validate empty graph
        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA), BASE);
        Graph data = GraphFactory.createDefaultGraph();
        Node shapeRef = NodeFactory.createURI("http://example.org/PersonShape");
        Node focus = NodeFactory.createURI("http://example.org/alice");
        Node predicate = NodeFactory.createURI("http://example.org/age");
        data.add(Triple.create(
            focus,predicate,NodeFactory.createLiteralString("thirty")
        ));
        ShexReport report = ShexValidator.get().validate(
            data, schema, shapeRef, focus
        );

        assertFalse(report.conforms());
        ReportItem datatypeFailure = report.getEntries().stream()
            .filter(item -> item.getMessage().contains("Wrong datatype"))
            .findFirst()
            .orElseThrow();
        ValidationTrace trace = datatypeFailure.getValidationTrace();
        assertNotNull(trace);
        List<ShexAnnotated> path = trace.getExpressionPath();
        assertEquals(2, path.size());
        assertTrue(path.get(0) instanceof ShapeExprTripleExpr);
        assertTrue(path.get(1) instanceof TripleConstraint);

        // Retrieve the annotations from the original AST expression.
        TripleConstraint constraint = (TripleConstraint) path.get(1);
        assertEquals(2, constraint.getAnnotations().size());
        assertEquals(
            "The person's age",
            constraint.getAnnotations().get(0)
                .getValue().getLiteralLexicalForm()
        );
        assertEquals(
            "Age must be an integer",
            constraint.getAnnotations().get(1)
                .getValue().getLiteralLexicalForm()
        );
    }

    public void testValidationTraceForClosedShapeFailure() {
        String SCHEMA_CLOSED = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:PersonShape CLOSED {
            ex:name xsd:string
        }
            // rdfs:comment "A person with no additional properties"
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_CLOSED), BASE);
        Graph data = GraphFactory.createDefaultGraph();
        Node focus = NodeFactory.createURI("http://example.org/alice");
        data.add(Triple.create(
            focus,
            NodeFactory.createURI("http://example.org/name"),
            NodeFactory.createLiteralString("Alice")
        ));
        // This additional property violates CLOSED.
        data.add(Triple.create(
            focus,
            NodeFactory.createURI("http://example.org/unknown"),
            NodeFactory.createLiteralString("unexpected")
        ));
        Node shapeRef = NodeFactory.createURI("http://example.org/PersonShape");
        ShexReport report = ShexValidator.get().validate(
            data, schema, shapeRef, focus
        );

        assertFalse(report.conforms());
        assertEquals(1, report.getEntries().size());

        ReportItem item = report.getEntries().iterator().next();
        assertEquals("Failed", item.getMessage());

        ValidationTrace trace = item.getValidationTrace();
        assertNotNull(trace);
        assertEquals(1, trace.getExpressionPath().size());
        assertTrue(trace.getExpressionPath().get(0) instanceof ShapeExprTripleExpr);

        ShapeExprTripleExpr failedShape =
            (ShapeExprTripleExpr) trace.getExpressionPath().get(0);
        assertEquals(1, failedShape.getAnnotations().size());
        assertEquals(
            "A person with no additional properties",
            failedShape.getAnnotations().get(0)
                .getValue().getLiteralLexicalForm()
        );
    }

    public void testOneOfFailedAlternativeThenSuccessfulAlternative() {
        String SCHEMA_ONEOF = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:ValueShape {
            ex:value xsd:integer
                // rdfs:comment "Integer alternative"
            |
            ex:value xsd:string
                // rdfs:comment "String alternative"
        }
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_ONEOF), BASE);
        Graph data = GraphFactory.createDefaultGraph();
        Node focus = NodeFactory.createURI("http://example.org/alice");
        data.add(Triple.create(
            focus,
            NodeFactory.createURI("http://example.org/value"),
            NodeFactory.createLiteralString("hello")
        ));
        Node shapeRef = NodeFactory.createURI("http://example.org/ValueShape");
        ShexReport report = ShexValidator.get().validate(
            data, schema, shapeRef, focus
        );

        assertTrue(report.conforms());
        // Failed attempts from the first alternative must not leak.
        assertTrue(report.getEntries().isEmpty());
    }

    public void testOneOfAllAlternativesFailWithCorrectTrace() {
        String SCHEMA_ONEOFFAIL = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:ValueShape {
            ex:value xsd:integer
                // rdfs:comment "Integer alternative"
            |
            ex:value xsd:boolean
                // rdfs:comment "Boolean alternative"
        }
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_ONEOFFAIL), BASE);
        Graph data = GraphFactory.createDefaultGraph();
        Node focus = NodeFactory.createURI("http://example.org/alice");
        data.add(Triple.create(
            focus,
            NodeFactory.createURI("http://example.org/value"),
            NodeFactory.createLiteralString("thirty")
        ));
        Node shapeRef = NodeFactory.createURI("http://example.org/ValueShape");
        ShexReport report = ShexValidator.get().validate(
            data, schema, shapeRef, focus
        );

        assertFalse(report.conforms());
        List<ReportItem> datatypeFailures = report.getEntries().stream()
            .filter(item -> item.getMessage().contains("Wrong datatype"))
            .toList();
        assertEquals(2, datatypeFailures.size());

        List<String> branchAnnotations = new ArrayList<>();
        for (ReportItem item : datatypeFailures) {
            ValidationTrace trace = item.getValidationTrace();
            assertNotNull(trace);
            List<ShexAnnotated> path = trace.getExpressionPath();
            assertEquals(3, path.size());
            assertTrue(path.get(0) instanceof ShapeExprTripleExpr);
            assertTrue(path.get(1) instanceof TripleExprOneOf);
            assertTrue(path.get(2) instanceof TripleConstraint);
            TripleConstraint constraint = (TripleConstraint) path.get(2);
            assertEquals(1, constraint.getAnnotations().size());
            branchAnnotations.add(
                constraint.getAnnotations().get(0)
                    .getValue().getLiteralLexicalForm()
            );
        }
        assertEquals(
            List.of("Integer alternative", "Boolean alternative"),
            branchAnnotations
        );
    }

    public void testSuccessfulOneOfDoesNotLeakFailedBranchReport() {
        String SCHEMA_ONEOFFAIL = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:PersonShape {
            (
                ex:value xsd:integer
                    // rdfs:comment "Integer alternative"
                |
                ex:value xsd:string
                    // rdfs:comment "String alternative"
            ) ;
            ex:active xsd:boolean
                // rdfs:comment "Active must be boolean"
        }
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_ONEOFFAIL), BASE);
        Graph data = GraphFactory.createDefaultGraph();
        Node focus = NodeFactory.createURI("http://example.org/alice");
        // The first OneOf alternative fails, the second succeeds.
        data.add(Triple.create(
            focus,
            NodeFactory.createURI("http://example.org/value"),
            NodeFactory.createLiteralString("hello")
        ));
        // This constraint fails after the OneOf succeeds.
        data.add(Triple.create(
            focus,
            NodeFactory.createURI("http://example.org/active"),
            NodeFactory.createLiteralString("not-a-boolean")
        ));
        Node shapeRef = NodeFactory.createURI("http://example.org/PersonShape");
        ShexReport report = ShexValidator.get().validate(
            data, schema, shapeRef, focus
        );

        assertFalse(report.conforms());
        // The failed OneOf alternative must not leave report entries.
        // The actual active constraint produces two diagnostics:
        // Wrong datatype + Minimum cardinality violation
        List<ReportItem> entries = new ArrayList<>(report.getEntries());
        assertEquals(2, entries.size());
        assertEquals(1, entries.stream()
            .filter(item -> item.getMessage().contains("Wrong datatype"))
            .count());
        assertEquals(1, entries.stream()
            .filter(item -> item.getMessage().contains("Cardinality violation"))
            .count());

        // Every remaining report entry must belong to ex:active.
        for (ReportItem item : entries) {
            ValidationTrace trace = item.getValidationTrace();
            assertNotNull(trace);
            List<ShexAnnotated> path = trace.getExpressionPath();
            assertEquals(3, path.size());
            assertTrue(path.get(0) instanceof ShapeExprTripleExpr);
            assertTrue(path.get(1) instanceof TripleExprEachOf);
            assertTrue(path.get(2) instanceof TripleConstraint);
            TripleConstraint constraint =(TripleConstraint) path.get(2);
            assertEquals(
                "http://example.org/active",
                constraint.getPredicate().getURI()
            );
            assertEquals(
                "Active must be boolean",
                constraint.getAnnotations().get(0)
                    .getValue().getLiteralLexicalForm()
            );
        }
    }

    public void testEachOfPartitionBacktracking() {
        String SCHEMA_EACHOF = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:PairShape {
            ex:value xsd:string
                // rdfs:comment "Text value" ;
            ex:value xsd:integer
                // rdfs:comment "Numeric value"
        }
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_EACHOF), BASE);
        Graph data = GraphFactory.createDefaultGraph();
        Node focus = NodeFactory.createURI("http://example.org/pair");
        Node predicate = NodeFactory.createURI("http://example.org/value");
        data.add(Triple.create(
            focus, predicate, NodeFactory.createLiteralString("hello")
        ));
        data.add(Triple.create(
            focus, predicate,
            NodeFactory.createLiteral("42", null, XSDDatatype.XSDinteger)
        ));
        Node shapeRef = NodeFactory.createURI("http://example.org/PairShape");
        ShexReport report = ShexValidator.get().validate(
            data, schema, shapeRef, focus
        );

        assertTrue(report.conforms());
        assertTrue(report.getEntries().isEmpty());
    }

    public void testNestedEachOfFailureTrace() {
        String SCHEMA_EACHOFNESTED = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:PersonShape {
            ex:name xsd:string ;
            (
                ex:age xsd:integer
                    // rdfs:comment "Age in years" ;
                ex:height xsd:integer
            )
            // rdfs:comment "Measurements"
        }
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_EACHOFNESTED), BASE);
        Graph data = GraphFactory.createDefaultGraph();
        Node focus = NodeFactory.createURI("http://example.org/alice");
        data.add(Triple.create(
            focus,
            NodeFactory.createURI("http://example.org/name"),
            NodeFactory.createLiteralString("Alice")
        ));
        data.add(Triple.create(
            focus,
            NodeFactory.createURI("http://example.org/age"),
            NodeFactory.createLiteralString("thirty")
        ));
        data.add(Triple.create(
            focus,
            NodeFactory.createURI("http://example.org/height"),
            NodeFactory.createLiteral("180", null, XSDDatatype.XSDinteger)
        ));
        Node shapeRef = NodeFactory.createURI("http://example.org/PersonShape");
        ShexReport report = ShexValidator.get().validate(
            data, schema, shapeRef, focus
        );

        assertFalse(report.conforms());
        ReportItem item = report.getEntries().stream()
            .filter(entry -> entry.getMessage().contains("Wrong datatype"))
            .findFirst()
            .orElseThrow();

        ValidationTrace trace = item.getValidationTrace();
        assertNotNull(trace);

        List<ShexAnnotated> path = trace.getExpressionPath();
        assertEquals(4, path.size());
        assertTrue(path.get(0) instanceof ShapeExprTripleExpr);
        assertTrue(path.get(1) instanceof TripleExprEachOf);
        assertTrue(path.get(2) instanceof TripleExprEachOf);
        assertTrue(path.get(3) instanceof TripleConstraint);

        // Check the annotation on the nested EachOf.
        TripleExprEachOf innerGroup = (TripleExprEachOf) path.get(2);
        assertEquals(
            "Measurements",
            innerGroup.getAnnotations().get(0)
                .getValue().getLiteralLexicalForm()
        );

        // Check the annotation on the actual failing constraint.
        TripleConstraint ageConstraint = (TripleConstraint) path.get(3);
        assertEquals(
            "Age in years",
            ageConstraint.getAnnotations().get(0)
                .getValue().getLiteralLexicalForm()
        );
    }

    public void testSuccessfulEachOfPartitionDoesNotLeakFailedPartitionReports() {
        String SCHEMA_EACHOF = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:PairShape {
            (
                ex:value xsd:string
                    // rdfs:comment "Text value" ;
                ex:value xsd:integer
                    // rdfs:comment "Numeric value"
            ) ;
            ex:active xsd:boolean
                // rdfs:comment "Active must be boolean"
        }
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_EACHOF), BASE);
        Graph data = GraphFactory.createDefaultGraph();
        Node focus = NodeFactory.createURI("http://example.org/pair");
        Node valuePredicate = NodeFactory.createURI("http://example.org/value");
        Node activePredicate = NodeFactory.createURI("http://example.org/active");
        data.add(Triple.create(
            focus,
            valuePredicate,
            NodeFactory.createLiteralString("hello")
        ));
        data.add(Triple.create(
            focus,
            valuePredicate,
            NodeFactory.createLiteral("42", null, XSDDatatype.XSDinteger)
        ));
        // This constraint fails after the nested EachOf succeeds.
        data.add(Triple.create(
            focus,
            activePredicate,
            NodeFactory.createLiteralString("not-a-boolean")
        ));
        Node shapeRef = NodeFactory.createURI("http://example.org/PairShape");
        ShexReport report = ShexValidator.get().validate(
            data, schema, shapeRef, focus
        );

        assertFalse(report.conforms());

        List<ReportItem> entries = new ArrayList<>(report.getEntries());
        // Only the two diagnostics from ex:active should remain.
        assertEquals(2, entries.size());
        assertEquals(1, entries.stream()
            .filter(item -> item.getMessage().contains("Wrong datatype"))
            .count());
        assertEquals(1, entries.stream()
            .filter(item -> item.getMessage().contains("Cardinality violation"))
            .count());

        // No report from the unsuccessful EachOf partitions may remain.
        for (ReportItem item : entries) {
            ValidationTrace trace = item.getValidationTrace();
            assertNotNull(trace);
            List<ShexAnnotated> path = trace.getExpressionPath();
            assertEquals(3, path.size());
            assertTrue(path.get(0) instanceof ShapeExprTripleExpr);
            assertTrue(path.get(1) instanceof TripleExprEachOf);
            assertTrue(path.get(2) instanceof TripleConstraint);
            TripleConstraint constraint = (TripleConstraint) path.get(2);
            assertEquals(
                "http://example.org/active",
                constraint.getPredicate().getURI()
            );
            assertEquals(
                "Active must be boolean",
                constraint.getAnnotations().get(0)
                    .getValue().getLiteralLexicalForm()
            );
        }
    }

    public void testEachOfAllPartitionsFailPreservesFailureTraces() {
        String SCHEMA_EACHOFFAIL = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:PairShape {
            ex:value xsd:string
                // rdfs:comment "Text value" ;
            ex:value xsd:integer
                // rdfs:comment "Numeric value"
        }
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_EACHOFFAIL), BASE);
        Graph data = GraphFactory.createDefaultGraph();
        Node focus = NodeFactory.createURI("http://example.org/pair");
        Node predicate = NodeFactory.createURI("http://example.org/value");
        data.add(Triple.create(
            focus,
            predicate,
            NodeFactory.createLiteralString("hello")
        ));
        data.add(Triple.create(
            focus,
            predicate,
            NodeFactory.createLiteralString("world")
        ));
        Node shapeRef = NodeFactory.createURI("http://example.org/PairShape");
        ShexReport report = ShexValidator.get().validate(
            data, schema, shapeRef, focus
        );

        assertFalse(report.conforms());
        assertFalse(report.getEntries().isEmpty());
        boolean foundTextConstraint = false;
        boolean foundNumericConstraint = false;
        for (ReportItem item : report.getEntries()) {
            ValidationTrace trace = item.getValidationTrace();
            assertNotNull(trace);
            List<ShexAnnotated> path = trace.getExpressionPath();
            assertEquals(3, path.size());
            assertTrue(path.get(0) instanceof ShapeExprTripleExpr);
            assertTrue(path.get(1) instanceof TripleExprEachOf);
            assertTrue(path.get(2) instanceof TripleConstraint);
            TripleConstraint constraint = (TripleConstraint) path.get(2);
            String annotation = constraint.getAnnotations()
                .get(0).getValue().getLiteralLexicalForm();
            if ("Text value".equals(annotation)) {
                foundTextConstraint = true;
            }
            else if ("Numeric value".equals(annotation)) {
                foundNumericConstraint = true;
            }
            else {
                fail("Unexpected annotation: " + annotation);
            }
        }

        // Diagnostics from both constraints must be retained.
        assertTrue("Missing diagnostics for text constraint",
                foundTextConstraint);
        assertTrue("Missing diagnostics for numeric constraint",
                foundNumericConstraint);
    }

    public void testCardinalityFailureTrace() {
        String SCHEMA_CARDINALITY = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:TagsShape {
            (
                ex:tag xsd:string ;
                ex:category xsd:string
            )+
            // rdfs:comment "At least one tag/category pair"
        }
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_CARDINALITY), BASE);
        Graph data = GraphFactory.createDefaultGraph();
        Node focus = NodeFactory.createURI("http://example.org/item");
        Node shapeRef = NodeFactory.createURI("http://example.org/TagsShape");
        ShexReport report = ShexValidator.get().validate(
            data, schema, shapeRef, focus
        );

        assertFalse(report.conforms());
        assertEquals(1, report.getEntries().size());

        ReportItem item = report.getEntries().iterator().next();
        assertEquals("Failed", item.getMessage());

        ValidationTrace trace = item.getValidationTrace();
        assertNotNull(trace);

        List<ShexAnnotated> path = trace.getExpressionPath();
        assertEquals(2, path.size());
        assertTrue(path.get(0) instanceof ShapeExprTripleExpr);
        assertTrue(path.get(1) instanceof TripleExprCardinality);

        TripleExprCardinality cardinality = (TripleExprCardinality) path.get(1);
        assertEquals(
            "At least one tag/category pair",
            cardinality.getAnnotations().get(0)
                .getValue().getLiteralLexicalForm()
        );
    }

    public void testSuccessfulCardinalityPartitionDoesNotLeakReports() {
        String SCHEMA_CARDINALITY = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:PairShape {
            (ex:value xsd:string{1,1})+ ;
            ex:active xsd:boolean
                // rdfs:comment "Active must be boolean"
        }
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_CARDINALITY), BASE);
        Graph data = GraphFactory.createDefaultGraph();
        Node focus = NodeFactory.createURI("http://example.org/pair");
        Node valuePredicate = NodeFactory.createURI("http://example.org/value");
        data.add(Triple.create(
            focus, valuePredicate, NodeFactory.createLiteralString("hello")
        ));
        data.add(Triple.create(
            focus, valuePredicate, NodeFactory.createLiteralString("world")
        ));
        // The following constraint fails after cardinality evaluation succeeds.
        data.add(Triple.create(
            focus,
            NodeFactory.createURI("http://example.org/active"),
            NodeFactory.createLiteralString("not-a-boolean")
        ));
        Node shapeRef = NodeFactory.createURI("http://example.org/PairShape");
        ShexReport report = ShexValidator.get().validate(
            data, schema, shapeRef, focus
        );

        assertFalse(report.conforms());
        List<ReportItem> entries = new ArrayList<>(report.getEntries());
        // Only the actual active constraint failure should remain.
        assertEquals(2, entries.size());
        assertEquals(1, entries.stream()
            .filter(item -> item.getMessage().contains("Wrong datatype"))
            .count());
        assertEquals(1, entries.stream()
            .filter(item -> item.getMessage().contains("Cardinality violation"))
            .count());
        for (ReportItem item : entries) {
            ValidationTrace trace = item.getValidationTrace();
            assertNotNull(trace);
            TripleConstraint constraint = (TripleConstraint) trace.getInnermostExpression();
            assertEquals(
                "http://example.org/active",
                constraint.getPredicate().getURI()
            );
        }
    }

    public void testShapeExprOrReportsLastFailedTrace() {
        String SCHEMA_OR = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:RootShape @ex:FirstShape OR @ex:LastShape
        ex:FirstShape {
            ex:first xsd:string
        }
            // rdfs:comment "First branch"
        ex:LastShape {
            ex:last xsd:string
        }
            // rdfs:comment "Last branch"
        """;

        Graph data = GraphFactory.createDefaultGraph();
        ShexReport report = validateOrSchema(SCHEMA_OR, data);

        assertFalse(report.conforms());
        List<ReportItem> entries = new ArrayList<>(report.getEntries());
        assertTrue(entries.size() >= 2);
        // Find the OR-level summary.
        ReportItem orItem = entries.stream()
            .filter(item -> "OR expression not satisfied:".equals(item.getMessage()))
            .findFirst()
            .orElseThrow();
        // No detailed diagnostic from the first alternative may remain.
        assertFalse(entries.stream()
            .filter(item -> item != orItem)
            .flatMap(item -> item.getAnnotations().stream())
            .anyMatch(annotation ->
                "First branch".equals(annotation.getValue().getLiteralLexicalForm())
            ));
        // Find a detailed diagnostic from the last failed alternative.
        ReportItem detailItem = entries.stream()
            .filter(item -> !"OR expression not satisfied:".equals(item.getMessage()))
            .filter(item -> !item.getAnnotations().isEmpty())
            .findFirst()
            .orElseThrow();
        // The detailed diagnostic must carry the LastShape annotation.
        assertEquals("Last branch",
            detailItem.getAnnotations().get(0).getValue().getLiteralLexicalForm()
        );
        // The OR-level report must carry that same annotation.
        assertNotNull(orItem.getValidationTrace());
        assertEquals(1, orItem.getAnnotations().size());
        assertEquals("Last branch",
            orItem.getAnnotations().get(0).getValue().getLiteralLexicalForm()
        );
        // Both diagnostics must resolve to the same annotated AST expression.
        assertSame(detailItem.getAnnotationSource(), orItem.getAnnotationSource());
    }

    public void testShapeExprOrFirstAlternativeSucceeds() {
        String SCHEMA_OR = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        ex:RootShape @ex:FirstShape OR @ex:LastShape
        ex:FirstShape {
            ex:first xsd:string
        }
        ex:LastShape {
            ex:last xsd:string
        }
        """;

        Graph data = GraphFactory.createDefaultGraph();
        Node focus = NodeFactory.createURI("http://example.org/alice");
        Node predicate = NodeFactory.createURI("http://example.org/first");
        Node value = NodeFactory.createLiteral(
            "valid", null, XSDDatatype.XSDstring
        );
        data.add(Triple.create(focus, predicate, value));
        ShexReport report = validateOrSchema(SCHEMA_OR, data);

        assertTrue(report.conforms());
        assertTrue(report.getEntries().isEmpty());
    }

    public void testShapeExprOrSecondAlternativeSucceeds() {
        String SCHEMA_OR = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:RootShape @ex:FirstShape OR @ex:LastShape
        ex:FirstShape {
            ex:first xsd:string
        }
            // rdfs:comment "First branch"
        ex:LastShape {
            ex:second xsd:string
        }
            // rdfs:comment "Last branch"
        """;

        Graph data = GraphFactory.createDefaultGraph();
        Node focus = NodeFactory.createURI("http://example.org/alice");
        Node predicate = NodeFactory.createURI("http://example.org/second");
        Node value = NodeFactory.createLiteral("valid", null, XSDDatatype.XSDstring);
        data.add(Triple.create(focus, predicate, value));
        ShexReport report = validateOrSchema(SCHEMA_OR, data);

        assertTrue(report.conforms());
        // The failed first alternative must not contribute diagnostics.
        assertTrue(report.getEntries().isEmpty());
    }

    public void testShapeExprOrFallsBackToEarlierDiagnostics() {
        String SCHEMA_OR = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:RootShape @ex:FirstShape OR @ex:MissingShape
        ex:FirstShape {
            ex:first xsd:string
        }
            // rdfs:comment "First branch"
        """;

        Graph data = GraphFactory.createDefaultGraph();
        ShexReport report = validateOrSchema(SCHEMA_OR, data);

        assertFalse(report.conforms());
        List<ReportItem> entries = new ArrayList<>(report.getEntries());
        assertTrue(entries.size() >= 2);
        ReportItem orItem = entries.stream()
            .filter(item -> "OR expression not satisfied:".equals(item.getMessage()))
            .findFirst()
            .orElseThrow();
        ReportItem detailItem = entries.stream()
            .filter(item -> !"OR expression not satisfied:".equals(item.getMessage()))
            .filter(item -> !item.getAnnotations().isEmpty())
            .findFirst()
            .orElseThrow();
        // The first alternative's detailed diagnostic must be retained.
        assertEquals(
            "First branch",
            detailItem.getAnnotations().get(0)
                .getValue().getLiteralLexicalForm()
        );
        // The OR summary must fall back to the last available failure trace.
        assertNotNull(orItem.getValidationTrace());
        assertEquals(1, orItem.getAnnotations().size());
        assertEquals(
            "First branch",
            orItem.getAnnotations().get(0)
                .getValue().getLiteralLexicalForm()
        );
        assertSame(detailItem.getAnnotationSource(), orItem.getAnnotationSource());
    }

    public void testShapeExprOrAllAlternativesFailWithoutDiagnostics() {
        String SCHEMA_OR = """
        PREFIX ex: <http://example.org/>
        ex:RootShape @ex:MissingFirst OR @ex:MissingLast
        """;

        Graph data = GraphFactory.createDefaultGraph();
        ShexReport report = validateOrSchema(SCHEMA_OR, data);

        assertFalse(report.conforms());
        // Only the OR-level report should exist.
        assertEquals(1, report.getEntries().size());
        ReportItem item = report.getEntries().iterator().next();
        assertEquals("OR expression not satisfied:", item.getMessage());
        assertNull(item.getValidationTrace());
        assertNull(item.getAnnotationSource());
        assertTrue(item.getAnnotations().isEmpty());
    }

    public void testTripleExprRefTrace() {
        String SCHEMA_TRIPLEREF = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:BaseShape {
            $ex:personDetails (
                ex:name xsd:string ;
                ex:age xsd:integer
                    // rdfs:comment "Age must be an integer"
            )
            // rdfs:comment "Shared person details"
        }
        ex:EmployeeShape {
            &ex:personDetails ;
            ex:employeeId xsd:string
        }
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_TRIPLEREF), BASE);
        Graph data = GraphFactory.createDefaultGraph();
        Node focus = NodeFactory.createURI("http://example.org/alice");
        data.add(Triple.create(
            focus,
            NodeFactory.createURI("http://example.org/name"),
            NodeFactory.createLiteralString("Alice")
        ));
        data.add(Triple.create(
            focus,
            NodeFactory.createURI("http://example.org/age"),
            NodeFactory.createLiteralString("thirty")
        ));
        data.add(Triple.create(
            focus,
            NodeFactory.createURI("http://example.org/employeeId"),
            NodeFactory.createLiteralString("E-123")
        ));
        Node shapeRef = NodeFactory.createURI("http://example.org/EmployeeShape");
        ShexReport report = ShexValidator.get().validate(
            data, schema, shapeRef, focus
        );

        assertFalse(report.conforms());
        ReportItem item = report.getEntries().stream()
            .filter(entry -> entry.getMessage().contains("Wrong datatype"))
            .findFirst()
            .orElseThrow();
        ValidationTrace trace = item.getValidationTrace();
        assertNotNull(trace);
        List<ShexAnnotated> path = trace.getExpressionPath();
        assertEquals(5, path.size());
        assertTrue(path.get(0) instanceof ShapeExprTripleExpr);
        assertTrue(path.get(1) instanceof TripleExprEachOf);
        assertTrue(path.get(2) instanceof TripleExprRef);
        assertTrue(path.get(3) instanceof TripleExprEachOf);
        assertTrue(path.get(4) instanceof TripleConstraint);

        // Check the reference.
        TripleExprRef ref = (TripleExprRef) path.get(2);
        assertEquals(
            "http://example.org/personDetails",
            ref.ref().getURI()
        );

        // Check the referenced group's annotation.
        TripleExprEachOf referencedGroup = (TripleExprEachOf) path.get(3);
        assertEquals(
            "Shared person details",
            referencedGroup.getAnnotations().get(0)
                .getValue().getLiteralLexicalForm()
        );

        // Check the actual failing constraint.
        TripleConstraint constraint = (TripleConstraint) path.get(4);
        assertEquals(
            "Age must be an integer",
            constraint.getAnnotations().get(0)
                .getValue().getLiteralLexicalForm()
        );
    }

    public void testNestedShapeReferenceTrace() {
        String SCHEMA_REFNESTED = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:PersonShape {
            ex:address @ex:AddressShape
                // rdfs:comment "The person's address"
        }
        ex:AddressShape {
            ex:postalCode xsd:integer
                // rdfs:comment "Postal code must be an integer"
        }
            // rdfs:comment "An address resource"
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA_REFNESTED), BASE);
        Graph data = GraphFactory.createDefaultGraph();
        Node person = NodeFactory.createURI("http://example.org/alice");
        Node address = NodeFactory.createURI("http://example.org/address1");
        data.add(Triple.create(
            person,
            NodeFactory.createURI("http://example.org/address"),
            address
        ));
        data.add(Triple.create(
            address,
            NodeFactory.createURI("http://example.org/postalCode"),
            NodeFactory.createLiteralString("ABCDE")
        ));
        Node shapeRef = NodeFactory.createURI("http://example.org/PersonShape");
        ShexReport report = ShexValidator.get().validate(
            data, schema, shapeRef, person
        );

        assertFalse(report.conforms());
        ReportItem item = report.getEntries().stream()
            .filter(entry -> entry.getMessage().contains("Wrong datatype"))
            .findFirst()
            .orElseThrow();
        ValidationTrace trace = item.getValidationTrace();
        assertNotNull(trace);
        List<ShexAnnotated> path = trace.getExpressionPath();
        assertEquals(4, path.size());
        assertTrue(path.get(0) instanceof ShapeExprTripleExpr);
        assertTrue(path.get(1) instanceof TripleConstraint);
        assertTrue(path.get(2) instanceof ShapeExprTripleExpr);
        assertTrue(path.get(3) instanceof TripleConstraint);

        // Outer constraint: PersonShape -> address
        TripleConstraint addressConstraint = (TripleConstraint) path.get(1);
        assertEquals(
            "The person's address",
            addressConstraint.getAnnotations().get(0)
                .getValue().getLiteralLexicalForm()
        );

        // Nested shape: AddressShape
        ShapeExprTripleExpr addressShape = (ShapeExprTripleExpr) path.get(2);
        assertEquals(
            "An address resource",
            addressShape.getAnnotations().get(0)
                .getValue().getLiteralLexicalForm()
        );

        // Inner constraint: AddressShape -> postalCode
        TripleConstraint postalCodeConstraint = (TripleConstraint) path.get(3);
        assertEquals(
            "Postal code must be an integer",
            postalCodeConstraint.getAnnotations().get(0)
                .getValue().getLiteralLexicalForm()
        );
    }

    public void testValidationContextChildInheritsExpressionPath() {
        String SCHEMA = """
        PREFIX ex: <http://example.org/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        ex:PersonShape {
            ex:age xsd:integer
                // rdfs:comment "Person's age"
        }
        """;

        ShexSchema schema = ShExC.parse(new StringReader(SCHEMA), BASE);
        ShapeExprTripleExpr shapeExpr =
            (ShapeExprTripleExpr) schema.getShapes().get(0).getShapeExpression();
        TripleConstraint constraint = (TripleConstraint) shapeExpr.getTripleExpr();
        Graph data = GraphFactory.createDefaultGraph();
        ValidationContext parent = new ValidationContext(data, schema);

        // Simulate an enclosing shape evaluation.
        parent.enterExpression(shapeExpr);

        // Create a child validation context.
        ValidationContext child = parent.create();
        assertEquals(1, child.getValidationTrace().getExpressionPath().size());

        child.enterExpression(constraint);
        child.reportEntry(new ReportItem("Test failure", null));
        ReportItem childItem = child.getReportItems().get(0);
        ValidationTrace trace = childItem.getValidationTrace();
        assertNotNull(trace);
        assertEquals(2, trace.getExpressionPath().size());
        assertSame(shapeExpr, trace.getExpressionPath().get(0));
        assertSame(constraint, trace.getExpressionPath().get(1));

        // Copy the report into the parent context.
        child.copyInto(parent);
        assertEquals(1, parent.getReportItems().size());
        ReportItem parentItem = parent.getReportItems().get(0);

        // The original trace must survive report propagation.
        assertSame(trace, parentItem.getValidationTrace());

        // Child stack changes must not affect the parent.
        child.leaveExpression(constraint);
        assertEquals(1, parent.getValidationTrace().getExpressionPath().size());
        parent.leaveExpression(shapeExpr);
        assertTrue(parent.getValidationTrace().getExpressionPath().isEmpty());
    }

    public void testValidationContextDoesNotInheritFailureTrace() {
        ShexSchema schema = ShExC.parse(
            new StringReader(getDummySchema()), BASE);
        ShapeExprTripleExpr shapeExpr =
            (ShapeExprTripleExpr) schema.getShapes().get(0).getShapeExpression();
        Graph data = GraphFactory.createDefaultGraph();
        ValidationContext parent = new ValidationContext(data, schema);
        parent.setLastFailureTrace(new ValidationTrace(List.of(shapeExpr)));
        ValidationContext child = parent.create();

        // The active expression path is inherited.
        assertEquals(0, child.getValidationTrace().getExpressionPath().size());
        // A previous failure is not inherited.
        assertNull(child.getLastFailureTrace());
        // The parent remains unchanged.
        assertNotNull(parent.getLastFailureTrace());
    }

    public void testValidationTraceIsImmutable() {
        ShexSchema schema = ShExC.parse(
            new StringReader(getDummySchema()), BASE);
        ShapeExprTripleExpr shapeExpr =
            (ShapeExprTripleExpr) schema.getShapes().get(0).getShapeExpression();
        TripleConstraint constraint = (TripleConstraint) shapeExpr.getTripleExpr();
        List<ShexAnnotated> path = new ArrayList<>();
        path.add(shapeExpr);
        path.add(constraint);

        ValidationTrace trace = new ValidationTrace(path);

        // Modifying the original list must not affect the trace.
        path.clear();
        assertEquals(2, trace.getExpressionPath().size());
        assertSame(shapeExpr, trace.getExpressionPath().get(0));
        assertSame(constraint, trace.getExpressionPath().get(1));

        // The returned path must be immutable.
        try {
            trace.getExpressionPath().clear();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // Expected.
        }
        assertSame(constraint, trace.getInnermostExpression());
    }

    public void testValidationTracePreservedWhenCopyingReport() {
        ShexSchema schema = ShExC.parse(
            new StringReader(getDummySchema()), BASE);
        ShapeExprTripleExpr shapeExpr =
            (ShapeExprTripleExpr) schema.getShapes().get(0).getShapeExpression();
        TripleConstraint constraint =
            (TripleConstraint) shapeExpr.getTripleExpr();
        Graph data = GraphFactory.createDefaultGraph();
        ValidationTrace trace = new ValidationTrace(List.of(shapeExpr, constraint));
        ReportItem item = new ReportItem("Datatype failure", null, trace);
        ValidationContext parent = new ValidationContext(data, schema);
        ValidationContext child = parent.create();
        child.reportEntry(item);

        // Copy child report into parent.
        child.copyInto(parent);
        assertEquals(1, parent.getReportItems().size());
        ReportItem copiedItem = parent.getReportItems().get(0);
        // The exact same trace instance should survive.
        assertSame(trace, copiedItem.getValidationTrace());
    }

    public void testShexReportPreservesValidationTrace() {
        ShexSchema schema = ShExC.parse(
            new StringReader(getDummySchema()), BASE);
        ShapeExprTripleExpr shapeExpr =
            (ShapeExprTripleExpr) schema.getShapes().get(0).getShapeExpression();
        TripleConstraint constraint = (TripleConstraint) shapeExpr.getTripleExpr();
        Graph data = GraphFactory.createDefaultGraph();
        Node focus = NodeFactory.createURI("http://example.org/alice");
        Node shapeRef = NodeFactory.createURI("http://example.org/PersonShape");

        ValidationTrace trace =
            new ValidationTrace(List.of(shapeExpr, constraint));
        ReportItem item = new ReportItem("Datatype failure", null, trace);
        ValidationContext context = new ValidationContext(data, schema);
        context.reportEntry(item);
        ShexRecord record = new ShexRecord(focus, shapeRef);
        context.shexReport(
            record, focus, ShexStatus.nonconformant, item.getMessage()
        );

        ShexReport report = context.generateReport();
        assertFalse(report.conforms());
        assertEquals(1, report.getEntries().size());
        ReportItem reportItem = report.getEntries().iterator().next();
        assertSame(trace, reportItem.getValidationTrace());
        // The returned collection must be immutable.
        try {
            report.getEntries().clear();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // Expected.
        }
    }

    public void testReportItemWithoutExpressionTrace() {
        ShexSchema schema = ShExC.parse(
            new StringReader(getDummySchema()), BASE);
        Graph data = GraphFactory.createDefaultGraph();
        ValidationContext context = new ValidationContext(data, schema);
        ReportItem item = new ReportItem("General validation error", null);
        context.reportEntry(item);

        assertEquals(1, context.getReportItems().size());
        ReportItem result = context.getReportItems().get(0);
        assertEquals("General validation error", result.getMessage());
        assertNull(result.getValidationTrace());
    }

    // -------------------------------------------------
    // HELPER methods
    // -------------------------------------------------

    private String getDummySchema() {
        return """
            PREFIX ex: <http://example.org/>
            PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
            ex:PersonShape {
                ex:age xsd:integer
            }
            """;
    }

    private ShexReport validateOrSchema(String schemaText, Graph data) {
        ShexSchema schema = ShExC.parse(new StringReader(schemaText), BASE);
        Node focus = NodeFactory.createURI("http://example.org/alice");
        Node shapeRef = NodeFactory.createURI("http://example.org/RootShape");
        return ShexValidator.get().validate(data, schema, shapeRef, focus);
    }

    private ReportItem validateAgeFailure(String constraintAnnotation, String groupAnnotation, String shapeAnnotation) {
        String shexc = """
            PREFIX ex: <http://example.org/>
            PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            ex:PersonShape {
                (
                    ex:name xsd:string ;
                    ex:age xsd:integer
                        %s
                )
                %s
            }
            %s
            """.formatted(
                annotationComment(constraintAnnotation),
                annotationComment(groupAnnotation),
                annotationComment(shapeAnnotation)
            );

        ShexSchema schema = ShExC.parse(new StringReader(shexc), BASE);
        Graph data = GraphFactory.createDefaultGraph();
        Node focus = NodeFactory.createURI("http://example.org/alice");
        data.add(Triple.create(
            focus,
            NodeFactory.createURI("http://example.org/name"),
            NodeFactory.createLiteralString("Alice")
        ));
        data.add(Triple.create(
            focus,
            NodeFactory.createURI("http://example.org/age"),
            NodeFactory.createLiteralString("thirty")
        ));
        Node shapeRef = NodeFactory.createURI("http://example.org/PersonShape");
        ShexReport report = ShexValidator.get().validate(
            data, schema, shapeRef, focus
        );

        assertFalse(report.conforms());
        return report.getEntries().stream()
            .filter(item -> item.getMessage().contains("Wrong datatype"))
            .findFirst()
            .orElseThrow();
    }

    private String annotationComment(String comment) {
        return comment == null ? "" : "// rdfs:comment \"" + comment + "\"";
    }
}
