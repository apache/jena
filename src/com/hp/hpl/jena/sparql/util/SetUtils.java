/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.util.*;

/** Non-destructive versions of set operations.  May not be efficient on large sets */

public class SetUtils
{
    static public Set intersection(Set s1, Set s2)
    {
        Set s3 = new HashSet(s1) ;
        s3.retainAll(s2) ;
        return s3 ;
    }
    
    static public boolean intersectionP(Set s1, Set s2)
    {
        Set x = null ;
        Set y = null ;
        
        if ( s1.size() < s2.size() )
            return _intersectionP(s1, s2) ;
        else
            return _intersectionP(s2, s1) ;
    }
    
    static private boolean _intersectionP(Set s1, Set s2)
    {
        for( Iterator iter = s1.iterator() ; iter.hasNext() ; )
        {
            Object elt = iter.next() ;
            if ( s2.contains(elt) ) 
                return true ;
        }
        return false ;
    }
    
    
    static public Set union(Set s1, Set s2)
    {
        Set s3 = new HashSet(s1) ;
        s3.addAll(s2) ;
        return s3 ;
    }

    
    /** Return is s1 \ s2 */
    
    static public Set difference(Set s1, Set s2)
    {
        Set s3 = new HashSet(s1) ;
        s3.removeAll(s2) ;
        return s3 ;
    }

}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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