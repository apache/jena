/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine;

import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.algebra.AlgebraGenerator;
import com.hp.hpl.jena.query.algebra.Op;
import com.hp.hpl.jena.query.algebra.OpVars;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.util.Context;

public abstract class QueryEngineOpBase extends QueryEngineBase
{
    private Op queryOp = null ;

    protected QueryEngineOpBase(Query q, Context context)
    { super(q, context) ; }

    protected QueryEngineOpBase(Op op, Context context)
    { 
        super(dummy(op), context) ;
        queryOp = op ;
    }
    
    static Query dummy(Op op)
    {
        Set x = OpVars.patternVars(op) ;
        Query query = new Query() ;
        query.setQuerySelectType() ;
        for ( Iterator iter = x.iterator() ; iter.hasNext() ; )
        {
            Var v = (Var)iter.next();
            query.addResultVar(v) ;
        }
        return query ;
    }
    
    final
    protected Plan queryToPlan(Query query)
    {
        Op op = getOp() ;
        op = modifyQueryOp(op) ;
        QueryIterator qIter = createQueryIterator(op) ;
        return new PlanOp(op, qIter) ;
    }

    /** Turn a SPARQL algebra expression into a QueryIterator */ 
    protected abstract QueryIterator createQueryIterator(Op op) ;
    
    protected Op createOp()
    { 
        Op op = createPatternOp() ;
        op = modifyPatternOp(op) ;
        op = AlgebraGenerator.compileModifiers(getQuery(), op) ;
        op = modifyQueryOp(op) ;
        return op ;
    }
    
    protected Op createPatternOp()
    {
        return AlgebraGenerator.compile(getQuery().getQueryPattern()) ;
    }
    
    public Op getOp()
    {
        if ( queryOp == null )
            queryOp = createOp() ; 
        return queryOp ;
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