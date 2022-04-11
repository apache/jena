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

package org.apache.jena.shacl.tests;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.validation.VLib;
import org.apache.jena.shacl.validation.ValidationListener;
import org.apache.jena.shacl.validation.event.*;
import org.apache.jena.vocabulary.XSD;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static org.apache.jena.shacl.validation.event.EventPredicates.*;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class ValidationListenerTests {
    private final String shapesGraphUri;
    private final String dataGraphUri;
    private final PredicateTreeNode predicateTree;

    public ValidationListenerTests(String shapesGraphUri, String dataGraphUri,
                    PredicateTreeNode predicateTree) {
        this.shapesGraphUri = shapesGraphUri;
        this.dataGraphUri = dataGraphUri;
        this.predicateTree = predicateTree;
    }

    @SuppressWarnings("HttpUrlsUsage")
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                        { "src/test/files/std/core/node/datatype-001.ttl",
                                        "src/test/files/std/core/node/datatype-001.ttl",
                                        EventTestBuilder
                                                        .builder()
                                                        .choice()
                                                        .when(isOfType(FocusNodeValidationStartedEvent.class)
                                                                                        .and(focusNode().literalEquals(
                                                                                                        LiteralLabelFactory.create("42",
                                                                                                                        XSDDatatype.XSDinteger)))
                                                                                        .and(shapeNode().uriEquals(
                                                                                                        "http://datashapes.org/sh/tests/core/node/datatype-001.test#TestShape")),
                                                                        sb -> sb
                                                                                        .nextVerify(
                                                                                                        isOfType(ConstraintEvaluationForNodeShapeStartedEvent.class)
                                                                                                                        .and(hasDatatypeConstraint()))
                                                                                        .nextVerify(
                                                                                                        isOfType(ConstraintEvaluatedOnFocusNodeEvent.class)
                                                                                                                        .and(hasDatatypeConstraint())
                                                                                                                        .and(isValid()))
                                                                                        .nextVerify(
                                                                                                        isOfType(ConstraintEvaluationForNodeShapeFinishedEvent.class)
                                                                                                                        .and(hasDatatypeConstraint()))
                                                                                        .nextVerify(
                                                                                                        isOfType(FocusNodeValidationFinishedEvent.class)
                                                                                                                        .and(not(hasDatatypeConstraint()))))
                                                        .when(isOfType(FocusNodeValidationStartedEvent.class)
                                                                                        .and(focusNode().isBlank()),
                                                                        sb -> sb
                                                                                        .nextVerify(
                                                                                                        isOfType(ConstraintEvaluationForNodeShapeStartedEvent.class)
                                                                                                                        .and(hasDatatypeConstraint()))
                                                                                        .nextVerify(
                                                                                                        isOfType(ConstraintEvaluatedOnFocusNodeEvent.class)
                                                                                                                        .and(not(isValid())))
                                                                                        .nextVerify(
                                                                                                        isOfType(ConstraintEvaluationForNodeShapeFinishedEvent.class)
                                                                                                                        .and(shapeNode().uriEquals(
                                                                                                                                        "http://datashapes.org/sh/tests/core/node/datatype-001.test#TestShape"))
                                                                                        )
                                                                                        .nextVerify(
                                                                                                        isOfType(FocusNodeValidationFinishedEvent.class)
                                                                                                                        .and(focusNode().isBlank()))
                                                        )
                                                        .when(isOfType(FocusNodeValidationStartedEvent.class)
                                                                                        .and(focusNode().literalEquals(
                                                                                                        LiteralLabelFactory.create(
                                                                                                                        "aldi",
                                                                                                                        XSDDatatype.XSDinteger))),
                                                                        sb -> sb
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluationForNodeShapeStartedEvent.class))
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluatedOnFocusNodeEvent.class)
                                                                                                        .and(hasDatatypeConstraint())
                                                                                                        .and(not(isValid())))
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluationForNodeShapeFinishedEvent.class).and(
                                                                                                        focusNode().isLiteral()))
                                                                                        .nextVerify(isOfType(
                                                                                                        FocusNodeValidationFinishedEvent.class)))
                                                        .when(isOfType(FocusNodeValidationStartedEvent.class)
                                                                                        .and(focusNode().uriEquals(
                                                                                                        XSD.integer.getURI())),
                                                                        sb -> sb
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluationForNodeShapeStartedEvent.class)
                                                                                                        .and(focusNode().uriEquals(
                                                                                                                        XSD.integer.getURI())))
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluatedOnFocusNodeEvent.class)
                                                                                                        .and(focusNode().uriEquals(
                                                                                                                        XSD.integer.getURI()))
                                                                                                        .and(hasDatatypeConstraint())
                                                                                                        .and(not(isValid())))
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluationForNodeShapeFinishedEvent.class)
                                                                                                        .and(focusNode().uriEquals(
                                                                                                                        XSD.integer.getURI())))
                                                                                        .nextVerify(isOfType(
                                                                                                        FocusNodeValidationFinishedEvent.class)
                                                                                                        .and(focusNode().uriEquals(
                                                                                                                        XSD.integer.getURI()))))
                                                        .build()
                        },
                        {
                                        "src/test/files/std/core/node/class-001.ttl",
                                        "src/test/files/std/core/node/class-001.ttl",
                                        EventTestBuilder.builder()
                                                        .choice()
                                                        .when(isOfType(FocusNodeValidationStartedEvent.class)
                                                                                        .and(focusNode().uriEquals(
                                                                                                        "http://datashapes.org/sh/tests/core/node/class-001.test#Someone"))
                                                                                        .and(shapeNode().uriEquals(
                                                                                                        "http://datashapes.org/sh/tests/core/node/class-001.test#TestShape")),
                                                                        sb -> sb
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluationForNodeShapeStartedEvent.class))
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluatedOnFocusNodeEvent.class)
                                                                                                        .and(isValid()))
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluationForNodeShapeFinishedEvent.class))
                                                                                        .nextVerify(isOfType(
                                                                                                        FocusNodeValidationFinishedEvent.class)))
                                                        .when(isOfType(FocusNodeValidationStartedEvent.class)
                                                                                        .and(focusNode().uriEquals(
                                                                                                        "http://datashapes.org/sh/tests/core/node/class-001.test#John"))
                                                                                        .and(shapeNode().uriEquals(
                                                                                                        "http://datashapes.org/sh/tests/core/node/class-001.test#TestShape")),
                                                                        sb -> sb
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluationForNodeShapeStartedEvent.class))
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluatedOnFocusNodeEvent.class)
                                                                                                        .and(isValid()))
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluationForNodeShapeFinishedEvent.class))
                                                                                        .nextVerify(isOfType(
                                                                                                        FocusNodeValidationFinishedEvent.class)))
                                                        .when(isOfType(FocusNodeValidationStartedEvent.class)
                                                                                        .and(focusNode().uriEquals(
                                                                                                        "http://datashapes.org/sh/tests/core/node/class-001.test#Typeless"))
                                                                                        .and(shapeNode().uriEquals(
                                                                                                        "http://datashapes.org/sh/tests/core/node/class-001.test#TestShape")),
                                                                        sb -> sb
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluationForNodeShapeStartedEvent.class))
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluatedOnFocusNodeEvent.class)
                                                                                                        .and(not(isValid())))
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluationForNodeShapeFinishedEvent.class))
                                                                                        .nextVerify(isOfType(
                                                                                                        FocusNodeValidationFinishedEvent.class)))
                                                        .when(isOfType(FocusNodeValidationStartedEvent.class)
                                                                                        .and(focusNode().uriEquals(
                                                                                                        "http://datashapes.org/sh/tests/core/node/class-001.test#Quokki"))
                                                                                        .and(shapeNode().uriEquals(
                                                                                                        "http://datashapes.org/sh/tests/core/node/class-001.test#TestShape")),
                                                                        sb -> sb
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluationForNodeShapeStartedEvent.class))
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluatedOnFocusNodeEvent.class)
                                                                                                        .and(not(isValid())))
                                                                                        .nextVerify(isOfType(
                                                                                                        ConstraintEvaluationForNodeShapeFinishedEvent.class))
                                                                                        .nextVerify(isOfType(
                                                                                                        FocusNodeValidationFinishedEvent.class)))
                                                        .build()
                        }
        });
    }

    @Test
    public void testOnlyExpectedEventsEmitted() {
        Graph shapesGraph = RDFDataMgr.loadGraph(shapesGraphUri);
        Graph dataGraph = RDFDataMgr.loadGraph(dataGraphUri);
        RecordingValidationListener listener = new RecordingValidationListener();
        Shapes shapes = Shapes.parse(shapesGraph);
        ValidationContext vCtx = ValidationContext.create(shapes, dataGraph, listener);
        for (Shape shape : shapes.getTargetShapes()) {
            Collection<Node> focusNodes = VLib.focusNodes(dataGraph, shape);
            for (Node focusNode : focusNodes) {
                VLib.validateShape(vCtx, dataGraph, shape, focusNode);
            }
        }
        List<ValidationEvent> actualEvents = listener.getEvents();
        boolean allTestsRun = false;
        PredicateTreeNode currentNode = predicateTree;
        List<ValidationEvent> acceptedEvents = new ArrayList<>();
        for (ValidationEvent e : actualEvents) {
            if (allTestsRun) {
                provideInfoForFailure(acceptedEvents);
                fail("Spurious event: " + e);
            }
            TestResult testResult = currentNode.performTest(e);
            if (testResult.isPassed()) {
                acceptedEvents.add(e);
            } else {
                provideInfoForFailure(acceptedEvents);
                fail("Event failed test: " + e);
            }
            currentNode = testResult.getNextNode();
            if (currentNode == null) {
                allTestsRun = true;
            }
        }
        while (currentNode != null && currentNode != predicateTree) {
            currentNode = currentNode.performTest(null).getNextNode();
        }
    }

    private void provideInfoForFailure(List<ValidationEvent> acceptedEvents) {
        System.err.println("Test failed!");
        if (acceptedEvents.size() > 0) {
            System.err.println("The following ValidationEvents were accepted before the test failed:");
            for (ValidationEvent acceptedEvent : acceptedEvents) {
                System.err.println("    event passed test: " + acceptedEvent);
            }
        } else {
            System.err.println("No ValidationEvents were accepted before the test failed.");
        }
    }

    private static class RecordingValidationListener implements ValidationListener {
        private final List<ValidationEvent> events = new ArrayList<>();

        @Override public void onValidationEvent(ValidationEvent e) {
            if (events.contains(e)) {
                fail(String.format("Duplicate event of type %s emitted by SHACL validation",
                                e.getClass().getSimpleName()));
            }
            events.add(e);
        }

        public List<ValidationEvent> getEvents() {
            return events;
        }
    }

    private static class TestResult {
        private final PredicateTreeNode nextNode;
        private final boolean passed;
        private final String message;

        public TestResult(PredicateTreeNode nextNode, boolean passed, String message) {
            this.nextNode = nextNode;
            this.passed = passed;
            this.message = message;
        }

        public PredicateTreeNode getNextNode() {
            return nextNode;
        }

        public boolean isPassed() {
            return passed;
        }

        @SuppressWarnings("unused")
        public String getMessage() {
            return message;
        }
    }

    private static abstract class PredicateTreeNode {
        private final PredicateTreeNode parent;

        public PredicateTreeNode getParent() {
            return parent;
        }

        public abstract TestResult performTest(ValidationEvent event);

        private PredicateTreeNode(PredicateTreeNode parent) {
            this.parent = parent;
        }

        protected TestResult deferTestToParent(ValidationEvent event) {
            if (getParent() != null) {
                return getParent().performTest(event);
            } else if (event == null) {
                return new TestResult(null, true, "Bubbling back up the test tree, not processing an event");
            }
            return new TestResult(null, false, "Spurious event");
        }
    }

    private static class SequenceNode extends PredicateTreeNode {
        private final List<PredicateTreeNode> children = new ArrayList<>();
        private Iterator<PredicateTreeNode> childrenIterator;

        public SequenceNode(PredicateTreeNode parent) {
            super(parent);
        }

        public void addNode(PredicateTreeNode node) {
            children.add(node);
        }

        private Iterator<PredicateTreeNode> getChildrenIteratorLazily() {
            if (childrenIterator == null) {
                childrenIterator = children.iterator();
            }
            return childrenIterator;
        }

        @Override public TestResult performTest(ValidationEvent event) {
            PredicateTreeNode child = getNextChild();
            if (child == null) {
                return deferTestToParent(event);
            }
            return child.performTest(event);
        }

        private PredicateTreeNode getNextChild() {
            Iterator<PredicateTreeNode> it = getChildrenIteratorLazily();
            if (it.hasNext()) {
                return it.next();
            }
            return null;
        }
    }

    private static class LeafNode extends PredicateTreeNode {
        private final Predicate<ValidationEvent> predicate;

        public LeafNode(PredicateTreeNode parent,
                        Predicate<ValidationEvent> predicate) {
            super(parent);
            this.predicate = predicate;
        }

        @Override public TestResult performTest(ValidationEvent event) {
            return new TestResult(getParent(), predicate.test(event), "Result of predicate evaluation");
        }
    }

    private static class ChoiceNode extends PredicateTreeNode {
        private final Map<Predicate<ValidationEvent>, PredicateTreeNode> alternatives = new HashMap<>();
        private final Set<Predicate<ValidationEvent>> alreadySelected = new HashSet<>();

        public ChoiceNode(PredicateTreeNode parent) {
            super(parent);
        }

        public void addAlternative(Predicate<ValidationEvent> key, PredicateTreeNode tree) {
            this.alternatives.put(key, tree);
        }

        @Override public TestResult performTest(ValidationEvent event) {
            if (alternatives.keySet().size() == alreadySelected.size()) {
                // all alternatives have been verified, continue with parent
                return deferTestToParent(event);
            }
            for (Predicate<ValidationEvent> possibleAlternative : alternatives.keySet()) {
                if (possibleAlternative.test(event)) {
                    if (alreadySelected.contains(possibleAlternative)) {
                        return new TestResult(null, false,
                                        "At least two events satisfy condition of choice node, this one was encountered second: "
                                                        + event);
                    }
                    alreadySelected.add(possibleAlternative);
                    return new TestResult(alternatives.get(possibleAlternative), true,
                                    "Positive evaluation of the choice condition consumes the event");
                }
            }
            return new TestResult(null, false, "Unexpected event: " + event);
        }
    }

    private static class EventTestBuilder {
        private EventTestBuilder() {
        }

        public static SequenceBuilder builder() {
            return new SequenceBuilder(null);
        }
    }

    private static class ChoiceBuilder {
        private final ChoiceNode product;

        private ChoiceBuilder(PredicateTreeNode parent) {
            this.product = new ChoiceNode(parent);
        }

        public ChoiceBuilder when(Predicate<ValidationEvent> condition, Consumer<SequenceBuilder> sequenceConfigurer) {
            SequenceBuilder sequenceBuilder = new SequenceBuilder(product);
            sequenceConfigurer.accept(sequenceBuilder);
            this.product.addAlternative(condition, sequenceBuilder.build());
            return this;
        }

        public ChoiceNode build() {
            return this.product;
        }
    }

    private static class SequenceBuilder {
        private final SequenceNode product;

        private SequenceBuilder(PredicateTreeNode parent) {
            this.product = new SequenceNode(parent);
        }

        public SequenceBuilder nextVerify(Predicate<ValidationEvent> predicate) {
            this.product.addNode(new LeafNode(product, predicate));
            return this;
        }

        public ChoiceBuilder choice() {
            return new ChoiceBuilder(product);
        }

        public SequenceNode build() {
            return product;
        }
    }
}
