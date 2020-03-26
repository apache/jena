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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.shacl.lib.G;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.parser.ShaclParseException;
import org.apache.jena.shacl.sys.C;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.path.*;
import org.apache.jena.sparql.path.eval.PathEval;

public class ShaclPaths {
//  SPARQL Property path: ex:parent
//  SHACL Property path: ex:parent
//
//  SPARQL Property path: ^ex:parent
//  SHACL Property path: [ sh:inversePath ex:parent ]
//
//  SPARQL Property path: ex:parent/ex:firstName
//  SHACL Property path: ( ex:parent ex:firstName )
//
//  SPARQL Property path: rdf:type/rdfs:subClassOf*
//  SHACL Property path: ( rdf:type [ sh:zeroOrMorePath rdfs:subClassOf ] )
//
//  SPARQL Property path: ex:father|ex:mother
//  SHACL Property path: [ sh:alternativePath ( ex:father ex:mother  ) ]

//  2.3.1 SHACL Property Paths
//  2.3.1.1 Predicate Paths
//  2.3.1.2 Sequence Paths
//  2.3.1.3 Alternative Paths
//  2.3.1.4 Inverse Paths
//  2.3.1.5 Zero-Or-More Paths
//  2.3.1.6 One-Or-More Paths
//  2.3.1.7 Zero-Or-One Paths

    public static Set<Node> valueNodes(Graph graph, Node node, Path path) {
        if ( path instanceof P_Link ) {
            // Fast path common case.
            Node p = ((P_Link)path).getNode();
            return G.setSP(graph, node, p);
        }
        // Value nodes are a set.
        return Iter.toSet(pathReachIter(graph, node, path));
    }

    private static Iterator<Node> pathReachIter(Graph graph, Node node, Path path) {
        if ( path instanceof P_Link ) {
            // Fast path common case.
            Node p = ((P_Link)path).getNode();
            // Not an extended iterator.
            return G.iterSP(graph, node, p);
        }
        return PathEval.eval(graph, node, path, null);
    }

    private static void toRDF(Graph graph, Node node, Path path) {
        throw new NotImplemented();
    }

    public static Path parsePath(Graph graph, Node node) {
        return path(graph, node);
    }

    // XXX Better error checking.
    private static Path path(Graph graph, Node node) {
        if ( node.isURI() && ! C.NIL.equals(node) )
            return PathFactory.pathLink(node);

        if ( isList(graph, node) ) {
            List<Node> nodes = G.rdfList(graph, node);
            if ( nodes.isEmpty() )
                throw new ShaclParseException("Empty list for path sequence");
            Path path = null;
            // Left deep. Like the jena path parser.
            for ( Node n : nodes ) {
                Path p = path(graph, n);
                if ( path == null )
                    path = p;
                else
                    path = PathFactory.pathSeq(path, p);
            }

//            // Nicer - right deep.
//            ListIterator<Node> iter = nodes.listIterator(nodes.size());
//            while(iter.hasPrevious()) {
//                Node n = iter.previous();
//                Path p = path(graph, n);
//                if ( path == null )
//                    path = p;
//                else
//                    path = PathFactory.pathSeq(p, path);
//            }
            return path;
        }

        if ( node.isBlank() ) {
            if ( G.hasProperty(graph, node, SHACL.inversePath) ) {
                Node x = G.getSP(graph, node, SHACL.inversePath);
                Path p = path(graph, x);
                return PathFactory.pathInverse(p);
            }

            if ( G.hasProperty(graph, node, SHACL.zeroOrMorePath) ) {
                Node x = G.getSP(graph, node, SHACL.zeroOrMorePath);
                Path p = path(graph, x);
                return PathFactory.pathZeroOrMore1(p);
            }

            if ( G.hasProperty(graph, node, SHACL.oneOrMorePath) ) {
                Node x = G.getSP(graph, node, SHACL.oneOrMorePath);
                Path p = path(graph, x);
                return PathFactory.pathOneOrMore1(p);
            }

            if ( G.hasProperty(graph, node, SHACL.zeroOrOnePath) ) {
                Node x = G.getSP(graph, node, SHACL.zeroOrOnePath);
                Path p = path(graph, x);
                return PathFactory.pathZeroOrOne(p);
            }

            if ( G.hasProperty(graph, node, SHACL.alternativePath) ) {
                Node x = G.getSP(graph, node, SHACL.alternativePath);
                if ( ! isList(graph, x) ) {
                    throw new ShaclParseException("Not a list for path alternativePath");
                }
                List<Node> nodes = G.rdfList(graph, x);
                Path path = null;
                for ( Node n : nodes ) {
                    Path p = path(graph, n);
                    if ( path == null )
                        path = p;
                    else
                        path = PathFactory.pathAlt(path, p);
                }
                return path;
            }
        }
        throw new ShaclParseException("Bad list: "+ShLib.displayStr(node));
    }

    private static boolean isList(Graph graph, Node node) {
        return C.NIL.equals(node) || G.contains(graph, node, C.FIRST, null);
    }

    /**
     * Copy a SHACL path from one graph to another.
     * Return the path head to be connected to the rest of the graph.
     * This function does not preserve blank nodes in the SHACL path.
     */
    public static Node copyPath(Graph srcGraph, Graph dstGraph, Node start) {
        Path resultPath = ShaclPaths.parsePath(srcGraph, start);
        Node pn = ShaclPaths.pathToRDF(resultPath, dstGraph);
        return pn;
    }

    public static String pathToString(Path path) {
        return PathWriter.asString(path);
    }

    public static String pathToString(Graph graph, Path path) {
        Prologue prologue = new Prologue(graph.getPrefixMapping());
        return PathWriter.asString(path, prologue);
    }

    public static Node pathNode(Path path) {
        if ( path instanceof P_Link ) {
            return ((P_Link)path).getNode();
        }
        return null;
    }

    /*
     * P_Link
     * P_Inverse
     * P_Alt
     * P_Seq
     * P_OneOrMore1, P_OneOrMoreN
     * P_ZeroOrMore1, P_ZeroOrMoreN
     * P_ZeroOrOne
     */

    /*
SPARQL Property path: ex:parent
SHACL Property path: ex:parent

SPARQL Property path: ^ex:parent
SHACL Property path: [ sh:inversePath ex:parent ]

SPARQL Property path: ex:parent/ex:firstName
SHACL Property path: ( ex:parent ex:firstName )

SPARQL Property path: rdf:type/rdfs:subClassOf*
SHACL Property path: ( rdf:type [ sh:zeroOrMorePath rdfs:subClassOf ] )

SPARQL Property path: ex:father|ex:mother
SHACL Property path: [ sh:alternativePath ( ex:father ex:mother  ) ]
     */

//    public static Pair<Set<Triple>,Node> pathToRDF(Path p) {
//        Set<Triple> acc = new HashSet<>();
//        Node n = pathToRDF(acc::add, p);
//        return Pair.create(acc, n);
//    }

    /** Create triples for  path in dstGraph */
    public static Node pathToRDF(Path p, Graph dstGraph) {
        Node n = pathToRDF(dstGraph::add, p);
        return n;
    }

    public static Node pathToRDF(Consumer<Triple> acc, Path p) {
        PathToRDF proc = new PathToRDF(acc);
        p.visit(proc);
        return proc.point;
    }

    static class PathToRDF implements PathVisitor {

        private final Consumer<Triple> acc;
        private Node point;

        PathToRDF(Consumer<Triple> acc) {
            this.acc = acc;
        }

        private Node pathToRDF$(Path p) {
            p.visit(this);
            return point;
        }

        @Override
        public void visit(P_Link pathNode) {
            point = pathNode.getNode();
        }

        @Override
        public void visit(P_ReverseLink pathNode) {
            // [ sh:inversePath ex:parent ]
            Node n = pathNode.getNode();
            point = NodeFactory.createBlankNode();
            Triple t = Triple.create(point, SHACL.alternativePath, n);
            acc.accept(t);
        }

        @Override
        public void visit(P_Alt pathAlt) {
            Node n1 = pathToRDF$(pathAlt.getLeft());
            Node n2 = pathToRDF$(pathAlt.getRight());
            Node list = list(acc, n1, n2);
            Triple t = Triple.create(point, SHACL.alternativePath, list);
            point = list;
            acc.accept(t);
        }

        private static Node list(Consumer<Triple> acc, Node...elts) {
            Node list = C.NIL;
            for ( int i = elts.length-1; i >= 0 ; i--) {
                Node elt = elts[i];
                Node cell = NodeFactory.createBlankNode();
                Triple t1 = Triple.create(cell, C.REST, list);
                Triple t2 = Triple.create(cell, C.FIRST, elt);
                acc.accept(t1);
                acc.accept(t2);
                list = cell;
            }
            return list;
        }

        @Override
        public void visit(P_Seq pathSeq) {
            // Flatten left and right.
            Node n1 = pathToRDF$(pathSeq.getLeft());
            Node n2 = pathToRDF$(pathSeq.getRight());
            Node list = list(acc, n1, n2);
            point = list;
        }

        private Node step(Node predicate, Path path) {
            Node p = pathToRDF$(path);
            Node n = NodeFactory.createBlankNode();
            Triple t = Triple.create(n, predicate, p);
            acc.accept(t);
            return n;
        }

        @Override
        public void visit(P_OneOrMore1 path) {
            point = step(SHACL.oneOrMorePath, path.getSubPath());
        }

        @Override
        public void visit(P_ZeroOrOne path) {
            point = step(SHACL.zeroOrOnePath, path.getSubPath());
        }

        @Override
        public void visit(P_ZeroOrMore1 path) {
            point = step(SHACL.zeroOrMorePath, path.getSubPath());
        }

        @Override
        public void visit(P_OneOrMoreN path) {
            throw new ShaclParseException("Not part of SHACL: "+path);
        }

        @Override
        public void visit(P_ZeroOrMoreN path) {
            throw new ShaclParseException("Not part of SHACL: "+path);
        }

        @Override
        public void visit(P_Inverse inversePath) {
            point = step(SHACL.inversePath, inversePath.getSubPath());
        }

        @Override
        public void visit(P_Shortest pathShortest) { throw new ShaclParseException("Not part of SHACL: "+pathShortest); }

        @Override
        public void visit(P_Multi pathMulti) { throw new ShaclParseException("Not part of SHACL: "+pathMulti); }

        @Override
        public void visit(P_Distinct pathDistinct) { throw new ShaclParseException("Not part of SHACL: "+pathDistinct); }

        @Override
        public void visit(P_FixedLength pFixedLength) { throw new ShaclParseException("Not part of SHACL: "+pFixedLength); }

        @Override
        public void visit(P_Mod pathMod) { throw new ShaclParseException("Not part of SHACL: "+pathMod); }

        @Override
        public void visit(P_NegPropSet pathNotOneOf) { throw new ShaclParseException("Not part of SHACL: "+pathNotOneOf); }
    }
}
