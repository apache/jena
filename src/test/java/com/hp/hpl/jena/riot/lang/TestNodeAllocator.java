/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import org.junit.Test ;
import org.openjena.atlas.test.BaseTest ;

import com.hp.hpl.jena.graph.Node ;

public class TestNodeAllocator extends BaseTest
{
    static Node gragh1 = Node.createURI("g1") ;
    static Node gragh2 = Node.createURI("g2") ;
    
    // ---- One Scope
    @Test public void allocOneScope1()
    {
        LabelToNode alloc = LabelToNode.createScopeByDocument() ;
        Node b1 = alloc.get(gragh1, "xyz" ) ;
        Node b2 = alloc.get(gragh1, "xyz" ) ;
        // SAME
        assertEquals(b1,b2) ;
        assertSame(b1,b2) ;
    }
    
    @Test public void allocOneScope2()
    {
        LabelToNode alloc = LabelToNode.createScopeByDocument() ;
        Node b1 = alloc.get(gragh1, "xyz" ) ;
        Node b2 = alloc.get(gragh1, "123" ) ;
        // DIFFERENT
        assertNotEquals(b1,b2) ;
    }

    @Test public void allocOneScope3()
    {
        LabelToNode alloc = LabelToNode.createScopeByDocument() ;
        Node b1 = alloc.get(gragh1, "xyz" ) ;
        Node b2 = alloc.get(gragh2, "xyz" ) ;
        // SAME
        assertEquals(b1,b2) ;
    }
    
    @Test public void allocOneScope4()
    {
        LabelToNode alloc = LabelToNode.createScopeByDocument() ;
        Node b1 = alloc.get(null, "xyz" ) ;
        Node b2 = alloc.get(gragh2, "xyz" ) ;
        // SAME
        assertEquals(b1,b2) ;
    }
    
    @Test public void allocOneScope5()
    {
        LabelToNode alloc = LabelToNode.createScopeByDocument() ;
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
        LabelToNode alloc = LabelToNode.createScopeByDocument() ;
        Node b1 = alloc.get(null, "xyz" ) ;
        Node b2 = alloc.get(gragh2, "xyz" ) ;
        // DIFFERENT
        assertEquals(b1,b2) ;
    }
    
    @Test public void allocGraphScope5()
    {
        LabelToNode alloc = LabelToNode.createScopeByDocument() ;
        Node b1 = alloc.get(null, "xyz" ) ;
        Node b2 = alloc.get(null, "xyz" ) ;
        // SAME
        assertEquals(b1,b2) ;
    }

}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */