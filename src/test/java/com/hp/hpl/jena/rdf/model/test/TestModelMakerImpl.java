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

package com.hp.hpl.jena.rdf.model.test;

import java.util.ArrayList ;
import java.util.List ;

import junit.framework.TestSuite ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.GraphMaker ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.test.GraphTestBase ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelMaker ;
import com.hp.hpl.jena.rdf.model.impl.ModelMakerImpl ;
import com.hp.hpl.jena.shared.ReificationStyle ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.NullIterator ;

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
        @Deprecated
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
