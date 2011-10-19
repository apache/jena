/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestModelMakerImpl.java,v 1.1 2009-06-29 08:55:33 castagna Exp $
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

    @Override
    public void setUp()
        {
        graph = GraphTestBase.graphWith( "" );
        graphMaker = new MockGraphMaker( graph );
        maker = new ModelMakerImpl( graphMaker );
        }

    public void testClose()
        {
        maker.close();
        checkHistory( listOfOne( "close()") );
        }

    public void testRemove()
        {
        maker.removeModel( "London" );
        checkHistory( listOfOne( "remove(London)" ) );
        }

    public void testCreateFreshModel()
        {
        maker.createFreshModel();
        checkHistory( listOfOne( "create()" ) );
        }

    public void testCreateDefaultModel()
        {
        maker.createDefaultModel();
        checkHistory( listOfOne( "get()" ) );
        }

    public void testCreateNamed()
        {
        Model m = maker.createModel( "petal" );
        checkHistory( listOfOne("create(petal,false)" ) );
        assertTrue( m.getGraph() == graph );
        }

    public void testCreateTrue()
        {
        Model m = maker.createModel( "stem", true );
        checkHistory( listOfOne("create(stem,true)" ) );
        assertTrue( m.getGraph() == graph );
        }

    public void testCreateFalse()
        {
        Model m = maker.createModel( "leaf", false );
        checkHistory( listOfOne("create(leaf,false)" ) );
        assertTrue( m.getGraph() == graph );
        }

    public void testOpen()
        {
        Model m = maker.openModel( "trunk" );
        checkHistory( listOfOne("open(trunk,false)" ) );
        assertTrue( m.getGraph() == graph );
        }

    public void testOpenFalse()
        {
        Model m = maker.openModel( "branch", false );
        checkHistory( listOfOne("open(branch,false)" ) );
        assertTrue( m.getGraph() == graph );
        }

    public void testOpenTrue()
        {
        Model m = maker.openModel( "bark", true );
        checkHistory( listOfOne("open(bark,true)" ) );
        assertTrue( m.getGraph() == graph );
        }

    public void testListGraphs()
        {
        maker.listModels().close();
        checkHistory( listOfOne("listModels()" ) );
        }

    public void testGetGraphMaker()
        {
        assertTrue( maker.getGraphMaker() == graphMaker );
        }

    private void checkHistory( List<String> expected )
        { assertEquals( expected, history() ); }

    private List<String> history()
        { return ((MockGraphMaker) maker.getGraphMaker()).history; }

    static class MockGraphMaker implements GraphMaker
        {
        List<String> history = new ArrayList<String>();
        Graph graph;

        public MockGraphMaker( Graph graph )
            { this.graph = graph; }

        @Override
        public ReificationStyle getReificationStyle()
            {
            history.add( "getReificationStyle()" );
            return null;
            }

        @Override
        public Graph getGraph()
            {
            history.add( "get()" );
            return graph;
            }

        @Override
        public Graph createGraph()
            {
            history.add( "create()" );
            return graph;
            }

        @Override
        public Graph createGraph( String name, boolean strict )
            {
            history.add( "create(" + name + "," + strict + ")" );
            return graph;
            }

        @Override
        public Graph createGraph( String name )
            {
            history.add( "create(" + name + ")" );
            return graph;
            }

        @Override
        public Graph openGraph( String name, boolean strict )
            {
            history.add( "open(" + name + "," + strict + ")" );
            return graph;
            }

        @Override
        public Graph openGraph( String name )
            {
            history.add( "open(" + name + ")" );
            return graph;
            }

        @Override
        public void removeGraph( String name )
            {
            history.add( "remove(" + name + ")" );
            }

        @Override
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

        @Override
        public void close()
            {
            history.add( "close()" );
            }

        @Override
        public ExtendedIterator<String> listGraphs()
            {
            history.add( "listModels()" );
            return NullIterator.instance();
            }

        @Override
        public Graph openGraph()
            {
            
            return null;
            }
        }
    }


/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
