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

//=======================================================================
// Package
package com.hp.hpl.jena.db.impl;

//=======================================================================
// Imports
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.RDF;

//=======================================================================
/**
* Version of ResultSetIterator that extracts database rows as Triples from a reified statement table.
*
* @author hkuno.  Based on ResultSetResource Iterator, by Dave Reynolds, HPLabs, Bristol <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
* @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:37 $
*/
public class ResultSetReifIterator extends ResultSetIterator<Triple> {

    /** The rdf model in which to instantiate any resources */
    protected IDBID m_graphID;

    /** The database driver, used to access namespace and resource caches */
    protected IPSet m_pset;
        
	/** Holds subject, predicate and object of current row */
	protected Node m_subjNode;
	protected Node m_predNode;
	protected Node m_objNode;
        
    /** Statement URI of current row */
    protected Node m_stmtURI;
    
    /** HasType flag is true if reified statement has type rdf:Statement */
    protected boolean m_hasType;
    
	/** getTriples is true if this iterator should return all triples for the reified statement
	 *  otherwise, reified statements are returned. */
	protected boolean m_getTriples;
	
	/** a triple match over a reified table might return one property.
	 *  m_propCol identifies the column number of the property to return.
	 *  it ranges 1-4.
	 */
	protected int m_propCol;
	
	/** a triple match over a reified table might specify an object value
	 *  to match but no property. so, return all columns (properties)
	 *  that match the specified object value.
	 */
	protected Node m_matchObj;
	
	
	/** total number of fragments to generate for this row (ranges 1-4) */
	protected int m_fragCount;
	
	/** number of remaining fragments to generate for this row (ranges 1-4) */
	protected int m_fragRem;
	
	/** number of next fragment to generate (0-3 for subj, pred, obj, type). */
	protected int m_nextFrag;


    static protected Logger logger = LoggerFactory.getLogger( ResultSetReifIterator.class );
    
	// Constructor
	public ResultSetReifIterator(IPSet p, boolean getTriples, IDBID graphID) {
		m_pset = p;
		setGraphID(graphID);
		m_getTriples = getTriples;
		m_matchObj = null;
		m_propCol = 0;
	}

	public ResultSetReifIterator(IPSet p, char getProp, IDBID graphID) {
		m_pset = p;
		setGraphID(graphID);
		m_getTriples = true;
		m_matchObj = null;
		if ( getProp == 'S' ) m_propCol = 1;
		else if ( getProp == 'P' ) m_propCol = 2;
		else if ( getProp == 'O' ) m_propCol = 3;
		else if ( getProp == 'T' ) m_propCol = 4;
	}

	public ResultSetReifIterator(IPSet p, Node getObj, IDBID graphID) {
		m_pset = p;
		setGraphID(graphID);
		m_getTriples = true;
		m_matchObj = getObj;
		m_propCol = 0;
	}

	/**
	 * Set m_graphID.
	 * @param gid is the id of the graph associated with this iterator.
	 */
	public void setGraphID(IDBID gid) {
		m_graphID = gid;
	}
	
	/**
	 * Reset an existing iterator to scan a new result set.
	 * @param resultSet the result set being iterated over
	 * @param sourceStatement The source Statement to be cleaned up when the iterator finishes - return it to cache or close it if no cache
	 * @param cache The originating SQLcache to return the statement to, can be null
	 * @param opname The name of the original operation that lead to this statement, can be null if SQLCache is null
	 */
	@Override
    public void reset(ResultSet resultSet, PreparedStatement sourceStatement, SQLCache cache, String opname) {
		super.reset(resultSet, sourceStatement, cache, opname);
	}

    /**
     * Extract the current row into a triple. 
     * Requires the row to be of the form:
     *   subject URI (String)
     *   predicate URI (String)
     *   object URI (String)
     *   object value (String)
     *   Object literal id (Object)
     * 
     * The object of the triple can be either a URI, a simple literal (in 
     * which case it will just have an object value, or a complex literal 
     * (in which case both the object value and the object literal id 
     * columns may be populated.
     */
    @Override
    protected void extractRow() throws SQLException {
        int rx = 1;
        ResultSet rs = m_resultSet;
        String subj = rs.getString(1);
		String pred = rs.getString(2);
		String obj = rs.getString(3);
		
		m_stmtURI = m_pset.driver().RDBStringToNode(rs.getString(4));
		m_hasType = rs.getString(5).equals("T");
		
		m_fragRem = 0;
		if ( m_hasType )
			if ( (m_matchObj==null) || m_matchObj.equals(RDF.Nodes.Statement) )
				m_fragRem++;
			
		if ( subj == null ) {
			m_subjNode = Node.NULL;
		} else {
			m_subjNode = m_pset.driver().RDBStringToNode(subj);
			if ( (m_matchObj==null) || m_matchObj.equals(m_subjNode) )
				m_fragRem++;
		}
		if ( pred == null ) {
			m_predNode = Node.NULL;
		} else {
			m_predNode = m_pset.driver().RDBStringToNode(pred);
			if ( (m_matchObj==null) || m_matchObj.equals(m_predNode) )
				m_fragRem++;
		}
		if ( obj == null ) {
			m_objNode = Node.NULL;
		} else {
			m_objNode = m_pset.driver().RDBStringToNode(obj);
			if ( (m_matchObj==null) || m_matchObj.equals(m_objNode) )
				m_fragRem++;
		}
		if ( m_propCol > 0 ) {
			m_nextFrag = m_propCol - 1;
			m_fragCount = m_fragRem = 1;
		} else {
			m_nextFrag = 0;
			m_fragCount = m_fragRem;
		}
	}
	
		/**
		 * Return triples for the current row, which should have already been extracted.
		 */
		@Override
        protected Triple getRow() {
			Triple t = null;
			
			if ( m_getTriples == true ) {
				if ( m_nextFrag == 0) {
					if ( !m_subjNode.equals(Node.NULL) &&
						((m_matchObj==null) || m_matchObj.equals(m_subjNode)) ) {
							t = Triple.create(m_stmtURI,RDF.Nodes.subject,m_subjNode);
							m_fragRem--;
					} else
						m_nextFrag++;
				}
				if ( m_nextFrag == 1) {
					if ( !m_predNode.equals(Node.NULL) &&
						((m_matchObj==null) || m_matchObj.equals(m_predNode)) ) {
							t = Triple.create(m_stmtURI,RDF.Nodes.predicate,m_predNode);
							m_fragRem--;
					} else
						m_nextFrag++;
				}
				if ( m_nextFrag == 2) {
					if ( !m_objNode.equals(Node.NULL) &&
						((m_matchObj==null) || m_matchObj.equals(m_objNode)) ) {
							t = Triple.create(m_stmtURI,RDF.Nodes.object,m_objNode);
							m_fragRem--;
					} else
						m_nextFrag++;
				}
				if ( m_nextFrag >= 3) {
					if ( m_hasType &&
						((m_matchObj==null) || m_matchObj.equals(RDF.Nodes.Statement)) ) {
							t = Triple.create(m_stmtURI,RDF.Nodes.type,RDF.Nodes.Statement);
							m_fragRem--;							
						} else
						throw new JenaException("Reified triple not found");
				}
				m_nextFrag++;
				if ( m_fragRem > 0 )
					m_prefetched = true;

			} else {
				t = Triple.create(m_subjNode, m_predNode, m_objNode);
			}
		
			return t;
		}
		
	/**
	* Return the true if the current row has a non-null subject.
	*/
	protected boolean hasSubj() {
		return m_subjNode != Node.NULL;
	}

	/**
	* Return the true if the current row has a non-null predicate.
	*/
	protected boolean hasPred() {
		return m_predNode != Node.NULL;
	}

	/**
	* Return the true if the current row has a non-null object.
	*/
	protected boolean hasObj() {
		return m_objNode != Node.NULL;
	}

	/**
	* Return the true if the current row has T (true) for hasType.
	*/
	protected boolean hasType() {
		return m_hasType;
	}

	/**
	* Return the number of (reification statement) fragments for the current row.
	*/
	protected int getFragCount() {
		return m_fragCount;
	}

		/**
	 	* Return the reifying URI for current row, which should have already been extracted.
	 	*/
		protected Node getStmtURI() {
			return m_stmtURI;
		}
		
		/**
		* Return the hasType value of current row, which should have already been extracted.
		*/
		protected boolean getHasType() {
			return m_hasType;
		}
		
		/**
	 	* Delete the current row, which should have already been extracted.
	 	* Should only be used (carefully and) internally by db layer.
	 	*/
		protected void deleteRow() {
			try {
				m_resultSet.deleteRow();
			} catch (SQLException e) {
				throw new RDFRDBException("Internal sql error", e);
			}
		}
		

}
