/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestCompareToMem.java,v 1.6 2003-08-27 12:56:20 andy_seaborne Exp $
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

import junit.framework.*;
import org.apache.log4j.Logger;

public class TestCompareToMem extends TestCase
    {    
        
    public TestCompareToMem( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestCompareToMem.class ); }   
     
     static Logger logger = Logger.getLogger( TestCompareToMem.class );
     
    Model modelrdf = null;    
    Model modelmem = null;
    
	IDBConnection conn = null;
	
    protected void setUp() throws java.lang.Exception {
		conn = TestConnection.makeAndCleanTestConnection();
		modelrdf = ModelRDB.createModel(conn);
		modelmem = new ModelMem();
    }
    
    protected void tearDown() throws java.lang.Exception {
    	modelrdf.close();
    	modelrdf = null;
    	conn.cleanDB();
    	conn.close();
    	conn = null;
    }
    
	private void addRemove(Statement stmt) {
		modelrdf.add(stmt);
		modelmem.add(stmt);
		
		assertTrue( modelmem.size() == 1);
		assertTrue( modelrdf.size() == 1);

		compareModels();
		
		modelrdf.remove(stmt);
		modelmem.remove(stmt);
		
		assertTrue( modelmem.size() == 0);
		assertTrue( modelrdf.size() == 0);
		
		compareModels();		
	}

	private void compareModels() {
		
		Iterator it = modelmem.listStatements();
		while( it.hasNext()) {
			Statement s = (Statement)it.next();
			if( ! modelrdf.contains(s)) {
				logger.error("Statment:"+s+" is in mem but not rdf");
				logModel(modelmem, "Mem");
				logModel(modelrdf, "RDF");
			}
			assertTrue( modelrdf.contains(s));
		}
		it = modelrdf.listStatements();
		while( it.hasNext()) {
			Statement s = (Statement)it.next();
			if( ! modelmem.contains(s)) {
				logger.error("Statment:"+s+" is in rdf but not memory");
				logModel(modelmem, "Mem");
				logModel(modelrdf, "RDF");
			}
			assertTrue( modelmem.contains(s));
		}
    }
    
    private void logModel(Model m, String name) {
    	logger.debug("Model");
		Iterator it = m.listStatements();
		while( it.hasNext()) { 
			Statement s = (Statement)it.next();
			RDFNode object = s.getObject();
			if( object instanceof Literal )
				logger.debug(name+": "+s.getSubject()+s.getPredicate()+((Literal)object).getValue()+" "+((Literal)object).getDatatype()+" "+((Literal)object).getLanguage());
			else
				logger.debug(name+": "+it.next()); 	
    	}
    }
    
    public void testAddRemoveURI() {
    	Resource s = modelrdf.createResource("test#subject");
    	Property p = modelrdf.createProperty("test#predicate");
    	Resource o = modelrdf.createResource("test#object");
    	
		addRemove( modelrdf.createStatement(s,p,o));    		
    }
    
    public void testAddRemoveLiteral() {
    	Resource s = modelrdf.createResource("test#subject");
    	Property p = modelrdf.createProperty("test#predicate");
    	Literal l = modelrdf.createLiteral("testLiteral");
    	
    	addRemove( modelrdf.createStatement(s,p,l));    	
    } 

    public void testAddRemoveHugeLiteral() {
    	String base = "This is a huge string that repeats.";
    	StringBuffer buffer = new StringBuffer(4096);
    	while(buffer.length() < 4000 )
    		buffer.append(base);
    	Resource s = modelrdf.createResource("test#subject");
    	Property p = modelrdf.createProperty("test#predicate");
    	Literal l = modelrdf.createLiteral(buffer.toString());
    	
    	addRemove( modelrdf.createStatement(s,p,l));    	
    } 
    
    public void testAddRemoveDatatype() {
    	Resource s = modelrdf.createResource("test#subject");
    	Property p = modelrdf.createProperty("test#predicate");
    	Literal l = modelrdf.createTypedLiteral("stringType");
    	
    	addRemove( modelrdf.createStatement(s,p,l));    	
    } 

	public void testAddRemoveHugeDatatype() {
		String base = "This is a huge string that repeats.";
		StringBuffer buffer = new StringBuffer(4096);
		while(buffer.length() < 4000 )
			buffer.append(base);
		Resource s = modelrdf.createResource("test#subject");
		Property p = modelrdf.createProperty("test#predicate");
		Literal l2 = modelrdf.createTypedLiteral(buffer.toString());
    	
		addRemove( modelrdf.createStatement(s,p,l2));    	
	} 
    
	public void testAddRemoveHugeLiteral2() {
		String base = "This is a huge string that repeats.";
		StringBuffer buffer = new StringBuffer(4096);
		while(buffer.length() < 4000 )
			buffer.append(base);
		Resource s = modelmem.createResource("test#subject");
		Property p = modelmem.createProperty("test#predicate");
		Literal l2 = modelmem.createLiteral(buffer.toString());
		Literal l3 = modelmem.createLiteral(buffer.toString()+".");
    	
		Statement st1 = modelmem.createStatement(s,p,l2);
		Statement st2 = modelmem.createStatement(s,p,l3);
    	modelrdf.add(st1);
    	modelmem.add(st1);

		compareModels();

		modelrdf.add(st2);
		modelmem.add(st2);
		
		compareModels();
		
		modelrdf.remove(st2);
		modelmem.remove(st2);
 
		compareModels();

		modelrdf.remove(st1);
		modelmem.remove(st1);
			
	} 
    
}
    	

/*
    (c) Copyright 2002 Hewlett-Packard Development Company, LP
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
