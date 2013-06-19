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

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.lang.LabelToNode ;
import org.apache.jena.riot.system.SyntaxLabels ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;

public class TestNodeAllocator extends BaseTest
{
    static Node gragh1 = NodeFactory.createURI("g1") ;
    static Node gragh2 = NodeFactory.createURI("g2") ;
    
    // ---- Glaobl default policy needed for RDF parsing
    @Test public void allocOneScope1()
    {
        LabelToNode alloc = SyntaxLabels.createLabelToNode() ;
        Node b1 = alloc.get(gragh1, "xyz" ) ;
        Node b2 = alloc.get(gragh1, "xyz" ) ;
        // SAME
        assertEquals(b1,b2) ;
    }
    
    @Test public void allocOneScope2()
    {
        LabelToNode alloc = SyntaxLabels.createLabelToNode() ;
        Node b1 = alloc.get(gragh1, "xyz" ) ;
        Node b2 = alloc.get(gragh1, "123" ) ;
        // DIFFERENT
        assertNotEquals(b1,b2) ;
    }

    @Test public void allocOneScope3()
    {
        LabelToNode alloc = SyntaxLabels.createLabelToNode() ;
        Node b1 = alloc.get(gragh1, "xyz" ) ;
        Node b2 = alloc.get(gragh2, "xyz" ) ;
        // SAME
        assertEquals(b1,b2) ;
    }
    
    @Test public void allocOneScope4()
    {
        LabelToNode alloc = SyntaxLabels.createLabelToNode() ;
        Node b1 = alloc.get(null,   "xyz" ) ;
        Node b2 = alloc.get(gragh2, "xyz" ) ;
        // SAME
        assertEquals(b1,b2) ;
    }
    
    @Test public void allocOneScope5()
    {
        LabelToNode alloc = SyntaxLabels.createLabelToNode() ;
        Node b1 = alloc.get(null, "xyz" ) ;
        Node b2 = alloc.get(null, "xyz" ) ;
        // SAME
        assertEquals(b1,b2) ;
    }
    
    // ---- Graph Scope
    @Test public void allocGraphScope1()
    {
        LabelToNode alloc = LabelToNode.createScopeByGraph() ;
        Node b1 = alloc.get(gragh1, "xyz" ) ;
        Node b2 = alloc.get(gragh1, "xyz" ) ;
        // SAME
        assertEquals(b1,b2) ;
        assertSame(b1,b2) ;
    }
    
    @Test public void allocGraphScope2()
    {
        LabelToNode alloc = LabelToNode.createScopeByGraph() ;
        Node b1 = alloc.get(gragh1, "xyz" ) ;
        Node b2 = alloc.get(gragh1, "123" ) ;
        // DIFFERENT
        assertNotEquals(b1,b2) ;
    }

    @Test public void allocGraphScope3()
    {
        LabelToNode alloc = LabelToNode.createScopeByGraph() ;
        Node b1 = alloc.get(gragh1, "xyz" ) ;
        Node b2 = alloc.get(gragh2, "xyz" ) ;
        // DIFFERENT
        assertNotEquals(b1,b2) ;
    }
    
    @Test public void allocGraphScope4()
    {
        LabelToNode alloc = SyntaxLabels.createLabelToNode() ;
        Node b1 = alloc.get(null,   "xyz" ) ;
        Node b2 = alloc.get(gragh2, "xyz" ) ;
        // DIFFERENT
        assertEquals(b1,b2) ;
    }
    
    @Test public void allocGraphScope5()
    {
        LabelToNode alloc = SyntaxLabels.createLabelToNode() ;
        Node b1 = alloc.get(null, "xyz" ) ;
        Node b2 = alloc.get(null, "xyz" ) ;
        // SAME
        assertEquals(b1,b2) ;
    }

}
