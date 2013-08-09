/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.engine;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Substitute ;
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
        Op op = queryOp ;
        if ( ! startBinding.isEmpty() ) {
            op = Substitute.substitute(op, startBinding) ;
            context.put(ARQConstants.sysCurrentAlgebra, op) ;
            // Don't reset the startBinding because it also is
            // needed in the output.
        }
        op = modifyOp(op) ;

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
    @Override
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
    
    @Override
    public void close()
    { }
    
    protected void setOp(Op op)
    { 
        queryOp = op ;
        context.put(ARQConstants.sysCurrentAlgebra, op) ;
    }
}
