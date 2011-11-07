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

package com.hp.hpl.jena.assembler.acceptance;

import java.io.*;

import junit.extensions.TestSetup;
import junit.framework.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.test.AssemblerTestBase;
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.db.test.TestConnection;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.util.FileUtils;

public class AllAccept extends AssemblerTestBase
    {
    public AllAccept( String name )
        { super( name ); }
    
    public static TestSuite suite()
        {
        TestSuite result = new TestSuite();
        result.addTestSuite( AllAccept.class );
        result.addTest( new SetupDatabase( new TestSuite( TestDatabaseModes.class ) ) );
        return result;
        }
    
    public static class SetupDatabase extends TestSetup
        {
        public SetupDatabase( Test tests )
            { super( tests ); }
    
        @Override
        public void setUp() throws Exception
            {
            super.setUp();
            IDBConnection conn = TestConnection.makeAndCleanTestConnection();
            ModelRDB.createModel( conn, "square" );
            ModelRDB.createModel( conn, "circle" );
            ModelRDB.createModel( conn, "triangle" );
            ModelRDB.createModel( conn, "hex" );
            conn.close();
            IDBConnection x = ModelFactory.createSimpleRDBConnection();
            assertEquals( true, x.containsModel( "square" ) );
            assertEquals( false, x.containsModel( "line" ) );
            x.close();
            }
        }
    
    public void testUnadornedInferenceModel()
        {
        Resource root = resourceInModel( "x ja:reasoner R; R rdf:type ja:ReasonerFactory" );
        Model m = Assembler.general.openModel( root );
        assertInstanceOf( InfModel.class, m );
        InfModel inf = (InfModel) m;
        assertIsoModels( empty, inf.getRawModel() );
        assertInstanceOf( GenericRuleReasoner.class, inf.getReasoner() );
        }
    
    public void testWithContent() throws IOException
        {
        File f = FileUtils.tempFileName( "assembler-acceptance-", ".n3" );
        Model data = model( "a P b; b Q c" );
        FileOutputStream fs = new FileOutputStream( f );
        data.write( fs, "N3" );
        fs.close();
        Resource root = resourceInModel( "x rdf:type ja:MemoryModel; x ja:content y; y ja:externalContent file:" + f.getAbsolutePath() );
        Model m = Assembler.general.openModel( root );
        assertIsoModels( data, m );
        }
    }
