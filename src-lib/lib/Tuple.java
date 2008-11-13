/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package lib;

import iterator.Iter;

import java.util.Arrays;
import java.util.List;

public final class Tuple<T>
{
    public static <T> Tuple<T> blankTuple(int len)
    {
        @SuppressWarnings("unchecked")
        T[] tuple = (T[])new Object[len] ;
        return new Tuple<T>(tuple) ;
    }
    
    final T[] tuple ;
    
    public Tuple(T...tuple)
    {
        this.tuple = tuple ;
    }
    
    public T get(int idx) { return tuple[idx] ; }
    //public void set(int idx, T elt) { tuple[idx] = elt ; }

    public List<T> asList() { return Arrays.asList(tuple) ; }
    
    public T[] tuple() { return tuple ; }

    /** Return a tuple with the column mapping applied */
    public Tuple<T> map(ColumnMap colMap)
    {
        return colMap.map(this) ;
    }
    
    /** Return a tuple with the column mapping reversed */
    public Tuple<T> unmap(ColumnMap colMap)
    {
        return colMap.unmap(this) ;
    }
    
    public int size() { return tuple.length ; }
    
    @Override
    public int hashCode()
    { 
        int x = 99 ;
        for ( T n : tuple )
            x ^= n.hashCode() ;
        return x ;  
    }
    
    @Override
    public boolean equals(Object other) 
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof Tuple ) )
            return false ;
        Tuple<?> x = (Tuple<?>)other ;
        if ( x.size() != this.size() )
            return false ;
        for ( int i = 0 ; i < tuple.length ; i++ )
        {
            Object obj1 = tuple[i] ;
            Object obj2 = x.tuple[i] ;
            if ( ! Lib.equals(obj1, obj2) )
                return false ;
        }
        return true ; 
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder() ;
        return "["+Iter.asString(asList(), ", ")+"]" ;
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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