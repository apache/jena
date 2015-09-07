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

package org.apache.jena.sparql.join;

import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.query.ResultSetFactory ;
import org.apache.jena.sparql.algebra.Table ;
import org.apache.jena.sparql.algebra.TableFactory ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.join.JoinKey ;
import org.apache.jena.sparql.resultset.ResultSetCompare ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Assert ;
import org.junit.Test ;

public abstract class AbstractTestJoin extends Assert {
    static Var var_a = Var.alloc("a") ; 
    static Var var_b = Var.alloc("b") ; 
    static Var var_c = Var.alloc("c") ; 
    static Var var_d = Var.alloc("d") ; 

    static Table table0() { return parseTableInt("(table)") ; } 

    // For Mere, these must be in sort-joinkey order.

    private static Table parseTableInt(String... strings) {
        String x = StrUtils.strjoinNL(strings) ;
        return SSE.parseTable(x) ;
    }

    // Table of one row and no colums.
    static Table table1() { 
        return parseTableInt("(table (row))") ; }

    static Table tableD1() { 
        return parseTableInt("(table", 
                             "   (row (?a 1) (?b 2))",
                             "   (row (?a 1) (?b 3))",
                             "   (row (?a 1) (?b 2))",
            ")") ;
    }

    static Table tableD2() { 
        return parseTableInt("(table", 
                             "   (row (?a 0) (?d 8))",
                             "   (row (?a 1) (?c 9))",
            ")") ;
    }

    static Table tableD3() {
        return parseTableInt("(table", 
                             "   (row (?a 1) (?c 9) (?b 2))",
                             "   (row (?a 1) (?c 9) (?b 3))",
                             "   (row (?a 1) (?c 9) (?b 2))",
            ")") ;
    }

    static Table tableD4() {
        return parseTableInt("(table", 
                             "   (row (?a 1) (?b 2))",
                             "   (row (?a 1) (?b 3))",
                             "   (row (?a 4) (?b 4))",
                             "   (row (?a 4) (?b 5))",
            ")") ;
    }

    static Table tableD5() {
        return parseTableInt("(table", 
                             "   (row (?a 4) (?c 4))",
                             "   (row (?a 4) (?c 5))",
                             "   (row (?a 6) (?c 5))",
            ")") ;
    }

    static Table tableD6() {
        return parseTableInt("(table", 
                             "   (row (?a 1) (?c 2))",
                             "   (row (?a 1) (?c 3))",
                             "   (row (?a 4) (?c 4))",
                             "   (row (?a 4) (?c 5))",
            ")") ;
    }

    static Table tableD4x5() {
        return parseTableInt("(table", 
                             "   (row (?a 4) (?c 4) (?b 4))",
                             "   (row (?a 4) (?c 4) (?b 5))",
                             "   (row (?a 4) (?c 5) (?b 4))",
                             "   (row (?a 4) (?c 5) (?b 5))",
            ")") ;
    }

    static Table tableD4x6() {
        return parseTableInt("(table", 
                             "   (row (?a 1) (?c 2) (?b 2))",
                             "   (row (?a 1) (?c 2) (?b 3))",
                             "   (row (?a 1) (?c 3) (?b 2))",
                             "   (row (?a 1) (?c 3) (?b 3))",
                             "   (row (?a 4) (?c 4) (?b 4))",
                             "   (row (?a 4) (?c 4) (?b 5))",
                             "   (row (?a 4) (?c 5) (?b 4))",
                             "   (row (?a 4) (?c 5) (?b 5))",
            ")") ;
    }

    // Disjoint.
    static Table tableD8() {
        return parseTableInt("(table",
                             "  (row (?x 10))",
                             "  (row (?z 11))",
            ")") ; 
    }

    // Table8 crossproduct table2
    static Table tableD8x2() {
        return parseTableInt("(table",
                             "  (row (?a 0) (?d 8) (?z 11))",
                             "  (row (?a 0) (?d 8) (?x 10))",
                             "  (row (?a 1) (?c 9) (?z 11))",
                             "  (row (?a 1) (?c 9) (?x 10))",
            ")") ;
    }

    @Test public void join_00()  { testJoin("a", table0(), table0(), table0()) ; }
    @Test public void join_00a() { testJoin("a", table1(), table0(), table0()) ; }
    @Test public void join_00b() { testJoin("a", tableD1(), table1(), tableD1()) ; }
    @Test public void join_00c() { testJoin("z", tableD1(), table1(), tableD1()) ; }

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


    // Disjoint tables.
    @Test public void join_disjoint_01() { testJoin("a", tableD2(), tableD8(), tableD8x2()) ; }
    @Test public void join_disjoint_02() { testJoin("z", tableD2(), tableD8(), tableD8x2()) ; }

    private void testJoin(String var, Table left, Table right, Table tableOut) {
        JoinKey joinKey ;
        if ( var != null ) {
            if ( var.startsWith("?") )
                var = var.substring(1) ;
            joinKey = JoinKey.create(Var.alloc(var)) ;
        }
        else {
            // No vars in join key.  Legal, albeit silly.
            joinKey = new JoinKey.Builder().build() ;
        }

        testJoin1("1", joinKey, left, right, tableOut) ;
        // Commumute
        testJoin1("2", joinKey, right, left, tableOut) ;
    }

    private List<Binding> toList(Table table) {
        return Iter.toList(table.rows()) ;
    }

    private void testJoin1(String num, JoinKey joinKey, Table left, Table right, Table expectedResults) {
        Table x1 = joinMaterialize(joinKey, left, right) ;
        assertNotNull("Null table from join ("+num+")", x1) ;
        check("Results not equal ("+num+")", joinKey, left, right, expectedResults, x1) ;
    }

    private Table joinMaterialize(JoinKey joinKey, Table left, Table right) {
        QueryIterator qIter = join(joinKey , left , right) ;
        return TableFactory.create(qIter) ;
    }

    public abstract QueryIterator join(JoinKey joinKey , Table left , Table right) ;

    private static void check(String msg, JoinKey joinKey, Table left, Table right, Table expected, Table actual) {
        boolean b = equalTables(expected, actual) ;
        if ( ! b ) {
            System.out.println("Joinkey:  "+joinKey) ;
            System.out.println("Left:     "+left) ;
            System.out.println("Right:    "+right) ;
            System.out.println("Expected: "+expected) ;
            System.out.println("Actual:   "+actual) ;
            System.out.println() ;
        }

        assertTrue(msg, b) ;
    }

    private static boolean equalTables(Table table1, Table table2) {
        ResultSet rs1 =  ResultSetFactory.create(table1.iterator(null), table1.getVarNames()) ;
        ResultSet rs2 =  ResultSetFactory.create(table2.iterator(null), table2.getVarNames()) ;
        return ResultSetCompare.equalsByTerm(rs1, rs2) ;
    }

}


