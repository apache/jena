/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestModelMakerImpl.java,v 1.16 2004-11-02 15:57:12 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;

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
        checkHistory( one( "close()") );
        }
        
    public void testRemove()
        {
        maker.removeModel( "London" );
        checkHistory( one( "remove(London)" ) );
        }
        
    public void testCreate()
        {
        maker.createModel();
        checkHistory( one( "create()" ) );
        }
        
    public void testGet()
        {
        maker.getModel();
        checkHistory( one( "get()" ) );  
        }
        
    public void testCreateNamed()
        {
        Model m = maker.createModel( "petal" );
        checkHistory( one("create(petal,false)" ) );
        assertTrue( m.getGraph() == graph );
        }
        
    public void testCreateTrue()
        {
        Model m = maker.createModel( "stem", true );
        checkHistory( one("create(stem,true)" ) );
        assertTrue( m.getGraph() == graph );        
        }
        
    public void testCreateFalse()
        {
        Model m = maker.createModel( "leaf", false );
        checkHistory( one("create(leaf,false)" ) );
        assertTrue( m.getGraph() == graph );        
        }
        
    public void testOpen()
        {
        Model m = maker.openModel( "trunk" );
        checkHistory( one("open(trunk,false)" ) );
        assertTrue( m.getGraph() == graph );    
        }
        
    public void testOpenFalse()
        {
        Model m = maker.openModel( "branch", false );
        checkHistory( one("open(branch,false)" ) );
        assertTrue( m.getGraph() == graph );    
        }
        
    public void testOpenTrue()
        {
        Model m = maker.openModel( "bark", true );
        checkHistory( one("open(bark,true)" ) );
        assertTrue( m.getGraph() == graph );    
        }
        
    public void testListGraphs()
        {
        maker.listModels().close();
        checkHistory( one("listModels()" ) );    
        }
        
    public void testGetGraphMaker()
        {
        assertTrue( maker.getGraphMaker() == graphMaker );
        }
        
    public void testGetDescription()
        {
        maker.getDescription();
        checkHistory( one( "getDescription()" ) ); 
        }

    public void testModelSource()
        {
        assertTrue( hasAsParent( ModelMaker.class, ModelSource.class ) );
        assertTrue( hasAsParent( ModelSpec.class, ModelSource.class ) );
        ModelSource s = new ModelSourceImpl();
        assertNotNull( s.openModel( "henry" ) );
        assertNull( s.getExistingModel( "splendid" ) );
        }
    
    /**
     	Minimal test implementation of ModelSource. There should be more of
     	these.
     	
     	@author hedgehog
    */
    protected static class ModelSourceImpl implements ModelSource
    	{
        public Model openModel( String name )
            { return ModelFactory.createDefaultModel(); }

        public Model getExistingModel(String name)
            { return null; }
    	}

    private void checkHistory( List expected )
        { assertEquals( expected, history() ); }
        
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
            
        public ReificationStyle getReificationStyle()
            {
            history.add( "getReificationStyle()" );
            return null; 
            }
            
        public Graph getGraph()
            {
            history.add( "get()" );
            return graph;
            }
            
        public Graph createGraph()  
            {
            history.add( "create()" );
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
    
        public Graph getDescription()
            {
            history.add( "getDescription()" );
            return graphWith( "" );    
            }
                
        public Graph getDescription( Node root )
            {
            history.add( "getDescription(Node)" );
            return graphWith( "" );    
            }
            
        public Graph addDescription( Graph desc, Node self )
            {
            history.add( "addDescription()" );  
            return desc;  
            }
            
        public void close()
            {
            history.add( "close()" );
            }
            
        public ExtendedIterator listGraphs()
            {
            history.add( "listModels()" );
            return NullIterator.instance;    
            }
        }        
    }


/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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