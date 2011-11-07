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
