/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionBagMethods.java,v 1.2 2005-10-19 14:33:04 chris-dollin Exp $
*/

package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.regression.Regression.*;

import junit.framework.*;

public class NewRegressionBagMethods extends NewRegressionBase
    {
    public NewRegressionBagMethods( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( NewRegressionBagMethods.class );  }

    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }

    protected Model m;
    protected Resource r;

    public void setUp()
        { 
        m = getModel(); 
        r = m.createResource();
        }
    
    public void testEmptyBag() 
        { 
        Bag bag = m.createBag();
        assertTrue( m.contains( bag, RDF.type, RDF.Bag ) );
        assertEquals( 0, bag.size() );
        assertFalse( bag.contains( tvBoolean ) );
        assertFalse( bag.contains( tvByte ) );
        assertFalse( bag.contains( tvShort ) );
        assertFalse( bag.contains( tvInt ) );
        assertFalse( bag.contains( tvLong ) );
        assertFalse( bag.contains( tvChar ) );
        assertFalse( bag.contains( tvFloat ) );
        assertFalse( bag.contains( tvString ) );
        }
    
    public void testFillingBag()
        {
        Bag bag = m.createBag();
        String lang = "fr";
        Literal tvLiteral = m.createLiteral( "test 12 string 2" );
        Resource tvResObj = m.createResource( new ResTestObjF() );
        bag.add( tvBoolean ); assertTrue( bag.contains( tvBoolean ) );
        bag.add( tvByte ); assertTrue( bag.contains( tvByte ) );
        bag.add( tvShort ); assertTrue( bag.contains( tvShort ) );
        bag.add( tvInt ); assertTrue( bag.contains( tvInt ) );
        bag.add( tvLong ); assertTrue( bag.contains( tvLong ) );
        bag.add( tvChar ); assertTrue( bag.contains( tvChar ) );
        bag.add( tvFloat ); assertTrue( bag.contains( tvFloat ) );
        bag.add( tvString ); assertTrue( bag.contains( tvString ) );
        bag.add( tvString, lang ); assertTrue( bag.contains( tvString, lang ) );
        bag.add( tvLiteral ); assertTrue( bag.contains( tvLiteral ) );
        bag.add( tvResObj ); assertTrue( bag.contains( tvResObj ) );
        bag.add( tvLitObj ); assertTrue( bag.contains( tvLitObj ) );
        assertEquals( 12, bag.size() );
        }
    
    public void testBagOfIntegers()
        {
        int num = 10;
        Bag bag = m.createBag();
        for (int i = 0; i < num; i += 1) bag.add( i );
        assertEquals( num, bag.size() );
        NodeIterator it = bag.iterator();
        for (int i = 0; i < num; i += 1)
            assertEquals( i, ((Literal) it.nextNode()).getInt() );
        assertFalse( it.hasNext() );
        }
    
    public void testBagOfIntegersRemovingA()
        {
        boolean[] retain = { true,  true,  true,  false, false, false, false, false, true,  true };
        testBagOfIntegersWithRemoving( retain );
        }    
    
    public void testBagOfIntegersRemovingB()
        {
        boolean[] retain = { false, true, true, false, false, false, false, false, true, false };
        testBagOfIntegersWithRemoving( retain );
        }    
    
    public void testBagOfIntegersRemovingC()
        {
        boolean[] retain = { false, false, false, false, false, false, false, false, false, false };
        testBagOfIntegersWithRemoving( retain );
        }

    protected void testBagOfIntegersWithRemoving( boolean[] retain )
        {
        final int num = retain.length;
        boolean [] found = new boolean[num];
        Bag bag = m.createBag();
        for (int i = 0; i < num; i += 1) bag.add( i );
        NodeIterator it = bag.iterator();
        for (int i = 0; i < num; i += 1)
            {
            it.nextNode();
            if (retain[i] == false) it.remove();
            }
        NodeIterator s = bag.iterator();
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