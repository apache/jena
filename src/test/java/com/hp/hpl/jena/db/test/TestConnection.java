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
 * This tests basic open/create operations on the modelRDB.
 * 
 * To run, you must have a mySQL database operational on
 * localhost with a database name of "test" and allow use
 * by a user named "test" with an empty password.
 * 
 * (based in part on model tests written earlier by bwm and kers)
 * 
 * @author csayers
 * @version 0.1
*/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.db.impl.IRDBDriver;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.DB;

public class TestConnection extends TestCase {
    
    protected static Logger logger = LoggerFactory.getLogger( TestConnection.class );
	
	String DefModel = GraphRDB.DEFAULT;    
        
    public TestConnection( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestConnection.class ); }           
    
    @Override
    protected void setUp() throws java.lang.Exception {    	
    }
    
    @Override
    protected void tearDown() throws java.lang.Exception {
    }
    
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
        IDBConnection result = makeTestConnection();
        boolean tryClean = true;
        boolean didClean = false;
        boolean tryUnlock = true;
        String err = null;
        while ( tryClean && !didClean ) {
        	try {
        		result.cleanDB();
        		didClean = true;
        	} catch (Exception e) {
        		err = err + "\n" + e;
        		if ( tryUnlock ) {
        			tryUnlock = false;
        			if ( result.getDriver().DBisLocked() )
        				try {
        					result.getDriver().unlockDB();
        				} catch ( Exception e1 ) {
        					err = err + "\n" + e1;
        				}
        		} else
        			tryClean = false;
        	}
        }
        if ( didClean == false )
        	throw new JenaException("Failed to clean database.\n" + err);       
        return result;
        }
        
/*	public void testNoClass() throws java.lang.Exception {
		try {
			IDBConnection conn = new DBConnection(
			TestPackage.M_DB_URL, 
			TestPackage.M_DB_USER, 
			TestPackage.M_DB_PASSWD, 
			TestPackage.M_DB);
			conn.cleanDB();
			assertTrue(false); // should not get here
		} catch (Exception e) {
		}
	} */

    public void testRecovery() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
        ModelRDB m = null;
        try {
		m = ModelRDB.createModel(conn, "myName");
		m.close();
        } catch ( Exception e ) {
			assertTrue(false);
        }
		try {
			m = ModelRDB.createModel(conn, "myName");
			assertTrue(false);
		} catch ( Exception e ) {
		}
    	conn.close();
    }

    
    public void testDBConnect() throws java.lang.Exception {
		IDBConnection conn = makeTestConnection();
    	conn.close();
    }
    
	public void testBadConnection() throws java.lang.Exception {
		/*
		 * try { IDBConnection conn = new DBConnection( "Bad URL",
		 * TestPackage.M_DB_USER, TestPackage.M_DB_PASSWD, TestPackage.M_DB);
		 * conn.cleanDB(); assertTrue(false); // should not get here } catch
		 * (Exception e) { }
		 */
		try {
			IDBConnection conn = new DBConnection(
			TestPackage.M_DB_URL, 
			TestPackage.M_DB_USER, 
			TestPackage.M_DB_PASSWD, 
			"Bad DB");
			conn.cleanDB();
			assertTrue(false); // should not get here
		} catch (Exception e) {
		}
	}
    
    
    public void testConstructDefaultModel() throws java.lang.Exception {
		IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		m.remove();
    	conn.close();
    }
    
    public void testConstructAndOpenDefaultModel() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		m.close();
		ModelRDB m2 = ModelRDB.open(conn);
		m2.remove();
		conn.close();
    }
        
    public void testConstructNamedModel() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn, "myName");
		m.remove();
    	conn.close();
    }
    
	public void testBadNamedModel() throws java.lang.Exception {
		IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = null;
		try {
			m = ModelRDB.createModel(conn, DefModel);
			assertTrue(false);
		} catch (Exception e) {
		}
		conn.close();
	}
	
	public void testBadNamedFactoryModel() throws java.lang.Exception {
		IDBConnection conn = makeAndCleanTestConnection();
		ModelMaker maker = ModelFactory.createModelRDBMaker(conn);
		Model m = null;
		try {
			m  = maker.createModel(DefModel);
			assertTrue(false);
		} catch (Exception e) {
		}
		conn.close();
	}
	
	public void testReconstructDefaultModel() throws java.lang.Exception {
		IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn);
		m.remove();
		ModelRDB m1 = ModelRDB.createModel(conn);
		m1.remove();
		conn.close();
	}
    
	public void testReconstructNamedModel() throws java.lang.Exception {
		IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn, "myName");
		m.remove();
		ModelRDB m1 = ModelRDB.createModel(conn, "myName");
		m1.remove();
		conn.close();
	}

        
    public void testConstructAndOpenNamedModel() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn, "myName");
		m.close();
		ModelRDB m2 = ModelRDB.open(conn, "myName");
		m2.remove();
    	conn.close();
    }
        
    public void testConstructParamaterizedModel() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn, ModelRDB.getDefaultModelProperties(conn));
		m.remove();
    	conn.close();
    }
        
    public void testConstructAndOpenParamaterizedModel() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn, ModelRDB.getDefaultModelProperties(conn));
		m.close();
		ModelRDB m2 = ModelRDB.open(conn);
		m2.remove();
    	conn.close();
    }
        
	public void testConstructNamedParamaterizedModel() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn, "myName", ModelRDB.getDefaultModelProperties(conn));
		m.remove();
    	conn.close();
    }
        
	public void testConstructAndOpenNamedParamaterizedModel() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn, "myName", ModelRDB.getDefaultModelProperties(conn));
		m.close();
		ModelRDB m2 = ModelRDB.open(conn, "myName");
		m2.remove();
    	conn.close();
    }
    
	public void testOpenNamedNonExistentModel() throws java.lang.Exception {
        IDBConnection conn = makeTestConnection();
		try {
			ModelRDB m2 = ModelRDB.open(conn, "myName");
			m2.remove();
			conn.close();
			assertTrue("Successfully opened non-existent model", false);
		} catch ( DoesNotExistException e ) {
			conn.close();
		}   
	}

	public void testOpenUnnamedNonExistentModel() throws java.lang.Exception {
        IDBConnection conn = makeTestConnection();
		try {
			conn.cleanDB();
			ModelRDB m2 = ModelRDB.open(conn);
			m2.remove();
			conn.close();
			assertTrue("Successfully opened unnamed non-existent model", false);
		} catch ( DoesNotExistException e ) {
			conn.close();
		}   
	}

	public void testCreateExistingModel() throws java.lang.Exception {
        IDBConnection conn = makeAndCleanTestConnection();
		ModelRDB m = ModelRDB.createModel(conn, "myName", ModelRDB.getDefaultModelProperties(conn));
		try {
			ModelRDB m2 = ModelRDB.createModel(conn, "myName", ModelRDB.getDefaultModelProperties(conn));
			m.remove(); m2.remove();
			conn.close();
			assertTrue("Successfully created pre-existing model", false);
		} catch ( AlreadyExistsException e ) {
			m.remove();
			conn.close();
		}
	}
	
	public void addToDBGraphProp ( Model model, Property prop, String val ) {
		// first, get URI of the graph
		StmtIterator iter = model.listStatements(
			new SimpleSelector(null, DB.graphName, (RDFNode) null));
		assertTrue(iter.hasNext());
		
		Statement stmt = iter.nextStatement();
		assertTrue(iter.hasNext() == false);
		Resource graphURI = stmt.getSubject();
		Literal l = model.createLiteral(val);
		Statement s = model.createStatement(graphURI,prop,l);
		model.add(s);
		assertTrue(model.contains(s));
	}
	
	public void testConstructDefSchemaModel() throws java.lang.Exception {
		IDBConnection conn = makeAndCleanTestConnection();
		conn.getDriver().setStoreWithModel("");
		// Model props = ModelRDB.getDefaultModelProperties(conn);
		// addToDBGraphProp(props,DB.graphDBSchema,DefModel);
		// ModelRDB m = ModelRDB.createModel(conn, props);
		ModelRDB m = ModelRDB.createModel(conn);
		m.remove();
		conn.close();
	}
	
	public void testConstructBadSchemaModel() throws java.lang.Exception {
		IDBConnection conn = makeAndCleanTestConnection();
		// Model props = ModelRDB.getDefaultModelProperties(conn);
		// addToDBGraphProp(props,DB.graphDBSchema,"SCHEMA_DOES_NOT_EXIST");
		conn.getDriver().setStoreWithModel(DefModel);
		try {
			// ModelRDB m = ModelRDB.createModel(conn, props);
			ModelRDB m = ModelRDB.createModel(conn);
			m.remove();
			assertFalse("Created model with non-existent schema",true);
		} catch (RDFRDBException e) {
		}
		conn.getDriver().setStoreWithModel("MODEL_DOES_NOT_EXIST");
		try {
			// ModelRDB m = ModelRDB.createModel(conn, props);
			ModelRDB m = ModelRDB.createModel(conn);
			m.remove();
			assertFalse("Created model with non-existent schema",true);
		} catch (RDFRDBException e) {
		}
		conn.close();
	}
	
	public void testConstructNamedModelDefSchema() throws java.lang.Exception {
		// this named model uses the default schema
		IDBConnection conn = makeAndCleanTestConnection();
		// Model props = ModelRDB.getDefaultModelProperties(conn);
		// addToDBGraphProp(props,DB.graphDBSchema,DefModel);
		conn.getDriver().setStoreWithModel(null);
		// ModelRDB m = ModelRDB.createModel(conn, "myName", props);
		ModelRDB m = ModelRDB.createModel(conn, "myName");
		m.remove();
		conn.close();
	}

	public void testConstructNamedModelDefSchema1() throws java.lang.Exception {
		// same as testConstructNamedModelDefSchema except the default model already exists.
		// should new model should share tables with default. no way now to verify this
		// from the API though. have to check it manually.
		IDBConnection conn = makeAndCleanTestConnection();
		// ModelRDB mdef = ModelRDB.createModel(conn, ModelRDB.getDefaultModelProperties(conn));
		// Model props = ModelRDB.getDefaultModelProperties(conn);
		// addToDBGraphProp(props,DB.graphDBSchema,DefModel);
		// ModelRDB m = ModelRDB.createModel(conn, "myName", props);
		ModelRDB mdef = ModelRDB.createModel(conn);
		conn.getDriver().setStoreWithModel(DefModel);
		ModelRDB m = ModelRDB.createModel(conn, "myName");
		mdef.remove(); m.remove();
		conn.close();
	}
	
	public void testConstructNamedModelDefSchema2() throws java.lang.Exception {
		// similar to testConstructNamedModelDefSchema1 except the newly created
		// model should not share the default schema.
		IDBConnection conn = makeAndCleanTestConnection();
		// ModelRDB mdef = ModelRDB.createModel(conn, ModelRDB.getDefaultModelProperties(conn));
		// Model props = ModelRDB.getDefaultModelProperties(conn);
		// addToDBGraphProp(props,DB.graphDBSchema,DefModel);
		// ModelRDB m = ModelRDB.createModel(conn, "myName", props);
		ModelRDB mdef = ModelRDB.createModel(conn);
		conn.getDriver().setStoreWithModel(null);
		ModelRDB m = ModelRDB.createModel(conn, "myName");
		mdef.remove(); m.remove();
		conn.close();
	}

	public void testConstructNamedModelSchema() throws java.lang.Exception {
		// construct two named models that share a schema
		IDBConnection conn = makeAndCleanTestConnection();
		// ModelRDB m1 = ModelRDB.createModel(conn, "model1", ModelRDB.getDefaultModelProperties(conn));
		ModelRDB m1 = ModelRDB.createModel(conn, "model1");
		// Model props = ModelRDB.getDefaultModelProperties(conn);
		// addToDBGraphProp(props,DB.graphDBSchema,"model1");
		// ModelRDB m2 = ModelRDB.createModel(conn, "model2", props);
		conn.getDriver().setStoreWithModel("model1");
		ModelRDB m2 = ModelRDB.createModel(conn, "model2");
		m1.remove(); m2.remove();
		conn.close();
	}
	
	public void testNamedPrefixedModel() throws java.lang.Exception {
		IDBConnection conn = makeAndCleanTestConnection();
		IRDBDriver d = conn.getDriver();
		d.setTableNamePrefix("foo_");
		conn.cleanDB();  // just in case any crud lying about from previous test
		ModelRDB m = ModelRDB.createModel(conn, "myName");
		m.remove();
		conn.cleanDB();
		conn.close();
	}
	
	public void testNamedPrefixedPersists() throws java.lang.Exception {
		IDBConnection conn = makeTestConnection();
		IRDBDriver d = conn.getDriver();
		String pfx = "foo_";
		d.setTableNamePrefix(pfx);
		conn.cleanDB();  // just in case any crud lying about from previous test
		ModelRDB m = ModelRDB.createModel(conn, "myName");
		m.close();
		conn.close();
		conn = makeTestConnection();
		d = conn.getDriver();
		d.setTableNamePrefix(pfx);
		m = ModelRDB.open(conn, "myName");
		assertTrue(d.getTableNamePrefix().equalsIgnoreCase(pfx));
		conn.cleanDB();
	}

	public void testNamedPrefixFailure() throws java.lang.Exception {
		IDBConnection conn = makeAndCleanTestConnection();
		IRDBDriver d = conn.getDriver();
		String longPfx =
			"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
			"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
			"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
			"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
			"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
			"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
			"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
			"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" +
			"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
		try {
			d.setTableNamePrefix(longPfx);
			assertTrue(false);  // should not get here
		} catch (Exception e) {
		}
		ModelRDB m = ModelRDB.createModel(conn);
		try {
			d.setTableNamePrefix("foo_");
			assertTrue(false);  // should not get here
		} catch (Exception e) {
		}
		m.close();
		conn.close();
	}
	

	// helper class to sync threads
    public class SyncOnCount {
    	private int count;
    	
    	public SyncOnCount () { count = 0; }
    	
    	public synchronized boolean testCount ( int i ) {
    		return count >= i;
    	}
    	
    	public void incCount() { incCount(1) ; }

        public synchronized void incCount(int N) {
            count = count+N ;
        }

        
    	public void waitOnCount ( int cnt ) {
        	int i = 0;
        	for ( i=0; i<100; i++ )
        		try {
                    //System.err.println(cnt+"/"+count) ;
        			if ( testCount(cnt) )
                        break ;
        			Thread.yield();
        			Thread.sleep(1000);
        		} catch ( Exception e ) {
        			throw new RuntimeException("waitOnCount interrupted" + e);       			
        		}
        	if ( ! testCount(cnt) )
            {
                System.err.println() ;
                System.err.println("Expected="+cnt+" Actual="+count) ;
                assertTrue("waitOnCount", false) ;
            }
        }

    }
    
	//syncOnCount s;
    volatile String msg = "" ;
    
    public void testConcurrentThread() {
        
		class thread1 extends Thread {
			SyncOnCount s;

			public thread1(SyncOnCount sc) {
				super("thread1");
				s = sc;
			}

			@Override
            public void run() {
			    IDBConnection conn = makeAndCleanTestConnection();
//                try {
//                    // 0, 1, 2, 4, 8
//                    System.err.println("Connection.TRANSACTION_NONE = "+Connection.TRANSACTION_NONE) ;
//                    System.err.println("Connection.TRANSACTION_READ_UNCOMMITTED = "+Connection.TRANSACTION_READ_UNCOMMITTED) ;
//                    System.err.println("Connection.TRANSACTION_READ_COMMITTED = "+Connection.TRANSACTION_READ_COMMITTED) ;
//                    System.err.println("Connection.TRANSACTION_REPEATABLE_READ = "+Connection.TRANSACTION_REPEATABLE_READ) ;
//                    System.err.println("Connection.TRANSACTION_SERIALIZABLE = "+Connection.TRANSACTION_SERIALIZABLE) ;
//                    System.err.println("Level: "+conn.getConnection().getTransactionIsolation()) ;
//                } catch (Exception e) {}
                
			    try {
			        ModelRDB foo = ModelRDB.createModel(conn, "foo");
			        s.incCount(); // count is now 1
			        s.waitOnCount(2);
			        Resource u = foo.createResource("test#subject");
			        Property p = foo.createProperty("test#predicate");
			        Resource o = foo.createResource("test#object");
			        Statement stmt = foo.createStatement(u, p, o);
			        if ( foo.contains(stmt) )
			            synchronized(msg)
			            {
			                if ( msg.length() == 0 ) msg = "Thread 1 can see uncommited statement" ;
			                s.incCount(99);
			                return ;
			            }

			        //assertFalse(foo.contains(stmt));
			        s.incCount();
			        s.waitOnCount(4);
			        if ( ! foo.contains(stmt) )
			            synchronized(msg)
                        {
			                if ( msg.length() == 0 ) msg = "Thread 1 can't see commited statement" ;
			                s.incCount(99);
			                return ;
			            }
			        //assertTrue(foo.contains(stmt));
			        foo.remove(stmt);
			        s.incCount();
			    } finally {
			        try { conn.close() ; }
			        catch (Exception e) { e.printStackTrace() ; }
			    }
            }
        }

		class thread2 extends Thread {
			SyncOnCount s;

			public thread2(SyncOnCount sc) {
				super("thread2");
				s = sc;
			}

			@Override
            public void run() {
			    s.waitOnCount(1);
			    IDBConnection conn = makeTestConnection();

			    try {
			        ModelRDB foo = ModelRDB.open(conn, "foo");
			        foo.begin();
			        Resource u = foo.createResource("test#subject");
			        Property p = foo.createProperty("test#predicate");
			        Resource o = foo.createResource("test#object");
			        Statement stmt = foo.createStatement(u, p, o);
			        foo.add(stmt);
			        s.incCount();
			        s.waitOnCount(3);
			        if ( !foo.contains(stmt) )
			            synchronized(msg)
                        {
			                if ( msg.length() == 0 ) msg = "Thread 2 can't see statement it just added" ;
			                s.incCount(99);
			                return ;
			            }
			        //assertTrue(foo.contains(stmt));
			        try {
			            foo.commit();
			        } catch (Exception e) {
			            if ( msg.length() == 0 ) msg = "Failed to commit transaction: "+e.getMessage() ;
			            return ;
			        }
			        s.incCount(); // wake up thread 1
			        s.waitOnCount(5); // thread1 has now removed stmt
			        //assertFalse(foo.contains(stmt));
			        if ( foo.contains(stmt) )
			            synchronized(msg)
                        {
			                if ( msg.length() == 0 ) msg = "Thread 2 can see statement thread 1 should have removed" ;
			                s.incCount(99);
			                return ;
			            }
                    
                } finally {
                    try { conn.close() ; }
                    catch (Exception e) { e.printStackTrace() ; }
                }
			}
        }

        if ( ! TestPackage.M_DBCONCURRENT ) {
            logger.warn("Transaction isolation test surpressed");
            return;
        }
		SyncOnCount s = new SyncOnCount();
		Thread t1 = new thread1(s);
		Thread t2 = new thread2(s);
		t2.start();
		t1.start();
		try {
			t1.join();
			t2.join();
		} catch (Exception e) {
			assertTrue(false);
		}
        if ( msg != null && msg.length() > 0 )
            assertTrue(msg, false) ;
        
	}	

    public void testDBMutex() {
        IDBConnection conn = makeAndCleanTestConnection();
        IRDBDriver d = conn.getDriver();

        d.lockDB();
        try {
            ModelRDB foo = ModelRDB.createModel(conn,"foo");
            assertTrue(false); // db lock should prevent model create
        } catch ( Exception e) {            
        }

        d.unlockDB();
        
        if ( d.isDBFormatOK() )
            assertTrue(false); // db contains no model
        
        if ( conn.containsModel("foo") )
            assertTrue(false);
        
        // check that containsModel does not format db
        if ( d.isDBFormatOK() )
            assertTrue(false); // db contains no model
        
        ModelRDB foo = ModelRDB.createModel(conn,"foo");
        
        if ( d.isDBFormatOK() == false )
            assertTrue(false); // db should be formatted
        
        if ( conn.containsModel("foo") == false )
            assertTrue(false);
        
        if ( conn.containsModel("bar") )
            assertTrue(false);

        // now, delete a system table so db fmt is bad
        d.deleteTable(d.getSystemTableName(0));
        
        if ( d.isDBFormatOK() )
            assertTrue(false); // db should not be formatted
        
        try {
            conn.close();
        } catch ( Exception e) {
            assertTrue(false);
        }
        
        conn = makeTestConnection();
        d = conn.getDriver();

        if ( conn.containsModel("foo") )
            assertTrue(false);
        
        if ( d.isDBFormatOK() )
            assertTrue(false); // db should still not be formatted
    
        // following should format db
        ModelRDB bar = ModelRDB.createModel(conn,"bar");
        
        if ( d.isDBFormatOK() == false )
            assertTrue(false); // db should be formatted
            
        if ( conn.containsModel("foo") )
            assertTrue(false);

        if ( conn.containsModel("bar") == false )
            assertTrue(false);
        
        bar.begin();
        
        try {
            bar.remove(); // should fail due to active xact
            assertTrue(false);
        } catch ( Exception e) {            
        }
        
        bar.abort();
        
        bar.remove();
        
        try {
            conn.close();
        } catch ( Exception e) {
            assertTrue(false);
        }
    }

}
