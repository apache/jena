/*
 *  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 */
package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.RDF;

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
	
	/*
	 *  (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.SpecializedGraph#subsumes(com.hp.hpl.jena.graph.Triple, int)
	 *
	 * determine if the default graph has any triples of the given pattern.
	 * the table below indicates the return value for each reif style for
	 * the various types of patterns. 
	 * note: "conc" means the node in the pattern is not a concrete node.
	 * 
	 * Pattern                Minimal   Conv     Standard
	 * ANY rdf:subj ANY       all       none     none
	 * ANY rdf:pred ANY       all       none     none
	 * ANY rdf:obj  ANY       all       none     none
	 * ANY rdf:type rdf:stmt  all       none     none
	 * ANY rdf:type conc      all       all      all
	 * ANY rdf:type !conc     all       all      some
	 * ANY !conc    ANY       all       all      some
	 * else                   all       all      all
	 */
	
	@Override
    public char subsumes ( Triple pattern, int reifBehavior ) {
		// we assume that other sg's have been called first
		char res = allTriplesForPattern;
		if ( reifBehavior == GraphRDB.OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS )
			return res;
		Node pred = pattern.getPredicate();
		boolean isReifPred = pred.equals(RDF.Nodes.subject) ||
			pred.equals(RDF.Nodes.predicate) ||
			pred.equals(RDF.Nodes.object);
		boolean isPredType = pred.equals(RDF.Nodes.type);
		Node obj = pattern.getObject();
		boolean isObjStmt = obj.equals(RDF.Nodes.Statement);
		if ( reifBehavior == GraphRDB.OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING ) {
			if ( isReifPred ) res = noTriplesForPattern;
			else if ( isPredType ) {
				if ( isObjStmt ) res = noTriplesForPattern;
				else if ( !obj.isConcrete() ) res = someTriplesForPattern;
			} if ( !pred.isConcrete() ) res = someTriplesForPattern;
		} else {
			// reifBehavior == OPTIMIZE_AND_HIDE_FULL_AND_PARTIAL_REIFICATIONS
			if ( isReifPred || (isPredType && isObjStmt) )
				res = noTriplesForPattern;
		}
			return res;
		}
}

/*
 *  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
