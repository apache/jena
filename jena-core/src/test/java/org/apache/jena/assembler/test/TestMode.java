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

package org.apache.jena.assembler.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.assembler.Mode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Test;

public class TestMode {

    @Test
    public void testConstantsExist() {
        Mode a = Mode.CREATE, b = Mode.DEFAULT;
        Mode c = Mode.REUSE, d = Mode.ANY;

        assertNotEquals(Mode.CREATE, Mode.DEFAULT);
        assertNotEquals(Mode.CREATE, Mode.REUSE);
        assertNotEquals(Mode.CREATE, Mode.ANY);
        assertNotEquals(Mode.DEFAULT, Mode.REUSE);
        assertNotEquals(Mode.DEFAULT, Mode.ANY);
        assertNotEquals(Mode.REUSE, Mode.ANY);
    }

    private static final String someName = "aName";
    private static final Resource someRoot = ResourceFactory.createResource("http://example/aRoot");

    @Test
    public void testCreate() {
        assertTrue(Mode.CREATE.permitCreateNew(someRoot, someName));
        assertFalse(Mode.CREATE.permitUseExisting(someRoot, someName));
    }

    @Test
    public void testReuse() {
        assertFalse(Mode.REUSE.permitCreateNew(someRoot, someName));
        assertTrue(Mode.REUSE.permitUseExisting(someRoot, someName));
    }

    @Test
    public void testAny() {
        assertTrue(Mode.ANY.permitCreateNew(someRoot, someName));
        assertTrue(Mode.ANY.permitUseExisting(someRoot, someName));
    }

    @Test
    public void testDefault() {
        assertFalse(Mode.DEFAULT.permitCreateNew(someRoot, someName));
        assertTrue(Mode.DEFAULT.permitUseExisting(someRoot, someName));
    }
}
