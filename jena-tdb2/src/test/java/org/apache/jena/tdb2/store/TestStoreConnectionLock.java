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

package org.apache.jena.tdb2.store;

import static org.junit.Assert.*;

import org.apache.jena.dboe.base.file.AlreadyLocked;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.base.file.ProcessFileLock;
import org.apache.jena.tdb2.sys.StoreConnection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for {@link ProcessFileLock} in conjunction with {@link StoreConnection}s
 */
public class TestStoreConnectionLock {
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void lock_store_connection_01() {
        Location dir = Location.create(tempDir.getRoot().getAbsolutePath());
        ProcessFileLock lock = StoreConnection.lockForLocation(dir);
        assertFalse(lock.isLockedHere());

        StoreConnection sConn = StoreConnection.connectCreate(dir);
        assertEquals(dir, sConn.getLocation());
        assertEquals(lock, sConn.getLock());
        assertTrue(lock.isLockedHere());

        StoreConnection.release(dir);
        assertFalse(lock.isLockedHere());
    }

    @Test(expected=AlreadyLocked.class)
    public void lock_store_connection_02() {
        Location dir = Location.create(tempDir.getRoot().getAbsolutePath());
        ProcessFileLock lock = StoreConnection.lockForLocation(dir);
        lock.lockEx();
        StoreConnection sConn = StoreConnection.connectCreate(dir);
    }
//        Location dir = Location.create(tempDir.getRoot().getAbsolutePath());
//        // Creating a StoreConnection on the location will obtain the lock
//         StoreConnection.make(dir);
//        // Releasing the connection releases the lock
//        StoreConnection.release(dir);
//    }
//
//    @Test(expected = TDBException.class)
//    public void location_lock_store_connection_02() throws IOException {
//        Location dir = Location.create(tempDir.getRoot().getAbsolutePath());
//        StoreConnection.make(dir);
//    }
}
