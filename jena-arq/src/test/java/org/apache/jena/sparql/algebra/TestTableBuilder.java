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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.sparql.algebra.table.TableBuilder;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.NodeFactoryExtra;

public class TestTableBuilder {
    @Test public void table_builder_01() {
        Table expectedT1 = SSE.parseTable("(table (vars ?a ?b) (row (?a 1) (?b 2)))");
        Table expectedT2 = SSE.parseTable("(table (vars ?a ?b ?c) (row (?a 1) (?b 2)) (row (?c 3)))");

        TableBuilder builder = TableFactory.builder();
        Table actualT1 = builder.addRowsAndVars(expectedT1.rows()).build();
        assertEquals(expectedT1, actualT1);

        // Mutating the builder must not affect the tables created from it.
        Binding b = BindingFactory.binding(Var.alloc("c"), NodeFactoryExtra.intToNode(3));
        builder.addRowAndVars(b);
        Table actualT2 = builder.build();

        assertEquals(expectedT1, actualT1);
        assertEquals(expectedT2, actualT2);

        builder.reset();

        assertEquals(expectedT1, actualT1);
        assertEquals(expectedT2, actualT2);
        assertTrue(builder.snapshotVars().isEmpty());
        assertTrue(builder.snapshotRows().isEmpty());
    }
}

