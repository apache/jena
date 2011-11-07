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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.DB;

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
 * @version $Revision: 1.1 $
 */
public class DBPropLSet extends DBProp {

	public static Node_URI lSetName = (Node_URI)DB.lSetName.asNode();
	public static Node_URI lSetType = (Node_URI)DB.lSetType.asNode();
	public static Node_URI lSetPSet = (Node_URI)DB.lSetPSet.asNode();
	
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
	public String getType() { return getPropString( lSetType); }
	
	public DBPropPSet getPset() {
		ClosableIterator<Triple> matches = graph.find( self, lSetPSet, null, newComplete() );
		if( matches.hasNext() ) {
			try { return new DBPropPSet( graph, matches.next().getObject()); }
            finally { matches.close(); }
		}
		else
			return null;
	}

	@Override
    public void remove() {
		DBPropPSet pSet = getPset();
		if (pSet != null )
			pSet.remove();
		super.remove();
	}

	public ExtendedIterator<Triple> listTriples() {
		// First get all the triples that directly desrcribe this graph
		ExtendedIterator<Triple> result = DBProp.listTriples(graph, self);
		
		// Now get all the triples that describe the pset
		DBPropPSet pset = getPset();
		if( pset != null )
			result = result.andThen( DBProp.listTriples(graph, getPset().getNode()) );

		return result;
	}
	
	
}
