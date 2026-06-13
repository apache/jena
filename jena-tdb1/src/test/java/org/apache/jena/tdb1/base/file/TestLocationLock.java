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

package org.apache.jena.tdb1.base.file;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.apache.jena.tdb1.TDB1Exception;
import org.apache.jena.tdb1.sys.ProcessUtils;

/**
 * Tests for {@link LocationLock}
 */
public class TestLocationLock {

    private static boolean negativePidsTreatedAsAlive = false;

    @TempDir
    public Path tempDir;

    @BeforeAll
    public static void setup() {
        negativePidsTreatedAsAlive = ProcessUtils.negativePidsTreatedAsAlive();
    }

    @Test
    public void location_lock_mem() {
        Location mem = Location.mem();
        LocationLock lock = mem.getLock();
        assertFalse(lock.canLock());
        assertFalse(lock.isLocked());
        assertFalse(lock.isOwned());
        assertFalse(lock.canObtain());
    }

    @Test
    public void location_lock_dir_01() {
        Location dir = Location.create(tempDir.toAbsolutePath().toString());
        LocationLock lock = dir.getLock();
        assertTrue(lock.canLock());
        assertFalse(lock.isLocked());
        assertFalse(lock.isOwned());
        assertTrue(lock.canObtain());

        // Try to obtain the lock
        lock.obtain();
        assertTrue(lock.isLocked());
        assertTrue(lock.isOwned());

        // Release the lock
        lock.release();
        assertFalse(lock.isLocked());
        assertFalse(lock.isOwned());
    }

    @Test
    public void location_lock_dir_02() throws IOException {
        assumeTrue(negativePidsTreatedAsAlive);

        Location dir = Location.create(tempDir.toAbsolutePath().toString());
        LocationLock lock = dir.getLock();
        assertTrue(lock.canLock());
        assertFalse(lock.isLocked());
        assertFalse(lock.isOwned());
        assertTrue(lock.canObtain());

        // Write a fake PID to the lock file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dir.getPath("tdb.lock"), StandardCharsets.UTF_8))) {
            writer.write(Integer.toString(-1234)); // Fake PID that would never
                                                   // be valid
        }
        assertTrue(lock.isLocked());
        assertFalse(lock.isOwned());
        assertFalse(lock.canObtain());
    }

    @Test
    public void location_lock_dir_error_01() throws IOException {
        assumeTrue(negativePidsTreatedAsAlive);

        Location dir = Location.create(tempDir.toAbsolutePath().toString());
        LocationLock lock = dir.getLock();
        assertTrue(lock.canLock());
        assertFalse(lock.isLocked());
        assertFalse(lock.isOwned());
        assertTrue(lock.canObtain());

        // Write a fake PID to the lock file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dir.getPath("tdb.lock"), StandardCharsets.UTF_8))) {
            // Fake PID that would never be valid
            writer.write(Integer.toString(-1234));
        }
        assertTrue(lock.isLocked());
        assertFalse(lock.isOwned());

        // Attempting to obtain the lock should now error
        assertFalse(lock.canObtain());
        assertThrows(TDB1Exception.class, ()->lock.obtain());
    }

    @Test
    public void location_lock_dir_error_02() throws IOException {
        assumeTrue(negativePidsTreatedAsAlive);

        Location dir = Location.create(tempDir.toAbsolutePath().toString());
        LocationLock lock = dir.getLock();
        assertTrue(lock.canLock());
        assertFalse(lock.isLocked());
        assertFalse(lock.isOwned());
        assertTrue(lock.canObtain());

        // Write a fake PID to the lock file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dir.getPath("tdb.lock"), StandardCharsets.UTF_8))) {
            // Fake PID that would never be valid
            writer.write(Integer.toString(-1234));
        }
        assertTrue(lock.isLocked());
        assertFalse(lock.isOwned());

        // Attempting to release a lock we don't own should error
        assertFalse(lock.canObtain());
        assertThrows(TDB1Exception.class, ()->lock.release());
    }

    @Test
    public void location_lock_dir_error_03() throws IOException {
        assumeTrue(negativePidsTreatedAsAlive);

        Location dir = Location.create(tempDir.toAbsolutePath().toString());
        LocationLock lock = dir.getLock();
        assertTrue(lock.canLock());
        assertFalse(lock.isLocked());
        assertFalse(lock.isOwned());
        assertTrue(lock.canObtain());

        // Write a TDB1 format lock file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dir.getPath("tdb.lock"), StandardCharsets.UTF_8))) {
            // TDB1 format lock file, this writes a new line to the end of the lock file
            writer.write(Integer.toString(-1234));
            writer.write('\n');
        }

        // Trying to get the owner should error accordingly
        FileException e = assertThrows(FileException.class, ()->lock.canObtain());
        String errMsg = e.getMessage();
        assertNotNull(errMsg);
        assertTrue(errMsg.contains("appear to be for a TDB2 database"));
    }
}
