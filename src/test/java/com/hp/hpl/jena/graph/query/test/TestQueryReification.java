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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.GraphQuery;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import junit.framework.*;

public class TestQueryReification extends QueryTestBase
    {
    public TestQueryReification( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestQueryReification.class ); }
    
    public Graph getGraph()
        { return Factory.createDefaultGraph(); }
    
    public Graph getGraph( String triples )
        { return graphAdd( getGraph(), triples ); }
    
    public void testS()
        {  
        Graph g = geGraphSPO();
        GraphQuery q = new GraphQuery().addMatch( GraphQuery.X, RDF.Nodes.subject, GraphQuery.S );
        ExtendedIterator<Node> it = q.executeBindings( g, new Node[] {GraphQuery.X, GraphQuery.S} ).mapWith( select(1) );
        assertEquals( nodeSet( "S" ), it.toSet() );
        }
    public void testP()
        {  
        Graph g = geGraphSPO();
        GraphQuery q = new GraphQuery().addMatch( GraphQuery.X, RDF.Nodes.predicate, GraphQuery.P );
        ExtendedIterator<Node> it = q.executeBindings( g, new Node[] {GraphQuery.X, GraphQuery.P} ).mapWith( select(1) );
        assertEquals( nodeSet( "P" ), it.toSet() );
        }
    
    public void testO()
        {  
        Graph g = geGraphSPO();
        GraphQuery q = new GraphQuery().addMatch( GraphQuery.X, RDF.Nodes.object, GraphQuery.O );
        ExtendedIterator<Node> it = q.executeBindings( g, new Node[] {GraphQuery.X, GraphQuery.O} ).mapWith( select(1) );
        assertEquals( nodeSet( "O" ), it.toSet() );
        }
    
    protected Graph geGraphSPO()
        {
        return getGraph( "_x rdf:subject S; _x rdf:predicate P; _x rdf:object O; _x rdf:type rdf:Statement" );
        }
    
    }
