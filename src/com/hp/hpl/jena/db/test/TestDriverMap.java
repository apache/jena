/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestDriverMap.java,v 1.1 2005-07-29 09:11:28 chris-dollin Exp $
*/

package com.hp.hpl.jena.db.test;

import java.io.InputStream;

import junit.framework.TestSuite;

import com.hp.hpl.jena.db.impl.DriverMap;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.test.JenaTestBase;
import com.hp.hpl.jena.util.FileManager;

public class TestDriverMap extends JenaTestBase
    {
    public TestDriverMap( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestDriverMap.class ); }

    public void testAddDrivers()
        {
        String className = "some.class.name";
        String otherClassName = "other.class.name";
        assertEquals( null, DriverMap.get( "test-absent-A" ) );
        assertEquals( null, DriverMap.get( "test-Absent-A" ) );
        assertEquals( null, DriverMap.get( "Test-absent-A" ) );
        assertEquals( null, DriverMap.get( "tEST-absent-A" ) );
        assertEquals( null, DriverMap.get( "test-absent-B" ) );
        assertEquals( null, DriverMap.get( "test-absent-B" ) );
        DriverMap.add( "test-absent-A", className );
        assertEquals( className, DriverMap.get( "test-absent-A" ) );
        assertEquals( className, DriverMap.get( "test-absent-A" ) );
        assertEquals( className, DriverMap.get( "test-Absent-A" ) );
        assertEquals( className, DriverMap.get( "Test-absent-A" ) );
        assertEquals( className, DriverMap.get( "tEST-absent-A" ) );
        assertEquals( null, DriverMap.get( "test-absent-B" ) );
        assertEquals( null, DriverMap.get( "test-absent-B" ) );
        DriverMap.add( "test-absent-B", otherClassName );
        assertEquals( otherClassName, DriverMap.get( "test-absent-B" ) );
        assertEquals( otherClassName, DriverMap.get( "test-absent-B" ) );
        assertEquals( className, DriverMap.get( "test-absent-A" ) );
        assertEquals( className, DriverMap.get( "test-absent-A" ) );
        }
    
    public void testBuiltinDrivers()
        {
        assertEquals( "com.mysql.jdbc.Driver", DriverMap.get( "mysql" ) );
        assertEquals( "org.postgresql.Driver", DriverMap.get( "postgres" ) );
        assertEquals( "org.postgresql.Driver", DriverMap.get( "postgresql" ) );
        }
    
    public void testNormalDrivers()
        {
        assertEquals( "test.fake.Driver", DriverMap.get( "test-fake-driver" ) );
        testDrivers( FileManager.get().open( "etc/db-default-drivers.n3" ) );
        }
    
    public void testExtraDrivers()
        {
        testDrivers( FileManager.get().open( "etc/db-extra-drivers.n3" ) );
        }

    protected void testDrivers( InputStream in )
        {
        if (in != null)
            {
            Property ANY = null;
            Model m = ModelFactory.createDefaultModel();
            m.read( in, "", "N3" );
            StmtIterator A = m.listStatements( ANY, DriverMap.driverClass, ANY );
            while (A.hasNext())
                {
                Statement st = A.nextStatement();
                Resource S = st.getSubject();
                String className = st.getString();
                StmtIterator B = m.listStatements( S, DriverMap.driverName, ANY );
                while (B.hasNext())
                    {
                    String name = B.nextStatement().getString();
                    assertEquals( className, DriverMap.get( name ) );
                    }
                }
            }
        }
    
    }

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
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