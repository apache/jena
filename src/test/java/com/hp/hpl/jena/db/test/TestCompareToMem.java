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

import java.util.Iterator;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.*;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;

public class TestCompareToMem extends TestCase
    {    
        
    public TestCompareToMem( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestCompareToMem.class ); }   
     
     static Logger logger = LoggerFactory.getLogger( TestCompareToMem.class );
     
    Model modelrdf = null;    
    Model modelmem = null;
    
	IDBConnection conn = null;
	
    @Override
    protected void setUp() throws java.lang.Exception {
		conn = TestConnection.makeAndCleanTestConnection();
		modelrdf = ModelRDB.createModel(conn);
		modelmem = ModelFactory.createDefaultModel();
    }
    
    @Override
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

	    {
	        Iterator<Statement> it = modelmem.listStatements();
	        while( it.hasNext()) {
	            Statement s = it.next();
	            if( ! modelrdf.contains(s)) {
	                logger.error("Statment:"+s+" is in mem but not rdf");
	                logModel(modelmem, "Mem");
	                logModel(modelrdf, "RDF");
	            }
	            assertTrue( modelrdf.contains(s));
	        }
	    }
	    {
	        Iterator<Statement> it = modelrdf.listStatements();
	        while( it.hasNext()) {
	            Statement s = it.next();
	            if( ! modelmem.contains(s)) {
	                logger.error("Statment:"+s+" is in rdf but not memory");
	                logModel(modelmem, "Mem");
	                logModel(modelrdf, "RDF");
	            }
	            assertTrue( modelmem.contains(s));
	        }
	    }
	}
    
    private void logModel(Model m, String name) {
    	logger.debug("Model");
        Iterator<Statement> it = m.listStatements();
		while( it.hasNext()) { 
			Statement s = it.next();
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
    	String base = Data.strLong ;
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
        String base = Data.strLong ;
		StringBuffer buffer = new StringBuffer(4096);
		while(buffer.length() < 4000 )
			buffer.append(base);
		Resource s = modelrdf.createResource("test#subject");
		Property p = modelrdf.createProperty("test#predicate");
		Literal l2 = modelrdf.createTypedLiteral(buffer.toString());
    	
		addRemove( modelrdf.createStatement(s,p,l2));    	
	} 
    
	public void testAddRemoveHugeLiteral2() {
		String base = Data.strLong ;
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
