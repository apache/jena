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
 * @version $Revision: 1.1 $
 */
public class DBPropDatabase extends DBProp {

	/**
	 * @since Jena 2.0
	 */

	public static final Node_URI dbEngineType = (Node_URI)DB.engineType.asNode();
	public static final Node_URI dbLayoutVersion = (Node_URI)DB.layoutVersion.asNode();
	public static final Node_URI dbDriverVersion = (Node_URI)DB.driverVersion.asNode();
	public static final Node_URI dbFormatDate = (Node_URI)DB.formatDate.asNode();
	public static final Node_URI dbGraph = (Node_URI)DB.graph.asNode();
	public static final Node_URI dbLongObjectLength = (Node_URI)DB.longObjectLength.asNode();
	public static final Node_URI dbIndexKeyLength = (Node_URI)DB.indexKeyLength.asNode();
	public static final Node_URI dbIsTransactionDb = (Node_URI)DB.isTransactionDb.asNode();
	public static final Node_URI dbDoCompressURI = (Node_URI)DB.doCompressURI.asNode();
	public static final Node_URI dbCompressURILength = (Node_URI)DB.compressURILength.asNode();
	public static final Node_URI dbTableNamePrefix = (Node_URI)DB.tableNamePrefix.asNode();
	
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
		ClosableIterator<Triple> matches = graph.find( self, dbGraph, g.getNode(), complete);
		if( matches.hasNext() ) {
			graph.delete(matches.next(), complete );
			g.remove();
            matches.close();
		}
	}
	
	public ExtendedIterator<DBPropGraph> getAllGraphs() {
		return 
            graph.find( self, dbGraph, null, newComplete() ) 
            .mapWith( new MapToLSet() );
	}
	
	public ExtendedIterator<String> getAllGraphNames() {
        return getAllGraphs() .mapWith( graphToName ); 
	}

    static final Map1<DBPropGraph,String> graphToName = new Map1<DBPropGraph,String>() 
        { @Override
        public String map1( DBPropGraph o)  { return o.getName(); } };

	private class MapToLSet implements Map1<Triple,DBPropGraph> {
		@Override
        public DBPropGraph map1( Triple t) {
			return new DBPropGraph( graph, t.getObject() );			
		}
	}
    
	static Node findDBPropNode( SpecializedGraph g) {
		Node res = null;
		ClosableIterator<Triple> matches = g.find( null, dbEngineType, null, newComplete() );
		if (matches.hasNext()) {
			try {
				res = matches.next().getSubject();
				if (matches.hasNext())
					res = null;
				return res;
			} finally {
				matches.close();
			}
		}
		return null;		
	}
	
	protected String findDBPropString( Node_URI predicate) {
		// similar to getPropString but doesn't match on the subject field.
		// there should only be one instance of the db properties in the database.
		// if zero or multiple instances of the property, return null.
		ClosableIterator<Triple> it = graph.find(null, predicate, null, newComplete());
		if (it.hasNext()) {
			try {
				String res = null;
				Node obj = it.next().getObject();
				if (!it.hasNext())
					res = obj.getLiteralLexicalForm();
				return res;
			} finally {
				it.close();
			}
		}
		return null;
	}
	
	public String getInitLongObjectLength() { return findDBPropString( dbLongObjectLength); }
	public String getInitIndexKeyLength() { return findDBPropString( dbIndexKeyLength); }
	public String getInitDoCompressURI() { return findDBPropString( dbDoCompressURI); }
	public String getInitCompressURILength() { return findDBPropString( dbCompressURILength); }
	

}
