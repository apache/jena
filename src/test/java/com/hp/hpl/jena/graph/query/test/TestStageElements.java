/*
 	(c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestStageElements.java,v 1.1 2009-06-29 08:55:50 castagna Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;

public class TestStageElements extends QueryTestBase
    {
    public TestStageElements( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestStageElements.class ); }

    protected static final class MockElement extends StageElement
        {
        public boolean wasRun = false;

        public Domain lastDomain = null;

        @Override
        public void run( Domain current )
            { wasRun = true; lastDomain = current; }
        }

    protected static final class ControlledValuator extends ValuatorSet
        {
        protected boolean result;
        
        public ControlledValuator( boolean result )
            { this.result = result; }
        
        @Override
        public boolean evalBool( IndexValues vv )
            { return result; }
        }

    protected static final class Pipelet implements Pipe
        {
        public Domain lastPut;
        
        @Override
        public boolean hasNext()
            {
            return false;
            }

        @Override
        public Domain get()
            {
            return null;
            }

        @Override
        public void put( Domain d )
            { lastPut = d; }

        @Override
        public void close()
            {
            }

        @Override
        public void close( Exception e )
            {
            }
        }
    
    protected Pipelet p = new Pipelet();
    
    public void testPutBindings()
        { 
        StageElement.PutBindings b = new StageElement.PutBindings( p );
        Domain d = makeDomain( Node.ANY );
        b.run( d );
        assertEquals( d, p.lastPut );
        assertNotSame( d, p.lastPut );
        }

    public void testPutBindingsTwice()
        {
        StageElement.PutBindings b = new StageElement.PutBindings( p );
        Domain d1 = makeDomain( Node.ANY );
        Domain d2 = makeDomain( NodeCreateUtils.create( "_blank" ), NodeCreateUtils.create( "17" ) );
        b.run( d1 );
        b.run( d2 );
        assertEquals( d2, p.lastPut );
        assertNotSame( d2, p.lastPut );
        }
    
    public void testValuatorStageTrue()
        {
        ValuatorSet vs = new ControlledValuator( true );
        MockElement next = new MockElement();
        StageElement.RunValuatorSet r = new StageElement.RunValuatorSet( vs, next );
        Domain d = makeDomain( NodeCreateUtils.create( "_blank" ), NodeCreateUtils.create( "17" ) );
        r.run( d );
        assertTrue( next.wasRun );
        assertSame( d, next.lastDomain );
        }
    
    public void testValuatorStageFalse()
        {
        ValuatorSet vs = new ControlledValuator( false );
        MockElement next = new MockElement();
        StageElement.RunValuatorSet r = new StageElement.RunValuatorSet( vs, next );
        Domain d = makeDomain( NodeCreateUtils.create( "_blank" ), NodeCreateUtils.create( "17" ) );
        r.run( d );
        assertFalse( next.wasRun );
        assertNull( next.lastDomain );
        }    
    protected Domain makeDomain( Node X )
        { return makeDomain( X, null ); }
    
    protected Domain makeDomain( Node X, Node Y )
        {
        Domain d = new Domain(3);
        d.setElement( 1, X );
        d.setElement( 2, Y );
        return d;
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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