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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.shared.*;

import java.util.*;

import junit.framework.*;

/**
    Apply the abstract query tests to an RDB graph.
 	@author kers
*/
public class TestQuery1 extends AbstractTestQuery1
    {
    public TestQuery1( String name )
        { super( name ); }

	public static TestSuite suite()
        { return new TestSuite( TestQuery1.class ); }     
       
    private IDBConnection theConnection;
    private int count = 0;
    
    private List<GraphRDB> graphs;
    
    @Override
    public void setUp() throws Exception
        {
        theConnection = TestConnection.makeTestConnection();
        graphs = new ArrayList<GraphRDB>();
        super.setUp();
        }
        
    @Override
    public void tearDown() throws Exception
        {
        removeGraphs();
        theConnection.close(); 
        super.tearDown(); 
        }
        
    private void removeGraphs()
        { for (int i = 0; i < graphs.size(); i += 1) graphs.get(i).remove(); }

	@Override
    public Graph getGraph ( ) {
		return getGraph( ReificationStyle.Minimal );
	}
        
    @Override
    public Graph getGraph ( ReificationStyle style )
        { 
        String name = "jena-test-rdb-TestQuery1-" + count ++;
        if (theConnection.containsModel( name )) makeGraph( name, false, style ).remove();
        GraphRDB result = makeGraph( name, true, style );
        graphs.add( result );    
        return result;
        }
        
    protected GraphRDB makeGraph( String name, boolean fresh, ReificationStyle style )
        { return new GraphRDB
            (
            theConnection,
            name, 
            theConnection.getDefaultModelProperties().getGraph(),
            GraphRDB.styleRDB( style ), 
            fresh
            ); }

    }
