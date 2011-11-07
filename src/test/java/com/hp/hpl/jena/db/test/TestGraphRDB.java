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

package com.hp.hpl.jena.db.test;

import java.sql.SQLException;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import junit.framework.*;

/**
 	@author kers
*/
public class TestGraphRDB extends MetaTestGraph
    {
    public TestGraphRDB( String name )
        { super( name ); }
    
    public TestGraphRDB( Class<? extends Graph> graphClass, String name, ReificationStyle style ) 
        { super( graphClass, name, style ); }
        
    public static TestSuite suite()
        { return MetaTestGraph.suite( TestGraphRDB.class, LocalGraphRDB.class ); }

    private IDBConnection con;
    private int count = 0;
    private Graph properties;
    
    @Override public void setUp()
        { con = TestConnection.makeAndCleanTestConnection();
        properties = con.getDefaultModelProperties().getGraph(); }
        
    @Override public void tearDown() throws SQLException
        { con.close(); }
        
    public class LocalGraphRDB extends GraphRDB
        {
        public LocalGraphRDB( ReificationStyle style )
            { super( con, "testGraph-" + count ++, properties, styleRDB( style ), true ); }   
        } 
    
    protected final class GraphRDBWithoutFind extends GraphRDB
        {
        public GraphRDBWithoutFind()
            {
            super( con, "testGraph-" + count ++, properties, styleRDB( ReificationStyle.Minimal ), true );
            }

        @Override public ExtendedIterator<Triple> graphBaseFind( TripleMatch t )
            { throw new JenaException( "find is Not Allowed" ); }
        
        @Override public void performDelete( Triple t )
            { throw new JenaException( "delete is Not Allowed" ); }
        }
    
    public void testRemoveAllUsesClearNotDelete()
        {
        Graph g = new GraphRDBWithoutFind();
        graphAdd( g, "a P b; c Q d" );
        g.getBulkUpdateHandler().removeAll();
        assertEquals( 0, g.size() );
        }
    }
