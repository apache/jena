/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: PSet_TripleStore_RDB.java,v 1.1 2009-06-29 08:55:37 castagna Exp $
*/

package com.hp.hpl.jena.db.impl;

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.db.impl.DriverRDB;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
* @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:37 $
*/

public  class PSet_TripleStore_RDB implements IPSet {

//=======================================================================
// Cutomization variables

   /**
	* Holds name of AssertedStatement table (defaults to JENA_SYS_AssStatements).
	* Every triple store has at least one tables for AssertedStatements.
	*/
   public String m_tblName = null;
   
    /** The SQL type to use for storing ids (compatible with wrapDBID) */
    protected String ID_SQL_TYPE = null;

    /** Set to true if the insert operations already check for duplications */
    protected boolean SKIP_DUPLICATE_CHECK = false;

    /** Set to true to enable cache of pre-prepared statements */
    protected boolean CACHE_PREPARED_STATEMENTS = true;

	/** The table of sql driver statements */
	protected SQLCache m_sql = null;
	
//=======================================================================
// Internal variables

    /** default size for literal and resource caches */
    protected final static int DEFAULT_CACHE = 1000;

    /** Cache of literals */
    protected ICache<IDBID, Node_Literal> literalCache = new SimpleCache<IDBID, Node_Literal>(DEFAULT_CACHE);

    /** Cache of resources */
    protected ICache<IDBID, Node> resourceCache = new SimpleCache<IDBID, Node>(DEFAULT_CACHE);
	
	/** 
	 * The IRDBDriver for the database.	 
	 */
	protected IRDBDriver m_driver = null;
	
//=======================================================================
// Constructors and accessors

    /**
     * Constructor.
     */
    public PSet_TripleStore_RDB(){
    }
    	    
	/**
	 * Link an existing instance of the IPSet to a specific driver
	 */
	@Override
    public void setDriver(IRDBDriver driver) throws RDFRDBException {
		m_driver = driver;
	}

    private static Logger logger = LoggerFactory.getLogger(PSet_TripleStore_RDB.class);
    
	@Override
    public void setSQLType(String value) { ID_SQL_TYPE = value; }
	@Override
    public void setSkipDuplicateCheck(boolean value) { SKIP_DUPLICATE_CHECK = value;}
	@Override
    public void setSQLCache(SQLCache cache ) { m_sql = cache; }
	@Override
    public SQLCache getSQLCache() { return m_sql; }
	@Override
    public void setCachePreparedStatements(boolean value) { CACHE_PREPARED_STATEMENTS = value; }
	
	
	/**
	 * Sets m_tblName variable.
	 * @param tblName the name of the Statement Table
	 */
	@Override
    public void setTblName(String tblName){
		m_tblName = tblName;
	}
	
	/**
	 * Accessor for m_tblName.
	 * @return name of the Statement table.
	 */
	@Override
    public String getTblName() {
		return m_tblName;
	}
	

	/**
	 * Close this PSet
	 */
	@Override
    public void close() {
		// no need to do anything here for now
	}
	
	/**
	 * @return the database driver.
	 */

	@Override
    public IRDBDriver driver() {
		return m_driver;
	}

    
    /**
     * Remove all RDF information about this pset from a database.
     */
    @Override
    public void cleanDB() {
    	
    	// drop my own table(s)
    	try {
    		m_sql.runSQLGroup("dropStatementTable",getTblName());
    	} catch (SQLException e) {
			logger.warn( "Problem dropping table " + getTblName(), e );
			throw new RDFRDBException("Failed to drop table ", e);
		}
    		        
    }

     /**
      * Printable name for the driver configuration
      */
     @Override
    public String toString() {
        return this.getClass().getPackage().getName();
     }
     
	/**
	 * Fetch a literal from the cache just knowing its literal rdb-id.
	 * If it is not in the cache, do not attempt to retrieve it from the database.
	 */
	public Node_Literal getLiteralFromCache(IDBID id) {
		return literalCache.get(id);
	}

    /**
     * Convert the raw SQL object used to store a database identifier into a java object
     * which meets the IDBID interface.
     */
    public IDBID wrapDBID(Object id) throws RDFRDBException {
        if (id instanceof Number) {
            return new DBIDInt(((Number)id).intValue());
        } else if (id == null) {
            return null;
        } else {
            throw new RDFRDBException("Unexpected DB identifier type: " + id);
            //return null;
        }
    }
    
	/** 
	 * Compute the number of rows in a table.
	 * 
	 * @return int count.
	 */
	@Override
    public int rowCount(int gid) {
	String tName = getTblName();
	int result = 0;
	ResultSet rs=null;

	try {
		 String op = "getRowCount"; 
		 PreparedStatement ps = m_sql.getPreparedSQLStatement(op,tName);
		 ps.setInt(1, gid);
		 rs = ps.executeQuery();
	     while ( rs.next() ) result = rs.getInt(1); 
		m_sql.returnPreparedSQLStatement(ps);
	} catch (SQLException e) {
	   logger.debug("tried to count rows in " + tName);
	   logger.debug("Caught exception: ", e);
           throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
	} finally {
		if (rs != null)
				try {
					rs.close();
				} catch (SQLException e1) {
					throw new RDFRDBException("Failed to get last inserted ID: " + e1);
				}
	}
	return result;
	}

	//=======================================================================
// Patched functions to adapt to oracle jdbc driver expectations

	/**
	 * Convert the current row of a result set from a ResultSet
	 * to a Triple.
	 * Expects row to contain:
	 *    S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral
	 * @param rs the resultSet to be processed.
	 */
	@Override
    public Triple extractTripleFromRowData(
		String subj,
		String pred,
		String obj) {
		
		Node subjNode = subj == null ? null : m_driver.RDBStringToNode(subj);
		Node predNode = pred == null ? null : m_driver.RDBStringToNode(pred);
		Node objNode = obj == null ? null : m_driver.RDBStringToNode(obj);

		return ( Triple.create(subjNode, predNode, objNode) );
	}



    /**
     * Wrap up a boolean flag as a object which the jdbc driver can assert into a boolean/short column.
     */
    public Object wrapFlag(boolean flag) {
        return  flag ? new Short((short)1) : new Short((short)0);
    }
    
	/**
	 *
	 * Attempt to remove a statement from an Asserted_Statement table,
	 * if it is present.  Return without error if the statement is not
	 * present.
	 *
	 * @param subj_uri is the URI of the subject
	 * @param pred_uri is the URI of the predicate (property)
	 * @param obj_node is the URI of the object (can be URI or literal)
	 * @param graphID is the ID of the graph
	 * @param complete is true if this handler is capable of adding this triple.
	 *
	 **/
  @Override
public void deleteTriple(Triple t, IDBID graphID) {
  	deleteTriple(t, graphID, false, null);
  }
  	
  /**
   *
   * Attempt to remove a statement from an Asserted_Statement table,
   * if it is present.  Return without error if the statement is not
   * present.
   *
   * @param subj_uri is the URI of the subject
   * @param pred_uri is the URI of the predicate (property)
   * @param obj_node is the URI of the object (can be URI or literal)
   * @param graphID is the ID of the graph
   * @param complete is true if this handler is capable of adding this triple.
   *
   **/
	public void deleteTriple(Triple t, IDBID graphID, boolean isBatch,
		Hashtable<String, PreparedStatement> batchedPreparedStatements) {
			deleteTripleAR(t,graphID,null,isBatch,batchedPreparedStatements);
		}
	/**
	 *
	 * Attempt to remove a statement from an Asserted_Statement table,
	 * if it is present.  Return without error if the statement is not
	 * present.
	 *
	 * @param subj_uri is the URI of the subject
	 * @param pred_uri is the URI of the predicate (property)
	 * @param obj_node is the URI of the object (can be URI or literal)
	 * @param stmtURI is the URI of the statement if reified, null for asserted
	 * @param graphID is the ID of the graph
	 * @param complete is true if this handler is capable of adding this triple.
	 *
	 **/
public void deleteTripleAR(
	Triple t,
	IDBID graphID,
	Node reifNode,
	boolean isBatch,
	Hashtable<String, PreparedStatement> batchedPreparedStatements) {
	boolean isReif = reifNode != null;

	String subj =
		t.getSubject().equals(Node.NULL) ? null : m_driver.nodeToRDBString(t.getSubject(),false);
	String pred =
		t.getPredicate().equals(Node.NULL) ? null : m_driver.nodeToRDBString(t.getPredicate(),false);
	String obj =
		t.getObject() == Node.ANY ? null : m_driver.nodeToRDBString(t.getObject(),false);
//	String gid = graphID.getID().toString();
	int gid = ((DBIDInt) graphID).getIntID();
	int argc = 1;
	String stmtStr;
	
	if ((subj == null) || (pred == null) || (obj == null)) {
//		throw new JenaException("Attempt to delete triple with missing values");
//		used to think this was an exception. i guess it's not.
		return;
	}

	// get statement string	   	   
	PreparedStatement ps = null;
	stmtStr = isReif ? "deleteReified" : "deleteStatement";
	try {
		ps =
			getPreparedStatement(
				stmtStr,
				getTblName(),
				isBatch,
				batchedPreparedStatements);
		//ps.clearParameters();

	} catch (SQLException e1) {
	    logger.debug( "SQLException caught " + e1.getErrorCode(), e1);
            throw new JenaException("Exception during database access", e1);    // Rethrow in case there is a recovery option
	}

	// now fill in parameters
	try {
		ps.setString(argc++, subj);
		ps.setString(argc++, pred);
		ps.setString(argc++, obj);

		ps.setInt(argc++, gid);

		if (isReif) {
			String stmtURI = m_driver.nodeToRDBString(reifNode,false);
			ps.setString(argc++, stmtURI);
			ps.setString(argc++,"T");
		}
	} catch (SQLException e1) {
	    logger.debug("(in delete) SQLException caught ", e1);
            throw new JenaException("Exception during database access", e1);    // Rethrow in case there is a recovery option
	}

	try {
		if (isBatch) {
			ps.addBatch();
		} else {
			ps.executeUpdate();
			m_sql.returnPreparedSQLStatement(ps);
		}
	} catch (SQLException e1) {
		logger.error("Exception executing delete: ", e1);
                throw new JenaException("Exception during database access", e1);    // Rethrow in case there is a recovery option
	}
}

		/**
		 *
		 * Attempt to store a statement into an Asserted_Statement table.
		 *
		 * @param subj_uri is the URI of the subject
		 * @param pred_uri is the URI of the predicate (property)
		 * @param obj_node is the URI of the object (can be URI or literal)
		 * @param graphID is the ID of the graph
		 * @param complete is true if this handler is capable of adding this triple.
		 *
		 **/
	  @Override
    public void storeTriple(Triple t, IDBID graphID) {
	  	storeTriple(t,graphID,false, null);
	  }
	  
	  /**
	   * Given an operation name, a table name, whether or not this operation is part of a batched update, and
	   * a table of batched prepared statements, find or create an appropriate PreparedStatement.
	   * 
	   * @param op
	   * @param tableName
	   * @param isBatch
	   * @param batchedPreparedStatements
	   * @return the prepared statement
	   * @throws SQLException
	   */
	  public PreparedStatement getPreparedStatement(String op, 
	  				String tableName, 
	  				boolean isBatch, 
	  				Hashtable<String, PreparedStatement> batchedPreparedStatements) throws SQLException {
	  	PreparedStatement ps = null;
		String opname = SQLCache.concatOpName(op,tableName);
		if (isBatch) {
			ps = batchedPreparedStatements.get(opname);
			if (ps == null) {
				ps = m_sql.getPreparedSQLStatement(op,tableName);
				batchedPreparedStatements.put(opname,ps);
			}
		} else {
			ps = m_sql.getPreparedSQLStatement(op,tableName);
		}
	 	 
		if (ps == null) {
			logger.error("prepared statement not found for " + opname);
		}
		return ps;
	  }


		/**
		 *
		 * Attempt to store a statement into an Asserted_Statement table.
		 *
		 * @param subj_uri is the URI of the subject
		 * @param pred_uri is the URI of the predicate (property)
		 * @param obj_node is the URI of the object (can be URI or literal)
		 * @param graphID is the ID of the graph
		 * @param isBatch is true if this request is part of a batch operation.
		 *
		 **/
	  public void storeTriple(Triple t, 
	  						IDBID graphID,
	  						boolean isBatch, 
	  						Hashtable<String, PreparedStatement> batchedPreparedStatements) {
	  		 storeTripleAR(t,graphID,null,false,isBatch,batchedPreparedStatements);
	  }
	  
		/**
		 *
		 * Attempt to store a statement into an Asserted_Statement table.
		 *
		 * @param subj_uri is the URI of the subject
		 * @param pred_uri is the URI of the predicate (property)
		 * @param obj_node is the URI of the object (can be URI or literal)
		 * @param stmtURI is the URI of the statement if reified, null for asserted
		 * @param hasType is true if the hasType flag should be set for a reified stmt 
		 * @param graphID is the ID of the graph
		 * @param isBatch is true if this request is part of a batch operation.
		 *
		 **/
	public void storeTripleAR(
		Triple t,
		IDBID graphID,
		Node reifNode,
		boolean hasType,
		boolean isBatch,
		Hashtable<String, PreparedStatement> batchedPreparedStatements) {
		String objURI;
		Object obj_val;
		boolean isReif = reifNode != null;

		//	if database doesn't perform duplicate check
		if (!SKIP_DUPLICATE_CHECK && !isReif) {
			// if statement already in table
			if (statementTableContains(graphID, t)) {
				return;
			}
		}
		
		String obj_res, obj_lex, obj_lit;
		// TO DO: Node.NULL is only valid for reif triple stores. should check this.
		String subj =
			t.getSubject().equals(Node.NULL) ? null : m_driver.nodeToRDBString(t.getSubject(),true);
		String pred =
			t.getPredicate().equals(Node.NULL) ? null : m_driver.nodeToRDBString(t.getPredicate(),true);
		String obj =
			t.getObject().equals(Node.NULL) ? null : m_driver.nodeToRDBString(t.getObject(),true);
//		String gid = graphID.getID().toString();
		int gid = ((DBIDInt) graphID).getIntID();

		int argc = 1;
		String stmtStr;

		if ((subj == null) || (pred == null) || (obj == null)) {
			if (!isReif)
				throw new JenaException("Attempt to assert triple with missing values");
		}
		// get statement string

		PreparedStatement ps = null;
		stmtStr = isReif ? "insertReified" : "insertStatement";
		try {
			ps =
				getPreparedStatement(
					stmtStr,
					getTblName(),
					isBatch,
					batchedPreparedStatements);
			//ps.clearParameters();

		} catch (SQLException e1) {
			logger.debug("SQLException caught " + e1.getErrorCode(), e1);
                        throw new JenaException("Exception during database access", e1);    // Rethrow in case there is a recovery option
		}
		// now fill in parameters
		try {
			if (subj == null)
				ps.setNull(argc++, java.sql.Types.VARCHAR);
			else
				ps.setString(argc++, subj);
			if (pred == null)
				ps.setNull(argc++, java.sql.Types.VARCHAR);
			else
				ps.setString(argc++, pred);
			if (obj == null)
				ps.setNull(argc++, java.sql.Types.VARCHAR);
			else
				ps.setString(argc++, obj);

			// add graph id and, if reifying, stmturi and hastype
			ps.setInt(argc++, gid);
			if (isReif) {
				String stmtURI = m_driver.nodeToRDBString(reifNode,true);
				ps.setString(argc++, stmtURI);
				if (hasType == true)
					ps.setString(argc++, "T");
				else
					ps.setString(argc++, " "); // not nullable
			}

		} catch (SQLException e1) {
			logger.debug("SQLException caught " + e1.getErrorCode(), e1);
                        throw new JenaException("Exception during database access", e1);    // Rethrow in case there is a recovery option
		}

		try {
			if (isBatch) {
				ps.addBatch();
			} else {
				ps.executeUpdate();
				m_sql.returnPreparedSQLStatement(ps);
			}
			//ps.close();
		} catch (SQLException e1) {
			// we let Oracle handle duplicate checking
			if (!((e1.getErrorCode() == 1)
				&& (m_driver.getDatabaseType().equalsIgnoreCase("oracle")))) {
				logger.error(
					"SQLException caught during insert"
						+ e1.getErrorCode(),
						e1);
                          throw new JenaException("Exception during database access", e1 );
			}
		}
	}
	
	/**
	 * Attempt to add a list of triples to the specialized graph.
	 * 
	 * As each triple is successfully added it is removed from the List. If
	 * complete is true then the entire List was added and the List will be
	 * empty upon return. if complete is false, then at least one triple remains
	 * in the List.
	 * 
	 * If a triple can't be stored for any reason other than incompatability
	 * (for example, a lack of disk space) then the implemenation should throw a
	 * runtime exception.
	 * 
	 * @param triples
	 *            List of triples to be added. This is modified by the call.
	 * @param my_GID
	 *            ID of the graph.
	 */
	@Override
    public void storeTripleList(List<Triple> triples, IDBID my_GID) {
		// for relational dbs, there are two styles for bulk inserts.
		// JDBC 2.0 supports batched updates.
		// MySQL also supports a multiple-row insert.
		// For now, we support only jdbc 2.0 batched updates
		/** Set of PreparedStatements that need executeBatch() * */
		Triple t;
		String cmd;
		boolean autoState = false;
		DriverRDB drvr = (DriverRDB) m_driver;
		Iterator<Triple> it = triples.iterator();
		Hashtable<String, PreparedStatement> batchedPreparedStatements = null;

		if ( SKIP_DUPLICATE_CHECK == false ) {
//		if ( false ) {
			while (it.hasNext()) {
				t = it.next();
				storeTriple(t, my_GID, false, null);
			}
		} else 
		try {
			autoState = drvr.xactOp(DriverRDB.xactAutoOff);
			batchedPreparedStatements = new Hashtable<String, PreparedStatement>();
			while (it.hasNext()) {
				t = it.next();
				storeTriple(t, my_GID, true, batchedPreparedStatements);
			}

			Enumeration<String> en = batchedPreparedStatements.keys();
			while (en.hasMoreElements()) {
				String op = en.nextElement();
				PreparedStatement p = batchedPreparedStatements
						.get(op);
				p.executeBatch();
				batchedPreparedStatements.remove(op);
				m_sql.returnPreparedSQLStatement(p);
			}
			if (autoState) {
				drvr.xactOp(DriverRDB.xactCommit);
				drvr.xactOp(DriverRDB.xactAutoOn);
			}
			batchedPreparedStatements = null; 
		
		// WARNING: caught exceptions should drop through to return.
		// if not, be sure to reset autocommit before exiting.

		} catch (BatchUpdateException b) {
			System.err.println("SQLException: " + b.getMessage());
			System.err.println("SQLState: " + b.getSQLState());
			System.err.println("Message: " + b.getMessage());
			System.err.println("Vendor: " + b.getErrorCode());
			System.err.print("Update counts: ");
			int[] updateCounts = b.getUpdateCounts();
			for (int i = 0; i < updateCounts.length; i++) {
				System.err.print(updateCounts[i] + " ");
			}
			if (autoState) drvr.xactOp(DriverRDB.xactAutoOn);
		} catch (SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.err.println("SQLState: " + ex.getSQLState());
			System.err.println("Message: " + ex.getMessage());
			System.err.println("Vendor: " + ex.getErrorCode());
			if (autoState) drvr.xactOp(DriverRDB.xactAutoOn);
		} finally {
			if ( batchedPreparedStatements != null ) {
			Enumeration<String> en = batchedPreparedStatements.keys();
			while (en.hasMoreElements()) {
				String op = en.nextElement();
				PreparedStatement p = batchedPreparedStatements
						.get(op);
				batchedPreparedStatements.remove(op);
				m_sql.returnPreparedSQLStatement(p);
			}
			}
		}
		//ArrayList<Triple> c = new ArrayList<Triple>(triples);
		// triples.removeAll(c);
		triples.removeAll(triples);
	}

	/**
	 * Attempt to remove a list of triples from the specialized graph.
	 * 
	 * As each triple is successfully deleted it is removed from the List. If
	 * complete is true then the entire List was added and the List will be
	 * empty upon return. if complete is false, then at least one triple remains
	 * in the List.
	 * 
	 * If a triple can't be stored for any reason other than incompatability
	 * (for example, a lack of disk space) then the implemenation should throw a
	 * runtime exception.
	 * 
	 * @param triples
	 *            List of triples to be added. This is modified by the call.
	 * @param my_GID
	 *            ID of the graph.
	 */
	@Override
    public void deleteTripleList(List<Triple> triples, IDBID my_GID) {
		// for relational dbs, there are two styles for bulk operations.
		// JDBC 2.0 supports batched updates.
		// MySQL also supports a multiple-row update.
		// For now, we support only jdbc 2.0 batched updates

		/** Set of PreparedStatements that need executeBatch() * */
		Hashtable<String, PreparedStatement> batchedPreparedStatements = null;
		Triple t;
		String cmd;
		boolean autoState = false;
		DriverRDB drvr = (DriverRDB) m_driver;
		Iterator<Triple> it = triples.iterator();
		
		if ( SKIP_DUPLICATE_CHECK == false ) {
//		if ( false ) {

			while (it.hasNext()) {
				t = it.next();
				deleteTriple(t, my_GID, false, null);
			}
		} else 
		try {
			autoState = drvr.xactOp(DriverRDB.xactAutoOff);
			batchedPreparedStatements = new Hashtable<String, PreparedStatement>();
			while (it.hasNext()) {
				t = it.next();
				deleteTriple(t, my_GID, true, batchedPreparedStatements);
			}

			Enumeration<String> en = batchedPreparedStatements.keys();
			while (en.hasMoreElements()) {
				String op = en.nextElement();
				PreparedStatement p = batchedPreparedStatements
						.get(op);
				p.executeBatch();
				batchedPreparedStatements.remove(op);
				m_sql.returnPreparedSQLStatement(p);
			}
			if (autoState) {
				drvr.xactOp(DriverRDB.xactCommit);
				drvr.xactOp(DriverRDB.xactAutoOn);
			}
			batchedPreparedStatements = null;
			
		// WARNING: caught exceptions should drop through to return.
		// if not, be sure to reset autocommit before exiting.

	} catch (BatchUpdateException b) {
			System.err.println("SQLException: " + b.getMessage());
			System.err.println("SQLState: " + b.getSQLState());
			System.err.println("Message: " + b.getMessage());
			System.err.println("Vendor: " + b.getErrorCode());
			System.err.print("Update counts: ");
			int[] updateCounts = b.getUpdateCounts();
			for (int i = 0; i < updateCounts.length; i++) {
				System.err.print(updateCounts[i] + " ");
			}
			if (autoState) drvr.xactOp(DriverRDB.xactAutoOn);
		} catch (SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.err.println("SQLState: " + ex.getSQLState());
			System.err.println("Message: " + ex.getMessage());
			System.err.println("Vendor: " + ex.getErrorCode());
			if (autoState) drvr.xactOp(DriverRDB.xactAutoOn);
		}
		finally {
			if ( batchedPreparedStatements != null ) {
				Enumeration<String> en = batchedPreparedStatements.keys();
				while (en.hasMoreElements()) {
					String op = en.nextElement();
					PreparedStatement p = batchedPreparedStatements
							.get(op);
					batchedPreparedStatements.remove(op);
					m_sql.returnPreparedSQLStatement(p);
				}
			}
		}
		ArrayList<Triple> c = new ArrayList<Triple>(triples);
		triples.removeAll(c);
	}

	/**
	 * Compute the number of unique triples added to the Specialized Graph.
	 * 
	 * @return int count.
	 */
	@Override
    public int tripleCount(IDBID graphId) {
		int gid = ((DBIDInt) graphId).getIntID();
		return(rowCount(gid));
	}
    

	/**
	 * Tests if a triple is contained in the specialized graph.
	 * @param t is the triple to be tested
	 * @param graphID is the id of the graph.
	 * @return boolean result to indicte if the tripple was contained
	 */
	@Override
    public boolean statementTableContains(IDBID graphID, Triple t) {
	   ExtendedIterator<Triple> it = find( t,  graphID );
	   boolean res = it.hasNext();
	   it.close();
	   return res;
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.IPSet#find(com.hp.hpl.jena.graph.TripleMatch, com.hp.hpl.jena.db.impl.IDBID)
	 */
	@Override
    public ExtendedIterator<Triple> find(TripleMatch t, IDBID graphID) {
		String astName = getTblName();
		Node subj_node = t.getMatchSubject();
		Node pred_node = t.getMatchPredicate();
		Node obj_node = t.getMatchObject();
		Node_Literal objLit;
		//	   String gid = graphID.getID().toString();
		int gid = ((DBIDInt) graphID).getIntID();
		boolean notFound = false;
int hack = 0;

		ResultSetTripleIterator result =
			new ResultSetTripleIterator(this, graphID);

		PreparedStatement ps = null;

		String subj = null;
		String pred = null;
		String obj = null;
		String op = "selectStatement";
		String qual = "";
		int args = 1;
if ( hack != 0 ) {
	subj_node = pred_node = obj_node = null;
}
		if (subj_node != null) {
			subj = m_driver.nodeToRDBString(subj_node, false);
			if (subj == null)
				notFound = true;
			else
				qual += "S";
		}
		if (pred_node != null) {
			pred = m_driver.nodeToRDBString(pred_node, false);
			if (pred == null)
				notFound = true;
			else
				qual += "P";
		}
		if (obj_node != null) {
			obj = m_driver.nodeToRDBString(obj_node, false);
			if (obj == null)
				notFound = true;
			else
				qual += "O";
		}
		if (notFound == false)
			try {
				op += qual;
				/*
				ps = m_sql.getPreparedSQLStatement(op, getTblName());
				if ( qual.equals("") ) {
					ps = m_sql.getPreparedSQLStatement(op+"Limit", getTblName(),Integer.toString(gid));
				
					// Statement stmt = m_driver.getConnection().getConnection().createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
					//			  java.sql.ResultSet.CONCUR_READ_ONLY);
					// stmt.setFetchSize(10);
					// String qry = "SELECT S.Subj, S.Prop, S.Obj FROM " + getTblName() + " S WHERE S.GraphID = "
					// 	+ gid;
					// ResultSet res = stmt.executeQuery(qry);
					result = new ResultSetLimitTripleIter(this, graphID);
				} else {
				//*/
				ps = m_sql.getPreparedSQLStatement(op, getTblName());
				if (obj != null)
					ps.setString(args++, obj);
				if (subj != null)
					ps.setString(args++, subj);
				if (pred != null)
					ps.setString(args++, pred);

				ps.setInt(args++, gid);
				//*/ }
				
//				if ( getTblName().equals("jena_g1t1_stmt"))
//				{
//				    // DEBUG - destructive.
//	                System.out.println("SQL: "+op+"("+subj+","+pred+","+obj+") ["+gid+"] "+getTblName()) ;
//	                ps.execute() ;
//	                ResultSet rs = ps.getResultSet();
//	                while(rs.next())
//	                {
//	                    System.out.println(rs.getString(1)) ;
//                        System.out.println(rs.getString(2)) ;
//                        System.out.println(rs.getString(3)) ;
//	                }
//				}
				
				m_sql.executeSQL(ps, op, result);

				//m_sql.returnPreparedSQLStatement(ps,op);
			} catch (Exception e) {
				notFound = true;
				logger.debug( "find encountered exception: args=" + args + " err: ",  e);
                                throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
			}

		if ( notFound ) result.close();
		return (new TripleMatchIterator(t.asTriple(), result));
	}

		/* (non-Javadoc)
		 * @see com.hp.hpl.jena.graphRDB.IPSet#removeStatementsFromDB()
		 */
		@Override
        public void removeStatementsFromDB(IDBID graphID) {
			int gid = graphID.getIntID() ;
			
			try {
				  PreparedStatement ps = m_sql.getPreparedSQLStatement("removeRowsFromTable",getTblName());
				  ps.clearParameters();	
	
				  ps.setInt(1,gid);
				  ps.executeUpdate();
				  m_sql.returnPreparedSQLStatement(ps);
				 } catch (SQLException e) {
					logger.error("Problem removing statements from table: ", e);
                                        throw new JenaException("Exception during database access", e);    // Rethrow in case there is a recovery option
				 }
		}
}

/*
 *  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
	
 
