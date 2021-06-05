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

package org.apache.jena.shacl.engine;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.other.G;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.vocabulary.SHACL;

/** Analyse and record the various kinds of shapes an targets */
public class Targets {
    /*
    2.1.3 Targets
    2.1.3.1 Node targets (sh:targetNode)
    2.1.3.2 Class-based Targets (sh:targetClass)
    2.1.3.3 Implicit Class Targets
    2.1.3.4 Subjects-of targets (sh:targetSubjectsOf)
    2.1.3.5 Objects-of targets (sh:targetObjectsOf)
    */

    public Graph        shapesGraph;

    public Set<Node>    targetNodes;
    public Set<Node>    targetClasses;

    public Set<Node>    targetObjectsOf;
    // Derived: the properties of targetObjectsOf shapes.
    public Set<Node>    propertyTargetObjectsOf;

    public Set<Node>    targetSubjectsOf;
    // Derived: the properties of targetSubjectsOf shapes.
    public Set<Node>    propertyTargetSubjectsOf;

    // 2.1.3.3 Implicit Class Targets
    public Set<Node>    implicitClassTargets;

    // SPARQL-AF: 3. Custom Targets
    // Any target that uses sh:target such as:
    // 3.1  SPARQL-based Targets
    public Set<Node>    targetExtension;
    // 3.2 SPARQL-based Target Types
    //public Set<Node>    targetTypeSPARQL;

    public Set<Node>    allTargets;

    public static Targets create(Graph shapesGraph) {
        Targets targets = new Targets();
        targets.shapesGraph = shapesGraph;
        // sh:targetNode
        targets.targetNodes             = TargetOps.shapesTargetNode(shapesGraph);
        // sh:targetClass
        targets.targetClasses           = TargetOps.shapesTargetClass(shapesGraph);
        // sh:targetObjectsOf
        targets.targetObjectsOf         = TargetOps.shapesTargetObjectsOf(shapesGraph);
        // sh:targetSubjectsOf
        targets.targetSubjectsOf        = TargetOps.shapesTargetSubjectsOf(shapesGraph);
        // ?X rdf:type sh:NodeShape
        targets.implicitClassTargets    = TargetOps.implicitClassTargets(shapesGraph);

        // SHACL-AF
        // sh:target [ a sh:SPARQLTarget ; ... ]
        targets.targetExtension         = TargetOps.shapesTargetExtension(shapesGraph);

        // Derived: Calculate the set of all target predicates of all targetObjectsOf and targetSubjectsOf.
        targets.propertyTargetObjectsOf   = targetPredicatesOf(shapesGraph, SHACL.targetObjectsOf, targets.targetObjectsOf);
        targets.propertyTargetSubjectsOf  = targetPredicatesOf(shapesGraph, SHACL.targetSubjectsOf, targets.targetSubjectsOf);

        Set<Node> allTargets = new HashSet<>();
        allTargets.addAll(targets.targetNodes);
        allTargets.addAll(targets.targetClasses);
        allTargets.addAll(targets.targetObjectsOf);
        allTargets.addAll(targets.targetSubjectsOf);
        allTargets.addAll(targets.implicitClassTargets);
        allTargets.addAll(targets.targetExtension);
        targets.allTargets = allTargets;
        return targets;
    }

    public Graph getShapesGraph() {
        return shapesGraph;
    }

    public Set<Node> getTargetNodes() {
        return targetNodes;
    }

    public Set<Node> getTargetClasses() {
        return targetClasses;
    }

    public Set<Node> getTargetObjectsOf() {
        return targetObjectsOf;
    }

    public Set<Node> getPropertyTargetObjectsOf() {
        return propertyTargetObjectsOf;
    }

    public Set<Node> getTargetSubjectsOf() {
        return targetSubjectsOf;
    }

    public Set<Node> getPropertyTargetSubjectsOf() {
        return propertyTargetSubjectsOf;
    }

    public Set<Node> getClassNodeShape() {
        return implicitClassTargets;
    }

    public Set<Node> getTargetExt() {
        return targetExtension;
    }

    private Targets() {}

    public Set<Node> targets() {
        return allTargets;
    }

    /** Calculate the targets of a "targets of predicate" (sh:targetObjectsOf or sh:targetSubjectsOf) */
    /*package*/ static Set<Node> targetPredicatesOf(Graph shapesGraph, Node predicate, Set<Node> targetsOf) {
        if ( ! predicate.equals(SHACL.targetObjectsOf) && ! predicate.equals(SHACL.targetSubjectsOf) )
            throw new IllegalArgumentException(ShLib.displayStr(predicate));

//        // Flat map.
//        Set<Node> x = new HashSet<>();
//        targetOf.forEach(shape->{
//            List<Node> targetPredicates = G.listSP(shapesGraph, shape, predicate);
//            x.addAll(targetPredicates);
//            //shapesGraph.find(shape, predicate, null).mapWith(Triple::getObject).forEachRemaining(x::add);
//        });
        Set<Node> x =
            targetsOf.stream()
                .flatMap(shape->G.listSP(shapesGraph, shape, predicate).stream())
                .collect(Collectors.toSet());
        return x;
    }

    public void output(OutputStream out) {
        PrintStream pOut = new PrintStream(out);
        pOut.println("targetNode:       " + this.targetNodes);
        pOut.println("targetClass:      " + this.targetClasses);
        pOut.println("targetObjectsOf:  " + this.targetObjectsOf);
        pOut.println("targetSubjectsOf: " + this.targetSubjectsOf);
        pOut.println("sh:NodeShape:     " + this.implicitClassTargets);
        pOut.flush();
    }

    // Share utilities with FocusNodes.

    /* 2.2 NodeShape
     *
     * A node shape is a shape in the shapes graph that is not the subject of a triple
     * with sh:path as its predicate. It is recommended, but not required, for a node
     * shape to be declared as a SHACL instance of sh:NodeShape. SHACL instances of
     * sh:NodeShape cannot have a value for the property sh:path.
     */

//    private static void nodeShapes(Graph shapesGraph) {
//        Shapes shapes = Shapes.create(shapesGraph);
//    }

    /* PropertyShape
     *
     * A property shape is a shape in the shapes graph that is the subject of a triple
     * that has sh:path as its predicate. A shape has at most one value for sh:path. Each
     * value of sh:path in a shape must be a well-formed SHACL property path. It is
     * recommended, but not required, for a property shape to be declared as a SHACL
     * instance of sh:PropertyShape. SHACL instances of sh:PropertyShape have one value
     * for the property sh:path.
     */
}
