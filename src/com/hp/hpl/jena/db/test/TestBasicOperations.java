/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestBasicOperations.java,v 1.10 2003-08-26 17:47:21 wkw Exp $
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

import junit.framework.TestCase;
import junit.framework.TestSuite;



public class TestBasicOperations extends TestCase {

	public TestBasicOperations(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return new TestSuite(TestBasicOperations.class);
	}

	ModelRDB model = null;
	IRDBDriver dbDriver = null;
	IDBConnection conn = null;

	protected void setUp() throws java.lang.Exception {
		conn = TestConnection.makeAndCleanTestConnection();
		model = ModelRDB.createModel(conn);
		dbDriver = conn.getDriver();
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
		assertTrue(model.contains(stmt));
		model.remove(stmt);
		assertTrue(!model.contains(stmt));
		model.add(stmt);
		assertTrue(model.contains(stmt));
		model.remove(stmt);
		assertTrue(!model.contains(stmt));
		model.add(stmt);
		model.add(stmt);
		assertTrue(model.contains(stmt));
		model.remove(stmt);
		assertTrue(!model.contains(stmt));
		model.add(stmt);
		model.add(stmt);
		model.add(stmt);
		model.add(stmt);
		model.add(stmt);
		model.add(stmt);
		assertTrue(model.contains(stmt));
		model.remove(stmt);
		assertTrue(!model.contains(stmt));
	}
	
	public void testAddRemoveURI() {
		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Resource o = model.createResource("test#object");

		addRemove(model.createStatement(s, p, o));
	}

	public void testAddRemoveLiteral() {
		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Literal l = model.createLiteral("testLiteral");

		addRemove(model.createStatement(s, p, l));
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
		try {
			tearDown();
			conn = TestConnection.makeTestConnection();
			dbDriver = conn.getDriver();
			len = dbDriver.getLongObjectLength();
			dbDriver.setLongObjectLength(len / 2);
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
			assertTrue(len / 2 == dbDriver.getLongObjectLength());
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	public void testAddRemoveHugeLiteral() {
		String base = "This is a huge string that repeats.";
		StringBuffer buffer = new StringBuffer(4096);
		while (buffer.length() < 4000)
			buffer.append(base);
		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Literal l = model.createLiteral(buffer.toString());

		addRemove(model.createStatement(s, p, l));
	}
	
	public void testCompressHugeURI() throws java.lang.Exception {
		// in this test, the prefix exceeed longObjectLength but the
		// compressed URI is less than longObjectLength
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		IRDBDriver d = conn.getDriver();
		d.setDoCompressURI(true);
		model = ModelRDB.createModel(conn);
		String pfx = "a123456789";
		String longPfx = "";
		long longLen = dbDriver.getLongObjectLength();
		// make long prefix
		while ( longPfx.length() < longLen )
			longPfx += pfx;
		String URI = longPfx + ":/www.foo/#bar";
		Resource s = model.createResource(URI);
		Property p = model.createProperty("test#predicate");
		Resource o = model.createResource("test#object");
		addRemove(model.createStatement(s, p, o));
	}

	public void testCompressHugeURI1() throws java.lang.Exception {
		// in this test, the prefix exceeed longObjectLength but the
		// compressed URI also exceeds longObjectLength
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		IRDBDriver d = conn.getDriver();
		d.setDoCompressURI(true);
		model = ModelRDB.createModel(conn);
		String pfx = "a123456789";
		String longPfx = "";
		long longLen = dbDriver.getLongObjectLength();
		// make long prefix
		while ( longPfx.length() < longLen )
			longPfx += pfx;
		String URI = longPfx + ":/www.foo/#bar" + longPfx;
		Resource s = model.createResource(URI);
		Property p = model.createProperty("test#predicate");
		Resource o = model.createResource("test#object");
		addRemove(model.createStatement(s, p, o));
	}

	public void testNoCompressHugeURI() throws java.lang.Exception {
		// in this test, the prefix exceeed longObjectLength but the
		// compressed URI is less than longObjectLength
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		IRDBDriver d = conn.getDriver();
		d.setDoCompressURI(false);
		model = ModelRDB.createModel(conn);
		String pfx = "a123456789";
		String longPfx = "";
		long longLen = dbDriver.getLongObjectLength();
		// make long prefix
		while ( longPfx.length() < longLen )
			longPfx += pfx;
		String URI = longPfx + ":/www.foo/#bar";
		Resource s = model.createResource(URI);
		Property p = model.createProperty("test#predicate");
		Resource o = model.createResource("test#object");
		addRemove(model.createStatement(s, p, o));
	}
	
	public void testNoCompressHugeURI1() throws java.lang.Exception {
		// in this test, the prefix exceeed longObjectLength but the
		// compressed URI also exceeds longObjectLength
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		IRDBDriver d = conn.getDriver();
		d.setDoCompressURI(false);
		model = ModelRDB.createModel(conn);
		String pfx = "a123456789";
		String longPfx = "";
		long longLen = dbDriver.getLongObjectLength();
		// make long prefix
		while ( longPfx.length() < longLen )
			longPfx += pfx;
		String URI = longPfx + ":/www.foo/#bar" + longPfx;
		Resource s = model.createResource(URI);
		Property p = model.createProperty("test#predicate");
		Resource o = model.createResource("test#object");
		addRemove(model.createStatement(s, p, o));
	}

	public void testAddRemoveHugeURI() throws java.lang.Exception {
		String pfx = "a123456789";
		String longPfx = "";
		long longLen = dbDriver.getLongObjectLength();
		// make long prefix
		while ( longPfx.length() < longLen )
			longPfx += pfx;
		String URI = longPfx + ":/www.foo/#bar" + longPfx;
		Resource s = model.createResource(URI);
		Property p = model.createProperty("test#predicate");
		Resource o = model.createResource("test#object");
		addRemove(model.createStatement(s, p, o));
	}
	
	public void testPrefixCache() throws java.lang.Exception {
		// in this test, add a number of long prefixes until the cache
		// overflows and then make sure they can be retrieved.
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		IRDBDriver d = conn.getDriver();
		d.setDoCompressURI(true);
		model = ModelRDB.createModel(conn);
		int cacheSize = d.getCompressCacheSize();
		cacheSize = 10;
		d.setCompressCacheSize(cacheSize);

		String pfx = "a123456789";
		String longPfx = "";
		long longLen = dbDriver.getLongObjectLength();
		// make long prefix
		while ( longPfx.length() < longLen )
			longPfx += pfx;
		int i;
		for(i=0;i<cacheSize*2;i++) {
			String URI = longPfx + i + ":/www.foo/#bar";
			Resource s = model.createResource(URI);
			Property p = model.createProperty("test#predicate");
			Resource o = model.createResource("test#object");
			model.add(s, p, o);
		}
		for(i=0;i<cacheSize*2;i++) {
			String URI = longPfx + i + ":/www.foo/#bar";
			Resource s = model.createResource(URI);
			Property p = model.createProperty("test#predicate");
			Resource o = model.createResource("test#object");
			assertTrue(model.contains(s, p, o));
		}

	}

	public void testAddRemoveDatatype() {
		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Literal l = model.createTypedLiteral("stringType");

		addRemove(model.createStatement(s, p, l));
	}

	public void testAddRemoveHugeDatatype() {
		String base = "This is a huge string that repeats.";
		StringBuffer buffer = new StringBuffer(4096);
		while (buffer.length() < 4000)
			buffer.append(base);
		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Literal l2 = model.createTypedLiteral(buffer.toString());

		addRemove(model.createStatement(s, p, l2));
	}

	public void testAddRemoveBNode() {
		Resource s = model.createResource();
		Property p = model.createProperty("test#predicate");
		Resource o = model.createResource();

		addRemove(model.createStatement(s, p, o));

	}

	public void testBNodeIdentityPreservation() {
		Resource s = model.createResource();
		Property p = model.createProperty("test#predicate");
		Resource o = model.createResource();

		// Create two statements that differ only in
		// the identity of the bnodes - then perform
		// add-remove on one and verify the other is
		// unchanged.
		Statement spo = model.createStatement(s, p, o);
		Statement ops = model.createStatement(o, p, s);
		model.add(spo);
		addRemove(ops);
		assertTrue(model.contains(spo));
		model.remove(spo);
	}

	public void testDuplicateCheck() {
		Resource s = model.createResource();
		Property p = model.createProperty("test#predicate");
		Resource o = model.createResource();
		Statement spo = model.createStatement(s, p, o);
		model.add(spo);
		try {
			model.add(spo); // should be fine
			assertTrue(model.size() == 1);
		} catch (Exception e) {
			assertTrue(false); // should not get here
		}
		model.setDoDuplicateCheck(false);
		try {
			model.add(spo); // should be fine - just inserted a dup
			assertTrue(model.size() == 2);
		} catch (Exception e) {
			assertTrue(false); // should not get here
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
