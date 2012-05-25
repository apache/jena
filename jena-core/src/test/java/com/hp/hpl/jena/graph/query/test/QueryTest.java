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

import junit.framework.TestCase ;
import junit.framework.TestSuite ;

import com.hp.hpl.jena.graph.Factory ;
import com.hp.hpl.jena.graph.Graph ;

/**
    Test query over plain memory graphs.
*/

public class QueryTest extends TestCase
    {
	public QueryTest( String name )
		{ super( name ); }
		
    public static TestSuite suite()
    	{ 
        TestSuite result = new TestSuite();
        result.addTest( new TestSuite( TestQueryGraphMem.class ) );
        result.addTestSuite( TestQuery.class );
        result.setName(QueryTest.class.getName());
        return result;
        } 
    
    public static class TestQueryGraphMem extends AbstractTestQuery
        {
        public TestQueryGraphMem( String name ) { super( name ); }
        
        @Override public Graph getGraph() { return Factory.createGraphMem(); }       
        }
    }
