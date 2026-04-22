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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.apache.jena.atlas.lib.FileOps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Regression test: {@link BinaryDataFileRandomAccess#truncate(long)} must reset
 * the internal {@code writePosition} field.
 *
 * <p>Without the reset, a subsequent {@code switchToWriteMode()} triggered by a
 * read-then-write sequence seeks the underlying {@link RandomAccessFile} to the
 * stale {@code writePosition} (past the just-truncated EOF). The next
 * {@code file.write(...)} extends the file at that position; the filesystem
 * zero-fills the range between the truncated length and the stale seek
 * position, leaving a contiguous zero gap inside an otherwise append-only
 * binary data file.
 *
 * <p>Under TDB2 this surfaces as {@code NodeTableTRDF/Read ->
 * TProtocolException: Unrecognized type 0} on any {@code nodes-data.obj}
 * lookup whose NodeId resolves into the zero gap.
 */
public class TestBinaryDataFileRandomAccessTruncate {

    private static final String FILE = "target/test-bdfra-truncate-writepos";

    private BinaryDataFile file;

    @BeforeEach public void before() {
        FileOps.delete(FILE);
        file = new BinaryDataFileRandomAccess(FILE);
        file.open();
    }

    @AfterEach public void after() {
        file.close();
        FileOps.delete(FILE);
    }

    /**
     * truncate() then write() (no intervening read) — file must contain only
     * the written bytes up to the new length.
     */
    @Test public void truncate_then_write_no_gap() {
        byte[] block = filled(300 * 1024, (byte) 0xAB);
        file.write(block);
        assertEquals(300 * 1024L, file.length());

        file.truncate(100 * 1024);
        assertEquals(100 * 1024L, file.length());

        byte[] tail = filled(100, (byte) 0xCD);
        long writtenAt = file.write(tail);
        assertEquals(100 * 1024L, writtenAt,
                "write() after truncate() must start at the truncated length, not at a stale writePosition");
        assertEquals(100 * 1024L + 100, file.length());

        assertNoZeroRun(FILE);
    }

    /**
     * truncate() then read() then write() — the read flips the internal
     * readMode flag, so the subsequent write goes through switchToWriteMode
     * and seeks to the stored writePosition. Without the fix, that position
     * is stale and writes past the truncated EOF, zero-filling the gap.
     */
    @Test public void truncate_then_read_then_write_no_gap() {
        byte[] block = filled(300 * 1024, (byte) 0xAB);
        file.write(block);

        file.truncate(100 * 1024);
        assertEquals(100 * 1024L, file.length());

        byte[] sample = new byte[16];
        file.read(0, sample);

        byte[] tail = filled(100, (byte) 0xCD);
        long writtenAt = file.write(tail);
        assertEquals(100 * 1024L, writtenAt,
                "write() after truncate()+read() must start at the truncated length, not at a stale writePosition");
        assertEquals(100 * 1024L + 100, file.length());

        assertNoZeroRun(FILE);
    }

    private static byte[] filled(int size, byte v) {
        byte[] b = new byte[size];
        Arrays.fill(b, v);
        return b;
    }

    /**
     * Fail the enclosing test if the on-disk file contains a contiguous run
     * of zero bytes of length >= 16. The source blocks are all 0xAB/0xCD so
     * any zero byte is evidence of filesystem zero-fill past a stale seek.
     */
    private static void assertNoZeroRun(String path) {
        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            long total = raf.length();
            byte[] buf = new byte[(int) total];
            raf.readFully(buf);
            int runStart = -1;
            int runLen = 0;
            for (int i = 0; i < buf.length; i++) {
                if (buf[i] == 0) {
                    if (runStart < 0) { runStart = i; runLen = 1; }
                    else runLen++;
                } else {
                    if (runLen >= 16) {
                        assertTrue(false,
                                "Zero-byte run of " + runLen + " bytes starting at offset " + runStart
                                        + " indicates a filesystem zero-fill (stale writePosition after truncate).");
                    }
                    runStart = -1;
                    runLen = 0;
                }
            }
            if (runLen >= 16) {
                assertTrue(false,
                        "Zero-byte run of " + runLen + " bytes at end of file starting at offset " + runStart);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
