/*
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.db.test;

import junit.framework.* ;
import com.hp.hpl.jena.rdf.model.*;

/**
 * Based on earlier Jena tests by members of the Jena team.
 * 
 * @author		csayers
 * @version 	$Revision: 1.24 $
 */
public class TestPackage extends TestSuite
{
	
	/*
	 * this testPackage requires the parameters for a
	 * database connection to be defined. one method
	 * is to explicitly define the parameters for
	 * mysql, oracle, postgresql as shown here below.
	 * 
	 * an alternative is to use the "guess" methods
	 * in ModelFactoryBase to get the parameters from
	 * a configuration file (see test-db.sh, test.bat,
	 * test.sh). if the guess methods do not work for
	 * you, comment them out and use/modify the manual
	 * settings for your database engine, below.
	 */
 
	/*/ oracle settings
	static String M_DB_URL = "jdbc:oracle:oci8:@";
	static String M_DB_USER = "scott";
	static String M_DB_PASSWD = "tiger";
	static String M_DB = "Oracle";
	static String M_DBDRIVER_CLASS = "oracle.jdbc.OracleDriver";
	// */
	
	// */ mysql settings
	static String M_DB_URL = "jdbc:mysql://localhost/test";
	static String M_DB_USER = "test";
	static String M_DB_PASSWD = "";
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
        
	/*/
	static String M_DB_URL = ModelFactoryBase.guessDBURL();
    static String M_DB_USER = ModelFactoryBase.guessDBUser();
    static String M_DB_PASSWD = ModelFactoryBase.guessDBPassword();
    static String M_DB = ModelFactoryBase.guessDBType();
    static String M_DBDRIVER_CLASS = ModelFactoryBase.guessDBDriver();  
    // */      

    static public TestSuite suite() {
        return new TestPackage();
    }
    
    /** Creates new TestPackage */
    private TestPackage() {
        super("GraphRDB");
		addTest( "TestConnection", TestConnection.suite() );
        addTest( "TestBasicOperations", TestBasicOperations.suite() );
        addTest( "TestSimpleSelector", TestSimpleSelector.suite() );
		addTest( "TestCompatability", TestCompatability.suite() );
		addTest( "TestCompareToMem", TestCompareToMem.suite() );
		addTest( "TestGraphRDB", TestGraphRDB.suite());
		addTest( "TestGraphRDBMaker", TestGraphRDB.suite());
		addTest( "TestMultiModel", TestMultiModel.suite());
		addTest( "TestNsPrefix", TestNsPrefix.suite());
		addTest( "TestPrefixMapping", TestPrefixMapping.suite());
		addTest( "TestTransactions", TestTransactions.suite() );
        addTest( "TestDBSpec", TestDBSpec.suite() );
		addTest( "TestReifier", TestReifier.suite() );
		addTest( "TestReifierCompareToMem", TestReifierCompareToMem.suite());
		addTest( "TestQueryRDB", TestQueryRDB.suite());
		addTest( "TestQuery1", TestQuery1.suite());
        }

    private void addTest(String name, TestSuite tc) {
        tc.setName(name);
        addTest(tc);
    }

}

/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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
