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

package org.apache.jena.shacl.lib;

import java.util.*;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.shacl.sys.C;
import org.apache.jena.sparql.util.graph.GNode;
import org.apache.jena.sparql.util.graph.GraphList;
import org.apache.jena.util.iterator.ExtendedIterator;

/** Library of functions for convenience using {@link GNode}. */
public class G {
        // Node filter tests.
//    public static boolean isURI(GNode n)         { return n != null && isURI(n.getNode()); }
//    public static boolean isBlank(GNode n)       { return n != null && isBlank(n.getNode()); }
//    public static boolean isLiteral(GNode n)     { return n != null && isLiteral(n.getNode()); }
//    public static boolean isResource(GNode n)    { return n != null && isURI(n.getNode())||isBlank(n.getNode()); }

    // Node versions
    public static Node subject(Triple triple) {
        return triple == null ? null : triple.getSubject();
    }

    public static Node predicate(Triple triple) {
        return triple == null ? null : triple.getPredicate();
    }

    public static Node object(Triple triple) {
        return triple == null ? null : triple.getObject();
    }

    // Node filter tests.
    public static boolean isURI(Node n)         { return n != null && n.isURI(); }
    public static boolean isBlank(Node n)       { return n != null && n.isBlank(); }
    public static boolean isLiteral(Node n)     { return n != null && n.isLiteral(); }
    public static boolean isResource(Node n)    { return n != null && (n.isURI()||n.isBlank()); }

    // Has type or is rdfs:subclassOf.
    public static boolean isOfType(Graph graph, Node x, Node type) {
        Objects.requireNonNull(x, "Subject");
        Objects.requireNonNull(type, "Type");
        List<Node> allClasses = listSubClasses(graph, type);
        for ( Node c : allClasses ) {
            if ( hasType(graph, x, c) )
                return true;
        }
        return false;
    }
    
    public static boolean hasType(Graph graph, Node x, Node type) {
        Objects.requireNonNull(x, "Subject");
        Objects.requireNonNull(type, "Type");
        return contains(graph, x, C.rdfType, type);
    }

    public static Node getSP(Graph graph, Node s, Node p) {
        return object(first(find(graph, s, p, Node.ANY)));
    }

    public static List<Node> listSP(Graph graph, Node s, Node p) {
        return iterSP(graph, s, p).toList();
    }

    public static long countSP(Graph graph, Node s, Node p) {
        return Iter.count(iterSP(graph, s, p));
    }

    public static ExtendedIterator<Node> iterSP(Graph graph, Node s, Node p) {
        return graph.find(s, p, null).mapWith(Triple::getObject);
    }

    public static List<Node> listPO(Graph graph, Node p, Node o) {
        return graph.find(null, p, o).mapWith(Triple::getSubject).toList();
    }

    public static List<Node> listSubjectsOfType(Graph graph, Node type) {
        return graph.find(null, C.rdfType, type).mapWith(Triple::getSubject).toList();
    }

    public static List<Node> rdfList(Graph graph, Node node) {
        GNode gNode = GN.create(graph, node);
        return GraphList.members(gNode);
    }

    // Follows RDFS

    /**
     * List the subclasses of a type, including itself.
     * This is <tt>?x rdfs:subClassOf* type</tt>.
     * The list does not contain duplicates.   
     */
    public static List<Node> listSubClasses(Graph graph, Node type) {
        List<Node> acc = new ArrayList<>();
        // Subclasses are follow rdfs:subclassOf in reverse - object to subject.
        // Transitive.transitive is "visit once".
        Transitive.transitiveInc(graph, false, type, C.rdfsSubclassOf, acc);
        return acc;
    }

    /**
     * List the superclasses of a type, including itself.
     * This is <tt>type rdfs:subClassOf* ?x</tt>.
     * The list does not contain duplicates.   
     */
    public static List<Node> listSuperClasses(Graph graph, Node type) {
        List<Node> acc = new ArrayList<>();
        // Super classes are follow rdfs:subclassOf - subject to object.
        // Transitive.transitive is "visit once".
        Transitive.transitiveInc(graph, true, type, C.rdfsSubclassOf, acc);
        return acc;
    }

    public static List<Node> listNodesOfType(Graph graph, Node type) {
        return graph.find(null, C.rdfType, type).mapWith(Triple::getSubject).toList();
    }

    /** List the types of a node/subject */
    public static List<Node> listTypesOfNode(Graph graph, Node subject) {
        return graph.find(subject, C.rdfType, null).mapWith(Triple::getObject).toList();
    }

    /** List the types of a node/subject, following rdfs:subClassOf for super classes. */
    public static List<Node> listAllTypesOfNode(Graph graph, Node subject) {
        List<Node> types = listTypesOfNode(graph, subject);
        List<Node> types2 = new ArrayList<>();
        types.forEach(t->{
            List<Node> subClasses = listSuperClasses(graph, t);
            types2.addAll(subClasses);
        });
        return types2;
    }

    /** List all the node of type, including considering rdfs:subClassOf */
    public static List<Node> listAllNodesOfType(Graph graph, Node type) {
        List<Node> types = G.listSubClasses(graph, type);
        List<Node> nodes = new ArrayList<>();
        types.forEach(t->nodes.addAll(G.listNodesOfType(graph, t)));
        return nodes;
    }

    // Set versions.
    
    public static Set<Node> setNodesOfType(Graph graph, Node type) {
        return graph.find(null, C.rdfType, type).mapWith(Triple::getSubject).toSet();
    }

    /** Set of types of a node/subject */
    public static Set<Node> setTypesOfNode(Graph graph, Node subject) {
        return graph.find(subject, C.rdfType, null).mapWith(Triple::getObject).toSet();
    }
 
    /**
     * Set of the subclasses of a type, including itself.
     * This is <tt>?x rdfs:subClassOf* type</tt>.
     */
    public static Set<Node> setSubClasses(Graph graph, Node type) {
        Set<Node> acc = new HashSet<>();
        // Subclasses are follow rdfs:subclassOf in reverse - object to subject.
        Transitive.transitiveInc(graph, false, type, C.rdfsSubclassOf, acc);
        return acc;
    }
    
    /** List all the node of type, including considering rdfs:subClassOf */
    public static Set<Node> setAllNodesOfType(Graph graph, Node type) {
        Set<Node> types = G.setSubClasses(graph, type);
        Set<Node> nodes = new HashSet<>();
        types.forEach(t->nodes.addAll(G.listNodesOfType(graph, t)));
        return nodes;
    }
    
    public static Set<Node> setSP(Graph graph, Node s, Node p) {
        return graph.find(s, p, null).mapWith(Triple::getObject).toSet();
    }

    public static Set<Node> setPO(Graph graph, Node p, Node o) {
        return graph.find(null, p, o).mapWith(Triple::getSubject).toSet();
    }
    // Exactly one, exception on none or more than one.
    public static Node getOneSP(Graph graph, Node s, Node p) {
        return object(findUnique(graph, s, p, Node.ANY));
    }

    // Zero or one, exception on more than one.
    public static Node getZeroOrOneSP(Graph graph, Node s, Node p) {
        return object(findZeroOne(graph, s, p, Node.ANY));
    }

    // Exactly one, exception on none or more than one.
    public static Triple getOne(Graph graph, Node s, Node p, Node o) {
        return findUnique(graph, s, p, Node.ANY);
    }

    public static boolean contains(Graph g, Node s, Node p, Node o) {
        return g.contains(s, p, o);
    }

    public static boolean absentOrOne(Graph g, Node s, Node p, Node o) {
        if ( ! g.contains(s, p, o) )
            return false;
         long x = Iter.count(g.find(s,p,null));
         if ( x == 1 )
             return true;
         throw new RDFDataException("More then one ("+x+") "+matchStr(s,p,o));
    }

    public static boolean containsOne(Graph g, Node s, Node p, Node o) {
        return g.contains(s, p, o) && Iter.count(g.find(s,p,null)) == 1;
    }
    
    public static boolean hasProperty(Graph g, Node s, Node p) {
        return g.contains(s, p, null);
    }

    public static ExtendedIterator<Triple> find(Graph g, Node s, Node p, Node o) {
        return g.find(s, p, o);
    }

    private static Triple findUnique(Graph g, Node s, Node p, Node o) {
        ExtendedIterator<Triple> iter = g.find(s, p, o);
        try {
            if ( ! iter.hasNext() )
                throw new RDFDataException("No match : "+matchStr(s,p,o));
            Triple x = iter.next();
            if ( iter.hasNext() )
                throw new RDFDataException("More than one match : "+matchStr(s,p,o));
            return x;
        } finally { iter.close(); }
    }

    private static Triple findZeroOne(Graph g, Node s, Node p, Node o) {
        ExtendedIterator<Triple> iter = g.find(s, p, o);
        try {
            if ( ! iter.hasNext() )
                return null;
            Triple x = iter.next();
            if ( iter.hasNext() ) {
                //g.find(s, p, o).forEachRemaining(System.err::println);
                throw new RDFDataException("More than one match : "+matchStr(s,p,o));
            }
            return x;
        } finally { iter.close(); }
    }

    private static String matchStr(Node s, Node p, Node o) {
        //return String.format("(%s %s %s)", s, p, o);
        return "("+NodeFmtLib.strNodes(s,p,o)+")";
    }

    private static Triple first(ExtendedIterator<Triple> iter) {
        try {
            if ( ! iter.hasNext() )
                return null;
            return iter.next();
        } finally { iter.close(); }
    }

    @SafeVarargs
    public static <X> boolean allNonNull(X ... objects) {
        return countNonNulls(objects) == objects.length;
    }

    @SafeVarargs
    public static <X> boolean exactlyOneSet(X ... objects) {
        return countNonNulls(objects) == 1;
    }

    @SafeVarargs
    public static <X> X atMostOne(X ... objects) {
        int c = 0;
        X x = null;
        for ( X obj : objects ) {
            if ( obj != null ) {
                c++;
                if ( c > 1 )
                    throw new RDFDataException("ExactlyOne:"+Arrays.asList(objects));
                if ( x == null )
                    x = obj;
            }
        }
        // Exactly one.
//        if ( x == null )
//            throw new RDFDataException("ExactlyOne: None");
        return x;
    }

    @SafeVarargs
    public static <X> int countNonNulls(X ... objects) {
        int x = 0;
        for ( Object obj : objects ) {
            if ( obj != null )
                x++;
        }
        return x;
    }

}
