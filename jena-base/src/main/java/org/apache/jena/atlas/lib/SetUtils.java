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

public class SetUtils
{
    private SetUtils() {}
    
    public static <X> Set<X> setOfOne(X element) { return DS.setOfOne(element) ; }
    
     // Set specific operations
    
    public static <T> Set<T> intersection(Set<? extends T> setLeft, Set<? extends T> setRight)
    {
        Set<T> results = new HashSet<>(setLeft) ;
        results.retainAll(setRight) ;
        return results ;
    }

    public static <T> boolean intersectionP(Set<? extends T> s1, Set<? extends T> s2)
    {
    		return s1.stream().anyMatch(s2::contains);
    }

    public static <T> Set<T> union(Set<? extends T> s1, Set<? extends T> s2)
    {
        Set<T> s3 = new HashSet<>(s1) ;
        s3.addAll(s2) ;
        return s3 ;
    }

    /** Return is s1 \ s2 */

    public static <T> Set<T> difference(Set<? extends T> s1, Set<? extends T> s2)
    {
        Set<T> s3 = new HashSet<>(s1) ;
        s3.removeAll(s2) ;
        return s3 ;
    }
}

