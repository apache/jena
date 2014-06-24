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


import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Collections ;
import java.util.Comparator ;
import java.util.List ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.ReverseComparator ;
import org.junit.Test ;

public class TestReverseComparator extends BaseTest
{
    static Comparator<String> normal = new Comparator<String>()
    {
        @Override
        public int compare(String o1, String o2)
        {
            return o1.compareTo(o2);
        }
    };
    
    static Comparator<String> reverse = new ReverseComparator<>(normal);
    
    
    static Comparator<String> maxMin = new Comparator<String>()
    {
        @Override
        public int compare(String o1, String o2)
        {
            int value = o1.compareTo(o2);
            if (value > 0)
                return Integer.MAX_VALUE;
            else if (value < 0)
                return Integer.MIN_VALUE;
            else
                return 0;
        }
    };
    
    static Comparator<String> reverseMaxMin = new ReverseComparator<>(maxMin);
    
    static List<String> items = Arrays.asList("a", "b", "c", "d");
    static List<String> itemsReverse = Arrays.asList("d", "c", "b", "a");
    
    @Test public void reverse_01()
    {
        List<String> modified = new ArrayList<>(items);
        Collections.sort(modified, reverse);
        
        test(itemsReverse, modified);
    }
    
    @Test public void reverse_02()
    {
        List<String> modified = new ArrayList<>(items);
        Collections.sort(modified, reverseMaxMin);
        
        test(itemsReverse, modified);
    }
    
    private void test(List<?> expected, List<?> actual)
    {
        assertEquals(expected.size(), actual.size());
        
        for (int i=0; i<expected.size(); i++)
        {
            assertEquals(expected.get(i), actual.get(i)) ;
        }
    }
}
