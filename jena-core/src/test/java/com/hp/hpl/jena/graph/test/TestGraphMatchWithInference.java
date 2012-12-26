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

package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.rdf.model.*;

import junit.framework.*;

/**
    Test that an inferred graph and an identical concrete graph 
    compare as equal.
*/

public class TestGraphMatchWithInference extends GraphTestBase
    {
    public TestGraphMatchWithInference( String name )
        { super( name ); }

   public static TestSuite suite()
        {
        TestSuite result = new TestSuite( TestGraphMatchWithInference.class );
        return result;
        }
        
   
    public void testBasic()
        {
        Model mrdfs = ModelFactory.createRDFSModel(ModelFactory.createDefaultModel());
        Model concrete = ModelFactory.createDefaultModel();
        concrete.add(mrdfs);
        
        assertIsomorphic( concrete.getGraph(),  mrdfs.getGraph() );
        
        assertIsomorphic( mrdfs.getGraph(), concrete.getGraph() );
        }
    
       
    }
