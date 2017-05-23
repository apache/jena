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

package org.seaborne.dboe.base.file;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.io.IOException;

import org.apache.jena.atlas.lib.FileOps;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seaborne.dboe.base.file.ProcessFileLock;
import org.seaborne.dboe.sys.Names;

public class TestProcessFileLock {
    // TestLocationLockStoreConnection
    
    private static final String DIR  = "target/locktest";
    private static final String lockfile = DIR+"/"+Names.TDB_LOCK_FILE;
    
    
    @BeforeClass public static void beforeClass() {
        FileOps.ensureDir(DIR);
    }
    
    @Before public void beforeTest() {
        File f = new File(lockfile);
        try {
            f.delete();
            f.createNewFile();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test public void process_lock_1() {
        ProcessFileLock lock = ProcessFileLock.create(lockfile);
        String fn = new File(lockfile).getAbsolutePath();
        assertEquals(fn, lock.getPath().toString());
    }
    
    @Test public void process_lock_2() {
        ProcessFileLock lock1 = ProcessFileLock.create(lockfile);
        ProcessFileLock lock2 = ProcessFileLock.create(lockfile);
        assertSame(lock1, lock2);
    }

    @Test public void process_lock_3() {
        ProcessFileLock lock1 = ProcessFileLock.create(lockfile);
        ProcessFileLock.release(lock1);
        ProcessFileLock lock2 = ProcessFileLock.create(lockfile);
        assertNotSame(lock1, lock2);
    }
    
    @Test public void process_lock_4() {
        ProcessFileLock lock = ProcessFileLock.create(lockfile);
        assertFalse(lock.isLockedHere());
        lock.lockEx();
        assertTrue(lock.isLockedHere());
        lock.unlock();
        assertFalse(lock.isLockedHere());
    }

    @Test(expected=AlreadyLocked.class)
    public void process_lock_5() {
        ProcessFileLock lock = ProcessFileLock.create(lockfile);
        lock.lockEx();
        lock.lockEx();
    }
    
    @Test(expected=AlreadyLocked.class)
    public void process_lock_6() {
        ProcessFileLock lock = ProcessFileLock.create(lockfile);
        lock.lockEx();
        boolean b = lock.tryLock();
        assertFalse(b);
    }

    @Test(expected=AlreadyLocked.class)
    public void process_lock_7() {
        ProcessFileLock lock = ProcessFileLock.create(lockfile);
        lock.tryLock();
        lock.tryLock();
    }
}
