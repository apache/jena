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

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.dboe.base.file.BinaryDataFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public abstract class AbstractTestBinaryDataFile {
    private BinaryDataFile file;
    protected abstract BinaryDataFile createBinaryDataFile();

    static final String stringData = "Hello world\n";
    static final byte[] data = StrUtils.asUTF8bytes(stringData);

    // Default action.
    protected void releaseBinaryDataFile(BinaryDataFile file) {
        file.close();
    }

    @Before public void before() {
        file = createBinaryDataFile();
        file.open();
        file.truncate(0);
    }

    @After public void after() {
        releaseBinaryDataFile(file);
    }

    @Test public void basic_open() {
        assertTrue(file.isOpen());
    }

    @Test public void basic_open_close() {
        assertTrue(file.isOpen());
        file.close();
        assertFalse(file.isOpen());
    }

    @Test public void writeread_01() {
        assertEquals(0, file.length());
    }

    @Test public void writeread_02() {
        assertEquals(0, file.length());
        file.write(data);
        assertNotEquals(0, file.length());
        assertEquals(data.length, file.length());
    }

    @Test public void writeread_03() {
        long x = file.write(data);
        byte[] data2 = new byte[data.length+100];
        int x1 = file.read(x, data2);
        assertEquals(data.length, x1);
        int x2 = file.read(data.length, data2);
        assertEquals(-1, x2);
    }


}

