/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.util.alg;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.sdb.util.Alg;

public class SetUtils extends Alg
{
    // TODO Tidy up to use streams

    // Set specific operations and functions returning something
    // that need to know the concrete type of that thing.
    // Hence not in general AlgUtils
    
    public static <T> Set<T> intersection(Set<? extends T> setLeft, Set<? extends T> setRight)
    {
        Set<T> results = new HashSet<T>(setLeft) ;
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
        Set<T> s3 = new HashSet<T>(s1) ;
        s3.addAll(s2) ;
        return s3 ;
    }


    /** Return is s1 \ s2 */

    public static <T> Set<T> difference(Set<? extends T> s1, Set<? extends T> s2)
    {
        Set<T> s3 = new HashSet<T>(s1) ;
        s3.removeAll(s2) ;
        return s3 ;
    }

    public static <T> Set<T> filter(Set<? extends T> s, Filter<T> f)
    { return Alg.toSet(Alg.filter(s, f)) ; }

    public static <T, R> Set<R> map(Set<? extends T> s, Transform<T, R> converter)
    {
        return Alg.toSet(Alg.map(s, converter)) ;
    }
}


/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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