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

package org.apache.jena.dboe.base.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.nio.MappedByteBuffer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.dboe.ConfigTestDBOE;
import org.apache.jena.dboe.base.block.Block;

/**
 * The shared per-segment {@link MappedByteBuffer} in {@link BlockAccessMapped}
 * must never have its position/limit mutated by block access.
 *
 * <p>The previous implementation sliced blocks with a
 * position/limit/slice/reset-limit sequence on the shared segment buffer. If
 * anything threw between the limit-shrink and the reset (e.g. an
 * {@link OutOfMemoryError} during {@code slice()}), the segment buffer was left
 * with a shrunken limit and every later access to a higher block in that
 * segment failed with {@code IllegalArgumentException: newPosition &gt; limit}
 * from {@code Buffer.position(int)} - observed in production as persistent 500s
 * on reads of a B+Tree index until restart. The fix slices with the absolute
 * {@code slice(index, length)}, which never touches the shared buffer's state.
 * These tests lock in that invariant.</p>
 */
public class TestBlockAccessMappedSegmentState {

    private static final int BlockSize = 64;
    private static int counter = 0;

    private String filename;
    private BlockAccessMapped file;

    @BeforeEach public void before() {
        filename = ConfigTestDBOE.getTestingDir() + "/test-segment-state-" + (counter++);
        FileOps.deleteSilent(filename);
        file = new BlockAccessMapped(filename, BlockSize);
    }

    @AfterEach public void after() {
        file.close();
        FileOps.deleteSilent(filename);
    }

    private Block writePatternBlock(int marker) {
        Block b = file.allocate(BlockSize);
        for ( int i = 0; i < BlockSize; i++ )
            b.getByteBuffer().put(i, (byte)((marker + i) & 0xFF));
        file.write(b);
        return b;
    }

    private MappedByteBuffer segmentBuffer(int seg) throws Exception {
        Field f = BlockAccessMapped.class.getDeclaredField("segments");
        f.setAccessible(true);
        MappedByteBuffer[] segments = (MappedByteBuffer[])f.get(file);
        return segments[seg];
    }

    @Test
    public void sharedSegmentBufferStateUntouchedByAccess() throws Exception {
        final int numBlocks = 20;
        Block[] blocks = new Block[numBlocks];
        for ( int i = 0; i < numBlocks; i++ )
            blocks[i] = writePatternBlock(i * 7);

        // Interleave reads in an order that, with the old position/limit
        // slicing, walked the shared buffer's position and limit up and down.
        // End on a high block: the old implementation left position at that
        // block's segment offset.
        for ( int i = 0; i < numBlocks; i++ )
            file.read(blocks[i].getId().longValue());
        file.read(blocks[0].getId().longValue());
        file.read(blocks[numBlocks - 1].getId().longValue());

        MappedByteBuffer seg0 = segmentBuffer(0);
        assertNotNull(seg0, "segment 0 should be mapped after block access");
        System.out.printf("segment 0 after %d writes + %d reads: position=%d limit=%d capacity=%d%n",
                          numBlocks, numBlocks + 2, seg0.position(), seg0.limit(), seg0.capacity());

        // A shrunken limit is the "newPosition > limit" poisoning vector.
        assertEquals(seg0.capacity(), seg0.limit(),
                     "shared segment buffer limit was shrunk by block access - "
                     + "an exception mid-access would poison all higher blocks in the segment");
        assertEquals(0, seg0.position(),
                     "shared segment buffer position was mutated by block access");
    }

    @Test
    public void blockRoundTripAfterMixedAccess() {
        final int numBlocks = 8;
        Block[] written = new Block[numBlocks];
        for ( int i = 0; i < numBlocks; i++ )
            written[i] = writePatternBlock(i * 31);

        for ( int i = 0; i < numBlocks; i++ ) {
            Block back = file.read(written[i].getId().longValue());
            assertEquals(0, back.getByteBuffer().position(), "returned block slice should start at position 0");
            assertEquals(BlockSize, back.getByteBuffer().capacity(), "returned block slice capacity");
            for ( int j = 0; j < BlockSize; j++ ) {
                byte expected = (byte)((i * 31 + j) & 0xFF);
                byte actual = back.getByteBuffer().get(j);
                if ( expected != actual )
                    System.out.printf("MISMATCH block=%d byte=%d expected=%02x actual=%02x%n",
                                      i, j, expected, actual);
                assertEquals(expected, actual,
                             String.format("content mismatch: block=%d byte=%d", i, j));
            }
        }
    }

    /**
     * Deterministic reproduction of the production failure.
     *
     * <p>Production stack: {@code IllegalArgumentException: newPosition > limit}
     * from {@code Buffer.position(int)} inside {@code getByteBuffer} while
     * reading a B+Tree node - every read of a high block in the segment kept
     * failing (HTTP 500s) until restart. The poisoned state - a shrunken limit
     * on the shared segment buffer - is what an asynchronous throwable (e.g.
     * OOME) escaping the old position/limit/slice/reset-limit window left
     * behind. The trigger itself cannot be provoked deterministically, so this
     * test injects the resulting state by reflection and verifies reads still
     * work. Against the old implementation this reproduces the production
     * exception exactly.</p>
     */
    @Test
    public void poisonedSegmentLimit_readsStillWork() throws Exception {
        final int numBlocks = 8;
        Block[] written = new Block[numBlocks];
        for ( int i = 0; i < numBlocks; i++ )
            written[i] = writePatternBlock(i * 13);

        MappedByteBuffer seg0 = segmentBuffer(0);
        seg0.limit(BlockSize); // block 0 only - every higher block is beyond the limit
        System.out.printf("injected poisoned segment state: limit=%d capacity=%d%n",
                          seg0.limit(), seg0.capacity());

        // Old implementation: read of any block >= 1 threw
        //   IllegalArgumentException: newPosition > limit: (segOff > 64)
        for ( int i = numBlocks - 1; i >= 1; i-- ) {
            Block back = file.read(written[i].getId().longValue());
            for ( int j = 0; j < BlockSize; j++ )
                assertEquals((byte)((i * 13 + j) & 0xFF), back.getByteBuffer().get(j),
                             String.format("content mismatch after poisoning: block=%d byte=%d", i, j));
        }
        System.out.printf("all %d high blocks readable despite poisoned shared-buffer limit%n",
                          numBlocks - 1);
    }

    @Test
    public void returnedSlicesAreIndependent() {
        Block b0 = writePatternBlock(1);
        Block b1 = writePatternBlock(101);

        Block r0 = file.read(b0.getId().longValue());
        Block r1 = file.read(b1.getId().longValue());

        // Moving one slice's position/limit must not disturb the other slice
        // or the shared segment buffer behind them.
        r0.getByteBuffer().position(BlockSize / 2);
        r0.getByteBuffer().limit(BlockSize / 2);

        assertEquals(0, r1.getByteBuffer().position(), "sibling slice position disturbed");
        assertEquals(BlockSize, r1.getByteBuffer().limit(), "sibling slice limit disturbed");

        Block again = file.read(b1.getId().longValue());
        assertEquals((byte)((101) & 0xFF), again.getByteBuffer().get(0), "content after sibling slice mutation");
    }
}
