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

package org.apache.jena.sparql.algebra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.table.TableN;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.VarFinder;
import org.apache.jena.sparql.sse.SSE;

public class TestVarFinder2
{
    private static Binding row_xy = SSE.parseBinding("(row (?x 1) (?y 2))");
    private static Binding row_x  = SSE.parseBinding("(row (?x 1))");
    private static Binding row_y  = SSE.parseBinding("(row (?y 2))");
    private static Binding row0  = SSE.parseBinding("(row)");
    private static Var var_x  = Var.alloc("x");
    private static Var var_y  = Var.alloc("y");
    private static Var[] empty = {};

    @Test public void varfind_table_1() {
        OpTable opTable = createTable(row_xy);
        VarFinder vf = VarFinder.process(opTable);
        assertSetVars(vf.getFixed(), var_x, var_y);
        assertSetVars(vf.getOpt(), empty);

        assertDifferentVars(vf.getFixed(), var_x);
        assertDifferentVars(vf.getFixed(), var_y);
        assertDifferentVars(vf.getFixed(), empty);
    }

    @Test public void varfind_table_2() {
        OpTable opTable = createTable(row_xy, row_x);
        VarFinder vf = VarFinder.process(opTable);
        assertSetVars(vf.getFixed(), var_x);
        assertSetVars(vf.getOpt(), var_y);
    }

    @Test public void varfind_table_3() {
        OpTable opTable = createTable(row_xy, row_x, row_y);
        VarFinder vf = VarFinder.process(opTable);
        assertSetVars(vf.getFixed(), empty);
        assertSetVars(vf.getOpt(), var_x, var_y);
    }

    @Test public void varfind_table_4() {
        OpTable opTable = createTable(row_x, row0);
        VarFinder vf = VarFinder.process(opTable);
        assertSetVars(vf.getFixed(), empty);
        assertSetVars(vf.getOpt(), var_x);
    }

    /** Create a table of rows */
    private static OpTable createTable(Binding... rows) {
        Table table = new TableN();
        for(Binding b : rows) {
            table.addBinding(b);
        }
        return OpTable.create(table);
    }

    @Test public void varfind_extend_1() {
        Op op = SSE.parseOp("(extend (?x 1) (table))");
        VarFinder vf = VarFinder.process(op);
        assertSetVars(vf.getFixed(), var_x);
    }

    @Test public void varfind_extend_2() {
        Op op = SSE.parseOp("(extend (?x (coalesce 1 2)) (table))");
        VarFinder vf = VarFinder.process(op);
        assertSetVars(vf.getFixed(), var_x);
    }

    @Test public void varfind_extend_3() {
        Op op = SSE.parseOp("(extend (?x (coalesce ?y 2)) (table))");
        VarFinder vf = VarFinder.process(op);
        assertSetVars(vf.getFixed(), var_x);
    }

    @Test public void varfind_extend_4() {
        Op op = SSE.parseOp("(extend (?x (coalesce ?y)) (table))");
        VarFinder vf = VarFinder.process(op);
        assertSetVars(vf.getFixed(), empty);
        assertSetVars(vf.getOpt(), var_x);
    }

    @Test public void varfind_extend_5() {
        Op op = SSE.parseOp("""
          (extend ((?x (coalesce ?y)))
            (extend ((?y 1))
              (table unit)
           ))
        """);
        VarFinder vf = VarFinder.process(op);
        assertSetVars(vf.getFixed(), var_x, var_y);
    }

    @Test public void varfind_extend_6() {
        Op op = SSE.parseOp("""
          (extend ((?x (coalesce ?z)))
            (extend ((?y 1))
              (table unit)
           ))
        """);
        VarFinder vf = VarFinder.process(op);
        assertSetVars(vf.getFixed(), var_y);
        assertSetVars(vf.getOpt(), var_x);
    }

    /* Assert that a set contains exactly the given vars. */
    private static void assertSetVars(Set<Var> vars, Var... expectedVars) {
        Set<Var> expected = Set.of(expectedVars);
        assertEquals(expected, vars);
    }

    /* Assert that a set differs from exactly the given vars. */
    private static void assertDifferentVars(Set<Var> vars, Var... notExpectedVars) {
        Set<Var> expected = Set.of(notExpectedVars);
        assertNotEquals(expected, vars);
    }
}
