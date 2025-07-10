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

package org.apache.jena.sparql.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import org.apache.jena.sparql.engine.Timeouts;
import org.apache.jena.sparql.engine.Timeouts.Timeout;

public class TestTimeouts {
    @Test
    public void testUnset() {
        Timeout timeout = roundtrip(Timeout.UNSET);
        String str = Timeouts.toString(timeout);
        assertNull(str);
    }

    @Test
    public void testInitialTimeout() {
        Timeout timeout = roundtrip(new Timeout(6, -1));
        String str = Timeouts.toString(timeout);
        assertEquals("6,-1", str);
    }

    @Test
    public void testOverallTimeout() {
        Timeout timeout = roundtrip(new Timeout(-1, 6));
        String str = Timeouts.toString(timeout);
        assertEquals("6", str);
    }

    @Test
    public void testInitialAndOverallTimeout() {
        Timeout timeout = roundtrip(new Timeout(6, 6));
        String str = Timeouts.toString(timeout);
        assertEquals("6,6", str);
    }

    public static Timeout roundtrip(Timeout timeout) {
        Object obj = Timeouts.toContextValue(timeout);
        Timeout result = Timeouts.parseTimeout(obj);
        assertEquals(timeout, result);
        return result;
    }
}
