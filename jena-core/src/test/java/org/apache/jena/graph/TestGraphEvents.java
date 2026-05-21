/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.graph;

import org.apache.jena.junit.NodeCreateUtils;
import org.apache.jena.test.JenaTestBase;

public class TestGraphEvents extends JenaTestBase {
    public TestGraphEvents(String name) {
        super(name);
    }

    public void testGraphEventContent() {
        testGraphEventContents("testing", "an example");
        testGraphEventContents("toasting", Boolean.TRUE);
        testGraphEventContents("tasting", NodeCreateUtils.createTriple("we are here"));
    }

    public void testGraphEventsRemove() {
        testGraphEventsRemove("s", "p", "o");
        testGraphEventsRemove("s", "p", "17");
        testGraphEventsRemove("_s", "p", "'object'");
        testGraphEventsRemove("not:known", "p", "'chat'fr");
    }

    private void testGraphEventsRemove(String S, String P, String O) {
        Triple expected = NodeCreateUtils.createTriple(S + " " + P + " " + O);
        GraphEvents e = GraphEvents.remove(GraphTestLib.node(S), GraphTestLib.node(P), GraphTestLib.node(O));
        assertEquals(expected, e.getContent());
        assertEquals("remove", e.getTitle());
    }

    private void testGraphEventContents(String title, Object expected) {
        GraphEvents e = new GraphEvents(title, expected);
        assertEquals(title, e.getTitle());
        assertEquals(expected, e.getContent());
    }
}
