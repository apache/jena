/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestSimpleSelector.java,v 1.8 2003-07-01 14:55:46 chris-dollin Exp $
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
