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
 * This tests basic selection operations on the modelRDB.
 * 
 * To run, you must have a mySQL database operational on
 * localhost with a database name of "test" and allow use
 * by a user named "test" with an empty password.
 * 
 * (Based on an earlier Jena test by bwm and kers.)
 * 
 * @author csayers
*/

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;


public class TestSimpleSelector extends TestCase
    {    
        
    public TestSimpleSelector( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestSimpleSelector.class ); }   
 /*           
    public void assertFalse( String name, boolean b )
        { assertTrue( name, !b ); } */
        
    
    Model model = null;    
	IDBConnection conn = null;
	   
    @Override
    protected void setUp() throws java.lang.Exception {
    	
		conn = TestConnection.makeAndCleanTestConnection();
		model = ModelRDB.createModel(conn, TestPackage.M_DB); 
		
    	model.createResource()
    	     .addProperty(RDF.type, RDFS.Resource)
    	     .addProperty(RDFS.label, "foo")
    	     .addProperty(RDF.value, "123");
    	model.createResource()
    	     .addProperty(RDF.type, RDFS.Resource)
    	     .addProperty(RDFS.label, "bar")
    	     .addProperty(RDF.value, "123");
    	
    }
    
    @Override
    protected void tearDown() throws java.lang.Exception {
    	model.close();
    	model = null;
    	conn.cleanDB();
    	conn.close();   	
    }
    
    public void testAll() {
    	StmtIterator iter = model.listStatements(
    	  new SimpleSelector(null, null, (RDFNode) null));
    	int i =0;
    	while (iter.hasNext()) {
    		i++;
    		iter.next();
    	}
    	assertEquals(6, i);
    }
    
    public void testFindProperty() {
    	StmtIterator iter = model.listStatements(
    	  new SimpleSelector(null, RDFS.label, (RDFNode) null));
    	int i =0;
    	while (iter.hasNext()) {
    		i++;
    		Statement stmt = iter.nextStatement();
    		assertEquals(RDFS.label, stmt.getPredicate());
    	}
    	assertEquals(2, i);
    }
    
    public void testFindObject() {
    	StmtIterator iter = model.listStatements(
    	  new SimpleSelector(null, null, RDFS.Resource));
    	int i =0;
    	while (iter.hasNext()) {
    		i++;
    		Statement stmt = iter.nextStatement();
    		assertEquals(RDFS.Resource, stmt.getObject());
    	}
    	assertEquals(2, i);
    }
    
    public void testFindSubject() {
    	StmtIterator iter = model.listStatements( new SimpleSelector(null, null, RDFS.Resource));
    	assertTrue( iter.hasNext() );
    	Resource subject = iter.nextStatement().getSubject();
        iter.close();
    	iter = model.listStatements( new SimpleSelector(subject, null, (RDFNode) null));
    	int i =0;
    	while (iter.hasNext()) {
    		i++;
    		Statement stmt = iter.nextStatement();
    		assertEquals(subject, stmt.getSubject());
    	}
    	assertEquals(3, i);
    }
    
    public void testFindPropertyAndObject() {
    	StmtIterator iter = model.listStatements(
    	  new SimpleSelector(null, RDF.value, 123));
    	int i =0;
    	while (iter.hasNext()) {
    		i++;
    		Statement stmt = iter.nextStatement();
    		assertEquals(RDF.value, stmt.getPredicate());
    		assertEquals(123, stmt.getInt()); 
      	}
    	assertEquals(2, i);
    }
    
}
