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

package org.apache.jena.atlas.lib;

import static org.apache.jena.atlas.lib.CollectionUtils.oneElt;
import static org.apache.jena.atlas.lib.CollectionUtils.sameElts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Set ;

import org.junit.Test ;

public class TestCollectionUtils {
    @Test
    public void sameElts_01() {
        List<String> x1 = Arrays.asList("a", "b", "c") ;
        List<String> x2 = Arrays.asList("a", "c", "b") ;
        assertTrue(sameElts(x1, x2)) ;
    }

    @Test
    public void sameElts_02() {
        List<String> x1 = Arrays.asList("a", "b", "c", "a") ;
        List<String> x2 = Arrays.asList("a", "c", "b") ;
        assertTrue(sameElts(x1, x2)) ;
    }

    @Test
    public void sameElts_03() {
        List<String> x1 = Arrays.asList("a", "b", "c") ;
        List<String> x2 = Arrays.asList("a", "c", "b") ;
        Set<String>  z1 = Set.copyOf(x2) ;
        assertTrue(sameElts(x1, z1)) ;
    }

    @Test
    public void sameElts_04() {
        List<String> x1 = Arrays.asList("a", "b", "X") ;
        List<String> x2 = Arrays.asList("a", "c", "b") ;
        assertFalse(sameElts(x1, x2)) ;
    }

    @Test
    public void sameElts_05() {
        List<String> x1 = Arrays.asList("a", "b", "c") ;
        List<String> x2 = Arrays.asList("a", "b") ;
        assertFalse(sameElts(x1, x2)) ;
    }

    @Test
    public void sameElts_06() {
        List<String> x1 = Arrays.asList("a", "b", "X") ;
        List<String> x2 = Arrays.asList("a", "c", "b") ;
        Set<String>  z1 = new HashSet<>(x2) ;
        assertFalse(sameElts(x1, z1)) ;
    }

    @Test
    public void oneElt_01() {
        List<String> x = Arrays.asList("a", "b", "c") ;
        assertEquals("a", oneElt(x));
    }

    @Test
    public void oneElt_02() {
        List<String> x = Arrays.asList() ;
        assertNull(oneElt(x));
    }
}
