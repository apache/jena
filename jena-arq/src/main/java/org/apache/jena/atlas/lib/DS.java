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


import java.util.* ;

import org.apache.jena.atlas.iterator.NullIterator ;

public class DS
{
    private DS() {}
    
    public static <X> Set<X> setOfNone() { return Collections.emptySet()  ; }
    public static <X> Set<X> setOfOne(X element) { return Collections.singleton(element) ; }
    public static <X> Set<X> set() { return new HashSet<X>(); }  
    public static <X> Set<X> set(int initialSize) { return new HashSet<X>(initialSize); }  
    public static <X> Set<X> set(Set<X> other) { return new HashSet<X>(other); }  

    public static <K, V> Map<K,V> mapOfNone() { return Collections.emptyMap() ; }
    public static <K, V> Map<K,V> mapOfOne(K key, V value) { return Collections.singletonMap(key, value) ; }
    public static <K, V> Map<K,V> map() { return new HashMap<K,V>(); }  
    public static <K, V> Map<K,V> map(int initialSize) { return new HashMap<K,V>(initialSize); }  
    public static <K, V> Map<K,V> map(Map<K,V> other) { return new HashMap<K,V>(other); }  

    @SuppressWarnings("rawtypes")
    private static final Iterator iter0 = new NullIterator() ;
    
    @SuppressWarnings({"unchecked", "cast"})
    public static <T> Iterator<T> nothing() { return (NullIterator<T>)iter0 ; }
    
    public static <T> List<T> listOfNone() { return Collections.emptyList() ; }
    public static <T> List<T> listOfOne(T element) { return Collections.singletonList(element) ; }
    public static <T> List<T> list() { return new ArrayList<T>(); }  
    public static <T> List<T> list(int initialSize) { return new ArrayList<T>(initialSize); }  
    public static <T> List<T> list(List<T> other) { return new ArrayList<T>(other); }
}
