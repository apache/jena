/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Driver_Oracle.java,v 1.4 2003-05-13 16:44:30 hkuno Exp $
*/

package com.hp.hpl.jena.db.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.util.Log;


/**
 * @author hkuno based on code by Dave Reynolds
 *
 * Extends DriverRDB with Oracle-specific parameters.
 */
public class Driver_Oracle extends DriverRDB {
	
	public int MAX_DB_IDENTIFIER_LENGTH = 30;
	public int m_tablecounter;
	
	/** 
	 * Constructor
	 */
	public Driver_Oracle() {
		
		super();
		
		//	Oracle not supported at this time
	    if (true) {
	    	throw(new RDFRDBException("Oracle is not yet supported for Jena."));
	    }
		

		String myPackageName = this.getClass().getPackage().getName();
		
		DATABASE_TYPE = "Oracle";
		DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";
		
		EMPTY_LITERAL_MARKER = "EmptyLiteral";
		ID_SQL_TYPE = "INTEGER";
		INSERT_BY_PROCEDURE = false;
		MAX_LITERAL = 4000;
		SKIP_ALLOCATE_ID = false;
		SKIP_DUPLICATE_CHECK = true;
		EMPTY_LITERAL_MARKER = "EmptyLiteral";
		SQL_FILE = "etc/oracle.sql";
		
		m_psetClassName = myPackageName + ".PSet_TripleStore_RDB";
		m_psetReifierClassName = myPackageName + ".PSet_ReifStore_RDB";
		
		m_lsetClassName = myPackageName + ".SpecializedGraph_TripleStore_RDB";						
		m_lsetReifierClassName = myPackageName + ".SpecializedGraphReifier_RDB";	
		
		m_tablecounter = 1;											
	}
	
	/**
	 * Set the database connection
	 */
	public void setConnection( IDBConnection dbcon ) {
		m_dbcon = dbcon;
		
		try {   		
			Properties defaultSQL = SQLCache.loadSQLFile(DEFAULT_SQL_FILE, null, ID_SQL_TYPE);
			m_sql = new SQLCache(SQL_FILE, defaultSQL, dbcon, ID_SQL_TYPE);
		} catch (Exception e) {
            e.printStackTrace( System.err );
			Log.severe("Unable to set connection for Driver:" + e);
		}
	}
	
	/**
	 * Convert a string into a form suitable for a legal identifier
	 * name for the database type.
	 * @author hkuno
	 *
	 */
	public String toDBIdentifier(String aString) {
		String newString = super.toDBIdentifier(aString);
		if (m_tablecounter > 999) {
			Log.severe("Driver_Oracle m_tablecounter exceeded 999");
		}
		
		// 11 characters for index name, 3 chars for counter
		if (newString.length() > MAX_DB_IDENTIFIER_LENGTH) 
			newString = newString.substring(0,MAX_DB_IDENTIFIER_LENGTH - 14) + m_tablecounter++;
		return newString;
	}

	/**
	 * If the underlying database connection supports transactions,
	 * call commit(), then turn autocommit on.
	 */
	public void commit() throws RDFRDBException{
		if (transactionsSupported()) {
			try {
				  if (inTransaction) {
					Connection c = m_sql.getConnection();
					c.commit();
					c.setAutoCommit(true);
					c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
					inTransaction =  false;
				   }
				} catch (SQLException e) {
						throw new RDFRDBException("Transaction support failed: ", e);
				}
		} 
	}
        





}

/*
 *  (c) Copyright Hewlett-Packard Company 2003.
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
