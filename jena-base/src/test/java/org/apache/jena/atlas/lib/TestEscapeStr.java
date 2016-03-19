/**
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

import org.junit.Assert ;
import org.junit.Test ;

// Testing is a bit light here but the RDF term output and 
// the language level output covers the ground as well.
// See TestQuotedString in ARQ.

public class TestEscapeStr {
    
    @Test public void escape_str_01()   { test("", "") ; }
    @Test public void escape_str_02()   { test("A", "A") ; }
    @Test public void escape_str_03()   { test("\n", "\\n") ; }
    @Test public void escape_str_04()   { test("A\tB", "A\\tB") ; }
    @Test public void escape_str_05()   { test("\"", "\\\"") ; }

    private void test(String input, String expected) {
        String output = EscapeStr.stringEsc(input) ;
        Assert.assertEquals(expected, output);
    }
}
