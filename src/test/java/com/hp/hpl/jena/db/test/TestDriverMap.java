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
