/*
 * Created on May 5, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
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
	public Blob getBlob() {
		// TODO Auto-generated method stub
		return ((java.sql.Blob) m_blob);
	}

}
