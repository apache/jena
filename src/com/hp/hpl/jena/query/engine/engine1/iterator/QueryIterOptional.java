/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.engine1.iterator;

import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.engine.engine1.PlanElement;
import com.hp.hpl.jena.query.engine.iterator.QueryIterDefaulting;
import com.hp.hpl.jena.query.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.query.serializer.SerializationContext;
import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.query.util.Utils;


public class QueryIterOptional extends QueryIterPlanRepeatApply
{
    PlanElement cElt ;
    
    public QueryIterOptional(QueryIterator input, PlanElement cElt,
                             ExecutionContext context)
    {
        super(input, context) ;
        this.cElt = cElt ;
    }

    protected QueryIterator nextStage(Binding binding)
    {
        QueryIterator cIter = cElt.build(new QueryIterSingleton(binding, getExecContext()), getExecContext()) ;

//        if ( getExecContext().getContext().isFalse(EngineConfig.niceOptionals) )
//            // Need to yield the input binding first regard less.
//            // return new QueryIterFirstElement(new FirstBinding(binding), cIter) ;
//            throw new ARQInternalErrorException("Not implemented: NotNiceOptionals") ;

        cIter = new QueryIterDefaulting(cIter, binding, getExecContext()) ;
        return cIter ; 
    }
    
    protected void details(IndentedWriter out, SerializationContext sCxt)
    {
        out.println(Utils.className(this)) ;
        out.incIndent() ;
        cElt.output(out, sCxt) ;
        out.decIndent() ;
        
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