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

package com.hp.hpl.jena.sparql.util;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.logging.Log ;


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
    
    /** Return a list of lists of all the elements of collection in every order
     *  Easy to run out of heap memory.
     */  
    static public <T> List<List<T>> permute(List<T> c)
    {
        if ( c.size() > 5 )
        {
            Log.warn(CollectionUtils.class, "Attempt to permute more than 5 items - think again") ;
            return null ;
        }
        
        List<List<T>> x = new ArrayList<List<T>>() ;
        if ( c.size() == 1 )
        {
            x.add(c) ;
            return x ;
        }

        for ( T obj : c )
        {
            List<T> c2 = new ArrayList<T>(c) ;
            c2.remove(obj) ;
            List<List<T>> x2 = permute(c2) ;
            // For each list returned
            for ( List<T> x3 : x2 )
            {
                // Gives a more expected ordering
                x3.add(0,obj) ;
                x.add(x3) ;
            }
        }
        return x ;
    }
}
