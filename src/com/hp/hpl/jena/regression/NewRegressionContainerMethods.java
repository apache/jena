/*
 	(c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionContainerMethods.java,v 1.4 2007-01-02 11:49:22 andy_seaborne Exp $
*/

package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.regression.Regression.ResTestObjF;
import com.hp.hpl.jena.vocabulary.RDF;

public abstract class NewRegressionContainerMethods extends NewRegressionBase
    {
    public NewRegressionContainerMethods( String name )
        { super( name ); }
    
    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }

    protected Model m;
    protected Resource r;

    public void setUp()
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
        Resource tvResObj = m.createResource( new ResTestObjF() );
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
        c.add( tvResObj ); assertTrue( c.contains( tvResObj ) );
        c.add( tvLitObj ); assertTrue( c.contains( tvLitObj ) );
        assertEquals( 12, c.size() );
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


/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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