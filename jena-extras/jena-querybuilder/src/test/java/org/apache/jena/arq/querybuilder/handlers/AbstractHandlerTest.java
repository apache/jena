/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder.handlers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.arq.AbstractRegexpBasedTest;

public abstract class AbstractHandlerTest extends AbstractRegexpBasedTest {

    protected final static String[] byLine(String s) {
        return s.split("\n");
    }

    protected final static void assertContains(String expected, String[] lst) {
        List<String> s = Arrays.asList(lst);
        assertTrue(String.format("%s not found in %s", expected, s), s.contains(expected));
    }

    protected final static void assertNotContains(String expected, String[] lst) {
        List<String> s = Arrays.asList(lst);
        assertFalse(String.format("%s found in %s", expected, s), s.contains(expected));
    }

}
