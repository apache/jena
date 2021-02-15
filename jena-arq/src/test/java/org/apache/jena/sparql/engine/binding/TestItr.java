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
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.jena.sparql.engine.binding.itr.Itr;
import org.junit.Test;

public class TestItr {

    @Test public void itr_0() {
        Iterator<String> iter = Itr.iter0();
        assertFalse(iter.hasNext());
    }

    @Test public void itr_1() {
        Iterator<String> iter = Itr.iter1("A");
        assertTrue(iter.hasNext());
        assertEquals("A", iter.next());
        assertFalse(iter.hasNext());
    }

    @Test public void itr_2() {
        Iterator<String> iter = Itr.iter2("A", "B");
        assertTrue(iter.hasNext());
        assertEquals("A", iter.next());
        assertEquals("B", iter.next());
        assertFalse(iter.hasNext());
    }

    @Test public void itr_3() {
        Iterator<String> iter = Itr.iter3("A", "B", "C");
        assertTrue(iter.hasNext());
        assertEquals("A", iter.next());
        assertEquals("B", iter.next());
        assertEquals("C", iter.next());
        assertFalse(iter.hasNext());
    }

    @Test public void itr_4() {
        Iterator<String> iter = Itr.iter4("A", "B", "C", "D");
        assertTrue(iter.hasNext());
        assertEquals("A", iter.next());
        assertEquals("B", iter.next());
        assertEquals("C", iter.next());
        assertEquals("D", iter.next());
        assertFalse(iter.hasNext());
    }

    @Test(expected=NoSuchElementException.class)
    public void itr_no_next() {
        Iterator<String> iter = Itr.iter1("A");
        assertTrue(iter.hasNext());
        assertEquals("A", iter.next());
        iter.next();
    }

}
