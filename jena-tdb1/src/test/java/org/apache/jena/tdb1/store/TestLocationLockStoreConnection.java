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

package org.apache.jena.tdb1.store;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.tdb1.StoreConnection;
import org.apache.jena.tdb1.TDB1Exception;
import org.apache.jena.tdb1.base.file.Location;
import org.apache.jena.tdb1.base.file.LocationLock;
import org.apache.jena.tdb1.sys.ProcessUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for {@link LocationLock} inconjucntion with {@link StoreConnection}s 
 */
public class TestLocationLockStoreConnection {

    private static boolean negativePidsTreatedAsAlive = false;
    
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    
    @BeforeClass
    public static void setup() {
        negativePidsTreatedAsAlive = ProcessUtils.negativePidsTreatedAsAlive();
    }
    
    @Test
    public void location_lock_store_connection_01() {
        Location dir = Location.create(tempDir.getRoot().getAbsolutePath());
        LocationLock lock = dir.getLock();
        Assert.assertTrue(lock.canLock());
        Assert.assertFalse(lock.isLocked());
        Assert.assertFalse(lock.isOwned());
        Assert.assertTrue(lock.canObtain());

        // Creating a StoreConnection on the location will obtain the lock
        StoreConnection.make(dir);
        Assert.assertTrue(lock.isLocked());
        Assert.assertTrue(lock.isOwned());
        Assert.assertTrue(lock.canObtain());

        // Releasing the connection releases the lock
        StoreConnection.release(dir);
        Assert.assertFalse(lock.isLocked());
        Assert.assertFalse(lock.isOwned());
        Assert.assertTrue(lock.canObtain());
    }

    @Test(expected = TDB1Exception.class)
    public void location_lock_store_connection_02() throws IOException {
        Assume.assumeTrue(negativePidsTreatedAsAlive);
        
        Location dir = Location.create(tempDir.getRoot().getAbsolutePath());
        LocationLock lock = dir.getLock();
        Assert.assertTrue(lock.canLock());
        Assert.assertFalse(lock.isLocked());
        Assert.assertFalse(lock.isOwned());
        Assert.assertTrue(lock.canObtain());

        // Write a fake PID to the lock file
        try(BufferedWriter writer = 
            new BufferedWriter(new FileWriter(dir.getPath("tdb.lock")))) {
            // Fake PID that would never be valid
            writer.write(Integer.toString(-1234)); 
        }
        Assert.assertTrue(lock.isLocked());
        Assert.assertFalse(lock.isOwned());

        // Attempting to create a connection on this location should error
        StoreConnection.make(dir);
    }
}
