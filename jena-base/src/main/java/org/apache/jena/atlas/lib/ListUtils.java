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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList ;
import java.util.List ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.logging.Log ;

/** Various things for lists */
public class ListUtils
{
    private ListUtils() {}

    public static <T>
    List<T> unique(List<T> list)
    {
    		return list.stream().distinct().collect(toList());
    }
    
    public static
    List<Integer> asList(int... values)
    {
        List<Integer> x = new ArrayList<>(values.length) ;
        for ( int v : values )
            x.add(v) ;
        return x ;
    }
    
    public static <T> String str(T[] array)
    {
        return stream(array).map(String::valueOf).collect(joining(", ", "[", "]"));
    }
    
    public static String str(int[] array)
    {
    		return stream(array).mapToObj(String::valueOf).collect(joining(", ", "[", "]"));
    }
    
    public static String str(long[] array)
    {
    		return stream(array).mapToObj(String::valueOf).collect(joining(", ", "[", "]"));
    }

    public static <T> void print(IndentedWriter out, List<T> list)
    { 
        print(out, list, " ") ;
    }
    
    public static <T> void print(final IndentedWriter out, List<T> list, final CharSequence sep)
    {
		out.print(list.stream().map(String::valueOf).collect(joining(sep)));
    }
    
    /** Return a list of lists of all the elements of collection in every order
     *  Easy to run out of heap memory.
     *  
     *  See also {@code org.apache.jena.ext.com.google.common.collect.Collections2#permutations}
     */  
    static public <T> List<List<T>> permute(List<T> c)
    {
        if ( c.size() > 5 )
        {
            Log.warn(ListUtils.class, "Attempt to permute more than 5 items - think again") ;
            return null ;
        }
        
        List<List<T>> x = new ArrayList<>() ;
        if ( c.size() == 1 )
        {
            x.add(c) ;
            return x ;
        }

        for ( T obj : c )
        {
            List<T> c2 = new ArrayList<>(c) ;
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
