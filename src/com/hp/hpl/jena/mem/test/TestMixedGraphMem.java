/*
  (c) (c) Copyright 2004, 2005, 2006 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestMixedGraphMem.java,v 1.6 2006-03-22 13:53:26 andy_seaborne Exp $
*/

package com.hp.hpl.jena.mem.test;

import java.util.*;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.mem.MixedGraphMem;


/**
 @author hedgehog
 */
public class TestMixedGraphMem extends TestGraphMem
    {
    public TestMixedGraphMem( String name ) 
        { super( name );}
    
    public static TestSuite suite()
        { return new TestSuite( TestMixedGraphMem.class ); }
        
    public Graph getGraph()
        { return new MixedGraphMem(); }
    
    public void testRepeatedAddSuppressesPredicateAndObject()
        {
        final List history = new ArrayList();
        MixedGraphMemStore t = new MixedGraphMemStore( getGraph() )
            {
            protected boolean add( Node key, Triple t )
                {
                history.add( key );
                return super.add( key, t );
                }
            };
        t.add( triple( "s P o" ) );
        assertEquals( nodeList( "s P o" ), history );
        t.add( triple( "s P o" ) );
        assertEquals( nodeList( "s P o s" ), history );
        }
    
    public void testUnnecessaryMatches() { 
        /* test not appropriate for subclass */ 
        }
    public void testRemoveAbsentSuppressesPredicateAndObject()
        {
        final List history = new ArrayList();
        MixedGraphMemStore t = new MixedGraphMemStore( getGraph() )
            {
            protected boolean remove( Node key, Triple t )
                {
                history.add( key );
                return super.remove( key, t );
                }
            };
        t.remove( triple( "s P o" ) );
        assertEquals( nodeList( "s" ), history );
        }
    }

/*
	 *  (c) Copyright 2004, 2005, 2006 Hewlett-Packard Development Company, LP
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