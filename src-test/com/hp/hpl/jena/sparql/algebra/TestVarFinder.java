/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.Arrays ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Set ;

import junit.framework.JUnit4TestAdapter ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.main.VarFinder ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestVarFinder extends BaseTest
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(TestVarFinder.class) ;
    }
    
    @Test public void varfind_01_1() { varfindFixed("(bgp (?s <p> <o>))", "s") ; }
    @Test public void varfind_01_2() { varfindOpt("(bgp (?s <p> <o>))") ; }
    @Test public void varfind_01_3() { varfindFilter("(bgp (?s <p> <o>))") ; }
    
    @Test public void varfind_02_1() { varfindFixed("(graph ?g (bgp (?s <p> <o>)))", "s", "g") ; }
    @Test public void varfind_02_2() { varfindOpt("(graph ?g (bgp (?s <p> <o>)))") ; }
    @Test public void varfind_02_3() { varfindFilter("(graph ?g (bgp (?s <p> <o>)))") ; }

    @Test public void varfind_03_1() { varfindFixed("(filter (?s) (bgp (?s <p> <o>)))", "s") ; }
    @Test public void varfind_03_2() { varfindOpt("(filter (?s) (bgp (?s <p> <o>)))") ; }
    @Test public void varfind_03_3() { varfindFilter("(filter (?s) (bgp (?s <p> <o>)))", "s") ; }

    @Test public void varfind_04_1() { varfindFixed("(leftjoin (bgp (?x <q> <v>)) (filter (?s) (bgp (?s <p> <o>))))", "x") ; }
    @Test public void varfind_04_2() { varfindOpt("(leftjoin (bgp (?x <q> <v>)) (filter (?s) (bgp (?s <p> <o>))))", "s") ; }
    @Test public void varfind_04_3() { varfindFilter("(leftjoin (bgp (?x <q> <v>)) (filter (?s) (bgp (?s <p> <o>))))", "s") ; }

    
    private static void varfindFixed(String string, String...vars)
    {
        varfind(string, vars, null, null) ;
    }

    private static void varfindOpt(String string, String...vars)
    {
        varfind(string, null, vars, null) ;
    }

    private static void varfindFilter(String string, String...vars)
    {
        varfind(string, null, null, vars) ;
    }

    private static void varfind(String string, String[] varsFixed, String [] varsOpt, String [] varsFilter)
    {
        Op op = SSE.parseOp(string) ;
        VarFinder vf = new VarFinder(op) ;
        if ( varsFixed != null ) check(varsFixed, vf.getFixed()) ;
        if ( varsOpt != null ) check(varsOpt, vf.getOpt()) ;
        if ( varsFilter != null ) check(varsFilter, vf.getFilter()) ;
    }

    private static void check(String[] varsExpected, Set<Var> varsFound)
    {
        Var[] vars = new Var[varsExpected.length] ;
        for ( int i = 0 ; i < varsExpected.length ; i++ )
        {
            Var v = Var.alloc(varsExpected[i]) ;
            vars[i] = v ;
        }
        
        List<Var> varList = Arrays.asList(vars) ;
        HashSet<Var> varSet = new HashSet<Var>() ;
        varSet.addAll(varList) ;
        assertEquals(varSet, varsFound) ;
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