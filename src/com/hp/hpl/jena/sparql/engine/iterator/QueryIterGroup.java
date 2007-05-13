/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.*;

import com.hp.hpl.jena.sparql.ARQNotImplemented;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingProject;

public class QueryIterGroup extends QueryIter
{
    private boolean hasYieledGroup = false ;
    
    public QueryIterGroup(QueryIterator qIter, 
                          List groupVars,
                          List aggregators,
                          ExecutionContext execCxt)
    {
        super(execCxt) ;
        calc(qIter, groupVars, aggregators) ;
        throw new ARQNotImplemented("QueryIterGroup") ;
        // Calculate
    }

//    public boolean hasNextGroup()
//    { return ! hasYieledGroup ; }
//
//    public QueryIterator nextGroup()
//    {
//        if ( ! hasNextGroup() )
//            throw new NoSuchElementException("QueryIterGroup.nextGroup") ;
//        hasYieledGroup = true ;
//        return null ;
//    }
    
    protected void closeIterator()
    {
//        if ( qIter != null )
//            qIter.close() ;
//        qIter = null ;
    }

    protected boolean hasNextBinding()
    {
        return false ;
    }

    protected Binding moveToNextBinding()
    {
        return null ;
    }
    
    private void calc(QueryIterator iter, List groupVars, List aggregators)
    {
        Map groups = new HashMap() ;    // Key ==> Binding being built.
        Map aggregations = null ;       // Key ==> 
        
        for ( ; iter.hasNext() ; )
        {
            Binding b = iter.nextBinding() ;
            
            // Stored Binding is (groupVars, aggregates, other [first]) 
            
            // Better to do a space saving copy?
            Binding key = new BindingProject(groupVars, b) ;
            
            for ( Iterator aggIter = aggregators.iterator() ; aggIter.hasNext() ; )
            {
                Aggregator agg = (Aggregator)aggIter.next();
                agg.accumulate(key, b) ;
            }
            
        }
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