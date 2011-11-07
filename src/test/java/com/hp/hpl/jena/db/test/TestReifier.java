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
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.shared.*;

import junit.framework.*;

/**
    Derived from the original reifier tests, and then folded back in by using an
    abstract test base class.
    @author kers, csayers.
*/

public class TestReifier extends AbstractTestReifier  {
    
    private int count;
    private Graph properties;
	private IDBConnection con;

	public TestReifier( String name ) 
        { super(name); }

    /** 
        Initialiser required for MetaTestGraph interface.
     */
    public TestReifier( Class<?> graphClass, String name, ReificationStyle style ) 
        { super( name ); }
        
	public static TestSuite suite() {
		return MetaTestGraph.suite( TestReifier.class, LocalGraphRDB.class );
	}
        
    /**
        LocalGraphRDB - an extension of GraphRDB that fixes the connection to 
        TestReifier's connection, passes in the appropriate reification style, uses the
        default properties of the connection, and gives each graph a new name
        exploiting the count.
    
    	@author kers
     */
    public class LocalGraphRDB extends GraphRDB
        {
        public LocalGraphRDB( ReificationStyle style )
            { super( con, "testGraph-" + count ++, properties, styleRDB( style ), true ); }   
        } 
        
	@Override
    public void setUp() 
        { con = TestConnection.makeAndCleanTestConnection(); 
        properties = con.getDefaultModelProperties().getGraph(); }

	@Override
    public void tearDown() throws Exception 
        { con.close(); }

    @Override
    public Graph getGraph( ReificationStyle style )
        { return new LocalGraphRDB( style ); }
    
    @Override
    public Graph getGraph()
        { return getGraph( ReificationStyle.Minimal ); }
        
    }
