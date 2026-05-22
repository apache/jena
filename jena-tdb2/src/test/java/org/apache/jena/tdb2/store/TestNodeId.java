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

package org.apache.jena.tdb2.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.BitsLong;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.util.NodeFactoryExtra;

public class TestNodeId
{
    // Pointers.
    @Test public void nodeId_ptr_01() {
        NodeId nodeId = NodeIdFactory.createPtrLong(17, 37);
        assertEquals(NodeIdType.PTR, nodeId.type());
//        assertEquals(37L, nodeId.getPtrLo());
//        assertEquals(17, nodeId.getPtrHi());
        assertEquals(37L, nodeId.getValue2());
        assertEquals(17, nodeId.getValue1());
    }

    @Test public void nodeId_ptr_02() {
        NodeId nodeId = NodeIdFactory.createPtr(37);
        assertEquals(NodeIdType.PTR, nodeId.type());
//        assertEquals(37L, nodeId.getPtrLo());
//        assertEquals(0, nodeId.getPtrHi());
        assertEquals(37L, nodeId.getPtrLocation());
    }

    @Test public void nodeId_ptr_03() {
        NodeId nodeId = NodeIdFactory.createPtr(39);
        // 64 bit
        long x = nodeId.getValue2();
        long t = BitsLong.unpack(x, 56, 64);
        assertEquals(0, t);
        assertEquals(NodeIdType.PTR.type(), t);
    }

    // Specials.
    @Test public void nodeId_special_01() {
        assertFalse(NodeId.isConcrete(NodeId.NodeDoesNotExist));
        assertEquals(NodeIdType.SPECIAL, NodeId.NodeDoesNotExist.type());
    }

    @Test public void nodeId_special_02() {
        assertFalse(NodeId.isConcrete(NodeId.NodeIdAny));
        assertEquals(NodeIdType.SPECIAL, NodeId.NodeIdAny.type());
    }

    // Storage

    @Test public void nodeId_codec_01() { testCodecArray(NodeIdFactory.createPtr(37)); }

    @Test public void nodeId_codec_02() { testCodecArray(NodeId.createRaw(NodeIdType.XSD_INTEGER, 1)); }

    // 56 bit -1.
    @Test public void nodeId_codec_03() { testCodecArray(NodeId.createRaw(NodeIdType.XSD_INTEGER, BitsLong.clear(-1L, 56,64))); }

    @Test public void nodeId_codec_04() { testCodecArray("12.34"); }

    @Test public void nodeId_codec_05() { testCodecArray("'2.2'^^xsd:float"); }

    private static void testCodecArray(String str) {
        Node n = NodeFactoryExtra.parseNode(str);
        NodeId nid = NodeIdInline.inline(n);
        testCodecArray(nid);
    }

    private static void testCodecArray(NodeId nid) {
        testCodecArray(nid, nid);
    }

    private static void testCodecArray(NodeId testNid,NodeId expected) {
        byte[] b = new byte[8];
        NodeIdFactory.set(testNid, b);
        NodeId nid1 = NodeIdFactory.get(b);
        assertEquals(expected, nid1);
    }

    @Test public void nodeId_codec_11() { testCodecBuffer(NodeIdFactory.createPtr(37)); }

    @Test public void nodeId_codec_12() { testCodecBuffer(NodeId.createRaw(NodeIdType.XSD_INTEGER, 1)); }

    @Test public void nodeId_codec_13() { testCodecBuffer(NodeId.createRaw(NodeIdType.XSD_INTEGER, BitsLong.clear(-1L, 56,64))); }

    @Test public void nodeId_codec_14() { testCodecBuffer("12.34"); }

    @Test public void nodeId_codec_15() { testCodecBuffer("'2.2'^^xsd:float"); }

    private static void testCodecBuffer(String str) {
        Node n = NodeFactoryExtra.parseNode(str);
        NodeId nid = NodeIdInline.inline(n);
        testCodecArray(nid);
    }

    private static void testCodecBuffer(NodeId nid) {
        testCodecArray(nid, nid);
    }

    private static void testCodecBuffer(NodeId testNid,NodeId expected) {
        ByteBuffer b = ByteBuffer.allocate(8);
        NodeIdFactory.set(testNid, b);
        NodeId nid1 = NodeIdFactory.get(b);
        assertEquals(expected, nid1);
    }

    @Test public void nodeId_compare_01() {
        // Same value - should be equal (0)
        NodeId n1 = NodeIdFactory.createPtrLong(42, 100L);
        NodeId n2 = NodeIdFactory.createPtrLong(42, 100L);
        assertEquals(0, NodeId.compare(n1, n2));
        assertEquals(0, n1.compareTo(n2));
        assertTrue(n1.equals(n2));
    }

    @Test public void nodeId_compare_02() {
        // Different value1 - value2 should not affect ordering
        NodeId n1 = NodeIdFactory.createPtrLong(3, 1000L);
        NodeId n2 = NodeIdFactory.createPtrLong(7, 50L);
        assertTrue(NodeId.compare(n1, n2) < 0);
        assertTrue(n1.compareTo(n2) < 0);

        // Reverse direction
        assertTrue(NodeId.compare(n2, n1) > 0);
        assertTrue(n2.compareTo(n1) > 0);
    }

    @Test public void nodeId_compare_03() {
        // Same value1, different value2 - should compare on value2
        NodeId n1 = NodeIdFactory.createPtrLong(5, 100L);
        NodeId n2 = NodeIdFactory.createPtrLong(5, 200L);
        assertTrue(NodeId.compare(n1, n2) < 0);
        assertTrue(n1.compareTo(n2) < 0);

        // Reverse direction
        assertTrue(NodeId.compare(n2, n1) > 0);
        assertTrue(n2.compareTo(n1) > 0);
    }
}

