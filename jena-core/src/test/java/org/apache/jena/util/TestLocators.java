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

package org.apache.jena.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.apache.jena.test.JenaTestLib;

@SuppressWarnings("deprecation")
public class TestLocators {
    private static final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
    private static final ClassLoader otherClassLoader = new ClassLoader() {};

    @Test
    public void testClassLoaderLocatorEquality() {
        Locator A1 = new LocatorClassLoader(systemClassLoader);
        Locator A2 = new LocatorClassLoader(systemClassLoader);
        Locator B = new LocatorClassLoader(otherClassLoader);
        testLocatorEquality(A1, A2, B);
    }

    private void testLocatorEquality(Locator A1, Locator A2, Locator B) {
        assertEquals(A1, A1);
        assertEquals(A2, A2);
        assertEquals(A1, A2);
        assertEquals(A2, A1);
        assertEquals(B, B);
        JenaTestLib.assertDiffer(A1, B);
        JenaTestLib.assertDiffer(B, A1);
    }

    @Test
    public void testClassLoaderLocatorHashcode() {
        assertEquals(systemClassLoader.hashCode(), new LocatorClassLoader(systemClassLoader).hashCode());
        assertEquals(otherClassLoader.hashCode(), new LocatorClassLoader(otherClassLoader).hashCode());
    }

    @Test
    public void testLocatorFileEquality() {
        Locator A1 = new LocatorFile("foo/bar");
        Locator A2 = new LocatorFile("foo/bar");
        Locator B = new LocatorFile("bill/ben");
        testLocatorEquality(A1, A2, B);
    }

    @Test
    public void testLocatorFileHashcode() {
        testLocatorFileHashCode("foo/bar");
        testLocatorFileHashCode("bill/ben");
        testLocatorFileHashCode("another/night");
    }

    private void testLocatorFileHashCode(String dirName) {
        assertEquals(dirName.hashCode(), new LocatorFile(dirName).hashCode());
    }

    @Test
    public void testLocatorURLEquality() {
        Locator A1 = new LocatorURL();
        Locator A2 = new LocatorURL();
        assertEquals(A1, A2);
        JenaTestLib.assertDiffer(A1, "");
    }

    @Test
    public void testLocatorURLHashcode() {
        assertEquals(LocatorURL.class.hashCode(), new LocatorURL().hashCode());
    }
}
