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

package org.apache.jena.atlas.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests for operations in {@link Lib} */
public class TestBaseLib {
    @Test public void lowercase_1() { assertEquals("",    Lib.lowercase("")); }
    @Test public void lowercase_2() { assertEquals("abc", Lib.lowercase("AbC")); }
    @Test public void lowercase_3() { assertEquals("abc", Lib.lowercase("ABC")); }
    @Test public void lowercase_4() { assertEquals("abc", Lib.lowercase("abc")); }

    @Test public void uppercase_1() { assertEquals("",    Lib.uppercase("")); }
    @Test public void uppercase_2() { assertEquals("ABC", Lib.uppercase("AbC")); }
    @Test public void uppercase_3() { assertEquals("ABC", Lib.uppercase("ABC")); }
    @Test public void uppercase_4() { assertEquals("ABC", Lib.uppercase("abc")); }

    @Test public void isEmpty_1() { assertEquals(true, Lib.isEmpty("")); }
    @Test public void isEmpty_2() { assertEquals(true, Lib.isEmpty(null)); }
    @Test public void isEmpty_3() { assertEquals(false, Lib.isEmpty("X")); }

    @Test public void concatpaths_1() { assertEquals("A/B", Lib.concatPaths("A", "B")); }
    @Test public void concatpaths_2() { assertEquals("A/B", Lib.concatPaths("A/", "B")); }
    @Test public void concatpaths_3() { assertEquals("/B", Lib.concatPaths("A", "/B")); }
    @Test public void concatpaths_4() { assertEquals("A", Lib.concatPaths("A", "")); }
    @Test public void concatpaths_5() { assertEquals("A/", Lib.concatPaths("A/", "")); }
}
