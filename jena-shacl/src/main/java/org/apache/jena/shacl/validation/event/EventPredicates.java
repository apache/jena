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

package org.apache.jena.shacl.validation.event;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.shacl.engine.constraint.DatatypeConstraint;
import org.apache.jena.shacl.parser.Constraint;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;


public abstract class EventPredicates {
    public static Predicate<ValidationEvent> isOfType(Class<? extends ValidationEvent> type) {
        return e -> type.equals(e.getClass());
    }

    public static Predicate<ValidationEvent> isOfTypeOrSubtype(Class<? extends ValidationEvent> type) {
        return e -> EventUtil.getSuperclassesAndInterfaces(e.getClass()).anyMatch(s -> s.equals(type));
    }

    public static <E extends ValidationEvent> Predicate<ValidationEvent> testIfType(Class<E> type, Predicate<E> predicate, boolean defaultValue){
        return e -> testIfType(e, type, predicate, defaultValue);
    }

    public static <E extends ValidationEvent> Predicate<ValidationEvent> testIfTypeElseFalse(Class<E> type, Predicate<E> predicate){
        return e -> testIfType(e, type, predicate, false);
    }

    private static <E extends ValidationEvent> boolean testIfType(ValidationEvent e, Class<E> type, Predicate<E> predicate, boolean defaultValue){
        if (type.isAssignableFrom(e.getClass())) {
            return predicate.test(type.cast(e));
        }
        return defaultValue;
    }

    public static NodePredicate<ShapeValidationEvent> shapeNode(){
        return new NodePredicate<>(ShapeValidationEvent.class, e -> e.getShape().getShapeNode());
    }

    public static NodePredicate<FocusNodeValidationEvent> focusNode(){
        return new NodePredicate<>(FocusNodeValidationEvent.class, FocusNodeValidationEvent::getFocusNode);
    }

    public static Predicate<ValidationEvent> hasConstraintOfType(Class<? extends Constraint> constraintType) {
        Objects.requireNonNull(constraintType);
        return testIfType(ConstraintEvaluationEvent.class, e -> constraintType.isAssignableFrom(e.getConstraint().getClass()), false);
    }

    public static Predicate<ValidationEvent> hasDatatypeConstraint(){
        return hasConstraintOfType(DatatypeConstraint.class);
    }

    public static Predicate<ValidationEvent> isValid(){
        return testIfTypeElseFalse(ConstraintEvaluatedEvent.class, ConstraintEvaluatedEvent::isValid);
    }

    public static class NodePredicate<E extends ValidationEvent> {
        private final Function<ValidationEvent, Node> nodeAccessor;

        @SuppressWarnings("unchecked")
        public NodePredicate(Class<E> type, Function<E, Node> nodeAccessor) {
            this.nodeAccessor = e -> type.isAssignableFrom(e.getClass()) ?  nodeAccessor.apply((E) e) : null;
        }

        public Predicate<ValidationEvent> makePredicate(Predicate<Node> predicate){
            return e -> Optional.ofNullable(nodeAccessor.apply(e)).map(predicate::test).orElse(false);
        }

        public Predicate<ValidationEvent> isBlank(){
            return makePredicate(Node::isBlank);
        }

        public Predicate<ValidationEvent> isLiteral(){
            return makePredicate(Node::isLiteral);
        }

        public Predicate<ValidationEvent> uriEquals(String uri){
            return makePredicate( n -> n.isURI() && n.getURI().equals(uri));
        }

        public Predicate<ValidationEvent> literalEquals(LiteralLabel literalLabel) {
            return makePredicate( n -> n.isLiteral() && n.getLiteral().sameValueAs(literalLabel));
        }
    }
}
