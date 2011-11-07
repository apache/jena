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

import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.TestSuite;

/**
 	@author kers
*/
public class TestIterators extends GraphTestBase
    {
    public static TestSuite suite()
        { return new TestSuite( TestIterators.class ); }   
        
    public TestIterators(String name)
        { super(name); }

    /**
        bug detected in StatementIteratorImpl - next does not
        advance current, so remove doesn't work with next;
        this test should expose the bug.
    */
    public void testIterators()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource S = m.createResource( "S" );
        Property P = m.createProperty( "P" );
        RDFNode O = m.createResource( "O " );
        m.add( S, P, O );
        StmtIterator it = m.listStatements();
        it.next();
        it.remove();
        assertEquals( "", 0, m.size() );
        }
    }
