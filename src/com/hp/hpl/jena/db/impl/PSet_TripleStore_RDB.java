/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
*/

package com.hp.hpl.jena.db.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import java.util.zip.CRC32;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.util.Log;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

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
* @version $Revision: 1.21 $ on $Date: 2003-06-12 15:10:01 $
*/

public  class PSet_TripleStore_RDB implements IPSet {

//=======================================================================
// Cutomization variables

   public static String SYS_LITERAL_TNAME = "JENA_LITERALS";
   public static String SYS_AS_TNAME = "JENA_StmtAsserted";
   
   /**
    * Names of tables for which this PSet instance is responsible.
    * Contains Strings.
    */
   protected ArrayList m_tablenames = new ArrayList();
	
   /**
	* Holds name of AssertedStatement table (defaults to JENA_SYS_AssStatements).
	* Every triple store has at least one tables for AssertedStatements.
	*/
   public String m_ASTName = null;
   
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

	public void setMaxLiteral(int value) { MAX_LITERAL = value; }
	public void setSQLType(String value) { ID_SQL_TYPE = value; }
	public void setSkipDuplicateCheck(boolean value) { SKIP_DUPLICATE_CHECK = value;}
	public void setSkipAllocateId(boolean value ) { SKIP_ALLOCATE_ID = value;}
	public void setEmptyLiteralMarker(String value ) { EMPTY_LITERAL_MARKER = value;}
	public void setSQLCache(SQLCache cache ) { m_sql = cache; }
	public void setInsertByProcedure(boolean value) { INSERT_BY_PROCEDURE = value; }
	public void setCachePreparedStatements(boolean value) { CACHE_PREPARED_STATEMENTS = value; }
	
	
	/**
	 * Sets m_ASTName variable.
	 * 
	 * @param newName the name of the Asserted Statement Table
	 */
	public void setASTname(String newName){
		m_ASTName = m_driver.toDBIdentifier(newName);
		if (! doesTableExist(m_ASTName)) {
			createASTable(m_ASTName);
		}
		if (! m_tablenames.contains(m_ASTName))
			m_tablenames.add(m_ASTName);
	}
	
	/**
	 * Accessor for m_ASTName.
	 * @returns name of the asserted statements table.
	 */
	protected String getASTname() {
		return m_ASTName;
	}
	

	/**
	 * Close this PSet
	 */
	public void close() {
		// no need to do anything here for now
	}

    
//=======================================================================
// Database operations

    
    /**
     * Check to see if a table with the specified name exists in the database.
     * @param tName table name
     * @return boolean indicating whether or not table is present.
     */
    public boolean doesTableExist(String tName) {
		try {
			DatabaseMetaData dbmd = m_driver.getConnection().getConnection().getMetaData();
			String[] tableTypes = { "TABLE" };
			ResultSet alltables = dbmd.getTables(null, null, "JENA%", tableTypes);
			List tablesPresent = new ArrayList(10);
			while (alltables.next()) {
				tablesPresent.add(alltables.getString("TABLE_NAME").toUpperCase());
			}
			alltables.close();
			boolean ok = true;
			//TODO get these names from someplace
			ok &= tablesPresent.contains(tName.toUpperCase());
			return ok;
		} catch (SQLException e1) {
			throw new RDFRDBException("Internal SQL error in driver", e1);
		}
    }
    
    /**
     * Create a table for storing asserted statements.
     * 
     * @param astName name of table.
     */
    public void createASTable(String astName) {
    	
		try {m_sql.runSQLGroup("createStatementTable", getASTname());
		} catch (SQLException e) {
			com.hp.hpl.jena.util.Log.warning("Problem formatting database", e);
			throw new RDFRDBException("Failed to format database", e);
		}
		m_tablenames.add(astName);
    }

    /**
     * Remove all RDF information about this pset from a database.
     */
    public void cleanDB() {
    	
    	// drop my own table(s)
    	try {
    		m_sql.runSQLGroup("dropStatementTable",getASTname());
    	} catch (SQLException e) {
			Log.warning("Problem dropping table " + getASTname() + e);
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
	
	/**
	 * Register a literal in the literals table.  
	 * Add it if it is not there.  If it already present, return its id.
	 * @return the db index of the added literal 
	 */
	public IDBID addLiteral(Node_Literal l) throws RDFRDBException {
        IDBID id = null;
        if (!SKIP_DUPLICATE_CHECK) {
            id = getLiteralID(l);
            if (id != null) return id;
        }
        try {
            int val = 0;               // Integer translation of l, if possible
            boolean isInt = false; // true if literal can be interpreted as an integer
			boolean hasLang = false; // true if literal has language
			boolean hasType = false;
			boolean isHuge = false; // true if literal larger than MAX_LITERAL
/*            // Check if the literal can be translated to an int
            try {
                val = Integer.parseInt(l.toString());  note: toString doesn't work here. KW
                isInt = true;
            } catch (NumberFormatException e) {
                isInt = false;
            }
*/
            String opname = "insertLiteral";
            			
			LiteralLabel ll = l.getLiteral();
			String lit = ll.getLexicalForm();


			int len = lit.length();
			if (len > MAX_LITERAL) 
			    isHuge = true;
            
            if (! isInt && (len >= MAX_LITERAL))
            	opname += "Blob";
       
			hasLang = literalHasLang(ll);
			hasType = literalHasType(ll);
			
            //DEBUG System.out.println("opname was " + opname);
            PreparedStatement ps = m_sql.getPreparedSQLStatement(opname);
            int argi = 1;
            if (!SKIP_ALLOCATE_ID) {
                id = allocateLiteralID();
                ps.setObject(argi++, id.getID());
            }
            
            // always populate LiteralIdx (departure from Jena1)
			// insert a subset for indexing and put whole into a blob
			 ps.setString(argi++, len == 0 ? EMPTY_LITERAL_MARKER : getLiteralIdx(lit));
            
            if (len >= MAX_LITERAL || len == 0) {
                // First convert the literal to a UTF-16 encoded byte array
                // (this wouldn't be needed for jdbc 2.0 drivers but not all db's have them)
                byte[] temp = lit.getBytes("UTF-8");
                int lenb = temp.length;
                //System.out.println("utf-16 len = " + lenb);
                byte[] litData = new byte[lenb + 4];
                litData[0] = (byte)(lenb & 0xff);
                litData[1] = (byte)((lenb >> 8) & 0xff);
                litData[2] = (byte)((lenb >> 16) & 0xff);
                litData[3] = (byte)((lenb >> 24) & 0xff);
                System.arraycopy(temp, 0, litData, 4, lenb);
                
                // Oracle has its own way to insert Blobs
				if (isHuge && m_driver.getDatabaseType().equalsIgnoreCase("Oracle")) {
            		//TODO fix to use Blob
            		// For now, we do not support Blobs under Oracle
            		throw new RDFRDBException("Oracle driver does not currently support large literals.");
				} else {
					ps.setBinaryStream(argi++, new ByteArrayInputStream(litData), litData.length);
				}
            } 
            
/*            if (isInt) {
                ps.setInt(argi++, val);
            }       
*/           
            if (hasLang)
				ps.setString(argi++, l.getLiteral().language());
            else
            	ps.setNull(argi++, java.sql.Types.VARCHAR);
			if (hasType)
				ps.setString(argi++, l.getLiteral().getDatatypeURI());
			else
				ps.setNull(argi++, java.sql.Types.VARCHAR);

			
			// TODO update here to work with executeBatch()
            if (INSERT_BY_PROCEDURE) {
                ResultSet rs = ps.executeQuery();
                ResultSetIterator it = new ResultSetIterator(rs, ps, m_sql, opname);
                if (it != null && it.hasNext())
                    id = wrapDBID(it.getSingleton());
            } else {
				  ps.executeUpdate();
        //          m_sql.returnPreparedSQLStatement(ps, opname);
            }
            if (id == null)
                id = getLiteralID(l);
            return id;
        } catch (Exception e1) {
            // /* DEBUG */ System.out.println("Problem on literal (l=" + l.toString().length() + "): " + l);
            /* DEBUG */ System.out.println("Problem on literal (l=" + l.toString().length() + ") " + e1 );
            // System.out.println("ID is: " + id);
            throw new RDFRDBException("Failed to register literal ", e1);
        }
    }
    
    /** The prefix used to distinguish blank nodes from URI's in the database */
    protected static String BlankNodeRDBPrefix = ">";
    
	/**
	* Convert a node (blank or URI only) to a string.
	* @return the string.
	*/
    public static String nodeToRDBString ( Node blankOrURI ) throws RDFRDBException {
    	String res;
    	if ( blankOrURI.isBlank() ) {
    		res = BlankNodeRDBPrefix + blankOrURI.getBlankNodeId().toString();
    	} else if ( blankOrURI.isURI() ) {
    		res = ((Node_URI) blankOrURI).getURI();
			if ( res.startsWith(BlankNodeRDBPrefix) ) {
				throw new RDFRDBException ("URI Node looks like a blank node: " + res );
			}
    	} else {
    		throw new RDFRDBException ("Expected Blank or URI Node, got " + blankOrURI.toString() );	
    	}
    	return res;
    }
    
	/**
	* Convert an RDB string to a node (blank or URI only).
	* @return the node.
	*/
	public static Node RDBStringToNode ( String RDBString ) {
		Node res;
		if ( RDBString.startsWith(BlankNodeRDBPrefix) ) {
			res = Node.createAnon( new AnonId (RDBString.substring(BlankNodeRDBPrefix.length())) );
		} else {
			res = Node.createURI(RDBString);
		}
		return res;
	}
	
	/**
	* Check if a literal (label) is plain.
	* @return true if literal is plain, else false.
	*/
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
	 * Check if database contains this PSet's table(s). 
	 * If not, run specified command to create it.
	 */
	public void initializeStatementTable() throws RDFRDBException {
		Iterator it = m_tablenames.iterator();
		String tname;
		
		while (it.hasNext()) {
			tname = (String) it.next();
			if (! doesTableExist(tname)) {
		  		try {
				   m_sql.runSQLGroup("initializeASTable", getASTname());
		  		} catch (Exception e2) {
					throw new RDFRDBException("Failed to create new statement table", e2);
		  		}
			}
		}
	}


     /**
      * Printable name for the driver configuration
      */
     public String toString() {
        return this.getClass().getPackage().getName();
     }
     
	/**
	 * Extract an indexable sub-string from a literal.
	 * This is used for literals that are too long to index for the given
	 * database.
	 */
	public String getLiteralIdx(String literal) {
		String literal_idx = literal;
		if (literal.length() > (MAX_LITERAL - 16)) {
			// Some databases can't index long literals so literals are stored in two parts,
			// a shorter indexable string plus the full string
			CRC32 checksum = new CRC32();
			checksum.update(literal.getBytes());
			literal_idx = literal.substring(0, MAX_LITERAL-16) + Long.toHexString(checksum.getValue());
		}
		return literal_idx;
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
	 * Fetch a literal just knowing its literal rdb-id.
	 * Can be null if the ID can from a hash function and literal 
	 * isn't registered yet.
	 */
	public Node_Literal getLiteral(IDBID id) throws RDFException {
			// check in cache
			Node_Literal lit = getLiteralFromCache(id);
			if (lit != null) return lit;
			// nope, go back to the database
			try {
				PreparedStatement ps = m_sql.getPreparedSQLStatement("getLiteral");
				ps.setObject(1, id.getID());
				ResultSet rs = ps.executeQuery();
				if (!rs.next()) {
			//		m_sql.returnPreparedSQLStatement(ps, "getLiteral");
					return null;
				}
				lit = extractLiteralFromRow(rs);
	//			m_sql.returnPreparedSQLStatement(ps, "getLiteral");
				literalCache.put(id, lit);
				return lit;
			} catch (Exception e) {
				throw new RDFRDBException("Internal sql error", e);
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
		 PreparedStatement ps = m_sql.getPreparedSQLStatement("getRowCount",tName);
	     ResultSet rs = ps.executeQuery();
	     while ( rs.next() ) {
		  result = rs.getInt("COUNT(*)");
	     } 
	//	m_sql.returnPreparedSQLStatement(ps, "getRowCount");
	} catch (SQLException e) {
	 		Log.debug("tried to count rows in " + tName);
		   	Log.debug("Caught exception: " + e);
	}
	return(result);
	}

	//=======================================================================
// Patched functions to adapt to oracle jdbc driver expectations

    /**
     * Return the database ID for the literal, if it exists
     */
    public IDBID getLiteralID(Node_Literal lnode) throws RDFRDBException {
        LiteralLabel l = ((Node_Literal) lnode).getLiteral();
        String dtype = l.getDatatypeURI();
        String ls = (String)(l.getValue());
        try {
            int len = ls.length();
            if (len >= MAX_LITERAL) {
                ls = getLiteralIdx(ls);
            } // TODO should there be an else return here?
            String lang = l.language();
            boolean nullLang = (lang == null) || (lang.equals(""));
            // Oracle (thin driver at least) fails to match empty strings so have to watch out for when lang is empty
            String opName = nullLang ? "getLiteralIDNoLang" : "getLiteralID";
            if (len == 0) {
                opName += "NullLiteral";
                ls = EMPTY_LITERAL_MARKER;
            }
            PreparedStatement ps = m_sql.getPreparedSQLStatement(opName);
            ps.setString(1, ls);
            if (!nullLang) ps.setString(2, lang);
            ResultSet rs = ps.executeQuery();
            IDBID result = null;
            if (rs.next()) {
                result = wrapDBID(rs.getObject(1));
            };
         //   m_sql.returnPreparedSQLStatement(ps, opName);
            return result;
        } catch (SQLException e1) {
            // /* DEBUG */ System.out.println("Literal truncation (" + l.toString().length() + ") " + l.toString().substring(0, 150));
            throw new RDFRDBException("Failed to find literal", e1);
        }
    }
    
	/**
	 * Convert the current row of a result set from a ResultSet
	 * to a literal.
	 * Expects row to contain: 
	 *    asBLOB, LITERALIDX, LANG 
	 *     
	 * @param rs the resultSet to be processed.
	 */
	public Node_Literal extractLiteralFromRow(ResultSet rs) throws SQLException, IOException,
				RDFException, UnsupportedEncodingException {
		Object blob = rs.getObject(1);
		String literal = null;
		if (blob == null) {
			// No blob so the literal must have fitted within the index field
			literal = rs.getString(2);
		} else {
			InputStream blobin = null;
			if (blobin instanceof InputStream) {
				blobin = (InputStream) blob;
			} else {
				// Probably postgresql with it's BLOB=OID stuff and a broken jdbc driver
				// This re-open of an arg is not guaranteed to work for all drivers but
				// this code is probably only reached for the broken postgresql case which
				// does support arg reopening.
				blobin = rs.getBinaryStream(1);
			}
			int len = blobin.read() & 0xff;
			len |= ((blobin.read() & 0xff) << 8);
			len |= ((blobin.read() & 0xff) << 16);
			len |= ((blobin.read() & 0xff) << 24);
			//System.out.println("Len = " + len);
			byte[] data = new byte[len];
			int read = 0;
			while (read < len) {
				int got = blobin.read(data, read, len-read);
				if (got == -1) {
				throw new RDFRDBException("Premature end of blob in large literal, got " + read);
				}
				read += got;
			}
			blobin.close();
			literal = new String(data, "UTF-8");
		}
		String Lang = rs.getString(3);
		String typeRes = rs.getString(4);
		LiteralLabel llabel;
		if ( typeRes == null ) {
			llabel = new LiteralLabel(literal,Lang == null ? "" : Lang);
		} else {
//			BaseDatatype dt = new BaseDatatype(typeRes);
			RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(typeRes);
			llabel = new LiteralLabel(literal,Lang, dt);
		}	 
		return ((Node_Literal)Node.createLiteral(llabel));
	}

	/**
	 * Convert the current row of a result set from a ResultSet
	 * to a Triple.
	 * Expects row to contain:
	 *    S.SubjRes, S.PropRes, S.ObjRes, S.ObjStr, S.ObjLiteral
	 * @param rs the resultSet to be processed.
	 */
	public Triple extractTripleFromRowData(
		String subjURI,
		String predURI,
		String objURI,
		String objVal,
		String objRef) throws RDFException {
		
		Node subjNode = subjURI == null ? null : RDBStringToNode(subjURI);
		Node predNode = predURI == null ? null : RDBStringToNode(predURI);
		Node objNode = null;
		
		if (objURI != null) {
			objNode = RDBStringToNode(objURI);
		} else if (objRef != null) {
			IDBID objLid = new DBIDInt(Integer.parseInt(objRef));
			objNode = getLiteral(objLid);
		} else if (objVal != null) {
			LiteralLabel llabel = new LiteralLabel(objVal,"");
			objNode = new Node_Literal(llabel);
		} else {
			//no object?
			// throw new RDFRDBException("No object found in Asserted Statement Table");
		}
		
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
	String objURI;
	Object obj_val;
	boolean isReif = reifNode != null;

	String obj_res, obj_lex, obj_lit;

	String subjURI =
		t.getSubject() == Node.ANY ? null : nodeToRDBString(t.getSubject());
	String predURI =
		t.getPredicate() == Node.ANY ? null : nodeToRDBString(t.getPredicate());
	Node obj_node = t.getObject() == Node.ANY ? null : t.getObject();
	String gid = graphID.getID().toString();

	int argc = 1;
	String stmtStr;
	Node_Literal litNode = null;
	// have to init these next 4 vars to prevent java warnings
	LiteralLabel ll = null;
	String lval = null;
	boolean litIsPlain = true;

	if ((subjURI == null) || (predURI == null) || (obj_node == null)) {
		throw new JenaException("Attempt to delete triple with missing values");
	}

	// get statement string	   	   
	PreparedStatement ps = null;
	if ((obj_node == null) || obj_node.isURI() || obj_node.isBlank()) {
		stmtStr =
			isReif
				? "deleteReifStatementObjectURI"
				: "deleteStatementObjectURI";
	} else {
		litNode = (Node_Literal) obj_node;
		ll = litNode.getLiteral();
		lval = (String) ll.getValue();
		litIsPlain = literalIsPlain(ll);
		if (litIsPlain) {
			// object literal can fit in statement table
			stmtStr =
				isReif
					? "deleteReifStatementLiteralVal"
					: "deleteStatementLiteralVal";
		} else {
			stmtStr =
				isReif
					? "deleteReifStatementLiteralRef"
					: "deleteStatementLiteralRef";
		}
	}
	try {
		ps =
			getPreparedStatement(
				stmtStr,
				getASTname(),
				isBatch,
				batchedPreparedStatements);
		ps.clearParameters();

	} catch (SQLException e1) {
		Log.debug("SQLException caught " + e1.getErrorCode() + ": " + e1);
	}

	// now fill in parameters
	try {
		ps.setString(argc++, subjURI);
		ps.setString(argc++, predURI);

		if (obj_node.isURI() || obj_node.isBlank()) {
			objURI = nodeToRDBString(obj_node);
			ps.setString(argc++, objURI);

		} else if (obj_node.isLiteral()) {
			litNode = (Node_Literal) obj_node;
			ll = litNode.getLiteral();
			lval = (String) ll.getValue();
			litIsPlain = literalIsPlain(ll);

			if (litIsPlain) {
				// object literal can fit in statement table

				ps.setString(argc++, lval);
			} else {
				// belongs in literal table
				IDBID lid = getLiteralID(litNode);
				if (lid == null) {
					// return, because if literalid does not exist, then statement
					// can't be in store.
					return;
				}
				ps.setString(argc++, lid.getID().toString());
			}
		}
		ps.setString(argc++, gid);

		if (isReif) {
			String stmtURI = nodeToRDBString(reifNode);
			ps.setString(argc++, stmtURI);
		}
	} catch (SQLException e1) {
		Log.debug("(in delete) SQLException caught " + e1);
	}

	try {
		if (isBatch) {
			ps.addBatch();
		} else {
			ps.executeUpdate();
		}
	} catch (SQLException e1) {
		Log.severe("Exception executing delete: " + e1);
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
	   * @return
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
			Log.severe("prepared statement not found for insertStatementObjectURI");
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
		// TODO: Node.ANY is only valid for reif triple stores. should check this.
		String subjURI =
			t.getSubject() == Node.ANY ? null : nodeToRDBString(t.getSubject());
		String predURI =
			t.getPredicate() == Node.ANY
				? null
				: nodeToRDBString(t.getPredicate());
		Node obj_node = t.getObject() == Node.ANY ? null : t.getObject();
		String gid = graphID.getID().toString();

		int argc = 1;
		String stmtStr;
		Node_Literal litNode = null; // have to init these next 4 vars to prevent java warnings
		LiteralLabel ll = null;
		String lval = null;
		boolean litIsPlain = true;

		if ((subjURI == null) || (predURI == null) || (obj_node == null)) {
			if (!isReif)
				throw new JenaException("Attempt to assert triple with missing values");
		}
		// get statement string

		PreparedStatement ps = null;
		if ((obj_node == null) || obj_node.isURI() || obj_node.isBlank()) {
			stmtStr =
				isReif
					? "insertReifStatementObjectURI"
					: "insertStatementObjectURI";
		} else {
			litNode = (Node_Literal) obj_node;
			ll = litNode.getLiteral();
			lval = (String) ll.getValue();
			litIsPlain = literalIsPlain(ll);
			if (litIsPlain) {
				// object literal can fit in statement table
				stmtStr =
					isReif
						? "insertReifStatementLiteralVal"
						: "insertStatementLiteralVal";
			} else {
				stmtStr =
					isReif
						? "insertReifStatementLiteralRef"
						: "insertStatementLiteralRef";
			}
		}
		try {
			ps =
				getPreparedStatement(
					stmtStr,
					getASTname(),
					isBatch,
					batchedPreparedStatements);
			ps.clearParameters();

		} catch (SQLException e1) {
			Log.debug("SQLException caught " + e1.getErrorCode() + ": " + e1);
		}
		// now fill in parameters
		try {
			if (subjURI == null)
				ps.setNull(argc++, java.sql.Types.VARCHAR);
			else
				ps.setString(argc++, subjURI);
			if (predURI == null)
				ps.setNull(argc++, java.sql.Types.VARCHAR);
			else
				ps.setString(argc++, predURI);

			// add object
			if ((obj_node == null) || obj_node.isURI() || obj_node.isBlank()) {
				objURI = obj_node == null ? null : nodeToRDBString(obj_node);
				if (objURI == null)
					ps.setNull(argc++, java.sql.Types.VARCHAR);
				else
					ps.setString(argc++, objURI);
			} else if (obj_node.isLiteral()) {

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
					ps.setString(argc++, lid.getID().toString());
					ps.setString(argc++, litIdx);
					// TODO should this be here?  Seems redundant to store litIdx?
				}
			}
			// add graph id and, if reifying, stmturi and hastype
			ps.setString(argc++, gid);
			if (isReif) {
				String stmtURI = nodeToRDBString(reifNode);
				ps.setString(argc++, stmtURI);
				if (hasType == true)
					ps.setInt(argc++, 1);
				else
					ps.setNull(argc++, java.sql.Types.INTEGER);
			}

		} catch (SQLException e1) {
			Log.debug("SQLException caught " + e1.getErrorCode() + ": " + e1);
		}

		try {
			if (isBatch) {
				ps.addBatch();
			} else {
				ps.executeUpdate();
			}
		} catch (SQLException e1) {
			// we let Oracle handle duplicate checking
			if (!((e1.getErrorCode() == 1)
				&& (m_driver.getDatabaseType().equalsIgnoreCase("oracle")))) {
				Log.severe(
					"SQLException caught during insert"
						+ e1.getErrorCode()
						+ ": "
						+ e1);
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
				//	m_sql.returnPreparedSQLStatement(p,op);
				}

				m_sql.getConnection().commit();
				m_sql.getConnection().setAutoCommit(true);
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
				m_sql.getConnection().commit();
				m_sql.getConnection().setAutoCommit(true);
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
		return(rowCount(getASTname()));
	}
    

	/**
	 * Tests if a triple is contained in the specialized graph.
	 * @param t is the triple to be tested
	 * @param graphID is the id of the graph.
	 * @return boolean result to indicte if the tripple was contained
	 */
	public boolean statementTableContains(IDBID graphID, Triple t) {
	   PreparedStatement ps;
	   StandardTripleMatch tm = new StandardTripleMatch(t.getSubject(), t.getPredicate(), t.getObject());
			 
	   ExtendedIterator it = find(tm, graphID);
	   return (it.hasNext());
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.IPSet#find(com.hp.hpl.jena.graph.TripleMatch, com.hp.hpl.jena.db.impl.IDBID)
	 */
	public ExtendedIterator find(TripleMatch t, IDBID graphID) {
	   String astName = getASTname();
	   Node subj =  t.getMatchSubject();
	   Node pred =  t.getMatchPredicate();
	   Node obj_node = t.getMatchObject();
	   Node_Literal objLit;
	   String gid = graphID.getID().toString();
	   ResultSetTripleIterator result= new ResultSetTripleIterator(this, graphID);
	   
	   PreparedStatement ps = null;
		
	   String subj_uri = null;
	   String pred_uri = null;
	   String obj_uri = null;
	   String lindx;
	   IDBID lid;
	   String op = "SelectStatement";
	   boolean objIsBlankOrURI = false;
	   int args = 1;
		
	   if (subj != null){
		   subj_uri = nodeToRDBString(subj);
		   op += "S";
	   }
		
	   if (pred!= null) {
		   pred_uri = nodeToRDBString(pred);
		   op += "P";
	   }
	   
	   if ( (obj_node!= null) && (obj_node.isURI() || obj_node.isBlank()) ) {
			objIsBlankOrURI = true;
			obj_uri = nodeToRDBString(obj_node);
	   }
	   
	   try {
	   	if (obj_node != null) {
		   if (objIsBlankOrURI) {
			   op += "OU";
			   ps = m_sql.getPreparedSQLStatement(op,getASTname());
			   ps.setString(args++,obj_uri);
		   } else if (obj_node.isLiteral()) {
			objLit = (Node_Literal)obj_node;
			LiteralLabel ll = objLit.getLiteral();
			String lval = (String)ll.getValue();
			lval = ll.toString();
			boolean litIsPlain = literalIsPlain(ll);
	  
			if (litIsPlain) {
				op += "OV";
				ps = m_sql.getPreparedSQLStatement(op,getASTname());
				ps.setString(args++,lval);
			} else {
				op += "OR";
				IDBID litID = getLiteralID(objLit);
				if (litID == null) {
					// if literal wasn't in store, triple can't be there
					return((ExtendedIterator) result);
				}
				ps = m_sql.getPreparedSQLStatement(op,getASTname());
				ps.setString(args++,litID.getID().toString());
				}
		   }
	   } else {
	   	// if object was null
	    ps = m_sql.getPreparedSQLStatement(op,getASTname());
	   	}
	   
	    if (subj != null){
		   ps.setString(args++,subj_uri);
	    }
		
	    if (pred!= null) {
		  ps.setString(args++,pred_uri);
	     }
	   
	   // set other args
	   ps.setString(args++,gid);
	   } catch (Exception e) {
			   	Log.warning("Getting prepared statement for " + op + " Caught exception " + e);
			   }
			   
	   try {
		  m_sql.executeSQL(ps, op, result);
	   } catch (Exception e) {
		 Log.debug("find encountered exception " + e);
	   }
	   return ( new TripleMatchIterator( t.asTriple(), result ) );
   }

		/* (non-Javadoc)
		 * @see com.hp.hpl.jena.graphRDB.IPSet#removeStatementsFromDB()
		 */
		public void removeStatementsFromDB(IDBID graphID) {
			String gid = graphID.getID().toString();
			
			try {
				  PreparedStatement ps = m_sql.getPreparedSQLStatement("removeRowsFromTable",getASTname());
				  ps.clearParameters();	
	
				  ps.setString(1,gid);
				  ps.executeUpdate();
				 } catch (SQLException e) {
					Log.severe("Problem removing statements from table: ", e);
				 }
		}

		/* (non-Javadoc)
		 * @see com.hp.hpl.jena.graphRDB.IPSet#tableExists(java.lang.String)
		 */
		public boolean tableExists(String tName) {
			return(doesTableExist(tName));
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
	
 
