/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
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
 * @version $Revision: 1.10 $
 */
public class DBPropDatabase extends DBProp {

	/**
	 * @since Jena 2.0
	 */

	public static final Node_URI dbEngineType = (Node_URI)DB.engineType.getNode();
	public static final Node_URI dbLayoutVersion = (Node_URI)DB.layoutVersion.getNode();
	public static final Node_URI dbDriverVersion = (Node_URI)DB.driverVersion.getNode();
	public static final Node_URI dbFormatDate = (Node_URI)DB.formatDate.getNode();
	public static final Node_URI dbGraph = (Node_URI)DB.graph.getNode();
	public static final Node_URI dbLongObjectLength = (Node_URI)DB.longObjectLength.getNode();
	public static final Node_URI dbIndexKeyLength = (Node_URI)DB.indexKeyLength.getNode();
	public static final Node_URI dbIsTransactionDb = (Node_URI)DB.isTransactionDb.getNode();
	public static final Node_URI dbDoCompressURI = (Node_URI)DB.doCompressURI.getNode();
	public static final Node_URI dbCompressURILength = (Node_URI)DB.compressURILength.getNode();
	public static final Node_URI dbTableNamePrefix = (Node_URI)DB.tableNamePrefix.getNode();
	
	public static final String dbSystemGraphName = "SystemGraph";
	
	protected static SimpleDateFormat dateFormat = null;

	public DBPropDatabase ( SpecializedGraph g, String engineType, String driverVersion,
		String layoutVersion, String longObjectLength, String indexKeyLength,
		String isTransactionDb, String doCompressURI, String compressURILength,
		String tableNamePrefix ) {
		super(g);
		
		if( dateFormat == null ) {
			// Use ISO 8601 Date format and write all dates as UTC time
			dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            dateFormat.setTimeZone( TimeZone.getTimeZone("GMT"));
		}
		
		String today = dateFormat.format( new Date());
		if( engineType != null ) putPropString(dbEngineType, engineType);
		if( driverVersion != null ) putPropString(dbDriverVersion, driverVersion);
		putPropString(dbLayoutVersion, layoutVersion);
		putPropString(dbFormatDate, today);
		putPropString(dbLongObjectLength, longObjectLength);
		putPropString(dbIndexKeyLength, indexKeyLength);
		putPropString(dbIsTransactionDb, isTransactionDb);
		putPropString(dbDoCompressURI, doCompressURI);
		putPropString(dbCompressURILength, compressURILength);
		putPropString(dbTableNamePrefix, tableNamePrefix);
	}
	
	public DBPropDatabase( SpecializedGraph g, Node n) {
		super(g,n);
	}	
	
	public DBPropDatabase( SpecializedGraph g) {
		super(g,findDBPropNode(g));
	}	
	
	public String getName() { return self.getURI(); }
	public String getEngineType() { return getPropString( dbEngineType); }
	public String getDriverVersion() { return getPropString( dbDriverVersion);}
	public String getFormatDate() { return getPropString( dbFormatDate); }
	public String getLayoutVersion() { return getPropString( dbLayoutVersion); }
	public String getLongObjectLength() { return getPropString( dbLongObjectLength); }
	public String getIndexKeyLength() { return getPropString( dbIndexKeyLength); }
	public String getIsTransactionDb() { return getPropString( dbIsTransactionDb); }
	public String getDoCompressURI() { return getPropString( dbDoCompressURI); }
	public String getCompressURILength() { return getPropString( dbCompressURILength); }
	public String getTableNamePrefix() { return getPropString( dbTableNamePrefix); }
	
	public void addGraph( DBPropGraph g ) {
		putPropNode( dbGraph, g.getNode() );
	}

	public void removeGraph( DBPropGraph g ) {
		SpecializedGraph.CompletionFlag complete = newComplete();
		Iterator matches = graph.find( self, dbGraph, g.getNode(), complete);
		if( matches.hasNext() ) {
			graph.delete( (Triple)(matches.next()), complete );
			g.remove();
		}
	}
	
	public ExtendedIterator getAllGraphs() {
		return 
            graph.find( self, dbGraph, null, newComplete() ) 
            .mapWith( new MapToLSet() );
	}
	
	public ExtendedIterator getAllGraphNames() {
        return getAllGraphs() .mapWith( graphToName ); 
	}

    static final Map1 graphToName = new Map1() 
        { public Object map1( Object o)  { return ((DBPropGraph) o).getName(); } };

	private class MapToLSet implements Map1 {
		public Object map1( Object o) {
			Triple t = (Triple) o;
			return new DBPropGraph( graph, t.getObject() );			
		}
	}
    
	static Node findDBPropNode( SpecializedGraph g) {
		Iterator matches = g.find( null, dbEngineType, null, newComplete() );
		if( matches.hasNext()) return ((Triple) matches.next()).getSubject();
		return null;		
	}
}

/*
 *  (c) Copyright 2003 Hewlett-Packard Development Company, LP
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