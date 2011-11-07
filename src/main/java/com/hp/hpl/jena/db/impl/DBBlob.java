/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.db.impl;

import java.sql.Blob;

import com.hp.hpl.jena.db.RDFRDBException;

/**
 * @author hkuno
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class DBBlob implements IDBBlob {

    // NOT CURRENTLY USED (Jena 2.4)
    
	/** the real blob */
	protected Object m_blob;
	
	
	/** the target database type */
	protected String m_dbtype;

	/** constructor */
	public DBBlob(Object ablob, String dbType) {
			m_dbtype = dbType;
			m_blob = ablob;
			
	}
	
	public  DBBlob() {
	}
	
	/**
     TODO is this obsolete? It doesn't look useful and it's never called.
	 * Creates and returns instance of appropriate subclass of DBBlob.
	 * @param ablob
	 * @param dbType
	 * @return null ?
	 */
	public static IDBBlob getDBBlob(Object ablob, String dbType) {
		IDBBlob result = null;
		if (dbType.equalsIgnoreCase("oracle")) {
			// result = new DBBlob_Oracle(ablob, dbType);
		} else {
			throw new RDFRDBException("No appropriate blob type found for " + dbType);
		}
		return (result);
		
	
	}
	

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.impl.IDBBlob#asOracleBlob()
	 */
	@Override
    public Blob getBlob() {
		// TODO Auto-generated method stub
		return ((java.sql.Blob) m_blob);
	}

}
