/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
*/

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.*;
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
 * 
 * @author csayers
 * @version $Revision: 1.11 $
 * @since Jena 2.0
 */
public class DBPropGraph extends DBProp {

	public static Node_URI graphName = (Node_URI)DB.graphName.getNode();
	public static Node_URI graphType = (Node_URI)DB.graphType.getNode();
	public static Node_URI graphLSet = (Node_URI)DB.graphLSet.getNode();
	public static Node_URI graphPrefix = (Node_URI)DB.graphPrefix.getNode();
	public static Node_URI graphId = (Node_URI)DB.graphId.getNode();
	public static Node_URI stmtTable = (Node_URI)DB.stmtTable.getNode();
	public static Node_URI reifTable = (Node_URI)DB.reifTable.getNode();
	public static Node_URI graphDBSchema = (Node_URI)DB.graphDBSchema.getNode();


	
	public DBPropGraph( SpecializedGraph g, String symbolicName, String type) {
		super(g);
		
		putPropString(graphName, symbolicName);
		putPropString(graphType, type);
	}
	
	public DBPropGraph( SpecializedGraph g, Node n) {
		super(g,n);
	}	
	
	public DBPropGraph( SpecializedGraph g, String newSymbolicName, Graph oldProperties) {
		super(g);
		
		putPropString(graphName, newSymbolicName);
		// only copy user-configurable properties
		Iterator it = oldProperties.find( Node.ANY, Node.ANY, Node.ANY);
		while ( it.hasNext() ) {
			Triple t = (Triple) it.next();
			if ( t.getPredicate().equals(graphName) ||
				t.getPredicate().equals(graphId) ||
				t.getPredicate().equals(stmtTable) ||
				t.getPredicate().equals(reifTable) )
				continue;
			putPropNode((Node_URI)t.getPredicate(),t.getObject());
		}
	}

	
	public void addLSet( DBPropLSet lset ) {
		putPropNode( graphLSet, lset.getNode() );
	}

	public void addPrefix( DBPropPrefix prefix ) {
		// First drop existing uses of prefix or URI
		DBPropPrefix existing = getPrefix( prefix.getValue());
		if( existing != null)
			removePrefix( existing);
		existing = getURI( prefix.getURI());
		if( existing != null)
			removePrefix( existing);
		putPropNode( graphPrefix, prefix.getNode() );
	}
	
	public void removePrefix( DBPropPrefix prefix ) {
		SpecializedGraph.CompletionFlag complete = newComplete();
		Iterator matches = graph.find( self, graphPrefix, prefix.getNode(), complete);
		if( matches.hasNext() ) {
			graph.delete( (Triple)(matches.next()), complete );
			prefix.remove();
		}
	}
	
	public void addPrefix( String prefix, String uri ) {
		addPrefix( new DBPropPrefix( graph, prefix, uri) );
	}
	
	public void addGraphId( int id ) {
		putPropString(graphId, Integer.toString(id));
	}

	public void addStmtTable( String table ) {
		putPropString(stmtTable, table);
	}
	
	public void addDBSchema( String schemaName ) {
		putPropString(graphDBSchema, schemaName);
	}
	public void addReifTable( String table ) {
		putPropString(reifTable, table);
	}


	
	public String getName() { return getPropString( graphName); }
	public String getType() { return getPropString( graphType); };
	public String getStmtTable() { return getPropString(stmtTable); }
	public String getReifTable() { return getPropString(reifTable); }
	public int getGraphId() { return Integer.parseInt(getPropString(graphId)); };
	public String getDBSchema() { return getPropString(graphDBSchema); }


	
	public ExtendedIterator getAllLSets() {
		return 
            graph.find( self, graphLSet, null, newComplete() )
             .mapWith ( new MapToLSet() );
	}
	
	public ExtendedIterator getAllPrefixes() {
		return 
            graph.find( self, graphPrefix, null, newComplete() )
            .mapWith ( new MapToPrefix() );
	}
	
	public DBPropPrefix getPrefix( String value ) {
		ExtendedIterator prefixes = getAllPrefixes();
		while( prefixes.hasNext() ) {
			DBPropPrefix prefix = (DBPropPrefix)prefixes.next();
			if( prefix.getValue().compareTo(value)==0) 
				return prefix;
		}
		return null;
	}
	
	public DBPropPrefix getURI( String uri ) {
		ExtendedIterator prefixes = getAllPrefixes();
		while( prefixes.hasNext() ) {
			DBPropPrefix prefix = (DBPropPrefix)prefixes.next();
			if( prefix.getURI().compareTo(uri)==0) 
				return prefix;
		}
		return null;
	}
	
	public ExtendedIterator listTriples() {
		// First get all the triples that directly desrcribe this graph
		ExtendedIterator result = DBProp.listTriples( graph, self );
		
		// Now get all the triples that describe any lsets
		ExtendedIterator lsets = getAllLSets();
		while( lsets.hasNext()) {
			result = result.andThen( ((DBPropLSet)lsets.next()).listTriples() );
		}

		// Now get all the triples that describe any prefixes
		ExtendedIterator prefixes = getAllPrefixes();
		while( prefixes.hasNext()) {
			result = result.andThen( ((DBPropPrefix)prefixes.next()).listTriples() );
		}
		return result;
	}
	
	
	private class MapToLSet implements Map1 {
		public Object map1( Object o) {
			Triple t = (Triple) o;
			return new DBPropLSet( graph, t.getObject() );			
		}
	}
	
	private class MapToPrefix implements Map1 {
		public Object map1( Object o) {
			Triple t = (Triple) o;
			return new DBPropPrefix( graph, t.getObject() );			
		}
	}
	
	public static DBPropGraph findPropGraphByName( SpecializedGraph graph, String name ) {
		
		Node myNode = new Node_Literal( new LiteralLabel(name, ""));
		Iterator it =  graph.find( null, graphName, myNode, newComplete() );
		if( it.hasNext() )
			return new DBPropGraph( graph, ((Triple)it.next()).getSubject());
		else
			return null;
	}
	
	public void remove() {
		Iterator it = getAllPrefixes();
		while( it.hasNext()) {
			((DBPropPrefix)it.next()).remove();			
		}
		it = getAllLSets();
		while( it.hasNext()) {
			((DBPropLSet)it.next()).remove();			
		}
		super.remove();
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