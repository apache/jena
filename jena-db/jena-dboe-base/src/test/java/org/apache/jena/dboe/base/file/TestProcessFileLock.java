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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.apache.jena.dboe.sys.Names;

public class TestProcessFileLock {

    private String lockfile;
    private Path lockfilePath;
    @TempDir Path tempDir;

    @BeforeEach public void beforeTest() {
        lockfilePath = tempDir.resolve(Names.TDB_LOCK_FILE).toAbsolutePath();
        try {
            Files.createFile(lockfilePath);
            lockfile = lockfilePath.toRealPath().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test public void process_lock_1() throws IOException {
        ProcessFileLock lock = ProcessFileLock.create(lockfile);
        assertTrue(Files.isSameFile(lock.getPath(),lockfilePath), "Not the same file");
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

    @Test
    public void process_lock_5() {
        ProcessFileLock lock = ProcessFileLock.create(lockfile);
        lock.lockEx();
        assertThrows(AlreadyLocked.class, ()->lock.lockEx());
    }

    @Test
    public void process_lock_6() {
        ProcessFileLock lock = ProcessFileLock.create(lockfile);
        lock.lockEx();
        // Held by this process => exception
        assertThrows(AlreadyLocked.class, ()->lock.tryLock());
    }

    @Test
    public void process_lock_7() {
        ProcessFileLock lock = ProcessFileLock.create(lockfile);
        lock.tryLock();
        // Held by this process => exception
        assertThrows(AlreadyLocked.class, ()->lock.tryLock());
    }
}
