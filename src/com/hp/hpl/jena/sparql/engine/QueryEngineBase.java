/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Graph;

import com.hp.hpl.jena.sparql.algebra.AlgebraGenerator;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.DataSourceGraphImpl;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.util.Context;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;

public abstract class QueryEngineBase implements OpExec
{
    private DatasetGraph dataset = null ;
    private Context context ;
    private Binding startBinding ;
    
    private Op queryOp = null ;
    private Plan plan = null ;
    
    protected QueryEngineBase(Query query,
                              DatasetGraph dataset, 
                              AlgebraGenerator gen,
                              Binding input,
                              Context context)
    {
        this(dataset, input, context) ;
        // Build the Op.
        queryOp = createOp(query, gen) ;
    }
    
    protected QueryEngineBase(Op op, DatasetGraph dataset, Binding input, Context context)
    {
        this(dataset, input, context) ;
        queryOp = op ;
    }
    
    private QueryEngineBase(DatasetGraph dataset, Binding input, Context context)
    {
        this.dataset = dataset ;    // Maybe null i.e. in query
        if ( context == null )      // Copy of global context to protect against chnage.
            context = ARQ.getContext().copy() ;
        this.context = context ;
        if ( input == null )
        {
            LogFactory.getLog(QueryEngineBase.class).warn("Null initial input") ;
            input = BindingRoot.create() ;
        }
        this.startBinding = input ;
    }
    
    public Plan getPlan()
    {
        if ( plan == null )
            plan = createPlan() ;
        return plan ;
    }
    
    protected Plan createPlan()
    {
        QueryIterator queryIterator = eval(queryOp, dataset, 
                                           startBinding, context) ;
        return new PlanOp(getOp(), queryIterator) ;
    }
    
    private Op createOp(Query query, AlgebraGenerator gen)
    {
        query.setResultVars() ;
        Op op = gen.compile(query) ;
        return op ;
    }
    
    public QueryIterator eval(Op op, Graph graph)
    { return eval(op, new DataSourceGraphImpl(graph), ARQ.getContext()) ; }
    
    public QueryIterator eval(Op op, DatasetGraph dsg, Context context)
    { return eval(op, dsg, BindingRoot.create(), context) ; }
    
    abstract 
    public QueryIterator eval(Op op, DatasetGraph dsg, Binding binding, Context context) ;
    
    protected Op getOp() { return queryOp ; }
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