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

package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.rdf.model.*;

import junit.framework.*;

/**
    A revamped version of the regression set-operation tests.
    @author kers
*/
public class NewRegressionSet extends NewRegressionBase
    {
    public NewRegressionSet( String name )
        { super( name ); }

    public static Test suite()
        { return new TestSuite( NewRegressionSet.class );  }

    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }

    protected Model m;
    
    @Override
    public void setUp()
        { 
        m = getModel();
        }
    
    public void testUnion()
        {
        Model m1 = getModel();
        Model m2 = getModel();
        modelAdd( m1, "a P b; w R x" );
        modelAdd( m2, "w R x; y S z" );
        Model um = m1.union( m2 );
        assertFalse( m1.containsAll( m2 ) );
        assertFalse( m2.containsAll( m1 ) );
        assertTrue( um.containsAll( m1 ) );
        assertTrue( um.containsAll( m2 ) );
        for (StmtIterator it = um.listStatements(); it.hasNext();)
            {
            Statement s = it.nextStatement();
            assertTrue( m1.contains( s ) || m2.contains( s ) );
            }
        for (StmtIterator it = m1.listStatements(); it.hasNext();)
            assertTrue( um.contains( it.nextStatement() ) );
        for (StmtIterator it = m2.listStatements(); it.hasNext();)
            assertTrue( um.contains( it.nextStatement() ) );
        assertTrue( um.containsAll( m1.listStatements() ) );
        assertTrue( um.containsAll( m2.listStatements() ) );
        }
    
    public void testIntersection()
        {
        Model m1 = getModel();
        Model m2 = getModel();
        modelAdd( m1, "a P b; w R x" );
        modelAdd( m2, "w R x; y S z" );
        Model im = m1.intersection( m2 );
        assertFalse( m1.containsAll( m2 ) );
        assertFalse( m2.containsAll( m1 ) );
        assertTrue( m1.containsAll( im ) );
        assertTrue( m2.containsAll( im ) );
        for (StmtIterator it = im.listStatements(); it.hasNext();)
            {
            Statement s = it.nextStatement();
            assertTrue( m1.contains( s ) && m2.contains( s ) );
            }
        for (StmtIterator it = im.listStatements(); it.hasNext();)
            assertTrue( m1.contains( it.nextStatement() ) );
        for (StmtIterator it = im.listStatements(); it.hasNext();)
            assertTrue( m2.contains( it.nextStatement() ) );
        assertTrue( m1.containsAll( im.listStatements() ) );
        assertTrue( m2.containsAll( im.listStatements() ) );
        }
    
    public void testDifference()
        {
        Model m1 = getModel();
        Model m2 = getModel();
        modelAdd( m1, "a P b; w R x" );
        modelAdd( m2, "w R x; y S z" );
        Model dm = m1.difference( m2 );
        for (StmtIterator it = dm.listStatements(); it.hasNext();)
            {
            Statement s = it.nextStatement();
            assertTrue( m1.contains( s ) && !m2.contains( s ) );
            }
        for (StmtIterator it = m1.union( m2 ).listStatements(); it.hasNext(); )
            {
            Statement s = it.nextStatement();
            assertEquals( m1.contains( s ) && !m2.contains( s ), dm.contains( s ) );
            }
        assertTrue( dm.containsAny( m1 ) );
        assertTrue( dm.containsAny( m1.listStatements() ) );
        assertFalse( dm.containsAny( m2 ) );
        assertFalse( dm.containsAny( m2.listStatements() ) );
        assertTrue( m1.containsAll( dm ) );
        }
    }
