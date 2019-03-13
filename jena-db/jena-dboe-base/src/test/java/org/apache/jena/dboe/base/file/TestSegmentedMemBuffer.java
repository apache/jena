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

package org.apache.jena.dboe.base.file;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.jena.dboe.base.file.SegmentedMemBuffer;
import org.junit.Assert;
import org.junit.Test;

public class TestSegmentedMemBuffer extends Assert {
    private static byte[] data1 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    private static byte[] data2 = { 10,11,12 };

    @Test public void membuffer_00() {
        SegmentedMemBuffer space = new SegmentedMemBuffer();
        assertTrue(space.isOpen());
        assertEquals(0, space.length());
        space.close();
        assertFalse(space.isOpen());
        space.close();
    }

    @Test public void membuffer_01() {
        SegmentedMemBuffer space = new SegmentedMemBuffer();
        writeread1(space);
    }

    @Test public void membuffer_02() {
        SegmentedMemBuffer space = new SegmentedMemBuffer();
        writeread2(space);
    }

    @Test public void membuffer_03() {
        SegmentedMemBuffer space = new SegmentedMemBuffer();
        writeread1(space);
        space.truncate(0);
        writeread2(space);
    }

    @Test public void membuffer_11() {
        SegmentedMemBuffer space = new SegmentedMemBuffer(2);
        writeread1(space);
    }

    @Test public void membuffer_12() {
        SegmentedMemBuffer space = new SegmentedMemBuffer(2);
        writeread2(space);
    }

    @Test public void membuffer_13() {
        SegmentedMemBuffer space = new SegmentedMemBuffer(2);
        writeread1(space);
        space.truncate(0);
        writeread2(space);
    }

    @Test public void membuffer_21() {
        SegmentedMemBuffer space = new SegmentedMemBuffer();
        writeread1a(space);
    }

    @Test public void membuffer_22() {
        SegmentedMemBuffer space = new SegmentedMemBuffer();
        writeread2a(space);
    }

    @Test public void membuffer_23() {
        SegmentedMemBuffer space = new SegmentedMemBuffer();
        writeread1(space);
        space.truncate(0);
        writeread2a(space);
    }

    @Test public void membuffer_31() {
        SegmentedMemBuffer space = new SegmentedMemBuffer(2);
        writeread1a(space);
    }

    @Test public void membuffer_32() {
        SegmentedMemBuffer space = new SegmentedMemBuffer(2);
        writeread2(space);
    }

    @Test public void membuffer_33() {
        SegmentedMemBuffer space = new SegmentedMemBuffer(2);
        writeread1a(space);
        space.truncate(0);
        writeread2a(space);
    }

    private void writeread1(SegmentedMemBuffer space) {
        long x = space.length();
        space.write(x, data1);
        assertEquals(x+data1.length, space.length());
        byte[] bytes2 = new byte[data1.length+10];
        int y = space.read(x, bytes2);
        assertEquals(data1.length, y);
        byte[] bytes3 = Arrays.copyOf(bytes2, y);
        assertArrayEquals(data1, bytes3);
    }

    private void writeread2(SegmentedMemBuffer space) {
        // Offset.
        space.write(0, data2);
        long x = data2.length;
        space.write(x, data1);
        assertEquals(x+data1.length, space.length());
        byte[] bytes2 = new byte[data1.length+10];
        int y = space.read(x, bytes2);
        assertEquals(data1.length, y);
        byte[] bytes3 = Arrays.copyOf(bytes2, y);
        assertArrayEquals(data1, bytes3);
    }

    private void writeread1a(SegmentedMemBuffer space) {
        long x = space.length();
        ByteBuffer bb1 = ByteBuffer.wrap(data1);
        space.write(x, bb1);
        assertEquals(x+data1.length, space.length());
        ByteBuffer bb2 = ByteBuffer.allocate(data1.length);
        int y = space.read(x, bb2);
        assertEquals(data1.length, y);
        byte[] bytes3 = Arrays.copyOf(bb2.array(), y);
        assertArrayEquals(data1, bytes3);
    }

    private void writeread2a(SegmentedMemBuffer space) {
        // Offset.
        space.write(0,  ByteBuffer.wrap(data2));
        long x = data2.length;
        ByteBuffer bb1 = ByteBuffer.wrap(data1);
        space.write(x, bb1);
        assertEquals(x+data1.length, space.length());
        ByteBuffer bb2 = ByteBuffer.allocate(data1.length);
        int y = space.read(x, bb2);
        assertEquals(data1.length, y);
        byte[] bytes3 = Arrays.copyOf(bb2.array(), y);
        assertArrayEquals(data1, bytes3);
    }

}

