/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: DBReifier.java,v 1.1 2003-05-02 16:54:55 csayers Exp $
*/

package com.hp.hpl.jena.db.impl;

/**
 *  Implementation of Reifier for graphs stored in a database.
 * 
 * @author csayers based in part on SimpleReifier by kers.
*/

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;

public class DBReifier implements Reifier
    {
    private Graph m_parent = null;
    
    // For now, we just deal with a single specializedGraphReifier,
    // but in the future we could replace this with a list of
    // those and operate much as the GraphRDB implementation
    // does with it's list of SpecializedGraphs.
    private SpecializedGraphReifier m_reifier = null;
    
    /** 
        Construct a reifier for GraphRDB's.
        
        @param parent the Graph which we're reifiying for
    */
    public DBReifier( Graph parent, SpecializedGraphReifier specializedReifierGraph) {
        m_parent = parent;
        m_reifier = specializedReifierGraph;
    }
            
    /* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#getParentGraph()
	 */
	public Graph getParentGraph() { 
    	return m_parent; }

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#getHiddenTriples()
	 */
	public Graph getHiddenTriples() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#reifyAs(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple)
	 */
	public Node reifyAs(Node n, Triple t) {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		m_reifier.add(n,t,complete);
		return n;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#hasTriple(com.hp.hpl.jena.graph.Node)
	 */
	public boolean hasTriple(Node n) {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		return m_reifier.findReifiedTriple(n, complete) != null;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#hasTriple(com.hp.hpl.jena.graph.Triple)
	 */
	public boolean hasTriple(Triple t) {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		TripleMatch match = new StandardTripleMatch(t.getSubject(), t.getPredicate(), t.getObject());
		return m_reifier.findReifiedNodes(match, complete).hasNext();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#allNodes()
	 */
	public ExtendedIterator allNodes() {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		TripleMatch match = new StandardTripleMatch(null, null, null);
		return m_reifier.findReifiedNodes(match, complete);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#remove(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple)
	 */
	public void remove(Node n, Triple t) {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		m_reifier.delete(n,t, complete);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#remove(com.hp.hpl.jena.graph.Triple)
	 */
	public void remove(Triple t) {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		m_reifier.delete(null,t, complete);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#handledAdd(com.hp.hpl.jena.graph.Triple)
	 */
	public boolean handledAdd(Triple t) {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		m_reifier.add(t, complete);
		return complete.isDone();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.Reifier#handledRemove(com.hp.hpl.jena.graph.Triple)
	 */
	public boolean handledRemove(Triple t) {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		m_reifier.delete(t, complete);
		return complete.isDone();
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.GetTriple#getTriple(com.hp.hpl.jena.graph.Node)
	 */
	public Triple getTriple(Node n) {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		return m_reifier.findReifiedTriple(n, complete);
	}
        
}
    
/*
    (c) Copyright Hewlett-Packard Company 200, 2003
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
