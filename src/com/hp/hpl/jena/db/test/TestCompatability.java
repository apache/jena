/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestCompatability.java,v 1.5 2003-08-27 12:56:20 andy_seaborne Exp $
*/

package com.hp.hpl.jena.db.test;

/**
 * 
 * This tests basic open/create operations on the modelRDB.
 * 
 * To run, you must have a mySQL database operational on
 * localhost with a database name of "test" and allow use
 * by a user named "test" with an empty password.
 * 
 * NOTE - this will generate deprecated API warnings when compiled -
 * this is deliberate - it is designed to test the deprecated APIs
 * to make sure they still work.
 * 
 * (copied from the Jena 1 RDB tests written by der).
 * 
 * The only code changes from Jena 1 ModelRDB operations are the 
 * in the cleanup code (it was calling getStore()).
 *
 * @author csayers
 * @version $Revision: 1.5 $
*/

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.db.impl.*;
import com.hp.hpl.jena.regression.*;
import com.hp.hpl.jena.shared.*;

import junit.framework.*;


public class TestCompatability extends TestCase {    
        
    public TestCompatability( String name )
        { super( name ); }
        
    protected void setUp() throws java.lang.Exception {  
    }
    
    protected void tearDown() throws java.lang.Exception {
    }
    

    public static TestSuite suite() { 
        ConfigTestCaseRDB config = new ConfigTestCaseRDB(TestPackage.M_DB_URL, TestPackage.M_DB_USER, TestPackage.M_DB_PASSWD, "Generic", TestPackage.M_DB );
        
        TestSuite suite = new TestSuite();
        suite.addTest(new TestCaseRDB("test0", config));
        suite.addTest(new TestCaseRDB("test1", config));
        suite.addTest(new TestCaseRDB("test2", config));
        suite.addTest(new TestCaseRDB("test3", config));
        suite.addTest(new TestCaseRDB("test4", config));
        suite.addTest(new TestCaseRDB("test5", config));
        suite.addTest(new TestCaseRDB("test6", config));
        suite.addTest(new TestCaseRDB("test7", config));
        suite.addTest(new TestCaseRDB("test8", config));
        suite.addTest(new TestCaseRDB("test9", config));
        suite.addTest(new TestCaseRDB("test10", config));
        suite.addTest(new TestCaseRDB("test11", config));
        suite.addTest(new TestCaseRDB("test12", config));
        suite.addTest(new TestCaseRDB("test13", config));
        suite.addTest(new TestCaseRDB("test14", config));
        suite.addTest(new TestCaseRDB("test15", config));
        suite.addTest(new TestCaseRDB("test16", config));
        suite.addTest(new TestCaseRDB("test17", config));
        suite.addTest(new TestCaseRDB("test18", config));
        suite.addTest(new TestCaseRDB("test19", config));

        return suite;

    }



	/** Inner class which provides config information to TestCaseRDB */

	protected static class ConfigTestCaseRDB {

		/** base uri for the test databases*/
		String m_baseuri;

		/** User name for access the databases */
		String m_user;

		/** Password for this user */
		String m_password;

		/** table layout version to test */
		String m_layout;

		/** database type under test */
		String m_databaseType;

		/** flag if this database config supports multiple models per database */

		boolean supportsMultipleModels;

		/** flag if this database config supports jena-style reification */

		boolean supportsJenaReification;

		/** flag if the tearDown code should leave the DB tables intact by doing a manual database cleanup */

		boolean noReformat;

		/** Database connection */

		DBConnection m_dbconn = null;

		/** Create config.		
		 *  Needs a base uri for the database, user name and login, format and database type to test.		
		 *  For databases which support multiple models given the whole database uri. For single model		
		 *  databases give a base uri to which the model names should be appended.	
		 */

		ConfigTestCaseRDB(String baseuri, String user, String password, String layout, String database) {

			m_baseuri = baseuri;
			m_user = user;
			m_password = password;
			m_layout = layout;
			m_databaseType = database;

			try {
				Class.forName(TestPackage.M_DBDRIVER_CLASS); // ADDED  	
			} catch (Exception e) {
				throw new JenaException("Unable to instantiate  driver: " + TestPackage.M_DBDRIVER_CLASS);
			}

			try { 
				DBConnection temp = new DBConnection(baseuri, user, password);
				IRDBDriver driver = temp.getDriver(layout, database);
				supportsMultipleModels = driver.supportsMultipleModels();
				supportsJenaReification = driver.supportsJenaReification();
			} catch (RDFRDBException e) {

				supportsMultipleModels = false;
			}
			noReformat = false;
		}

		/** Create config.		
		 *  Needs a base uri for the database, user name and login, format and database type to test.		
		 *  For databases which support multiple models given the whole database uri. For single model
		 *  databases give a base uri to which the model names should be appended.		
		 */

		ConfigTestCaseRDB(
			String baseuri,
			String user,
			String password,
			String layout,
			String database,
			boolean noReformat) {
			this(baseuri, user, password, layout, database);
			this.noReformat = noReformat;
		}

		/** Create a model of the given name for this database config */

		ModelRDB createModel(String name) {
			if (supportsMultipleModels) {
				if (m_dbconn == null) {
					m_dbconn = new DBConnection(m_baseuri, m_user, m_password);
					if (!m_dbconn.isFormatOK()) {
						IRDBDriver driver = m_dbconn.getDriver(m_layout, m_databaseType);
						driver.formatDB();
					}
				}
				return ModelRDB.createModel(m_dbconn, name);
			} else {
				DBConnection dbcon = new DBConnection(m_baseuri + name, m_user, m_password);
				return ModelRDB.create(dbcon, m_layout, m_databaseType);
			}
		}
	} /// End of inner class ConfigTestCaseRDB

	/** Adapt the overall jena test suite to use an RDB store */

	protected static class TestCaseRDB extends TestCaseBasic {

		ConfigTestCaseRDB m_config;

		public TestCaseRDB(String name, ConfigTestCaseRDB config) {
			super(name);
			m_config = config;
		}
		
		// Override set up to create RDB models instead of mem models
		public void setUp() {
			m1 = m_config.createModel("jr1");
			m2 = m_config.createModel("jr2");
			m3 = m_config.createModel("jr3");
			m4 = m_config.createModel("jr4");
		}

		public void tearDown() {
			if (m_config.supportsMultipleModels && !m_config.noReformat) {
				// The brute force clean deletes the entire DB so only need to do it once
				cleanModel((ModelRDB) m1);
				m2.close();
				m3.close();
				m4.close();
				// Close connection so next time it reformats the DB

				try {
					m_config.m_dbconn.close();
				} catch (java.sql.SQLException e) {
					System.out.println("Problem during db clean up in regression test");
				}
				m_config.m_dbconn = null;
			} else {
				cleanModel((ModelRDB) m1);
				cleanModel((ModelRDB) m2);
				cleanModel((ModelRDB) m3);
				cleanModel((ModelRDB) m4);
			}
		}

		private void cleanModel(ModelRDB m) {
			try {
				if (m_config.noReformat) {
					// Do a slow, brute force manual clean to avoid the database getting reformatted
					// This is needed to supporting checking of legacy formats
					for (StmtIterator i = m.listStatements(); i.hasNext();) {
						i.next();
						i.remove();
					}
				} else {
					// Turn off messages about deleting tables that aren't there any more.
					//int l = Log.getInstance().getLevel();
					//Log.getInstance().setLevel(Log.SEVERE);
					//IRDBDriver driver = m.getStore().getDriver();
					//driver.cleanDB();
					//driver.close();
					m.getConnection().cleanDB();
					m.close();
					//Log.getInstance().setLevel(l);
				}
			} catch (Exception e) {
				assertTrue("Problem clearning up regression databases: " + e, false);
			}
		}
	} // End of inner class TestCaseRDB
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
