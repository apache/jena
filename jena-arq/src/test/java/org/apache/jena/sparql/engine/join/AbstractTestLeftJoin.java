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

package org.apache.jena.sparql.engine.join;

import org.junit.jupiter.api.Test;

import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.sse.SSE;

public abstract class AbstractTestLeftJoin extends AbstractTestJoin {

    @Override
    protected void executeTest(JoinKey joinKey, Table left, Table right, ExprList conditions, Table expectedResults) {
        executeTestJoin("LJ", joinKey, left, right, conditions, expectedResults);
    }

    @Test public void leftjoin_basic_1()    { testJoin("a", table0(), table0(), table0()); }
    @Test public void leftjoin_basic_2()    { testJoin("a", table1(), table0(), table1()); }
    @Test public void leftjoin_basic_3()    { testJoin("a", tableD1(), table1(), tableD1()); }
    @Test public void leftjoin_basic_4()    { testJoin("z", tableD1(), table1(), tableD1()); }

    @Test public void leftjoin_01()         { testJoin("a", tableL1(), tableL2(), table1LJ2()); }
    @Test public void leftjoin_02()         { testJoin("a", tableL4(), tableL5(), table4LJ5()); }

    @Test public void leftjoin_03()         { testJoin("z", tableL1(), tableL2(), table1LJ2()); }
    @Test public void leftjoin_04()         { testJoin("z", tableL4(), tableL5(), table4LJ5()); }

    @Test public void leftjoin_J01()        { testJoin("a", tableD1(), table1(), tableD1()); }
    @Test public void leftjoin_J01a()       { testJoin("z", tableD1(), table1(), tableD1()); }

    @Test public void leftjoin_J02()        { testJoin("a", tableD1(), table0(), tableD1()); }
    @Test public void leftjoin_J03()        { testJoin("a", tableD1(), tableD2(), tableD3()); }

    // Identity.
    @Test public void leftjoin_J04()        { testJoin("a", tableD2(), table1(), tableD2()); }
    @Test public void leftjoin_J05()        { testJoin("a", table1(), tableD2(), tableD2()); }
    // Identity, keymiss
    @Test public void leftjoin_J06()        { testJoin("z", table1(), tableD2(), tableD2()); }
    @Test public void leftjoin_J07()        { testJoin("z", tableD2(), table1(), tableD2()); }

    @Test public void leftjoin_J08()        { testJoin("a", tableD4(), tableD5(), tableD4x5_LJ()); }
    @Test public void leftjoin_J09()        { testJoin("a", tableD5(), tableD4(), tableD5x4_LJ()); }

    @Test public void leftjoin_J10()        { testJoin("a", tableD4(), tableD6(), tableD4x6()); }
    @Test public void leftjoin_J11()        { testJoin("a", tableD6(), tableD4(), tableD4x6()); }

    // Not the right join key - should still work albeit less efficiently.
    @Test public void leftjoin_J12()        { testJoin("z", tableD1(), tableD2(), tableD3()); }
    @Test public void leftjoin_J13()        { testJoin("z", tableD2(), tableD1(), tableD3_LJ()); }

    // No key.
    @Test public void leftjoin_14()         { testJoin(null, tableD1(), tableD2(), tableD3()); }

    // Disjoint tables.
    @Test public void leftjoin_disjoint_01() { testJoin("a", tableD2(), tableD8(), tableD8x2()); }
    @Test public void leftjoin_disjoint_02() { testJoin("z", tableD2(), tableD8(), tableD8x2()); }

    // Conditions.
    @Test public void leftjoin_condition_01() {
        Table tableD1c = parseTableInt("(table",
                                       "   (row (?a 1) (?b 3))",
                                       ")");
        testJoin("a", table1(), tableD1(), "((= ?b 3))", tableD1c);
    }


    @Test public void leftjoin_condition_02() {
        Table tableD3_LJc = parseTableInt("(table",
                                       "   (row (?d 8) (?a 0))",
                                       "   (row (?a 1) (?c 9) (?b 2))",
                                       "   (row (?a 1) (?c 9) (?b 2))",
                                       ")");
        testJoin("a", tableD2(), tableD1(), "((= ?a 1) (= ?b 2))", tableD3_LJc);
    }

    @Test public void leftjoin_condition_03() {
        // Never match
        ExprList exprs = SSE.parseExprList("((= ?b 99))");
        testJoin("a", table1(), tableD1(), "((= ?b 99))", table1());
    }
}


