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

package com.hp.hpl.jena.sparql.algebra;

import java.util.Arrays ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.main.VarFinder ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestVarFinder extends BaseTest
{
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
        HashSet<Var> varSet = new HashSet<>() ;
        varSet.addAll(varList) ;
        assertEquals(varSet, varsFound) ;
    }
    
}
