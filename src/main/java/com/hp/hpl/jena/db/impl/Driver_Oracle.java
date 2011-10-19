/*
 *  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 *
 */

package com.hp.hpl.jena.db.impl;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.RDFRDBException;

/**
 * @author hkuno based on code by Dave Reynolds. 
 * Modified for later, more standard, Oracle drivers by Andy Seaborne (Dec 2006)
 * 
 *  See alo Driver_Oracle_LOB for code that uses native
 *  Oracle blobs more directly.  That needs uncommenting
 *  some code sections and recompiling against Oracle
 *  Java libraries.   
 */

public class Driver_Oracle extends DriverRDB {

	public Driver_Oracle( ){
		super();

		String myPackageName = this.getClass().getPackage().getName();
		
		DATABASE_TYPE = "Oracle";
        // Must aline to driver.  But the EngineType is set from the driver.  
        // See also PSet_TripleStore_RDB.storeTripleAR which tests specifically for "Oracle".
        
		DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";
		
		ID_SQL_TYPE = "INTEGER";
		URI_COMPRESS = false;
//		LONG_OBJECT_LENGTH_MAX = INDEX_KEY_LENGTH_MAX = INDEX_KEY_LENGTH = 2000;
//		LONG_OBJECT_LENGTH = 250;
        
      LONG_OBJECT_LENGTH_MAX = INDEX_KEY_LENGTH_MAX = INDEX_KEY_LENGTH = 250;
      LONG_OBJECT_LENGTH = 250;
        
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
	@Override
    public void setConnection( IDBConnection dbcon ) {
		m_dbcon = dbcon;
        
        
		try {
			DatabaseMetaData dmd = dbcon.getConnection().getMetaData();
			if (dmd == null)
				throw new RDFRDBException("Oracle database metadata not available.");
			TABLE_NAME_LENGTH_MAX =	dmd.getMaxTableNameLength();
			setTableNames(TABLE_NAME_PREFIX);  // need to recheck that table names are not too long
		} catch ( SQLException e ) {
			throw new RDFRDBException("Problem accessing Oracle database metadata.");
		}	  

        try {   		
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
	@Override
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
	@Override
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

	// Now common code moved to DriverRDB - delete this anytime after Jena 2.5.2
// 	public int getInsertID ( String tableName ) {
//		DBIDInt result = null;
//		try {
//			String op = "getInsertID";
//			PreparedStatement ps = m_sql.getPreparedSQLStatement(op,tableName);
//			ResultSet rs = ps.executeQuery();
//			if (rs.next()) {
//				result = wrapDBID(rs.getObject(1));
//			} else
//				throw new RDFRDBException("No insert ID");
//			m_sql.returnPreparedSQLStatement(ps);
//		} catch (SQLException e) {
//			throw new RDFRDBException("Failed to insert ID: " + e);
//		}
//		return result.getIntID();
//	}

	
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

		objColType = "NVARCHAR2(" + LONG_OBJECT_LENGTH + ")";
		STRINGS_TRIMMED = false;
		param[0] = objColType;
		
		// length of head column in literal tables 
		String headColType = "NVARCHAR2(" + INDEX_KEY_LENGTH + ")";
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

	@Override
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
	@Override
    protected String[] getDbInitTablesParams() {
		String [] res = new String[3];
		
		getTblParams (res);
		EOS_LEN = EOS.length();

		return res;
	}
	
//	/**
//	 * Insert a long object into the database.  
//	 * This assumes the object is not already in the database.
//	 * @return the db index of the added literal 
//	 */

//    // Try to use standard blob handling
//    
//	public DBIDInt addRDBLongObject(RDBLongObject lobj, String table) throws RDFRDBException {
//        // Assumes tail is in the prepared statement
//	    return super.addRDBLongObject(lobj, table) ;
//	}

//	/**
//	 * Retrieve LongObject from database.
//	 */
//	protected RDBLongObject IDtoLongObject ( int dbid, String table ) {
//	    return super.IDtoLongObject(dbid, table) ;
//	}

    @Override
    protected void setLongObjectHashAndTail(PreparedStatement ps, int argi, RDBLongObject lobj)
    throws SQLException
    {
        int paramCount = ps.getParameterMetaData().getParameterCount() ;
        // In Jena 2.4 and before, using Oracle specific BLOB code, this is 3.
        // In Jena 2.5 it is 4 because of a change to the SQL statement in etc/oracle.sql
        // Check this here.
        if ( paramCount != 4)
        {
            logger.warn("Warning: Driver_Oracle: Mismatch in prepared statement paramter count: Expected "+4+" : Got: "+paramCount) ;   
            logger.warn("Maybe running with an old (pre Jena2.5) etc/oracle.sql file?") ;
        }
        
        super.setLongObjectHashAndTail_Binary(ps, argi, lobj) ;
    }
    
	/**
	 * Drop all Jena-related sequences from database, if necessary.
	 * Override in subclass if sequences must be explicitly deleted.
	 */
	@Override
    public void clearSequences() {
	    Iterator<String> seqIt = getSequences().iterator();
	    while (seqIt.hasNext()) {
	        removeSequence(seqIt.next());
	    }
	}

    /** Oracle implementation of getConnection().getMetaData() can see all tables if run sufficiently priviledge.
     * 
     */
	@Override
    protected List<String> getAllTables() {
	    try {
//	        DatabaseMetaData dbmd = m_dbcon.getConnection().getMetaData();
//	        String[] tableTypes = { "TABLE" };
//	        String prefixMatch = stringToDBname(TABLE_NAME_PREFIX + "%");
//	        ResultSet rs = dbmd.getTables(null, dbmd.getUserName(), prefixMatch, tableTypes);

            // This way (Oracle specific) see only the tables we own and access.
            String sql = "SELECT TNAME AS TABLE_NAME FROM tab WHERE TNAME LIKE '"+TABLE_NAME_PREFIX+"%'" ;
            ResultSet rs = m_dbcon.getConnection().createStatement().executeQuery(sql) ;

	        List<String> tables = new ArrayList<String>() ;
	        while(rs.next())
	            tables.add(rs.getString("TABLE_NAME"));
	        rs.close() ;
	        return tables ;
	    } catch (SQLException e1) {
	        throw new RDFRDBException("Internal SQL error in driver - " + e1);
	    }
	}


	@Override
    public String genSQLStringMatchLHS_IC(String var) {
	    return "UPPER(" + var + ")";
	}

	@Override
    public String genSQLStringMatchRHS_IC(String strToMatch) {
	    return "UPPER(" + strToMatch + ")";
	}

	@Override
    public String stringMatchEscapeChar() { return "\\"; }

	@Override
    public String genSQLStringMatchEscape() {
	    return " " + genSQLEscapeKW() + " '" + stringMatchEscapeChar() + "'";
	}
}

/*
 *  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
