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

package org.apache.jena.dboe.base.block;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestBlockMgrTracked
{
    // Mainly testing the tracking

    static boolean b;

    @BeforeAll
    static public void beforeClass() {
        b = BlockMgrTracker.verbose;
        BlockMgrTracker.verbose = false;
    }

    @AfterAll
    static public void afterClass() {
        BlockMgrTracker.verbose = b;
    }

    @Test public void track_01() {
        BlockMgr mgr = BlockMgrFactory.createMem("BPTRecord", 4);
        mgr = BlockMgrFactory.tracker(mgr);
        mgr.beginUpdate();
        Block block = mgr.allocate(4);
        ByteBuffer bb = block.getByteBuffer();
        bb.putInt(0,1234);
        mgr.write(block);
        mgr.release(block);
        // -----
        Block block2 = mgr.getRead(block.getId());
        ByteBuffer bb2 = block2.getByteBuffer();
        assertArrayEquals(bb.array(), bb2.array());
        mgr.release(block2);
        mgr.endUpdate();
    }

    // Multiple overlapping read operations.
    static BlockMgr setup() {
        BlockMgr mgr = BlockMgrFactory.createMem("BPTRecord", 4);
        mgr = BlockMgrFactory.tracker(mgr);
        return mgr;
    }

    static void write(BlockMgr mgr, int value) {
        mgr.beginUpdate();
        Block block = mgr.allocate(4);
        ByteBuffer bb = block.getByteBuffer();
        bb.putInt(0,value);
        mgr.write(block);
        mgr.release(block);
        mgr.endUpdate();
    }

    @Test public void track_02() {
        BlockMgr mgr = setup();
        write(mgr, 1234);
        write(mgr, 5678);

        mgr.beginRead();
        mgr.beginRead();

        Block b0 = mgr.getRead(0);
        Block b1 = mgr.getRead(1);

        mgr.release(b1);
        mgr.release(b0);

        mgr.endRead();
        mgr.endRead();
    }

    @Test
    public void track_03() {
        BlockMgr mgr = setup();
        write(mgr, 1234);
        write(mgr, 5678);

        mgr.beginRead();
        assertThrows(BlockException.class, ()->mgr.getWrite(0));
    }

    @Test
    public void track_04() {
        BlockMgr mgr = setup();
        write(mgr, 1234);
        mgr.beginRead();
        Block b0 = mgr.getRead(0);
        assertThrows(BlockException.class, ()->mgr.promote(b0));
    }

    @Test
    public void track_05() {
        BlockMgr mgr = setup();
        mgr.beginRead();
        assertThrows(BlockException.class, ()->mgr.endUpdate());
    }

    @Test
    public void track_06() {
        BlockMgr mgr = setup();
        mgr.beginUpdate();
        assertThrows(BlockException.class, ()->mgr.endRead());
    }

}
