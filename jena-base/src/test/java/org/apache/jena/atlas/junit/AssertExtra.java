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

package org.apache.jena.atlas.junit ;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List ;
import java.util.function.Supplier;

import org.apache.jena.atlas.lib.ListUtils ;

public class AssertExtra {

    public static <T> void assertEqualsUnordered(List<T> list1, List<T> list2) {
        assertEqualsUnordered(list1, list2, (Supplier<String>)null);
    }

    public static <T> void assertEqualsUnordered(List<T> list1, List<T> list2, String msg) {
        assertEqualsUnordered(list1, list2, ()->msg);
    }

    public static <T> void assertEqualsUnordered(List<T> list1, List<T> list2, Supplier<String> msg) {
        String x = (msg == null) ? null : msg.get();
        if ( ! ListUtils.equalsUnordered(list1, list2) )
            fail(msg(x, list1, list2)) ;
    }

    private static <T> String msg(String msg, List<T> list1, List<T> list2) {
        String x = ( msg == null ) ? "" : msg+": " ;
        x = x +"Expected: " + list1 + " : Actual: " + list2 ;
        return x ;
    }
}
