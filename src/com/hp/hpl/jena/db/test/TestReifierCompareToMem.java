/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestReifierCompareToMem.java,v 1.1 2003-05-08 21:33:10 csayers Exp $
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

import java.util.Iterator;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.mem.ModelMem;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.Log;
import com.hp.hpl.jena.vocabulary.RDF;

import junit.framework.TestCase;
import junit.framework.TestSuite;



public class TestReifierCompareToMem extends TestCase
    {    
        
    public TestReifierCompareToMem( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestReifierCompareToMem.class ); }   
     
    Model modelrdb = null;    
    Model modelmem = null;
    
	IDBConnection conn = null;
	
    protected void setUp() throws java.lang.Exception {
		conn = TestConnection.makeAndCleanTestConnection();
		modelrdb = ModelRDB.createModel(conn);
		modelmem = new ModelMem();
    }
    
    protected void tearDown() throws java.lang.Exception {
    	modelrdb.close();
    	modelrdb = null;
    	conn.cleanDB();
    	conn.close();
    	conn = null;
    }
    
	private void addRemove(Statement stmt) {
		modelrdb.add(stmt);
		modelmem.add(stmt);
		
		compareModels();
		
		modelrdb.remove(stmt);
		modelmem.remove(stmt);
		
		compareModels();		
	}

	private void compareModels() {
		
		Iterator it = modelmem.listStatements();
		while( it.hasNext()) {
			Statement s = (Statement)it.next();
			if( ! modelrdb.contains(s)) {
				Log.severe("Statment:"+s+" is in mem but not rdf");
				logModel(modelmem, "Mem");
				logModel(modelrdb, "RDF");
			}
			assertTrue( modelrdb.contains(s));
		}
		it = modelrdb.listStatements();
		while( it.hasNext()) {
			Statement s = (Statement)it.next();
			if( ! modelmem.contains(s)) {
				Log.severe("Statment:"+s+" is in rdf but not memory");
				logModel(modelmem, "Mem");
				logModel(modelrdb, "RDF");
			}
			assertTrue( modelmem.contains(s));
		}
		
		assertTrue( modelmem.size() == modelrdb.size() );
    }
    
    private void logModel(Model m, String name) {
    	Log.debug("Model");
		Iterator it = m.listStatements();
		while( it.hasNext()) { 
			Statement s = (Statement)it.next();
			RDFNode object = s.getObject();
			if( object instanceof Literal )
				Log.debug(name+": "+s.getSubject()+s.getPredicate()+((Literal)object).getValue()+" "+((Literal)object).getDatatype()+" "+((Literal)object).getLanguage());
			else
				Log.debug(name+": "+it.next()); 	
    	}
    }
    
	public void testAddPredicate() {
 
 		Resource s = modelrdb.createResource("SSS"), o = modelrdb.createResource("OOO");
		
		Statement stmt = modelrdb.createStatement(s,RDF.object,o);
		modelrdb.add(stmt);
		modelmem.add(stmt);
		
		compareModels();
		
		modelrdb.remove(stmt);
		modelmem.remove(stmt);
		
		compareModels();
		  		
	}
    
	public void testAddRemoveFullReification() {
    	
		Resource s = modelrdb.createResource("SSS"), p = modelrdb.createResource("PPP"), o = modelrdb.createResource("OOO");
		
		Statement stmtT = modelrdb.createStatement(s,RDF.type,RDF.Statement);
		Statement stmtS = modelrdb.createStatement(s,RDF.subject,s);
		Statement stmtP = modelrdb.createStatement(s,RDF.predicate,p);
		Statement stmtO = modelrdb.createStatement(s,RDF.object,o);

		modelrdb.add(stmtT);
		modelmem.add(stmtT);

		compareModels();
		
		modelrdb.add(stmtS);
		modelmem.add(stmtS);

		compareModels();
		
		modelrdb.add(stmtP);
		modelmem.add(stmtP);

		compareModels();
		
		modelrdb.add(stmtO);
		modelmem.add(stmtO);
		
		compareModels();
		
		modelrdb.remove(stmtO);
		modelmem.remove(stmtO);

		compareModels();

		modelrdb.remove(stmtP);
		modelmem.remove(stmtP);

		compareModels();

		modelrdb.remove(stmtS);
		modelmem.remove(stmtS);

		compareModels();

		modelrdb.remove(stmtT);		
		modelmem.remove(stmtT);		  		

		compareModels();
	}
    
    public void testAddRemoveLiteralObject() {
    	Resource s = modelrdb.createResource("test#subject");
    	Literal l = modelrdb.createLiteral("testLiteral");
    	
    	addRemove( modelrdb.createStatement(s,RDF.object,l));    	
    } 

    public void testAddRemoveHugeLiteralObject() {
    	String base = "This is a huge string that repeats.";
    	StringBuffer buffer = new StringBuffer(4096);
    	while(buffer.length() < 4000 )
    		buffer.append(base);
    	Resource s = modelrdb.createResource("test#subject");
    	Literal l = modelrdb.createLiteral(buffer.toString());
    	
    	addRemove( modelrdb.createStatement(s,RDF.object,l));    	
    } 
    
    public void testAddRemoveDatatypeObject() {
    	Resource s = modelrdb.createResource("test#subject");
    	Literal l = modelrdb.createTypedLiteral("stringType");
    	
    	addRemove( modelrdb.createStatement(s,RDF.object,l));    	
    } 

	public void testAddRemoveHugeDatatypeObject() {
		String base = "This is a huge string that repeats.";
		StringBuffer buffer = new StringBuffer(4096);
		while(buffer.length() < 4000 )
			buffer.append(base);
		Resource s = modelrdb.createResource("test#subject");
		Literal l2 = modelrdb.createTypedLiteral(buffer.toString());
    	
		addRemove( modelrdb.createStatement(s,RDF.object,l2));    	
	} 
    
	public void testAddRemoveHugeLiteral2Object() {
		String base = "This is a huge string that repeats.";
		StringBuffer buffer = new StringBuffer(4096);
		while(buffer.length() < 4000 )
			buffer.append(base);
		Resource s = modelmem.createResource("test#subject");
		Literal l2 = modelmem.createLiteral(buffer.toString());
		Literal l3 = modelmem.createLiteral(buffer.toString()+".");
    	
		Statement st1 = modelmem.createStatement(s,RDF.object,l2);
		Statement st2 = modelmem.createStatement(s,RDF.object,l3);
    	modelrdb.add(st1);
    	modelmem.add(st1);

		compareModels();

		modelrdb.add(st2);
		modelmem.add(st2);
		
		compareModels();
		
		modelrdb.remove(st2);
		modelmem.remove(st2);
 
		compareModels();

		modelrdb.remove(st1);
		modelmem.remove(st1);
			
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
