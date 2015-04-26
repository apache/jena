/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.engine;

import static org.seaborne.dboe.engine.QJT.parseTableInt ;

import java.util.List ;
import java.util.Set ;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.junit.Assert ;
import org.junit.Test ;
import org.seaborne.dboe.engine.join.RowOrder ;

import org.apache.jena.sparql.core.Var ;

/** Simple stuff - includes answers, rather than comparing to InnerLoopJoinBase */
public abstract class AbstractTestJoin1 extends Assert {
    static Var var_a = Var.alloc("a") ; 
    static Var var_b = Var.alloc("b") ; 
    static Var var_c = Var.alloc("c") ; 
    static Var var_d = Var.alloc("d") ; 
    
    static RowList<Integer> table0() { return parseTableInt("(table)") ; } 
    
    // For Mere, these must be in sort-joinkey order.
    
    // Table of one row and no colums.
    static RowList<Integer> table1() { 
        return parseTableInt("(table (row))") ; }
    
    static RowList<Integer> tableD1() { 
        return parseTableInt("(table", 
                             "   (row (?a 1) (?b 2))",
                             "   (row (?a 1) (?b 3))",
                             "   (row (?a 1) (?b 2))",
                             ")") ;
    }
                                                
    static RowList<Integer> tableD2() { 
        return parseTableInt("(table", 
                             "   (row (?a 0) (?d 8))",
                             "   (row (?a 1) (?c 9))",
                             ")") ;
    }
    
    static RowList<Integer> tableD3() {
        return parseTableInt("(table", 
                             "   (row (?a 1) (?c 9) (?b 2))",
                             "   (row (?a 1) (?c 9) (?b 3))",
                             "   (row (?a 1) (?c 9) (?b 2))",
                             ")") ;
    }

    static RowList<Integer> tableD4() {
        return parseTableInt("(table", 
                             "   (row (?a 1) (?b 2))",
                             "   (row (?a 1) (?b 3))",
                             "   (row (?a 4) (?b 4))",
                             "   (row (?a 4) (?b 5))",
                             ")") ;
    }

    static RowList<Integer> tableD5() {
        return parseTableInt("(table", 
                             "   (row (?a 4) (?c 4))",
                             "   (row (?a 4) (?c 5))",
                             "   (row (?a 6) (?c 5))",
                             ")") ;
    }
    
    static RowList<Integer> tableD6() {
        return parseTableInt("(table", 
                             "   (row (?a 1) (?c 2))",
                             "   (row (?a 1) (?c 3))",
                             "   (row (?a 4) (?c 4))",
                             "   (row (?a 4) (?c 5))",
                             ")") ;
    }
    
    static RowList<Integer> tableD4x5() {
        return parseTableInt("(table", 
                             "   (row (?a 4) (?c 4) (?b 4))",
                             "   (row (?a 4) (?c 4) (?b 5))",
                             "   (row (?a 4) (?c 5) (?b 4))",
                             "   (row (?a 4) (?c 5) (?b 5))",
                             ")") ;
    }
        
        static RowList<Integer> tableD4x6() {
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
    static RowList<Integer> tableD8() {
        return parseTableInt("(table",
                             "  (row (?x 10))",
                             "  (row (?z 11))",
                             ")") ; 
    }
    
    // Table8 crossproduct table2
    static RowList<Integer> tableD8x2() {
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
    
    
    private void testJoin(String var, RowList<Integer> left, RowList<Integer> right, RowList<Integer> tableOut) {
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
        
        Set<Var> varsLeft = left.vars() ;
        Set<Var> varsRight = right.vars() ;
        Set<Var> varsResults = tableOut.vars();
        
        List<Row<Integer>> rowsLeft = left.toList() ;
        List<Row<Integer>> rowsRight = right.toList() ;    
        List<Row<Integer>> rowsResult = tableOut.toList() ;

        testJoin1("1", joinKey, rowsLeft, varsLeft, rowsRight, varsRight, rowsResult, varsResults) ;
        // Commumute
        testJoin1("2", joinKey, rowsRight, varsRight, rowsLeft, varsLeft, rowsResult, varsResults) ;
    }
    
    static RowOrder<Integer> comparator = new RowOrder<Integer>(){
        @Override
        public int compare(JoinKey joinKey, Row<Integer> row1, Row<Integer> row2) {
            if ( Join.compatible(row1, row2) )
                return 0 ;
            for ( Var v : joinKey ) {
                Integer x1 = row1.get(v) ; 
                Integer x2 = row2.get(v) ;
                if ( x1 == null ) 
                    throw new InternalErrorException("comparator: "+v+" : x1 is null") ;
                if ( x2 == null ) 
                    throw new InternalErrorException("comparator: "+v+" : x2 is null") ;
                int z = x1.compareTo(x2) ;
                if ( z != 0 )
                    return z ;
            }
            // But they were join compatible. 
            return 0 ; 
        }} ;
    
    private void testJoin1(String num, JoinKey joinKey, 
                           List<Row<Integer>> rowsLeft, Set<Var> varsLeft, 
                           List<Row<Integer>> rowsRight, Set<Var> varsRight, 
                           List<Row<Integer>> rowsResult,Set<Var> varsResults) {
        RowList<Integer> left = RowLib.createRowList(varsLeft, rowsLeft.iterator()) ;
        RowList<Integer> right = RowLib.createRowList(varsRight, rowsRight.iterator()) ;
        RowList<Integer> tableOut = RowLib.createRowList(varsResults, rowsResult.iterator()) ;
        
        RowList<Integer> x1 = join(joinKey, left, right, comparator) ;
        assertNotNull("Null rowlist from join ("+num+")", x1) ;
        List<Row<Integer>> rowsX1 =  x1.toList() ;
        x1 = RowLib.createRowList(x1.vars(), rowsX1.iterator()) ;
        check("Results not equal ("+num+")", joinKey, rowsLeft, rowsRight, rowsResult, rowsX1) ;
    }

    public abstract <X> RowList<X> join(JoinKey joinKey , RowList<X> left , RowList<X> right , RowOrder<X> comparator ) ;
    
    private static <X> void check(String msg, JoinKey joinKey, List<Row<X>> left, List<Row<X>> right, List<Row<X>> expected, List<Row<X>> actual) {
        boolean b = QJT.equal(expected, actual) ;
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

}
