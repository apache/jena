/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.main.iterators;

import java.util.*;

import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.engine.binding.BindingComparator;
import com.hp.hpl.jena.query.engine.iterator.QueryIterPlainWrapper;

/** Sort a query iterator.  Uses an in-memory sort, so limiting the size of
 * iterators that can be handled.
 * 
 * @author Andy Seaborne
 * @version $Id: QueryIterSort.java,v 1.5 2007/01/02 11:19:31 andy_seaborne Exp $
 */

public class QueryIterSort
    extends QueryIterPlainWrapper
{
    boolean finished = false ;
    QueryIterator qIterSorted ;
    
    public QueryIterSort(QueryIterator qIter, List conditions, ExecutionContext context)
    {
        this(qIter, new BindingComparator(conditions), context) ;
    }

    public QueryIterSort(QueryIterator qIter, Comparator comparator, ExecutionContext context)
    {
        super(sort(qIter, comparator), context) ;
    }
    
    private static Iterator sort(QueryIterator qIter, Comparator comparator)
    {
        // Be careful about duplicates.
        // Used to use a TreeSet but, well, that's a set.
        List x = new ArrayList() ;
        for ( ; qIter.hasNext() ; )
        {
            Binding b = (Binding)qIter.next() ;
            x.add(b) ;
        }
        Binding[] y = (Binding[])x.toArray(new Binding[]{}) ;
        x = null ;      // Drop the List now - might be big.  Unlikely to really make a real difference.  But we can try.
        Arrays.sort(y, comparator) ;
        x = Arrays.asList(y) ;
        return x.iterator() ;
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