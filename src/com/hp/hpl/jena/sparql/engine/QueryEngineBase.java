/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.Closeable ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.mgt.QueryEngineInfo ;
import com.hp.hpl.jena.sparql.util.ALog ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;

/** Main part of a QueryEngine - somethign that takes responsibility for a complete query execution */ 
public abstract class QueryEngineBase implements OpEval, Closeable
{
    public final static QueryEngineInfo queryEngineInfo = new QueryEngineInfo() ;
    // See also ExecutinContext.getDataset()
    protected DatasetGraph dataset = null ;
    protected Context context ;
    private Binding startBinding ;
    
    private Op queryOp = null ;
    private Plan plan = null ;
    
    protected QueryEngineBase(Query query,
                              DatasetGraph dataset, 
                              //AlgebraGenerator gen,
                              Binding input,
                              Context context)
    {
        this(dataset, input, context) ;
        this.context.put(ARQConstants.sysCurrentQuery, query) ;
        // Build the Op.
        query.setResultVars() ;
        setOp(createOp(query)) ;
    }
    
    protected QueryEngineBase(Op op, DatasetGraph dataset, Binding input, Context context)
    {
        this(dataset, input, context) ;
        setOp(op) ;
    }
    
    private QueryEngineBase(DatasetGraph dataset, Binding input, Context context)
    {
        this.dataset = dataset ;    // Maybe null i.e. in query
        if ( context == null )      // Copy of global context to protect against chnage.
            context = ARQ.getContext().copy() ;
        this.context = context ;
        if ( input == null )
        {
            ALog.warn(this, "Null initial input") ;
            input = BindingRoot.create() ;
        }
        this.startBinding = input ;
        
        initContext(context) ;
    }
    
    // Put any per-query execution global configuration state here.
    private static void initContext(Context context)
    {
        context.set(ARQConstants.sysCurrentTime, NodeFactory.nowAsDateTime()) ;
        
//        context.set(ARQConstants.sysVarAllocNamed, new VarAlloc(ARQConstants.allocVarMarkerExec)) ;
//        context.set(ARQConstants.sysVarAllocAnon,  new VarAlloc(ARQConstants.allocVarAnonMarkerExec)) ;
        
        // Add VarAlloc for variables and bNodes (this is not the parse name). 
        // More added later e.g. query (if there is a query), algebra form (in setOp)
    }
    
    public Plan getPlan()
    {
        if ( plan == null )
            plan = createPlan() ;
        return plan ;
    }
    
    protected Plan createPlan()
    {
        Op op = modifyOp(queryOp) ;

        QueryIterator queryIterator = null ;
        if ( dataset != null )
            // Null means setting up but not executing a query.
            queryIterator = evaluate(op, dataset, startBinding, context) ;
        else
            // Bypass management interface
            queryIterator = evaluateNoMgt(op, dataset, startBinding, context) ;
        // This could be an automagic iterator to catch close.
        return new PlanOp(getOp(), this, queryIterator) ;
    }
    
    protected Op modifyOp(Op op)
    { return op ; }
    
    protected Op createOp(Query query)
    {
        Op op = Algebra.compile(query) ;
        return op ;
    }
    
    // Record the query operation as it goes pass and call the actual worker
    final
    public QueryIterator evaluate(Op op, DatasetGraph dsg, Binding binding, Context context)
    {
        queryEngineInfo.incQueryCount() ;
        queryEngineInfo.setLastQueryExecAt() ;
        //queryEngineInfo.setLastQueryExecTime(-1) ;
        queryEngineInfo.setLastQueryString((Query)context.get(ARQConstants.sysCurrentQuery)) ;
        queryEngineInfo.setLastOp(op) ;
        return eval(op, dsg, binding, context) ;
    }
    
    private QueryIterator evaluateNoMgt(Op op, DatasetGraph dsg, Binding binding, Context context)
    {
        return eval(op, dsg, binding, context) ;
    }
    
    abstract protected
    QueryIterator eval(Op op, DatasetGraph dsg, Binding binding, Context context) ;

    public Op getOp() { return queryOp ; }
    
    public void close()
    { }
    
    protected void setOp(Op op)
    { 
        queryOp = op ;
        context.put(ARQConstants.sysCurrentAlgebra, op) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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