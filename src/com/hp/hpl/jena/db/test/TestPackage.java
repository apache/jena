/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.db.test;

import junit.framework.* ;
import com.hp.hpl.jena.util.Log;

/**
 * Based on earlier Jena tests by members of the Jena team.
 * 
 * @author		csayers
 * @version 	$Revision: 1.2 $
 */
public class TestPackage extends TestSuite
{
	static String MYSQL_URL = "jdbc:mysql://localhost/test";
	static String MYSQL_USER = "test";
	static String MYSQL_PASSWD = "";
	static String MYDB = "MySQL";

    static public TestSuite suite() {
        return new TestPackage();
    }
    
    /** Creates new TestPackage */
    private TestPackage() {
        super("GraphRDB");
        Log.getInstance().setLevel(Log.FINEST);
        Log.getInstance().setConsoleHandler();
		addTest( "TestConnection", TestConnection.suite() );
        addTest( "TestBasicOperations", TestBasicOperations.suite() );
        addTest( "TestSimpleSelector", TestSimpleSelector.suite() );
		addTest( "TestCompatability", TestCompatability.suite() );
		addTest( "TestCompareToMem", TestCompareToMem.suite() );
		addTest( "TestGraphRDB", TestGraphRDB.suite());
		// TODO remove the dmoz test for now - it fails because of invalid
		// characters in the Resource URIs in the DMOZ file.
		// addTest( "TestCompareDMOZ", TestCompareDMOZ.suite() );
        }

    private void addTest(String name, TestSuite tc) {
        tc.setName(name);
        addTest(tc);
    }

}

/*
 *  (c) Copyright Hewlett-Packard Company 2001-2003
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
