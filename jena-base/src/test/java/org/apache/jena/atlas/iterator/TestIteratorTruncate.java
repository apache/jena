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

package org.apache.jena.atlas.iterator;

import static org.junit.jupiter.api.Assertions.assertEquals ;

import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.junit.jupiter.api.Test ;

public class TestIteratorTruncate {
    @Test public void iter_truncate_00() { 
        Iterator<String> iter = new IteratorTruncate<>(Iter.nullIterator(), (item)->true) ;
        long n = Iter.count(iter) ;
        assertEquals(0, n) ;
    }

    @Test public void iter_truncate_01() { 
        List<String> data = Arrays.asList("a", "b", "c") ;
        Iterator<String> iter = new IteratorTruncate<>(data.iterator(), (item)->true) ;
        long n = Iter.count(iter) ;
        assertEquals(3, n) ;
    }
    
    @Test public void iter_truncate_02() { 
        List<String> data = Arrays.asList("a", "b", "c") ;
        Iterator<String> iter = new IteratorTruncate<>(data.iterator(), (item)-> item.equals("a")) ;
        long n = Iter.count(iter) ;
        assertEquals(1, n) ;
    }

    @Test public void iter_truncate_03() { 
        List<String> data = Arrays.asList("a", "b", "c") ;
        Iterator<String> iter = new IteratorTruncate<>(data.iterator(), (item)-> !item.equals("c")) ;
        long n = Iter.count(iter) ;
        assertEquals(2, n) ;
    }

    @Test public void iter_truncate_04() { 
        List<String> data = Arrays.asList("a", "b", "c") ;
        Iterator<String> iter = new IteratorTruncate<>(data.iterator(), (item)-> item.equals("koala")) ;
        long n = Iter.count(iter) ;
        assertEquals(0, n) ;
    }
    
    @Test public void iter_truncate_05() { 
        List<String> data = Arrays.asList("a", "b", "b", "c" , "c", "d") ;
        Iterator<String> iter = new IteratorTruncate<>(data.iterator(), (item)-> !item.equals("c")) ;
        long n = Iter.count(iter) ;
        assertEquals(3, n) ;
    }
}
