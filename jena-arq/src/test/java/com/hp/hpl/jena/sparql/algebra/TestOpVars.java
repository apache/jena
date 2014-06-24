/**
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
import java.util.Collection ;
import java.util.HashSet ;
import java.util.List ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestOpVars extends BaseTest
{
    @Test public void opvars_01() { visible("(bgp (?s :p ?o))", "s", "o") ; }
    @Test public void opvars_02() { visible("(leftjoin (bgp (?s :p ?o)) (bgp (?s1 :p ?o1)) )", "s1", "o1", "s", "o") ; }
    @Test public void opvars_03() { visible("(leftjoin (bgp (?s :p ?o)) (bgp (?s :p ?o)) )", "s", "o") ; }
    
    @Test public void opvars_04() { visible("(project (?s) (bgp(?s :p ?o)))", "s") ; }
    @Test public void opvars_05() { visible("(minus (bgp (?s :p ?o)) (bgp (?s1 :p ?o1)) )", "s", "o") ; }
    @Test public void opvars_06() { visible("(join (project (?x) (bgp(?x :p ?z)))  (bgp(?s :p 1)) )", "x", "s") ; }
    
    
    @Test public void opvars_10() { fixed("(bgp (?s :p ?o))", "s", "o") ; }
    @Test public void opvars_11() { fixed("(leftjoin (bgp (?s :p ?o)) (bgp (?s1 :p ?o1)) )", "s", "o") ; }
    @Test public void opvars_12() { fixed("(leftjoin (bgp (?s :p ?o)) (bgp (?s :p ?o)) )", "s", "o") ; }
    
    @Test public void opvars_13() { fixed("(union (bgp (?s :p ?o1)) (bgp (?s :p ?o2)) )", "s") ; }
    @Test public void opvars_14() { fixed("(minus (bgp (?s :p ?o)) (bgp (?s1 :p ?o1)) )", "s", "o") ; }
    @Test public void opvars_15() { fixed("(join (project (?x) (bgp(?x :p ?z)))  (bgp(?s :p 1)) )", "x", "s") ; }
    
    
    private static void visible(String string, String... vars)
    {
        Op op = SSE.parseOp(string) ;
        Collection<Var> c = OpVars.visibleVars(op) ;
        check(vars, c) ;
    }
    
    private static void fixed(String string, String... vars)
    {
        Op op = SSE.parseOp(string) ;
        Collection<Var> c = OpVars.fixedVars(op) ;
        check(vars, c) ;
    }
    

    private static void check(String[] varsExpected, Collection<Var> varsFound)
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

