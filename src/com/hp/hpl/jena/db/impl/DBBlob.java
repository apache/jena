/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: DBBlob.java,v 1.3 2003-06-19 10:48:40 chris-dollin Exp $
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
/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
