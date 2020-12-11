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

package org.apache.jena.shacl.parser;

import static org.apache.jena.riot.other.G.*;
import static org.apache.jena.shacl.engine.ShaclPaths.pathToString;
import static org.apache.jena.shacl.lib.ShLib.displayStr;
import static org.apache.jena.shacl.sys.C.rdfsClass;

import java.util.*;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.other.G;
import org.apache.jena.riot.other.RDFDataException;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.engine.ShaclPaths;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.engine.TargetType;
import org.apache.jena.shacl.engine.Targets;
import org.apache.jena.shacl.engine.constraint.JLogConstraint;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.sys.ShaclSystem;
import org.apache.jena.shacl.validation.Severity;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.util.iterator.ExtendedIterator;

public class ShapesParser {

    public static Targets targets(Graph shapesGraph) {
        return Targets.create(shapesGraph);
    }

    static class ParserState {
        Targets rootShapes;
        ConstraintComponents sparqlConstraintComponents;
        Map<Node, Shape> shapesMap;
    }

    private static final boolean DEBUG = false;
    private static IndentedWriter OUT = IndentedWriter.stdout;

    /**
     * Parse, starting from the given targets.
     * The {@code shapesMap} is modified, adding in all shapes processed.
     * <p>
     * Applications should call functions in {@link Shapes} rather than call the parser directly.
     */
    public static Collection<Shape> parseShapes(Graph shapesGraph, Targets targets, Map<Node, Shape> shapesMap) {
        // Cycle detection.
        Set<Node> cycles = new HashSet<>();
        return parseShapes(shapesGraph, targets, shapesMap, cycles);
    }

    /*package*/ static Collection<Shape> parseShapes(Graph shapesGraph, Targets targets, Map<Node, Shape> shapesMap, Set<Node> cycles) {

        Targets rootShapes = targets;

        if ( DEBUG )
            OUT.println("SparqlConstraintComponents");
        ConstraintComponents sparqlConstraintComponents = ConstraintComponents.parseSparqlConstraintComponents(shapesGraph);

        // LinkedHashSet - convenience, so shapes are kept in
        // the order of discovery below.
        // Record all added by name.
        // If a shape has two targets, include once.
        // If a shape with target that refers to another shape with target, include both shapes.
        // We want both as top-level shapes.
        Map<Node, Shape> acc = new LinkedHashMap<>();

        if ( DEBUG )
            OUT.println("sh:targetNodes");
        for ( Node shapeNode : rootShapes.targetNodes ) {
            parseShapeAcc(acc, shapesMap, shapesGraph, shapeNode);
        }

        if ( DEBUG )
            OUT.println("sh:targetClasses");
        for ( Node shapeNode : rootShapes.targetClasses ) {
            parseShapeAcc(acc, shapesMap, shapesGraph, shapeNode);
        }

        if ( DEBUG )
            OUT.println("targetObjectsOf");
        for ( Node shapeNode : rootShapes.targetObjectsOf ) {
            parseShapeAcc(acc, shapesMap, shapesGraph, shapeNode);
        }

        if ( DEBUG )
            OUT.println("targetSubjectsOf");
        for ( Node shapeNode : rootShapes.targetSubjectsOf ) {
            parseShapeAcc(acc, shapesMap, shapesGraph, shapeNode);
        }

        if ( DEBUG )
            OUT.println("implicitClassTargets");
        for ( Node shapeNode : rootShapes.implicitClassTargets ) {
            parseShapeAcc(acc, shapesMap, shapesGraph, shapeNode);
        }

        if ( DEBUG )
            OUT.println("ConstraintComponents");
        if ( sparqlConstraintComponents != null && ! sparqlConstraintComponents.isEmpty() ) {
            // Deep for all shapes.
            shapesMap.values().forEach(shape->{
                List<Constraint> x = ConstraintComponents.processShape(shapesGraph, sparqlConstraintComponents, shape);
                if ( x != null && !x.isEmpty() ) {
                    shape.getConstraints().addAll(x);
                }
            });
        }

        // Syntax rules for well-formed shapes.
        //   https://www.w3.org/TR/shacl/#syntax-rules
        return shapes(acc) ;
    }

    /** Find all names for shapes with explicit type NodeShape or PropertyShape. */
    public static Collection<Node> findDeclaredShapes(Graph shapesGraph) {
        Set<Node> declared = new HashSet<>();
        G.allNodesOfTypeRDFS(shapesGraph, SHACL.NodeShape).forEach(declared::add);
        G.allNodesOfTypeRDFS(shapesGraph, SHACL.PropertyShape).forEach(declared::add);
        return declared;
    }

    private static Collection<Shape> shapes(Map<Node, Shape> acc) {
        // The list will be in insertion order.
        return new ArrayList<>(acc.values());
    }

    /**
     * Parse shape "shNode". If it has been parsed before, simply return.
     * If not, parse, adding to the overall map of parsed shapes
     * "parsed" and also add it to the accumulator map.
     * <p>
     * Used by the targets stage.
     */
    private static Shape parseShapeAcc(Map<Node, Shape> acc, Map<Node, Shape> shapesMap, Graph shapesGraph, Node shNode) {
        if ( acc.containsKey(shNode) )
            return acc.get(shNode);
        if ( DEBUG )
            OUT.incIndent();
        Shape shape = parseShape(shapesMap, shapesGraph, shNode);
        if ( acc != null )
            acc.put(shNode, shape);
        if ( DEBUG )
            OUT.decIndent();
        return shape;
    }

    // ---- Main parser worker.
    /**
     *  Parse one shape, updating the record of shapes already parsed.
     *
     * @param shapesMap
     * @param shapesGraph
     * @param shNode
     * @return Shape
     */

    public static Shape parseShape(Map<Node, Shape> shapesMap, Graph shapesGraph, Node shNode) {
        Set<Node> traversed = new HashSet<>();
        Shape shape = parseShapeStep(traversed, shapesMap, shapesGraph, shNode);
        return shape;
    }

//    /** Parse a specific shape from the Shapes graph */
//    private static Shape parseShape(Graph shapesGraph, Node shNode) {
//        // Avoid recursion.
//        Map<Node, Shape> parsed = new HashMap<>();
//        return parseShapeStep(parsed, shapesGraph, shNode);
//    }

    /*
    ?X rdfs:domain sh:Shape
    -----------------------
    | X                   |
    =======================
    | sh:targetClass      |
    | sh:targetSubjectsOf |
    | sh:targetObjectsOf  |
    | sh:severity         |
    | sh:property         |
    | sh:targetNode       |
    | sh:sparql           |
    | sh:target           |
    | sh:rule             |
    -----------------------
     */
    /*
    ?X rdfs:domain sh:NodeShape
    -----
    | X |
    =====
    -----
     */
    /*
    ?X rdfs:domain sh:PropertyShape
    -------------------
    | X               |
    ===================
    | sh:name         |
    | sh:group        |
    | sh:description  |
    | sh:defaultValue |
    | sh:path         |
    -------------------
     */

    /** Do nothing placeholder shape. */
    static Shape unshape(Graph shapesGraph, Node shapeNode) { return
            new NodeShape(shapesGraph, shapeNode, false, Severity.Violation,
                          Collections.emptySet(), Collections.emptySet(),
                          Collections.singleton(new JLogConstraint("Cycle")),
                          Collections.emptySet());
    }

    /** parse a shape during a parsing process */
    /*package*/ static Shape parseShapeStep(Set<Node> traversed, Map<Node, Shape> parsed, Graph shapesGraph, Node shapeNode) {
        try {
            // Called by Constraints
            if ( parsed.containsKey(shapeNode) )
                return parsed.get(shapeNode);
            // Loop detection. Do before parsing.
            if ( traversed.contains(shapeNode) ) {
                ShaclSystem.systemShaclLogger.warn("Cycle detected : node "+ShLib.displayStr(shapeNode));
                // Put in a substitute shape.
                return unshape(shapesGraph, shapeNode);
            }

            traversed.add(shapeNode);
            Shape shape = parseShape$(traversed, parsed, shapesGraph, shapeNode);
            parsed.put(shapeNode, shape);
            traversed.remove(shapeNode);
            return shape;
        } catch (RDFDataException ex) {
            throw new ShaclParseException(ex.getMessage());
        }
    }

    private static Shape parseShape$(Set<Node> traversed, Map<Node, Shape> parsed, Graph shapesGraph, Node shapeNode) {
        if ( DEBUG )
            OUT.printf("Parse shape : %s\n", displayStr(shapeNode));
        boolean isDeactivated = contains(shapesGraph, shapeNode, SHACL.deactivated, NodeConst.nodeTrue);
        Collection<Target> targets = targets(shapesGraph, shapeNode);
        List<Constraint> constraints = Constraints.parseConstraints(shapesGraph, shapeNode, parsed, traversed);
        Severity severity = severity(shapesGraph, shapeNode);
        List<Node> messages = listSP(shapesGraph, shapeNode, SHACL.message);

        if ( DEBUG )
            OUT.incIndent();
        // sh:Property PropertyShapes from this shape.
        // sh:node is treated as a constraint.
        List<PropertyShape> propertyShapes = findPropertyShapes(traversed, parsed, shapesGraph, shapeNode);
        if ( DEBUG )
            OUT.decIndent();

        boolean isPropertyShape = contains(shapesGraph, shapeNode, SHACL.path, Node.ANY);
        if ( ! isPropertyShape ) {
            if ( DEBUG )
                OUT.printf("Node shape %s\n", displayStr(shapeNode));
            return new NodeShape(shapesGraph, shapeNode, isDeactivated, severity, messages, targets, constraints, propertyShapes);
        }

        // -- Property shape.

        if ( DEBUG )
            OUT.incIndent();
        Node pathNode = getOneSP(shapesGraph, shapeNode, SHACL.path);
        Path path = parsePath(shapesGraph, pathNode);
        if ( DEBUG )
            OUT.printf("Property shape: path = %s\n", pathToString(shapesGraph, path));
        // 2.3.2 Non-Validating Property Shape Characteristics
        // 2.3.2.1 sh:name and sh:description
        // 2.3.2.2 sh:order
        // 2.3.2.3 sh:group
        // 2.3.2.4 sh:defaultValue
        // sh:order and sh:defaultValue - unique.

        List<Node> names = listSP(shapesGraph, shapeNode, SHACL.name);
        List<Node> descriptions = listSP(shapesGraph, shapeNode, SHACL.description);
        List<Node> groups = listSP(shapesGraph, shapeNode, SHACL.group);
        Node defaultValue = G.getZeroOrOneSP(shapesGraph, shapeNode, SHACL.defaultValue);
        // The values of sh:order are decimals. "maybe used on any type of subject" but section is "property shapes".
        // We allow more than decimals.
        Node order = G.getZeroOrOneSP(shapesGraph, shapeNode, SHACL.order);
        if ( order != null && ! isDecimalCompatible(order) )
            throw new ShaclParseException("Not an xsd:decimal for sh:order");
        if ( DEBUG ) {
            OUT.printf("Property shape %s\n", displayStr(shapeNode));
            OUT.decIndent();
        }
        return new PropertyShape(shapesGraph, shapeNode, isDeactivated, severity, messages, targets, path, constraints, propertyShapes);
    }

    /* Some of the data types that are derived from xsd:decimal (not all of them) */
    private static Set<RDFDatatype> decimalCompatible = new HashSet<>();
    static {
        decimalCompatible.add(XSDDatatype.XSDdecimal);
        decimalCompatible.add(XSDDatatype.XSDinteger);
        decimalCompatible.add(XSDDatatype.XSDlong);
        decimalCompatible.add(XSDDatatype.XSDint);
    }

    private static boolean isDecimalCompatible(Node node) {
        try {
            RDFDatatype dt = node.getLiteralDatatype();
            return decimalCompatible.contains(dt);
        } catch (JenaException ex) { return false; }
    }

    private static Path parsePath(Graph shapesGraph, Node node) {
        return ShaclPaths.parsePath(shapesGraph, node);
    }

    private static List<PropertyShape> findPropertyShapes(Set<Node> traversed, Map<Node, Shape> parsed, Graph shapesGraph, Node shapeNode) {
        List<Triple> propertyTriples = G.find(shapesGraph, shapeNode, SHACL.property, null).toList();
        List<PropertyShape> propertyShapes = new ArrayList<>();
        for ( Triple t : propertyTriples) {
            // Must be a property shape.
            Node propertyShape = object(t);

            long x = countSP(shapesGraph, propertyShape, SHACL.path);
            if ( x == 0 ) {
                // Is it a typo? -> Can we find it as a subject?
                boolean existsAsSubject = G.contains(shapesGraph, propertyShape, null,null);
                if ( ! existsAsSubject )
                    throw new ShaclParseException("Missing property shape: node="+displayStr(shapeNode)+" sh:property "+displayStr(propertyShape));
                else
                    throw new ShaclParseException("No sh:path on a property shape: node="+displayStr(shapeNode)+" sh:property "+displayStr(propertyShape));
            }
            if ( x > 1 ) {
                List<Node> paths = listSP(shapesGraph, propertyShape, SHACL.path);
                throw new ShaclParseException("Multiple sh:path on a property shape: "+displayStr(shapeNode)+" sh:property"+displayStr(propertyShape)+ " : "+paths);
            }
            PropertyShape ps = (PropertyShape)parseShapeStep(traversed, parsed, shapesGraph, propertyShape);
            propertyShapes.add(ps);
        }
        return propertyShapes;
    }

    private static Collection<Target> targets(Graph shapesGraph, Node shape) {
        //sh:targetClass : rdfs:Class
        //sh:targetNode : any IRI or literal
        //sh:targetObjectsOf : rdf:Property
        //sh:targetSubjectsOf : rdf:Property

        List<Target> x = new ArrayList<>();

        accTarget(x, shapesGraph, shape, TargetType.targetNode);
        accTarget(x, shapesGraph, shape, TargetType.targetClass);
        accTarget(x, shapesGraph, shape, TargetType.targetObjectsOf);
        accTarget(x, shapesGraph, shape, TargetType.targetSubjectsOf);

        // TargetType.implicitClass : some overlap with TargetOps.implicitClassTargets
        // Explicitly sh:NodeShape or sh:PropertyShape and also subClassof* rdfs:Class.
        if ( isShapeType(shapesGraph, shape) && isOfType(shapesGraph, shape, rdfsClass) )
            x.add(Target.create(TargetType.implicitClass, shape));
        return x;
    }

    private static boolean isShapeType(Graph shapesGraph, Node shape) {
        return hasType(shapesGraph, shape, SHACL.NodeShape) || hasType(shapesGraph, shape, SHACL.PropertyShape);
    }

    private static Severity severity(Graph shapesGraph, Node shNode) {
        Node sev = G.getSP(shapesGraph, shNode, SHACL.severity);
        if ( sev == null )
            return Severity.Violation;
        return Severity.create(sev);
    }

    private static void accTarget(Collection<Target> acc, Graph shapesGraph, Node shape, TargetType targetType) {
        ExtendedIterator<Triple> iter = shapesGraph.find(shape, targetType.predicate, null);
        try {
            iter.mapWith(triple->Target.create(targetType, triple.getObject()))
                .forEachRemaining(target->acc.add(target));
        } finally { iter.close(); }
    }
}
