/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
*/

package com.hp.hpl.jena.db.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.db.impl.SpecializedGraphReifier_RDB.StmtMask;

import com.hp.hpl.jena.vocabulary.RDF;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//=======================================================================
/**
* Handles Physical storage for implementing SpecializedGraphs.
* Different PSet classes are needed for different databases and different
* layout schemes.
* <p>
* This class is a base implemention from which database-specific
* drivers can inherit. It is not generic in the sense that it will work
* on any minimal SQL store and so should be treated as if it were
* an abstract class.
* <p>The SQL statements which implement each of the functions are
* loaded in a separate file etc/[layout]_[database].sql from the classpath.
* See {@link SQLCache SQLCache documentation} for more information on the
* format of this file.
* 
* Based on Driver* classes by Dave Reynolds.
*
* @author <a href="mailto:harumi.kuno@hp.com">Harumi Kuno</a>
* @version $Revision: 1.21 $ on $Date: 2004-07-24 20:07:37 $
*/

public class PSet_ReifStore_RDB extends PSet_TripleStore_RDB {

	//=======================================================================
	// Internal variables
    
    protected static Log logger = LogFactory.getLog( PSet_ReifStore_RDB.class );
    
	//=======================================================================
	// Constructors and accessors

	/**
	 * Constructor.
	 */
	public PSet_ReifStore_RDB() {
	}

	//=======================================================================
	
	
	// Database operations

	public void storeReifStmt(Node n, Triple t, IDBID my_GID) {
		storeTripleAR(t, my_GID, n, true, false, null);
	}

	public void deleteReifStmt(Node n, Triple t, IDBID my_GID) {
		deleteTripleAR(t, my_GID, n, false, null);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.IPSet#find(com.hp.hpl.jena.graph.TripleMatch, com.hp.hpl.jena.db.impl.IDBID)
	 */
	public ResultSetReifIterator findReifStmt(
		Node stmtURI,
		boolean hasType,
		IDBID graphID, boolean getTriples) {
		String astName = getTblName();
		String gid = graphID.getID().toString();
		ResultSetReifIterator result = new ResultSetReifIterator(this, getTriples, graphID);

		PreparedStatement ps = null;

		boolean objIsBlankOrURI = false;
		int args = 1;
		String stmtStr;
		boolean findAll = (stmtURI == null) || stmtURI.equals(Node.ANY);
		boolean notFound = false;

		if ( findAll )
			stmtStr = hasType ? "selectReifiedT" :  "selectReified";
		else
			stmtStr = hasType ? "selectReifiedNT" : "selectReifiedN";
		try {
			ps = m_sql.getPreparedSQLStatement(stmtStr, getTblName());

			if (!findAll) {
				String stmt_uri = m_driver.nodeToRDBString(stmtURI, false);
				if ( stmt_uri == null ) notFound = true;
				else ps.setString(args++, stmt_uri);
			}
			if (hasType)
				ps.setString(args++, "T");

			ps.setString(args++, gid);

		} catch (Exception e) {
			notFound = true;
			logger.warn( "Getting prepared statement for " + stmtStr + " Caught exception ", e);
            throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
		}

		if ( notFound )
			result.close();
		else {
			try {
			m_sql.executeSQL(ps, stmtStr, result);
			} catch (Exception e) {
				logger.debug( "find encountered exception ", e);
                                throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
			}
		}
		return result;
	}
	
	public ResultSetReifIterator findReifTripleMatch(
		TripleMatch t,
		IDBID graphID) {
		String astName = getTblName();
		String gid = graphID.getID().toString();
		ResultSetReifIterator result = new ResultSetReifIterator(this, true, graphID);

		PreparedStatement ps = null;

		String stmtStr = "*findReif ";
		boolean gotStmt = false;
		boolean gotPred = false;
		boolean gotObj = false;
		boolean objIsStmt = false;
		char reifProp = ' ';
		boolean done = false;
		int argc = 1;
		
		Node stmtURI = t.getMatchSubject();
		Node obj = t.getMatchObject();
		Node pred = t.getMatchPredicate();
		
		if ( (stmtURI != null) && !stmtURI.equals(Node.ANY) ) {
			gotStmt = true;
			stmtStr += "N";
		}
		if ( (pred != null) && !pred.equals(Node.ANY) ) {
			gotPred = true;
			if ( pred.equals(RDF.Nodes.subject) ) reifProp = 'S';
			else if ( pred.equals(RDF.Nodes.predicate) ) reifProp = 'P';
			else if ( pred.equals(RDF.Nodes.object) ) reifProp = 'O';
			else if ( pred.equals(RDF.Nodes.type) ) reifProp = 'T';
			else done = true;
			stmtStr += ("P" + reifProp);
		}
		if ( (obj != null) && !obj.equals(Node.ANY) ) {
			gotObj = true;
			stmtStr += "O";
			if ( obj.equals(RDF.Nodes.Statement) ) {
				objIsStmt = true;
				stmtStr += "C"; 
			} else if ( reifProp == 'T' )	
				// reifier only stores patterns like (-, rdf:type, rdf:Statement)
				done = true;			
		}

		if ( done == false ) try {
			ps = m_sql.getPreparedSQLStatement(stmtStr, getTblName());
			ps.setString(argc++, gid);
			if ( gotStmt ) {
				String stmtNode = m_driver.nodeToRDBString(stmtURI, false);
				if ( stmtNode == null ) done = true;
				else ps.setString(argc++, stmtNode);
			}
			if ( gotObj ) {
				// no arguments in case match is <-,rdf:type,rdf:Statement>
				if ( !(gotPred && objIsStmt) ) {
					String objNode = m_driver.nodeToRDBString(obj, false);
					ps.setString(argc++,objNode);
					if ( gotPred == false ) {
						// if no predicate, object value could be in subj, pred or obj column
						ps.setString(argc++,objNode);
						ps.setString(argc++,objNode);
					}
				}
			}

		} catch (Exception e) {
			done = true;
			logger.warn( "Getting prepared statement for " + stmtStr + " Caught exception ", e);
                        throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
		}

		if ( done )
			result.close();
		else {
			try {
			m_sql.executeSQL(ps, stmtStr, result);
			} catch (Exception e) {
				logger.debug( "find encountered exception ", e);
                                throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
			}
		}
		return result;
	}


	
	/*
	 * (non-Javadoc)
	 * return all nodes which reify the triple as a statement. no
	 * need to do distinct here since we only return nodes for reified statments.
	 */
	
	public ExtendedIterator findReifStmtURIByTriple(Triple t, IDBID my_GID) {
		String stmtStr = null;
		int argc = 1;
		PreparedStatement ps = null;
		ResultSetIterator result = new ResultSetIterator();
		boolean notFound = false;

		stmtStr = "selectReifNode";
		stmtStr += (t == null) ? "T" : "SPOT";

		try {
			ps = m_sql.getPreparedSQLStatement(stmtStr, getTblName());
			ps.clearParameters();

			if (t != null) {
				String argStr;
				argStr = m_driver.nodeToRDBString(t.getSubject(),false);
				if ( argStr == null ) notFound = true;
				else ps.setString(argc++, argStr);
				argStr = m_driver.nodeToRDBString(t.getPredicate(),false);
				if ( argStr == null ) notFound = true;
				else ps.setString(argc++, argStr);
				argStr = m_driver.nodeToRDBString(t.getObject(),false);
				if ( argStr == null ) notFound = true;
				else ps.setString(argc++, argStr);
			}

				ps.setString(argc, my_GID.getID().toString());
		} catch (Exception e) {
			notFound = true;
			logger.warn( "Getting prepared statement for " + stmtStr + " Caught exception ",  e);
                        throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
		}

		// find on object field
		if ( notFound )
			result.close();
		else {
		try {
			m_sql.executeSQL(ps, stmtStr, result);
		} catch (Exception e) {
			logger.debug("find encountered exception ", e);
            throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
		}
		}
		return result.mapWith(new MapResultSetToNode());
	}
	
	private class MapResultSetToNode implements Map1 {

		/* (non-Javadoc)
		 * @see com.hp.hpl.jena.util.iterator.Map1#map1(java.lang.Object)
		 */
		public Object map1(Object o) {
			// TODO Auto-generated method stub
			List l = (List) o;
            // TODO we have a BUG here somewhere, hence the message.
            if (l.get(0) instanceof String) {} else 
                throw new JenaException( "String required: " + l.get(0).getClass() + " " + l.get(0) );
			Node n = m_driver.RDBStringToNode((String) l.get(0));
			return n;
		}
		
	}

	/* (non-Javadoc)
		*  return (distinct) nodes which reify something (have any fragment)
		*/

	public ExtendedIterator findReifNodes(Node stmtURI, IDBID graphID) {
		String astName = getTblName();
		String gid = graphID.getID().toString();
		ResultSetIterator result = new ResultSetIterator();
		int argc = 1;
		PreparedStatement ps = null;
		boolean notFound = false;

		String stmtStr =
			stmtURI == null ? "selectReifNode" : "selectReifNodeN";
		try {
			ps = m_sql.getPreparedSQLStatement(stmtStr, getTblName());

			if (stmtURI != null) {
				String stmt_uri = m_driver.nodeToRDBString(stmtURI,false);
				if ( stmtURI == null ) notFound = true;
				else ps.setString(argc++, stmt_uri);
			}

			ps.setString(argc, gid);

		} catch (Exception e) {
			notFound = true;
			logger.warn( "Getting prepared statement for " + stmtStr + " Caught exception ", e);
            throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
		}

		if ( notFound )
			result.close();
		else try {
			result = m_sql.executeSQL(ps, stmtStr, result);
		} catch (Exception e) {
			logger.debug("find encountered exception ", e);
            throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
		}
		return result;
	}

	public void storeFrag(
		Node stmtURI,
		Triple frag,
		StmtMask fragMask,
		IDBID my_GID) {
		Node subj = fragMask.hasSubj() ? frag.getObject() : Node.NULL;
		Node prop = fragMask.hasPred() ? frag.getObject() : Node.NULL;
		Node obj = fragMask.hasObj() ? frag.getObject() : Node.NULL;
		Triple t = Triple.create(subj, prop, obj);
		storeTripleAR(t, my_GID, stmtURI, fragMask.hasType(), false, null);
	}

	public void updateOneFrag(
		Node stmtURI,
		Triple frag,
		StmtMask fragMask,
		boolean nullify,
		IDBID my_GID) {
			
			String stmtStr = null;
			Node val = null;
			int argc = 1;
			String argStr;
			
			if ( !fragMask.hasOneBit() )
				throw new JenaException("Reification can only update one column");
			PreparedStatement ps = null;

			if ( fragMask.hasSubj() ) {
				stmtStr = "updateReifiedS";
				if ( !nullify ) val = frag.getObject();
			} else if ( fragMask.hasPred() ) {
				stmtStr = "updateReifiedP";
				if ( !nullify ) val = frag.getObject();
			} else if ( fragMask.hasObj() ) {
				stmtStr = "updateReifiedO";
				if ( !nullify ) val = frag.getObject();
			} else if ( fragMask.hasType() ) {
				stmtStr = "updateReifiedT";
			} 
				
			try {
				ps = m_sql.getPreparedSQLStatement(stmtStr, getTblName());
				ps.clearParameters();
				if ( fragMask.hasSubj() || fragMask.hasPred() || fragMask.hasObj() ) {
					if (nullify)
						ps.setNull(argc++,java.sql.Types.VARCHAR);
					else {
						argStr = m_driver.nodeToRDBString(val,true);
						if ( argStr == null )
							throw new RDFRDBException("Invalid update argument: " + val.toString());
						ps.setString(argc++,argStr);
					}
				} else {
					// update hasType field
					if ( nullify )
						ps.setString(argc++," ");  // not nullable
					else
						ps.setString(argc++,"T");
				}
				argStr = m_driver.nodeToRDBString(stmtURI,true);
				if ( argStr == null )
					throw new RDFRDBException("Invalid update statement URI: " + stmtURI.toString());
				ps.setString(argc++,argStr);

				ps.setString(argc++,my_GID.getID().toString());
			} catch (Exception e) {
				logger.warn( "Getting prepared statement for "	+ stmtStr + " Caught exception ", e);
                throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
			}
			try {
	  			ps.executeUpdate();
 			 } catch (SQLException e1) {
				 logger.error("SQLException caught during reification update" + e1.getErrorCode(), e1);
                throw new JenaException("Exception during database access", e1);    // Rethrow in case there is a recovery option
}
		}

	public void nullifyFrag(Node stmtURI, StmtMask fragMask, IDBID my_GID) {
		updateOneFrag(stmtURI,null,fragMask,true,my_GID);
	}
	
	public void updateFrag(
		Node stmtURI,
		Triple frag,
		StmtMask fragMask,
		IDBID my_GID) {		
			updateOneFrag(stmtURI,frag,fragMask,false,my_GID);
		}

	public ResultSetReifIterator findFrag(
		Node stmtURI,
		Triple frag,
		StmtMask fragMask,
		IDBID my_GID) {
			
			String stmtStr = null;
			Node val = null;
			int argc = 1;
			ResultSetReifIterator result =
				new ResultSetReifIterator(this, true, my_GID);
			boolean notFound = false;
			String argStr;
			
			Node_Literal litNode = null;
			LiteralLabel ll = null;
			String lval = null;
			boolean litIsPlain = false;
			boolean objIsURI = false;

		
			if ( !fragMask.hasOneBit() )
				throw new JenaException("Reification can only find one column");
			PreparedStatement ps = null;

			val = frag.getObject();
			if ( fragMask.hasSubj() ) {
				stmtStr = "selectReifiedNS";
			} else if ( fragMask.hasPred() ) {
				stmtStr = "selectReifiedNP";
			} else if ( fragMask.hasObj() ) {
				stmtStr = "selectReifiedNO";
			} else if ( fragMask.hasType() ) {
				stmtStr = "selectReifiedNT";
			}
				
			try {
				ps = m_sql.getPreparedSQLStatement(stmtStr, getTblName());
				ps.clearParameters();
				argStr = m_driver.nodeToRDBString(stmtURI,false);
				if ( argStr == null ) notFound = true;
				else ps.setString(argc++,argStr);
				if ( fragMask.hasSubj() || fragMask.hasPred() || fragMask.hasObj()) {
					argStr = m_driver.nodeToRDBString(val,false);
					if ( argStr == null ) notFound = true;
					else ps.setString(argc++,argStr);
				} else {
					// find on hasType field
					ps.setString(argc++,"T");
				}
				ps.setString(argc,my_GID.getID().toString());
				
			} catch (Exception e) {
				logger.warn( "Getting prepared statement for " + stmtStr + " Caught exception ", e);
			}

			if ( notFound )
				result.close();
			else
			try {
				m_sql.executeSQL(ps, stmtStr, result);
			} catch (Exception e) {
				logger.debug("find encountered exception ", e);
			}
		return result;
	}
	
	public void deleteFrag(
		Triple frag,
		StmtMask fragMask,
		IDBID my_GID) {
			
			if ( !fragMask.hasOneBit() )
				throw new JenaException("Can only delete one fragment");
			int argc = 1;
		
			PreparedStatement ps = null;
			String stmtStr = "deleteReified";
			if ( fragMask.hasSubj() )
				stmtStr += "S";
			else if ( fragMask.hasPred() )
				stmtStr += "P";
			else if ( fragMask.hasObj() )
				stmtStr += "O";
			else if ( fragMask.hasType() )
				stmtStr += "T";
			else
				throw new JenaException("Unspecified reification fragment in deleteFrag");

			Node val = frag.getObject();
			String argStr = m_driver.nodeToRDBString(val,false);
			Node stmtURI = frag.getSubject();
			String uriStr = m_driver.nodeToRDBString(stmtURI,false);
							
			try {
				ps = m_sql.getPreparedSQLStatement(stmtStr, getTblName());
				ps.clearParameters();
				
				if ( fragMask.hasSubj() )
					ps.setString(argc++,argStr);
				else if ( fragMask.hasPred() )
					ps.setString(argc++,argStr);
				else if ( fragMask.hasObj() )
					ps.setString(argc++,argStr);
				ps.setString(argc++,my_GID.getID().toString());
				ps.setString(argc,uriStr);
				
			} catch (Exception e) {
				logger.warn( "Getting prepared statement for " + stmtStr + " Caught exception ", e);
			}
			try {
				ps.executeUpdate();
			} catch (Exception e) {
				logger.debug("deleteFrag encountered exception ", e);
			}
		return;
	}
}
	/*
	 *  (c) Copyright 2000, 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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
 
