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

import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.rdf.model.*;
import junit.framework.*;

public class TestModelPolymorphism extends GraphTestBase
    {
    public static TestSuite suite()
        { return new TestSuite( TestModelPolymorphism.class ); }   
        
    public TestModelPolymorphism(String name)
        {
        super(name);
        }

    public void testPoly()
        {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource( "http://www.electric-hedgehog.net/a-o-s.html" );
        assertFalse( "the Resouce should not be null", r == null );
        assertTrue( "the Resource can be a Property", r.canAs( Property.class ) );
        Property p = r.as( Property.class );
        assertFalse( "the Property should not be null", p == null );
        assertFalse( "the Resource and Property should not be identical", r == p );
        }
    }
