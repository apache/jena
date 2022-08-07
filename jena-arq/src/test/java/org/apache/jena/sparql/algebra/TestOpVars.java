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

package org.apache.jena.sparql.algebra;

import static org.junit.Assert.assertEquals;

import java.util.Arrays ;
import java.util.Collection ;
import java.util.HashSet ;
import java.util.List ;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Test ;

public class TestOpVars
{
    @Test public void opvars_visible_1() { visible("(bgp (?s :p ?o))", "s", "o") ; }
    @Test public void opvars_visible_2() { visible("(leftjoin (bgp (?s :p ?o)) (bgp (?s1 :p ?o1)) )", "s1", "o1", "s", "o") ; }
    @Test public void opvars_visible_3() { visible("(leftjoin (bgp (?s :p ?o)) (bgp (?s :p ?o)) )", "s", "o") ; }

    @Test public void opvars_visible_4() { visible("(project (?s) (bgp(?s :p ?o)))", "s") ; }
    @Test public void opvars_visible_5() { visible("(minus (bgp (?s :p ?o)) (bgp (?s1 :p ?o1)) )", "s", "o") ; }
    @Test public void opvars_visible_6() { visible("(join (project (?x) (bgp(?x :p ?z)))  (bgp(?s :p 1)) )", "x", "s") ; }
    @Test public void opvars_visible_7() { visible("(triple ?s :p ?o)", "s", "o") ; }
    @Test public void opvars_visible_8() { visible("(quad :g ?s :p ?o)", "s", "o") ; }

    @Test public void opvars_visible_9() {
        // JENA-2342
        String s = StrUtils.strjoinNL(
                                      "(group (?id) ((?.0 (count ?v1)) (?.1 (count ?v2)))",
                                      "        (table (vars ?id ?v1 ?v2)",
                                      "          (row [?id 'A'] [?v1 'B'] [?v2 'C'])",
                                      "        ))"
                                      );
        visible(s, "id", ".0", ".1") ;
    }

    @Test public void opvars_fixed_1() { fixed("(bgp (?s :p ?o))", "s", "o") ; }
    @Test public void opvars_fixed_2() { fixed("(leftjoin (bgp (?s :p ?o)) (bgp (?s1 :p ?o1)) )", "s", "o") ; }
    @Test public void opvars_fixed_3() { fixed("(leftjoin (bgp (?s :p ?o)) (bgp (?s :p ?o)) )", "s", "o") ; }

    @Test public void opvars_fixed_4() { fixed("(union (bgp (?s :p ?o1)) (bgp (?s :p ?o2)) )", "s") ; }
    @Test public void opvars_fixed_5() { fixed("(minus (bgp (?s :p ?o)) (bgp (?s1 :p ?o1)) )", "s", "o") ; }
    @Test public void opvars_fixed_6() { fixed("(join (project (?x) (bgp(?x :p ?z)))  (bgp(?s :p 1)) )", "x", "s") ; }
    @Test public void opvars_fixed_7() { fixed("(triple ?s :p ?o)", "s", "o") ; }
    @Test public void opvars_fixed_8() { fixed("(quad :g ?s :p ?o)", "s", "o") ; }
    @Test public void opvars_fixed_9() {
        // JENA-2342
        String s = StrUtils.strjoinNL(
                                      "(group (?id) ((?.0 (count ?v1)) (?.1 (count ?v2)))",
                                      "        (table (vars ?id ?v1 ?v2)",
                                      "          (row [?id 'A'] [?v1 'B'] [?v2 'C'])",
                                      "        ))"
                                      );
        fixed(s, "id", ".0", ".1") ;
    }

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

