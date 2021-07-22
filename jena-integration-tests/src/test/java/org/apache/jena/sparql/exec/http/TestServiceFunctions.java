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

package org.apache.jena.sparql.exec.http;

import static org.junit.Assert.assertEquals;

import org.apache.jena.atlas.logging.LogCtl;
import org.junit.Test;

public class TestServiceFunctions {
    @Test public void service_timeout_1() {
        long x = Service.parseTimeout(Integer.valueOf(56));
        assertEquals(56, x);
    }
    @Test public void service_timeout_2() {
        long x = Service.parseTimeout("1045");
        assertEquals(1045, x);
    }

    @Test public void service_timeout_3() {
        long x = Service.parseTimeout(null);
        assertEquals(-1L, x);
    }

    @Test public void service_timeout_4() {
        String level = LogCtl.getLevel(Service.class);
        try {
            LogCtl.setLevel(Service.class, "ERROR");
            long x = Service.parseTimeout("Not a number");
            assertEquals(-1L, x);
        } finally {
            LogCtl.setLevel(Service.class, level);
        }
    }
}
