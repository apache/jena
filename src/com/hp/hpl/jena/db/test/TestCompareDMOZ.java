/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestCompareDMOZ.java,v 1.3 2003-07-01 12:48:27 chris-dollin Exp $
*/

package com.hp.hpl.jena.db.test;

/**
 * 
 * This tests basic operations on the modelRDB.
 * 
 * It loads the DMOZ database again and again and
 * compares the results.
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

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.mem.ModelMem;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.*;
import org.apache.log4j.*;

public class TestCompareDMOZ extends TestCase 
    {    
        
    public static String filename = "file:etc/10000dmoz.rdf";
    
    public TestCompareDMOZ( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestCompareDMOZ.class ); }   

    static Logger logger = Logger.getLogger( TestCompareDMOZ.class );
    
    private static class Log
        {
        static void severe( String s ) {}
        static void debug( String s ) {}
        }
             
    Model modelrdf = null;    
    Model modelmem = null;
    
	IDBConnection conn = null;
	
    protected void setUp() throws java.lang.Exception {
    	
        Class.forName(TestPackage.M_DBDRIVER_CLASS);
		conn = new DBConnection(TestPackage.M_DB_URL, TestPackage.M_DB_USER, TestPackage.M_DB_PASSWD, TestPackage.M_DB);
		conn.cleanDB(); // start with a fresh slate.
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
    
	private void compareModels() {
		
		Iterator it = modelrdf.listStatements();
		while( it.hasNext()) {
			Statement s = (Statement)it.next();
			if( ! modelmem.contains(s)) {
				Log.severe("Statment:"+s+" is in rdb but not memory");
			}
			assertTrue( modelmem.contains(s));
		}
		it = modelmem.listStatements();
		while( it.hasNext()) {
			Statement s = (Statement)it.next();
			if( ! modelrdf.contains(s)) {
				Log.severe("Statment:"+s+" is in mem but not rdb");
			}
			assertTrue( modelrdf.contains(s));
		}
   }
       
	private void compareModelsWithSelf() {
		
		Iterator it = modelrdf.listStatements();
		while( it.hasNext()) {
			Statement s = (Statement)it.next();
			if( ! modelrdf.contains(s)) {
				Log.severe("Statment:"+s+" is in rdb but contains returns false");
			}
			assertTrue( modelmem.contains(s));
		}
		it = modelmem.listStatements();
		while( it.hasNext()) {
			Statement s = (Statement)it.next();
			if( ! modelmem.contains(s)) {
				Log.severe("Statment:"+s+" is in mem but contains returns false");
			}
			assertTrue( modelrdf.contains(s));
		}
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
    
	public void testStmt1() {
		
		Resource s = modelrdf.createResource();
		Property p = modelrdf.createProperty("http://purl.org/dc/elements/1.0/Description");
		Literal l = modelrdf.createLiteral("Choosing the right revenue system for you adult site is one of the most important things to do in order to run a successful adult site. Beware of programs promising incredible conversion and high payouts, usually they ain't that good.  ");
		Statement st = modelrdf.createStatement(s,p,l);
		modelrdf.add(st);
		if( ! modelrdf.contains(st) ) {
			Log.severe("Error, failed to add statement:"+st);
		}
		assertTrue( modelrdf.contains(st));		
	}
	
	public void testStmt2() {
		
		Resource s = modelrdf.createResource("dmoz:Top/Adult/Computers/Internet/Web_Design_and_Development/Authoring/Webmaster_Resources/Affiliate_Programs");
		Property p = modelrdf.createProperty("http://purl.org/dc/elements/1.0/Description");
		Literal l = modelrdf.createLiteral("Choosing the right revenue system for you adult site is one of the most important things to do in order to run a successful adult site. Beware of programs promising incredible conversion and high payouts, usually they ain't that good.  ");
		Statement st = modelrdf.createStatement(s,p,l);
		modelrdf.add(st);
		if( ! modelrdf.contains(st) ) {
			Log.severe("Error, failed to add statement:"+st);
		}
		assertTrue( modelrdf.contains(st));		
	}
	
		
		public void testLoadOnce() {
		
		ModelMem temp = new ModelMem();
		temp.read(filename);
int dbg = 0;
		
		Iterator it = temp.listStatements();
		while( it.hasNext()) {
			Statement s = (Statement)it.next();
if ( s.getObject().toString().startsWith("This category is for Internet-exclusive"))
	dbg = 1;  /* remove after debugging; set breakpoint here. */
			modelrdf.add(s);
			modelmem.add(s);

			if( ! modelrdf.contains(s) ) {
				Log.severe("Error, failed to add statement:"+s);
			}
			assertTrue( modelrdf.contains(s));			
			assertTrue( modelrdf.size() == modelmem.size());
							
		}
    	
		compareModels();
		compareModelsWithSelf();
	}
    
	public void testLoadTwice() {
		modelrdf.read(filename);
		modelmem.read(filename);
		modelrdf.read(filename);
		modelmem.read(filename);
    	
		compareModels();
		compareModelsWithSelf();
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
