/*
  (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestRDQLMatch.java,v 1.2 2004-12-06 13:50:09 andy_seaborne Exp $
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

import junit.framework.TestCase;
import junit.framework.TestSuite;


public class TestRDQLMatch extends TestCase {    
        
    public TestRDQLMatch( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestRDQLMatch.class ); }           
    
    protected void setUp() throws java.lang.Exception {    	
    }
    
    protected void tearDown() throws java.lang.Exception {
    }
    
	String pfxQueryString = "SELECT * WHERE " +
	   "(?s, <ex:prop>, ?o) and ?o =~ /^hi/" + 
	   "USING ex FOR  <http://example.org/> ";
         
	String sfxQueryString = "SELECT * WHERE " +
		 "(?s, <ex:prop>, ?o) and ?o =~ /there$/ " + 
		 "USING ex FOR  <http://example.org/> ";

	  String ipfxQueryString = "SELECT * WHERE " +
		 "(?s, <ex:prop>, ?o) and ?o =~ /^hi/i" + 
		 "USING ex FOR  <http://example.org/> ";
         
	  String isfxQueryString = "SELECT * WHERE " +
		   "(?s, <ex:prop>, ?o) and ?o =~ /there$/i " + 
		   "USING ex FOR  <http://example.org/> ";

	String epfxQueryString = "SELECT * WHERE " +
		 "(?s, <ex:prop>, ?o) and ?o =~ /^yo_their/ " + 
		 "USING ex FOR  <http://example.org/> ";
		 
	String unboundQueryString = "SELECT * WHERE " +
	   "(?s, ?p, ?o) and ?o =~ /hi/";

	String lobjQueryString = "SELECT * WHERE " +
		 "(?s, <ex:prop>, ?o) and ?o =~ /789obj/ " + 
		 "USING ex FOR  <http://example.org/> ";
		 
	String lsubjQueryString = "SELECT * WHERE " +
		 "(?s, <ex:prop>, ?o) and ?s =~ /789subj/ " + 
		 "USING ex FOR  <http://example.org/> ";

	String lpredQueryString = "SELECT * WHERE " +
		 "(<ex:subj>, ?p, ?o) and ?p =~ /789pred/ " + 
		 "USING ex FOR  <http://example.org/> ";



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
        
	public void testMatches() throws java.lang.Exception {

		Class.forName(TestPackage.M_DBDRIVER_CLASS);
		IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB model = ModelRDB.createModel(conn);
		
		IRDBDriver dbDriver = null;
		dbDriver = conn.getDriver();

		Resource subj =
			ResourceFactory.createResource("http://example.org/subj");
		Property pred =
			ResourceFactory.createProperty("http://example.org/prop");
		Resource obj = ResourceFactory.createResource("http://example.org/obj");
		Statement s1 = ResourceFactory.createStatement(subj, pred, obj);

		Literal lit1 = ResourceFactory.createPlainLiteral("hi there");
		Statement s2 = ResourceFactory.createStatement(subj, pred, lit1);

		Literal lit2 = ResourceFactory.createPlainLiteral("hi there again");
		Statement s3 = ResourceFactory.createStatement(subj, pred, lit2);

		Literal lit3 = ResourceFactory.createPlainLiteral("HI THERE");
		Statement s4 = ResourceFactory.createStatement(subj, pred, lit3);

		Literal lit4 = ResourceFactory.createPlainLiteral("HI THERE AGAIN");
		Statement s5 = ResourceFactory.createStatement(subj, pred, lit4);

		Literal lit5 = ResourceFactory.createPlainLiteral("yo_their");
		Statement s6 = ResourceFactory.createStatement(subj, pred, lit5);

		Literal lit6 = ResourceFactory.createPlainLiteral("yo their");
		Statement s7 = ResourceFactory.createStatement(subj, pred, lit6);

		// create three statements with long URIs and long literals
		String sfx = "0123456789";
		String longURI = "http://example.org/long#";
		long longLen = dbDriver.getLongObjectLength();
		// make long prefix
		while ( longURI.length() < longLen )
			longURI += sfx;
		Resource subjLong = ResourceFactory.createResource(longURI+"subj");
		Property predLong = ResourceFactory.createProperty(longURI+"pred");
		Resource objLong = ResourceFactory.createResource(longURI+"obj");
		Literal litLong = ResourceFactory.createPlainLiteral(longURI+"obj");
		Statement s8 = ResourceFactory.createStatement(subjLong,pred,objLong);
		Statement s9 = ResourceFactory.createStatement(subjLong,pred,litLong);
		Statement s10 = ResourceFactory.createStatement(subj,predLong,litLong);


		model.add(s1);
		model.add(s2);
		model.add(s3);
		model.add(s4);
		model.add(s5);
		model.add(s6);
		model.add(s7);
		model.add(s8);
		model.add(s9);
		model.add(s10);

		Query query;
		QueryExecution qe;
		QueryResults results;
		int i;

		query = new Query(pfxQueryString);
		query.setSource(model);
		qe = new QueryEngine(query);
		results = qe.exec();

		i = 0;
		while (results.hasNext()) {
			results.next(); i++;
		}
		assertTrue(i == 2);

		query = new Query(ipfxQueryString);
		query.setSource(model);
		qe = new QueryEngine(query);
		results = qe.exec();

		i = 0;
		while (results.hasNext()) {
			results.next(); i++;
		}
		assertTrue(i == 4);

		query = new Query(sfxQueryString);
		query.setSource(model);
		qe = new QueryEngine(query);
		results = qe.exec();

		i = 0;
		while (results.hasNext()) {
			results.next(); i++;
		}
		assertTrue(i == 1);

		query = new Query(isfxQueryString);
		query.setSource(model);
		qe = new QueryEngine(query);
		results = qe.exec();

		i = 0;
		while (results.hasNext()) {
			results.next(); i++;
		}
		assertTrue(i == 2);
	
		query = new Query(epfxQueryString);
		query.setSource(model);
		qe = new QueryEngine(query);
		results = qe.exec();

		i = 0;
		while (results.hasNext()) {
			results.next(); i++;
		}
		assertTrue(i == 1);
		
		query = new Query(unboundQueryString);
		query.setSource(model);
		qe = new QueryEngine(query);
		results = qe.exec();

		i = 0;
		while (results.hasNext()) {
			results.next(); i++;
		}
		assertTrue(i == 2);
		
		query = new Query(lsubjQueryString);
		query.setSource(model);
		qe = new QueryEngine(query);
		results = qe.exec();

		i = 0;
		while (results.hasNext()) {
			results.next(); i++;
		}
		assertTrue(i == 2);
		
		query = new Query(lpredQueryString);
		query.setSource(model);
		qe = new QueryEngine(query);
		results = qe.exec();

		i = 0;
		while (results.hasNext()) {
			results.next(); i++;
		}
		assertTrue(i == 1);
		
		query = new Query(lobjQueryString);
		query.setSource(model);
		qe = new QueryEngine(query);
		results = qe.exec();

		i = 0;
		while (results.hasNext()) {
			results.next(); i++;
		}
		assertTrue(i == 2);

	}


}
    	

/*
    (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
