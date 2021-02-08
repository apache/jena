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

package org.apache.jena.riot.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Node ;
import org.apache.jena.riot.system.RiotLib ;
import org.junit.Test ;

/**
 * IRI-related tests for thing other that RFC IRI parsing and processing.
 */
public class TestIRI
{
    @Test public void bNodeIRI_1()
    {
        Node n = RiotLib.createIRIorBNode("_:abc") ;
        assertTrue(n.isBlank()) ;
        assertEquals("abc", n.getBlankNodeLabel()) ;
    }

    @Test public void bNodeIRI_2()
    {
        Node n = RiotLib.createIRIorBNode("abc") ;
        assertTrue(n.isURI()) ;
        assertEquals("abc", n.getURI()) ;
    }
}
