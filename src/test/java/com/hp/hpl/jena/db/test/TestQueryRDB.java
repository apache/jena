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

import com.hp.hpl.jena.graph.query.test.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.shared.*;

import junit.framework.*;

/**
    Apply the abstract query tests to an RDB graph.
 	@author kers
*/
public class TestQueryRDB extends AbstractTestQuery
    {
    public TestQueryRDB( String name )
        { super( name ); }

	public static TestSuite suite()
        { return new TestSuite( TestQueryRDB.class ); }     
     
    private IDBConnection theConnection;
    private int count = 0;
    
    @Override
    public void setUp()
        {
        theConnection = TestConnection.makeAndCleanTestConnection();
        super.setUp();
        }
        
    @Override
    public void tearDown()
        {
        try { theConnection.close(); }
        catch (Exception e) { throw new JenaException( e ); }
        }
        
    @Override
    public Graph getGraph()
        { 
        return new GraphRDB
            (
            theConnection,
            "testGraph-" + count ++, 
            theConnection.getDefaultModelProperties().getGraph(), 
            GraphRDB.OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS, 
            true
            );
        }

    }
