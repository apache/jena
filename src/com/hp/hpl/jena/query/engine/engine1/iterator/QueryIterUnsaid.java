/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.engine1.iterator;


import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.engine.engine1.PlanElement;
import com.hp.hpl.jena.query.engine.iterator.QueryIterFilter;
import com.hp.hpl.jena.query.engine.iterator.QueryIterSingleton;

/** 
 *  Filter a stream of bindings by a number of constraints.  
 * 
 * @author Andy Seaborne
 * @version $Id: QueryIterUnsaid.java,v 1.7 2007/02/06 17:06:03 andy_seaborne Exp $
 */

public class QueryIterUnsaid extends QueryIterFilter
{
    PlanElement cElt ;
    
    public QueryIterUnsaid(QueryIterator input, PlanElement cElt, ExecutionContext context)
    {
        super(input, context) ;
        this.cElt = cElt ;
    }
    
    public boolean accept(Binding b)
    {
        // Happens at execution time.
        
        QueryIterator input = null ;
        if ( b != null )
            input = new QueryIterSingleton(b, getExecContext()) ;

        QueryIterator cIter = cElt.build(input, getExecContext()) ;
        boolean r = cIter.hasNext() ;
        // special close()
        cIter.close() ;
        return !r;
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
