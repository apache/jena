/******************************************************************
 * File:        TransitiveGraphCache.java
 * Created by:  Dave Reynolds
 * Created on:  13-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TransitiveGraphCache.java,v 1.14 2004-03-17 12:04:25 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.transitiveReasoner;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.reasoner.*;
import java.util.*;

/**
 * Datastructure used to represent a closed transitive reflexive relation.
 * This should be more efficient than dynamically computing the relation
 * from a triple store. Designed to be space efficient and not too
 * time inefficient - though more work could be done to pick a real
 * closure algorithm. The cost of space efficiency is that we have to
 * reconstruct the triple set from the core graph which turns over a lot
 * of storage. 
 * <p>
 * The current implementation stores the direct relations as real graph
 * (objects linked together by pointers). As relations are added redundant
 * ones which could be reconstructed from the closure are dropped. To 
 * access the direct "minimal" version of the relation we just have to
 * read out these links. To access the transitively closed version we
 * have to walk the graph, however the results of these walks can be optionally
 * cached (switch on using setCaching method). An alternative implementation
 * would represent the closure using adjacency matrices and construct
 * the complete closure in one go using the standard algorithm. This would
 * be more efficient but require more space unless we have a good sparse binary
 * matrix package.</p>
 * <p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.14 $ on $Date: 2004-03-17 12:04:25 $
 */
public class TransitiveGraphCache implements Finder {

    /** Map from RDF Node to the corresponding Graph node. */
    protected HashMap nodeMap = new HashMap();
    
    /** If caching enable then this maps RDF Node to its 
     *  closure (a List of Triples) */
    protected HashMap cacheClosureForward = null;
    
    /** If caching enable then this maps RDF Node to its 
     *  closure (a List of Triples) */
    protected HashMap cacheClosureBackward = null;
    
    /** Set to true if caching of full closure is enabled */
    protected boolean cacheOn = false;
    
    /** The RDF predicate representing the direct relation */
    protected Node directPredicate;
    
    /** The RDF predicate representing the closed relation */
    protected Node closedPredicate;
    
    /**
     * Constructor - create a new cache to hold the given relation information.
     * @param directPredicate The RDF predicate representing the direct relation
     * @param closedPredicate The RDF predicate representing the closed relation
     */
    public TransitiveGraphCache(Node directPredicate, Node closedPredicate) {
        this.directPredicate = directPredicate;
        this.closedPredicate = closedPredicate;
    }
    
    /**
     * Register a new relation instance in the cache
     */
    public void addRelation(Node start, Node end) {
        clearClosureCache();
        getGraphNode(start).addDirectLink(getGraphNode(end));
    }
    
    /**
     * Remove an instance of a relation from the cache.
     */
    public void removeRelation(Node start, Node end) {
        clearClosureCache();
        getGraphNode(start).removeLink(getGraphNode(end));
    }
    
    /**
     * Clear the entire cache contents. 
     */
    public void clear() {
        clearClosureCache();
        nodeMap.clear();
    }
    
    /**
     * Clear the closure cache, if any.
     */
    private synchronized void clearClosureCache() {
        if (cacheOn) {
            // blow away the cache, don't try to do incremental updates
            if (cacheClosureBackward.size() > 0) cacheClosureBackward.clear();
            if (cacheClosureForward.size() > 0) cacheClosureForward.clear();
        }
    }
    
    /**
     * Switch on/off caching of transitive closures.
     * Any time a closure is requested the complete result set
     * will be cached for future reuse.
     * @param cacheOn set to true to start cache, to false to stop caching
     */
    public synchronized void setCaching(boolean cacheOn) {
        if (this.cacheOn != cacheOn) {
            this.cacheOn = cacheOn;
            if (cacheOn) {
                cacheClosureBackward = new HashMap();
                cacheClosureForward = new HashMap();
            } else {
                cacheClosureBackward = null;
                cacheClosureBackward = null;
            }
        }
    }
    
    /**
     * Cache all instances of the given predicate which are
     * present in the given Graph.
     * @param graph the searchable set of triples to cache
     * @param predicate the predicate to cache, need not be the registered
     * predicate due to subProperty declarations
     * @return returns true if new information has been cached
     */
    public boolean cacheAll(Finder graph, Node predicate) {
        ExtendedIterator it = graph.find(new TriplePattern(null, predicate, null));
        boolean foundsome = it.hasNext();
        while (it.hasNext()) {
            Triple triple = (Triple) it.next();
            addRelation(triple.getSubject(), triple.getObject());
        }
        it.close();
        return foundsome;
    }
    
    /**
     * Basic pattern lookup interface.
     * @param pattern a TriplePattern to be matched against the data
     * @return a ExtendedIterator over all Triples in the data set
     *  that match the pattern
     */
    public ExtendedIterator find(TriplePattern pattern) {
        Node s = pattern.getSubject();
        Node p = pattern.getPredicate();
        Node o = pattern.getObject();
        
        if (p.isVariable() || p.equals(directPredicate) || p.equals(closedPredicate)) {
            boolean closed = !p.equals(directPredicate);
            Node pred = closedPredicate; // p.isVariable() ? closedPredicate : p;
            if (s.isVariable()) {
                if (o.isVariable()) {
                    // list all the graph contents
                    Iterator i = new ListAll(nodeMap.values().iterator(), closed, pred);
                    return WrappedIterator.create(i);
                } else {
                    // list all backwards from o
                    GraphNode gn_o = (GraphNode)nodeMap.get(o);
                    if (gn_o == null) return new NiceIterator();
                    return new GraphWalker(gn_o, false, closed, pred);
                }
            } else {
                GraphNode gn_s = (GraphNode)nodeMap.get(s);
                if (gn_s == null) return new NiceIterator();
                if (o.isVariable()) {
                    // list forward from s
                    return new GraphWalker(gn_s, true, closed, pred);
                } else {
                    // Singleton test
                    GraphNode gn_o = (GraphNode)nodeMap.get(o);
                    if (gn_o == null) return new NiceIterator();
                    if (gn_s.linksTo(gn_o)) {
                        return new SingletonIterator(new Triple(s, pred, o));
                    } else {
                        return new NiceIterator();
                    }
                }
            }
        } else {
            // No matching triples in this cache
            return new NiceIterator();
        }
    }

    /**
     * Return true if the given pattern occurs somewhere in the find sequence.
     */
    public boolean contains(TriplePattern pattern) {
        ClosableIterator it = find(pattern);
        boolean result = it.hasNext();
        it.close();
        return result;
    }
    
    /**
     * Return an iterator over all registered property nodes
     */
    public ExtendedIterator listAllProperties() {
        return WrappedIterator.create(nodeMap.keySet().iterator());
    }
   
    /**
     * Return true if the given Node is registered as a property
     */
    public boolean isProperty(Node node) {
        return nodeMap.keySet().contains(node);
    }
    
    /**
     * Create a deep copy of the cache contents.
     * Works by creating a completely new cache and just adding in the
     * direct links.
     */
    public TransitiveGraphCache deepCopy() {
        TransitiveGraphCache copy = new TransitiveGraphCache(directPredicate, closedPredicate);
        Iterator i = find(new TriplePattern(null, directPredicate, null));
        while (i.hasNext()) {
            Triple t = (Triple)i.next();
            copy.addRelation(t.getSubject(), t.getObject());
        }
        return copy;
    }
    
    /**
     * Return an iterator over all the forward or backward links to 
     * a given node. May be cached.
     */
    private synchronized ExtendedIterator walk(GraphNode node, boolean forward, boolean closed, Node predicate) {
        if (cacheOn && closed) {
            // Check cache
            Map cache = forward ? cacheClosureForward : cacheClosureBackward;
            List closure = (List) cache.get(node);
            if (closure == null) {
                Iterator it = new GraphWalker(node, forward, closed, predicate);
                closure = new ArrayList();
                while (it.hasNext()) {
                    closure.add(it.next());
                }
                cache.put(node, closure);
            }
            return WrappedIterator.create(closure.iterator());
        } else {
            return new GraphWalker(node, forward, closed, predicate);
        }
    }

    /**
     * Extended find interface used in situations where the implementator
     * may or may not be able to answer the complete query. 
     * <p>
     * In this case any query on the direct or closed predicates will
     * be assumed complete, any other query will pass on to the continuation.</p>
     * @param pattern a TriplePattern to be matched against the data
     * @param continuation either a Finder or a normal Graph which
     * will be asked for additional match results if the implementor
     * may not have completely satisfied the query.
     */
    public ExtendedIterator findWithContinuation(TriplePattern pattern, Finder continuation) {
        Node p = pattern.getPredicate();
        
        if (p.isVariable()) {
            // wildcard predicate so return merge of cache and continuation
            return find(pattern).andThen(continuation.find(pattern));
        } else if (p.equals(directPredicate) || p.equals(closedPredicate)) {
            // Satisfy entire query from the cache
            return find(pattern);
        } else {
            // No matching triples in this cache so just search the continuation
            return continuation.find(pattern);
        }
        
    }
   
    /**
     * Returns the closedPredicate.
     * @return Node
     */
    public Node getClosedPredicate() {
        return closedPredicate;
    }

    /**
     * Returns the directPredicate.
     * @return Node
     */
    public Node getDirectPredicate() {
        return directPredicate;
    }
    
    /**
     * Return the GraphNode corresponding to the given RDF node, creating
     * it if necessary.
     */
    protected GraphNode getGraphNode(Node n) {
        GraphNode gn = (GraphNode)nodeMap.get(n);
        if (gn == null) {
            gn = new GraphNode(n);
            nodeMap.put(n, gn);
            // Add reflexive version
            gn.addDirectLink(gn);
        }
        return gn;
    }
    
    /**
     * Print a dump of the cache
     */
    protected void printAll() {
        for (Iterator i = nodeMap.values().iterator(); i.hasNext(); ) {
            GraphNode gn = (GraphNode)i.next();
            System.out.println(gn.longString());
        }
    }
    
    /**
     * Inner class. Implements an iterator over all stored relations.
     */
    private static class ListAll implements Iterator {
        
        /** set to true to list the close relation rather then just the direct version */
        boolean closed;
        
        /** an iterator over all the graph nodes */
        Iterator nodes;
        
        /** the predicate used to report the relations */
        Node predicate;
        
        /** iterator which is walking the relation for the current node */
        Iterator listRelated;
        
        /** the current root node */
        GraphNode current;
        
        /** The next triple to return */
        Triple next;
        
        /** 
         * constructor 
         * @param closed set to true to list the close relation rather
         * then just the direct version
         * @param nodes an iterator over all the graph nodes
         * @param the predicate used to report the relations
         */
        ListAll(Iterator nodes, boolean closed, Node predicate) {
            this.nodes = nodes;
            this.closed = closed;
            this.predicate = predicate;
            listRelated = null;
            walkOne();
        }
        
        /** Iterator interface - test if more values available */
        public boolean hasNext() {
            return next != null;
        }
        
        /** Iterator interface - get next value */
        public Object next() {
            Object toReturn = next;
            walkOne();
            return toReturn;
        }
        
        /** Iterator interface - remove the current value - blocked */
        public void remove() {
            throw new UnsupportedOperationException("GraphCache does not yet implement remove operation");
        }
        
        /** Step the iterator forward one */
        private void walkOne() {
            if (listRelated != null && listRelated.hasNext()) {
                next = (Triple) listRelated.next();
            } else {
                if (nodes.hasNext()) {
                    current = (GraphNode) nodes.next();
                    listRelated = new GraphWalker(current, true, closed, predicate);
                    walkOne();
                } else {
                    // end
                    next = null;
                    return;
                }
            }
        }   
    }
    
    /**
     * Inner class. Instances of this are used to represent
     * nodes in the graph. They are bidirectinally linked becase
     * we want to be able to lookup the inverse relationship as well.
     * Links are only established to represent the minimal direct relationship,
     * the closure is determined by walking the graph on request.
     * <p>I justify the direct accesses to GraphNode fields on the grounds
     * that this is a private inner class ...</p>
     */
    private static class GraphNode {
        /** Pointer to the RDF Node this graph node describes */
        Node node;
        
        /** forward links to successor nodes */
        List successors;
        
        /** backward links to predecessors */
        List predecessors;        
        
        /** Construct a Graph node to represent node */
        GraphNode(Node n) {
            node = n;
            successors = new ArrayList(2);
            predecessors = new ArrayList(2);
        }
        
        /** Add a new forward link pointing to the given graph node */
        void addLink(GraphNode n) {
            if (!successors.contains(n)) {
                successors.add(n);
                n.predecessors.add(this);
            }
        }
        
        /** Remove a link, if any, from this node to the given graph node */
        void removeLink(GraphNode n) {
            successors.remove(n);
            n.predecessors.remove(this);
        }
               
        /** Add a new forward link retaining only direct links */
        void addDirectLink(GraphNode n) {
            if (this == n) {
                // Reflexive case 
                addLink(n);
                return;
            }
            if (linksTo(n)) {
                // Link is already derivable from current graph
                return;
            }
            // Check current successors to see if any would now be indirect
            Collection redundant = accessible(n, successors, this);
            for (Iterator it = redundant.iterator(); it.hasNext(); ) {
                GraphNode suc = (GraphNode)it.next();
                successors.remove(suc);
                suc.predecessors.remove(this);
            }
            addLink(n);
        }
        
        /** Check if the given node is on the forward link path */
        boolean linksTo(GraphNode n) {
            return linksToInternal(n, new HashSet());
        }
        
        /** internal version of linksTo */
        private boolean linksToInternal(GraphNode n, HashSet visited) {
            for (Iterator it = successors.iterator(); it.hasNext(); ) {
                GraphNode suc = (GraphNode)it.next();
                // Check for duplicate visits to junction nodes
                if (suc == n) {
                    return true;
                }
                if ( ! visited.add(suc) ) continue;
                if (suc.linksToInternal(n, visited)) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Check if any of the given list of nodes is accessible from
         * start without going through block. Returns a collection
         * of accessible nodes.
         */
        Collection accessible(GraphNode start, List targets, GraphNode block) {
            List found = new ArrayList();
            Set targetSet = new HashSet(targets);
            doAccessible(start, targetSet, block, found, new HashSet());
            return found;
        }
        
        /**
         * Internal implementation of accessible.
         */
        void doAccessible(GraphNode start, Collection targets, GraphNode block, List found, Set visited) {
            visited.add(start);
            for (Iterator it = start.successors.iterator(); it.hasNext(); ) {
                GraphNode suc = (GraphNode)it.next();
                if ( ! visited.add(suc) ) continue;
                if (suc == block) return;
                if (targets.contains(suc)) {
                    found.add(suc);
                }
                doAccessible(suc, targets, block, found, visited);
            }
        }
        
        /** Printable representation */
        public String toString() {
            return node.toString();
        }
        
        /** Longer printable representation */
        public String longString() {
            StringBuffer display = new StringBuffer();
            display.append(node.toString());
            display.append(" [");
            for (Iterator i = successors.iterator(); i.hasNext(); ) {
                display.append(i.next().toString() + " ");
            }
            display.append("]");
            return display.toString();
        }
        
    } // End of GraphNode inner class
    
    /**
     * Inner class used to walk to forward or backward links 
     * of the graph.
     * <p> The triples are dynamically allocated which is costly. 
     * <p>I justify the direct accesses to GraphNode fields on the grounds
     * that this is a private inner class ...</p>
     */
    private static class GraphWalker extends NiceIterator implements ExtendedIterator {
        /** Indicate if this is a forward or backward walk */
        boolean isForward;
        
        /** Indicate if this is a shallow or deep walk */
        boolean isDeep;
        
        /** The current node being visited */
        GraphNode node;
        
        /** The root node for reconstructing triples */
        Node root;
        
        /** The predicate for reconstructing triples */
        Node predicate; 
        
        /** The current successor to this node being checked */
        int index;
        
        /** stack of graph nodes being walked */
        ArrayList nodeStack = new ArrayList();
        
        /** stack of link index for the corresponding node */
        int[] indexStack;
        
        /** The next value to be returned */
        Triple next;
        
        /** The set of junction nodes already visited */
        HashSet visited = new HashSet();
        
        /** 
         * Constructor. Creates an iterator which will walk
         * the graph, returning triples.
         * @param node the starting node for the walk
         * @param forward set to true if walking the forward links
         * @param closed set to true of walking the whole transitive closure
         * @param predicate the predicate to be walked
         */
        GraphWalker(GraphNode node, boolean forward, boolean closed, Node predicate) {
            isForward = forward;
            isDeep = closed;
            indexStack = new int[20];
            this.node = node;
            this.root = node.node;
            this.predicate = predicate;
            index = -1;
            walkOne();
        }
        
        /** Iterator interface - test if more values available */
        public boolean hasNext() {
            return next != null;
        }
        
        /** Iterator interface - get next value */
        public Object next() {
            Object toReturn = next;
            walkOne();
            return toReturn;
        }
                
        /**
         * Walk one step
         */
        protected void walkOne() {
            index++;
            if (index >= (isForward ? node.successors.size() : node.predecessors.size())) {
                // Finished this node
                if (nodeStack.isEmpty()) {
                    next = null;
                    return;
                }
                popStack();
                walkOne();
            } else {
                GraphNode nextNode = (GraphNode) (isForward ? node.successors.get(index) 
                                                            : node.predecessors.get(index));
                if (visited.contains(nextNode)) {
                    // Already visited this junction, skip it
                    walkOne();
                    return;
                } else {
                    visited.add(nextNode);
                }
                // Set up for depth-first visit next
                if (isDeep) pushStack(nextNode);
                next = isForward ? new Triple(root, predicate, nextNode.node)
                                 : new Triple(nextNode.node, predicate, root);
            }
        }
        
        /**
         * Push the current state onto the stack
         */
        protected void pushStack(GraphNode next) {
            nodeStack.add(node);
            node = next;
            int len = indexStack.length;
            if (nodeStack.size() > len) {
                // Grow index stack
                int[] newStack = new int[len + len/2];
                for (int i = 0; i < len; i++) {
                    newStack[i] = indexStack[i];
                }
                indexStack = newStack;
            }
            indexStack[nodeStack.size()-1] = index;
            index = -1;
        }
        
        /**
         * Pop the prior state back onto the stack
         */
        protected void popStack() {
            int i = nodeStack.size()-1;
            index = indexStack[i];
            node = (GraphNode) nodeStack.get(i);
            nodeStack.remove(i);
        }
        
    } // End of GraphWalker inner class

    /**
     * Temporary testing method.
     * Prints all the matches to a given find
     */
    private static void listFind(TransitiveGraphCache cache, Node s, Node p, Node o) {
        System.out.print("Checking triple pattern: ");
        System.out.print(" " + s);
        System.out.print(" " + p);
        System.out.print(" " + o + "\n");

        Iterator i = cache.find(new TriplePattern(s, p, o));
        while (i.hasNext()) {
            System.out.println("  - " + i.next());
        }
    }    
 
}

/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

