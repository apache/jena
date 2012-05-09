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

package com.hp.hpl.jena.rdf.model.test;

/**
	@author bwm out of kers
*/

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import junit.framework.*;

public class TestSimpleSelector extends TestCase
    {    
        
    public TestSimpleSelector( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestSimpleSelector.class ); }   
    
    Model model = null;    
        
    @Override
    protected void setUp() throws java.lang.Exception {
    	model = ModelFactory.createDefaultModel();
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
    	model = null;
    }
    
    /**
        A plain SimpleSelector must be simple.
    */
    public void testSimpleIsSimple()
        { assertTrue( new SimpleSelector( null, null, (RDFNode) null ).isSimple() ); }
        
    /**
        A random sub-class of SimpleSelector must not be simple.
    */
    public void testSimpleSubclassIsntSimple()
        { assertFalse( new SimpleSelector( null, null, (RDFNode) null ){}.isSimple() ); }
        
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
