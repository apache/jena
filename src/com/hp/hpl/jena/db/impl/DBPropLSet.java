/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
*/

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.DB;

import java.util.*;

/**
 *
 * A wrapper to assist in getting and setting DB information from 
 * a persistent store.
 * 
 * This is written in the style of enhanced nodes - no state is
 * stored in the DBStoreDesc, instead all state is in the
 * underlying graph and this is just provided as a convenience.
 * 
 * (We don't use enhanced nodes because, since we control everything
 * in the persistent store system description, we can avoid any
 * need to handle polymorhphism).
 * 
 * @since Jena 2.0
 * 
 * @author csayers
 * @version $Revision: 1.4 $
 */
public class DBPropLSet extends DBProp {

	public static Node_URI lSetName = (Node_URI)DB.lSetName.getNode();
	public static Node_URI lSetType = (Node_URI)DB.lSetType.getNode();
	public static Node_URI lSetPSet = (Node_URI)DB.lSetPSet.getNode();
	
	public DBPropLSet( SpecializedGraph g, String name, String type) {
		super( g);
		putPropString(lSetName, name);
		putPropString(lSetType, type);
	}
	
	public DBPropLSet( SpecializedGraph g, Node n) {
		super(g,n);
	}	
	
	public void setPSet( DBPropPSet pset ) {
		putPropNode( lSetPSet, pset.getNode() );
	}
	
	public String getName() { return self.getURI().substring(DB.getURI().length()); }
	public String getType() { return getPropString( lSetType); };
	
	public DBPropPSet getPset() {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		TripleMatch match = new StandardTripleMatch( self, lSetPSet, null);
		Iterator matches = graph.find(match, complete);
		if( matches.hasNext() ) {
			Triple t = (Triple)matches.next();
			return new DBPropPSet(graph, t.getObject());
		}
		else
			return null;
	}

	public void remove() {
		DBPropPSet pSet = getPset();
		if (pSet != null )
			pSet.remove();
		super.remove();
	}

	public ExtendedIterator listTriples() {
		// First get all the triples that directly desrcribe this graph
		ExtendedIterator result = DBProp.listTriples(graph, self);
		
		// Now get all the triples that describe the pset
		DBPropPSet pset = getPset();
		if( pset != null )
			result = result.andThen( DBProp.listTriples(graph, getPset().getNode()) );

		return result;
	}
	
	
}

/*
 *  (c) Copyright Hewlett-Packard Company 2003.
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