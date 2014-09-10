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

package com.hp.hpl.jena.sparql.algebra.optimize;

import org.apache.jena.atlas.lib.StrUtils;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend;
import com.hp.hpl.jena.sparql.algebra.op.OpTable;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;
import com.hp.hpl.jena.sparql.sse.SSE;

/**
 * Tests for {@code TransformPromoteTableEmpty}
 * 
 */
public class TestTransformPromoteTableEmpty {

    private Transform t_promote = new TransformPromoteTableEmpty();

    @Test
    public void promote_table_empty_assignment_01() {
        test("(extend ((?x 1)) (table empty))", t_promote, "(table empty)");
    }

    @Test
    public void promote_table_empty_assignment_02() {
        // Intentionally using assign and extend because otherwise algebra
        // parsing will collapse to a single operator behind our backs
        test("(assign ((?y 2)) (extend ((?x 1)) (table empty)))", t_promote, "(table empty)");
    }

    @Test
    public void promote_table_empty_assignment_03() {
        // Force algebra to have separate extends by using extendDirect()
        Op input = OpTable.empty();
        input = OpExtend.create(input, new VarExprList(Var.alloc("x"), new NodeValueInteger(1)));
        input = OpExtend.create(input, new VarExprList(Var.alloc("y"), new NodeValueInteger(2)));

        test(input, t_promote, "(table empty)");
    }

    @Test
    public void promote_table_empty_join_01() {
        test("(join (table unit) (table empty))", t_promote, "(table empty)");
    }

    @Test
    public void promote_table_empty_join_02() {
        test("(join (table empty) (table unit))", t_promote, "(table empty)");
    }

    @Test
    public void promote_table_empty_join_03() {
        // Promotion should percolate upwards
        test("(join (table unit) (join (table unit) (table empty)))", t_promote, "(table empty)");
    }

    @Test
    public void promote_table_empty_leftjoin_01() {
        // Table empty on RHS allows only left join to be eliminated but keeps
        // the LHS
        test("(leftjoin (table unit) (table empty) ())", t_promote, "(table unit)");
    }

    @Test
    public void promote_table_empty_leftjoin_02() {
        // Table empty on LHS allows whole thing to be eliminated
        test("(leftjoin (table empty) (table unit) ())", t_promote, "(table empty)");
    }

    @Test
    public void promote_table_empty_leftjoin_03() {
        // Table empty on RHS allows only left join to be eliminated but keeps
        // the LHS
        test("(leftjoin (table unit) (leftjoin (table empty) (table unit) ()) ())", t_promote, "(table unit)");
    }

    @Test
    public void promote_table_empty_leftjoin_04() {
        // Table empty on LHS allows whole thing to be eliminated
        test("(leftjoin (leftjoin (table empty) (table unit) ()) (table unit) ())", t_promote, "(table empty)");
    }

    @Test
    public void promote_table_empty_union_01() {
        // Table empty on only one side allows the union to be eliminated but
        // keeps the other side
        test("(union (table empty) (table unit))", t_promote, "(table unit)");
    }

    @Test
    public void promote_table_empty_union_02() {
        // Table empty on only one side allows the union to be eliminated but
        // keeps the other side
        test("(union (table unit) (table empty))", t_promote, "(table unit)");
    }

    @Test
    public void promote_table_empty_union_03() {
        // Table empty on both sides allows the whole thing to be eliminated
        test("(union (table empty) (table empty))", t_promote, "(table empty)");
    }
    
    @Test
    public void promote_table_empty_union_04() {
        // Promotion should percolate upwards
        test("(union (union (table unit) (table empty)) (union (table empty) (table unit)))", t_promote, "(union (table unit) (table unit))");
    }
    
    @Test
    public void promote_table_empty_union_05() {
        // Promotion should percolate upwards
        test("(union (union (table empty) (table empty)) (union (table empty) (table empty)))", t_promote, "(table empty)");
    }
    
    @Test
    public void promote_table_empty_minus_01() {
        // If RHS is table empty the minus can be eliminated and the LHS is kept
        test("(minus (table unit) (table empty))", t_promote, "(table unit)");
    }
    
    @Test
    public void promote_table_empty_minus_02() {
        // If LHS is table empty the entire thing can be eliminated
        test("(minus (table empty) (table unit))", t_promote, "(table empty)");
    }

    public static void test(String input, Transform transform, String... output) {
        Op opInput = SSE.parseOp(input);
        test(opInput, transform, output);
    }

    public static void test(Op input, Transform transform, String... output) {
        Op opOptimized = Transformer.transform(transform, input);
        if (output == null) {
            // No transformation.
            Assert.assertEquals(input, opOptimized);
            return;
        }

        Op op3 = SSE.parseOp(StrUtils.strjoinNL(output));
        Assert.assertEquals(op3, opOptimized);
    }
}
