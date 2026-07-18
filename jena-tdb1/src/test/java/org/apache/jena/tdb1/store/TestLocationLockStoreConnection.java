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

package org.apache.jena.tdb1.store;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.tdb1.ConfigTest;
import org.apache.jena.tdb1.TDB1Exception;
import org.apache.jena.tdb1.base.file.Location;
import org.apache.jena.tdb1.base.file.LocationLock;
import org.apache.jena.tdb1.sys.ProcessUtils;
import org.apache.jena.tdb1.sys.StoreConnection;
import org.apache.jena.tdb1.sys.TDBInternal;

/**
 * Tests for {@link LocationLock} in conjunction with {@link StoreConnection}s
 */
public class TestLocationLockStoreConnection {

    private static boolean negativePidsTreatedAsAlive = false;

    // Do not use @TempDir - deleted files don't get cleaned up
    // immediately on MS Windows and JUnit6 checks this when
    // cleaning @TempDir
    // See beforeEach, afterEach.
    public String tempDir;

    @BeforeAll
    public static void setup() {
        negativePidsTreatedAsAlive = ProcessUtils.negativePidsTreatedAsAlive();
    }

    @BeforeEach
    public void beforeEach() {
        tempDir = ConfigTest.getCleanDir()+"/store-location";
        FileOps.ensureDir(tempDir);
    }

    @SuppressWarnings("removal")
    @AfterEach
    public void afterEach() {
        TDBInternal.reset();
        FileOps.clearDirectory(tempDir);
    }

    @Test
    public void location_lock_store_connection_01() {
        Location dir = Location.create(tempDir);
        LocationLock lock = dir.getLock();
        assertTrue(lock.canLock());
        assertFalse(lock.isLocked());
        assertFalse(lock.isOwned());
        assertTrue(lock.canObtain());

        // Creating a StoreConnection on the location will obtain the lock
        StoreConnection.make(dir);
        assertTrue(lock.isLocked());
        assertTrue(lock.isOwned());
        assertTrue(lock.canObtain());

        // Releasing the connection releases the lock
        StoreConnection.release(dir);
        assertFalse(lock.isLocked());
        assertFalse(lock.isOwned());
        assertTrue(lock.canObtain());
    }

    @Test
    public void location_lock_store_connection_02() throws IOException {
        assumeTrue(negativePidsTreatedAsAlive);

        Location dir = Location.create(tempDir);
        LocationLock lock = dir.getLock();
        assertTrue(lock.canLock());
        assertFalse(lock.isLocked());
        assertFalse(lock.isOwned());
        assertTrue(lock.canObtain());

        // Write a fake PID to the lock file
        try(BufferedWriter writer =
            new BufferedWriter(new FileWriter(dir.getPath("tdb.lock"), StandardCharsets.UTF_8))) {
            // Fake PID that would never be valid
            writer.write(Integer.toString(-1234));
        }
        assertTrue(lock.isLocked());
        assertFalse(lock.isOwned());

        // Attempting to create a connection on this location should error
        assertThrows(TDB1Exception.class, ()->StoreConnection.make(dir));
    }
}
