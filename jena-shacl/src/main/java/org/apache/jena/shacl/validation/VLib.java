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
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.other.G;
import org.apache.jena.shacl.engine.ShaclPaths;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.NodeShape;
import org.apache.jena.shacl.parser.PropertyShape;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.sparql.path.Path;

/**
 * The validation algorithm from the
 * <a href="https://www.w3.org/TR/shacl/#validation-definition">SHACL specification - section 3.4</a>.
 */

public class VLib {
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
    private static IndentedWriter out  = IndentedWriter.clone(IndentedWriter.stdout);

    public static void validateShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode) {
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

        // Reachable shapes.
        // Follow sh:property (sh:node behaves as a constraint).
        validationPropertyShapes(vCxt, data, shape.getPropertyShapes(), focusNode);
        if ( vCxt.isVerbose() )
            out.println();
    }

    static void validationPropertyShapes(ValidationContext vCxt, Graph data, Collection<PropertyShape> propertyShapes, Node focusNode) {
        if ( propertyShapes == null )
            return;
        for ( PropertyShape propertyShape : propertyShapes ) {
            validationPropertyShape(vCxt, data, propertyShape, focusNode);
        }
    }

    // This is nearly validationShape. The difference is passing in of vNodes
    // evalConstraint (null for a NodeShape) and the loop on vNodes for
    // getPropertyShapes(). Having extra verbose output helps.
    private static void validationPropertyShape(ValidationContext vCxt, Graph data, PropertyShape propertyShape, Node focusNode) {
        if ( propertyShape.deactivated() )
            return;
        if ( vCxt.isVerbose() )
            out.println("P: "+propertyShape);

        Path path = propertyShape.getPath();
        Set<Node> vNodes = ShaclPaths.valueNodes(data, focusNode, path);

        for ( Constraint c : propertyShape.getConstraints() ) {
            if ( vCxt.isVerbose() )
                out.println("C: "+focusNode+" :: "+c);
            // Pass vNodes here.
            evalConstraint(vCxt, data, propertyShape, focusNode, path, vNodes, c);
        }
        vNodes.forEach(vNode->{
            validationPropertyShapes(vCxt, data, propertyShape.getPropertyShapes(), vNode);
        });
    }

    // ValidationProc
    public static Collection<Node> focusNodes(Graph data, Shape shape) {
        Collection<Node> acc = new HashSet<>();
        shape.getTargets().forEach(target->
            acc.addAll(focusNodes(data, target)));
        return acc;
    }

    // ValidationProc
    public static Collection<Node> focusNodes(Graph data, Target target) {
        return target.getFocusNodes(data);
    }

    // From ValidationProc.
    public static boolean isFocusNode(Shape shape, Node node, Graph data) {
        return shape.getTargets()
                .stream()
                .anyMatch(target -> isFocusNode(target, node, data));
    }

    public static boolean isFocusNode(Target target, Node node, Graph data) {
        Node targetObject = target.getObject();
        switch (target.getTargetType()) {
            case targetClass:
            case implicitClass:
                return G.isOfType(data, node, targetObject);
            case targetNode:
                return targetObject.equals(node);
            case targetObjectsOf:
                return data.contains(null, targetObject, node);
            case targetSubjectsOf:
                return data.contains(node, targetObject, null);
            case targetExtension:
                // Ouch
                focusNodes(data, target).contains(node);
            default:
                return false;
        }
    }

    public static void evalConstraint(ValidationContext vCxt, Graph data, Shape shape, Node focusNode, Path path, Set<Node> pathNodes, Constraint c) {
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

}

