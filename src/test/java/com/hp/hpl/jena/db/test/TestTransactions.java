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
 * This tests basic operations on the modelRDB.
 * 
 * It adds/removes statements of different types and verifys
 * that the correct statements exist at the correct times.
 * 
 * To run, you must have a mySQL database operational on
 * localhost with a database name of "test" and allow use
 * by a user named "test" with an empty password.
 * 
 * @author hkuno
*/

import junit.framework.*;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.db.impl.DriverRDB;
import com.hp.hpl.jena.db.impl.Driver_MySQL;
import com.hp.hpl.jena.rdf.model.*;

public class TestTransactions extends TestCase
    {    
        
    public TestTransactions( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestTransactions.class ); }   
     
    ModelRDB model = null;    
	Model dbProperties = null;    
	IDBConnection conn = null;
	DriverRDB m_driver = null;
	
    @Override
    protected void setUp() throws java.lang.Exception {
    	
		conn = TestConnection.makeAndCleanTestConnection();
		dbProperties = conn.getDatabaseProperties();
		model = ModelRDB.createModel(conn); 
		m_driver = new Driver_MySQL();
		m_driver.setConnection(conn);
	}
    
    @Override
    protected void tearDown() throws java.lang.Exception {
    	model.close();
    	model = null;
    	conn.cleanDB();
    	conn.close();
    	conn = null;
    }

    private void addCommit(Statement stmt) {
    	model.remove(stmt);
    	model.begin();
    	model.add(stmt);
    	model.commit();
    	assertTrue( model.contains(stmt) );
    }
    
	private void addAbort(Statement stmt) {
			model.remove(stmt);
			// try {	
				model.begin();
				model.add(stmt);
				model.abort();			
			// } catch(Exception e) {
				// throw new JenaException( e ); // System.out.println("addAbort caught exception: " + e);
			// }
			assertTrue(!model.contains(stmt) );
		}
		
    public void testAddCommitURI() {
    	Resource s = model.createResource("test#subject");
    	Property p = model.createProperty("test#predicate");
    	Resource o = model.createResource("test#object");
    	
		addCommit( model.createStatement(s,p,o));    		
    }
    
	public void testAddAbortURI() {
			Resource s = model.createResource("test#subject");
			Property p = model.createProperty("test#predicate");
			Resource o = model.createResource("test#object");
    	
			addAbort( model.createStatement(s,p,o));    		
		}
    
    public void testAddCommitLiteral() {
    	Resource s = model.createResource("test#subject");
    	Property p = model.createProperty("test#predicate");
    	Literal l = model.createLiteral("testLiteral");
    	
    	addCommit( model.createStatement(s,p,l));    	
    } 
	
	
	public void testAddCommitHugeLiteral() {
        String base = Data.strLong ;
    	StringBuffer buffer = new StringBuffer(4096);
    	while(buffer.length() < 4000 )
    		buffer.append(base);
    	Resource s = model.createResource("test#subject");
    	Property p = model.createProperty("test#predicate");
    	Literal l = model.createLiteral(buffer.toString());
    	
    	addCommit( model.createStatement(s,p,l));    	
    } 
    
	public void testAddAbortHugeLiteral() {
	    String base = Data.strLong ;
	    StringBuffer buffer = new StringBuffer(4096);
	    while(buffer.length() < 4000 )
	        buffer.append(base);
	    Resource s = model.createResource("test#subject");
	    Property p = model.createProperty("test#predicate");
	    Literal l = model.createLiteral(buffer.toString());

	    addAbort( model.createStatement(s,p,l));    	
	} 
    
    public void testAddCommitDatatype() {
    	Resource s = model.createResource("test#subject");
    	Property p = model.createProperty("test#predicate");
    	Literal l = model.createTypedLiteral("stringType");
    	
    	addCommit( model.createStatement(s,p,l));    	
    } 
    
	public void testAddAbortDatatype() {
			Resource s = model.createResource("test#subject");
			Property p = model.createProperty("test#predicate");
			Literal l = model.createTypedLiteral("stringType");
    	
			addAbort( model.createStatement(s,p,l));    	
		} 


    public void testAddAbortHugeDatatype() {
        String base = Data.strLong ;
    	StringBuffer buffer = new StringBuffer(4096);
    	while(buffer.length() < 4000 )
    		buffer.append(base);
    	Resource s = model.createResource("test#subject");
    	Property p = model.createProperty("test#predicate");
    	Literal l2 = model.createTypedLiteral(buffer.toString());
    	
    	addAbort( model.createStatement(s,p,l2));    	
    } 
    
    public void testAddCommitHugeDatatype() {
        String base = Data.strLong ;
        StringBuffer buffer = new StringBuffer(4096);
        while(buffer.length() < 4000 )
            buffer.append(base);
        Resource s = model.createResource("test#subject");
        Property p = model.createProperty("test#predicate");
        Literal l2 = model.createTypedLiteral(buffer.toString());

        addCommit( model.createStatement(s,p,l2));    	
    } 
    
   public void testAddCommitBNode() {
    	Resource s = model.createResource();
    	Property p = model.createProperty("test#predicate");
    	Resource o = model.createResource();
    	    	
     	addCommit( model.createStatement(s,p,o)); 	
   }
   
   public void testAddAbortBNode() {
		   Resource s = model.createResource();
		   Property p = model.createProperty("test#predicate");
		   Resource o = model.createResource();
    	    	
		   addAbort( model.createStatement(s,p,o)); 	
	  }


}
