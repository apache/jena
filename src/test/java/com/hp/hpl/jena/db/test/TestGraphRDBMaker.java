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

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.db.impl.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.shared.*;

import junit.framework.*;

/**
 	@author hedgehog
    
    Test the RDB graph factory, based on the abstract test class. We track the
    current graph factory so that we can discard all the graphs we create during
    the test.
*/

public class TestGraphRDBMaker extends AbstractTestGraphMaker
    {
    /**
         The connection for the graph factory.
    */
    IDBConnection connection;
    
    public TestGraphRDBMaker( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestGraphRDBMaker.class ); }
        
    @Override
    public void setUp()
        { // order is import - super.setUp grabs a graph 
        connection = TestConnection.makeAndCleanTestConnection();
        super.setUp();
        }

    /**
        The current factory object, or null when there isn't one.
     */
    private GraphRDBMaker current;
    
    /**
        Invent a new factory on the connection, record it, and return it.    
    */
    @Override
    public GraphMaker getGraphMaker()
        { return current = new GraphRDBMaker( connection, ReificationStyle.Minimal ); }    
        
    /**
        Run the parent teardown, and then remove all the freshly created graphs.
     * @throws  
    */
    @Override
    public void tearDown()
        {
        super.tearDown();
        if (current != null) current.removeAll();
        try { connection.close(); } catch (Exception e) { throw new JenaException( e ); }
        }
    }
