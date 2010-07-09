/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.HashSet ;
import java.util.Set ;

import junit.framework.JUnit4TestAdapter ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.main.VarRename ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestVarRename extends BaseTest
{

    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(TestVarRename.class) ;
    }

    @Test public void rename_01() { rename("(bgp (<s> <p> <o>))", "(bgp (<s> <p> <o>))") ; }
    @Test public void rename_02() { rename("(bgp (<s> ?p <o>))", "(bgp (<s> ?/p <o>))") ; }
    @Test public void rename_03() { rename("(bgp (?s ?p <o>))", "(bgp (?s ?/p <o>))", "s") ; }
    @Test public void rename_04() { rename("(filter (+ ?s ?x) (bgp (?s ?p <o>)))", "(filter (+ ?s ?/x) (bgp (?s ?/p <o>)))", "s") ; }

    @Test public void rename_05() { rename("(group ((?.1 (str ?x))) ((?.0 (count))) (bgp (triple ?x :p ?v)))",
                                           "(group ((?/.1 (str ?x))) ((?/.0 (count))) (bgp (triple ?x :p ?/v)))", "x" ) ; }

    @Test public void rename_06() { rename("(assign ((?x (+ ?/a ?/b))) (table unit))", 
                                           "(assign ((?/x (+ ?//a ?//b))) (table unit))") ; }
    @Test public void rename_07() { rename("(assign ((?x (+ ?/a ?/b))) (table unit))", 
                                           "(assign ((?/x (+ ?/a ?//b))) (table unit))",
                                           "/a") ; }
    
    private static void rename(String string, String string2, String... varNames)
    {
        Set<Var> s = new HashSet<Var>() ;
        for ( String vn : varNames )
            s.add(Var.alloc(vn)) ;
        rename(string,string2, s) ;
    }

    private static void rename(String string, String string2,  Set<Var> constant)
    {
        Op op = SSE.parseOp(string) ;
        Op op2 = SSE.parseOp(string2) ;
        Op op3 = VarRename.rename(op, constant) ;
        assertEquals(op2, op3) ;
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