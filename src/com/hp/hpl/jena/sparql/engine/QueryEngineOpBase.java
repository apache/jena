/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecException;

import com.hp.hpl.jena.sparql.algebra.AlgebraGenerator;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.core.DataSourceGraphImpl;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorCheck;
import com.hp.hpl.jena.sparql.engine.ref.Eval;
import com.hp.hpl.jena.sparql.engine.ref.Evaluator;
import com.hp.hpl.jena.sparql.engine.ref.EvaluatorFactory;
import com.hp.hpl.jena.sparql.util.Context;

import com.hp.hpl.jena.graph.Graph;

public class QueryEngineOpBase extends QueryEngineBase implements OpExec
{
    private Op queryOp = null ;
    private AlgebraGenerator gen = null ;
    private Plan plan = null ;
    
    protected QueryEngineOpBase(Query query,
                                DatasetGraph dataset, 
                                AlgebraGenerator gen,
                                Binding input,
                                Context context)
    {
        super(query, dataset, input, context) ;
        this.gen = gen ;
    }
    
    public Plan getPlan()
    {
        if ( plan == null )
            plan = createPlan() ;
        return plan ;
    }
    
    public Plan createPlan()
    {
        QueryIterator queryIterator = eval(getOp(), getInputBinding(), 
                                           getDatasetGraph(), context()) ;
        return new PlanOp(getOp(), queryIterator) ;
    }
    
    final
    public QueryIterator eval(Op op, Graph graph)
    { return eval(op, new DataSourceGraphImpl(graph), ARQ.getContext()) ; }
    
    final
    public QueryIterator eval(Op op, DatasetGraph dsg, Context context)
    { return eval(op, BindingRoot.create(), dsg, context) ; }
    
    public QueryIterator eval(Op op, Binding binding, DatasetGraph dsg, Context context)
    {
        if ( binding.vars().hasNext() )
            // Easy ways to fix this limitation - use a wrapper to add the necessary bindings.
            // Or mess with table to join in the binding.
            // Or ...
            throw new QueryExecException("Initial bindings to ref evaluation") ;
        
        ExecutionContext execCxt = new ExecutionContext(context, dsg.getDefaultGraph(), dsg) ;
        Evaluator eval = EvaluatorFactory.create(execCxt) ;
        Table table = Eval.eval(eval, op) ;
        QueryIterator qIter = table.iterator(execCxt) ;
        return QueryIteratorCheck.check(qIter, execCxt) ;
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
        return gen.compile(getQuery().getQueryPattern()) ;
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