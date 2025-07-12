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

package org.apache.jena.riot;

import static org.apache.jena.atlas.lib.Lib.lowercase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.base.Sys;

public class TestSysRIOT {
    @Test
    public void chooseBaseIRI_1() {
        testChooseBaseIRI("http://example/foo/bar", "http://example/foo/bar");
    }

    @Test
    public void chooseBaseIRI_2() {
        testChooseBaseIRI("-", "http://localhost/stdin/");
    }

    // "c:" exists (almost always)
    @Test
    public void chooseBaseIRI_3() {
        if ( Sys.isWindows ) {
            if ( IO.exists("c:/") )
                testChooseBaseIRI_windows("c:/", "file:///c:/");
        } else
            testChooseBaseIRI("x:", "x:");
    }

    @Test
    public void chooseBaseIRI_4() {
        if ( Sys.isWindows ) {
            if ( IO.exists("c:/") )
                testChooseBaseIRI_windows("c:", "file:///c:");
        } else
            testChooseBaseIRI("x:/", "x:/");
    }

    @Test
    public void chooseBaseIRI_10() {
        String x = SysRIOT.chooseBaseIRI(null, "foo");
        assertTrue(x.startsWith("file:///"));
    }

    private void testChooseBaseIRI(String input, String expected) {
        String x = SysRIOT.chooseBaseIRI(null, input);
        assertEquals(expected, x);
    }

    private void testChooseBaseIRI_windows(String input, String prefix) {
        String x = SysRIOT.chooseBaseIRI(null, input);
        String x1 = lowercase(x);
        boolean b = x1.startsWith(prefix);
        if ( ! b )
            System.out.printf("Input: %s => (prefix(%s)  A:%s)=\n", input, prefix, x1);
        // drive letters can be uppercase.
        assertTrue(x1.startsWith(prefix));
    }
}
