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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class TestParseIPv6Address {
    @Test public void addr_ipv6_01() { good6("[0001:0002:0003:0004:0005:0006:0007:0008]"); }
    @Test public void addr_ipv6_02() { good6("[123:5678::ABCD:89EF]"); }
    @Test public void addr_ipv6_03() { good6("[123:5678::ABCD]"); }
    @Test public void addr_ipv6_04() { good6("[99::]"); }


    // IPv6 "unspecified address"
    @Test public void addr_ipv6_05() { good6("[::]"); }
    // IPv6 loopback address.
    @Test public void addr_ipv6_06() { good6("[::1]"); }

    @Test public void addr_ipv6_07() { good6("[98::15.16.17.18]"); }
    @Test public void addr_ipv6_08() { good6("[98::2.16.17.1]"); }
    @Test public void addr_ipv6_09() { good6("[::2.16.17.1]"); }
    @Test public void addr_ipv6_10() { good6("[1234:5678::123.123.123.123]"); }

    // Bad.
    @Test public void addr_ipv6_bad_00() { bad6("[]"); }
    @Test public void addr_ipv6_bad_01() { bad6("[1234:5678]"); }
    @Test public void addr_ipv6_bad_02() { bad6("[1234]"); }
    @Test public void addr_ipv6_bad_03() { bad6("[0001:0002:0003:0004:0005:0006:0007]"); }
    @Test public void addr_ipv6_bad_04() { bad6("[0001:0002:0003:0004:0005:0006:0007:0008:0009]"); }
    @Test public void addr_ipv6_bad_05() { bad6("[123Z:5678::1]"); }

    @Test public void addr_ipv6_bad_06() { bad6("[1234:1.2.3.4]"); }
    @Test public void addr_ipv6_bad_07() { bad6("[::1.1]"); }
    @Test public void addr_ipv6_bad_08() { bad6("[::99:1.2.3.4.5]"); }
    @Test public void addr_ipv6_bad_09() { bad6("[::99:.1.2.3]");  }
    @Test public void addr_ipv6_bad_10() { bad6("[::99:1..2.3.4]"); }
    @Test public void addr_ipv6_bad_11() { bad6("[::99:1.2.3..4]"); }

    @Test public void addr_ipv6_bad_20() { bad6("[::"); }
    @Test public void addr_ipv6_bad_21() { bad6("[1234::5678"); }
    @Test public void addr_ipv6_bad_22() { bad6("1234::5678]"); }
    @Test public void addr_ipv6_bad_23() { bad6("[1234::5678][1234:5678]"); }

    // IPv6 address with zone id
    // https://www.rfc-editor.org/rfc/rfc6874.html
    /*
     * IP-literal = "[" ( IPv6address / IPv6addrz / IPvFuture ) "]"
     *
     * ZoneID = 1*( unreserved / pct-encoded )
     *
     * IPv6addrz = IPv6address "%25" ZoneID
     */

    @Test public void addr_ipv6_zone_1() { good6("[fe80::a%25en1]"); }
    @Test public void addr_ipv6_zone_2() { good6("[fe80::2%253]"); }
    @Test public void addr_ipv6_zone_3() { good6("[0001:0002:0003:0004:0005:0006:0007:0008%25zone]"); }

    @Test public void addr_ipv6_zone_bad_1() { bad6("[%25link]"); }
    @Test public void addr_ipv6_zone_bad_2() { bad6("[fe80::%25link:]"); }
    @Test public void addr_ipv6_zone_bad_3() { bad6("[fe80::%2]"); }
    @Test public void addr_ipv6_zone_bad_4() { bad6("[fe80::%30abc]"); }
    @Test public void addr_ipv6_zone_bad_5() { bad6("[fe80::%20abc]"); }

    @Test public void addr_ipvFuture_1() { good6("[v7.ZZZZZZ]"); }
    @Test public void addr_ipvFuture_2() { good6("[vF.1:]"); }

    @Test public void addr_ipvFuture_bad_1() { bad6("[vH.1]"); }
    @Test public void addr_ipvFuture_bad_2() { bad6("[v.1]"); }
    @Test public void addr_ipvFuture_bad_3() { bad6("[v71]"); }
    @Test public void addr_ipvFuture_bad_4() { bad6("[v7.1]2]"); }
    @Test public void addr_ipvFuture_bad_5() { bad6("[v7.1@2]"); }

    @Test public void addr_ipv4_1()     { good6("[::1234:123.156.178.199]"); }
    @Test public void addr_ipv4_2()     { good6("[::1234:123.156.178.199]"); }

    @Test public void addr_ipv4_bad_2() { bad6("[::567.78.99.1]"); }
    @Test public void addr_ipv4_bad_3() { bad6("[::1234:1.256.78.99]"); }
    @Test public void addr_ipv4_bad_4() { bad6("[::1234:1.99.78.999]"); }
    @Test public void addr_ipv4_bad_5() { bad6("[::1234:1.99.78.1111]"); }
    @Test public void addr_ipv4_bad_6() { bad6("[::1234:.123.123.123]"); }
    @Test public void addr_ipv4_bad_7() { bad6("[::1234.56.78.99]"); }

    private void good6(String string) {
        ParseIPv6Address.checkIPv6(string);
    }

    private void bad6(String string) {
        assertThrows(IRIParseException.class, ()->ParseIPv6Address.checkIPv6(string));
    }
}
