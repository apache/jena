/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.*;

import com.hp.hpl.jena.sparql.ARQNotImplemented;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingProject;

public class QueryIterGroup extends QueryIterPlainWrapper
{
    // Aggregators as subpackage of iterators?
    
    public QueryIterGroup(QueryIterator qIter, 
                          List groupVars,
                          List aggregators,
                          ExecutionContext execCxt)
    {
        super(calc(qIter, groupVars, aggregators),
              execCxt) ;
        throw new ARQNotImplemented("QueryIterGroup") ;
    }

    protected void closeIterator()
    {
    }

    protected boolean hasNextBinding()
    {
        return false ;
    }

    protected Binding moveToNextBinding()
    {
        return null ;
    }
    
    private static QueryIterator calc(QueryIterator iter, List groupVars, List aggregators)
    {
        if ( true )
            throw new ARQNotImplemented("QueryIterGroup.calc: magic happens") ;
        Map buckets = new HashMap() ;    // Key ==> Binding being built.
        Map aggregations = null ;       // Key ==> 
        
        for ( ; iter.hasNext() ; )
        {
            Binding b = iter.nextBinding() ;
            Binding key = new BindingProject(groupVars, b) ;
            
            for ( Iterator aggIter = aggregators.iterator() ; aggIter.hasNext() ; )
            {
                Aggregator agg = (Aggregator)aggIter.next();
                agg.accumulate(key, b) ;
            }
            Binding out = new BindingMap() ;
            for ( Iterator gvIter = groupVars.iterator() ; gvIter.hasNext() ; )
            {
                Var v = (Var)gvIter.next();
                // magic happens
                out.add(v, null) ;
            }
        }
        
        
        return null ;
    }

    private Binding group(Map groups, Binding key)
    {
        Binding x = (Binding)groups.get(key) ;
        if ( x == null )
        {
            // Better o copy here to free the key (which is a wrapper)
            x = new BindingMap() ;
            x.addAll(key) ;
        }
        return x ;
    }

}



/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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


/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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