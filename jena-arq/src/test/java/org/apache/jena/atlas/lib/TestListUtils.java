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

import static org.apache.jena.atlas.lib.ListUtils.unique ;

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.List ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

public class TestListUtils extends BaseTest
{
    @Test public void list01() 
    {
        List<Integer> x = Arrays.asList(1,2,3) ;
        x = unique(x) ;
        assertEquals(3, x.size()) ;
        test(x, 1,2,3) ;
    }

    @Test public void list02() 
    {
        List<Integer> x = Arrays.asList(1,2,3,1,3,2) ;
        x = unique(x) ;
        assertEquals(3, x.size()) ;
        test(x, 1,2,3) ;
    }

    @Test public void list03() 
    {
        List<Integer> x = new ArrayList<>() ;
        x = unique(x) ;
        assertEquals(0, x.size()) ;
        test(x) ;
    }

    @Test public void list04() 
    {
        List<Integer> x = Arrays.asList(99) ;
        x = unique(x) ;
        assertEquals(1, x.size()) ;
        test(x, 99) ;
    }

    @Test public void list05() 
    {
        List<Integer> x = Arrays.asList(1,1,2,3,1,1,3) ;
        x = unique(x) ;
        assertEquals(3, x.size()) ;
        test(x, 1,2,3) ;
    }

    private void test(List<Integer> x, int... args)
    {
        assertEquals(args.length, x.size()) ;
        
        for ( int i = 0; i < args.length ; i++ )
            assertEquals(args[i], x.get(i).intValue()) ;
    }
}
