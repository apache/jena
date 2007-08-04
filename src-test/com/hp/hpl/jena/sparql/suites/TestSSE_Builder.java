/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpNull;
import com.hp.hpl.jena.sparql.sse.SSE;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestSSE_Builder extends TestCase
{
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestSSE_Builder.class) ;
        ts.setName("SSE Op") ;
        return ts ;
    }
    
    public void test_01() { SSE.parseTriple("[triple ?s ?p ?o]") ; }
    public void test_02() { SSE.parseTriple("[?s ?p ?o]") ; }
    public void test_03() { SSE.parseTriple("[?s ?p ?o]") ; }
    public void test_04() { SSE.parseTriple("(?s ?p ?o)") ; }
    public void test_05() { SSE.parseQuad("(_ ?s ?p ?o)") ; }
    public void test_06() { SSE.parseQuad("(quad _ ?s ?p ?o)") ; }
    
    public void test_07() { SSE.parseExpr("1") ; }
    public void test_08() { SSE.parseExpr("(+ 1 2)") ; }
    
    public void testOp_01() { opSame("(null)") ; }
    public void testOp_02() { opSame("(null)", new OpNull()) ; }
    public void testOp_03() { opSame("(bgp [triple ?s ?p ?o])") ; }

    
    private static void opSame(String str)
    {
        opSame(str, SSE.parseOp(str)) ;
    }
    
    private static void opSame(String str , Op other)
    {
        Op op = SSE.parseOp(str) ;
        assertEquals(op, other) ;
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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