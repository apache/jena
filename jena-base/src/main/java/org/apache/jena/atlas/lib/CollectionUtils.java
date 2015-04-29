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

import java.util.Collection ;
import java.util.Iterator ;

public class CollectionUtils
{
    static public <T> void removeNulls(Collection<T> list)
    {
        for ( Iterator<T> iter = list.iterator() ; iter.hasNext() ; )
        {
            T e = iter.next() ;
            if ( e == null )
                iter.remove() ;
        }
    }
    
    static public <T> boolean disjoint(Collection<T> c1, Collection<T> c2)
    {
        if ( c2.size() < c1.size() )
        {
            Collection<T> t = c1 ;
            c1 = c2 ;
            c2 = t ;
        }
        
        for ( T t : c1 )
        {
            if ( c2.contains(t) )
                return false ;
        }
        return true ;
    }
}
