/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestTransactions.java,v 1.6 2003-05-21 16:45:17 chris-dollin Exp $
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
	
    protected void setUp() throws java.lang.Exception {
    	
		conn = TestConnection.makeAndCleanTestConnection();
		dbProperties = conn.getDatabaseProperties();
		model = ModelRDB.createModel(conn); 
		m_driver = new Driver_MySQL();
		m_driver.setConnection(conn);
	}
    
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
    	String base = "This is a huge string that repeats.";
    	StringBuffer buffer = new StringBuffer(4096);
    	while(buffer.length() < 4000 )
    		buffer.append(base);
    	Resource s = model.createResource("test#subject");
    	Property p = model.createProperty("test#predicate");
    	Literal l = model.createLiteral(buffer.toString());
    	
    	addCommit( model.createStatement(s,p,l));    	
    } 
    
	public void testAddAbortHugeLiteral() {
			String base = "This is a huge string that repeats.";
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
    	String base = "This is a huge string that repeats.";
    	StringBuffer buffer = new StringBuffer(4096);
    	while(buffer.length() < 4000 )
    		buffer.append(base);
    	Resource s = model.createResource("test#subject");
    	Property p = model.createProperty("test#predicate");
    	Literal l2 = model.createTypedLiteral(buffer.toString());
    	
    	addAbort( model.createStatement(s,p,l2));    	
    } 
    
	public void testAddCommitHugeDatatype() {
			String base = "This is a huge string that repeats.";
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
    	

/*
    (c) Copyright Hewlett-Packard Company 2002
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
