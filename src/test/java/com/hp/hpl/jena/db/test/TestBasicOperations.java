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

import com.hp.hpl.jena.rdf.model.*;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.db.impl.IRDBDriver;
import com.hp.hpl.jena.vocabulary.RDF;



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

	@Override
    protected void setUp() throws java.lang.Exception {
		conn = TestConnection.makeAndCleanTestConnection();
		model = ModelRDB.createModel(conn);
		dbDriver = conn.getDriver();
	}

	@Override
    protected void tearDown() throws java.lang.Exception {
		if ( model != null ) model.close();
		model = null;
		if ( conn != null ) {
			conn.cleanDB();
			conn.close();
		}
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
	
	private Statement crAssertedStmt(String subj, String pred, String obj) {
		Resource s = model.createResource(subj);
		Property p = model.createProperty(pred);
		Resource o = model.createResource(obj);
		return crAssertedStmt(s, p, o);
	}
	
	private Statement crAssertedStmt(Resource s, Property p, RDFNode o) {
		Statement stmt = model.createStatement(s, p, o);
		model.add(stmt);
		return stmt;
	}
	
	private Resource crReifiedStmt(String node, Statement stmt) {
		Resource n = model.createResource(node);
		Resource s = stmt.getSubject();
		Property p = stmt.getPredicate();
		RDFNode o = stmt.getObject();
		
		crAssertedStmt(n,RDF.subject,s);
		crAssertedStmt(n,RDF.predicate,p);
		crAssertedStmt(n,RDF.object,o);
		crAssertedStmt(n,RDF.type,RDF.Statement);
		return n;
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

	public void testSetLongObjectLenMax() {
		int len = dbDriver.getLongObjectLength();
		int newLen = len;
		int lenMax = dbDriver.getLongObjectLengthMax();
		int hdrLen = 32; // allow 32 bytes for hdrs, etc.
		try {
			tearDown();
			conn = TestConnection.makeTestConnection();
			dbDriver = conn.getDriver();
			len = dbDriver.getLongObjectLength();
			lenMax = dbDriver.getLongObjectLengthMax();
			if ( len == lenMax )
				return; // nothing to test
			newLen = lenMax - hdrLen;
			dbDriver.setLongObjectLength(newLen);
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
			assertTrue(newLen == dbDriver.getLongObjectLength());
		} catch (Exception e) {
			assertTrue(false);
		}
	}
	
    public void testAddRemoveUTFLiteral()
    {
        String str = Data.strLong ;
        Resource s = model.createResource("test#subject");
        Property p = model.createProperty("test#predicate");
        Literal l = model.createLiteral(str);

        addRemove(model.createStatement(s, p, l));
    }
    
    public void testAddRemoveLiteralSpecials()
    {
        String str = Data.strSpecial ;
        Resource s = model.createResource("test#subject");
        Property p = model.createProperty("test#predicate");
        Literal l = model.createLiteral(str);
        addRemove(model.createStatement(s, p, l));
    }
    
	public void testAddRemoveHugeLiteral() {
		String base = Data.strLong ;
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
	
	public void testPrefixCachePersists() throws java.lang.Exception {
		// check that the prefix cache persists and affects all models in db.
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		IRDBDriver d = conn.getDriver();
		d.setDoCompressURI(true);
		model = ModelRDB.createModel(conn);
		int cacheSize = d.getCompressCacheSize();
		d.setCompressCacheSize(cacheSize/2);	
		model.close();
		conn.close();
		
		conn = TestConnection.makeTestConnection();
		d = conn.getDriver();
		try {
			d.setDoCompressURI(false);
			assertFalse(true); // should not get here
		} catch (Exception e) {
			model = ModelRDB.createModel(conn,"NamedModel");
			assertTrue(d.getDoCompressURI() == true);
			assertTrue(d.getCompressCacheSize() == cacheSize);
		}
	}


	public void testAddRemoveDatatype() {
		Resource s = model.createResource("test#subject");
		Property p = model.createProperty("test#predicate");
		Literal l = model.createTypedLiteral("stringType");

		addRemove(model.createStatement(s, p, l));
	}

	public void testAddRemoveHugeDatatype() {
		String base = Data.strLong ;
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
	
	private int countIter( Iterator<?> it ) {
		int i = 0;
		while( it.hasNext()) {
			Statement s = (Statement) it.next();
			i++;
		}
		return i;
	}

	private int countAll()
	{
		Iterator<?> it = model.listStatements();
		return countIter(it);
	}

	private int countSubj(Resource s)
	{
		Iterator<?> it = model.listStatements(s,(Property)null,(RDFNode)null);
		return countIter(it);
	}

	private int countObj(RDFNode o)
	{
		Iterator<?> it = model.listStatements((Resource)null,(Property)null,o);
		return countIter(it);
	}

	private int countPred(Property o)
	{
		Iterator<?> it = model.listStatements((Resource)null,o,(RDFNode)null);
		return countIter(it);
	}

	public void testQueryOnlyOption() {
		GraphRDB g = new GraphRDB( conn, null, null, 
				GraphRDB.OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING, false);
		model = new ModelRDB(g);

		Statement a1 = crAssertedStmt("s1","p1","o1");
		Statement a2 = crAssertedStmt("s2","p2","s1");
		Resource r1 = crReifiedStmt("r1",a1);
		Resource r2 = crReifiedStmt("r2",a2);
		
		// should find 10 statements total
		int cnt;
		Resource s1 = a1.getSubject();
		Property p1 = a1.getPredicate();
		RDFNode o1 = a1.getObject();
		
		cnt = countAll();
		assertTrue(cnt==10);
		cnt = countSubj(s1);
		assertTrue(cnt==1);
		cnt = countObj(o1);
		assertTrue(cnt==2);
		cnt = countObj(s1);
		assertTrue(cnt==3);
		cnt = countPred(p1);
		assertTrue(cnt==1);
		cnt = countPred(RDF.predicate);
		assertTrue(cnt==2);
		cnt = countSubj(r2);
		assertTrue(cnt==4);
		
		model.setQueryOnlyAsserted(true);
		cnt = countAll();
		assertTrue(cnt==2);
		cnt = countSubj(s1);
		assertTrue(cnt==1);
		cnt = countObj(o1);
		assertTrue(cnt==1);
		cnt = countObj(s1);
		assertTrue(cnt==1);
		cnt = countPred(p1);
		assertTrue(cnt==1);
		cnt = countPred(RDF.predicate);
		assertTrue(cnt==0);
		cnt = countSubj(r2);
		assertTrue(cnt==0);

		model.setQueryOnlyReified(true);
		cnt = countAll();
		assertTrue(cnt==8);
		cnt = countSubj(s1);
		assertTrue(cnt==0);
		cnt = countObj(o1);
		assertTrue(cnt==1);
		cnt = countObj(s1);
		assertTrue(cnt==2);
		cnt = countPred(p1);
		assertTrue(cnt==0);
		cnt = countPred(RDF.predicate);
		assertTrue(cnt==2);
		cnt = countSubj(r2);
		assertTrue(cnt==4);

		model.setQueryOnlyReified(false);
		cnt = countAll();
		assertTrue(cnt==10);
	}

}
