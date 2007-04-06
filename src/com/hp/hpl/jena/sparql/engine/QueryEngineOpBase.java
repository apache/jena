/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;

import com.hp.hpl.jena.sparql.algebra.AlgebraGenerator;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpSubstitute;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils;
import com.hp.hpl.jena.sparql.util.Context;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;

// This is the main adapter from the Query/syntax level to the algebra level. 

public class QueryEngineOpBase extends QueryEngineBase
{
    private Op queryOp = null ;
    private AlgebraGenerator gen = null ;
    private OpExec executor = null ; 

    public QueryEngineOpBase(Query q, 
                             AlgebraGenerator gen, 
                             Context context,
                             OpExec executor) 
    { 
        super(q, context) ;
        this.gen = gen ;
        this.executor = executor ;
    }  
    // --------------------------------
    
    final
    protected Plan queryToPlan(Query query, QuerySolution startSolution)
    {
        Op op = getOp() ;
        if ( startSolution == null )
        {
            QueryIterator qIter = executor.eval(op, getDatasetGraph(), getContext()) ;
            return new PlanOp(op, qIter) ;
        }
        
        //if ( executor instanceof OpExecInitial )
        
        Binding b = BindingRoot.create() ;
          
          // If there is some initial bindings
          if ( startSolution != null )
          {
              b = new BindingMap(b) ;
              BindingUtils.addToBinding(b, startSolution) ;
              // Substitute in the Op, and use this b as the root. 
              op = OpSubstitute.substitute(op, b) ;
          }
        QueryIterator qIter = executor.eval(op, b, getDatasetGraph(), getContext()) ;
        // qIter add parent.  Tweak root?
        return new PlanOp(op, qIter) ;
    }
    
    public Op getOp()
    {
        if ( queryOp == null )
            queryOp = createOp() ; 
        return queryOp ;
    }

    protected Op createOp()
    {
        Op op = createPatternOp() ;
        op = modifyPatternOp(op) ;
        op = gen.compileModifiers(getQuery(), op) ;
        op = modifyQueryOp(op) ;
        return op ;
    }
    
    protected Op createPatternOp()
    {
        return gen.compile(query.getQueryPattern()) ;
    }
    
    /** Allow the algebra expression to be modifed */
    protected Op modifyQueryOp(Op op)
    {
        return op ;
    }

    /** Allow the algebra expression to be modifed */
    protected Op modifyPatternOp(Op op)
    {
        return op ;
    }
}


class QueryEngineOpFactory
{
    
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