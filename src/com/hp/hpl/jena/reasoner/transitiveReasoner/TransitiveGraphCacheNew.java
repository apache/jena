/******************************************************************
 * File:        TransitiveGraphCacheNew.java
 * Created by:  Dave Reynolds
 * Created on:  16-Nov-2004
 * 
 * (c) Copyright 2004, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TransitiveGraphCacheNew.java,v 1.2 2004-11-25 17:30:40 der Exp $
 *****************************************************************/

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
 * predecessors and successors and its closed successors. A big cost penalty 
 * is the storage turnover involved in turning the graph representation back into 
 * triples to answer queries. We avoid this by optionally also storing the
 * manifested triples for the links. The storage cost thus
 * scales L^2 where L is the length of the relation chains (e.g. the depth in the
 * subClass hiearachy). This could be reduced by using interval indexes (Agrawal, 
 * Borigda and Jagadish 1989) at the cost of complicating incremental inserts 
 * and dynamic storage turnover.
 * </p><p>
 * Cycles are currently handled by collapsing strongly connected components.
 * Incremental deletes would be possible but at the price of substanially 
 * more storage and code complexity. We compromise by doing the easy cases
 * incrementally but some deletes (those that break strongly connected components)
 * will trigger a fresh rebuild.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $
 */

// TODO: This version is a compromise between bug fixing earlier code,
// getting reasonable performance and cost, and speed of implementation.
// In an ideal world we'd create a test harness to measure the space and
// time costs in practice and compare variants on the implementation 
// including the internal index alternative

public class TransitiveGraphCacheNew {

	/** Flag controlling the whether the triples 
	 *  representing the closed relation should also be cached. */
	protected boolean cacheTriples = false;
	
    /** Map from RDF Node to the corresponding Graph node. */
    protected HashMap nodeMap = new HashMap();
    
    /** The RDF predicate representing the direct relation */
    protected Node directPredicate;
    
    /** The RDF predicate representing the closed relation */
    protected Node closedPredicate;
	
    /**
     * Inner class used to represent vistors than can be applied to each
     * node in a graph walk.
     */
    static interface Visitor {
    	void visit(GraphNode node, Object arg1, Object arg2);
    }
    
	/**
	 * Inner class used to represent the graph node structure.
	 */
	private static class GraphNode {
        /** The RDF Graph Node this corresponds to */
        protected Node rdfNode;
        
		/** The list of direct successor nodes to this node */
		protected Set succ = new HashSet();
		
		/** The list of direct predecessors nodes */
		protected Set pred = new HashSet();
		
		/** The set of all transitive successor nodes to this node */
		protected Set succClosed = new HashSet();
		
		/** An optional cache of the triples that represent succClosed */
		protected List succClosedTriples;
		
		/** Null for simple nodes. For the lead node in a SCC will be a list
		 * of all the nodes in the SCC. For non-lead nodes it will be a ref to the lead node. */
		protected Object aliases;

        /**
         * Constructor.
         */
        public GraphNode(Node node) {
            rdfNode = node;
        }
        
        /**
         * Return true if there is a path from this node to the argument node.
         */
        public boolean pathTo(GraphNode A) {
            return succClosed.contains(A);
        }

        /**
         * Return true if there is a direct path from this node to the argument node.
         */
        public boolean directPathTo(GraphNode A) {
            return succ.contains(A);
        }
		
		/**
		 * Return the lead node in the strongly connected component containing this node.
		 * It will be the node itself if it is a singleton or the lead node. 
		 */
		public GraphNode leadNode() {
			if (aliases != null && aliases instanceof GraphNode) {
				return (GraphNode)aliases;
			} else {
				return this;
			}
		}
		
		/**
		 * Visit each predecessor of this node applying the given visitor.
		 */
		public void visitPredecessors(Visitor visitor, Object arg1, Object arg2) {
			doVisitPredecessors(visitor, arg1, arg2, new HashSet());
		}
		
		/**
		 * Visit each predecessor of this node applying the given visitor.
		 */
		private void doVisitPredecessors(Visitor visitor, Object arg1, Object arg2, Set seen) {
			if (seen.add(this)) {
				visitor.visit(this, arg1, arg2);
				for (Iterator i = pred.iterator(); i.hasNext(); ) {
					GraphNode pred = (GraphNode)i.next();
					pred.doVisitPredecessors(visitor, arg1, arg2, seen);
				}
			}
		}
		
		/**
		 * Return an iterator over all the indirect successors of this node.
		 */
		public Iterator iteratorOverSuccessors() {
			return succClosed.iterator();
		}
		
		/**
		 * Assert a direct link between this node and this given target.
		 * Does not update the closed successor cache
		 */
		public void assertLinkTo(GraphNode target) {
			succ.add(target);
			target.pred.add(this);
			clearTripleCache();
		}
		
		/**
		 * Remove a direct link currently from this node to the given target.
		 * Does not update the closed successor cache.
		 */
		public void retractLinkTo(GraphNode target) {
			succ.remove(target);
			target.pred.remove(this);
			clearTripleCache();
		}
		
		/**
		 * Assert an inferred indirect link from this node to the given traget
		 */
		public void assertIndirectLinkTo(GraphNode target) {
			succClosed.add(target);
			clearTripleCache();
		}
		
		/**
		 * Clear the option cache of the closure triples.
		 */
		public void clearTripleCache() {
			succClosedTriples = null;
		}
        
        /**
         * Given a set of SCC nodes make this the lead member of the SCC and
         * reroute all incoming and outgoing links accordingly.
         * This eager rewrite is based on the assumption that there are few cycles
         * so it is better to rewrite once and keep the graph easy to traverse.
         */
        public void makeLeadNodeFor(Set members) {
            // Accumulate all successors
            Set newSucc = new HashSet();
            Set newSuccClosed = new HashSet();
            for (Iterator i = members.iterator(); i.hasNext(); ) {
                GraphNode n = (GraphNode)i.next();
                newSucc.addAll(n.succ);
                newSuccClosed.addAll(n.succClosed);
            }
            succ = newSucc;
            succClosed = newSuccClosed;
            
            // Rewrite all direct successors to have us as predecessor
            for (Iterator i = succ.iterator(); i.hasNext();) {
                GraphNode n = (GraphNode)i.next();
                n.pred.removeAll(members);
                n.pred.add(this);
            }
            
            // Find all predecessor nodes and relink link them to point to us
            Set done = new HashSet();
            this.aliases = members;
            for (Iterator i = members.iterator(); i.hasNext(); ) {
                GraphNode n = (GraphNode)i.next();
                pred.addAll(n.pred);
                n.relocateAllRefTo(this, done);
                n.aliases = this;
            }
        }
		
        /**
         * This node is being absorbed into an SCC with the given node as the
         * new lead node. Trace out all predecessors to this node and relocate
         * them to point to the new lead node.
         */
        private void relocateAllRefTo(GraphNode lead, Set done) {
            visitPredecessors(new Visitor(){
                public void visit(GraphNode node, Object done, Object leadIn) {
                    if (((Set)done).add(node)) {
                        GraphNode lead = (GraphNode)leadIn;
                        Set members = (Set)lead.aliases;
                        int before = node.succ.size();
                        node.succ.removeAll(members);
                        node.succClosed.removeAll(members);
                        node.succClosed.add(lead);
                        if (node.succ.size() != before) {
                            node.succ.add(lead);
                        }
                    }
                }
            }, done, lead);
        }
        
        /**
         * Return an iterator over all of the triples representing outgoing links
         * from this node.  
         * @param closed if set to true it returns triples in the transitive closure,
         * if set to false it returns triples in the transitive reduction
         * @param cache the enclosing TransitiveGraphCache
         */
        public ExtendedIterator listTriples(boolean closed, TransitiveGraphCacheNew tgc) {
            if (tgc.cacheTriples) {
                // TODO implement
                throw new ReasonerException("Not yet implemented triple result caching");
            } else {
                if (closed) {
                    return WrappedIterator.create(triplesForSuccessors(succClosed, tgc).iterator());
                } else {
                    return WrappedIterator.create(triplesForSuccessors(succ, tgc).iterator());
                }
            }
        }
        
        /**
         * Create a list of triples for a given set of successors to this node.
         */
        private List triplesForSuccessors(Set successors, TransitiveGraphCacheNew tgc) {
            ArrayList result = new ArrayList(successors.size());
            for (Iterator i = successors.iterator(); i.hasNext(); ) {
                result.add(new Triple(rdfNode, tgc.closedPredicate, ((GraphNode)i.next()).rdfNode));
            }
            return result;
        }
        
        /**
         * Return an iterator over all of the triples representing incoming links to this node.
         * Currently no caching enabled.
         */
        public ExtendedIterator listPredecessorTriples(boolean closed, TransitiveGraphCacheNew tgc) {
            return new GraphWalker(this, closed, tgc.closedPredicate);
        }
        
	} // End of GraphNode inner class
	
    /**
     * Inner class used to walk backward links of the graph.
     * <p> The triples are dynamically allocated which is costly. 
     */
    private static class GraphWalker extends NiceIterator implements ExtendedIterator {
        
        /** Indicate if this is a shallow or deep walk */
        boolean isDeep;
        
        /** The current node being visited */
        GraphNode node;
        
        /** The root node for reconstructing triples */
        Node root;
        
        /** The predicate for reconstructing triples */
        Node predicate; 
        
        /** Iterator over the predecessors to the current node bein walked */
        Iterator iterator = null;
        
        /** stack of graph nodes being walked */
        ArrayList nodeStack = new ArrayList();
        
        /** stack of iterators for the higher nodes in the walk */
        ArrayList iteratorStack = new ArrayList();
        
        /** The next value to be returned */
        Triple next;
        
        /** The set of junction nodes already visited */
        HashSet visited = new HashSet();
        
        /** 
         * Constructor. Creates an iterator which will walk
         * the graph, returning triples.
         * @param node the starting node for the walk
         * @param closed set to true of walking the whole transitive closure
         * @param predicate the predicate to be walked
         */
        GraphWalker(GraphNode node, boolean closed, Node predicate) {
            isDeep = closed;
            this.node = node;
            this.root = node.rdfNode;
            this.predicate = predicate;
            this.iterator = node.pred.iterator();
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
            if (iterator.hasNext()) {
                GraphNode nextNode = (GraphNode)iterator.next();
                if (visited.add(nextNode)) {
                    // Set up for depth-first visit next
                    if (isDeep)
                        pushStack(nextNode);
                    next =  new Triple(nextNode.rdfNode, predicate, root);
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
            iterator = (Iterator) iteratorStack.remove(i);
            node = (GraphNode) nodeStack.remove(i);
        }
        
    } // End of GraphWalker inner class    
    
    /**
     * Constructor - create a new cache to hold the given relation information.
     * @param directPredicate The RDF predicate representing the direct relation
     * @param closedPredicate The RDF predicate representing the closed relation
     */
    public TransitiveGraphCacheNew(Node directPredicate, Node closedPredicate) {
        this.directPredicate = directPredicate;
        this.closedPredicate = closedPredicate;
    }
    
    /**
     * Register a new relation instance in the cache
     */
    public void addRelation(Node start, Node end) {
    	GraphNode startN = getLead(start);
    	GraphNode endN = getLead(end);
    	boolean needJoin = endN.pathTo(startN);
    	
    	// Check if this link is already known about
    	if (startN.pathTo(endN)) {
    		// yes, so no work to do
    		return;
    	} else {
    		startN.assertLinkTo(endN);
    	}
    	
    	// Walk all predecessors of start retracting redundant direct links
    	// and adding missing closed links
    	startN.visitPredecessors(new Visitor() {
    		public void visit(GraphNode node, Object arg1, Object arg2) {
    			GraphNode target = (GraphNode)arg1;
    			if (node.pathTo(target)) {
    				// This is a redundant link 
    				node.retractLinkTo(target);
    			} else {
    				// Propagate closure
    				LinkedList taskStack = new LinkedList();
    				taskStack.addLast(target);
    				while ( ! taskStack.isEmpty()) {
    					GraphNode next = (GraphNode) taskStack.removeLast();
    					node.assertIndirectLinkTo(next);
    					for (Iterator i = next.iteratorOverSuccessors(); i.hasNext(); ) {
    						GraphNode s = (GraphNode)i.next();
    						if (node.pathTo(s)) {
    							node.retractLinkTo(s);
    						} else {
    							taskStack.addLast(s);
    						}
    					}
    				}
    			}
    		}
    	}, endN, null);
    	
    	if (needJoin) {
    		// Create a new strongly connected component
    		
    		// First find all the members of the component
    		Set members = new HashSet();
            members.add(startN);
    		endN.visitPredecessors(new Visitor() {
    			public void visit(GraphNode node, Object members, Object startN) {
    				if (((GraphNode)startN).pathTo(node)) ((Set)members).add(node);
    			} }, members, startN);
    		
            startN.makeLeadNodeFor(members);
    	}
    }
    
    /**
     * Remove an instance of a relation from the cache.
     */
    public void removeRelation(Node start, Node end) {
    	// TODO implement
        throw new ReasonerException("Call to unimplemented remove method in TransitiveGraphCache");
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
                    ExtendedIterator result = null;
                    for (Iterator i = nodeMap.values().iterator(); i.hasNext(); ) {
                        ExtendedIterator nexti = ((GraphNode)i.next()).listTriples(closed, this);
                        if (result == null) {
                            result = nexti;
                        } else {
                            result = result.andThen(nexti);
                        }
                    }
                    return result;
                } else {
                    // list all backwards from o
                    GraphNode gn_o = (GraphNode)nodeMap.get(o);
                    if (gn_o == null) return NullIterator.instance;
                    return gn_o.listPredecessorTriples(closed, this);
                }
            } else {
                GraphNode gn_s = getLead(s);
                if (gn_s == null) return NullIterator.instance;
                if (o.isVariable()) {
                    // list forward from s
                    return gn_s.listTriples(closed, this);
                } else {
                    // Singleton test
                    GraphNode gn_o = getLead(o);
                    if (gn_o == null) return NullIterator.instance;
                    if ( closed ? gn_s.pathTo(gn_o) : gn_s.directPathTo(gn_o) ) {
                        return new SingletonIterator(new Triple(s, pred, o));
                    } else {
                        return NullIterator.instance;
                    }
                }
            }
        } else {
            // No matching triples in this cache
            return NullIterator.instance;
        }
    }
    
    /**
     * Create a deep copy of the cache contents.
     * Works by creating a completely new cache and just adding in the
     * direct links.
     */
    public TransitiveGraphCacheNew deepCopy() {
        TransitiveGraphCacheNew copy = new TransitiveGraphCacheNew(directPredicate, closedPredicate);
        Iterator i = find(new TriplePattern(null, directPredicate, null));
        while (i.hasNext()) {
            Triple t = (Triple)i.next();
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
    		for (Iterator i = nodeMap.values().iterator(); i.hasNext(); ) {
    			((GraphNode)i.next()).clearTripleCache();
    		}
    	}
    	cacheTriples = enable;
    }
    
//  ----------------------------------------------------------------------
//  Internal utility methods    
//  ----------------------------------------------------------------------
    
    /**
     * Return the lead node of the strongly connected component corresponding
     * to the given RDF node. 
     */
    private GraphNode getLead(Node n) {
    	GraphNode gn = (GraphNode)nodeMap.get(n);
    	return gn.leadNode();
    }
    
}


/*
    (c) Copyright 2004 Hewlett-Packard Development Company, LP
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
