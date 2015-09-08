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
import org.apache.jena.sparql.engine.join.JoinKey ;
import org.apache.jena.sparql.resultset.ResultSetCompare ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Assert ;

/** Tests for inner/equi joins */ 
public abstract class AbstractTestJoin extends Assert {
    protected static Table parseTableInt(String... strings) {
        String x = StrUtils.strjoinNL(strings) ;
        return SSE.parseTable(x) ;
    }

    protected void testJoin(String var, Table left, Table right, Table tableOut) {
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

        executeTest(joinKey, left, right, tableOut) ;
    }

    protected abstract void executeTest(JoinKey joinKey, Table left, Table right, Table expectedResults) ;
    
    
    private List<Binding> toList(Table table) {
        return Iter.toList(table.rows()) ;
    }

    protected void executeTestJoin(String num, JoinKey joinKey, Table left, Table right, Table expectedResults) {
        Table x1 = joinMaterialize(joinKey, left, right) ;
        assertNotNull("Null table from join ("+num+")", x1) ;
        check("Results not equal ("+num+")", joinKey, left, right, expectedResults, x1) ;
    }

    private Table joinMaterialize(JoinKey joinKey, Table left, Table right) {
        QueryIterator qIter = join(joinKey, left , right) ;
        return TableFactory.create(qIter) ;
    }

    public abstract QueryIterator join(JoinKey joinKey , Table left , Table right) ;

    private static void check(String msg, JoinKey joinKey, Table left, Table right, Table expected, Table actual) {
        boolean b = equalTables(expected, actual) ;
        if ( ! b ) {
            System.out.println("Joinkey:  "+joinKey) ;
            System.out.println("Left:     \n"+left) ;
            System.out.println("Right:    \n"+right) ;
            System.out.println("Expected: \n"+expected) ;
            System.out.println("Actual:   \n"+actual) ;
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


