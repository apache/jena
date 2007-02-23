/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.util.* ;


import org.apache.commons.logging.LogFactory;

/** com.hp.hpl.jena.query.util.ListUtils
 * 
 * @author Andy Seaborne
 * @version $Id: CollectionUtils.java,v 1.5 2007/01/02 11:20:06 andy_seaborne Exp $
 */

public class CollectionUtils
{
    static public void removeNulls(Collection list)
    {
        for ( Iterator iter = list.iterator() ; iter.hasNext() ; )
        {
            Object e = iter.next() ;
            if ( e == null )
                iter.remove() ;
        }
    }
    
    /** Return a list of lists of all the elements of collection in every order
     *  Easy to run out of heap memory.
     */  
    static public List permute(Collection c)
    {
        if ( c.size() > 5 )
        {
            LogFactory.getLog(SetUtils.class).warn("Attempt to permute more than 5 items - think again") ;
            return null ;
        }
        
        List x = new ArrayList() ;
        if ( c.size() == 1 )
        {
            x.add(c) ;
            return x ;
        }

        for ( Iterator iter = c.iterator() ; iter.hasNext() ; )
        {
            Object obj = iter.next() ;

            List c2 = new ArrayList(c) ;
            c2.remove(obj) ;
            List x2 = permute(c2) ;
            // For each list returned
            for ( Iterator iter2 = x2.iterator() ; iter2.hasNext() ; )
            {
                List x3 = (List)iter2.next() ;
                // Gives a more expected ordering
                x3.add(0,obj) ;
                x.add(x3) ;
            }
        }
        return x ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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