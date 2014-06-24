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

package com.hp.hpl.jena.sparql.engine;

import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.algebra.table.TableEmpty ;
import com.hp.hpl.jena.sparql.algebra.table.TableN ;
import com.hp.hpl.jena.sparql.algebra.table.TableUnit ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestTableLib extends BaseTest
{
    Table unit = new TableUnit() ; 
    Table zero = new TableEmpty() ;
    Table zeroData = SSE.parseTable("(table)") ;
    Table unitData = SSE.parseTable("(table (row))") ;
    Table data1 = SSE.parseTable("(table (row (?a 1) (?b 2)))") ;
    Table data2 = SSE.parseTable("(table (row (?a 1) (?c 3)) (row (?a 9) (?c 5))  )") ;
    Table data3 = SSE.parseTable("(table (row (?a 1) (?c 3)) (row (?a 1) (?c 4)) (row (?a 9) (?c 5))  )") ;
    
    Table data1J2 = SSE.parseTable("(table (row (?a 1) (?b 2) (?c 3)) )") ;
    Table data1LJ2 = SSE.parseTable("(table (row (?a 1) (?b 2) (?c 3)) (row (?a 9) (?c 5)) )") ;
    Table data2LJ1 = SSE.parseTable("(table (row (?a 1) (?b 2) (?c 3)) )") ;
    
    Table data1J3 = SSE.parseTable("(table (row (?a 1) (?b 2) (?c 3)) (row (?a 1) (?b 2) (?c 4)) )") ;
    
    @Test public void table_01() { test(unit, zero, true, null, zero) ; }
    @Test public void table_02() { test(zero, unit, true, null, zero) ; }
    @Test public void table_03() { test(unit, zero, false, null, unit) ; }
    @Test public void table_04() { test(zero, unit, false, null, zero) ; }
    // Same again but with non-special tables.
    @Test public void table_05() { test(unitData, zeroData, true, null, zeroData) ; }
    @Test public void table_06() { test(zeroData, unitData, true, null, zeroData) ; }
    @Test public void table_07() { test(unitData, zeroData, false, null, unitData) ; }
    @Test public void table_08() { test(zeroData, unitData, false, null, zeroData) ; }

    @Test public void table_10() { test(data1, zero, true, null, zero) ; }
    @Test public void table_11() { test(zero, data1, true, null, zero) ; }
    @Test public void table_12() { test(data1, zero, false, null, data1) ; }
    @Test public void table_13() { test(zero, data1, false, null, zero) ; }
    
    @Test public void table_14() { test(data1, zeroData, true, null, zeroData) ; }
    @Test public void table_15() { test(zeroData, data1, true, null, zeroData) ; }
    @Test public void table_16() { test(data1, zeroData, false, null, data1) ; }
    @Test public void table_17() { test(zeroData, data1, false, null, zeroData) ; }

    @Test public void table_18() { test(data2, unitData, true, null, data2) ; }
    @Test public void table_19() { test(unitData, data2, true, null, data2) ; }
    
    @Test public void table_20() { test(data1, data2, true, null, data1J2) ; }
    @Test public void table_21() { test(data2, data1, true, null, data1J2) ; }
    @Test public void table_22() { test(data1, data2, false, null, data1LJ2) ; }
    @Test public void table_23() { test(data2, data1, false, null, data2LJ1) ; }
    
    @Test public void table_24() { test(data1, data3, true, null, data1J3) ; }
    @Test public void table_25() { test(data3, data1, true, null, data1J3) ; }

    private void test(Table left, Table right, boolean normalJoin, ExprList exprs, Table expected) {
        ExecutionContext execCxt = new ExecutionContext(ARQ.getContext(), null, null, null) ;
        QueryIterator leftIter = left.iterator(execCxt) ;
        QueryIterator qIter = normalJoin 
            ? TableJoin.join(leftIter, right, exprs, execCxt)
            : TableJoin.leftJoin(leftIter, right, exprs, execCxt) ;
        
            // Order issues
            
        Set<String> vars1 = new HashSet<>() ;
        vars1.addAll(left.getVarNames()) ;
        vars1.addAll(right.getVarNames()) ;
        
        TableN results = new TableN(qIter) ;
        boolean b = TableCompare.equalsByTerm(expected, results) ;
        if ( !b ) {
            System.out.println("** Expected") ;
            System.out.println(expected) ;
            System.out.println("** Actual") ;
            System.out.println(results) ;
        }
        assertTrue(b) ;
    }
    
    static class TableCompare {
        public static boolean equalsByTerm(Table table1, Table table2) {
            ResultSet rs1 = ResultSetFactory.create(table1.iterator(null), table1.getVarNames()) ;
            ResultSet rs2 = ResultSetFactory.create(table2.iterator(null), table2.getVarNames()) ;
            return ResultSetCompare.equalsByTerm(rs1, rs2) ;
        }
    }
        
}
