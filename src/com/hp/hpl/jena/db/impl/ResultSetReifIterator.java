/*
 *  (c) Copyright Hewlett-Packard Company 2003
 *  All rights reserved.
 *
 *
 */

//=======================================================================
// Package
package com.hp.hpl.jena.db.impl;

//=======================================================================
// Imports
import java.sql.*;

import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.JenaException;

import org.apache.log4j.Logger;

//=======================================================================
/**
* Version of ResultSetIterator that extracts database rows as Triples from a reified statement table.
*
* @author hkuno.  Based on ResultSetResource Iterator, by Dave Reynolds, HPLabs, Bristol <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
* @version $Revision: 1.1 $ on $Date: 2003-07-10 18:41:10 $
*/
public class ResultSetReifIterator extends ResultSetIterator {

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
	
	/** number of triples to generate for this row (ranges 1-4) */
	protected int m_tripleCount;
	
	/** number of triples already generated for this row. (ranges 0-4) */
	protected int m_tripleNum;


    static protected Logger logger = Logger.getLogger( ResultSetReifIterator.class );
    
	// Constructor
	public ResultSetReifIterator(IPSet p, boolean getTriples, IDBID graphID) {
		m_pset = p;
		setGraphID(graphID);
		m_getTriples = getTriples;
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
    protected void extractRow() throws SQLException {
        int rx = 1;
        ResultSet rs = m_resultSet;
        String subj = rs.getString(1);
		String pred = rs.getString(2);
		String obj = rs.getString(3);
		
		m_stmtURI = m_pset.driver().RDBStringToNode(rs.getString(4));
		m_hasType = rs.getString(5).equals("T");
		
		m_tripleCount = m_hasType ? 1 : 0;
		if ( subj == null ) {
			m_subjNode = Node.ANY;
		} else {
			m_subjNode = m_pset.driver().RDBStringToNode(subj);
			m_tripleCount++;
		}
		if ( pred == null ) {
			m_predNode = Node.ANY;
		} else {
			m_predNode = m_pset.driver().RDBStringToNode(pred);
			m_tripleCount++;
		}
		if ( obj == null ) {
			m_objNode = Node.ANY;
		} else {
			m_objNode = m_pset.driver().RDBStringToNode(obj);
			m_tripleCount++;
		}		
		m_tripleNum = 0;
	}
	
		/**
		 * Return triples for the current row, which should have already been extracted.
		 */
		protected Object getRow() {
			Triple t = null;
			
			if ( m_getTriples == true ) {
				if ( m_tripleNum == 0) {
					if ( !m_subjNode.equals(Node.ANY) ) {
						t = new Triple(m_stmtURI,Reifier.subject,m_subjNode);
						m_tripleCount--;
					} else
						m_tripleNum++;
				}
				if ( m_tripleNum == 1) {
					if ( !m_predNode.equals(Node.ANY) ) {
						t = new Triple(m_stmtURI,Reifier.predicate,m_predNode);
						m_tripleCount--;
					} else
						m_tripleNum++;
				}
				if ( m_tripleNum == 2) {
					if ( !m_objNode.equals(Node.ANY) ) {
						t = new Triple(m_stmtURI,Reifier.object,m_objNode);
						m_tripleCount--;
					} else
						m_tripleNum++;
				}
				if ( m_tripleNum >= 3) {
					if ( m_hasType ) {
						t = new Triple(m_stmtURI,Reifier.type,Reifier.Statement);
						m_tripleCount--;
					} else
						throw new JenaException("Reified triple not found");
				}
				m_tripleNum++;
				if ( m_tripleCount > 0 )
					m_prefetched = true;

			} else {
				t = new Triple(m_subjNode, m_predNode, m_objNode);
			}
		
			return t;
		}
		
		/**
	 	* Return the current row, which should have already been extracted.
	 	*/
		protected Node getStmtURI() {
			return m_stmtURI;
		}
		
		/**
		* Return the current row, which should have already been extracted.
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
		

} // End class

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

