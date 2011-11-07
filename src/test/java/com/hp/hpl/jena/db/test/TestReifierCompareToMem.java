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

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

import junit.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestReifierCompareToMem extends TestCase
    {    
        
    public TestReifierCompareToMem( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestReifierCompareToMem.class ); }   
     
     protected static Logger logger = LoggerFactory.getLogger( TestReifierCompareToMem.class );

    Model modelrdb = null;    
    Model modelmem = null;
    
	IDBConnection conn = null;
	
    @Override
    protected void setUp() throws java.lang.Exception {
		conn = TestConnection.makeAndCleanTestConnection();
		modelrdb = ModelRDB.createModel(conn);
		modelmem = ModelFactory.createDefaultModel();
    }
    
    @Override
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
	    {
	        Iterator<Statement> it = modelmem.listStatements();
	        while( it.hasNext()) {
	            Statement s = it.next();
	            if( ! modelrdb.contains(s)) {
	                logger.error( "Statment:"+s+" is in mem but not rdf");
	                logModel(modelmem, "Mem");
	                logModel(modelrdb, "RDF");
	            }
	            assertTrue( modelrdb.contains(s));
	        }
	    }
	    {
	        Iterator<Statement> it = modelrdb.listStatements();
	        while( it.hasNext()) {
	            Statement s = it.next();
	            if( ! modelmem.contains(s)) {
	                logger.error("Statment:"+s+" is in rdf but not memory");
	                logModel(modelmem, "Mem");
	                logModel(modelrdb, "RDF");
	            }
	            assertTrue( modelmem.contains(s));
	        }
	    }
	    assertTrue( modelmem.size() == modelrdb.size() );
	}
    
    private void logModel(Model m, String name) {
    	logger.debug("Model");
        Iterator<Statement> it = m.listStatements();
		while( it.hasNext()) { 
            logger.debug( name + ": " + it.next() );
//			Statement s = (Statement)it.next();
//			RDFNode object = s.getObject();
//			if( object instanceof Literal )
//				logger.debug(name+": "+s.getSubject()+s.getPredicate()+((Literal)object).getValue()+" "+((Literal)object).getDatatype()+" "+((Literal)object).getLanguage());
//			else
//				logger.debug(name+": "+it.next()); 	
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
        String base = Data.strLong ;
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
        String base = Data.strLong ;
		StringBuffer buffer = new StringBuffer(4096);
		while(buffer.length() < 4000 )
			buffer.append(base);
		Resource s = modelrdb.createResource("test#subject");
		Literal l2 = modelrdb.createTypedLiteral(buffer.toString());
    	
		addRemove( modelrdb.createStatement(s,RDF.object,l2));    	
	} 
    
	public void testAddRemoveHugeLiteral2Object() {
        String base = Data.strLong ;
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
