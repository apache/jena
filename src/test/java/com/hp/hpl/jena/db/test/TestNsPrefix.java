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
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import junit.framework.*;

public class TestNsPrefix extends ModelTestBase {    
        
    public TestNsPrefix( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestNsPrefix.class ); }           
    
    @Override
    protected void setUp() throws java.lang.Exception {    	
    }
    
    @Override
    protected void tearDown() throws java.lang.Exception {
    }    
        
    protected PrefixMapping getMapping() 
        {
        IDBConnection conn = TestConnection.makeAndCleanTestConnection();
        return ModelRDB.createModel( conn );
        }
    
	public void testSinglePrefix() throws java.lang.Exception {
		String testPrefix = "testPrefix";
		String testURI = "http://someTestURI#";
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		assertEquals( 0, m.getNsPrefixMap().size() ); // brand new model should have no prefixes
		m.setNsPrefix(testPrefix,testURI);
		assertEquals( 1, m.getNsPrefixMap().size() );
		assertEquals( testURI, m.getNsPrefixURI( testPrefix ) );		
		m.close();
		conn.close();		
	}

	public void testDuplicatePrefix() throws java.lang.Exception {
		String testPrefix = "testPrefix";
		String testURI = "http://someTestURI#";
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		assertEquals( 0, m.getNsPrefixMap().size() ); // brand new model should have no prefixes
		m.setNsPrefix( testPrefix,testURI );
		m.setNsPrefix( testPrefix,testURI );
		assertEquals( 1, m.getNsPrefixMap().size() );
		assertEquals( testURI, m.getNsPrefixURI( testPrefix ) );		
		m.close();
		conn.close();		
	}

	public void testChangingPrefixMapping() throws java.lang.Exception {
		String testPrefix = "testPrefix";
		String testURI = "http://someTestURI#";
		String someOtherTestURI = "http://someOtherTestURI#";
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		assertEquals( 0, m.getNsPrefixMap().size() ); // brand new model should have no prefixes
		m.setNsPrefix(testPrefix,testURI);
		m.setNsPrefix(testPrefix,someOtherTestURI);
		assertEquals( 1, m.getNsPrefixMap().size() );
		assertDiffer( testURI, m.getNsPrefixURI( testPrefix ) );		
		assertEquals( someOtherTestURI, m.getNsPrefixURI( testPrefix ) );		
		m.close();
		conn.close();		
	}

	public void testPersistenceOfPrefixes() throws java.lang.Exception {
		String testPrefix = "testPrefix";
		String testURI = "http://someTestURI#";
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		assertEquals( 0, m.getNsPrefixMap().size() ); // brand new model should have no prefixes
        m.setNsPrefix(testPrefix,testURI);
		assertEquals( 1, m.getNsPrefixMap().size() );
		assertEquals( testURI, m.getNsPrefixURI( testPrefix ) );		
		m.close();
		
		// Now create a different model and check there is no prefix
		// and that removing it has no effect on the first model.
		ModelRDB m2 = ModelRDB.createModel(conn,"myName");
		assertEquals( 0, m2.getNsPrefixMap().size() );
		m2.remove();
		m2.close();
		
		// Now reopen the first Model and check the prefix was persisted
		ModelRDB m3 = ModelRDB.open(conn);
		assertEquals( 1, m3.getNsPrefixMap().size() );
		assertEquals( testURI, m3.getNsPrefixURI( testPrefix ) );		
		m3.close();
		
		conn.close();		
	}
	
	public void testIdependenceOfPrefixes() throws java.lang.Exception {
		String testPrefix1 = "testPrefix1";
		String testURI1 = "http://someTestURI1#";
		String testPrefix2 = "testPrefix2";
		String testURI2 = "http://someTestURI2#";
		String testPrefix3 = "testPrefix3";
		String testURI3 = "http://someTestURI3#";
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		
		// Create a first model with a set of prefixes
		ModelRDB m = ModelRDB.createModel(conn);
		assertEquals( 0, m.getNsPrefixMap().size() ); // brand new model should have no prefixes
		m.setNsPrefix(testPrefix1,testURI1);
		m.setNsPrefix(testPrefix2,testURI2);
		assertEquals( 2, m.getNsPrefixMap().size() );
		assertEquals( testURI1, m.getNsPrefixURI( testPrefix1 ) );		
		assertEquals( testURI2, m.getNsPrefixURI( testPrefix2 ) );		
		
		// Create a second model with an overlapping set of prefixes
		ModelRDB m2 = ModelRDB.createModel(conn,"secondGraph");
		assertEquals( 0, m2.getNsPrefixMap().size() ); // brand new model should have no prefixes
		m2.setNsPrefix(testPrefix2,testURI2);
		m2.setNsPrefix(testPrefix3,testURI3);
		
		// Verify second model has the correct contents
		assertEquals( 2, m2.getNsPrefixMap().size() );
		assertEquals( testURI2, m2.getNsPrefixURI( testPrefix2 ) );		
		assertEquals( testURI3, m2.getNsPrefixURI( testPrefix3 ) );		
		
		// Verify that first model was unchanged.
		assertEquals( 2, m.getNsPrefixMap().size() );
		assertEquals( testURI1, m.getNsPrefixURI( testPrefix1 ) );		
		assertEquals( testURI2, m.getNsPrefixURI( testPrefix2 ) );		
		
		// Now remove the second model
		m2.remove();
		m2.close();
		m2 = null;
		
		// Verify that first model was still unchanged.
		assertEquals( 2, m.getNsPrefixMap().size() );
		assertEquals( testURI1, m.getNsPrefixURI( testPrefix1 ) );		
		assertEquals( testURI2, m.getNsPrefixURI( testPrefix2 ) );		
		
		// Now close and reopen the first Model and check the prefix was persisted
		m.close();
		m = null;
		
		ModelRDB m3 = ModelRDB.open(conn);
		assertEquals( 2, m3.getNsPrefixMap().size() );
		assertEquals( testURI1, m3.getNsPrefixURI( testPrefix1 ) );		
		assertEquals( testURI2, m3.getNsPrefixURI( testPrefix2 ) );		
		m3.close();
		
		conn.close();		
	}
	
	public void testPersistedChangedPrefixes() throws java.lang.Exception {
		String testPrefix1 = "testPrefix1";
		String testURI1 = "http://someTestURI/1#";
		String testURI1b = "http://someTestURI/1b#";
		String testPrefix2 = "testPrefix2";
		String testURI2 = "http://someTestURI/2#";
		String testPrefix3 = "testPrefix3";
		String testURI3 = "http://someTestURI/3#";
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		
		ModelRDB m = ModelRDB.createModel(conn);
		assertEquals( 0, m.getNsPrefixMap().size() ); // brand new model should have no prefixes
		m.setNsPrefix(testPrefix1,testURI1);
		m.setNsPrefix(testPrefix2,testURI2);
		assertEquals( 2, m.getNsPrefixMap().size() );
		assertEquals( testURI1, m.getNsPrefixURI( testPrefix1 ) );		
		assertEquals( testURI2, m.getNsPrefixURI( testPrefix2 ) );		
		m.close();
		m=null;
		
		// Now reopen the first Model and check the prefixes were persisted
		ModelRDB m2 = ModelRDB.open(conn);
		assertEquals( 2, m2.getNsPrefixMap().size() );
		assertEquals( testURI1, m2.getNsPrefixURI(testPrefix1) );		
		assertEquals( testURI2, m2.getNsPrefixURI(testPrefix2) );		
		
		// Now change one prefix and add a third
		m2.setNsPrefix(testPrefix1,testURI1b);
		m2.setNsPrefix(testPrefix3, testURI3);				

		assertEquals( 3, m2.getNsPrefixMap().size() );
		assertEquals( testURI1b, m2.getNsPrefixURI(testPrefix1) );		
		assertEquals( testURI2, m2.getNsPrefixURI(testPrefix2) );		
		assertEquals( testURI3, m2.getNsPrefixURI(testPrefix3) );		

		m2.close();
		m2 = null;
		
		// Now reopen again and check it's all still as expected.
		ModelRDB m3 = ModelRDB.open(conn);
		assertEquals( 3, m3.getNsPrefixMap().size() );
		assertEquals( testURI1b, m3.getNsPrefixURI(testPrefix1) );		
		assertEquals( testURI2, m3.getNsPrefixURI(testPrefix2) );		
		assertEquals( testURI3, m3.getNsPrefixURI(testPrefix3) );		
		
		m3.close();
		conn.close();		
	}

	public void testCopyPersistentPrefixMapping() throws java.lang.Exception {
		String testPrefix1 = "testPrefix1";
		String testPrefix2 = "testPrefix2";
		String testURI1 = "http://someTestURI/1#";
		String testURI2 = "http://someTestURI/2#";
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		assertEquals( 0, m.getNsPrefixMap().size()  ); // brand new model should have no prefixes
		m.setNsPrefix(testPrefix1,testURI1);
		m.setNsPrefix(testPrefix2,testURI2);
		assertEquals( 2, m.getNsPrefixMap().size() );
		assertEquals( testURI1, m.getNsPrefixURI(testPrefix1) );		
		assertEquals( testURI2, m.getNsPrefixURI(testPrefix2) );		
		
		// Now create a second model, copy the prefix mapping and make sure it matches
		ModelRDB m2 = ModelRDB.createModel(conn, "secondModel");
		assertEquals( 0, m2.getNsPrefixMap().size() );
		m2.setNsPrefixes(m.getNsPrefixMap());
		assertEquals( 2, m2.getNsPrefixMap().size() );
		assertEquals( testURI1, m2.getNsPrefixURI(testPrefix1) );		
		assertEquals( testURI2, m2.getNsPrefixURI(testPrefix2) );			

		m.close();
		m2.close();
		
		conn.close();		
	}


	public void testCopyMemoryPrefixMapping() throws java.lang.Exception {
		String testPrefix1 = "testPrefix1";
		String testPrefix2 = "testPrefix2";
		String testURI1 = "http://someTestURI/1#";
		String testURI2 = "http://someTestURI/2#";
		
		PrefixMapping myMap = new PrefixMappingImpl();
		myMap.setNsPrefix(testPrefix1, testURI1);
		myMap.setNsPrefix(testPrefix2, testURI2);

		assertEquals( 2, myMap.getNsPrefixMap().size() );
		assertEquals( testURI1, myMap.getNsPrefixURI(testPrefix1) );		
		assertEquals( testURI2, myMap.getNsPrefixURI(testPrefix2) );		
		
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		assertEquals( 0, m.getNsPrefixMap().size()  ); // brand new model should have no prefixes
		m.setNsPrefixes(myMap);
		assertEquals( 2, m.getNsPrefixMap().size() );
		assertEquals( testURI1, m.getNsPrefixURI(testPrefix1) );		
		assertEquals( testURI2, m.getNsPrefixURI(testPrefix2) );
		
		m.close();
		conn.close();		
	}
    
    public void testSecondPrefixDoesNotRemoveSharedURI() throws Exception
        {
        IDBConnection conn = TestConnection.makeAndCleanTestConnection();
        ModelRDB m = ModelRDB.createModel( conn );
        String someURI = "urn:x-hp:db-unit-testing:xxx";
        m.setNsPrefix( "p1", someURI );
        m.setNsPrefix( "p2", someURI );
        m.close();
        conn.close();
        IDBConnection conn2 = TestConnection.makeTestConnection();
        ModelRDB m2 = ModelRDB.open( conn2 );
        assertEquals( someURI, m2.getNsPrefixURI( "p1" ) );
        assertEquals( someURI, m2.getNsPrefixURI( "p2" ) );
        m2.close();
        conn2.close();
        }

}
