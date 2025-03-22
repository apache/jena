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

package org.apache.jena.rfc3986;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.Test;

public class TestParseIPv4Address {
    @Test public void addr_ipv4_01()    { good4("15.16.17.18"); }
    @Test public void addr_ipv4_02()    { good4("2.16.17.1"); }
    @Test public void addr_ipv4_03()    { good4("192.168.0.1"); }
    @Test public void addr_ipv4_04()    { good4("255.255.255.255"); }
    @Test public void addr_ipv4_05()    { good4("127.0.0.0"); }

    @Test public void addr_ipv4_bad_00() { bad4(""); }
    @Test public void addr_ipv4_bad_01() { bad4("1.1"); }
    @Test public void addr_ipv4_bad_02() { bad4("1.2.3.4.5"); }
    @Test public void addr_ipv4_bad_03() { bad4(".1.2.3");  }
    @Test public void addr_ipv4_bad_04() { bad4("1..2.3.4"); }
    @Test public void addr_ipv4_bad_05() { bad4("1.2.3..4"); }

    @Test public void addr_ipv4_bad_06() { bad4("567.78.99.1"); }
    @Test public void addr_ipv4_bad_07() { bad4("1.256.78.99"); }
    @Test public void addr_ipv4_bad_08() { bad4("1.99.1111.9"); }
    @Test public void addr_ipv4_bad_09() { bad4("1.99.78.1111"); }
    @Test public void addr_ipv4_bad_10() { bad4(".123.123.123"); }
    @Test public void addr_ipv4_bad_11() { bad4("1.99.78.111."); }

    private void good4(String string) {
        ParseIPv4Address.checkIPv4(string);
    }

    private void bad4(String string) {
        assertThrowsExactly(IRIParseException.class, ()->ParseIPv4Address.checkIPv4(string));
    }
}
