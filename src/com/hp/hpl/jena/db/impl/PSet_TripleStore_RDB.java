/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: PSet_TripleStore_RDB.java,v 1.36 2003-08-11 02:45:36 wkw Exp $
*/

package com.hp.hpl.jena.db.impl;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import org.apache.log4j.Logger;

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
* @version $Revision: 1.36 $ on $Date: 2003-08-11 02:45:36 $
*/

public  class PSet_TripleStore_RDB implements IPSet {

//=======================================================================
// Cutomization variables

   /**
	* Holds name of AssertedStatement table (defaults to JENA_SYS_AssStatements).
	* Every triple store has at least one tables for AssertedStatements.
	*/
   public String m_tblName = null;
   
    /** The maximum size of literals that can be added to Statement table */
    protected int MAX_LITERAL = 0;

    /** The SQL type to use for storing ids (compatible with wrapDBID) */
    protected String ID_SQL_TYPE = null;

    /** Set to true if the insert operations already check for duplications */
    protected boolean SKIP_DUPLICATE_CHECK = false;

    /** Set to true if the insert operations allocate object IDs themselves */
    protected boolean SKIP_ALLOCATE_ID = false;

    /** Set to true if the insert operations should be done using the "proc" versions */
    protected boolean INSERT_BY_PROCEDURE = false;

    /** Set to true to enable cache of pre-prepared statements */
    protected boolean CACHE_PREPARED_STATEMENTS = true;

	protected String EMPTY_LITERAL_MARKER = "EmptyLiteral";

	/** The table of sql driver statements */
	protected SQLCache m_sql = null;
	
//=======================================================================
// Internal variables

    /** default size for literal and resource caches */
    protected final static int DEFAULT_CACHE = 1000;

    /** Cache of literals */
    protected ICache literalCache = new SimpleCache(DEFAULT_CACHE);

    /** Cache of resources */
    protected ICache resourceCache = new SimpleCache(DEFAULT_CACHE);
	
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
	public void setDriver(IRDBDriver driver) throws RDFRDBException {
		m_driver = driver;
	}

    protected static Logger logger = Logger.getLogger(PSet_TripleStore_RDB.class);
    
	public void setMaxLiteral(int value) { MAX_LITERAL = value; }
	public void setSQLType(String value) { ID_SQL_TYPE = value; }
	public void setSkipDuplicateCheck(boolean value) { SKIP_DUPLICATE_CHECK = value;}
	public void setSQLCache(SQLCache cache ) { m_sql = cache; }
	public SQLCache getSQLCache() { return m_sql; }
	public void setCachePreparedStatements(boolean value) { CACHE_PREPARED_STATEMENTS = value; }
	
	
	/**
	 * Sets m_tblName variable.
	 * @param tblName the name of the Statement Table
	 */
	public void setTblName(String tblName){
		m_tblName = tblName;
	}
	
	/**
	 * Accessor for m_tblName.
	 * @return name of the Statement table.
	 */
	public String getTblName() {
		return m_tblName;
	}
	

	/**
	 * Close this PSet
	 */
	public void close() {
		// no need to do anything here for now
	}
	
	/**
	 * @return the database driver.
	 */

	public IRDBDriver driver() {
		return m_driver;
	}

    
    /**
     * Remove all RDF information about this pset from a database.
     */
    public void cleanDB() {
    	
    	// drop my own table(s)
    	try {
    		m_sql.runSQLGroup("dropStatementTable",getTblName());
    	} catch (SQLException e) {
			logger.warn( "Problem dropping table " + getTblName(), e );
			throw new RDFRDBException("Failed to drop table ", e);
		}
    		        
    }
//	=======================================================================
//	 Support for registering/looking up resources, literals, namespaces
//	 For most categories we need operations to:
//		getXID - determine the DBID, if available
//		allocateXID - allocate a new DBID, if possible
//		getX   - reconstruct the object from its DBID
//		addX   - add to the database, finds DBID as side effect
//	=======================================================================
	  /**
	   * General ID allocate stub.
	   * Calls the given SQL operation to perform the allocation.
	   */
	  public IDBID allocateID(String opname) throws RDFRDBException {
		  try {
			  ResultSetIterator it = m_sql.runSQLQuery(opname, new Object[] {});
			  if (it.hasNext()) {
				  return wrapDBID(it.getSingleton());
			  } else {
				  throw new RDFRDBException("Failed to allocate ID");
			  }
		  } catch (SQLException e) {
			  throw new RDFRDBException("Internal sql error", e);
		  }
	  }
	  
	  
	/**
	 * Return the database ID for the literal and allocate one of necessary
	 */
	public IDBID allocateLiteralID() throws RDFRDBException {
		return allocateID("allocateLiteralID");
	}
	
	protected boolean literalIsPlain ( LiteralLabel ll ) {
		String dtype = ll.getDatatypeURI();
		String lang = ll.language();
		String ls = (String)(ll.getValue());
		
		return (ls.length() < MAX_LITERAL) && !((literalHasLang(ll) || literalHasType(ll)));
	}
	
	protected static boolean literalHasLang ( LiteralLabel ll ) {
		String lang = ll.language();		
		return ((lang != null)  && !lang.equals(""));
	}

	protected static boolean literalHasType ( LiteralLabel ll ) {
		String dtype = ll.getDatatypeURI();		
		return ((dtype != null) && !dtype.equals(""));
	}
			
     /**
      * Printable name for the driver configuration
      */
     public String toString() {
        return this.getClass().getPackage().getName();
     }
     
	/**
	 * Fetch a literal from the cache just knowing its literal rdb-id.
	 * If it is not in the cache, do not attempt to retrieve it from the database.
	 */
	public Node_Literal getLiteralFromCache(IDBID id) {
		return (Node_Literal) literalCache.get(id);
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
	public int rowCount(String tName) {

	int result = 0;
	try {
		 String op = "getRowCount";
		 PreparedStatement ps = m_sql.getPreparedSQLStatement(op,tName);
	     ResultSet rs = ps.executeQuery();
	     while ( rs.next() ) {
		  result = rs.getInt(1);
	     } 
		m_sql.returnPreparedSQLStatement(ps, op);
	} catch (SQLException e) {
	 		logger.debug("tried to count rows in " + tName);
		   	logger.debug("Caught exception: ", e);
	}
	return(result);
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
	public Triple extractTripleFromRowData(
		String subj,
		String pred,
		String obj) {
		
		Node subjNode = subj == null ? null : m_driver.RDBStringToNode(subj);
		Node predNode = pred == null ? null : m_driver.RDBStringToNode(pred);
		Node objNode = obj == null ? null : m_driver.RDBStringToNode(obj);

		return (new Triple(subjNode, predNode, objNode));
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
  public void deleteTriple(Triple t, IDBID graphID) {
  	deleteTriple(t, graphID, false, new Hashtable());
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
		Hashtable batchedPreparedStatements) {
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
	Hashtable batchedPreparedStatements) {
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
		}
	} catch (SQLException e1) {
		logger.debug("(in delete) SQLException caught ", e1);
	}

	try {
		if (isBatch) {
			ps.addBatch();
		} else {
			ps.executeUpdate();
			m_sql.returnPreparedSQLStatement(ps,stmtStr);
		}
	} catch (SQLException e1) {
		logger.error("Exception executing delete: ", e1);
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
	  public void storeTriple(Triple t, IDBID graphID) {
	  	storeTriple(t,graphID,false, new Hashtable());
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
	  				Hashtable batchedPreparedStatements) throws SQLException {
	  	PreparedStatement ps = null;
		String opname = SQLCache.concatOpName(op,tableName);
		if (isBatch) {
			ps = (PreparedStatement) batchedPreparedStatements.get(opname);
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
	  						Hashtable batchedPreparedStatements) {
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
		Hashtable batchedPreparedStatements) {
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
		// TODO: Node.NULL is only valid for reif triple stores. should check this.
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
		}

		try {
			if (isBatch) {
				ps.addBatch();
			} else {
				ps.executeUpdate();
				ps.close();
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
                throw new JenaException( e1 );
			}
		}
	}
	
	/** 
	 * Attempt to add a list of triples to the specialized graph.
	 * 
	 * As each triple is successfully added it is removed from the List.
	 * If complete is true then the entire List was added and the List will 
	 * be empty upon return.  if complete is false, then at least one triple 
	 * remains in the List.
	 * 
	 * If a triple can't be stored for any reason other than incompatability
	 * (for example, a lack of disk space) then the implemenation should throw
	 * a runtime exception.
	 * 
	 * @param triples List of triples to be added.  This is modified by the call.
	 * @param my_GID  ID of the graph.
	 */
		public void storeTripleList(List triples, IDBID my_GID) {
			// for relational dbs, there are two styles for bulk inserts.
			// JDBC 2.0 supports batched updates.
			// MySQL also supports a multiple-row insert.
			// For now, we support only jdbc 2.0 batched updates
			/** Set of PreparedStatements that need executeBatch() **/
			Hashtable batchedPreparedStatements = new Hashtable();
			Triple t;
			String cmd;
			try {
				Connection con = m_sql.getConnection();
				boolean autoState = con.getAutoCommit();
				if (autoState) 
					con.setAutoCommit(false);
				
				Iterator it = triples.iterator();
				
				while (it.hasNext()) {
					t = (Triple) it.next(); 
					storeTriple(t, my_GID, true, batchedPreparedStatements);	
				}
				
				Enumeration enum = batchedPreparedStatements.keys() ; 
				while (enum.hasMoreElements()) {
					String op = (String) enum.nextElement();
					PreparedStatement p = (PreparedStatement) batchedPreparedStatements.get(op);
					p.executeBatch();
					m_sql.returnPreparedSQLStatement(p,op);
				}

				
				if (autoState) {
					m_sql.getConnection().commit();
					con.setAutoCommit(autoState);
				}
				batchedPreparedStatements = new Hashtable();
				ArrayList c = new ArrayList(triples);
				triples.removeAll(c);						
		} catch(BatchUpdateException b) {
					System.err.println("SQLException: " + b.getMessage());
					System.err.println("SQLState: " + b.getSQLState());
					System.err.println("Message: " + b.getMessage());
					System.err.println("Vendor: " + b.getErrorCode());
					System.err.print("Update counts: ");
					int [] updateCounts = b.getUpdateCounts();
					for (int i = 0; i < updateCounts.length; i++) {
						System.err.print(updateCounts[i] + " ");
					}
				} catch(SQLException ex) {
					System.err.println("SQLException: " + ex.getMessage());
					System.err.println("SQLState: " + ex.getSQLState());
					System.err.println("Message: " + ex.getMessage());
					System.err.println("Vendor: " + ex.getErrorCode());
				}
		}

	/** 
	 * Attempt to remove a list of triples from the specialized graph.
	 * 
	 * As each triple is successfully deleted it is removed from the List.
	 * If complete is true then the entire List was added and the List will 
	 * be empty upon return.  if complete is false, then at least one triple 
	 * remains in the List.
	 * 
	 * If a triple can't be stored for any reason other than incompatability
	 * (for example, a lack of disk space) then the implemenation should throw
	 * a runtime exception.
	 * 
	 * @param triples List of triples to be added.  This is modified by the call.
	 * @param my_GID  ID of the graph.
	 */
		public void deleteTripleList(List triples, IDBID my_GID) {
			// for relational dbs, there are two styles for bulk operations.
			// JDBC 2.0 supports batched updates.
			// MySQL also supports a multiple-row update.
			// For now, we support only jdbc 2.0 batched updates
			
			/** Set of PreparedStatements that need executeBatch() **/
			Hashtable batchedPreparedStatements = new Hashtable();
			Triple t;
			String cmd;
			try {
				 Connection con = m_sql.getConnection();
				 boolean autoCommitState = con.getAutoCommit();
				 if (autoCommitState)
				 	con.setAutoCommit(false);
				Iterator it = triples.iterator();
				
				while (it.hasNext()) {
					t = (Triple) it.next(); 
					deleteTriple(t, my_GID, true, batchedPreparedStatements);	
				}
				
				Enumeration enum = batchedPreparedStatements.keys() ; 
				while (enum.hasMoreElements()) {
					String op = (String) enum.nextElement();
					PreparedStatement p = (PreparedStatement) batchedPreparedStatements.get(op);
					p.executeBatch();
				//	m_sql.returnPreparedSQLStatement(p,op);
				} 
				
				if (autoCommitState) {
					m_sql.getConnection().commit();
					m_sql.getConnection().setAutoCommit(autoCommitState);
				}
				
				batchedPreparedStatements = new Hashtable();
				ArrayList c = new ArrayList(triples);
				triples.removeAll(c);						
		} catch(BatchUpdateException b) {
					System.err.println("SQLException: " + b.getMessage());
					System.err.println("SQLState: " + b.getSQLState());
					System.err.println("Message: " + b.getMessage());
					System.err.println("Vendor: " + b.getErrorCode());
					System.err.print("Update counts: ");
					int [] updateCounts = b.getUpdateCounts();
					for (int i = 0; i < updateCounts.length; i++) {
						System.err.print(updateCounts[i] + " ");
					}
				} catch(SQLException ex) {
					System.err.println("SQLException: " + ex.getMessage());
					System.err.println("SQLState: " + ex.getSQLState());
					System.err.println("Message: " + ex.getMessage());
					System.err.println("Vendor: " + ex.getErrorCode());
				}
		}

	/** 
	 * Compute the number of unique triples added to the Specialized Graph.
	 * 
	 * @return int count.
	 */
	public int tripleCount() {
		return(rowCount(getTblName()));
	}
    

	/**
	 * Tests if a triple is contained in the specialized graph.
	 * @param t is the triple to be tested
	 * @param graphID is the id of the graph.
	 * @return boolean result to indicte if the tripple was contained
	 */
	public boolean statementTableContains(IDBID graphID, Triple t) {
	   ExtendedIterator it = find( t,  graphID );
	   return (it.hasNext());
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.IPSet#find(com.hp.hpl.jena.graph.TripleMatch, com.hp.hpl.jena.db.impl.IDBID)
	 */
	public ExtendedIterator find(TripleMatch t, IDBID graphID) {
		String astName = getTblName();
		Node subj_node = t.getMatchSubject();
		Node pred_node = t.getMatchPredicate();
		Node obj_node = t.getMatchObject();
		Node_Literal objLit;
		//	   String gid = graphID.getID().toString();
		int gid = ((DBIDInt) graphID).getIntID();
		boolean notFound = false;

		ResultSetTripleIterator result =
			new ResultSetTripleIterator(this, graphID);

		PreparedStatement ps = null;

		String subj = null;
		String pred = null;
		String obj = null;
		String op = "selectStatement";
		int args = 1;

		if (subj_node != null) {
			subj = m_driver.nodeToRDBString(subj_node, false);
			if (subj == null)
				notFound = true;
			else
				op += "S";
		}
		if (pred_node != null) {
			pred = m_driver.nodeToRDBString(pred_node, false);
			if (pred == null)
				notFound = true;
			else
				op += "P";
		}
		if (obj_node != null) {
			obj = m_driver.nodeToRDBString(obj_node, false);
			if (obj == null)
				notFound = true;
			else
				op += "O";
		}
		if (notFound == false)
			try {
				ps = m_sql.getPreparedSQLStatement(op, getTblName());
				if (obj != null)
					ps.setString(args++, obj);
				if (subj != null)
					ps.setString(args++, subj);
				if (pred != null)
					ps.setString(args++, pred);

				ps.setInt(args++, gid);
				m_sql.executeSQL(ps, op, result);
				//m_sql.returnPreparedSQLStatement(ps,op);
			} catch (Exception e) {
				notFound = true;
				logger.debug( "find encountered exception: args=" + args + " err: ",  e);
			}

		if ( notFound ) result.close();
		return (new TripleMatchIterator(t.asTriple(), result));
	}

		/* (non-Javadoc)
		 * @see com.hp.hpl.jena.graphRDB.IPSet#removeStatementsFromDB()
		 */
		public void removeStatementsFromDB(IDBID graphID) {
			String gid = graphID.getID().toString();
			
			try {
				  PreparedStatement ps = m_sql.getPreparedSQLStatement("removeRowsFromTable",getTblName());
				  ps.clearParameters();	
	
				  ps.setString(1,gid);
				  ps.executeUpdate();
				 } catch (SQLException e) {
					logger.error("Problem removing statements from table: ", e);
				 }
		}

		/* (non-Javadoc)
		 * @see com.hp.hpl.jena.graphRDB.IPSet#tableExists(java.lang.String)
		 */
		public boolean tableExists(String tName) {
			return(m_driver.doesTableExist(tName));
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
	
 
