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

import org.junit.Assert ;
import org.junit.Test ;

public class TestSysRIOT {
    @Test public void chooseBaseIRI_1() {
        testChooseBaseIRI("http://example/foo/bar", "http://example/foo/bar") ;
    }

    @Test public void chooseBaseIRI_2() {
        testChooseBaseIRI("-", "http://localhost/stdin/") ;
    }

    @Test public void chooseBaseIRI_10() {
        String x = SysRIOT.chooseBaseIRI(null, "foo") ;
        Assert.assertTrue(x, x.startsWith("file:///"));
    }

    private void testChooseBaseIRI(String input, String expected) {
        String x = SysRIOT.chooseBaseIRI(null, input) ;
        Assert.assertEquals(expected, x) ;
    }
}
