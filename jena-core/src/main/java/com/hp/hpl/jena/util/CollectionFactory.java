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

package com.hp.hpl.jena.util;

import java.util.*;

/**
    CollectionFactory - a central place for allocating sets and maps, mostly so that
    it's easy to plug in new implementations (eg trove).
*/
public class CollectionFactory 
    {
    /**
         Answer a new Map which uses hashing for lookup.
    */
    public static <K,V> Map<K,V> createHashedMap() 
        { return new HashMap<>(); }
    
    /**
         Answer a new Map which uses hashing for lookup and has initial size
         <code>size</code>.
    */
    public static <K,V> Map<K,V> createHashedMap( int size ) 
        { return new HashMap<>( size ); }
    
    /**
         Answer a new Map which uses hashing for lookup and is initialised to be
         a copy of <code>toCopy</code>.
    */
    public static <K,V> Map<K,V> createHashedMap( Map<K,V> toCopy ) 
        { return new HashMap<>( toCopy ); }
    
    /**
         Answer a new Set which uses haashing for lookup.
    */
    public static <T> Set<T> createHashedSet() 
        { return new HashSet<>(); }
    
    /**
         Answer a new Set which uses hashing for lookup and is initialised as a copy
         of <code>toCopy</code>.
    */
    public static <T> Set<T> createHashedSet( Collection<T> toCopy ) 
        { return new HashSet<>( toCopy ); }
    }
