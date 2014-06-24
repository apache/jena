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
        for( T elt : s1 )
        {
            if ( s2.contains(elt) ) 
                return true ;
        }
        return false ;
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
    
    /** Return true if s1 and s2 are disjoint */
    public static <T> boolean isDisjoint(Set<? extends T> s1, Set<? extends T> s2)
    {
        Set<? extends T> x = s1 ;
        Set<? extends T> y = s2 ;
        if ( s1.size() < s2.size() )
        {
            x = s2 ;
            y = s1 ;
        }        
        
        for ( T item : x )
        {
            if ( y.contains(item)) 
                return false ;
        }
        return true ;
    }
}

