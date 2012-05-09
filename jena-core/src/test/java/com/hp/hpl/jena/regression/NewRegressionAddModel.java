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

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.vocabulary.RDF;

public class NewRegressionAddModel extends ModelTestBase
    {
    public NewRegressionAddModel( String name )
        { super( name ); }

    public static Test suite()
        { return new TestSuite( NewRegressionAddModel.class ); }
    
    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }
    
    protected Model m;
    
    @Override
    public void setUp()
        { m = getModel(); }
    
    @Override
    public void tearDown()
        { m = null; }
    
    public void testAddByIterator()
        {
        Model m1 = getModel();
        Model m2 = getModel();
        modelAdd( m1, "a P b; c P d; x Q 1; y Q 2" );
        m2.add( m1.listStatements() );
        assertEquals( m1.size(), m2.size() );
        assertSameStatements( m1, m2 );
        m1.add( m1.createResource(), RDF.value, m1.createResource() );
        m1.add( m1.createResource(), RDF.value, m1.createResource() );
        m1.add( m1.createResource(), RDF.value, m1.createResource() );
        StmtIterator s = m1.listStatements();
        m2.remove( s.nextStatement() ).remove( s );
        assertEquals( 0, m2.size() );
        }

    public void testAddByModel()
        {
        Model m1 = getModel(), m2 = getModel();
        modelAdd( m1, "a P b; c P d; x Q 1; y Q 2" );
        m2.add( m1 );
        assertEquals( m1.size(), m2.size() );
        assertSameStatements( m1, m2 );
        }

    public void testRemoveByModel()
        {
        Model m1 = getModel(), m2 = getModel();
        modelAdd( m1, "a P b; c P d; x Q 1; y Q 2" );
        m2.add( m1 ).remove( m1 );
        assertEquals( 0, m2.size() );
        assertFalse( m2.listStatements().hasNext() );
        }
    
    protected void assertSameStatements( Model m1, Model m2 )
        {
        assertContainsAll( m1, m2 );
        assertContainsAll( m2, m1 );
        }
    
    protected void assertContainsAll( Model m1, Model m2 )
        {
        for (StmtIterator s = m2.listStatements(); s.hasNext();)
            assertTrue( m1.contains( s.nextStatement() ) );
        }    
    }
