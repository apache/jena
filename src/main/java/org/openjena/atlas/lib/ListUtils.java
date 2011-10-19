/**
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

package org.openjena.atlas.lib;

import java.util.ArrayList ;
import java.util.List ;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.iterator.Action ;
import org.openjena.atlas.iterator.FilterUnique ;
import org.openjena.atlas.iterator.Iter ;

/*8 Various things for lists */
public class ListUtils
{
    private ListUtils() {}

    public static <T>
    List<T> unique(List<T> list)
    {
        Iter<T> iter = Iter.iter(list.iterator()) ;
        return iter.filter(new FilterUnique<T>()).toList() ;
    }
    
    public static
    List<Integer> asList(int... values)
    {
        List<Integer> x = new ArrayList<Integer>() ;
        for ( int v : values )
            x.add(v) ;
        return x ;
    }
    
    public static <T> String str(T[] array)
    {
        StringBuilder buff = new StringBuilder() ;
        String sep = "[" ;

        for ( int i = 0 ; i < array.length ; i++ )
        {
            buff.append(sep) ;
            sep = ", " ; 
            buff.append(array[i]) ;
        }
        buff.append("]") ;
        return buff.toString() ;
    }
    
    public static String str(int[] array)
    {
        StringBuilder buff = new StringBuilder() ;
        String sep = "[" ;

        for ( int i = 0 ; i < array.length ; i++ )
        {
            buff.append(sep) ;
            sep = ", " ; 
            buff.append(array[i]) ;
        }
        buff.append("]") ;
        return buff.toString() ;
    }
    
    public static String str(long[] array)
    {
        StringBuilder buff = new StringBuilder() ;
        String sep = "[" ;

        for ( int i = 0 ; i < array.length ; i++ )
        {
            buff.append(sep) ;
            sep = ", " ; 
            buff.append(array[i]) ;
        }
        buff.append("]") ;
        return buff.toString() ;
    }

    public static <T> void print(IndentedWriter out, List<T> list)
    { 
        print(out, list, " ") ;
    }
    
    public static <T> void print(final IndentedWriter out, List<T> list, final String sep)
    {
        Action<T> output = new Action<T>() {
            boolean first = true ;
            @Override
            public void apply(T item)
            {
                if ( ! first ) out.print(sep) ;
                out.print(item) ;
                first = false ;
            }
        } ;
        Iter.apply(list, output) ;
    }
}
