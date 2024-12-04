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

package org.apache.jena.sparql.util;

import org.junit.jupiter.api.Test;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

/**
 * Test query string parsing, conversion to the algebra
 */
public class TestQueryCheckRW {

    @Test public void rtt_abs() {
        // ABS is printed 'abs'
        test("SELECT * { BIND(ABS(?X) as ?absX) }");
    }

    @Test public void rtt_hashFunction_md5() {
        // MD5 is printed 'MD5' and is 'md5' in algebra.
        test("SELECT * { FILTER ( MD5('abc') != 0 ) }");
    }

    @Test public void rtt_hashFunction_sha1() {
        test("SELECT * { FILTER ( SHA1('abc') != 0 ) }");
    }

    @Test public void rtt_hashFunction_sha256() {
        test("SELECT * { FILTER ( SHA256('abc') != 0 ) }");
    }

    @Test public void rtt_hashFunction_sha384() {
        test("SELECT * { FILTER ( SHA384('abc') != 0 ) }");
    }

    @Test public void rtt_hashFunction_sha512() {
        test("SELECT * { FILTER ( SHA512('abc') != 0 ) }");
    }

    private static void test(String queryString) {
        Query query = QueryFactory.create(queryString);
        QueryCheck.checkParse(query);
        QueryCheck.checkOp(query, false);
        QueryCheck.checkOp(query, true);
    }
}
