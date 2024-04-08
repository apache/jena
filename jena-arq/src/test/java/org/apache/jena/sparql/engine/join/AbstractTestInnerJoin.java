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

package org.apache.jena.sparql.engine.join;

import org.apache.jena.sparql.algebra.Table ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.ExprList ;
import org.junit.Test ;

/** Tests for inner/equi joins */
public abstract class AbstractTestInnerJoin extends AbstractTestJoin {

    @Override
    protected void executeTest(JoinKey joinKey, Table left, Table right, ExprList conditions, Table expectedResults) {
        if ( conditions != null )
            fail("Conditions on inner join are meaningless (currently)") ;
        // No conditions.
        // Commutative.
        executeTestJoin("1", joinKey, left, right, null, expectedResults) ;
        executeTestJoin("2", joinKey, right, left, null, expectedResults) ;
    }

    @Test public void join_basic_1()    { testJoin("a", table0(), table0(), table0()) ; }
    @Test public void join_basic_2()    { testJoin("a", table1(), table0(), table0()) ; }
    @Test public void join_basic_3()    { testJoin("a", tableD1(), table1(), tableD1()) ; }
    @Test public void join_basic_4()    { testJoin("z", tableD1(), table1(), tableD1()) ; }

    @Test public void join_basic_5() { testJoin("a", table0(), table1(), table0()) ; }
    @Test public void join_basic_6() { testJoin("a", table1(), table0(), table0()) ; }

    @Test public void join_01() { testJoin("a", table0(), tableD2(), table0()) ; }
    @Test public void join_02() { testJoin("a", tableD1(), table0(), table0()) ; }
    @Test public void join_03() { testJoin("a", tableD1(), tableD2(), tableD3()) ; }

    // Identity.
    @Test public void join_04() { testJoin("a", tableD2(), table1(), tableD2()) ; }
    @Test public void join_05() { testJoin("a", table1(), tableD2(), tableD2()) ; }
    // Identity, keymiss
    @Test public void join_06() { testJoin("z", table1(), tableD2(), tableD2()) ; }
    @Test public void join_07() { testJoin("z", tableD2(), table1(), tableD2()) ; }

    @Test public void join_08() { testJoin("a", tableD4(), tableD5(), tableD4x5()) ; }
    @Test public void join_09() { testJoin("a", tableD5(), tableD4(), tableD4x5()) ; }

    @Test public void join_10() { testJoin("a", tableD4(), tableD6(), tableD4x6()) ; }
    @Test public void join_11() { testJoin("a", tableD6(), tableD4(), tableD4x6()) ; }

    // Not the right join key - should still work albeit less efficiently.
    @Test public void join_12() { testJoin("z", tableD1(), tableD2(), tableD3()) ; }
    @Test public void join_13() { testJoin("z", tableD2(), tableD1(), tableD3()) ; }

    // No key.
    @Test public void join_14() { testJoin(null, tableD1(), tableD2(), tableD3()) ; }

    @Test public void join_skew_01() { testJoin("x", tableS1(), tableS2(), tableS1J2()) ; }
    @Test public void join_skew_02() { testJoin("w", tableS1(), tableS2(), tableS1J2()) ; }
    @Test public void join_skew_03() { testJoin(null, tableS1(), tableS2(), tableS1J2()) ; }

    // Skew tests where the order of the two bindings in tableS1 is swapped.
    @Test public void join_skew_01b() { testJoin("x", tableS1b(), tableS2(), tableS1J2()) ; }
    @Test public void join_skew_02b() { testJoin("w", tableS1b(), tableS2(), tableS1J2()) ; }
    @Test public void join_skew_03b() { testJoin(null, tableS1b(), tableS2(), tableS1J2()) ; }


    @Test
    public void join_skew_04() {
        JoinKey joinKey = new JoinKey.Builder()
            .add(Var.alloc("x"))
            .add(Var.alloc("w"))
            .build() ;
        testJoinWithKey(joinKey, tableS1(), tableS2(), tableS1J2()) ;
    }

    @Test
    public void join_skew_05() {
        Table in = parseTableInt("""
            (table
              (row (?x undef) (?y 0))
              (row (?x     0) (?y 0))
            )""");

        Table expected = parseTableInt("""
            (table
              (row (?x undef) (?y 0))
              (row (?x     0) (?y 0))
              (row (?x     0) (?y 0))
              (row (?x     0) (?y 0))
            )""");

        testJoin(null, in, in, expected) ;
    }

    // Disjoint tables.
    @Test public void join_disjoint_01() { testJoin("a", tableD2(), tableD8(), tableD8x2()) ; }
    @Test public void join_disjoint_02() { testJoin("z", tableD2(), tableD8(), tableD8x2()) ; }
    @Test public void join_disjoint_03() { testJoin(null, tableD2(), tableD8(), tableD8x2()) ; }
}


