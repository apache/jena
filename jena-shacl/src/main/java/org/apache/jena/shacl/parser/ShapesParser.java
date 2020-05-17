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

import static org.apache.jena.shacl.engine.ShaclPaths.pathToString;
import static org.apache.jena.shacl.lib.G.*;
import static org.apache.jena.shacl.lib.ShLib.displayStr;
import static org.apache.jena.shacl.sys.C.rdfsClass;

import java.util.*;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shacl.engine.ShaclPaths;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.engine.TargetType;
import org.apache.jena.shacl.engine.Targets;
import org.apache.jena.shacl.lib.G;
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
    //private static Logger LOG = LoggerFactory.getLogger(ShapesParser.class);

    /** Return a list of "top shapes", those with targets.
     * The {@code shapesMap} is modified, adding in all shapes processed.
     */
    public static Collection<Shape> parseShapes(Graph shapesGraph, Targets targets, Map<Node, Shape> shapesMap) {
        Targets rootShapes = targets;

        if ( DEBUG )
            OUT.println("SparqlConstraintComponents");
        ConstraintComponents sparqlConstraintComponents = ConstraintComponents.parseSparqlConstraintComponents(shapesGraph);

        List<Shape> acc = new ArrayList<>();

        if ( DEBUG )
            OUT.println("sh:targetNodes");
        for ( Node shapeNode : rootShapes.targetNodes ) {
            parseRootShape(acc, shapesMap, shapesGraph, shapeNode);
        }

        if ( DEBUG )
            OUT.println("sh:targetClasses");
        for ( Node shapeNode : rootShapes.targetClasses ) {
            parseRootShape(acc, shapesMap, shapesGraph, shapeNode);
        }

        if ( DEBUG )
            OUT.println("targetObjectsOf");
        for ( Node shapeNode : rootShapes.targetObjectsOf ) {
            parseRootShape(acc, shapesMap, shapesGraph, shapeNode);
        }

        if ( DEBUG )
            OUT.println("targetSubjectsOf");
        for ( Node shapeNode : rootShapes.targetSubjectsOf ) {
            parseRootShape(acc, shapesMap, shapesGraph, shapeNode);
        }

        if ( DEBUG )
            OUT.println("implicitClassTargets");
        for ( Node shapeNode : rootShapes.implicitClassTargets ) {
            parseRootShape(acc, shapesMap, shapesGraph, shapeNode);
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
        //https://www.w3.org/TR/shacl/#syntax-rules
        // Note - we only have the reachable shapes in "shapesMap".

        return acc ;
    }

    /** Parse and add all the declared shapes into the map.
     * The {@code shapesMap} is modified, adding in all shapes processed.
     */
    public static Collection<Shape> declaredShapes(Graph shapesGraph, Map<Node, Shape> shapesMap) {
        // All declared shapes.
        List<Shape> acc = new ArrayList<>();
        G.listAllNodesOfType(shapesGraph, SHACL.NodeShape).forEach(shapeNode->
            parseRootShape(acc, shapesMap, shapesGraph, shapeNode));
        G.listAllNodesOfType(shapesGraph, SHACL.PropertyShape).forEach(shapeNode->
            parseRootShape(acc, shapesMap, shapesGraph, shapeNode));
        return acc;
    }

    private static void parseRootShape(List<Shape> acc, Map<Node, Shape> parsed, Graph shapesGraph, Node shNode) {
        if ( parsed.containsKey(shNode) )
            return ;
        if ( DEBUG )
            OUT.incIndent();
        Shape shape = parseShapeStep(parsed, shapesGraph, shNode);
        acc.add(shape);
        if ( DEBUG )
            OUT.decIndent();
    }

//    public static Shape parseShape(Graph shapesGraph, Map<Node, Shape> parsed, Node shNode) {
//        return parseShape(parsed, shapesGraph, shNode);
//    }

    /** Parse a specific shape from the Shapes graph */
    public static Shape parseShape(Graph shapesGraph, Node shNode) {
        // Avoid recursion.
        Map<Node, Shape> parsed = new HashMap<>();
        return parseShapeStep(parsed, shapesGraph, shNode);
    }

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
    /** parse a shape during a parsing process */
    /*package*/ static Shape parseShapeStep(Map<Node, Shape> parsed, Graph shapesGraph, Node shapeNode) {
        if ( parsed.containsKey(shapeNode) )
            return parsed.get(shapeNode);
        Shape shape = parseShape$(parsed, shapesGraph, shapeNode);
        parsed.put(shapeNode, shape);
        return shape;
    }

    private static Shape parseShape$(Map<Node, Shape> parsed, Graph shapesGraph, Node shapeNode) {
        if ( DEBUG )
            OUT.printf("Parse shape : %s\n", displayStr(shapeNode));
        boolean isDeactivated = absentOrOne(shapesGraph, shapeNode, SHACL.deactivated, NodeConst.nodeTrue);
        Collection<Target> targets = targets(shapesGraph, shapeNode);
        List<Constraint> constraints = Constraints.parseConstraints(shapesGraph, shapeNode, parsed);
        Severity severity = severity(shapesGraph, shapeNode);
        List<Node> messages = listSP(shapesGraph, shapeNode, SHACL.message);

        if ( DEBUG )
            OUT.incIndent();
        // sh:Property PropertyShapes from this shape.
        // sh:node is treated as a constraint.
        List<PropertyShape> propertyShapes = findPropertyShapes(parsed, shapesGraph, shapeNode);
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

    private static List<PropertyShape> findPropertyShapes(Map<Node, Shape> parsed, Graph shapesGraph, Node shapeNode) {
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
                throw new ShaclParseException("Muiltiple sh:path on a property shape: "+displayStr(shapeNode)+" sh:property"+displayStr(propertyShape)+ " : "+paths);
            }
            PropertyShape ps = (PropertyShape)parseShapeStep(parsed, shapesGraph, propertyShape);
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
