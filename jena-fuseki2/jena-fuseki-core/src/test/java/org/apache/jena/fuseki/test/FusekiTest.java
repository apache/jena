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

package org.apache.jena.fuseki.test;

import java.util.Objects;

import org.junit.Assert;

public class FusekiTest {

    /** Check whether str is a comma separated list of expected (unordered) */
    public static void assertStringList(String str, String... expected) {
        str = str.replace(" ", "");
        String[] x = str.split(",");
        for ( String ex : expected ) {
            Assert.assertTrue("Got: "+str+" - Does not contain "+ex, containsStr(ex, x));
        }
        for ( String s : x ) {
            Assert.assertTrue("Got: "+str+" - Not expected "+s, containsStr(s, expected));
        }
    }

    /** Is v in the list of strings? */
    public static boolean containsStr(String v, String[] strings) {
        for ( String s: strings ) {
            if ( Objects.equals(v, s))
                return true;
        }
        return false;
    }
}

