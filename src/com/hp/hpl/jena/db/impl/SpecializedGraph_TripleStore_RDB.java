/*
 *  (c) Copyright Hewlett-Packard Company 2003
 *  All rights reserved.
 *
 */


package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.JenaException;

/**
 *
 * @author  hkuno based on GraphMem.java, v 1.4 by Chris Dollin (chris-dollin)
 * @version 
 *
 * TripleStoreGraph_RDB is a table-specific, database-independent 
 * implementation of TripleStoreGraph.
 */

public class SpecializedGraph_TripleStore_RDB extends SpecializedGraph_TripleStore {
	
	/**
	 * Constructor.
	 * 
	 * Create a new instance of a TripleStore graph, taking an IPSet
	 * as an argument.  Used for bootstrapping, when we don't have a
	 * DBPropLSet yet.
	 */
	public SpecializedGraph_TripleStore_RDB(IPSet pSet, Integer dbGraphId) {
		super(pSet, dbGraphId);
	}
	
	public char subsumes ( Triple pattern, int reifBehavior ) {
			Node pred = pattern.getPredicate();
			char res = noTriplesForPattern;
			if ( pred.isConcrete() ) {
				// assumes that other sg's have been called first
				res = allTriplesForPattern;
			} else if ( (pred.isVariable()) || pred.equals(Node.ANY) ) {
				return reifBehavior == GraphRDB.OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING ?
					someTriplesForPattern : allTriplesForPattern;
			} else
				throw new JenaException("Unexpected predicate: " + pred.toString());
			return res;
		}
}

/*
 *  (c) Copyright Hewlett-Packard Company 2003
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
