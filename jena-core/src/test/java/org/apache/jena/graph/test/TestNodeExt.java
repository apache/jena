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

package org.apache.jena.graph.test;

import static org.junit.Assert.*;

import org.apache.jena.graph.*;
import org.junit.Test;

/** Tests for {@link Node_Ext} */
public class TestNodeExt {

    private static Node s = NodeFactory.createBlankNode();
    private static Node p = NodeCreateUtils.create("eg:p");
    private static Node o = NodeCreateUtils.create("'abc'");

    private static Triple triple1 = Triple.create(s,p,o);
    private static Triple triple2 = Triple.create(s,p,o);

    private static Triple triple9 = Triple.create(NodeFactory.createBlankNode(),p,o);

    @Test public void ext_triple_1() {
        Node_Triple nt = new Node_Triple(s,p,o);
        assertNotNull(nt.get());
        assertEquals(triple1, nt.get());

        assertEquals(nt, nt);
        assertNotEquals(nt.get().hashCode(), nt.hashCode());
        assertTrue(nt.sameValueAs(nt));
    }

    @Test public void ext_triple_2() {
        Node_Triple nt1 = new Node_Triple(s,p,o);
        Node_Triple nt2 = new Node_Triple(s,p,o);

        assertEquals(nt1, nt2);
        assertEquals(nt1.hashCode(), nt2.hashCode());
        assertTrue(nt1.sameValueAs(nt2));
    }

    @Test public void ext_triple_3() {
        Node_Triple nt1 = new Node_Triple(triple1);
        Node_Triple nt2 = new Node_Triple(triple2);
        assertNotSame(nt1.get(), nt2.get());
        assertNotSame(nt1, nt2);
        assertEquals(nt1, nt2);
        assertEquals(nt1.hashCode(), nt2.hashCode());
    }

    @Test public void ext_triple_4() {
        Node_Triple nt1 = new Node_Triple(triple1);
        Node_Triple nt9 = new Node_Triple(triple9);
        assertNotSame(nt1.get(), nt9.get());
        assertNotSame(nt1, nt9);
        assertNotEquals(nt1, nt9);
        assertFalse(nt1.sameValueAs(nt9));
    }
}
