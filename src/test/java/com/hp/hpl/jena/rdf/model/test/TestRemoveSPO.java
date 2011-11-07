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

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.WrappedGraph;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

import junit.framework.TestSuite;

public class TestRemoveSPO extends ModelTestBase
    {
    public TestRemoveSPO( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestRemoveSPO.class ); }
    
    public void testRemoveSPOReturnsModel()
        {
        Model m = new ModelCom( Factory.createDefaultGraph() );
        assertSame( m, m.remove( resource( "R" ), property( "P" ), rdfNode( m, "17" ) ) );
        }
    
    public void testRemoveSPOCallsGraphDeleteTriple()
        {
        Graph base = Factory.createDefaultGraph();
        final List<Triple> deleted = new ArrayList<Triple>();
        Graph wrapped = new WrappedGraph( base )
            { @Override
            public void delete( Triple t ) { deleted.add( t ); } };
        Model m = new ModelCom( wrapped );
        m.remove( resource( "R" ), property( "P" ), rdfNode( m, "17" ) );
        assertEquals( listOfOne( NodeCreateUtils.createTriple( "R P 17" ) ), deleted );
        }
    }
