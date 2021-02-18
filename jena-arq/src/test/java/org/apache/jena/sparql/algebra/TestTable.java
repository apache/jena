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

import static org.junit.Assert.assertEquals;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.writers.WriterOp;
import org.junit.Test ;

public class TestTable {
    @Test public void table_01() {
        Table table = TableFactory.createEmpty();
        Op opTable = OpTable.create(table);
        String x = str(opTable);
        assertEquals("(table empty)",  x);
    }

    // JENA-1468: Table, no rows , with declared variables.
    @Test public void table_02() {
        Table table = TableFactory.create();
        table.getVars().add(Var.alloc("a"));
        Op opTable = OpTable.create(table);
        String x = str(opTable);
        assertEquals("(table (vars ?a))",  x);
    }

    @Test public void table_03() {
        Table table = TableFactory.create();
        Binding b = BindingFactory.empty();
        table.addBinding(b);
        Op opTable = OpTable.create(table);
        String x = str(opTable);
        assertEquals("(table (vars) (row) )",  x);
    }

    // String, no adornment
    private static String str(Op op) {
        SerializationContext sCxt = new SerializationContext();
        IndentedLineBuffer out = new IndentedLineBuffer();
        out.setFlatMode(true);
        WriterOp.output(out, op, sCxt);
        String x = out.asString();
        return x.trim();
    }
}

