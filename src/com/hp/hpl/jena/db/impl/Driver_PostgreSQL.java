/*
 *  (c) Copyright 2003 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 *
 */

package com.hp.hpl.jena.db.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.RDFRDBException;


/**
 * @author hkuno based on code by Dave Reynolds
 *
 * Extends DriverRDB with PostgreSQL-specific parameters.
 */
public class Driver_PostgreSQL extends DriverRDB {
	
	/** The name of the database type this driver supports */
	
	/** 
	 * Constructor
	 */
	public Driver_PostgreSQL( ){
		super();

		String myPackageName = this.getClass().getPackage().getName();
		
		DATABASE_TYPE = "PostgreSQL";
		DRIVER_NAME = "org.postgresql.Driver";
		
		ID_SQL_TYPE = "INTEGER";
		URI_COMPRESS = false;
		INDEX_KEY_LENGTH_MAX = INDEX_KEY_LENGTH = 250;
		LONG_OBJECT_LENGTH_MAX = LONG_OBJECT_LENGTH = 250;
		TABLE_NAME_LENGTH_MAX = 63;
		IS_XACT_DB = true;
		PRE_ALLOCATE_ID = true;
		SKIP_DUPLICATE_CHECK = false;
		SQL_FILE = "etc/postgresql.sql";
		QUOTE_CHAR = '\'';
		DB_NAMES_TO_UPPER = false;
		setTableNames(TABLE_NAME_PREFIX);
		
		m_psetClassName = myPackageName + ".PSet_TripleStore_RDB";
		m_psetReifierClassName = myPackageName + ".PSet_ReifStore_RDB";
		
		m_lsetClassName = myPackageName + ".SpecializedGraph_TripleStore_RDB";						
		m_lsetReifierClassName = myPackageName + ".SpecializedGraphReifier_RDB";
	}
	
	/**
	 * Set the database connection
	 */
	public void setConnection( IDBConnection dbcon ) {
		m_dbcon = dbcon;
		
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
			dbid = getInsertID(GRAPH_TABLE);
			PreparedStatement ps = m_sql.getPreparedSQLStatement("insertGraph",GRAPH_TABLE);
			ps.setInt(1,dbid);
			ps.setString(2,graphName);
			ps.executeUpdate();
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
			PreparedStatement ps = m_sql.getPreparedSQLStatement("deleteGraph",GRAPH_TABLE);
			ps.setInt(1,graphId);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RDFRDBException("Failed to delete graph ID: " + e);
		}
		return;
	}

	public int getInsertID ( String tableName ) {
		DBIDInt result = null;
		try {
			PreparedStatement ps = m_sql.getPreparedSQLStatement("getInsertID",tableName);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				result = wrapDBID(rs.getObject(1));
			} else
				throw new RDFRDBException("No insert ID");
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
		String spoColType;
		String headColType;
		
		if ( LONG_OBJECT_LENGTH > 4000 )
			throw new RDFRDBException("Long object length specified (" + LONG_OBJECT_LENGTH +
					") exceeds maximum sane length of 4000.");
		if ( INDEX_KEY_LENGTH > 4000 )
			throw new RDFRDBException("Index key length specified (" + INDEX_KEY_LENGTH +
					") exceeds maximum sane length of 4000.");

		spoColType = "VARCHAR(" + LONG_OBJECT_LENGTH + ")";
		STRINGS_TRIMMED = false;
		param[0] = spoColType;
		headColType = "VARCHAR(" + INDEX_KEY_LENGTH + ")";
		STRINGS_TRIMMED = false;
		param[1] = headColType;
		param[2] = TABLE_NAME_PREFIX;
	}

	
	
	/**
	 * 
	 * Return the parameters for database initialization.
	 */
	protected String[] getDbInitTablesParams() {
		String [] res = new String[3];
		
		getTblParams (res);
		EOS_LEN = EOS.length();

		return res;
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
	
	public String genSQLStringMatchOp_IC( String fun ) {
		return "I" + genSQLLikeKW();
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
