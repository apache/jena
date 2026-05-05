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

package org.apache.jena.mem;

import org.junit.Test;

import static org.apache.jena.testing_framework.GraphHelper.triple;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * Concrete instantiation of {@link AbstractGraphMemTest} that exercises
 * {@link GraphMemFast} (a {@link GraphMem} backed by a
 * {@link org.apache.jena.mem.store.fast.FastTripleStore}). The shared
 * contract assertions live in the abstract base; this class only adds tests
 * that are specific to the {@code GraphMemFast} variant.
 */
public class GraphMemFastTest extends AbstractGraphMemTest {

    @Override
    protected GraphMem createGraph() {
        return new GraphMemFast();
    }

    @Test
    public void copyReturnsAGraphMemFastInstance() {
        sut.add(triple("s p o"));
        final var copy = sut.copy();
        // The override on GraphMemFast must preserve the runtime type so
        // callers don't lose subclass-specific functionality through copy().
        assertTrue("copy() must return a GraphMemFast", copy instanceof GraphMemFast);
        assertNotSame(sut, copy);
    }
}