/*
 *  (c) Copyright Hewlett-Packard Company 2003
 *  All rights reserved.
 *
 *
 */

package com.hp.hpl.jena.db.impl;

import java.util.Properties;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.util.Log;


/**
 * @author hkuno based on code by Dave Reynolds
 *
 * Extends DriverRDB with MySQL-specific parameters.
 */
public class Driver_MySQL extends DriverRDB {
	
	/** 
	 * Constructor
	 */
	public Driver_MySQL(){
		super();

		String myPackageName = this.getClass().getPackage().getName();
		
		DATABASE_TYPE = "MySQL";
		DRIVER_NAME = "com.mysql.jdbc.Driver";
		
		EMPTY_LITERAL_MARKER = "EmptyLiteral";
		ID_SQL_TYPE = "INTEGER";
		INSERT_BY_PROCEDURE = false;
		MAX_LITERAL = 250;
		SKIP_ALLOCATE_ID = true;
		SKIP_DUPLICATE_CHECK = false;
		EMPTY_LITERAL_MARKER = "EmptyLiteral";
		SQL_FILE = "etc/mysql.sql";
		
		m_psetClassName = myPackageName + ".PSet_TripleStore_RDB";
		
		m_lsetClassName = myPackageName + ".SpecializedGraph_TripleStore_RDB";						
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
