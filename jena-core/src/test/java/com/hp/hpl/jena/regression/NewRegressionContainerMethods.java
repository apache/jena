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
//import com.hp.hpl.jena.regression.Regression.ResTestObjF;
import com.hp.hpl.jena.vocabulary.RDF;

public abstract class NewRegressionContainerMethods extends NewRegressionBase
    {
    public NewRegressionContainerMethods( String name )
        { super( name ); }
    
    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }

    protected Model m;
    protected Resource r;

    @Override public void setUp()
        { 
        m = getModel(); 
        r = m.createResource();
        }
    
    protected abstract Container createContainer();
       
    protected abstract Resource getContainerType();

    public void testEmptyContainer()
        { 
        Container c = createContainer();
        assertTrue( m.contains( c, RDF.type, getContainerType() ) );
        assertEquals( 0, c.size() );
        assertFalse( c.contains( tvBoolean ) );
        assertFalse( c.contains( tvByte ) );
        assertFalse( c.contains( tvShort ) );
        assertFalse( c.contains( tvInt ) );
        assertFalse( c.contains( tvLong ) );
        assertFalse( c.contains( tvChar ) );
        assertFalse( c.contains( tvFloat ) );
        assertFalse( c.contains( tvString ) );
        }

    public void testFillingContainer()
        {
        Container c = createContainer();
        String lang = "fr";
        Literal tvLiteral = m.createLiteral( "test 12 string 2" );
//        Resource tvResObj = m.createResource( new ResTestObjF() );
        c.add( tvBoolean ); assertTrue( c.contains( tvBoolean ) );
        c.add( tvByte ); assertTrue( c.contains( tvByte ) );
        c.add( tvShort ); assertTrue( c.contains( tvShort ) );
        c.add( tvInt ); assertTrue( c.contains( tvInt ) );
        c.add( tvLong ); assertTrue( c.contains( tvLong ) );
        c.add( tvChar ); assertTrue( c.contains( tvChar ) );
        c.add( tvFloat ); assertTrue( c.contains( tvFloat ) );
        c.add( tvString ); assertTrue( c.contains( tvString ) );
        c.add( tvString, lang ); assertTrue( c.contains( tvString, lang ) );
        c.add( tvLiteral ); assertTrue( c.contains( tvLiteral ) );
//        c.add( tvResObj ); assertTrue( c.contains( tvResObj ) );
        c.add( tvLitObj ); assertTrue( c.contains( tvLitObj ) );
        assertEquals( 11, c.size() );
        }

    public void testContainerOfIntegers()
        {
        int num = 10;
        Container c = createContainer();
        for (int i = 0; i < num; i += 1) c.add( i );
        assertEquals( num, c.size() );
        NodeIterator it = c.iterator();
        for (int i = 0; i < num; i += 1)
            assertEquals( i, ((Literal) it.nextNode()).getInt() );
        assertFalse( it.hasNext() );
        }

    public void testContainerOfIntegersRemovingA()
        {
        boolean[] retain = { true,  true,  true,  false, false, false, false, false, true,  true };
        testContainerOfIntegersWithRemoving( retain );
        }

    public void testContainerOfIntegersRemovingB()
        {
        boolean[] retain = { false, true, true, false, false, false, false, false, true, false };
        testContainerOfIntegersWithRemoving( retain );
        }

    public void testContainerOfIntegersRemovingC()
        {
        boolean[] retain = { false, false, false, false, false, false, false, false, false, false };
        testContainerOfIntegersWithRemoving( retain );
        }

    protected void testContainerOfIntegersWithRemoving( boolean[] retain )
        {
        final int num = retain.length;
        boolean [] found = new boolean[num];
        Container c = createContainer();
        for (int i = 0; i < num; i += 1) c.add( i );
        NodeIterator it = c.iterator();
        for (int i = 0; i < num; i += 1)
            {
            it.nextNode();
            if (retain[i] == false) it.remove();
            }
        NodeIterator s = c.iterator();
        while (s.hasNext())
            {
            int v = ((Literal) s.nextNode()).getInt();
            assertFalse( found[v] );
            found[v] = true;
            }
        for (int i = 0; i < num; i += 1)
            assertEquals( "element " + i, retain[i], found[i] );
        }
    }
