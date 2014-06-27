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

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveGraphCache.*;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.util.iterator.*;

/**
	 * Inner class used to represent the graph node structure.
	 * Rather fat nodes (four sets)
	 */
class GraphNode {
    /** The RDF Graph Node this corresponds to */
    protected Node rdfNode;
    
	/** The list of direct successor nodes to this node */
	protected Set<GraphNode> succ = new HashSet<>();
	
	/** The list of direct predecessors nodes */
	protected Set<GraphNode> pred = new HashSet<>();
	
	/** The set of all transitive successor nodes to this node */
	protected Set<GraphNode> succClosed = new HashSet<>();
	
	/** An optional cache of the triples that represent succClosed */
	protected List<Triple> succClosedTriples;
	
	/** 
	     Plain Siblings for simple nodes. For the lead node in a SCC will be a Leader
	     with components containing all the nodes in the SCC. For non-lead nodes 
	     it will be a Subordinate referring to the lead node. 
	*/
	private Siblings siblings = new Siblings();
	
	static class Siblings
	    {	    
	    Set<GraphNode> members() 
	        { throw new BrokenException( "cannot ask for components of a raw GraphNode" ); }
	    
	    void addInto( Set<GraphNode> nodes, GraphNode m )
	        { nodes.add( m ); }
	    
	    void addSuccessors( Node base, TransitiveGraphCache tgc, ArrayList<Triple> result )
	        {}
	    
	    static final NullIterator<GraphNode> noMembers = NullIterator.instance();
	    
	    Iterator<GraphNode> siblingIterator()
	        { return noMembers; } 
	    
	    GraphNode leadNode( GraphNode unlessSpecified )
	        { return unlessSpecified; }
	    
	    String dump()
	        { return ""; }
	    }
	
	static class Leader extends Siblings
	    {
	    final Set<GraphNode> components;
	    
	    Leader( Set<GraphNode> components ) 
	        { this.components = components; }
	    
        @Override Set<GraphNode> members() 
            { return components; }
        
        @Override void addInto( Set<GraphNode> nodes, GraphNode ignored )
            { nodes.addAll( components ); }
        
        @Override void addSuccessors( Node base, TransitiveGraphCache tgc, ArrayList<Triple> result )
            {
                for ( GraphNode component : components )
                {
                    result.add( new Triple( base, tgc.closedPredicate, component.rdfNode ) );
                }
            }
        
        @Override Iterator<GraphNode> siblingIterator()
            { return components.iterator(); }
        
        @Override String dump()
            { return " SCC=" + dumpSet( components ) +", "; }
	    }
	
	static class Subordinate extends Siblings
	    {
	    final GraphNode leader;
	    
	    Subordinate( GraphNode n ) 
	        { leader = n; }
	    
        @Override Set<GraphNode> members() 
            { throw new BrokenException( "cannot ask for components of a raw GraphNode" ); }
        
        @Override GraphNode leadNode( GraphNode unlessSpecified )
            { return leader.leadNode(); }
        
        @Override String dump()
            { return " leader=" + leader + ", "; }
	    }
	
	// should only be called on a lead node
	private Set<GraphNode> siblings()
	    { return siblings.members(); }

    private void addSiblings( Set<GraphNode> target, GraphNode m )
        { m.siblings.addInto( target, m ); }

    /**
     * Return the lead node in the strongly connected component containing this node.
     * It will be the node itself if it is a singleton or the lead node. 
     */
    public GraphNode leadNode() 
        { return siblings.leadNode( this ); }
    
    public Iterator<GraphNode> siblingIterator()
        { return siblings.siblingIterator(); }
    
    public Iterator<GraphNode> concatenateSiblings( Iterator<GraphNode> base )
        { 
    		return WrappedIterator.create( base ).andThen( siblings.siblingIterator() );
        }

    private void becomeSubordinateOf( GraphNode leader )
        { this.siblings = new Subordinate( leader ); }

    private void becomeLeaderOf( Set<GraphNode> newSiblings )
        { this.siblings = new Leader( newSiblings ); }
    
    /**
     * Full dump for debugging
     */
    public String dump() {
        return 
            rdfNode.getLocalName()
            + siblings.dump()
            + " succ=" + dumpSet( succ ) 
            + ", succClose=" + dumpSet( succClosed ) 
            + ", pred=" + dumpSet( pred )
            ;
    }

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
        if (this == A) return true;
        return succClosed.contains(A);
    }

    /**
     * Return true if there is a direct path from this node to the argument node.
     */
    public boolean directPathTo(GraphNode A) {
        if (this == A) return true;
        return succ.contains(A);
    }

	
	/**
	 * Visit each predecessor of this node applying the given visitor.
	 */
	public <Alpha, Beta> void visitPredecessors(Visitor<Alpha, Beta> visitor, Alpha arg1, Beta arg2) {
        List<GraphNode> kill = visitor.visit(this, null, arg1, arg2);
        if (kill != null)  pred.removeAll(kill);
		doVisitPredecessors(visitor, arg1, arg2, new HashSet<GraphNode>());
	}
	
	/**
	 * Visit each predecessor of this node applying the given visitor.
     * Breadth first.
	 */
	private <Alpha, Beta> void doVisitPredecessors(Visitor<Alpha, Beta> visitor, Alpha arg1, Beta arg2, Set<GraphNode> seen) {
		if (seen.add(this)) {
            Collection<GraphNode> allKill = null;
            for (Iterator<GraphNode> i = pred.iterator(); i.hasNext(); ) {
                GraphNode pred = i.next();
                List<GraphNode> kill = visitor.visit(pred, this, arg1, arg2);
                if (kill != null) {
                    if (allKill == null) allKill = new ArrayList<GraphNode>();
                    allKill.addAll(kill);
                }
            }
            if (allKill != null) pred.removeAll(allKill);
            for (Iterator<GraphNode> i = pred.iterator(); i.hasNext(); ) {
                GraphNode pred = i.next();
                pred.doVisitPredecessors(visitor, arg1, arg2, seen);
            }
		}
	}
	
	/**
	 * Return an iterator over all the indirect successors of this node.
     * This does NOT include aliases of successors and is used for graph maintenance.
	 */
	public Iterator<GraphNode> iteratorOverSuccessors() {
		return succClosed.iterator();
	}
	
	/**
	 * Assert a direct link between this node and this given target.
	 * Does not update the closed successor cache
	 */
	public void assertLinkTo(GraphNode target) {
        if (this == target) return;
		succ.add(target);
		target.pred.add(this);
		clearTripleCache();
	}
	
	/**
	 * Remove a direct link currently from this node to the given target.
	 * Does not update the closed successor cache.
	 */
	public void retractLinkTo(GraphNode target) {
        if (this == target) return;
		succ.remove(target);
		target.pred.remove(this);
		clearTripleCache();
	}
	
	/**
	 * Assert an inferred indirect link from this node to the given traget
	 */
	public void assertIndirectLinkTo(GraphNode target) {
//            if (this == target) return;
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
	 * Propagate the results of adding a link from this
	 * node to the target node.
	 */
	public void propagateAdd(GraphNode target) {
        Set<GraphNode> sc = new HashSet<>(target.succClosed);
        sc.add(target); 
		visitPredecessors(new Visitor<Set<GraphNode>, GraphNode>() {
			@Override
            public List<GraphNode> visit(GraphNode node, GraphNode processing, Set<GraphNode> sc, GraphNode target) {
				// Add closure
				node.succClosed.addAll( sc );
				// Scan for redundant links
                List<GraphNode> kill = null;
				for (Iterator<GraphNode> i = node.succ.iterator(); i.hasNext();) {
					GraphNode s = i.next();
					if (sc.contains(s)) {
						i.remove();
                        if (s == processing) {
                            // Can't remove immediately w/o beaking the visitor loop
                            if (kill == null) kill = new ArrayList<>();
                            kill.add(node);
                        } else {
                            s.pred.remove(node);
                        }
					}
				}
                return kill;
			}
	    }, sc, target);
	}
    
	/**
	 * Propagate the results of creating a new SCC with this
	 * node as lead.
	 */
	public void propagateSCC() {
		Set<GraphNode> visited = new HashSet<>();
		visited.add(this);
		// Scan predecessors not including ourselves
		doVisitPredecessors(new Visitor<Set<GraphNode>, Object>() {
			@Override
            public List<GraphNode> visit(GraphNode node, GraphNode processing, Set<GraphNode> sc, Object ignored ) {
				// Add closure
				node.succClosed.addAll(sc);
				// Scan for redundant links
                List<GraphNode> kill = null;
				for (Iterator<GraphNode> i = node.succ.iterator(); i.hasNext();) {
					GraphNode s = i.next();
					if (sc.contains(s)) {
						i.remove();
//                            s.pred.remove(node);
                        if (s == processing) {
                            // Can't remove immediately w/o beaking the visitor loop
                            if (kill == null) kill = new ArrayList<>();
                            kill.add(node);
                        } else {
                            s.pred.remove(node);
                        }
					}
				}
                return kill;
			}
	    }, succClosed, null, visited);
	}
	
    /**
     * Given a set of SCC nodes make this the lead member of the SCC and
     * reroute all incoming and outgoing links accordingly.
     * This eager rewrite is based on the assumption that there are few cycles
     * so it is better to rewrite once and keep the graph easy to traverse.
     */
    public void makeLeadNodeFor(Set<GraphNode> members) {
        // Accumulate all successors
        Set<GraphNode> newSucc = new HashSet<>();
        Set<GraphNode> newSuccClosed = new HashSet<>();
        for ( GraphNode n : members )
        {
            newSucc.addAll( n.succ );
            newSuccClosed.addAll( n.succClosed );
        }
        newSucc.removeAll(members);
        newSuccClosed.removeAll(members);
        succ = newSucc;
        succClosed = newSuccClosed;
        
        // Rewrite all direct successors to have us as predecessor
        for ( GraphNode n : succ )
        {
            n.pred.removeAll( members );
            n.pred.add( this );
        }
        
        // Find all predecessor nodes and relink link them to point to us
        Set<GraphNode> done = new HashSet<>();
        Set<GraphNode> newAliases = new HashSet<>();
        for ( GraphNode member : members )
        {
            addSiblings( newAliases, member );
        }
        becomeLeaderOf( newAliases );
        for ( GraphNode n : members )
        {
            if ( n != this )
            {
                pred.addAll( n.pred );
                n.relocateAllRefTo( this, done );
                n.becomeSubordinateOf( this );
            }
        }
        pred.removeAll(members);
    }

    /**
     * This node is being absorbed into an SCC with the given node as the
     * new lead node. Trace out all predecessors to this node and relocate
     * them to point to the new lead node.
     */
    private void relocateAllRefTo(GraphNode lead, Set<GraphNode> done) {
        visitPredecessors(new Visitor<Set<GraphNode>, GraphNode>(){
            @Override
            public List<GraphNode> visit(GraphNode node, GraphNode processing, Set<GraphNode> done, GraphNode leadIn) {
                if (done.add( node )) {
                    Set<GraphNode> members = leadIn.siblings();
                    int before = node.succ.size();
                    node.succ.removeAll(members);
                    node.succClosed.removeAll(members);
                    node.succClosed.add(leadIn);
                    if (node.succ.size() != before) {
                        node.succ.add(leadIn);
                    }
                }
                return null;
            }
        }, done, lead);
    }
    
    /**
     * Return an iterator over all of the triples representing outgoing links
     * from this node.  
     * @param closed if set to true it returns triples in the transitive closure,
     * if set to false it returns triples in the transitive reduction
     * @param tgc the enclosing TransitiveGraphCache
     */
    public ExtendedIterator<Triple> listTriples(boolean closed, TransitiveGraphCache tgc) {
        if (tgc.cacheTriples) {
            // TODO implement - for now default to non-cached
            return WrappedIterator.create(leadNode().triplesForSuccessors(rdfNode, closed, tgc).iterator());
        } else {
            return WrappedIterator.create(leadNode().triplesForSuccessors(rdfNode, closed, tgc).iterator());
        }
    }
    
    /**
     * Create a list of triples for a given set of successors to this node.
     */
    private List<Triple> triplesForSuccessors(Node base, boolean closed, TransitiveGraphCache tgc) {
        Set<GraphNode> successors = closed ? succClosed : succ;
        ArrayList<Triple> result = new ArrayList<>(successors.size() + 10);
        result.add(new Triple(base, tgc.closedPredicate, base));    // implicit reflexive case 
        for ( GraphNode s : successors )
        {
            result.add( new Triple( base, tgc.closedPredicate, s.rdfNode ) );
            s.siblings.addSuccessors( base, tgc, result );
        }
        siblings.addSuccessors( base, tgc, result );
        return result;
    }

    
    /**
     * Return an iterator over all of the triples representing incoming links to this node.
     * Currently no caching enabled.
     */
    public ExtendedIterator<Triple> listPredecessorTriples(boolean closed, TransitiveGraphCache tgc) {
        return new GraphWalker(leadNode(), rdfNode, closed, tgc.closedPredicate);
    }
    
    /**
     * Print node label to assist with debug.
     */
    @Override public String toString() {
        return "[" + rdfNode.getLocalName() + "]";
    }
    
    /**
     * Dump a set to a string for debug.
     */
    private static String dumpSet(Set<GraphNode> s) {
    	StringBuffer sb = new StringBuffer();
    	sb.append("{");
    	boolean started = false;
        for ( GraphNode value : s )
        {
            if ( started )
            {
                sb.append( ", " );
            }
            else
            {
                started = true;
            }
            sb.append( value.toString() );
        }
    	sb.append("}");
    	return sb.toString();
    }
    
}
