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

package com.hp.hpl.jena.n3.turtle;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;

import junit.framework.TestCase;
import junit.framework.TestSuite;


public class TestTurtleReader extends TestCase
{
    
    public static TestSuite suite()
    {
        return new TestSuite(TestTurtleReader.class, "Basic Turtle tests") ;
    }
    
    public void test0()
    {
        RDFReader r = ModelFactory.createDefaultModel().getReader("Turtle") ;
        assertNotNull(r) ;
    }
    
    public void test1()
    {
        Model m = ModelFactory.createDefaultModel() ;
        m.read("file:testing/Turtle/simple.ttl", "TTL") ;
        assertFalse(m.isEmpty()) ;
    }
       
    public void test2()
    {
        Model m = ModelFactory.createDefaultModel() ;
        m.read("file:testing/Turtle/i18n.ttl", "TTL") ;
        assertFalse(m.isEmpty()) ;
    }
}
