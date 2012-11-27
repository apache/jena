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

import java.util.Map ;

/** Map specific functions */
public class MapUtils
{
    private MapUtils() {}
    
    public static <K, V> void apply(Map<K, V> map, ActionKeyValue<K, V> action)
    {
        for ( Map.Entry<K,V> entry : map.entrySet() )
            action.apply(entry.getKey(), entry.getValue()) ;
    }
    
    public static <T> void increment(Map<T, Integer> countMap, T key)
    { increment(countMap, key, 1) ; }
    
    public static <T> void increment(Map<T, Integer> countMap, T key, int incr)
    {
        Integer integer = countMap.get(key) ;
        if ( integer == null ) 
            countMap.put(key, incr) ;
        else
            countMap.put(key, integer+incr) ;
    }
}
