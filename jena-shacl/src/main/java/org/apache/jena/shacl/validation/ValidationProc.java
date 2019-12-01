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

import java.util.*;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.engine.ShaclPaths;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.lib.G;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.NodeShape;
import org.apache.jena.shacl.parser.PropertyShape;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.vocabulary.RDF;

import static org.apache.jena.shacl.lib.G.hasType;
import static org.apache.jena.shacl.lib.G.isOfType;

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

    public static ValidationReport simpleValidation(Graph shapesGraph, Graph data) {
        return simpleValidation(shapesGraph, data, false);
    }

    public static ValidationReport simpleValidation(Graph shapesGraph, Graph data, boolean verbose) {
        Shapes shapes = Shapes.parse(shapesGraph);
        return simpleValidation(shapes, data, verbose);
    }

    public static ValidationReport simpleValidation(Shapes shapes, Graph data, boolean verbose) {
        int x = out.getAbsoluteIndent();
        try {
            ValidationContext vCxt = new ValidationContext(shapes, data);
            vCxt.setVerbose(verbose);
            return simpleValidation(vCxt, shapes, data);
        //} catch (ShaclParseException ex) {
        } finally { out.setAbsoluteIndent(x); }
    }

    public static ValidationReport simpleValidation(ValidationContext vCxt, Iterable<Shape> shapes, Graph data) {
        //vCxt.setVerbose(true);
        for ( Shape shape : shapes ) {
            simpleValidation(vCxt, data, shape);
        }
        if ( vCxt.isVerbose() )
            out.ensureStartOfLine();
        return vCxt.generateReport();
    }

    public static void simpleValidation(ValidationContext vCxt, Graph data, Shape shape) {
        simpleValidationInternal(vCxt, data, getFocusNodes(data, shape), shape);
    }
    
    // ---- Single node.
    
    public static ValidationReport simpleValidationNode(Shapes shapes, Graph data, Node node, boolean verbose) {
        int x = out.getAbsoluteIndent();
        try {
            ValidationContext vCxt = new ValidationContext(shapes, data);
            vCxt.setVerbose(verbose);
            return simpleValidationNode(vCxt, shapes, node, data);
        //} catch (ShaclParseException ex) {
        } finally { out.setAbsoluteIndent(x); }
    }

    
    private static ValidationReport simpleValidationNode(ValidationContext vCxt, Shapes shapes, Node node, Graph data) {
        //vCxt.setVerbose(true);
        for ( Shape shape : shapes ) {
            simpleValidationNode(vCxt, data, node, shape);
        }
        if ( vCxt.isVerbose() )
            out.ensureStartOfLine();
        return vCxt.generateReport();

    }

    private static void simpleValidationNode(ValidationContext vCxt, Graph data, Node node, Shape shape) {
        if (isFocusNode(shape, node, data)) {
            simpleValidationInternal(vCxt, data, Collections.singleton(node), shape);
        }

    }

    // --- Top of process
    private static void simpleValidationInternal(ValidationContext vCxt, Graph data, Collection<Node> focusNodes, Shape shape) {
        if ( vCxt.isVerbose() ) {
            out.println(shape.toString());
            out.printf("N: FocusNodes(%d): %s\n", focusNodes.size(), focusNodes);
            out.incIndent();
        }

        for ( Node focusNode : focusNodes ) {
            if ( vCxt.isVerbose() )
                out.println("F: "+focusNode);
            validateShape(vCxt, data, shape, focusNode);
        }
        if ( vCxt.isVerbose() ) {
            out.decIndent();
        }
    }

    // Recursion for shapes of shapes. "shape-expecting constraint parameters"
    public static void execValidateShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode) {
        validateShape(vCxt, data, shape, focusNode);
    }

    private static void validateShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode) {
        if ( shape.deactivated() )
            return;
        if ( vCxt.isVerbose() )
            out.println("S: "+shape);

        Path path;
        Set<Node> vNodes;
        if ( shape instanceof NodeShape ) {
            path = null;
            vNodes = null;
        } else if ( shape instanceof PropertyShape ) {
            PropertyShape propertyShape = (PropertyShape)shape;
            path = propertyShape.getPath();
            vNodes = ShaclPaths.valueNodes(data, focusNode, propertyShape.getPath());
        } else {
            if ( vCxt.isVerbose() )
                out.println("Z: "+shape);
            return;
        }

        // Constraints of this shape.
        for ( Constraint c : shape.getConstraints() ) {
            if ( vCxt.isVerbose() )
                out.println("C: "+c);
            evalConstraint(vCxt, data, shape, focusNode, path, vNodes, c);
        }

        // Follow sh:property (sh:node behaves as a constraint).
        validationPropertyShapes(vCxt, data, shape.getPropertyShapes(), focusNode);
        if ( vCxt.isVerbose() )
            out.println();
    }

    private static void validationPropertyShapes(ValidationContext vCxt, Graph data, List<PropertyShape> propertyShapes, Node focusNode) {
        if ( propertyShapes == null )
            return;
        for ( PropertyShape propertyShape : propertyShapes ) {
            validationPropertyShape(vCxt, data, propertyShape, focusNode);
        }
    }

    // XXX This is *nearly* validationShape.
    private static void validationPropertyShape(ValidationContext vCxt, Graph data, PropertyShape propertyShape, Node focusNode) {
        //validateShape(vCxt, data, propertyShape, focusNode);
        // sh:property got us here.
        if ( propertyShape.deactivated() )
            return;
        if ( vCxt.isVerbose() )
            out.println("P: "+propertyShape);

        Set<Node> vNodes = ShaclPaths.valueNodes(data, focusNode, propertyShape.getPath());

        // DRY with validateShape.
        for ( Constraint c : propertyShape.getConstraints() ) {
            if ( vCxt.isVerbose() )
                out.println("C: "+focusNode+" :: "+c);
            // Pass vNodes here.
            evalConstraint(vCxt, data, propertyShape, focusNode, propertyShape.getPath(), vNodes, c);
        }
        vNodes.forEach(vNode->{
            validationPropertyShapes(vCxt, data, propertyShape.getPropertyShapes(), vNode);
        });
    }

    private static void evalConstraint(ValidationContext vCxt, Graph data, Shape shape, Node focusNode, Path path, Set<Node> pathNodes, Constraint c) {
        if ( path == null ) {
            if ( pathNodes != null )
                throw new InternalErrorException("Path is null but pathNodes is not null");
            c.validateNodeShape(vCxt, data, shape, focusNode);
            return;
        }
        if ( pathNodes == null )
            throw new InternalErrorException("Path is not null but pathNodes is null");
        c.validatePropertyShape(vCxt, data, shape, focusNode, path, pathNodes);
    }

    private static Collection<Node> getFocusNodes(Graph data, Shape shape) {
        Collection<Node> acc = new HashSet<>();
        shape.getTargets().forEach(target->
            acc.addAll(getFocusNodes(data, target)));
        return acc;
    }

    private static Collection<Node> getFocusNodes(Graph data, Target target) {
        Node targetObj = target.getObject();
        switch(target.getTargetType()) {
            case targetClass:
                return G.listPO(data, RDF.Nodes.type, targetObj);
            case targetNode:
                return Collections.singletonList(targetObj);
            case targetObjectsOf:
                return G.setSP(data, null, targetObj);
            case targetSubjectsOf:
                return G.setPO(data, targetObj, null);
            case implicitClass:
                // Instances of the class and its subtypes.
                return G.listAllNodesOfType(data, targetObj);
            default:
                return Collections.emptyList();
        }
    }

    private static boolean isFocusNode(Shape shape, Node node, Graph data) {
        return shape.getTargets()
                .stream()
                .anyMatch(target -> isFocusNode(target, node, data));
    }

    private static boolean isFocusNode(Target target, Node node, Graph data) {
        Node targetObject = target.getObject();
        switch (target.getTargetType()) {
            case targetClass:
                return hasType(data, node, targetObject);
            case targetNode:
                return targetObject.equals(node);
            case targetObjectsOf:
                return data.contains(null, targetObject, node);
            case targetSubjectsOf:
                return data.contains(node, targetObject, null);
            case implicitClass:
                return isOfType(data, node, targetObject);
            default:
                return false;
        }
    }
}
