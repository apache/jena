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
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.resultset.ResultSetCompare ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Assert ;

/** Tests for inner/equi joins */ 
public abstract class AbstractTestJoin extends Assert {

    protected static Table table0() { return parseTableInt("(table)") ; } 

    // Table of one row and no colums.
    protected static Table table1() { 
        return parseTableInt("(table (row))") ; }

    protected static Table tableD1() { 
        return parseTableInt("(table", 
                             "   (row (?a 1) (?b 2))",
                             "   (row (?a 1) (?b 3))",
                             "   (row (?a 1) (?b 2))",
            ")") ;
    }

    protected static Table tableD2() { 
        return parseTableInt("(table", 
                             "   (row (?a 0) (?d 8))",
                             "   (row (?a 1) (?c 9))",
            ")") ;
    }

    protected static Table tableD3() {
        return parseTableInt("(table",
                             "   (row (?a 1) (?c 9) (?b 2))",
                             "   (row (?a 1) (?c 9) (?b 3))",
                             "   (row (?a 1) (?c 9) (?b 2))",
            ")") ;
    }

    protected static Table tableD3_LJ() {
        return parseTableInt("(table", 
                             "   (row (?d 8) (?a 0))",
                             "   (row (?a 1) (?c 9) (?b 2))",
                             "   (row (?a 1) (?c 9) (?b 3))",
                             "   (row (?a 1) (?c 9) (?b 2))",
                             
            ")") ;
    }

    protected static Table tableD4() {
        return parseTableInt("(table", 
                             "   (row (?a 1) (?b 2))",
                             "   (row (?a 1) (?b 3))",
                             "   (row (?a 4) (?b 4))",
                             "   (row (?a 4) (?b 5))",
            ")") ;
    }

    protected static Table tableD5() {
        return parseTableInt("(table", 
                             "   (row (?a 4) (?c 4))",
                             "   (row (?a 4) (?c 5))",
                             "   (row (?a 6) (?c 5))",
            ")") ;
    }

    protected static Table tableD6() {
        return parseTableInt("(table", 
                             "   (row (?a 1) (?c 2))",
                             "   (row (?a 1) (?c 3))",
                             "   (row (?a 4) (?c 4))",
                             "   (row (?a 4) (?c 5))",
            ")") ;
    }

    protected static Table tableD4x5() {
        return parseTableInt("(table", 
                             "   (row (?a 4) (?c 4) (?b 4))",
                             "   (row (?a 4) (?c 4) (?b 5))",
                             "   (row (?a 4) (?c 5) (?b 4))",
                             "   (row (?a 4) (?c 5) (?b 5))",
            ")") ;
    }

    protected static Table tableD4x5_LJ() {
        return parseTableInt("(table",
                             "   (row (?a 4) (?c 4) (?b 4))",
                             "   (row (?a 4) (?c 4) (?b 5))",
                             "   (row (?a 4) (?c 5) (?b 4))",
                             "   (row (?a 4) (?c 5) (?b 5))",
                             "   (row (?b 2) (?a 1))",
                             "   (row (?b 3) (?a 1))",
            ")") ;
    }

    protected static Table tableD5x4_LJ() {
        return parseTableInt("(table", 
                             "   (row (?a 4) (?c 4) (?b 4))",
                             "   (row (?a 4) (?c 4) (?b 5))",
                             "   (row (?a 4) (?c 5) (?b 4))",
                             "   (row (?a 4) (?c 5) (?b 5))",
                             "   (row (?a 6) (?c 5))",
            ")") ;
    }

    protected static Table tableD4x6() {
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
    protected static Table tableD8() {
        return parseTableInt("(table",
                             "  (row (?x 10))",
                             "  (row (?z 11))",
            ")") ; 
    }

    // Table8 crossproduct table2
    protected static Table tableD8x2() {
        return parseTableInt("(table",
                             "  (row (?a 0) (?d 8) (?z 11))",
                             "  (row (?a 0) (?d 8) (?x 10))",
                             "  (row (?a 1) (?c 9) (?z 11))",
                             "  (row (?a 1) (?c 9) (?x 10))",
            ")") ;
    }
    
    // Left join data tables.
    protected static Table tableL1() {
        return parseTableInt("(table",
                             "  (row (?a 0) (?d 8))",
                             "  (row (?a 3) (?d 9))",
            ")") ;
    }

    protected static Table tableL2() {
        return parseTableInt("(table",
                             "  (row (?a 0) (?z 11))",
                             "  (row (?a 1) (?c 9) (?z 11))",
            ")") ;
    }

    // L3 := L1 leftjoin L2 
    protected static Table table1LJ2() {
        return parseTableInt("(table",
                             "  (row (?a 0) (?d 8) (?z 11))",
                             "  (row (?a 3) (?d 9))",
            ")") ;
    }
    
    protected static Table tableL4() {
        return parseTableInt("(table",
                             "  (row (?a 0) (?z 11))",
                             "  (row (?a 0) (?z 12))",
                             "  (row               (?r 99))",
                             "  (row        (?c 9) (?z 11))",
            ")") ;
    }

    protected static Table tableL5() {
        return parseTableInt("(table",
                             "  (row (?a 0) (?d 8))",
                             "  (row (?a 1) (?c 9) (?z 11))",
            ")") ;
    }

    // L3 := L1 leftjoin L2 
    protected static Table table4LJ5() {
        return parseTableInt("(table",
                             "  (row (?a 0) (?d 8) (?z 11))",
                             "  (row (?a 0) (?d 8) (?z 12))",
                             
                             "  (row (?a 0) (?d 8) (?r 99))",
                             "  (row (?a 1) (?c 9) (?z 11) (?r 99))",
                             
                             "  (row (?a 0) (?d 8) (?c 9) (?z 11))",
                             "  (row (?a 1) (?c 9) (?z 11))",
            ")") ;
    }
    
    // Code

    protected static Table parseTableInt(String... strings) {
        String x = StrUtils.strjoinNL(strings) ;
        return SSE.parseTable(x) ;
    }

    protected void testJoin(String var, Table left, Table right, Table tableOut) {
        testJoin(var, left, right, null, tableOut); 
    }
    
    protected void testJoin(String var, Table left, Table right, ExprList conditions, Table tableOut) {
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

        executeTest(joinKey, left, right, null, tableOut) ;
    }

    // Any kind of join (choose by abstract join() operation).
    protected abstract void executeTest(JoinKey joinKey, Table left, Table right, ExprList conditions, Table expectedResults) ;
    
    private List<Binding> toList(Table table) {
        return Iter.toList(table.rows()) ;
    }

    protected void executeTestJoin(String msg, JoinKey joinKey, Table left, Table right, ExprList conditions, Table expectedResults) {
        Table x1 = joinMaterialize(joinKey, left, right, conditions) ;
        assertNotNull("Null table from join ("+msg+")", x1) ;
        if ( false ) {
            System.out.println("Test :    "+msg) ;
            System.out.println("Joinkey:  "+joinKey) ;
            System.out.println("Left:     \n"+left) ;
            System.out.println("Right:    \n"+right) ;
            System.out.println("Expected: \n"+expectedResults) ;
            System.out.println("Actual:   \n"+x1) ;
            System.out.println() ;
        }
        
        check("Results not equal ("+msg+")", joinKey, left, right, expectedResults, x1) ;
    }

    private Table joinMaterialize(JoinKey joinKey, Table left, Table right, ExprList conditions) {
        QueryIterator qIter = join(joinKey, left , right, null) ;
        return TableFactory.create(qIter) ;
    }

    public abstract QueryIterator join(JoinKey joinKey, Table left , Table right, ExprList conditions) ;

    private static void check(String msg, JoinKey joinKey, Table left, Table right, Table expected, Table actual) {
        boolean b = equalTables(expected, actual) ;
        if ( ! b ) {
            System.out.flush() ;
            System.err.println("Joinkey:  "+joinKey) ;
            System.err.println("Left:     \n"+left) ;
            System.err.println("Right:    \n"+right) ;
            System.err.println("Expected: \n"+expected) ;
            System.err.println("Actual:   \n"+actual) ;
            System.err.println() ;
        }

        assertTrue(msg, b) ;
    }

    private static boolean equalTables(Table table1, Table table2) {
        ResultSet rs1 =  ResultSetFactory.create(table1.iterator(null), table1.getVarNames()) ;
        ResultSet rs2 =  ResultSetFactory.create(table2.iterator(null), table2.getVarNames()) ;
        return ResultSetCompare.equalsByTerm(rs1, rs2) ;
    }

}


