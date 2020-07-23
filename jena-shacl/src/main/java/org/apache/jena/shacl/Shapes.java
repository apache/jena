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

import static org.apache.jena.sparql.graph.NodeConst.nodeOwlImports;
import static org.apache.jena.sparql.graph.NodeConst.nodeOwlOntology;
import static org.apache.jena.sparql.graph.NodeConst.nodeRDFType;

import java.util.*;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.engine.Targets;
import org.apache.jena.shacl.lib.G;
import org.apache.jena.shacl.lib.RDFDataException;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.parser.ShapesParser;
import org.apache.jena.sys.JenaSystem;

/** A collection of shapes as output by the SHACL parser.
 * Usage:
 * <pre>
 *    Shapes myShapes = Shapes.parse(graph);
 * </pre>
 */
public class Shapes implements Iterable<Shape> {
    static { JenaSystem.init(); }

    private final Graph shapesGraph;
    //Map is Node to Shape.
    private final Map<Node, Shape> shapes;

    // Shapes with targets (including implicit class target).
    private final Collection<Shape> rootShapes;
    // Declared shapes, not in targetShapes.
    private final Collection<Shape> declShapes;
    private final Targets targets;

    private final Node shapesBase;
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

    /** Parse the graph and return the shapes connected to the targets. */
    public static Shapes parse(Graph graph) {
        return parseAll(graph);
    }

    /** Parse the graph and return the shapes connected to the targets. */
    public static Shapes parseTargets(Graph graph) {
        Targets targets = ShapesParser.targets(graph);
        Shapes shapes = parseProcess(graph, targets, Collections.emptyList());
        return shapes;
    }

    /**
     * Parse the graph and also include all declared (have rdf:type) node and property shapes
     * (i.e. have rdf:type sh:NodeShape or sh:PropertyShape)
     * whether connected to the targets or not.
     */
    private static Shapes parseAll(Graph graph) {
        Targets targets = ShapesParser.targets(graph);
        // May include targets if there are explicit node/property shapes and also have a target.
        Collection<Node> declShapes = ShapesParser.findDeclaredShapes(graph);
        Shapes shapes = parseProcess(graph, targets, declShapes);
        return shapes;
    }

    private static Shapes parseProcess(Graph shapesGraph, Targets targets, Collection<Node> declaredNodes) {
        Map<Node, Shape> shapesMap = new HashMap<>();
        // Returns shapes with targets.
        Collection<Shape> rootShapes = ShapesParser.parseShapes(shapesGraph, targets, shapesMap);

        // This skips declared+targets because the shapesMap is in common.
        declaredNodes.forEach(shapeNode -> {
            if ( !shapesMap.containsKey(shapeNode) ) {
                Shape shape = ShapesParser.parseShape(shapesMap, shapesGraph, shapeNode);
            }
        });

        // All declared shapes, without targets, so not a root shape.
        Collection<Shape> declShapes = new ArrayList<>();
        declaredNodes.forEach(shapeNode -> {
            if ( shapesMap.containsKey(shapeNode) ) {
                Shape sh = shapesMap.get(shapeNode);
                if ( ! rootShapes.contains(sh) )
                    declShapes.add(shapesMap.get(shapeNode));
            } else {
                throw new ShaclException("Failed to find shape for declared shape: "+shapeNode);
            }
        });

        return new Shapes(shapesGraph, shapesMap, targets, rootShapes, declShapes);
    }

    public Shapes(Graph shapesGraph, Map<Node, Shape> shapesMap, Targets targets,
                  Collection<Shape> rootShapes, Collection<Shape> declShapes) {
        this.shapesGraph = shapesGraph;
        this.targets = targets;
        this.shapes = shapesMap;
        this.rootShapes = rootShapes;
        this.declShapes = declShapes;

        Node _shapesBase = null;
        List<Node> _imports = null;
        // Extract base and imports.
        try {
            _shapesBase = G.getOnePO(shapesGraph, nodeRDFType, nodeOwlOntology);
            _imports = G.listSP(shapesGraph, _shapesBase, nodeOwlImports);
        } catch (RDFDataException ex) {}
        this.shapesBase = _shapesBase;
        this.imports = _imports;
    }

    public boolean isEmpty() {
        return shapes.isEmpty();
    }

    /** @deprecated Use {@link #getTargetShapes()} */
    @Deprecated
    public Collection<Shape> getRootShapes() {
        return getTargetShapes();
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

    public Shape getShape(Node node) {
        return shapes.get(node);
    }

    public Map<Node,Shape> getShapeMap() {
        return shapes;
    }

    public Graph getGraph() {
        return shapesGraph;
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

    /** Iterator over the shapes with targets and with explicit type NodeShape or PropertyShape. */
    public Iterator<Shape> iteratorAll() {
        // rootsShapes and declShaes are disjoint so no duplicates in the iterator.
        return Iter.concat(rootShapes.iterator(), declShapes.iterator());
    }
}
