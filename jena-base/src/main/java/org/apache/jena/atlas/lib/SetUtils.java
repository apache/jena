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

import java.util.HashSet ;
import java.util.Set ;
import java.util.concurrent.ConcurrentHashMap ;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Set specific operations */
public class SetUtils
{
    private SetUtils() {}
    
    /** "ConcurrentHashSet" */
    public static final <X> Set<X> concurrentHashSet() {
        return ConcurrentHashMap.newKeySet() ;
    }

    public static <T> Set<T> intersection(Set<? extends T> setLeft, Set<? extends T> setRight) {
        Set<T> results = new HashSet<>(setLeft) ;
        results.retainAll(setRight) ;
        return results ;
    }

    public static <T> boolean intersectionP(Set<? extends T> s1, Set<? extends T> s2) {
        return s1.stream().anyMatch(s2::contains) ;
    }

    /**
     * @param s1 a {@link Set}
     * @param s2 a {@link Set}
     * @return the set-theoretic union of {@code s1} and {@code s2}
     * 
     * @see org.apache.jena.ext.com.google.common.collect.Sets#union(Set, Set) for a view-based alternative
     */
    public static <T> Set<T> union(Set<? extends T> s1, Set<? extends T> s2) {
        Set<T> s3 = new HashSet<>(s1) ;
        s3.addAll(s2) ;
        return s3 ;
    }

    /**
     * @param s1 a {@link Set}
     * @param s2 a {@link Set}
     * @return {@code s1 ∪ s2  \ s1 ∩ s2} : those elements in {@code s1} or {@code s2} but not both
     * 
     * @see org.apache.jena.ext.com.google.common.collect.Sets#symmetricDifference(Set, Set) for a view-based alternative
     */
    public static <T> Set<T> symmetricDifference(Set<? extends T> s1, Set<? extends T> s2) {
        Set<T> s3 = new HashSet<>() ;
        s1.forEach(x -> {
            if ( !s2.contains(x) )
                s3.add(x) ;
        }) ;
        s2.forEach(x -> {
            if ( !s1.contains(x) )
                s3.add(x) ;
        }) ;
        return s3 ;
    }

    /**
     * @param s1 a {@link Set}
     * @param s2 a {@link Set}
     * @return {@code s1} \ {@code s2}
     * 
     * @see org.apache.jena.ext.com.google.common.collect.Sets#difference(Set, Set) for a view-based alternative
     */
    public static <T> Set<T> difference(Set<? extends T> s1, Set<? extends T> s2) {
        Set<T> s3 = new HashSet<>(s1) ;
        s3.removeAll(s2) ;
        return s3 ;
    }

    /** Stream to set */ 
    public static <T> Set<T> toSet(Stream<T> stream) {
        return stream.collect(Collectors.toSet());
    }
}

