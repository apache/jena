/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;

import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.mgt.Explain ;
import com.hp.hpl.jena.sparql.mgt.QueryEngineInfo ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Main part of a QueryEngine - something that takes responsibility for a complete query execution */ 
public abstract class QueryEngineBase implements OpEval, Closeable
{
    public final static QueryEngineInfo queryEngineInfo = new QueryEngineInfo() ;
    // See also ExecutinContext.getDataset()
    protected DatasetGraph dataset = null ;
    protected Context context ;
    private Binding startBinding ;
    
    private Query query = null ;
    private Op queryOp = null ;
    private Plan plan = null ;
    
    protected QueryEngineBase(Query query,
                              DatasetGraph dataset, 
                              Binding input,
                              Context cxt)
    {
        this(dataset, input, cxt) ;
        this.query = query ;
        query.setResultVars() ;
        // Unoptimized so far.
        setOp(createOp(query)) ;
    }
    
    protected QueryEngineBase(Op op, DatasetGraph dataset, Binding input, Context cxt)
    {
        this(dataset, input, cxt) ;
        // Ensure context setup - usually done in QueryExecutionBase
        // so it can be changed after initialization.
        if ( context == null )
            context = Context.setupContext(context, dataset) ;
        this.query = null ;
        setOp(op) ;
    }
    
    private QueryEngineBase(DatasetGraph dataset, Binding input, Context context)
    {
        this.context = context ;
        this.dataset = dataset ;    // Maybe null e.g. in query
        
        if ( input == null )
        {
            Log.warn(this, "Null initial input") ;
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
        // Decide the algebra to actually execute.
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
        if ( query != null ) 
            Explain.explain("QUERY", query, context) ;
        Explain.explain("ALGEBRA", op, context) ;
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

    /** Algebra expression (including any optimization) */
    public Op getOp() { return queryOp ; }
    
    protected Binding getStartBinding() { return startBinding ; }
    
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