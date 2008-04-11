/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb;

import static com.hp.hpl.jena.tdb.lib.NodeLib.*;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.sse.SSE;

import junit.TestBase;
import org.junit.Test;

public class TestNodeLib extends TestBase
{
    @Test public void hash1() 
    {
        Node x1 = SSE.parseNode("<http://example/x>") ;
        Node x2 = SSE.parseNode("<http://example/x>") ;
        assertEquals(hash(x1), hash(x2)) ;
    }
    
    @Test public void hash2() 
    {
        Node x1 = SSE.parseNode("<http://example/x1>") ;
        Node x2 = SSE.parseNode("<http://example/x2>") ;
        assertNotEquals(hash(x1), hash(x2)) ;
    }
    
    @Test public void hash3() 
    {
        Node x1 = SSE.parseNode("<lex>") ;
        Node x2 = SSE.parseNode("'lex'") ;
        Node x3 = SSE.parseNode("_:lex") ;
        assertNotEquals(hash(x1), hash(x2)) ;
        assertNotEquals(hash(x2), hash(x3)) ;
        assertNotEquals(hash(x3), hash(x1)) ;
    }
    
    @Test public void hash4() 
    {
        Node x1 = SSE.parseNode("123") ;
        Node x2 = SSE.parseNode("'123'") ;
        assertNotEquals(hash(x1), hash(x2)) ;
    }

    @Test public void hash5() 
    {
        Node x1 = SSE.parseNode("123") ;
        Node x2 = SSE.parseNode("123.0") ;
        Node x3 = SSE.parseNode("123e0") ;
        assertNotEquals(hash(x1), hash(x2)) ;
        assertNotEquals(hash(x2), hash(x3)) ;
        assertNotEquals(hash(x3), hash(x1)) ;
    }

}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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