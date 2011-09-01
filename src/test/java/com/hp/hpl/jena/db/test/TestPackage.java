/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.db.test;

import junit.framework.* ;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.rdf.model.*;

/**
 * Based on earlier Jena tests by members of the Jena team.
 * 
 * @author		csayers
 * @version 	$Revision: 1.1 $
 */
public class TestPackage extends TestCase
{
	
	/*
	 * this testPackage requires the parameters for a
	 * database connection to be defined. use the "guess" 
     * method in ModelFactoryBase to get the parameters from
	 * a configuration file (see test-db.sh, test.bat,
	 * test.sh). if the guess methods do not work for
	 * you, /contact Chris so we can work out why/ and in
     * the meantime /temporarily/ hack the code using the
     * examples below.
     * 
     * Using the guess* methods allows the same codebase to be
     * tested against different databases without having to
     * fiddle around by hand.
	 */
 
	/*/ oracle settings
	static String M_DB_URL = "jdbc:oracle:oci8:@";
	static String M_DB_USER = "scott";
	static String M_DB_PASSWD = "tiger";
	static String M_DB = "Oracle";
	static String M_DBDRIVER_CLASS = "oracle.jdbc.OracleDriver";
	// */
	
	/*/ mysql settings
	static String M_DB_URL = "jdbc:mysql://localhost/test";
	static String M_DB_USER = "test";
	static String M_DB_PASSWD = "test";
	static String M_DB = "MySQL";
	static String M_DBDRIVER_CLASS = "com.mysql.jdbc.Driver";
	// */
		
	/*/ postgresql settings
	static String M_DB_URL = "jdbc:postgresql://localhost/test";
	static String M_DB_USER = "test";
	static String M_DB_PASSWD = "";
	static String M_DB = "PostgreSQL";
	static String M_DBDRIVER_CLASS = "org.postgresql.Driver";
	// */
        
	
    /*
        Command-line controlled settings (using -Dfoo=bar options, see
        ModelFactoryBase.guess* methods). Note that the -D options can
        be set from inside Eclipse, and presumably other IDEs as well.      
    */
	//*/
	public static String M_DB_URL = ModelFactoryBase.guessDBURL();
    public static String M_DB_USER = ModelFactoryBase.guessDBUser();
    public static String M_DB_PASSWD = ModelFactoryBase.guessDBPassword();
    public static String M_DB = ModelFactoryBase.guessDBType();
    public static String M_DBDRIVER_CLASS = ModelFactoryBase.guessDBDriver();
    public static boolean  M_DBCONCURRENT = ModelFactoryBase.guessDBConcurrent();
    // */      

    static public TestSuite suite() {
        TestSuite ts = new TestSuite() ;
        try {
            ts.setName("GraphRDB");
            addTest(ts, "TestDriverMap", TestDriverMap.suite()) ;
            addTest(ts, "TestConnection", TestConnection.suite()) ;
            addTest(ts, "TestBasicOperations", TestBasicOperations.suite()) ;
            addTest(ts, "TestSimpleSelector", TestSimpleSelector.suite()) ;
            addTest(ts, "TestCompareToMem", TestCompareToMem.suite()) ;
            addTest(ts, "TestGraphRDB", TestGraphRDB.suite()) ;
            addTest(ts, "TestModelRDB", TestModelRDB.suite()) ;
            addTest(ts, "TestGraphRDBMaker", TestGraphRDBMaker.suite()) ;
            addTest(ts, "TestMultiModel", TestMultiModel.suite()) ;
            addTest(ts, "TestNsPrefix", TestNsPrefix.suite()) ;
            addTest(ts, "TestPrefixMapping", TestPrefixMapping.suite()) ;
            addTest(ts, "TestTransactions", TestTransactions.suite()) ;
            addTest(ts, "TestReifier", TestReifier.suite()) ;
            addTest(ts, "TestReifierCompareToMem", TestReifierCompareToMem.suite()) ;
            addTest(ts, "TestQueryRDB", TestQueryRDB.suite()) ;
            addTest(ts, "TestQuery1", TestQuery1.suite()) ;
            addTest(ts, "TestModelFactory", TestModelFactory.suite()) ;
            addTest(ts, "TestDBType", TestDBType.suite()) ;
            ts.addTestSuite(TestRDBAssemblerContents.class) ;
            return ts ;
        }
        catch (Error ex)
        {
            ex.printStackTrace(System.err) ;
            return ts ; 
        }
    }

    private static void addTest(TestSuite ts, String name, TestSuite tc) {
        if ( name != null )
            tc.setName(name);
        ts.addTest(tc);
    }
    
    public static class TestModelFactory extends TestCase
        {
        public TestModelFactory( String name ) { super( name ); }
        public static TestSuite suite() { return new TestSuite( TestModelFactory.class ); }
        
        public void testModelFactory()
            {
            IDBConnection c = TestConnection.makeAndCleanTestConnection();
            assertTrue( ModelFactory.createModelRDBMaker( c ).createFreshModel() instanceof ModelRDB );
            }
        }
}

/*
 *  (c) Copyright 2001, 2002, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
