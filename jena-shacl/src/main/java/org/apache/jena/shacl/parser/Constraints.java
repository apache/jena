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

import static org.apache.jena.shacl.lib.ShLib.displayStr;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.riot.other.G;
import org.apache.jena.shacl.engine.SparqlConstraints;
import org.apache.jena.shacl.engine.constraint.*;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.sys.C;
import org.apache.jena.shacl.vocabulary.SHACL;

public class Constraints {

//    Core Constraint Components
//
//    4.1 Value Type Constraint Components
//        4.1.1 sh:class
//        4.1.2 sh:datatype
//        4.1.3 sh:nodeKind
//    4.2 Cardinality Constraint Components
//        4.2.1 sh:minCount
//        4.2.2 sh:maxCount
//    4.3 Value Range Constraint Components
//        4.3.1 sh:minExclusive
//        4.3.2 sh:minInclusive
//        4.3.3 sh:maxExclusive
//        4.3.4 sh:maxInclusive
//    4.4 String-based Constraint Components
//        4.4.1 sh:minLength
//        4.4.2 sh:maxLength
//        4.4.3 sh:pattern
//        4.4.4 sh:languageIn
//        4.4.5 sh:uniqueLang
//    4.5 Property Pair Constraint Components
//        4.5.1 sh:equals
//        4.5.2 sh:disjoint
//        4.5.3 sh:lessThan
//        4.5.4 sh:lessThanOrEquals
//    4.6 Logical Constraint Components
//        4.6.1 sh:not
//        4.6.2 sh:and
//        4.6.3 sh:or
//        4.6.4 sh:xone
//    4.7 Shape-based Constraint Components
//        4.7.1 sh:node
//        4.7.2 sh:property
//        4.7.3 sh:qualifiedValueShape, sh:qualifiedMinCount, sh:qualifiedMaxCount
//    4.8 Other Constraint Components
//        4.8.1 sh:closed, sh:ignoredProperties
//        4.8.2 sh:hasValue
//        4.8.3 sh:in

    // The constraints that need just a single triple.
    static Map<Node, ConstraintMaker> dispatch = new HashMap<>();
    static {
        dispatch.put( SHACL.class_,            (g, s, p, o) -> new ClassConstraint(o)    );
        dispatch.put( SHACL.datatype,          (g, s, p, o) -> new DatatypeConstraint(o) );
        dispatch.put( SHACL.nodeKind,          (g, s, p, o) -> new NodeKindConstraint(o) );
        dispatch.put( SHACL.minCount,          (g, s, p, o) -> new MinCount(intValue(o)) );
        dispatch.put( SHACL.maxCount,          (g, s, p, o) -> new MaxCount(intValue(o)) );

        dispatch.put( SHACL.minInclusive,      (g, s, p, o) -> new ValueMinInclusiveConstraint(o) );
        dispatch.put( SHACL.minExclusive,      (g, s, p, o) -> new ValueMinExclusiveConstraint(o) );
        dispatch.put( SHACL.maxInclusive,      (g, s, p, o) -> new ValueMaxInclusiveConstraint(o) );
        dispatch.put( SHACL.maxExclusive,      (g, s, p, o) -> new ValueMaxExclusiveConstraint(o) );

        dispatch.put( SHACL.minLength,         (g, s, p, o) -> new StrMinLengthConstraint(intValue(o)) );
        dispatch.put( SHACL.maxLength,         (g, s, p, o) -> new StrMaxLengthConstraint(intValue(o)) );
        // Below
        //dispatch.put( SHACL.pattern,           (g, p, o) -> notImplemented(p) );
        dispatch.put( SHACL.languageIn,        (g, s, p, o) -> new StrLanguageIn(listString(g, o)) );
        dispatch.put( SHACL.uniqueLang,        (g, s, p, o) -> new UniqueLangConstraint(booleanValueStrict(o)) );

        dispatch.put( SHACL.hasValue,          (g, s, p, o) -> new HasValueConstraint(o) );
        dispatch.put( SHACL.in,                (g, s, p, o) -> new InConstraint(list(g,o)) );
        dispatch.put( SHACL.closed,            (g, s, p, o) -> new ClosedConstraint(g,s,booleanValue(o)) );

        dispatch.put( SHACL.equals,            (g, s, p, o) -> new EqualsConstraint( checkObjectIRI(g, s, p, o)) );
        dispatch.put( SHACL.disjoint,          (g, s, p, o) -> new DisjointConstraint( checkObjectIRI(g, s, p, o)) );
        dispatch.put( SHACL.lessThan,          (g, s, p, o) -> new LessThanConstraint( checkObjectIRI(g, s, p, o)) );
        dispatch.put( SHACL.lessThanOrEquals,  (g, s, p, o) -> new LessThanOrEqualsConstraint( checkObjectIRI(g, s, p, o)) );

        // Below
        //dispatch.put( SHACL.not,                (g, s, p, o) -> notImplemented(p) );
        //dispatch.put( SHACL.and,                (g, s, p, o) -> notImplemented(p) );
        //dispatch.put( SHACL.or,                 (g, s, p, o) -> notImplemented(p) );
        //dispatch.put( SHACL.xone,               (g, s, p, o) -> notImplemented(p) );
        //dispatch.put( SHACL.node,               (g, s, p, o) -> notImplemented(p) );

        dispatch.put(SHACL.sparql, (g, s, p, o) -> SparqlConstraints.parseSparqlConstraint(g, s, p, o) );
    }

    /**
     * The constraints that just need an input node, and do not look in the data.
     * For example, minCount is not here because needs all the instances to count them.
     */
    static Set<Node> immediate = new HashSet<>();
    static {
        immediate.add(SHACL.datatype);
        immediate.add(SHACL.nodeKind);
        // Value
        immediate.add(SHACL.minExclusive);
        immediate.add(SHACL.minInclusive);
        immediate.add(SHACL.maxExclusive);
        immediate.add(SHACL.maxInclusive);
        // String
        immediate.add(SHACL.minLength);
        immediate.add(SHACL.maxLength);
        immediate.add(SHACL.languageIn);
        //
        immediate.add(SHACL.in);
        immediate.add(SHACL.pattern);
    }

    /**
     * Entry point. Process all triples of a specific shape node (subject). Has
     * access to map of parsed shapes so it can recursively call back into the shapes
     * parser at when the constraint uses other shapes
     * (sh:and/sh:or/sh:not/sh:xone.sh:node).
     */
    /*package*/ static List<Constraint> parseConstraints(Graph shapesGraph, Node shape, Map<Node, Shape> parsed, Set<Node> traversed) {
        List<Constraint> constraints = new ArrayList<>();
        Iterator<Triple> iter = G.find(shapesGraph, shape, null, null);
        while(iter.hasNext()) {
            Triple t = iter.next();
            Node p = t.getPredicate();
            // The parser handles sh:property specially as a PropertyShape.
            if ( SHACL.property.equals(p) )
                continue;
            if ( SHACL.path.equals(p) )
                continue;
            Node s = t.getSubject();
            Node o = t.getObject();
            Constraint c = parseConstraint(shapesGraph, s, p, o, parsed, traversed);
            if ( c != null )
                constraints.add(c);
        }
        return constraints;
    }

    /**
     * The translate of an RDF triple into a {@link Constraint}.
     * Constraints require more that just the triple being inspected.
     */
    private static Constraint parseConstraint(Graph g, Node s, Node p, Node o, Map<Node, Shape> parsed, Set<Node> traversed) {

        // Test for single triple constraints.
        ConstraintMaker maker = dispatch.get(p);
        if ( maker != null )
            return maker.make(g, s, p, o);

        // These require the "parsed" map.
        if ( p.equals(SHACL.not) ) {
            Shape shape = ShapesParser.parseShapeStep(traversed, parsed, g, o);
            return new ShNot(shape);
        }

        if ( p.equals(SHACL.or) ) {
            List<Node> elts = list(g, o);
            List<Shape> shapes = elts.stream().map(x->ShapesParser.parseShapeStep(traversed, parsed, g, x)).collect(Collectors.toList());
            return new ShOr(shapes);
        }
        if ( p.equals(SHACL.and) ) {
            List<Node> elts = list(g, o);
            List<Shape> shapes = elts.stream().map(x->ShapesParser.parseShapeStep(traversed, parsed, g, x)).collect(Collectors.toList());
            return new ShAnd(shapes);
        }

        if ( p.equals(SHACL.xone) ) {
            List<Node> elts = list(g, o);
            List<Shape> shapes = elts.stream().map(x->ShapesParser.parseShapeStep(traversed, parsed, g, x)).collect(Collectors.toList());
            return new ShXone(shapes);
        }

        if ( p.equals(SHACL.node) ) {
            Shape other = ShapesParser.parseShapeStep(traversed, parsed, g, o);
            if ( other instanceof PropertyShape )
                throw new ShaclParseException("Object of sh:node must be a node shape, not a property shape");
            return new ShNode(other);
        }

        // sh:pattern is influenced by an adjacent sh:flags.
        if ( p.equals(SHACL.pattern) ) {
            Node pat = o;
            if ( ! Util.isSimpleString(pat) )
                throw new ShaclParseException("Pattern is not a string: Node = "+displayStr(s)+" : Pattern = "+displayStr(pat) );
            Node flagsNode = G.getSP(g, s, SHACL.flags);
            if ( flagsNode != null && ! Util.isSimpleString(flagsNode) )
                throw new ShaclParseException("Pattern flags not a string: Node = "+displayStr(s)+" : Pattern = "+displayStr(flagsNode) );
            return new PatternConstraint(pat.getLiteralLexicalForm(), (flagsNode!=null)?flagsNode.getLiteralLexicalForm(): null);
        }

        // Known component parameters.
        if ( p.equals(SHACL.ignoredProperties) )
            return null;

        if ( p.equals(SHACL.qualifiedValueShape) )
            return parseQualifiedValueShape(g, s, p, o, parsed, traversed);

        // sh:qualifiedValueShape parameters.
        if ( p.equals(SHACL.QualifiedMinCountConstraintComponent) ||
             p.equals(SHACL.QualifiedMaxCountConstraintComponent) ||
             p.equals(SHACL.qualifiedValueShapesDisjoint) )
            return null;

        // Non-Validating Property Shape Characteristics - in ShapesParser.parseShape$
//      if ( p.equals(SHACL.group) )         return null;
//      if ( p.equals(SHACL.name) )          return null;
//      if ( p.equals(SHACL.description) )   return null;
//      if ( p.equals(SHACL.defaultValue) )  return null;
//      if ( p.equals(SHACL.order) )         return null;

        if ( p.equals(SHACL.path ) )
            throw new ShaclParseException("Unexpected constraint: "+displayStr(p)+" on "+s);
        if ( p.equals(SHACL.property) )
            throw new ShaclParseException("Unexpected constraint: "+displayStr(p)+" on "+s);

        return null;
    }

    private static Constraint parseQualifiedValueShape(Graph g, Node s, Node p, Node o, Map<Node, Shape> parsed, Set<Node> traversed) {
        Shape sub = ShapesParser.parseShapeStep(traversed, parsed, g, o);
        // [PARSE] Syntax check needed
        Node qMin = G.getZeroOrOneSP(g, s, SHACL.qualifiedMinCount);
        Node qMax = G.getZeroOrOneSP(g, s, SHACL.qualifiedMaxCount);
        int vMin = intValue(qMin, -1);
        int vMax = intValue(qMax, -1);
        Node qDisjoint = G.getZeroOrOneSP(g, s, SHACL.qualifiedValueShapesDisjoint);
        if ( vMin < 0 && vMax < 0 )
            throw new ShaclParseException("At least one of sh:qualifiedMinCount and sh:qualifiedMaxCount required");
        return new QualifiedValueShape(sub, intValue(qMin, -1), intValue(qMax, -1), booleanValueStrict(qDisjoint)) ;
    }

    interface ConstraintMaker {
        Constraint make(Graph g, Node s, Node p, Node o);
    }

    private static Constraint notImplemented(Node p) {
        throw new NotImplemented(ShLib.displayStr(p));
    }

    static Node checkObjectIRI(Graph g, Node shape, Node p, Node o) {
        if ( ! o.isURI() )
            throw new ShaclParseException("IRI required: "+displayStr(o) + " at "+shape+" "+displayStr(p));
        return o;
    }

    static int intValue(Node n) {
        return ((Integer)(n.getLiteralValue())).intValue();
    }

    static int intValue(Node n, int dftValue) {
        if ( n == null )
            return dftValue;
        return intValue(n);
    }

    private static boolean booleanValueStrict(Node node) {
        // Only "true"^^xsd:boolean is in the spec.
        return C.TRUE.equals(node);
    }

    private static boolean booleanValue(Node node) {
        return (Boolean)node.getLiteralValue();
    }

    /** Return the list elements of an RDF list start at {@code node} */
    private static List<Node> list(Graph g, Node node) {
        return G.rdfList(g, node);
    }

    private static List<String> listString(Graph g, Node node) {
        List<Node> elts = list(g, node);
        return elts.stream().map(n -> {
            if ( ! Util.isSimpleString(n) )
                throw new ShaclParseException("Not a string "+displayStr(n)+" in list "+elts);
            return n.getLiteralLexicalForm();
        }).collect(Collectors.toList());
    }
}
