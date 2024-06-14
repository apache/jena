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

package org.apache.jena.fuseki.servlets.prefixes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsTests {
    @Test
    public void prefixIsValidTrue0() {
        assertTrue(PrefixUtils.prefixIsValid("prefix1"));
    }

    @Test
    public void prefixIsValidTrue1() {
        assertTrue(PrefixUtils.prefixIsValid("pr.efix1"));
    }

    @Test
    public void prefixIsValidTrue2() {
        assertTrue(PrefixUtils.prefixIsValid("pr-efix1"));
    }

    @Test
    public void prefixIsValidTrue3() {
        assertTrue(PrefixUtils.prefixIsValid("p"));
    }

    @Test
    public void prefixIsValidTrue4() {
        assertTrue(PrefixUtils.prefixIsValid("ca7--t"));
    }
    @Test
    public void prefixIsValidTrue5() {
        assertTrue(PrefixUtils.prefixIsValid("a__b"));
    }
    @Test
    public void prefixIsValidFalse0() {
        assertFalse(PrefixUtils.prefixIsValid("-prefix1"));
    }

    @Test
    public void prefixIsValidFalse1() {
        assertFalse(PrefixUtils.prefixIsValid("prefix1-"));
    }

    @Test
    public void prefixIsValidFalse2() {
        assertFalse(PrefixUtils.prefixIsValid(""));
    }

    @Test
    public void prefixIsValidFalse4() {
        assertFalse(PrefixUtils.prefixIsValid("c-b--"));
    }

    @Test
    public void prefixIsValidFalse5() {
        assertFalse(PrefixUtils.prefixIsValid("pre/fix"));
    }

    @Test
    public void uriIsValidTrue0() {
        assertTrue(PrefixUtils.uriIsValid("http://www.w3.org/"));
    }

    @Test
    public void uriIsValidFalse0() {
        assertFalse(PrefixUtils.uriIsValid("..."));
    }

}
