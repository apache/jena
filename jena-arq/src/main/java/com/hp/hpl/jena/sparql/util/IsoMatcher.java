/**
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

package com.hp.hpl.jena.sparql.util ;

import static org.apache.jena.atlas.lib.Tuple.createTuple ;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.NodeUtils.EqualityTest ;

// Needs to be back tracking?

/** Simple isomorphism testing
 * This code is simple and slow.
 * For graphs, the Graph isomorphism code in Jena is much better (better tested, better performance)
 * This code can work on any tuples of nodes. 
 */
public class IsoMatcher
{
    // Possible speed ups 
    //  A/ Phase 1 - do all non-bNode tuples. / Phase 2 : all tuples with a bNode 
    //  B/ turn tuples2 into a map, keyed by (1) constants or (2) first term. 
    
    static boolean DEBUG = false ;
    private final List<Tuple<Node>>        tuples1 ;
    private final List<Tuple<Node>>        tuples2 ;
    
//    private final Map<Node, Node>           mapping = new HashMap<>();
//    private final Queue<Pair<Node, Node>>   causes  = new LinkedList<>() ;  
    private final EqualityTest nodeTest ;

    static class Mapping {
        final Node     node1 ;
        final Node     node2 ;
        final Mapping  parent ;

        static Mapping rootMapping = new Mapping(null, null, null) ;

        public Mapping(Mapping parent, Node node1, Node node2) {
            super() ;
            this.parent = parent ;
            this.node1 = node1 ;
            this.node2 = node2 ;
        }

        public boolean mapped(Node node) {
            return map(node) != null ;
        }

        public boolean revmapped(Node node) {
            return revmap(node) != null ;
        }

        public Node map(Node node) {
            Mapping mapping = this ;
            while (mapping != rootMapping) {
                if ( mapping.node1.equals(node) )
                    return mapping.node2 ;
                mapping = mapping.parent ;
            }
            return null ;
        }

        // Reverse mapping.
        public Node revmap(Node node) {
            Mapping mapping = this ;
            while (mapping != rootMapping) {
                if ( mapping.node2.equals(node) )
                    return mapping.node1 ;
                mapping = mapping.parent ;
            }
            return null ;
        }

        @Override
        public String toString() {
            StringBuilder sbuff = new StringBuilder() ;
            Mapping mapping = this ;
            while (mapping != rootMapping) {
                sbuff.append("{" + mapping.node1 + " => " + mapping.node2 + "}") ;
                mapping = mapping.parent ;
            }
            sbuff.append("{}") ;
            return sbuff.toString() ;
        }
    }
    
    static class Cause {
        final Tuple<Node> tuple ;
        final Mapping     mapping ;

        public Cause(Tuple<Node> tuple, Mapping mapping) {
            super() ;
            this.tuple = tuple ;
            this.mapping = mapping ;
        }
    }

    public static boolean isomorphic(Graph g1, Graph g2) {
        List<Tuple<Node>> x1 = tuplesTriples(g1.find(null, null, null)) ;
        List<Tuple<Node>> x2 = tuplesTriples(g2.find(null, null, null)) ;
        
        IsoMatcher matcher = new IsoMatcher(x1, x2, NodeUtils.sameTerm) ;
        return matcher.match() ;
    }

    public static boolean isomorphic(DatasetGraph dsg1, DatasetGraph dsg2) {
        List<Tuple<Node>> x1 = tuplesQuads(dsg1.find()) ;
        List<Tuple<Node>> x2 = tuplesQuads(dsg2.find()) ;
        
        IsoMatcher matcher = new IsoMatcher(x1, x2, NodeUtils.sameTerm) ;
        return matcher.match() ;
    }

    public static boolean isomorphic(List<Tuple<Node>> x1, List<Tuple<Node>> x2) {
        x1 = new ArrayList<>(x1) ;
        x2 = new ArrayList<>(x2) ;
        IsoMatcher matcher = new IsoMatcher(x1, x2, NodeUtils.sameTerm) ;
        return matcher.match() ;
    }

    private static List<Tuple<Node>> tuplesTriples(Iterator<Triple> iter) {
        List<Tuple<Node>> tuples = new ArrayList<>() ;
        for ( ; iter.hasNext() ; ) {
            Triple t = iter.next() ;
            Tuple<Node> tuple = createTuple(t.getSubject(), t.getPredicate(), t.getObject()) ;
            tuples.add(tuple) ;
        }
        return tuples ;
    }

    private static List<Tuple<Node>> tuplesQuads(Iterator<Quad> iter) {
        List<Tuple<Node>> tuples = new ArrayList<>() ;
        for ( ; iter.hasNext() ; ) {
            Quad q = iter.next() ;
            Tuple<Node> tuple = createTuple(q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject()) ;
            tuples.add(tuple) ;
        }
        return tuples ;
    }

    private IsoMatcher(List<Tuple<Node>> g1, List<Tuple<Node>> g2, EqualityTest nodeTest) {
        this.tuples1 = g1 ;
        this.tuples2 = g2 ;
        this.nodeTest = nodeTest ;
    }

    // May MUTATE tuples1 or tuples2
    private boolean match() {
        return match(tuples1, tuples2, Mapping.rootMapping) ;
    }

    private boolean match(List<Tuple<Node>> tuples1, List<Tuple<Node>> tuples2, Mapping mapping) {
        if ( DEBUG ) {
            System.out.println("match: ") ;
            System.out.println("  "+tuples1) ;
            System.out.println("  "+tuples2) ;
            System.out.println("  "+mapping) ; 
        }
        if ( tuples1.size() != tuples2.size() )
            return false;
        
        List<Tuple<Node>> tuples = new ArrayList<>(tuples1) ;  // Copy, mutate
        for ( Tuple<Node> t1 : tuples1 ) {
            if ( DEBUG )
                System.out.println("  t1 = "+t1) ;
            tuples.remove(t1) ;
            List<Cause> causes = match(t1, tuples2, mapping) ;
            for ( Cause c : causes ) {
                if ( DEBUG ) 
                    System.out.println("  Try: "+c.mapping) ;
                // Try t1 -> t2
                Tuple<Node> t2 = c.tuple ;
                tuples2.remove(t2) ;
                if ( tuples2.isEmpty() )
                    return true ;
                if ( match(tuples, tuples2, c.mapping) ) {
                    if ( DEBUG ) 
                        System.out.println("Yes") ;
                    return true ;
                }
                if ( DEBUG ) 
                    System.out.println("No") ;
                tuples2.add(t2) ;
            }
            return false ;
        }
        // The empty-empty case
        return true ;
    }

    private List<Cause> match(Tuple<Node> t1, Collection<Tuple<Node>> g2, Mapping mapping) {
        List<Cause> matches = new ArrayList<>() ;
        for ( Tuple<Node> t2 : g2 ) {
            // No - multiple bNodes.
            Mapping step = gen(t1, t2, mapping) ;
            if (step != null) { 
                Cause c = new Cause(t2, step) ;
                matches.add(c) ;
            }
        }
        return matches ;
    }

    // Maybe several mappings!
    private Mapping gen(Tuple<Node> t1, Tuple<Node> t2, Mapping _mapping) {
        if ( t1.size() != t2.size() )
            return null ;
        
        Mapping mapping = _mapping ;
        for ( int i = 0 ; i < t1.size() ; i++ ) {
            Node n1 = t1.get(i) ;
            Node n2 = t2.get(i) ;
            if ( ! nodeTest.equal(n1, n2) ) {
                mapping = gen(n1, n2, mapping) ;
                if ( mapping == null )
                    return null ;
            }
        }            
        return mapping ;
    }

    private Mapping gen(Node x1, Node x2, Mapping mapping) {
        if ( x1.isBlank() && x2.isBlank() ) {
            // Is x1 already mapped?
            Node z = mapping.map(x1) ;
            if ( z != null )
                // Already mapped
                return (nodeTest.equal(x2, z)) ? mapping : null ;
            // Check reverse
            if ( mapping.revmapped(x2) )
                return null ;
            return new Mapping(mapping, x1, x2) ;
            
        }
        return null ;
    }
    
}
