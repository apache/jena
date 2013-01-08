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

package com.hp.hpl.jena.rdf.model.test;

import java.util.Arrays ;
import java.util.List ;

import junit.framework.TestSuite ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Statement ;

/**
    Tests of the Model-level bulk update API.

 	@author kers
 */

public class TestModelBulkUpdate extends ModelTestBase
{
    public TestModelBulkUpdate( String name )
    { super( name ); }

    public static TestSuite suite()
    { return new TestSuite( TestModelBulkUpdate.class ); }   

    public void testMBU()
    { testMBU( ModelFactory.createDefaultModel() ); }

    public void testContains( Model m, Statement [] statements )
    {
        for (int i = 0; i < statements.length; i += 1)
            assertTrue( "it should be here", m.contains( statements[i] ) );
    }

    public void testContains( Model m, List<Statement> statements )
    {
        for (int i = 0; i < statements.size(); i += 1)
            assertTrue( "it should be here", m.contains( statements.get(i) ) );
    }

    public void testOmits( Model m, Statement [] statements )
    {
        for (int i = 0; i < statements.length; i += 1)
            assertFalse( "it should not be here", m.contains( statements[i] ) );
    }

    public void testOmits( Model m, List<Statement> statements )
    {
        for (int i = 0; i < statements.size(); i += 1)
            assertFalse( "it should not be here", m.contains( statements.get(i) ) );
    }

    public void testMBU( Model m )
    {
        Statement [] sArray = statements( m, "moon orbits earth; earth orbits sun" );
        List<Statement> sList = Arrays.asList( statements( m, "I drink tea; you drink coffee" ) );
        m.add( sArray );
        testContains( m, sArray );
        m.add( sList );
        testContains( m, sList );
        testContains( m, sArray );
        /* */
        m.remove( sArray );
        testOmits( m, sArray );
        testContains( m, sList );    
        m.remove( sList );
        testOmits( m, sArray );
        testOmits( m, sList );
    }

    public void testBulkByModel()
    { testBulkByModel( ModelFactory.createDefaultModel() ); }

    public void testBulkByModel( Model m )
    {
        assertEquals( "precondition: model must be empty", 0, m.size() );
        Model A = modelWithStatements( "clouds offer rain; trees offer shelter" );
        Model B = modelWithStatements( "x R y; y Q z; z P x" );
        m.add( A );
        assertIsoModels( A, m );
        m.add( B );
        m.remove( A );
        assertIsoModels( B, m );
        m.remove( B );
        assertEquals( "", 0, m.size() );
    }

    public void testBulkRemoveSelf()
    {
        Model m = modelWithStatements( "they sing together; he sings alone" );
        m.remove( m );
        assertEquals( "", 0, m.size() );
    }

    public void addReification( Model m, String tag, String statement )
    {
        m.createReifiedStatement( tag, statement( m, statement ) );
    }
}
