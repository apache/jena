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

package org.apache.jena.shacl.validation;

import java.util.Collection;
import java.util.Collections;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.validation.event.*;

public class ValidationProc {
    /* 3.4 Validation
     *
     * Validation is a mapping from some input to validation results, as defined in the
     * following paragraphs.
     *
     * Validation of a data graph against a shapes graph: Given a data graph and a shapes
     * graph, the validation results are the union of results of the validation of the
     * data graph against all shapes in the shapes graph.
     *
     * Validation of a data graph against a shape: Given a data graph and a shape in the
     * shapes graph, the validation results are the union of the results of the validation
     * of all focus nodes that are in the target of the shape in the data graph.
     *
     * // shape -> focus nodes -> validation
     *
     * Validation of a focus node against a shape: Given a focus node in the data graph
     * and a shape in the shapes graph, the validation results are the union of the
     * results of the validation of the focus node against all constraints declared by the
     * shape, unless the shape has been deactivated, in which case the validation results
     * are empty.
     *
     * // focus node -> all constraints
     *
     * Validation of a focus node against a constraint: Given a focus node in the data
     * graph and a constraint of kind C in the shapes graph, the validation results are
     * defined by the validators of the constraint component C. These validators typically
     * take as input the focus node, the specific values of the parameters of C of the
     * constraint in the shapes graph, and the value nodes of the shape that declares the
     * constraint.
     *
     * // Constraint(focus node "typically") -> result
     *
     * During validation, the data graph and the shapes graph MUST remain immutable, i.e.
     * both graphs at the end of the validation MUST be identical to the graph at the
     * beginning of validation. SHACL processors MUST NOT change the graphs that they use
     * to construct the shapes graph or the data graph, even if these graphs are part of
     * an RDF store that allows changes to its stored graphs. SHACL processors MAY store
     * the graphs that they create, such as a graph containing validation results, and
     * this operation MAY change existing graphs in an RDF store, but not any of the
     * graphs that were used to construct the shapes graph or the data graph. SHACL
     * processing is thus idempotent.
     */

    private static IndentedWriter out  = IndentedWriter.stdout;

    public static ValidationReport plainValidation(Shapes shapes, Graph data) {
        int x = out.getAbsoluteIndent();
        try {
            ValidationContext vCxt = ValidationContext.create(shapes, data);
            return plainValidation(vCxt, shapes, data);
        } finally { out.setAbsoluteIndent(x); }
    }

    private static ValidationReport plainValidation(ValidationContext vCxt, Shapes shapes, Graph data) {
        Collection<Shape> targetShapes = shapes.getTargetShapes();
        vCxt.notifyValidationListener(() -> new TargetShapesValidationStartedEvent(vCxt, targetShapes));
        try {
            targetShapes.forEach(shape->plainValidation(vCxt, shape, data));
            if (vCxt.isVerbose())
                out.ensureStartOfLine();
            return vCxt.generateReport();
        } finally {
            vCxt.notifyValidationListener(() -> new TargetShapesValidationFinishedEvent(vCxt, targetShapes));
        }
    }

    private static void plainValidation(ValidationContext vCxt, Shape shape, Graph data) {
        plainValidationInternal(vCxt, data, null, shape);
    }

    // ---- Single node.

    public static ValidationReport plainValidationNode(Shapes shapes, Graph data, Node node) {
        int x = out.getAbsoluteIndent();
        try {
            ValidationContext vCxt = ValidationContext.create(shapes, data);
            return plainValidationNode(vCxt, shapes, node, data);
        } finally { out.setAbsoluteIndent(x); }
    }

    private static ValidationReport plainValidationNode(ValidationContext vCxt, Shapes shapes, Node node, Graph data) {
        Collection<Shape> targetShapes = shapes.getTargetShapes();
        vCxt.notifyValidationListener(() -> new TargetShapesValidationStartedEvent(vCxt, targetShapes));
        try {
            targetShapes.forEach(shape ->
                            plainValidationNode(vCxt, data, node, shape)
            );
            if (vCxt.isVerbose())
                out.ensureStartOfLine();
            return vCxt.generateReport();
        } finally {
            vCxt.notifyValidationListener(() -> new TargetShapesValidationFinishedEvent(vCxt, targetShapes));
        }
    }

    private static void plainValidationNode(ValidationContext vCxt, Graph data, Node node, Shape shape) {
        plainValidationInternal(vCxt, data, node, shape);
    }

    // --- Top of process

    /**
     * Validation process.
     * Either all focusNode for the shape (argument node == null)
     * or just for one node of the focusNodes of the shape.
     */
    private static void plainValidationInternal(ValidationContext vCxt, Graph data, Node node, Shape shape) {
        Collection<Node> focusNodes;
        vCxt.notifyValidationListener(() -> new ShapeValidationStartedEvent(vCxt, shape));
        if ( node != null ) {
            if (! VLib.isFocusNode(shape, node, data))
                return ;
            focusNodes = Collections.singleton(node);
        } else {
            focusNodes = VLib.focusNodes(data, shape);
        }
        vCxt.notifyValidationListener(() -> new FocusNodesDeterminedEvent(vCxt, shape, focusNodes));

        if ( vCxt.isVerbose() ) {
            out.println(shape.toString());
            out.printf("N: FocusNodes(%d): %s\n", focusNodes.size(), focusNodes);
            out.incIndent();
        }

        for ( Node focusNode : focusNodes ) {
            if ( vCxt.isVerbose() )
                out.println("F: "+focusNode);
            VLib.validateShape(vCxt, data, shape, focusNode);
        }
        if ( vCxt.isVerbose() ) {
            out.decIndent();
        }
        vCxt.notifyValidationListener(() -> new ShapeValidationFinishedEvent(vCxt, shape));
    }

    // Make ValidationContext carry the ShaclValidator to recurse on.
    // Recursion for shapes of shapes. "shape-expecting constraint parameters"
    public static void execValidateShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode) {
        vCxt.notifyValidationListener(() -> new ShapeValidationStartedEvent(vCxt, shape));
        try {
            VLib.validateShape(vCxt, data, shape, focusNode);
        } finally {
            vCxt.notifyValidationListener(() -> new ShapeValidationFinishedEvent(vCxt, shape));
        }
    }
}
