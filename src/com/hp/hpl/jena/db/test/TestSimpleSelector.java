/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestSimpleSelector.java,v 1.2 2003-05-04 17:36:56 hkuno Exp $
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

import com.hp.hpl.jena.db.DBConnection;
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
	   
    protected void setUp() throws java.lang.Exception {
    	
        Class.forName(TestPackage.M_DBDRIVER_CLASS);
		conn = new DBConnection(TestPackage.M_DB_URL, TestPackage.M_DB_USER, TestPackage.M_DB_PASSWD, TestPackage.M_DB);
		conn.cleanDB();
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
    	StmtIterator iter = model.listStatements(
    	  new SimpleSelector(null, null, RDFS.Resource));
    	assertTrue(iter.hasNext());
    	Resource subject = iter.nextStatement()
    	                       .getSubject();
    	iter = model.listStatements(
    	  new SimpleSelector(subject, null, (RDFNode) null));
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
