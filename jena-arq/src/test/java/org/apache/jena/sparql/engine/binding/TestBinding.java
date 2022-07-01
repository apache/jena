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

package org.apache.jena.sparql.engine.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

public class TestBinding {
    private static Node n_1 = SSE.parseNode("'1'");
    private static Node n_2 = SSE.parseNode("'2'");
    private static Node n_3 = SSE.parseNode("'3'");
    private static Node n_4 = SSE.parseNode("'4'");
    private static Node n_5 = SSE.parseNode("'5'");
    private static Node n_6 = SSE.parseNode("'6'");

    private static Node n_a = SSE.parseNode("'A'");
    private static Node n_b = SSE.parseNode("'A'");

    private static Var var1 = Var.alloc("v1");
    private static Var var2 = Var.alloc("v2");
    private static Var var3 = Var.alloc("v3");
    private static Var var4 = Var.alloc("v4");
    private static Var var5 = Var.alloc("v5");
    private static Var var6 = Var.alloc("v6");

    private static Var vp1 = Var.alloc("p1");
    private static Var vp2 = Var.alloc("p2");

    @Test public void binding_0() {
        Binding b = BindingFactory.binding();
        assertEquals(0, b.size());
        assertTrue(b.isEmpty());
        assertEquals(0, Iter.count(b.vars()));

        Binding bx = Binding.builder().build();
        assertEquals(b, bx);
    }

    @Test public void binding_1() {
        Binding b = BindingFactory.binding(var1, n_1);
        assertEquals(1, b.size());
        assertFalse(b.isEmpty());
        assertEquals(1, Iter.count(b.vars()));
        assertTrue(b.contains(var1));
        assertEquals(n_1, b.get(var1));

        Binding bx = Binding.builder().add(var1, n_1).build();
        assertEquals(b, bx);
    }

    @Test public void binding_2() {
        Binding b = BindingFactory.binding(var1, n_1, var2, n_2);
        assertEquals(2, b.size());
        assertFalse(b.isEmpty());
        assertEquals(2, Iter.count(b.vars()));

        Binding bx = Binding.builder().add(var1, n_1).add(var2, n_2).build();
        assertEquals(b, bx);
    }

    @Test public void binding_3() {
        Binding b = BindingFactory.binding(var1, n_1, var2, n_2, var3, n_3);
        assertEquals(3, b.size());
        assertFalse(b.isEmpty());
        assertEquals(3, Iter.count(b.vars()));

        Binding bx = Binding.builder().add(var1, n_1).add(var2, n_2).add(var3, n_3).build();
        assertEquals(b, bx);
    }

    @Test public void binding_4() {
        Binding b = BindingFactory.binding(var1, n_1, var2, n_2, var3, n_3, var4, n_4);
        assertEquals(4, b.size());
        assertFalse(b.isEmpty());
        assertEquals(4, Iter.count(b.vars()));

        Binding bx = Binding.builder()
                .add(var1, n_1)
                .add(var2, n_2)
                .add(var3, n_3)
                .add(var4, n_4)
                .build();
        assertEquals(b, bx);
    }

    @Test public void binding_5() {
        BindingBuilder builder = Binding.builder();
        assertEquals(0, builder.snapshot().size());
        builder.toString();

        add(builder, var1, n_1);
        builder.toString();
        assertEquals(1, builder.snapshot().size());

        add(builder, var2, n_2);
        builder.toString();
        assertEquals(2, builder.snapshot().size());

        add(builder, var3, n_3);
        builder.toString();
        assertEquals(3, builder.snapshot().size());

        add(builder, var4, n_4);
        builder.toString();
        assertEquals(4, builder.snapshot().size());

        add(builder, var5, n_5);
        builder.toString();
        assertEquals(5, builder.snapshot().size());

        Binding b = builder.build();
        assertEquals(5, b.size());
        assertFalse(b.isEmpty());
        assertEquals(5, Iter.count(b.vars()));
    }

    @Test public void binding_6() {
        Binding parent = BindingFactory.binding(vp1, n_a, vp2, n_b);

        BindingBuilder builder = Binding.builder(parent);
        assertEquals(2, builder.snapshot().size());
        builder.toString();

        add(builder, var1, n_1);
        builder.toString();
        assertEquals(3, builder.snapshot().size());

        add(builder, var2, n_2);
        builder.toString();
        assertEquals(4, builder.snapshot().size());

        add(builder, var3, n_3);
        builder.toString();
        assertEquals(5, builder.snapshot().size());

        add(builder, var4, n_4);
        builder.toString();
        assertEquals(6, builder.snapshot().size());

        // Start map.
        add(builder, var5, n_5);
        builder.toString();
        assertEquals(7, builder.snapshot().size());

        // Second item into the map.
        add(builder, var6, n_6);
        builder.toString();
        assertEquals(8, builder.snapshot().size());

        Binding b = builder.build();
        assertEquals(8, b.size());
        assertFalse(b.isEmpty());
        assertEquals(8, Iter.count(b.vars()));
    }

    @Test public void binding_set_1() {
        Binding parent = BindingFactory.binding(vp1, n_a);
        BindingBuilder builder = Binding.builder(parent);
        builder.set(var1, n_1);
        assertEquals(n_1, builder.get(var1));
        assertEquals(n_a, builder.get(vp1));

        builder.set(var1, n_1);
        assertEquals(n_1, builder.get(var1));
        Binding b = builder.build();
        assertEquals(2, b.size());
        assertFalse(b.isEmpty());
        assertEquals(2, Iter.count(b.vars()));
    }

    @Test public void binding_set_2() {
        Binding parent = BindingFactory.binding(vp1, n_a);
        BindingBuilder builder = Binding.builder(parent);
        builder.add(var1, n_1);
        builder.set(var1, n_1);
        assertEquals(n_1, builder.get(var1));
        assertEquals(n_a, builder.get(vp1));

        builder.set(var1, n_2);
        Binding b = builder.build();
        assertEquals(2, b.size());
        assertEquals(n_2, b.get(var1));
        assertFalse(b.isEmpty());
        assertEquals(2, Iter.count(b.vars()));
    }

    // UNIQUE_NAMES_CHECK_PARENT
//    @Test(expected=IllegalArgumentException.class)
//    public void binding_set_bad_1() {
//        Binding parent = BindingFactory.binding(vp1, n_a);
//        BindingBuilder builder = BindingFactory.builder(parent);
//        builder.add(var1, n_1);
//        builder.set(var1, n_1);
//        //builder.set(vp1, n_b);
//    }

    private static void add(BindingBuilder builder, Var var, Node node) {
        builder.add(var, node);
        assertTrue(builder.contains(var));
        assertEquals(node, builder.get(var));
    }

    private static void set(BindingBuilder builder, Var var, Node node) {
        builder.set(var, node);
        assertTrue(builder.contains(var));
        assertEquals(node, builder.get(var));
    }

    @Test public void binding_access_01() {
        Binding b = BindingFactory.binding(var1, n_1);
        assertTrue(b.contains(var1));
        assertFalse(b.contains(var2));
        assertEquals(n_1, b.get(var1));
        assertNull(b.get(var2));
    }

    @Test public void binding_access_02() {
        Binding parent = BindingFactory.binding(vp1, n_a);
        Binding b = BindingFactory.binding(parent, var1, n_1);
        assertTrue(b.contains(var1));
        assertFalse(b.contains(var2));
        assertEquals(n_1, b.get(var1));
        assertNull(b.get(var2));

        assertTrue(b.contains(vp1));
        assertEquals(n_a, b.get(vp1));
    }

    @Test public void binding_builder_01() {
        BindingBuilder builder = Binding.builder();
        assertTrue(builder.isEmpty());
        assertFalse(builder.contains(var1));
        assertNull(builder.get(var1));

        builder.add(var1,  n_1);
        assertFalse(builder.isEmpty());
        assertTrue(builder.contains(var1));
        assertEquals(n_1, builder.get(var1));
    }

    @Test public void binding_builder_02() {
        Binding parent = BindingFactory.binding(vp1, n_a);
        BindingBuilder builder = Binding.builder(parent);
        assertFalse(builder.isEmpty());
        assertFalse(builder.contains(var1));
        assertNull(builder.get(var1));

        builder.add(var1,  n_1);
        assertFalse(builder.isEmpty());

        assertTrue(builder.contains(var1));
        assertEquals(n_1, builder.get(var1));

        assertTrue(builder.contains(vp1));
        assertEquals(n_a, builder.get(vp1));
    }

    @Test(expected=NullPointerException.class)
    public void binding_bad_01() {
        Binding b = BindingFactory.binding(var1, null);
    }

    @Test(expected=NullPointerException.class)
    public void binding_bad_02() {
        Binding b = BindingFactory.binding(null, n_1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void binding_bad_03() {
        Binding b = BindingFactory.binding(var1, n_1, var1, n_2);
    }

    @Test(expected=IllegalArgumentException.class)
    public void binding_bad_04() {
        Binding b = BindingFactory.binding(var1, n_1, var2, n_2, var1, n_3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void binding_bad_05() {
        Binding b = BindingFactory.binding(var1, n_1, var2, n_2, var2, n_3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void binding_bad_06() {
        Binding b = BindingFactory.binding(var1, n_1, var1, n_2, var3, n_3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void binding_bad_07() {
        Binding b = BindingFactory.binding(var1, n_1, var2, n_2, var3, n_3, var1, n_4);
    }

    @Test(expected=NullPointerException.class)
    public void binding_builder_bad_01() {
        BindingBuilder builder = Binding.builder();
        builder.add(var1, null);
    }

    @Test(expected=NullPointerException.class)
    public void binding_builder_bad_02() {
        BindingBuilder builder = Binding.builder();
        builder.add(null, n_1);
        //var1, n_1, var2, n_2, var3, n_3, var1, n_4);
    }

    @Test(expected=IllegalArgumentException.class)
    public void binding_builder_bad_03() {
        BindingBuilder builder = Binding.builder();
        builder.add(var1, n_1);
        builder.add(var1, n_1);
    }

    // Assumes UNIQUE_NAMES_CHECK_PARENT
//    @Test(expected=IllegalArgumentException.class)
//    public void binding_builder_bad_04() {
//        Binding b = BindingFactory.binding(var1, n_1);
//        BindingBuilder builder = BindingFactory.builder(b);
//        builder.add(var1, n_1);
//    }
}
