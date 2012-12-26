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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.util.iterator.*;

import junit.framework.*;

public class TestGraphUtils extends GraphTestBase
    {
    public TestGraphUtils(String name)
        { super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestGraphUtils.class ); }
        
    private static class Bool 
        {
        boolean value;
        Bool( boolean value ) { this.value = value; }
        }
        
    public void testFindAll()
        {
        final Bool foundAll = new Bool( false );
        Graph mock = new GraphBase() 
            {
            @Override public ExtendedIterator<Triple> graphBaseFind( TripleMatch m )
                { 
                Triple t = m.asTriple();
                assertEquals( Node.ANY, t.getSubject() ); 
                assertEquals( Node.ANY, t.getPredicate() );
                assertEquals( Node.ANY, t.getObject() );
                foundAll.value = true;
                return null;
                }
            };
        GraphUtil.findAll( mock );
        assertTrue( "find(ANY, ANY, ANY) called", foundAll.value );
        }
    }
