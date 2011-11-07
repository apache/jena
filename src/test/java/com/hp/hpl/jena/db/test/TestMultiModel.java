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
 * (Based in part on earlier Jena tests by bwm, kers, et al.)
 * 
 * @author csayers
*/

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.db.impl.IRDBDriver;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.DB;

import junit.framework.TestCase;
import junit.framework.TestSuite;



public class TestMultiModel extends TestCase
    {
	String DefModel = GraphRDB.DEFAULT;    

        
    public TestMultiModel( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestMultiModel.class ); }   
     
    Model model = null;
    ModelRDB dmod1 = null;    
	ModelRDB dmod2 = null;
	ModelRDB nmod1 = null; 
	ModelRDB nmod2 = null; 
	IDBConnection conn = null;
	IRDBDriver dbDriver;
	
    @Override
    protected void setUp() throws java.lang.Exception {
    	conn = TestConnection.makeAndCleanTestConnection();
    	dbDriver = conn.getDriver();
		model = ModelRDB.createModel(conn);
		conn.getDriver().setStoreWithModel(DefModel);
		dmod1 = ModelRDB.createModel(conn,"Def_Model_1");
		conn.getDriver().setStoreWithModel("Def_Model_1");
		dmod2 = ModelRDB.createModel(conn, "Def_Model_2");
		conn.getDriver().setStoreWithModel(null);
		nmod1 = ModelRDB.createModel(conn,"Named_Model_1");
		conn.getDriver().setStoreWithModel("Named_Model_1");
		nmod2 = ModelRDB.createModel(conn,"Named_Model_2");
    }
    
    @Override
    protected void tearDown() throws java.lang.Exception {
    	dmod1.close(); dmod2.close();
    	nmod1.close(); nmod2.close();
    	conn.cleanDB();
    	conn.close();
    	conn = null;
    }
    
	
	public void addToDBGraphProp ( Model model, Property prop, String val ) {
		// first, get URI of the graph
		StmtIterator iter = model.listStatements(
			new SimpleSelector(null, DB.graphName, (RDFNode) null));
		assertTrue(iter.hasNext());
		
		Statement stmt = iter.nextStatement();
		assertTrue(iter.hasNext() == false);
		Resource graphURI = stmt.getSubject();
		Literal l = model.createLiteral(val);
		Statement s = model.createStatement(graphURI,prop,l);
		model.add(s);
		assertTrue(model.contains(s));
	}
	
    private void addOnModel(Model model, Statement stmt) {
    	model.add(stmt);
    	assertTrue( model.contains(stmt) );
    	model.add(stmt);
    	assertTrue( model.contains(stmt) );
    }
    
	private void rmvOnModel(Model model, Statement stmt) {
		assertTrue( model.contains(stmt) );
		model.remove(stmt);
		assertTrue( !model.contains(stmt) );
		model.add(stmt);
		assertTrue( model.contains(stmt) );
		model.remove(stmt);
		assertTrue( !model.contains(stmt) );
	}

    
	private void addRemove(Statement stmt) {
		long cnt = model.size();
		addOnModel(model,stmt);
		addOnModel(dmod1,stmt);
		addOnModel(dmod2,stmt);
		addOnModel(nmod1,stmt);
		addOnModel(nmod2,stmt);
		assertTrue( model.size() == (cnt+1));
		assertTrue( dmod1.size() == 1);
		assertTrue( dmod2.size() == 1);
		assertTrue( nmod1.size() == 1);
		assertTrue( nmod2.size() == 1);
		
		rmvOnModel(nmod2,stmt);
		rmvOnModel(nmod1,stmt);
		rmvOnModel(dmod2,stmt);
		rmvOnModel(dmod1,stmt);
		rmvOnModel(model,stmt);
		assertTrue( model.size() == cnt);
		assertTrue( dmod1.size() == 0);
		assertTrue( dmod2.size() == 0);
		assertTrue( nmod1.size() == 0);
		assertTrue( nmod2.size() == 0);
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

	public void testSetLongObjectLenFailure() {
		try {
			int len = dbDriver.getLongObjectLength();
			dbDriver.setLongObjectLength(len / 2);
			assertTrue(false);
		} catch (Exception e) {
		}
	}

	public void testLongObjectLen() {
		long longLen = dbDriver.getLongObjectLength();
		assertTrue(longLen > 0 && longLen < 100000);

		String base = ".";
		StringBuffer buffer = new StringBuffer(1024 + (int) longLen);
		/* long minLongLen = longLen < 1024 ? longLen - (longLen/2) : longLen - 512; */
		/* long maxLongLen = longLen + 1024; */
		/* TODO: find out why this test takes sooooo long (minutes!) with the above bounds */
		long minLongLen = longLen - 32;
		long maxLongLen = longLen + 32;
		assertTrue(minLongLen > 0);

		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Literal l;
		Statement stmt;
		while (buffer.length() < minLongLen) { /*( build base string */
			buffer.append(base);
		}
		/* add stmts with long literals */
		long modelSizeBeg = model.size();
		while (buffer.length() < maxLongLen) {
			l = model.createLiteral(buffer.toString());
			stmt = model.createStatement(s, p, l);
			model.add(stmt);
			assertTrue(model.contains(stmt));
			assertTrue(stmt.getObject().equals(l));
			buffer.append(base);
		}
		assertTrue(model.size() == (modelSizeBeg + maxLongLen - minLongLen));
		/* remove stmts with long literals */
		while (buffer.length() > minLongLen) {
			buffer.deleteCharAt(0);
			l = model.createLiteral(buffer.toString());
			stmt = model.createStatement(s, p, l);
			assertTrue(model.contains(stmt));
			model.remove(stmt);
			assertTrue(!model.contains(stmt));
		}
		assertTrue(model.size() == modelSizeBeg);
	}

	public void testSetLongObjectLen() {
		int len = dbDriver.getLongObjectLength();
        int len2 = len - 2 ;
		try {
			tearDown();
			conn = TestConnection.makeTestConnection();
			dbDriver = conn.getDriver();
			len = dbDriver.getLongObjectLength();
			dbDriver.setLongObjectLength(len2);
			model = ModelRDB.createModel(conn);
		} catch (Exception e) {
			assertTrue(false);
		}
		testLongObjectLen();

		// now make sure longObjectValue persists
		model.close();
		try {
			conn.close();
			conn = TestConnection.makeTestConnection();
			dbDriver = conn.getDriver();
			assertTrue(len == dbDriver.getLongObjectLength());
			model = ModelRDB.open(conn);
			assertTrue(len2 == dbDriver.getLongObjectLength());
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
	public void testAddRemoveHugeLiteral() {
        String base = Data.strLong ;
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
        String base = Data.strLong ;
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
   

}
