/*
  (c) Copyright 2002, 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestConnection.java,v 1.3 2003-05-04 17:36:13 hkuno Exp $
*/

package com.hp.hpl.jena.db.test;

/**
 * 
 * This tests basic open/create operations on the modelRDB.
 * 
 * To run, you must have a mySQL database operational on
 * localhost with a database name of "test" and allow use
 * by a user named "test" with an empty password.
 * 
 * (based in part on model tests written earlier by bwm and kers)
 * 
 * @author csayers
 * @version 0.1
*/

import com.hp.hpl.jena.db.*;

import junit.framework.*;

import com.hp.hpl.jena.util.*;

public class TestConnection extends TestCase {    
        
    public TestConnection( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestConnection.class ); }           
    
    protected void setUp() throws java.lang.Exception {    	
    }
    
    protected void tearDown() throws java.lang.Exception {
    }
    
	public void testNoClass() throws java.lang.Exception {
		/*
		IDBConnection conn = new DBConnection(TestPackage.M_DB_URL, TestPackage.MYSQL_USER, TestPackage.MYSQL_PASSWD);
		conn.cleanDB();
		ModelRDB m = ModelRDB.createModel(conn);
		m.remove();
		conn.close();
		*/
	}
        
	public void testNoConnection() throws java.lang.Exception {
		/*
		Class.forName(TestPackage.M_DBDRIVER_CLASS);
		IDBConnection conn = new DBConnection("Bad_URI", TestPackage.MYSQL_USER, TestPackage.MYSQL_PASSWD);
		ModelRDB m = ModelRDB.open(conn);
		m.remove();
		conn.close();
		*/
	}
    
    private static void loadClass()
        {
        try { Class.forName(TestPackage.M_DBDRIVER_CLASS); }
        catch (Exception e) { throw new JenaException( e ); }
        }
        
    public static IDBConnection makeTestConnection() 
        {
        loadClass();
        return new DBConnection
            (
            TestPackage.M_DB_URL, 
            TestPackage.M_DB_USER, 
            TestPackage.M_DB_PASSWD, 
            TestPackage.M_DB
            );
        }
        
    public static IDBConnection makeAndCleanTestConnection()
        {
        IDBConnection result = makeTestConnection();
        try { result.cleanDB(); }
        catch (Exception e) { throw new JenaException( e ); }        
        return result;
        }
        
    public void testDBConnect() throws java.lang.Exception {
		IDBConnection conn = makeTestConnection();
    	conn.close();
    }
    
    public void testConstructDefaultModel() throws java.lang.Exception {
		IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		m.remove();
    	conn.close();
    }
    
    public void testConstructAndOpenDefaultModel() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		m.close();
		ModelRDB m2 = ModelRDB.open(conn);
		m2.remove();
		conn.close();
    }
        
    public void testConstructNamedModel() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn, "myName");
		m.remove();
    	conn.close();
    }
        
    public void testConstructAndOpenNamedModel() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn, "myName");
		m.close();
		ModelRDB m2 = ModelRDB.open(conn, "myName");
		m2.remove();
    	conn.close();
    }
        
    public void testConstructParamaterizedModel() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn, ModelRDB.getDefaultModelProperties(conn));
		m.remove();
    	conn.close();
    }
        
    public void testConstructAndOpenParamaterizedModel() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn, ModelRDB.getDefaultModelProperties(conn));
		m.close();
		ModelRDB m2 = ModelRDB.open(conn);
		m2.remove();
    	conn.close();
    }
        
	public void testConstructNamedParamaterizedModel() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn, "myName", ModelRDB.getDefaultModelProperties(conn));
		m.remove();
    	conn.close();
    }
        
	public void testConstructAndOpenNamedParamaterizedModel() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn, "myName", ModelRDB.getDefaultModelProperties(conn));
		m.close();
		ModelRDB m2 = ModelRDB.open(conn, "myName");
		m2.remove();
    	conn.close();
    }
    
	public void testOpenNamedNonExistentModel() throws java.lang.Exception {
        IDBConnection conn = makeTestConnection();
		try {
			ModelRDB m2 = ModelRDB.open(conn, "myName");
			m2.remove();
			conn.close();
			assertTrue("Successfully opened non-existent model", false);
		} catch ( RDFRDBException e ) {
			conn.close();
		}   
	}

	public void testOpenUnnamedNonExistentModel() throws java.lang.Exception {
        IDBConnection conn = makeTestConnection();
		try {
			conn.cleanDB();
			ModelRDB m2 = ModelRDB.open(conn);
			m2.remove();
			conn.close();
			assertTrue("Successfully opened unnamed non-existent model", false);
		} catch ( RDFRDBException e ) {
			conn.close();
		}   
	}

	public void testCreateExistingModel() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn, "myName", ModelRDB.getDefaultModelProperties(conn));
		try {
			ModelRDB m2 = ModelRDB.createModel(conn, "myName", ModelRDB.getDefaultModelProperties(conn));
			m.remove(); m2.remove();
			conn.close();
			assertTrue("Successfully created pre-existing model", false);
		} catch ( RDFRDBException e ) {
			m.remove();
			conn.close();
		}
	}
}
    	

/*
    (c) Copyright Hewlett-Packard Company 2002, 2003
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
