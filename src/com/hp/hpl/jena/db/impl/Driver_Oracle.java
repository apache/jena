/*
 *  (c) Copyright 2003 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 *
 */

package com.hp.hpl.jena.db.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.RDFRDBException;

/* <---- TO WORK WITH ORACLE, PREFIX THIS LINE WITH "//" (I.E., TO EXPOSE IMPORT STATEMENTS)  -------
import oracle.jdbc.OracleResultSet;
import oracle.sql.BLOB;
import oracle.jdbc.OracleDatabaseMetaData;

/*--------------------------------------------------------------------*/

/**
 * @author hkuno based on code by Dave Reynolds
 *
 * Extends DriverRDB with Oracle-specific parameters.
 * Note: To use this class with Oracle:
 *       1. Uncomment the import statements above.
 *       2. Comment out the interface stubs below.
 *       3. Uncomment the try block in setConnection below.
 *
 */
   public class Driver_Oracle extends DriverRDB {

//* <----- TO WORK WITH ORACLE, PREFIX THIS LINE WITH "/*" (I.E., TO HIDE INTERFACE STUBS) ------
	
	public interface BLOB extends java.sql.Blob {
		OutputStream getBinaryOutputStream();
		int getBufferSize();
		boolean isOpen();
		void close();
	}
	
	private interface OracleResultSet extends ResultSet {
			BLOB getBLOB(int i);		
	}
	/*--------------------------------------------------------------------*/
	
	/** The name of the database type this driver supports */
	
	/** 
	 * Constructor
	 */
	public Driver_Oracle( ){
		super();

		String myPackageName = this.getClass().getPackage().getName();
		
		DATABASE_TYPE = "Oracle";
		DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";
		
		ID_SQL_TYPE = "INTEGER";
		URI_COMPRESS = false;
		INDEX_KEY_LENGTH_MAX = INDEX_KEY_LENGTH = 4000;
		LONG_OBJECT_LENGTH_MAX = LONG_OBJECT_LENGTH = 250;
		TABLE_NAME_LENGTH_MAX =	30;
		/* 30 is a guesstimate. setConnection should be called
		 * immediately to get the correct value. */
		IS_XACT_DB = true;
		PRE_ALLOCATE_ID = true;
		SKIP_DUPLICATE_CHECK = false;
		SQL_FILE = "etc/oracle.sql";
		
		m_psetClassName = myPackageName + ".PSet_TripleStore_RDB";
		m_psetReifierClassName = myPackageName + ".PSet_ReifStore_RDB";
		
		m_lsetClassName = myPackageName + ".SpecializedGraph_TripleStore_RDB";						
		m_lsetReifierClassName = myPackageName + ".SpecializedGraphReifier_RDB";	
		
		
		QUOTE_CHAR = '\'';
		
		DB_NAMES_TO_UPPER = true;
		setTableNames(TABLE_NAME_PREFIX);
	}
	
	/**
	 * Set the database connection
	 */
	public void setConnection( IDBConnection dbcon ) {
		m_dbcon = dbcon;
/* <---- TO WORK WITH ORACLE, PREFIX THIS LINE WITH "//" (I.E., TO EXPOSE TRY BLOCK)  -------
		try {
			OracleDatabaseMetaData dmd = (OracleDatabaseMetaData)dbcon.getConnection().getMetaData();
			if (dmd == null)
				throw new RDFRDBException("Oracle database metadata not available.");
			TABLE_NAME_LENGTH_MAX =	dmd.getMaxTableNameLength();
			setTableNames(TABLE_NAME_PREFIX);  // need to recheck that table names are not too long
		} catch ( SQLException e ) {
			throw new RDFRDBException("Problem accessing Oracle database metadata.");
		}	  
/*--------------------------------------------------------------------*/		
		try {   		
			// Properties defaultSQL = SQLCache.loadSQLFile(DEFAULT_SQL_FILE, null, ID_SQL_TYPE);
			// m_sql = new SQLCache(SQL_FILE, defaultSQL, dbcon, ID_SQL_TYPE);
			m_sql = new SQLCache(SQL_FILE, null, dbcon, ID_SQL_TYPE);
		} catch (Exception e) {
            e.printStackTrace( System.err );
			logger.error("Unable to set connection for Driver:", e);
		}
	}
	
	/**
	 * Allocate an identifier for a new graph.
	 *
	 */
	public int graphIdAlloc ( String graphName ) {
		DBIDInt result = null;
		int dbid = 0;
		try {
			String op = "insertGraph";
			dbid = getInsertID(GRAPH_TABLE);
			PreparedStatement ps = m_sql.getPreparedSQLStatement(op,GRAPH_TABLE);
			ps.setInt(1,dbid);
			ps.setString(2,graphName);
			ps.executeUpdate();
			m_sql.returnPreparedSQLStatement(ps);
		} catch (SQLException e) {
			throw new RDFRDBException("Failed to get last inserted ID: " + e);
		}
		return dbid;
	}
	
	/**
	 * Dellocate an identifier for a graph.
	 *
	 */
	public void graphIdDealloc ( int graphId ) {
		DBIDInt result = null;
		try {
			String op = "deleteGraph";
			PreparedStatement ps = m_sql.getPreparedSQLStatement(op,GRAPH_TABLE);
			ps.setInt(1,graphId);
			ps.executeUpdate();
			m_sql.returnPreparedSQLStatement(ps);
		} catch (SQLException e) {
			throw new RDFRDBException("Failed to delete graph ID: " + e);
		}
		return;
	}

	public int getInsertID ( String tableName ) {
		DBIDInt result = null;
		try {
			String op = "getInsertID";
			PreparedStatement ps = m_sql.getPreparedSQLStatement(op,tableName);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				result = wrapDBID(rs.getObject(1));
			} else
				throw new RDFRDBException("No insert ID");
			m_sql.returnPreparedSQLStatement(ps);
		} catch (SQLException e) {
			throw new RDFRDBException("Failed to insert ID: " + e);
		}
		return result.getIntID();
	}

	
	/**
	 * Return the parameters for table creation.
	 * 1) column type for subj, prop, obj.
	 * 2) column type for head.
	 * 3) table and index name prefix.
	 * @param param array to hold table creation parameters. 
	 */
	protected void getTblParams ( String [] param ) {
		String objColType;
		
		// length of varchar columns in statement tables
		if ( LONG_OBJECT_LENGTH > 4000 )
			throw new RDFRDBException("Long object length specified (" + LONG_OBJECT_LENGTH +
					") exceeds maximum sane length of 4000.");
		if ( INDEX_KEY_LENGTH > 4000 )
			throw new RDFRDBException("Index key length specified (" + INDEX_KEY_LENGTH +
					") exceeds maximum sane length of 4000.");

		objColType = "VARCHAR2(" + LONG_OBJECT_LENGTH + ")";
		STRINGS_TRIMMED = false;
		param[0] = objColType;
		
		// length of head column in literal tables 
		String headColType = "VARCHAR2(" + INDEX_KEY_LENGTH + ")";
		param[1] = headColType;
		param[2] = TABLE_NAME_PREFIX;
	}
	
	/**
	* 
	* Return the parameters for table creation.
	* Generate the table name by counting the number of existing
	* tables for the graph. This is not reliable if another client
	* is concurrently trying to create a table so, if failure, we
	* make several attempts to create the table.
	*/	

	protected String[] getCreateTableParams( int graphId, boolean isReif ) {
		String [] parms = new String[3];
		String [] res = new String[2];
				
		getTblParams (parms);
		int tblCnt = getTableCount(graphId);
		res[0] = genTableName(graphId,tblCnt,isReif);
		res[1] = parms[0];
		return res;
	}
	
	/**
	 * Return the parameters for database initialization.
	 */
	protected String[] getDbInitTablesParams() {
		String [] res = new String[3];
		
		getTblParams (res);
		EOS_LEN = EOS.length();

		return res;
	}
	
	/**
	 * Insert a long object into the database.  
	 * This assumes the object is not already in the database.
	 * @return the db index of the added literal 
	 */
	public DBIDInt addRDBLongObject(RDBLongObject lobj, String table) throws RDFRDBException {
		DBIDInt longObjID = null;
		try {
			int argi = 1;
			boolean save = m_dbcon.getConnection().getAutoCommit();
			
			String opname = (lobj.tail.length() > 0) ? "insertLongObjectEmptyTail" : "insertLongObject";    			
			PreparedStatement ps = m_sql.getPreparedSQLStatement(opname, table);
			int dbid = 0; // init only needed to satisy java compiler
			if ( PRE_ALLOCATE_ID ) {
				dbid = getInsertID(table);
				ps.setInt(argi++,dbid);
				longObjID = wrapDBID(new Integer(dbid));
			} 
			 ps.setString(argi++, lobj.head);
			 if ( lobj.tail.length() > 0 ) {
				ps.setLong(argi++, lobj.hash);
			 } else {
				ps.setNull(argi++,java.sql.Types.BIGINT);    
			 }
			ps.executeUpdate();
			m_sql.returnPreparedSQLStatement(ps);
			
			if ( lobj.tail.length() > 0) {
				if (! xactOp(xactIsActive)) {
				  m_dbcon.getConnection().setAutoCommit(false);				
				}
				opname = "getEmptyBLOB";
				String cmd = m_sql.getSQLStatement(opname, table, longObjID.getID().toString());
				Statement lobStmt = m_sql.getConnection().createStatement();
				ResultSet lrs = lobStmt.executeQuery(cmd);
				lrs.next();
		
				BLOB blob = ((OracleResultSet) lrs).getBLOB(1);
				OutputStream outstream = blob.getBinaryOutputStream();
				int size = blob.getBufferSize();
	
				int length = -1;
				InputStream instream = new StringBufferInputStream(lobj.tail);
		
				//		Buffer to hold chunks of data to being written to the Blob.        
				byte[] buffer = new byte[size];
		
				while ((length = instream.read(buffer)) != -1)
					outstream.write(buffer,0,length);
		
				if (blob.isOpen())
				blob.close();
				instream.close();
				outstream.close();
				lobStmt.close();
				if (! xactOp(xactIsActive)) {
			  		m_dbcon.getConnection().setAutoCommit(save);				
				}
			}

			if ( !PRE_ALLOCATE_ID ) {
				dbid = getInsertID(table); 
				longObjID = wrapDBID(new Integer(dbid));
			}
		} catch (Exception e1) {
			/* DEBUG */ System.out.println("Problem on long object (l=" + lobj.head + ") " + e1 );
			// System.out.println("ID is: " + id);
			throw new RDFRDBException("Failed to add long object ", e1);
		}
		return longObjID;
	}
	


/**
 * Retrieve LongObject from database.
 */
protected RDBLongObject IDtoLongObject ( int dbid, String table ) {
	RDBLongObject	res = null;
	try {
				String opName = "getLongObject";
				PreparedStatement ps = m_sql.getPreparedSQLStatement(opName, table); 
				ps.setInt(1,dbid);
				OracleResultSet rs = (OracleResultSet) ps.executeQuery();
				if (rs.next()) {
				   res = new RDBLongObject();
				   res.head = rs.getString(1);
				   BLOB blob = rs.getBLOB(2);
					
				   if (blob != null) {
					InputStream blobin = null;
					int len =  (int)blob.length();
					byte[] data = blob.getBytes(1,len);
					res.tail = new String(data, "UTF-8");
				   } else {
				   	res.tail = "";
				   }
				}
				rs.close();
				m_sql.returnPreparedSQLStatement(ps);
			
	} catch (SQLException e1) {
		// /* DEBUG */ System.out.println("Literal truncation (" + l.toString().length() + ") " + l.toString().substring(0, 150));
		throw new RDFRDBException("Failed to retrieve long object (SQL Exception): ", e1);
	} catch (UnsupportedEncodingException e2) {
		throw new RDFRDBException("Failed to retrieve long object (UnsupportedEncoding): ", e2);
	}
	return res;	
}
	
/**
 * Drop all Jena-related sequences from database, if necessary.
 * Override in subclass if sequences must be explicitly deleted.
 */
public void clearSequences() {
	Iterator seqIt = getSequences().iterator();
	while (seqIt.hasNext()) {
		removeSequence((String)seqIt.next());
	}
}
	
public String genSQLStringMatchLHS_IC(String var) {
	return "UPPER(" + var + ")";
}

public String genSQLStringMatchRHS_IC(String strToMatch) {
	return "UPPER(" + strToMatch + ")";
}

public String stringMatchEscapeChar() { return "\\"; }

public String genSQLStringMatchEscape() {
	return " " + genSQLEscapeKW() + " '" + stringMatchEscapeChar() + "'";
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
