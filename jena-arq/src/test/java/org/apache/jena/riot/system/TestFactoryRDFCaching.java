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

package org.apache.jena.riot.system;

import static org.junit.Assert. * ;
import org.apache.jena.graph.Node ;
import org.apache.jena.riot.lang.LabelToNode ;
import org.junit.Test ;

public class TestFactoryRDFCaching extends TestFactoryRDF {
 
    public TestFactoryRDFCaching() {
        super.factory = new FactoryRDFCaching(100, LabelToNode.createUseLabelAsGiven()) ;
    }
    
    @Test public void factory_cache_01() {
        Node n1 = factory.createStringLiteral("") ;
        Node n2 = factory.createStringLiteral("") ;
        assertSame(n1, n2); 
    }
    
    @Test public void factory_cache_02() {
        Node n1 = factory.createURI("http://test/n1") ;
        Node n2 = factory.createURI("http://test/n2") ;
        Node n3 = factory.createURI("http://test/n1") ;
        assertSame(n1, n3); 
    }
}


