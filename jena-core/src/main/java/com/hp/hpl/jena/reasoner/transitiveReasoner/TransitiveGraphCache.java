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

package com.hp.hpl.jena.reasoner.transitiveReasoner;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.reasoner.*;

import java.util.*;

/**
 * Datastructure used to represent a closed transitive reflexive relation.
 * It (mostly) incrementally maintains a transitive reduction and transitive
 * closure of the relationship and so queries should be faster than dynamically 
 * computing the closed or reduced relations.
 * <p>
 * The implementation stores the reduced and closed relations as real graph
 * (objects linked together by pointers). For each graph node we store its direct
 * predecessors and successors and its closed successors.  A cost penalty 
 * is the storage turnover involved in turning the graph representation back into 
 * triples to answer queries. We could avoid this by optionally also storing the
 * manifested triples for the links.
 * </p><p>
 * Cycles are currently handled by collapsing strongly connected components.
 * Incremental deletes would be possible but at the price of substanially 
 * more storage and code complexity. We compromise by doing the easy cases
 * incrementally but some deletes (those that break strongly connected components)
 * will trigger a fresh rebuild.
 * </p><p>
 * TODO Combine this with interval indexes (Agrawal, Borigda and Jagadish 1989) 
 * for storing the closure of the predecessor relationship. Typical graphs
 * will be nearly tree shaped so the successor closure is modest (L^2 where
 * L is the depth of the tree branch) but the predecessor closure would be 
 * expensive. The interval index would handle predecessor closure nicely.
 * </p>
 */

// Note to maintainers. The GraphNode object is treated as a record structure
// rather than an abstract datatype by the rest of the GraphCache code - which
// directly access its structure. I justify this on the flimsy grounds that it is a
// private inner class.

public class TransitiveGraphCache implements Finder {

	/** Flag controlling the whether the triples 
	 *  representing the closed relation should also be cached. */
	protected boolean cacheTriples = false;
	
    /** Map from RDF Node to the corresponding Graph node. */
    protected HashMap<Node, GraphNode> nodeMap = new HashMap<>();
    
    /** The RDF predicate representing the direct relation */
    protected Node directPredicate;
    
    /** The RDF predicate representing the closed relation */
    protected Node closedPredicate;
	
    /** A list of pending deletes which break the cycle-free normal form */
    protected Set<Triple> deletesPending;
    
	/** The original triples, needed for processing delete operations
	 * because some information is lost in the SCC process */ 
	protected Set<Triple> originalTriples = new HashSet<>();
	
    /**
     * Inner class used to represent vistors than can be applied to each
     * node in a graph walk. 
     */
    static interface Visitor<Alpha, Beta> {
        // The visitor must not delete and pred entries to avoid CME
        // If this is needed return a non-null result which is a list of pred nodes to kill
    	List<GraphNode> visit(GraphNode node, GraphNode processing, Alpha arg1, Beta arg2);
    }
    
	/**
     * Inner class used to walk backward links of the graph.
     * <p> The triples are dynamically allocated which is costly. 
     */
    static class GraphWalker extends NiceIterator<Triple> implements ExtendedIterator<Triple> {
        
        /** Indicate if this is a shallow or deep walk */
        boolean isDeep;
        
        /** The current node being visited */
        GraphNode node;
        
        /** The root node for reconstructing triples */
        Node root;
        
        /** The predicate for reconstructing triples */
        Node predicate; 
        
        /** Iterator over the predecessors to the current node bein walked */
        Iterator<GraphNode> iterator = null;
        
        /** Iterator over the aliases of the current predecessor being output */
        Iterator<GraphNode> aliasIterator = null;
        
        /** stack of graph nodes being walked */
        ArrayList<GraphNode> nodeStack = new ArrayList<>();
        
        /** stack of iterators for the higher nodes in the walk */
        ArrayList<Iterator<GraphNode>> iteratorStack = new ArrayList<>();
        
        /** The next value to be returned */
        Triple next;
        
        /** The set of junction nodes already visited */
        HashSet<GraphNode> visited = new HashSet<>();
        
        /** 
         * Constructor. Creates an iterator which will walk
         * the graph, returning triples.
         * @param node the starting node for the walk
         * @param rdfNode the rdfNode we are try to find predecessors for
         * @param closed set to true of walking the whole transitive closure
         * @param predicate the predicate to be walked
         */
        GraphWalker(GraphNode node, Node rdfNode, boolean closed, Node predicate) {
            isDeep = closed;
            this.node = node;
            this.root = rdfNode;
            this.predicate = predicate;
            this.iterator = node.pred.iterator();
            aliasIterator = node.siblingIterator();
            next = new Triple(root, predicate, root);   // implicit reflexive case 
        }
        
        /** Iterator interface - test if more values available */
        @Override public boolean hasNext() {
            return next != null;
        }
        
        /** Iterator interface - get next value */
        @Override public Triple next() {
            Triple toReturn = next;
            walkOne();
            return toReturn;
        }
                
        /**
         * Walk one step
         */
        protected void walkOne() {
            if (aliasIterator != null) {
                if (aliasIterator.hasNext()) {
                    GraphNode nextNode = aliasIterator.next();
                    next =  new Triple(nextNode.rdfNode, predicate, root);
                    return;
                } else {
                    aliasIterator = null;
                }
            }
            if (iterator.hasNext()) {
                GraphNode nextNode = iterator.next();
                if (visited.add(nextNode)) {
                    // Set up for depth-first visit next
                    if (isDeep)
                        pushStack(nextNode);
                    next =  new Triple(nextNode.rdfNode, predicate, root);
                    aliasIterator = nextNode.siblingIterator();
                } else { 
                    // Already visited this junction, skip it
                    walkOne();
                    return;
                }
            } else {
                // Finished this node
                if (nodeStack.isEmpty()) {
                    next = null;
                    return;
                }
                popStack();
                walkOne();
            }
        }
        
        /**
         * Push the current state onto the stack
         */
        protected void pushStack(GraphNode next) {
            nodeStack.add(node);
            iteratorStack.add(iterator);
            iterator = next.pred.iterator();
            node = next;
        }
        
        /**
         * Pop the prior state back onto the stack
         */
        protected void popStack() {
            int i = nodeStack.size()-1;
            iterator = iteratorStack.remove(i);
            node = nodeStack.remove(i);
        }
        
    } // End of GraphWalker inner class    
    
    /**
     * Inner class used to do a complete walk over the graph
     */
    private static class FullGraphWalker extends NiceIterator<Triple> implements ExtendedIterator<Triple> {

        /** Flag whether we are walking over the closed or direct relations */
        boolean closed;
        
        /** Iterator over the start nodes in the node map */
        Iterator<GraphNode> baseNodeIt;
        
        /** The current node being visited */
        GraphNode node;
        
        /** The root node for reconstructing triples */
        Node nodeN;
        
        /** The predicate for reconstructing triples */
        Node predicate; 
        
        /** Iterator over the successor nodes for the baseNode */
        Iterator<GraphNode> succIt = null;
        
        /** The current successor being processed */
        GraphNode succ;
        
        /** Iterator over the aliases for the current successor */
        Iterator<GraphNode> aliasesIt = null;
        
        /** The next value to be returned */
        Triple next;
        
        /** Construct a walker for the full closed or direct graph */
        FullGraphWalker(boolean closed, Node predicate, HashMap<Node, GraphNode> nodes) {
            this.predicate = predicate;
            this.closed = closed;
            baseNodeIt = nodes.values().iterator();
            walkOne();
        }
        
        /** Iterator interface - test if more values available */
        @Override public boolean hasNext() {
            return next != null;
        }
        
        /** Iterator interface - get next value */
        @Override public Triple next() {
            Triple toReturn = next;
            walkOne();
            return toReturn;
        }
                
        /**
         * Walk one step
         */
        protected void walkOne() {
            if (aliasesIt != null) {
                while (aliasesIt.hasNext()) {
                    GraphNode al = aliasesIt.next();
                    if (al != succ && al != node) {
                        next = new Triple(nodeN, predicate, al.rdfNode);
                        return;
                    }
                }
                aliasesIt = null;      // End of aliases
            }
            
            if (succIt != null) {
                while (succIt.hasNext()) {
                    succ = succIt.next();
                    if (succ == node) continue; // Skip accidental reflexive cases, already done
                    aliasesIt = succ.siblingIterator();
                    next = new Triple(nodeN, predicate, succ.rdfNode);
                    return;
                }
                succIt = null;      // End of the successors
            }
            
            if (baseNodeIt.hasNext()) {
                node = baseNodeIt.next();
                nodeN = node.rdfNode;
                GraphNode lead = node.leadNode();
                succIt = (closed ? lead.succClosed : lead.succ).iterator();
                succIt = lead.concatenateSiblings( succIt );
                next = new Triple(nodeN, predicate, nodeN); // Implicit reflexive case
            } else {
                next = null; // End of walk
            }
        }
        
    } // End of FullGraphWalker inner class
    
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
     * Register a new relation instance in the cache
     */
    public synchronized void addRelation(Triple t) {
    	originalTriples.add(t);
    	addRelation(t.getSubject(), t.getObject());
    }
    
    /**
     * Register a new relation instance in the cache
     */
    private void addRelation(Node start, Node end) {
        if (start.equals(end)) return;      // Reflexive case is built in
        GraphNode startN = getLead(start);
        GraphNode endN = getLead(end);
    	
    	// Check if this link is already known about
    	if (startN.pathTo(endN)) {
    		// yes, so no work to do
    		return;
    	}

    	boolean needJoin = endN.pathTo(startN);
        Set<GraphNode> members = null;
        if (needJoin) {
        	// Reduce graph to DAG by factoring out SCCs
//	        startN.assertLinkTo(endN);
            // First find all the members of the new component
            members = new HashSet<>();
            members.add(endN);
            startN.visitPredecessors(new Visitor<Set<GraphNode>, GraphNode>() {
                @Override
                public List<GraphNode> visit(GraphNode node, GraphNode processing, Set<GraphNode> members, GraphNode endN) {
                    if (endN.pathTo(node)) members.add( node );
                    return null;
                } }, members, endN);
            // Then create the SCC
            startN.makeLeadNodeFor(members);
            // Now propagate the closure in the normalized graph
            startN.propagateSCC();
        } else {
	    	// Walk all predecessors of start retracting redundant direct links
	    	// and adding missing closed links
	        startN.propagateAdd(endN);
	        startN.assertLinkTo(endN);
        }
        
    	if (needJoin) {
    		// Create a new strongly connected component
    	}
    }
    
    /**
     * Remove an instance of a relation from the cache.
     */
    public void removeRelation(Triple t) {
    	Node start = t.getSubject();
    	Node end = t.getObject();
    	if (start == end) {
    		return;		// Reflexive case is built in
    	}
    	GraphNode startN = getLead(start);
    	GraphNode endN = getLead(end);
    	if (startN != endN && !(startN.directPathTo(endN))) {
    		// indirect link can't be removed by itself
    		return;
    	}
    	// This is a remove of a direct link possibly within an SCC
    	// Delay as long as possible and do deletes in a batch
    	if (deletesPending == null) {
    		deletesPending = new HashSet<>();
    	}
    	deletesPending.add(t);
    }

    /**
     * Process outstanding delete actions
     */
    private void processDeletes() {
    	// The kernel is the set of start nodes of deleted links
    	Set<GraphNode> kernel = new HashSet<>();
        for ( Triple t : deletesPending )
        {
            GraphNode start = nodeMap.get( t.getSubject() );
            kernel.add( start );
        }
    	
    	// The predecessor set of kernel
    	Set<GraphNode> pKernel = new HashSet<>();
    	pKernel.addAll(kernel);
        for ( GraphNode n : nodeMap.values() )
        {
            for ( Iterator<GraphNode> j = kernel.iterator(); j.hasNext(); )
            {
                GraphNode target = j.next();
                if ( n.pathTo( target ) )
                {
                    pKernel.add( n );
                    break;
                }
            }
        }
    	
    	// Cut the pKernel away from the finge of nodes that it connects to
        for ( GraphNode n : pKernel )
        {
            for ( Iterator<GraphNode> j = n.succ.iterator(); j.hasNext(); )
            {
                GraphNode fringe = j.next();
                if ( !pKernel.contains( fringe ) )
                {
                    fringe.pred.remove( n );
                }
            }
            n.succ.clear();
            n.succClosed.clear();
            n.pred.clear();
        }
    	
    	// Delete the triples
    	originalTriples.removeAll(deletesPending);
    	deletesPending.clear();
    	
    	// Reinsert the remaining links
        for ( Triple t : originalTriples )
        {
            GraphNode n = nodeMap.get( t.getSubject() );
            if ( pKernel.contains( n ) )
            {
                addRelation( t );
            }
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
    @Override
    public ExtendedIterator<Triple> findWithContinuation(TriplePattern pattern, Finder continuation) {
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
     * Return true if the given pattern occurs somewhere in the find sequence.
     */
    @Override
    public boolean contains(TriplePattern pattern) {
        ClosableIterator<Triple> it = find(pattern);
        boolean result = it.hasNext();
        it.close();
        return result;
    }
    /**
     * Return an iterator over all registered subject nodes
     */
    public ExtendedIterator<Node> listAllSubjects() {
        return WrappedIterator.create(nodeMap.keySet().iterator());
    }
   
    /**
     * Return true if the given Node is registered as a subject node
     */
    public boolean isSubject(Node node) {
        return nodeMap.keySet().contains(node);
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
        ExtendedIterator<Triple> it = graph.find(new TriplePattern(null, predicate, null));
        boolean foundsome = it.hasNext();
        while (it.hasNext()) {
            addRelation(it.next());
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
    @Override
    public ExtendedIterator<Triple> find(TriplePattern pattern) {
    	if (deletesPending != null && deletesPending.size() > 0) {
    		processDeletes();
    	}

    	Node s = pattern.getSubject();
        Node p = pattern.getPredicate();
        Node o = pattern.getObject();
        
        if (p.isVariable() || p.equals(directPredicate) || p.equals(closedPredicate)) {
            boolean closed = !p.equals(directPredicate);
            Node pred = closedPredicate; // p.isVariable() ? closedPredicate : p;
            if (s.isVariable()) {
                if (o.isVariable()) {
                    // list all the graph contents
//                    ExtendedIterator result = null;
//                    for (Iterator i = nodeMap.values().iterator(); i.hasNext(); ) {
//                        ExtendedIterator nexti = ((GraphNode)i.next()).listTriples(closed, this);
//                        if (result == null) {
//                            result = nexti;
//                        } else {
//                            result = result.andThen(nexti);
//                        }
//                    }
//                    if (result == null) {
//                        return NullIterator.instance;
//                    }
                    return new FullGraphWalker(closed, closedPredicate, nodeMap);
                } else {
                    // list all backwards from o
                    GraphNode gn_o = nodeMap.get(o);
                    if (gn_o == null) return NullIterator.instance();
                    return gn_o.listPredecessorTriples(closed, this);
                }
            } else {
                GraphNode gn_s = nodeMap.get(s);
                if (gn_s == null) return NullIterator.instance();
                if (o.isVariable()) {
                    // list forward from s
                    return gn_s.listTriples(closed, this);
                } else {
                    // Singleton test
                    GraphNode gn_o = nodeMap.get(o);
                    gn_s = gn_s.leadNode();
                    if (gn_o == null) return NullIterator.instance();
                    gn_o = gn_o.leadNode();
                    if ( closed ? gn_s.pathTo(gn_o) : gn_s.directPathTo(gn_o) ) {
                        return new SingletonIterator<>(new Triple(s, pred, o));
                    } else {
                        return NullIterator.instance();
                    }
                }
            }
        } else {
            // No matching triples in this cache
            return NullIterator.instance();
        }
    }
    
    /**
     * Create a deep copy of the cache contents.
     * Works by creating a completely new cache and just adding in the
     * direct links.
     */
    public TransitiveGraphCache deepCopy() {
        TransitiveGraphCache copy = new TransitiveGraphCache(directPredicate, closedPredicate);
        Iterator<Triple> i = find(new TriplePattern(null, directPredicate, null));
        while (i.hasNext()) {
            Triple t = i.next();
            copy.addRelation(t.getSubject(), t.getObject());
        }
        return copy;
    }
    
    /**
     * Clear the entire cache contents. 
     */
    public void clear() {
        nodeMap.clear();
    }
	
    /**
     * Enable/disabling caching of the Triples representing the relationships. If this is
     * enabled then a number of triples quadratic in the graph depth will be stored. If it
     * is disabled then all queries will turn over storage dynamically creating the result triples.
     */
    public void setCaching(boolean enable) {
    	if (! enable && cacheTriples) {
    		// Switching off so clear the existing cache
            for ( GraphNode graphNode : nodeMap.values() )
            {
                graphNode.clearTripleCache();
            }
    	}
    	cacheTriples = enable;
    }
    
    /**
     * Dump a description of the cache to a string for debug.
     */
    public String dump() {
    	StringBuffer sb = new StringBuffer();
        for ( GraphNode n : nodeMap.values() )
        {
            sb.append( n.dump() );
            sb.append( "\n" );
        }
    	return sb.toString();
    }
    
//  ----------------------------------------------------------------------
//  Internal utility methods    
//  ----------------------------------------------------------------------
    
    /**
     * Return the lead node of the strongly connected component corresponding
     * to the given RDF node. 
     */
    private GraphNode getLead(Node n) {
    	GraphNode gn = nodeMap.get(n);
        if (gn == null) {
            gn = new GraphNode(n);
            nodeMap.put(n, gn);
            return gn;
        } else {
            return gn.leadNode();
        }
    }
    
}
