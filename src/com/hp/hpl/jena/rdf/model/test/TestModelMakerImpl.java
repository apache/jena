/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestModelMakerImpl.java,v 1.1 2003-05-11 10:02:09 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;

import java.util.*;

import junit.framework.*;

/**
    Test ModelMakerImpl using a mock GraphMaker. This is as much an
    exercise in learning testing technique as it is in actually doing the test ....
    
 	@author hedgehog
*/
public class TestModelMakerImpl extends ModelTestBase
    {
    public TestModelMakerImpl(String name)
        { super(name); }
        
    public static TestSuite suite()
        { return new TestSuite( TestModelMakerImpl.class ); }

    private ModelMaker maker;
    private Graph graph;
    private GraphMaker graphMaker;
    
    public void setUp()
        {
        graph = GraphTestBase.graphWith( "" );
        graphMaker = new MockGraphMaker( graph );
        maker = new ModelMakerImpl( graphMaker );
        }
        
    public void testClose()
        {
        maker.close();
        assertEquals( history(), one( "close()") );
        }
        
    public void testRemove()
        {
        maker.removeModel( "London" );
        assertEquals( history(), one( "remove(London)" ) );
        }
        
    public void testCreate()
        {
        Model m = maker.createModel( "petal" );
        assertEquals( history(), one("create(petal,false)" ) );
        assertTrue( m.getGraph() == graph );
        }
        
    public void testCreateTrue()
        {
        Model m = maker.createModel( "stem", true );
        assertEquals( history(), one("create(stem,true)" ) );
        assertTrue( m.getGraph() == graph );        
        }
        
    public void testCreateFalse()
        {
        Model m = maker.createModel( "leaf", false );
        assertEquals( history(), one("create(leaf,false)" ) );
        assertTrue( m.getGraph() == graph );        
        }
        
    public void testOpen()
        {
        Model m = maker.openModel( "trunk" );
        assertEquals( history(), one("open(trunk,false)" ) );
        assertTrue( m.getGraph() == graph );    
        }
        
    public void testOpenFalse()
        {
        Model m = maker.openModel( "branch", false );
        assertEquals( history(), one("open(branch,false)" ) );
        assertTrue( m.getGraph() == graph );    
        }
        
    public void testOpenTrue()
        {
        Model m = maker.openModel( "bark", true );
        assertEquals( history(), one("open(bark,true)" ) );
        assertTrue( m.getGraph() == graph );    
        }
        
    public void testGetGraphMaker()
        {
        assertTrue( maker.getGraphMaker() == graphMaker );
        }
        
    private List history()
        { return ((MockGraphMaker) maker.getGraphMaker()).history; }
        
    private List one( String s )
        {
        List result = new ArrayList();
        result.add( s );
        return result;
        }
        
    static class MockGraphMaker implements GraphMaker
        {
        List history = new ArrayList();
        Graph graph;
        
        public MockGraphMaker( Graph graph )
            { this.graph = graph; }
            
        public Graph getGraph()
            {
            history.add( "get()" );
            return graph;
            }
            
        public Graph createGraph( String name, boolean strict )
            {
            history.add( "create(" + name + "," + strict + ")" );
            return graph;
            }
    
        public Graph createGraph( String name )
            {
            history.add( "create(" + name + ")" );
            return graph;
            }        
    
        public Graph openGraph( String name, boolean strict )
            {
            history.add( "open(" + name + "," + strict + ")" );
            return graph;
            }
            
        public Graph openGraph( String name )
            {
            history.add( "open(" + name + ")" );
            return graph;
            }   
    
        public void removeGraph( String name )
            {
            history.add( "remove(" + name + ")" );
            }
    
        public boolean hasGraph( String name )
            {
            history.add( "has(" + name + ")" ); 
            return false;
            }
    
        public void close()
            {
            history.add( "close()" );
            }
        }        
    }


/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/