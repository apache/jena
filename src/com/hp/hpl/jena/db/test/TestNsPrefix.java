/*
  (c) Copyright 2002, 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestNsPrefix.java,v 1.2 2003-04-30 23:39:59 csayers Exp $
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
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

import junit.framework.*;

import java.util.Map;

public class TestNsPrefix extends TestCase {    
        
    public TestNsPrefix( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestNsPrefix.class ); }           
    
    protected void setUp() throws java.lang.Exception {    	
    }
    
    protected void tearDown() throws java.lang.Exception {
    }    
        
	public void testSinglePrefix() throws java.lang.Exception {
		String testPrefix = "testPrefix";
		String testURI = "http://someTestURI";
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		Map pre = m.getNsPrefixMap();
		assertTrue( pre.size() == 0 ); // brand new model should have no prefixes
		m.setNsPrefix(testPrefix,testURI);
		assertTrue( m.getNsPrefixMap().size() == 1);
		assertTrue( m.getNsPrefixURI(testPrefix).compareTo(testURI) ==0 );		
		m.close();
		conn.close();		
	}

	public void testDuplicatePrefix() throws java.lang.Exception {
		String testPrefix = "testPrefix";
		String testURI = "http://someTestURI";
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		Map pre = m.getNsPrefixMap();
		assertTrue( pre.size() == 0 ); // brand new model should have no prefixes
		m.setNsPrefix(testPrefix,testURI);
		m.setNsPrefix(testPrefix,testURI);
		assertTrue( m.getNsPrefixMap().size() == 1);
		assertTrue( m.getNsPrefixURI(testPrefix).compareTo(testURI) ==0 );		
		m.close();
		conn.close();		
	}

	public void testChangingPrefixMapping() throws java.lang.Exception {
		String testPrefix = "testPrefix";
		String testURI = "http://someTestURI";
		String someOtherTestURI = "http://someOtherTestURI";
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		Map pre = m.getNsPrefixMap();
		assertTrue( pre.size() == 0 ); // brand new model should have no prefixes
		m.setNsPrefix(testPrefix,testURI);
		m.setNsPrefix(testPrefix,someOtherTestURI);
		assertTrue( m.getNsPrefixMap().size() == 1);
		assertTrue( !(m.getNsPrefixURI(testPrefix).compareTo(testURI) ==0 ));		
		assertTrue( m.getNsPrefixURI(testPrefix).compareTo(someOtherTestURI) ==0 );		
		m.close();
		conn.close();		
	}

	public void testPersistenceOfPrefixes() throws java.lang.Exception {
		String testPrefix = "testPrefix";
		String testURI = "http://someTestURI";
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		Map pre = m.getNsPrefixMap();
		assertTrue( pre.size() == 0 ); // brand new model should have no prefixes
		m.setNsPrefix(testPrefix,testURI);
		assertTrue( m.getNsPrefixMap().size() == 1);
		assertTrue( m.getNsPrefixURI(testPrefix).compareTo(testURI) ==0 );		
		m.close();
		
		// Now create a different model and check there is no prefix
		ModelRDB m2 = ModelRDB.createModel(conn,"myName");
		assertTrue( m2.getNsPrefixMap().size() == 0);
		m2.close();
		
		// Now reopen the first Model and check the prefix was persisted
		ModelRDB m3 = ModelRDB.open(conn);
		assertTrue( m3.getNsPrefixMap().size() == 1);
		assertTrue( m3.getNsPrefixURI(testPrefix).compareTo(testURI) ==0 );		
		m3.close();
		
		conn.close();		
	}
	
	public void testPersistedPrefixesMatchMemory() throws java.lang.Exception {
		String testPrefix = "testPrefix";
		String testURI = "http://someTestURI";
		String someOtherTestURI = "http://someOtherTestURI";
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		Map pre = m.getNsPrefixMap();
		assertTrue( pre.size() == 0 ); // brand new model should have no prefixes
		m.setNsPrefix(testPrefix,testURI);
		m.setNsPrefix(testPrefix,testURI);
		m.setNsPrefix(testPrefix,someOtherTestURI);
		assertTrue( m.getNsPrefixMap().size() == 1);
		assertTrue( m.getNsPrefixURI(testPrefix).compareTo(someOtherTestURI) ==0 );		
		m.close();
		
		// Now reopen the first Model and check the prefix was persisted
		ModelRDB m3 = ModelRDB.open(conn);
		assertTrue( m3.getNsPrefixMap().size() == 1);
		assertTrue( m3.getNsPrefixURI(testPrefix).compareTo(someOtherTestURI) ==0 );		
		m3.close();
		
		conn.close();		
	}

	public void testCopyPersistentPrefixMapping() throws java.lang.Exception {
		String testPrefix1 = "testPrefix1";
		String testPrefix2 = "testPrefix2";
		String testURI1 = "http://someTestURI/1";
		String testURI2 = "http://someTestURI/2";
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		Map pre = m.getNsPrefixMap();
		assertTrue( pre.size() == 0 ); // brand new model should have no prefixes
		m.setNsPrefix(testPrefix1,testURI1);
		m.setNsPrefix(testPrefix2,testURI2);
		assertTrue( m.getNsPrefixMap().size() == 2);
		assertTrue( m.getNsPrefixURI(testPrefix1).compareTo(testURI1) ==0 );		
		assertTrue( m.getNsPrefixURI(testPrefix2).compareTo(testURI2) ==0 );		
		
		// Now create a second model, copy the prefix mapping and make sure it matches
		ModelRDB m2 = ModelRDB.createModel(conn, "secondModel");
		assertTrue( m2.getNsPrefixMap().size() == 0);
		m2.setNsPrefixes(m.getNsPrefixMap());
		assertTrue( m2.getNsPrefixMap().size() == 2);
		assertTrue( m2.getNsPrefixURI(testPrefix1).compareTo(testURI1) ==0 );		
		assertTrue( m2.getNsPrefixURI(testPrefix2).compareTo(testURI2) ==0 );			

		m.close();
		m2.close();
		
		conn.close();		
	}


	public void testCopyMemoryPrefixMapping() throws java.lang.Exception {
		String testPrefix1 = "testPrefix1";
		String testPrefix2 = "testPrefix2";
		String testURI1 = "http://someTestURI/1";
		String testURI2 = "http://someTestURI/2";
		
		PrefixMapping myMap = new PrefixMappingImpl();
		myMap.setNsPrefix(testPrefix1, testURI1);
		myMap.setNsPrefix(testPrefix2, testURI2);

		assertTrue( myMap.getNsPrefixMap().size() == 2);
		assertTrue( myMap.getNsPrefixURI(testPrefix1).compareTo(testURI1) ==0 );		
		assertTrue( myMap.getNsPrefixURI(testPrefix2).compareTo(testURI2) ==0 );		
		
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		assertTrue( m.getNsPrefixMap().size() == 0 ); // brand new model should have no prefixes
		m.setNsPrefixes(myMap);
		assertTrue( m.getNsPrefixMap().size() == 2);
		assertTrue( m.getNsPrefixURI(testPrefix1).compareTo(testURI1) ==0 );		
		assertTrue( m.getNsPrefixURI(testPrefix2).compareTo(testURI2) ==0 );
		
		m.close();
		conn.close();		
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
