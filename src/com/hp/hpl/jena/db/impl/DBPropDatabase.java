/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
*/

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.DB;

import java.text.SimpleDateFormat;
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
 * @version $Revision: 1.4 $
 */
public class DBPropDatabase extends DBProp {

	/**
	 * @since Jena 2.0
	 */

	public static final Node_URI dbEngineType = (Node_URI)DB.engineType.getNode();
	public static final Node_URI dbDriverVersion = (Node_URI)DB.driverVersion.getNode();
	public static final Node_URI dbFormatDate = (Node_URI)DB.formatDate.getNode();
	public static final Node_URI dbGraph = (Node_URI)DB.graph.getNode();
	public static final Node_URI dbMaxLiteral = (Node_URI)DB.maxLiteral.getNode(); 
	
	public static final String dbSystemGraphName = "SystemGraph";
	
	protected static SimpleDateFormat dateFormat = null;

	public DBPropDatabase( SpecializedGraph g, String engineType, String driverVersion, String maxLiteral) {
		super(g);
		
		if( dateFormat == null ) {
			// Use ISO 8601 Date format and write all dates as UTC time
			dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            dateFormat.setTimeZone( TimeZone.getTimeZone("GMT"));
		}
		
		String today = dateFormat.format( new Date());
		if( engineType != null ) putPropString(dbEngineType, engineType);
		if( driverVersion != null ) putPropString(dbDriverVersion, driverVersion);
		if( maxLiteral != null ) putPropString(dbMaxLiteral, maxLiteral);
		putPropString(dbFormatDate, today);
		
		// Need a set of default graph properties here.
	}
	
	public DBPropDatabase( SpecializedGraph g, Node n) {
		super(g,n);
	}	
	
	public DBPropDatabase( SpecializedGraph g) {
		super(g,findDBPropNode(g));
	}	
	
	public String getName() { return self.getURI(); }
	public String getEngineType() { return getPropString( dbEngineType); };
	public String getDriverVersion() { return getPropString( dbDriverVersion);};
	public String getFormatDate() { return getPropString( dbFormatDate); };
	
	public void addGraph( DBPropGraph g ) {
		putPropNode( dbGraph, g.getNode() );
	}

	public void removeGraph( DBPropGraph g ) {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Iterator matches = graph.find( self, dbGraph, g.getNode(), complete);
		if( matches.hasNext() ) {
			graph.delete( (Triple)(matches.next()), complete );
			g.remove();
		}
	}
	
	public ExtendedIterator getAllGraphs() {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Iterator matches = graph.find( self, dbGraph, null, complete );
		return new Map1Iterator(new MapToLSet(), matches);
	}
	
	public ExtendedIterator getAllGraphNames() {
		return new Map1Iterator(new MapGraphToName(), getAllGraphs());
	}

	private class MapToLSet implements Map1 {
		public Object map1( Object o) {
			Triple t = (Triple) o;
			return new DBPropGraph( graph, t.getObject() );			
		}
	}
	private class MapGraphToName implements Map1 {
		public Object map1( Object o) {
			DBPropGraph graph = (DBPropGraph) o;
			return graph.getName();			
		}
	}
	static Node findDBPropNode( SpecializedGraph g) {
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		Iterator matches = g.find( null, dbEngineType, null, complete );
		if( matches.hasNext())
			return ((Triple)matches.next()).getSubject();
		return null;		
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