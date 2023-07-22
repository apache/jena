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

package org.apache.jena.shacl;

import java.util.*;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.shacl.engine.Targets;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.parser.ShapesParser;
import org.apache.jena.shacl.parser.ShapesParser.ParserResult;
import org.apache.jena.sys.JenaSystem;

/**
 * A collection of shapes as output by the SHACL parser. Usage:
 *
 * <pre>
 * Shapes myShapes = Shapes.parse(graph);
 * </pre>
 */
public class Shapes implements Iterable<Shape> {
    static {
        JenaSystem.init();
    }

    // Keep for reference.
    private final ParserResult parserResult;
    private final Graph shapesGraph;
    private final Node shapesBase;
    private final PrefixMap prefixMap;
    // Map is Node to Shape.
    private final Map<Node, Shape> shapes;

    // Shapes with targets (including implicit class target).
    private final Collection<Shape> rootShapes;
    // Declared shapes, not in targetShapes.
    private final Collection<Shape> declShapes;
    // Shapes that are not declared shapes (by type), and not accessible from in targets.
    // This is placeholder.
    // It should be disjoint with rootShapes and declShapes.
    private final Collection<Shape> otherShapes;
    private final Targets targets;

    // Imports in the graph.
    private final List<Node> imports;
    /** Parse the model and return the shapes. */
    public static Shapes parse(Model model) {
        return parse(model.getGraph());
    }

    /** Load the file, parse the graph and return the shapes. */
    public static Shapes parse(String fileOrURL) {
        Graph g = RDFDataMgr.loadGraph(fileOrURL);
        return parse(g);
    }

    /** Load the file, parse the graph and return the shapes. */
    public static Shapes parse(String fileOrURL, boolean withImports) {
        Graph g = withImports ? Imports.loadWithImports(fileOrURL) : RDFDataMgr.loadGraph(fileOrURL);
        return parse(g);
    }

    /** Parse the graph and return the shapes connected to the targets. */
    public static Shapes parse(Graph graph) {
        return parseAll(graph);
    }

    /**
     * Parse the graph and return the shapes connected to the targets.
     *
     * @deprecated Use {@link #parse(Graph)}.
     */
    @Deprecated
    public static Shapes parseTargets(Graph graph) {
        Shapes shapes = parseProcess(graph, Collections.emptyList());
        return shapes;
    }

    /**
     * Parse the graph and also include all declared (have rdf:type) node and
     * property shapes (i.e. have rdf:type sh:NodeShape or sh:PropertyShape) whether
     * connected to the targets or not.
     */
    private static Shapes parseAll(Graph graph) {
        // May include targets if there are explicit node/property shapes and also
        // have a target.
        Collection<Node> declShapes = ShapesParser.findDeclaredShapes(graph);
        Shapes shapes = parseProcess(graph, declShapes);
        return shapes;
    }

    private static Shapes parseProcess(Graph shapesGraph, Collection<Node> declaredNodes) {
        ShapesParser.ParserResult x = ShapesParser.parseProcess(shapesGraph, declaredNodes);
        return new Shapes(shapesGraph, x);
    }

    private Shapes(Graph shapesGraph, ShapesParser.ParserResult x) {
        this.parserResult = x;
        this.shapesGraph = shapesGraph;
        this.prefixMap = Prefixes.adapt(shapesGraph);
        this.targets = x.targets;
        this.shapes = x.shapesMap;
        this.rootShapes = x.rootShapes;
        this.declShapes = x.declaredShapes;
        this.otherShapes = x.otherShapes;
        this.shapesBase = x.shapesBase;
        this.imports = x.imports;
        //x.sparqlConstraintComponents
        //x.targetExtensions
    }

//    private Shapes(Graph shapesGraph, Map<Node, Shape> shapesMap, Targets targets,
//                   Collection<Shape> rootShapes, Collection<Shape> declShapes) {
//        this.shapesGraph = shapesGraph;
//        this.targets = targets;
//        this.shapes = shapesMap;
//        this.rootShapes = rootShapes;
//        this.declShapes = declShapes;
//
//        // Extract base and imports.
//        Pair<Node, List<Node>> pair = Imports.baseAndImports(shapesGraph);
//        this.shapesBase = pair.getLeft();
//        this.imports = pair.getRight();
//    }

    public boolean isEmpty() {
        return shapes.isEmpty();
    }

    /** Return the shapes with targets. */
    public Collection<Shape> getTargetShapes() {
        return rootShapes;
    }

    public Collection<Node> getImports() {
        return imports;
    }

    public Node getBase() {
        return shapesBase;
    }

    public String getBaseURI() {
        if ( shapesBase == null || ! shapesBase.isURI() )
            return null;
        return shapesBase.getURI();
    }

    public PrefixMap getPrefixMap() {
        return prefixMap;
    }

    public Graph getGraph() {
        return shapesGraph;
    }

    public Shape getShape(Node node) {
        return shapes.get(node);
    }

    public Map<Node, Shape> getShapeMap() {
        return shapes;
    }

    public Targets getTargets() {
        return targets;
    }

    public int numShapes() {
        return shapes.values().size();
    }

    public int numRootShapes() {
        return rootShapes.size();
    }

    /** Iterator over the shapes with targets */
    @Override
    public Iterator<Shape> iterator() {
        return rootShapes.iterator();
    }

    /**
     * Iterator over the shapes with targets and with explicit type NodeShape or
     * PropertyShape.
     */
    public Iterator<Shape> iteratorAll() {
        // rootsShapes and declShapes are disjoint so no duplicates in the iterator.
        return
            Iter.iter(rootShapes.iterator())
                .append(declShapes.iterator())
                .append(otherShapes.iterator());
    }
}
