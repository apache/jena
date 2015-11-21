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

import java.util.ArrayList ;
import java.util.List ;
import java.util.Locale ;

import org.junit.Assert ;

public class BaseTest extends Assert {
    public static void assertEqualsIgnoreCase(String a, String b) {
        a = a.toLowerCase(Locale.ROOT) ;
        b = b.toLowerCase(Locale.ROOT) ;
        assertEquals(a, b) ;
    }

    public static void assertEqualsIgnoreCase(String msg, String a, String b) {
        a = a.toLowerCase(Locale.ROOT) ;
        b = b.toLowerCase(Locale.ROOT) ;
        assertEquals(msg, a, b) ;
    }

    public static <T> void assertEqualsUnordered(List<T> list1, List<T> list2) {
        assertEqualsUnordered(null, list1, list2) ;
    }
    
    public static <T> void assertEqualsUnordered(String msg, List<T> list1, List<T> list2) {
        if ( list1.size() != list2.size() )
            fail(msg(msg, list1, list2)) ;
        List<T> list2a = new ArrayList<>(list2) ;
        for ( T elt : list1 )
            list2a.remove(elt) ;
        if ( list2a.size() != 0 )
            fail(msg(msg, list1, list2)) ;
    }
    
    private static <T> String msg(String msg, List<T> list1, List<T> list2) {
        String x = ( msg == null ) ? "" : msg+": " ;
        x = x +"Expected: " + list1 + " : Actual: " + list2 ;
        return x ;
    }

}
