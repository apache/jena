/******************************************************************
 * File:        TransitiveGraphCacheNew.java
 * Created by:  Dave Reynolds
 * Created on:  16-Nov-2004
 * 
 * (c) Copyright 2004, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TransitiveGraphCacheNew.java,v 1.1 2004-11-16 16:06:49 der Exp $
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
 * @version $Revision: 1.1 $
 */

// TODO: This version is a compromise between bug fixing earlier code,
// getting reasonable performance and cost, and speed of implementation.
// In an ideal world we'd create a test harness to measure the space and
// time costs in practice and compare variants on the implementation 
// including the internal index alternative

public class TransitiveGraphCacheNew {

	/** Flag controlling the whether the triples 
	 *  representing the closed relation should also be cached. */
	protected boolean cacheTriples = true;
	
    /** Map from RDF Node to the corresponding Graph node. */
    protected HashMap nodeMap = new HashMap();
    
    /** The RDF predicate representing the direct relation */
    protected Node directPredicate;
    
    /** The RDF predicate representing the closed relation */
    protected Node closedPredicate;
	
	/**
	 * Inner class used to represent the graph node structure.
	 */
	static class GraphNode {
		/** The list of direct successor nodes to this node */
		protected List succ;
		
		/** The list of direct predecessors nodes */
		protected List pred;
		
		/** The set of all transitive successor notes to this node */
		protected Set succClosed;
		
		/** An optional cache of the triples that represent succClosed */
		protected List succClosedTriples;
		
		/** The alias for this node in its strongly connected component, if any */
		protected List aliases;
		
	}
}


/*
    (c) Copyright 2002 Hewlett-Packard Development Company, LP
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
