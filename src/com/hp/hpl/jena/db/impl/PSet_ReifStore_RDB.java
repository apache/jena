/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
*/

package com.hp.hpl.jena.db.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.util.Log;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.db.impl.SpecializedGraphReifier_RDB.StmtMask;

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
* @version $Revision: 1.6 $ on $Date: 2003-05-30 16:23:50 $
*/

public class PSet_ReifStore_RDB extends PSet_TripleStore_RDB {

	//=======================================================================
	// Cutomization variables

	public static String SYS_AS_TNAME = "JENA_StmtReified";

	//=======================================================================
	// Internal variables

	//=======================================================================
	// Constructors and accessors

	/**
	 * Constructor.
	 */
	public PSet_ReifStore_RDB() {
	}

	//=======================================================================
	
	/**
	 * Create a table for storing reified statements.
	 * 
	 * @param astName name of table.
	 */
	public void createASTable(String astName) {
    	
		try {m_sql.runSQLGroup("createReifStatementTable", getASTname());
		} catch (SQLException e) {
			com.hp.hpl.jena.util.Log.warning("Problem formatting database", e);
			throw new RDFRDBException("Failed to format database", e);
		}
		m_tablenames.add(astName);
	}


	
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
	public ResultSetIterator findReifStmt(
		Node stmtURI,
		boolean hasType,
		IDBID graphID, boolean useRSI) {
		String astName = getASTname();
		String gid = graphID.getID().toString();
		ResultSetIterator result;
		if ( useRSI ) 
		 result = new ResultSetIterator();
		else  result =
			new ResultSetTripleIterator(this, true, graphID);

		PreparedStatement ps = null;

		boolean objIsBlankOrURI = false;
		int args = 1;
		String stmtStr;
		boolean findAll = (stmtURI == null) || stmtURI.equals(Node.ANY);

		if ( findAll )
			stmtStr = hasType ? "SelectAllReifTypeStmt" :  "SelectAllReifStatement";
		else
			stmtStr = hasType ? "SelectReifTypeStatement" : "SelectReifStatement";
		try {
			ps = m_sql.getPreparedSQLStatement(stmtStr, getASTname());

			if (!findAll) {
				String stmt_uri = nodeToRDBString(stmtURI);
				ps.setString(args++, stmt_uri);
			}
			if (hasType)
				ps.setInt(args++, 1);

			ps.setString(args++, gid);

		} catch (Exception e) {
			Log.warning(
				"Getting prepared statement for "
					+ stmtStr
					+ " Caught exception "
					+ e);
		}

		try {
			m_sql.executeSQL(ps, stmtStr, result);
		} catch (Exception e) {
			Log.debug("find encountered exception " + e);
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

		Node_Literal litNode = null;
		LiteralLabel ll = null;
		String lval = null;
		boolean litIsPlain = false;
		Node obj = null;

		stmtStr = "SelectReifURI";

		if (t != null) {

			obj = t.getObject();
			// find on object field
			if (obj.isURI() || obj.isBlank()) {
				stmtStr += "ByOU";
			} else {
				litNode = (Node_Literal) obj;
				ll = litNode.getLiteral();
				lval = (String) ll.getValue();
				litIsPlain = literalIsPlain(ll);

				if (litIsPlain) {
					// object literal can fit in statement table
					stmtStr += "ByOV";
				} else {
					// belongs in literal table
					stmtStr += "ByOR";
				}
			}
		}

		try {
			ps = m_sql.getPreparedSQLStatement(stmtStr, getASTname());
			ps.clearParameters();

			if (t != null) {

				ps.setString(argc++, nodeToRDBString(t.getSubject()));
				ps.setString(argc++, nodeToRDBString(t.getPredicate()));

				// find on object field
				if (obj.isURI() || obj.isBlank()) {
					ps.setString(argc++, nodeToRDBString(obj));
				} else {
					if (litIsPlain) {
						// object literal can fit in statement table
						ps.setString(argc++, lval);
					} else {
						// belongs in literal table
						String litIdx = getLiteralIdx(lval);
						// TODO This happens in several places?
						IDBID lid = getLiteralID(litNode);
						if (lid == null) {
							lid = addLiteral(litNode);
						}
						ps.setString(argc++, litIdx);
						ps.setString(argc++, lid.getID().toString());
					}
				}
			}
			ps.setString(argc, my_GID.getID().toString());
		} catch (Exception e) {
			Log.warning(
				"Getting prepared statement for "
					+ stmtStr
					+ " Caught exception "
					+ e);
		}

		// find on object field
		try {
			m_sql.executeSQL(ps, stmtStr, result);
		} catch (Exception e) {
			Log.debug("find encountered exception " + e);
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
			Node n = RDBStringToNode((String) l.get(0));
			return n;
		}
		
	}

	/* (non-Javadoc)
		*  return (distinct) nodes which reify something (have any fragment)
		*/

	public ExtendedIterator findReifNodes(Node stmtURI, IDBID graphID) {
		String astName = getASTname();
		String gid = graphID.getID().toString();
		ResultSetIterator result = new ResultSetIterator();
		int argc = 1;
		PreparedStatement ps = null;

		String stmtStr =
			stmtURI == null ? "SelectAllReifNodes" : "SelectReifNode";
		try {
			ps = m_sql.getPreparedSQLStatement(stmtStr, getASTname());

			if (stmtURI != null) {
				String stmt_uri = nodeToRDBString(stmtURI);
				ps.setString(argc++, stmt_uri);
			}

			ps.setString(argc, gid);

		} catch (Exception e) {
			Log.warning(
				"Getting prepared statement for "
					+ stmtStr
					+ " Caught exception "
					+ e);
		}

		try {
			result = m_sql.executeSQL(ps, stmtStr, result);
		} catch (Exception e) {
			Log.debug("find encountered exception " + e);
		}
		return result;
	}

	public void storeFrag(
		Node stmtURI,
		Triple frag,
		StmtMask fragMask,
		IDBID my_GID) {
		Node subj = fragMask.hasSubj() ? frag.getObject() : Node.ANY;
		Node prop = fragMask.hasPred() ? frag.getObject() : Node.ANY;
		Node obj = fragMask.hasObj() ? frag.getObject() : Node.ANY;
		Triple t = new Triple(subj, prop, obj);
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
			
			if ( !fragMask.hasOneBit() )
				throw new RuntimeException("Reification can only update one column");
			PreparedStatement ps = null;

			if ( fragMask.hasSubj() ) {
				stmtStr = "updateReifSubj";
				if ( !nullify ) val = frag.getObject();
			} else if ( fragMask.hasPred() ) {
				stmtStr = "updateReifProp";
				if ( !nullify ) val = frag.getObject();
			} else if ( fragMask.hasObj() ) {
				stmtStr = "updateReifObj";
				if ( !nullify ) val = frag.getObject();
			} else if ( fragMask.hasType() ) {
				stmtStr = "updateReifHasType";
			} 
				
			try {
				ps = m_sql.getPreparedSQLStatement(stmtStr, getASTname());
				ps.clearParameters();
				if ( fragMask.hasSubj() || fragMask.hasPred() ) {
					if (nullify)
						ps.setNull(argc++,java.sql.Types.VARCHAR);
					else
						ps.setString(argc++,nodeToRDBString(val));
				} else if ( fragMask.hasObj() ){
					// update object field
					if (nullify) {
						ps.setNull(argc++,java.sql.Types.VARCHAR);
						ps.setNull(argc++,java.sql.Types.BINARY);
						ps.setNull(argc++,java.sql.Types.INTEGER);
					} else {
						if ( val.isURI() || val.isBlank() ) {
						  ps.setString(argc++,nodeToRDBString(val));
						  ps.setNull(argc++,java.sql.Types.BINARY);
						  ps.setNull(argc++,java.sql.Types.INTEGER);
						} else {
							Node_Literal litNode = (Node_Literal)val;
							LiteralLabel ll = litNode.getLiteral();
							String lval = (String)ll.getValue();
							boolean litIsPlain = literalIsPlain(ll);
		  
							if (litIsPlain) {
							  // object literal can fit in statement table
							  ps.setNull(argc++,java.sql.Types.VARCHAR);
							  ps.setString(argc++, lval);
							  ps.setNull(argc++,java.sql.Types.INTEGER);
							} else {
								// belongs in literal table
							   String litIdx = getLiteralIdx(lval); // TODO This happens in several places?
							   IDBID lid = getLiteralID(litNode);
							   if (lid == null) {
								 lid = addLiteral(litNode);
							   }
							   ps.setNull(argc++,java.sql.Types.VARCHAR);
							   ps.setString(argc++,litIdx);
							   ps.setString(argc++,lid.getID().toString());
							}
						}

					}
				} else {
					// update hasType field
					if ( nullify )
						ps.setNull(argc++,java.sql.Types.INTEGER);
					else
						ps.setInt(argc++,1);
				}
				ps.setString(argc++,nodeToRDBString(stmtURI));
				ps.setString(argc++,my_GID.getID().toString());
			} catch (Exception e) {
				Log.warning(
					"Getting prepared statement for "
						+ stmtStr
						+ " Caught exception "
						+ e);
			}
			try {
	  			ps.executeUpdate();
 			 } catch (SQLException e1) {
				 Log.severe("SQLException caught during reification update" + e1.getErrorCode() + ": " + e1);
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

	public ResultSetTripleIterator findFrag(
		Node stmtURI,
		Triple frag,
		StmtMask fragMask,
		IDBID my_GID) {
			
			String stmtStr = null;;
			Node val = null;
			int argc = 1;
			ResultSetTripleIterator result =
				new ResultSetTripleIterator(this, true, my_GID);
			Node_Literal litNode = null;
			LiteralLabel ll = null;
			String lval = null;
			boolean litIsPlain = false;
			boolean objIsURI = false;

		
			if ( !fragMask.hasOneBit() )
				throw new RuntimeException("Reification can only find one column");
			PreparedStatement ps = null;

			val = frag.getObject();
			if ( fragMask.hasSubj() ) {
				stmtStr = "findFragSubj";
			} else if ( fragMask.hasPred() ) {
				stmtStr = "findFragProp";
			} else if ( fragMask.hasObj() ) {
				stmtStr = "findFragObj";
				objIsURI = val.isURI() || val.isBlank();
				if ( objIsURI ) {
					stmtStr += "OU";
				} else {
					litNode = (Node_Literal) val;
					ll = litNode.getLiteral();
					lval = (String) ll.getValue();
					litIsPlain = literalIsPlain(ll);
					stmtStr += litIsPlain ? "OV" : "OR";
				}	
			} else if ( fragMask.hasType() ) {
				stmtStr = "findFragHasType";
			}
				
			try {
				ps = m_sql.getPreparedSQLStatement(stmtStr, getASTname());
				ps.clearParameters();
				ps.setString(argc++,nodeToRDBString(stmtURI));
				if ( fragMask.hasSubj() || fragMask.hasPred() ) {
					ps.setString(argc++,nodeToRDBString(val));
				} else if ( fragMask.hasObj() ){
					// find on object field
					if ( objIsURI ) {
						ps.setString(argc++,nodeToRDBString(val));
					} else {		  
						if (litIsPlain) {
							 // object literal can fit in statement table
							ps.setString(argc++, lval);
						} else {
							// belongs in literal table
							String litIdx = getLiteralIdx(lval); // TODO This happens in several places?
							IDBID lid = getLiteralID(litNode);
							if (lid == null) {
								lid = addLiteral(litNode);
							 }
							 ps.setString(argc++,litIdx);
							 ps.setString(argc++,lid.getID().toString());
						}
					}
				} else {
					// find on hasType field
					ps.setInt(argc++,1);
				}
				ps.setString(argc,my_GID.getID().toString());
				
			} catch (Exception e) {
				Log.warning(
					"Getting prepared statement for "
						+ stmtStr
						+ " Caught exception "
						+ e);
			}

			try {
				m_sql.executeSQL(ps, stmtStr, result);
			} catch (Exception e) {
				Log.debug("find encountered exception " + e);
			}
		return result;
	}
}
	/*
	 *  (c) Copyright Hewlett-Packard Company 2000-2003
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
 
