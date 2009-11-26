/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import junit.framework.Assert ;
import junit.framework.JUnit4TestAdapter ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.algebra.opt.TransformFilterEquality ;
import com.hp.hpl.jena.sparql.algebra.opt.TransformFilterDisjunction ;
import com.hp.hpl.jena.sparql.algebra.opt.TransformFilterPlacement ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.StrUtils ;

public class TestFilterTransform
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(TestFilterTransform.class) ;
    }
    
    private Transform t_equality    = new TransformFilterEquality() ;
    private Transform t_disjunction = new TransformFilterDisjunction() ;
    private Transform t_placement   = new TransformFilterPlacement() ;
    
    @Test public void equality01()
    {
        test("(filter (= ?x <x>) (bgp ( ?s ?p ?x)) )",
             t_equality,
             "(assign ((?x <x>)) (bgp ( ?s ?p <x>)) )") ;
    }
    
    @Test public void equality02()
    {
        // Not safe on strings
        test("(filter (= ?x 'x') (bgp ( ?s ?p ?x)) )",
             t_equality,
             (String[])null) ;
    }

    @Test public void equality03()
    {
        // Not safe on numbers
        test("(filter (= ?x 123) (bgp ( ?s ?p ?x)) )",
             t_equality,
             (String[])null) ;
    }
    
    @Test public void equality04()
    {
        // Unused
        test("(filter (= ?UNUSED <x>) (bgp ( ?s ?p ?x)) )",
             t_equality,
             (String[])null) ;
    }
    
    
    @Test public void disjunction01()
    {
        test("(filter (|| (= ?x <x>) (= ?x <y>)) (bgp ( ?s ?p ?x)) )",
             t_disjunction,
             "(disjunction ",
               "(assign ((?x <x>)) (bgp ( ?s ?p <x>)))",
               "(assign ((?x <y>)) (bgp ( ?s ?p <y>)))",
             ")") ;
    }
    
    @Test public void disjunction02()
    {
        test("(filter (|| (= ?x <x>) (!= ?x <y>)) (bgp ( ?s ?p ?x)) )",
             t_disjunction,
             "(disjunction ",
               "(assign ((?x <x>)) (bgp ( ?s ?p <x>)))",
               "(filter (!= ?x <y>) (bgp ( ?s ?p ?x)))",
             ")") ;
    }
    
    @Test public void disjunction03()
    {
        test("(filter (|| (!= ?x <x>) (= ?x <y>)) (bgp ( ?s ?p ?x)) )",
             t_disjunction,
             // Note - reording of disjunction terms.
             "(disjunction ",
               "(assign ((?x <y>)) (bgp ( ?s ?p <y>)))",
               "(filter (!= ?x <x>) (bgp ( ?s ?p ?x)))",
             ")") ;
    }
    
    @Test public void disjunction04()
    {
        test("(filter (|| (!= ?x <y>) (!= ?x <x>)) (bgp ( ?s ?p ?x)) )",
             t_disjunction,
             (String[])null) ;
    }
    
    @Test public void disjunction05()
    {
        test("(filter (exprlist (|| (= ?x <y>) (!= ?x <x>)) (lang ?x))    (bgp ( ?s ?p ?x)) )",
             t_disjunction,
             "(filter (lang ?x)", 
             "  (disjunction",
             "    (assign ((?x <y>)) (bgp ( ?s ?p <y>)))",
             "    (filter (!= ?x <x>) (bgp ( ?s ?p ?x)))",
             "))") ;
    }
    
    @Test public void disjunction06()
    {
        test("(filter (exprlist (lang ?x) (|| (= ?x <y>) (!= ?x <x>)))    (bgp ( ?s ?p ?x)) )",
             t_disjunction,
             "(filter (lang ?x)", 
             "  (disjunction",
             "    (assign ((?x <y>)) (bgp ( ?s ?p <y>)))",
             "    (filter (!= ?x <x>) (bgp ( ?s ?p ?x)))",
             "))") ;
    }
    
    @Test public void placement01()
    {
        test("(filter (= ?x 1) (bgp ( ?s ?p ?x)))",
             t_placement,
             "(filter (= ?x 1) (bgp ( ?s ?p ?x)))") ;
        	
    }
    
    @Test public void placement02()
    {
        test("(filter (= ?x 1) (bgp (?s ?p ?x) (?s1 ?p1 ?x1) ))",
             t_placement,
             "(sequence (filter (= ?x 1) (bgp ( ?s ?p ?x))) (bgp (?s1 ?p1 ?x1)))") ;
            
    }

    @Test public void placement03()
    {
        test("(filter (= ?x 1) (bgp (?s ?p ?x) (?s1 ?p1 ?x) ))",
             t_placement,
             "(sequence (filter (= ?x 1) (bgp ( ?s ?p ?x))) (bgp (?s1 ?p1 ?x)))") ;
    }

    @Test public void placement04()
    {
        test("(filter (= ?XX 1) (bgp (?s ?p ?x) (?s1 ?p1 ?XX) ))",
             t_placement,
             "(filter (= ?XX 1) (bgp (?s ?p ?x) (?s1 ?p1 ?XX) ))") ;
    }
    
    @Test public void placement10()
    {
        // Unbound
        test("(filter (= ?x ?unbound) (bgp (?s ?p ?x)))",
             t_placement,
             "(filter (= ?x ?unbound) (bgp (?s ?p ?x)))") ;
    }
    
    @Test public void placement11()
    {
        Op op1 = SSE.parseOp("(filter (= ?x ?unbound) (bgp (?s ?p ?x)))") ;
        OpFilter f = (OpFilter)op1 ;
        Op op2 = TransformFilterPlacement.transform(f.getExprs(), ((OpBGP)f.getSubOp()).getPattern()) ;
        Op op3 = SSE.parseOp("(filter (= ?x ?unbound) (bgp (?s ?p ?x)))") ;
        Assert.assertEquals(op3, op2) ;
    }

    @Test public void placement12()
    {
        Op op1 = SSE.parseOp("(filter (= ?x ?unbound) (bgp (?s ?p ?x) (?s1 ?p1 ?XX)))") ;
        OpFilter f = (OpFilter)op1 ;
        Op op2 = TransformFilterPlacement.transform(f.getExprs(), ((OpBGP)f.getSubOp()).getPattern()) ;
        Op op3 = SSE.parseOp("(filter (= ?x ?unbound) (bgp (?s ?p ?x) (?s1 ?p1 ?XX)))") ;
        Assert.assertEquals(op3, op2) ;
    }
    

    static void test(String input, Transform transform, String... output)
    {
        Op op1 = SSE.parseOp(input) ;
        Op op2 = Transformer.transform(transform, op1) ;
        if ( output == null )
        {
            // No transformation.
            Assert.assertEquals(op1, op2) ;
            return ;
        }
        
        Op op3 = SSE.parseOp(StrUtils.strjoinNL(output)) ;
        Assert.assertEquals(op3, op2) ;
    }
    
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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