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

package org.seaborne.dboe.engine.join2;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Assert ;
import org.seaborne.dboe.engine.JoinKey ;
import org.seaborne.dboe.engine.QJT;
import org.seaborne.dboe.engine.RowList ;

/** Tests for inner/equi joins */ 
public abstract class AbstractTestJoin extends Assert {

    protected static RowList<Integer> table0() { return parseTableInt("(table)") ; } 

    // RowList<Integer> of one row and no colums.
    protected static RowList<Integer> table1() { 
        return parseTableInt("(table (row))") ; }

    protected static RowList<Integer> tableD1() { 
        return parseTableInt("(table", 
                             "   (row (?a 1) (?b 2))",
                             "   (row (?a 1) (?b 3))",
                             "   (row (?a 1) (?b 2))",
            ")") ;
    }

    protected static RowList<Integer> tableD2() { 
        return parseTableInt("(table", 
                             "   (row (?a 0) (?d 8))",
                             "   (row (?a 1) (?c 9))",
            ")") ;
    }

    protected static RowList<Integer> tableD3() {
        return parseTableInt("(table",
                             "   (row (?a 1) (?c 9) (?b 2))",
                             "   (row (?a 1) (?c 9) (?b 3))",
                             "   (row (?a 1) (?c 9) (?b 2))",
            ")") ;
    }

    protected static RowList<Integer> tableD3_LJ() {
        return parseTableInt("(table", 
                             "   (row (?d 8) (?a 0))",
                             "   (row (?a 1) (?c 9) (?b 2))",
                             "   (row (?a 1) (?c 9) (?b 3))",
                             "   (row (?a 1) (?c 9) (?b 2))",
                             
            ")") ;
    }

    protected static RowList<Integer> tableD4() {
        return parseTableInt("(table", 
                             "   (row (?a 1) (?b 2))",
                             "   (row (?a 1) (?b 3))",
                             "   (row (?a 4) (?b 4))",
                             "   (row (?a 4) (?b 5))",
            ")") ;
    }

    protected static RowList<Integer> tableD5() {
        return parseTableInt("(table", 
                             "   (row (?a 4) (?c 4))",
                             "   (row (?a 4) (?c 5))",
                             "   (row (?a 6) (?c 5))",
            ")") ;
    }

    protected static RowList<Integer> tableD6() {
        return parseTableInt("(table", 
                             "   (row (?a 1) (?c 2))",
                             "   (row (?a 1) (?c 3))",
                             "   (row (?a 4) (?c 4))",
                             "   (row (?a 4) (?c 5))",
            ")") ;
    }

    protected static RowList<Integer> tableD4x5() {
        return parseTableInt("(table", 
                             "   (row (?a 4) (?c 4) (?b 4))",
                             "   (row (?a 4) (?c 4) (?b 5))",
                             "   (row (?a 4) (?c 5) (?b 4))",
                             "   (row (?a 4) (?c 5) (?b 5))",
            ")") ;
    }

    protected static RowList<Integer> tableD4x5_LJ() {
        return parseTableInt("(table",
                             "   (row (?a 4) (?c 4) (?b 4))",
                             "   (row (?a 4) (?c 4) (?b 5))",
                             "   (row (?a 4) (?c 5) (?b 4))",
                             "   (row (?a 4) (?c 5) (?b 5))",
                             "   (row (?b 2) (?a 1))",
                             "   (row (?b 3) (?a 1))",
            ")") ;
    }

    protected static RowList<Integer> tableD5x4_LJ() {
        return parseTableInt("(table", 
                             "   (row (?a 4) (?c 4) (?b 4))",
                             "   (row (?a 4) (?c 4) (?b 5))",
                             "   (row (?a 4) (?c 5) (?b 4))",
                             "   (row (?a 4) (?c 5) (?b 5))",
                             "   (row (?a 6) (?c 5))",
            ")") ;
    }

    protected static RowList<Integer> tableD4x6() {
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
    protected static RowList<Integer> tableD8() {
        return parseTableInt("(table",
                             "  (row (?x 10))",
                             "  (row (?z 11))",
            ")") ; 
    }

    // Table8 crossproduct table2
    protected static RowList<Integer> tableD8x2() {
        return parseTableInt("(table",
                             "  (row (?a 0) (?d 8) (?z 11))",
                             "  (row (?a 0) (?d 8) (?x 10))",
                             "  (row (?a 1) (?c 9) (?z 11))",
                             "  (row (?a 1) (?c 9) (?x 10))",
            ")") ;
    }
    
    // Left join data tables.
    protected static RowList<Integer> tableL1() {
        return parseTableInt("(table",
                             "  (row (?a 0) (?d 8))",
                             "  (row (?a 3) (?d 9))",
            ")") ;
    }

    protected static RowList<Integer> tableL2() {
        return parseTableInt("(table",
                             "  (row (?a 0) (?z 11))",
                             "  (row (?a 1) (?c 9) (?z 11))",
            ")") ;
    }

    // L3 := L1 leftjoin L2 
    protected static RowList<Integer> table1LJ2() {
        return parseTableInt("(table",
                             "  (row (?a 0) (?d 8) (?z 11))",
                             "  (row (?a 3) (?d 9))",
            ")") ;
    }
    
    protected static RowList<Integer> tableL4() {
        return parseTableInt("(table",
                             "  (row (?a 0) (?z 11))",
                             "  (row (?a 0) (?z 12))",
                             "  (row               (?r 99))",
                             "  (row        (?c 9) (?z 11))",
            ")") ;
    }

    protected static RowList<Integer> tableL5() {
        return parseTableInt("(table",
                             "  (row (?a 0) (?d 8))",
                             "  (row (?a 1) (?c 9) (?z 11))",
            ")") ;
    }

    // L3 := L1 leftjoin L2 
    protected static RowList<Integer> table4LJ5() {
        return parseTableInt("(table",
                             "  (row (?a 0) (?d 8) (?z 11))",
                             "  (row (?a 0) (?d 8) (?z 12))",
                             
                             "  (row (?a 0) (?d 8) (?r 99))",
                             "  (row (?a 1) (?c 9) (?z 11) (?r 99))",
                             
                             "  (row (?a 0) (?d 8) (?c 9) (?z 11))",
                             "  (row (?a 1) (?c 9) (?z 11))",
            ")") ;
    }
    
    // Skew tables for join testing.
    // Join keys of ?x ?w and [?x , ?w]
    
    protected static RowList<Integer> tableS1() {
        return parseTableInt("(table"
                             ,"  (row (?z 1) (?x 9) (?w 11))"
                             ,"  (row (?z 4) (?x 9)) )"
                            ); }
    protected static RowList<Integer> tableS2() {
        return parseTableInt("(table (row (?x 9) (?w 1)))") ;
    }
    
    protected static RowList<Integer> tableS1J2() {
        return parseTableInt("(table" 
                             ,"  (row (?z 4) (?x 9) (?w 1) ))" 
                            ); 
    }
    
    // Code

    protected static RowList<Integer> parseTableInt(String... strings) {
        return QJT.parseTableInt(strings) ;
    }

    protected void testJoin(String var, RowList<Integer> left, RowList<Integer> right, RowList<Integer> tableOut) {
        testJoin(var, left, right, null, tableOut); 
    }
    
    protected void testJoin(String var, RowList<Integer> left, RowList<Integer> right, String conditions, RowList<Integer> tableOut) {
        JoinKey joinKey ;
        if ( var != null ) {
            if ( var.startsWith("?") )
                var = var.substring(1) ;
            joinKey = JoinKey.create(Var.alloc(var)) ;
        }
        else {
            // No vars in join key.  Allow implementation to decide
            // if needed.  Join keys are only needed for hash join
            // (and related algorithms).
            joinKey = null ;
        }
        
        ExprList exprs = null ;
        if ( conditions != null )
            exprs = SSE.parseExprList(conditions) ;
        executeTest(joinKey, left, right, exprs, tableOut) ;
    }

    protected void testJoinWithKey(JoinKey joinKey, RowList<Integer> left, RowList<Integer> right, RowList<Integer> tableOut) {
        executeTest(joinKey, left, right, null, tableOut) ;
    }

    protected void testJoinWithKey(JoinKey joinKey, RowList<Integer> left, RowList<Integer> right, ExprList conditions, RowList<Integer> tableOut) {
        executeTest(joinKey, left, right, conditions, tableOut) ;
    }

    // Any kind of join (choose by abstract join() operation).
    protected abstract void executeTest(JoinKey joinKey, RowList<Integer> left, RowList<Integer> right, ExprList conditions, RowList<Integer> expectedResults) ;
    
    protected void executeTestJoin(String msg, JoinKey joinKey, RowList<Integer> left, RowList<Integer> right, ExprList conditions, RowList<Integer> expectedResults) {
        RowList<Integer> x1 = joinMaterialize(joinKey, left, right, conditions) ;
        assertNotNull("Null RowList<Integer> from join ("+msg+")", x1) ;
        if ( false )
            print(msg, joinKey, left, right, conditions, expectedResults, x1) ;
        check("Results not equal ("+msg+")", joinKey, left, right, conditions, expectedResults, x1) ;
    }

    private RowList<Integer> joinMaterialize(JoinKey joinKey, RowList<Integer> left, RowList<Integer> right, ExprList conditions) {
        return join(joinKey, left , right, conditions) ;
    }

    public abstract RowList<Integer> join(JoinKey joinKey, RowList<Integer> left , RowList<Integer> right, ExprList conditions) ;

    private static void check(String msg, JoinKey joinKey, RowList<Integer> left, RowList<Integer> right, ExprList conditions, RowList<Integer> expected, RowList<Integer> actual) {
        boolean b = QJT.equal(expected.toList(), actual.toList()) ;
        if ( ! b ) 
            print(msg, joinKey, left, right, conditions, expected, actual); 
        assertTrue(msg, b) ;
    }

    protected static void print(String msg, JoinKey joinKey, RowList<Integer> left, RowList<Integer> right, ExprList conditions, RowList<Integer> expected, RowList<Integer> actual) {
        System.err.flush() ;
        System.out.flush() ;
        IndentedWriter out = IndentedWriter.stderr ;
        out.println("Test :    "+msg) ;
        out.println("Joinkey:  "+joinKey) ;
        
        print(out, "Left:", left) ;
        print(out, "Right:", right) ;
        if ( conditions != null )
            out.println("Conditions: "+conditions) ;    
        print(out, "Expected:", expected) ;
        print(out, "Actual:", actual) ;
        out.println() ;
        out.flush() ;
    }
    
    protected static void print(IndentedWriter out, String label, RowList<Integer> table) {
        out.println(label) ;
        out.incIndent();
        out.println(table.toString()) ;
        out.decIndent();
    }
}


