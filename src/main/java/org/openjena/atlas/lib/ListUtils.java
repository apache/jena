/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;


import java.util.ArrayList;
import java.util.List;

import org.openjena.atlas.iterator.FilterUnique ;
import org.openjena.atlas.iterator.Iter ;


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

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */