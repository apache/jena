/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestBasicOperations.java,v 1.5 2003-05-21 14:50:18 chris-dollin Exp $
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
 * (Based in part on earlier Jena tests by bwm, kers, et al.)
 * 
 * @author csayers
*/

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.DB;

import junit.framework.TestCase;
import junit.framework.TestSuite;



public class TestBasicOperations extends TestCase
    {    
        
    public TestBasicOperations( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestBasicOperations.class ); }   
     
    ModelRDB model = null;    
	Model dbProperties = null;    
	IDBConnection conn = null;
	
    protected void setUp() throws java.lang.Exception {
    	conn = TestConnection.makeAndCleanTestConnection();
    	model = ModelRDB.createModel(conn);
    	dbProperties = conn.getDatabaseProperties();
    }
    
    protected void tearDown() throws java.lang.Exception {
    	model.close();
    	model = null;
    	conn.cleanDB();
    	conn.close();
    	conn = null;
    }
    
    private void addRemove(Statement stmt) {
    	model.add(stmt);
    	assertTrue( model.contains(stmt) );
    	model.remove(stmt);
    	assertTrue( !model.contains(stmt));
    	model.add(stmt);
    	assertTrue( model.contains(stmt) );
    	model.remove(stmt);
    	assertTrue( !model.contains(stmt));
    	model.add(stmt);
    	model.add(stmt);
    	assertTrue( model.contains(stmt) );
    	model.remove(stmt);
    	assertTrue( !model.contains(stmt));    	
    	model.add(stmt);
    	model.add(stmt);
    	model.add(stmt);
    	model.add(stmt);
    	model.add(stmt);
    	model.add(stmt);
    	assertTrue( model.contains(stmt) );
    	model.remove(stmt);
    	assertTrue( !model.contains(stmt));    	
    }
    
    public void testAddRemoveURI() {
    	Resource s = model.createResource("test#subject");
    	Property p = model.createProperty("test#predicate");
    	Resource o = model.createResource("test#object");
    	
		addRemove( model.createStatement(s,p,o));    		
    }
    
    public void testAddRemoveLiteral() {
    	Resource s = model.createResource("test#subject");
    	Property p = model.createProperty("test#predicate");
    	Literal l = model.createLiteral("testLiteral");
    	
    	addRemove( model.createStatement(s,p,l));    	
    } 

	public long getMaxLiteral() {
		Property p = DB.maxLiteral;
		StmtIterator iter = dbProperties.listStatements(
			new SimpleSelector(null, p, (RDFNode) null));
		assertTrue(iter.hasNext());
		
		Statement stmt = iter.nextStatement();
		assertTrue(iter.hasNext() == false);
		String maxLitStr = stmt.getString();
		/* String maxLitStr = "250"; */
		assertTrue(maxLitStr != null);		
		long maxLit = Integer.parseInt(maxLitStr);

		return maxLit;
	}
	
	public void testGetMaxLiteral() {
		long maxLit = getMaxLiteral();
		assertTrue(maxLit > 0 && maxLit < 100000);
		/* note: 100000 is a soft limit; some DBMS may allow larger max literals */
	}
	
	public void testMaxLiteral() {
		long maxLit = getMaxLiteral();
		assertTrue(maxLit > 0 && maxLit < 100000);

		String base = ".";
		StringBuffer buffer = new StringBuffer(1024+(int)maxLit);
		/* long minMaxLit = maxLit < 1024 ? maxLit - (maxLit/2) : maxLit - 512; */
		/* long maxMaxLit = maxLit + 1024; */
		/* TODO: find out why this test takes sooooo long (minutes!) with the above bounds */
		long minMaxLit = maxLit - 32;
		long maxMaxLit = maxLit + 32;
		assertTrue (minMaxLit > 0);


		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Literal l;
		Statement stmt;
		while(buffer.length() < minMaxLit) { /*( build base string */
			buffer.append(base);	
		}
		/* add stmts with long literals */
		long modelSizeBeg = model.size();
		while(buffer.length() < maxMaxLit) {
			l = model.createLiteral(buffer.toString());
			stmt = model.createStatement(s,p,l);
			model.add(stmt);
			assertTrue(model.contains(stmt));
			assertTrue(stmt.getObject().equals(l));
			buffer.append(base);
		}
		assertTrue ( model.size() == (modelSizeBeg + maxMaxLit - minMaxLit) ); 
		/* remove stmts with long literals */
		while(buffer.length() > minMaxLit ) {
			buffer.deleteCharAt(0);
			l = model.createLiteral(buffer.toString());
			stmt = model.createStatement(s,p,l);
			assertTrue( model.contains(stmt) );
			model.remove(stmt);
			assertTrue( !model.contains(stmt));    	
		}
		assertTrue ( model.size() == modelSizeBeg ); 
	} 
	
	public void testAddRemoveHugeLiteral() {
    	String base = "This is a huge string that repeats.";
    	StringBuffer buffer = new StringBuffer(4096);
    	while(buffer.length() < 4000 )
    		buffer.append(base);
    	Resource s = model.createResource("test#subject");
    	Property p = model.createProperty("test#predicate");
    	Literal l = model.createLiteral(buffer.toString());
    	
    	addRemove( model.createStatement(s,p,l));    	
    } 
    
    public void testAddRemoveDatatype() {
    	Resource s = model.createResource("test#subject");
    	Property p = model.createProperty("test#predicate");
    	Literal l = model.createTypedLiteral("stringType");
    	
    	addRemove( model.createStatement(s,p,l));    	
    } 

    public void testAddRemoveHugeDatatype() {
    	String base = "This is a huge string that repeats.";
    	StringBuffer buffer = new StringBuffer(4096);
    	while(buffer.length() < 4000 )
    		buffer.append(base);
    	Resource s = model.createResource("test#subject");
    	Property p = model.createProperty("test#predicate");
    	Literal l2 = model.createTypedLiteral(buffer.toString());
    	
    	addRemove( model.createStatement(s,p,l2));    	
    } 
    
   public void testAddRemoveBNode() {
    	Resource s = model.createResource();
    	Property p = model.createProperty("test#predicate");
    	Resource o = model.createResource();
    	    	
     	addRemove( model.createStatement(s,p,o));
   	   	
   }
   
   public void testBNodeIdentityPreservation() {
    	Resource s = model.createResource();
    	Property p = model.createProperty("test#predicate");
    	Resource o = model.createResource();
    	 
    	// Create two statements that differ only in
    	// the identity of the bnodes - then perform
    	// add-remove on one and verify the other is
    	// unchanged.
    	Statement spo = model.createStatement(s,p,o);
    	Statement ops = model.createStatement(o,p,s);
    	model.add(spo);
     	addRemove(ops);   	
    	assertTrue( model.contains(spo));
    	model.remove(spo);
   }
   
   public void testBNodePrefixedResource() {
		Resource s = model.createResource();
		Property p = model.createProperty("test#predicate");
		Resource o = model.createResource(">badURI");
		// badURI should be consistent with BlankNodeRDBPrefix in PSet_TripleStore_RDB
    	    	
		Statement stmt = model.createStatement(s,p,o);
        // TODO the thrown exception should be more specific 
		try {
			model.add(stmt);
			model.remove(stmt);
			assertTrue("Inserted URI with blank node prefix", false);
   		} catch ( RDFRDBException e ) {
	  }
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
