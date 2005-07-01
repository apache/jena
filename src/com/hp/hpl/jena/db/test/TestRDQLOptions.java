/*
  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestRDQLOptions.java,v 1.1 2005-07-01 21:56:07 wkw Exp $
*/

package com.hp.hpl.jena.db.test;

/**
 * 
 * This tests string match operations on the modelRDB.
 * 
*/


import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.db.impl.IRDBDriver;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdql.Query;
import com.hp.hpl.jena.rdql.QueryEngine;
import com.hp.hpl.jena.rdql.QueryExecution;
import com.hp.hpl.jena.rdql.QueryResults;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.RDF;

import junit.framework.TestCase;
import junit.framework.TestSuite;


public class TestRDQLOptions extends TestCase {    
        
    public TestRDQLOptions( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestRDQLOptions.class ); }           
    
    protected void setUp() throws java.lang.Exception {    	
    }
    
    protected void tearDown() throws java.lang.Exception {
    }

    private Statement crAssertedStmt(String subj, String pred, String obj) {
		Resource s = model.createResource("http://ex.org/" + subj);
		Property p = model.createProperty("http://ex.org/" + pred);
		Resource o = model.createResource("http://ex.org/" + obj);
		return crAssertedStmt(s, p, o);
	}
	
	private Statement crAssertedStmt(Resource s, Property p, RDFNode o) {
		Statement stmt = model.createStatement(s, p, o);
		model.add(stmt);
		return stmt;
	}
	
	private Resource crReifiedStmt(String node, Statement stmt) {
		Resource n = model.createResource("http://ex.org/" + node);
		Resource s = stmt.getSubject();
		Property p = stmt.getPredicate();
		RDFNode o = stmt.getObject();
		
		crAssertedStmt(n,RDF.subject,s);
		crAssertedStmt(n,RDF.predicate,p);
		crAssertedStmt(n,RDF.object,o);
		crAssertedStmt(n,RDF.type,RDF.Statement);
		return n;
	}
	
	private int countQuery(String qryString) {
	Query query;
	QueryExecution qe;
	QueryResults results;
	int i;

	query = new Query(qryString);
	query.setSource(model);
	qe = new QueryEngine(query);
	results = qe.exec();

	i = 0;
	while (results.hasNext()) {
		results.next(); i++;
	}
	return i;
	}
    
	String qryCountSubjS2 = "SELECT * WHERE " +
	   "(<ex:s2>, ?p1, ?v), (?v, ?p2, ?o)" + 
	   "USING ex FOR  <http://ex.org/> ";

	String qryCountObjS1 = "SELECT * WHERE " +
	   "(?s, ?p1, ?v), (?v, ?p2, <ex:o1>)" + 
	   "USING ex FOR  <http://ex.org/> ";

	String qryCountObjS1O1 = "SELECT * WHERE " +
	   "(?v, <rdf:subject>, <ex:s1>), (?v, <rdf:object>, <http://ex.org/o1>)" + 
	   "USING rdf FOR <http://www.w3.org/1999/02/22-rdf-syntax-ns#>, ex FOR <http://ex.org/>";


    private static void loadClass()
        {
        try { Class.forName(TestPackage.M_DBDRIVER_CLASS); }
        catch (Exception e) { throw new JenaException( e ); }
        }
        
    public static IDBConnection makeTestConnection() 
        {
        loadClass();
        return new DBConnection
            (
            TestPackage.M_DB_URL, 
            TestPackage.M_DB_USER, 
            TestPackage.M_DB_PASSWD, 
            TestPackage.M_DB
            );
        }
        
    public static IDBConnection makeAndCleanTestConnection()
        {
			DBConnection result;
        	try{
        result = (DBConnection) makeTestConnection();
        // result.getConnection().setAutoCommit(false);
		result.cleanDB();
        } catch (Exception e) { throw new JenaException( e ); }        
        return result;
        }

	IDBConnection conn = null;
    ModelRDB model = null;
        
	public void testMatches() throws java.lang.Exception {

		Class.forName(TestPackage.M_DBDRIVER_CLASS);
		conn = makeAndCleanTestConnection();
		model = ModelRDB.createModel(conn);
		
		IRDBDriver dbDriver = null;
		dbDriver = conn.getDriver();

		GraphRDB g = new GraphRDB( conn, null, null, 
				GraphRDB.OPTIMIZE_ALL_REIFICATIONS_AND_HIDE_NOTHING, false);
		model = new ModelRDB(g);

		Statement a1 = crAssertedStmt("s1","p1","o1");
		Statement a2 = crAssertedStmt("s2","p2","s1");
		Resource r1 = crReifiedStmt("r1",a1);
		Resource r2 = crReifiedStmt("r2",a2);

		int cnt;
//		model.setQueryOnlyReified(true);
//		model.setQueryFullReified(true);
		
//		cnt = countQuery(qryCountObjS1O1);
		cnt = countQuery(qryCountSubjS2);
		assertTrue(cnt == 1);

		cnt = countQuery(qryCountObjS1);
		assertTrue(cnt == 3);
		
		model.setQueryOnlyAsserted(true);
		cnt = countQuery(qryCountObjS1);
		assertTrue(cnt == 1);

		model.setQueryOnlyReified(true);
		model.setQueryFullReified(true);
		cnt = countQuery(qryCountObjS1);
		assertTrue(cnt == 0);

		cnt = countQuery(qryCountObjS1O1);
		assertTrue(cnt == 1);
		
		model.setQueryFullReified(false);
		try {
			// should throw exception that queryonlyreified
			// requires queryfullreified
			cnt = countQuery(qryCountObjS1O1);
			assertTrue(false);
		} catch (JenaException e) {
			assertTrue(true);
		}
	}


}
    	

/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
