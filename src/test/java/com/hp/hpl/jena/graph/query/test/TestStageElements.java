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
