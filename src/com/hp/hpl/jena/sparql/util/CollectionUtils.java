/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.util.* ;


/** 
 * @author Andy Seaborne
 */

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
            ALog.warn(CollectionUtils.class, "Attempt to permute more than 5 items - think again") ;
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

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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