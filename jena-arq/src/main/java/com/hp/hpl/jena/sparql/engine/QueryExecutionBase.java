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

import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;
import java.util.concurrent.TimeUnit ;

import org.apache.jena.atlas.lib.AlarmClock ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.n3.IRIResolver ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandler ;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandlerRegistry ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorWrapper ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.modify.TemplateLib ;
import com.hp.hpl.jena.sparql.syntax.ElementGroup ;
import com.hp.hpl.jena.sparql.syntax.Template ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.DatasetUtils ;
import com.hp.hpl.jena.sparql.util.ModelUtils ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;
import com.hp.hpl.jena.util.FileManager ;

/** All the SPARQL query result forms made from a graph-level execution object */ 

public class QueryExecutionBase implements QueryExecution
{
    // Pull over the "build dataset code"
    // Initial bindings.
    // Split : QueryExecutionGraph already has the dataset.

    private Query               query ;
    private Dataset             dataset ;
    private QueryEngineFactory  qeFactory ;
    private QueryIterator       queryIterator = null ;
    private Plan                plan = null ;
    private Context             context ;
    private FileManager         fileManager = FileManager.get() ;
    private QuerySolution       initialBinding = null ; 
    
    // Set if QueryIterator.cancel has been called 
    private volatile boolean    isCancelled = false ;
    private volatile TimeoutCallback expectedCallback = null ;    
    private TimeoutCallback timeout1Callback = null ;
    private TimeoutCallback timeout2Callback = null ;
    
    private final Object        lockTimeout = new Object() ;     // synchronization.  
    private static final long   TIMEOUT_UNSET = -1 ;
    private static final long   TIMEOUT_INF = -2 ;
    private long                timeout1 = TIMEOUT_UNSET ;
    private long                timeout2 = TIMEOUT_UNSET ;
    private final AlarmClock    alarmClock = AlarmClock.get() ;  

    public QueryExecutionBase(Query query, 
                              Dataset dataset,
                              Context context,
                              QueryEngineFactory qeFactory)
    {
        this.query = query ;
        this.dataset = dataset ;
        this.context = context ;
        this.qeFactory = qeFactory ;
        init() ;
    }
    
    private void init()
    {
        DatasetGraph dsg = (dataset == null) ? null : dataset.asDatasetGraph() ;
        context = Context.setupContext(context, dsg) ;
        if ( query != null )
            context.put(ARQConstants.sysCurrentQuery, query) ;
        // NB: Settign timeouts via the context after creating a QueryExecutionBase 
        // will not work.
        // But we can't move it until the point the execution starts because of
        // get and set timeout oeprations on this object.   
        setAnyTimeouts() ;
    }
    
    // Put any per-dataset execution global configuration state here.
    public static Context setupContext(Context context, DatasetGraph dataset)
    {
        if ( context == null )
            context = ARQ.getContext() ;    // Already copied?
        context = context.copy() ;

        if ( dataset != null && dataset.getContext() != null )
            // Copy per-dataset settings.
            context.putAll(dataset.getContext()) ;
        
        context.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime()) ;
        
        // Allocators.
//        context.set(ARQConstants.sysVarAllocNamed, new VarAlloc(ARQConstants.allocVarMarkerExec)) ;
//        context.set(ARQConstants.sysVarAllocAnon,  new VarAlloc(ARQConstants.allocVarAnonMarkerExec)) ;
        // Add VarAlloc for variables and bNodes (this is not the parse name). 
        // More added later e.g. query (if there is a query), algebra form (in setOp)
        
        return context ; 
    }
    
    private void setAnyTimeouts()
    {
        if ( context.isDefined(ARQ.queryTimeout) )
        {
            Object obj = context.get(ARQ.queryTimeout) ;
            if ( obj instanceof Number )
            {
                long x = ((Number)obj).longValue() ;
                setTimeout(x) ;
            } else if ( obj instanceof String )
            {
                try {
                    String str = obj.toString() ;
                    if ( str.contains(",") )
                    {
                        String[] a = str.split(",") ;
                        long x1 = Long.parseLong(a[0]) ;
                        long x2 = Long.parseLong(a[1]) ;
                        setTimeout(x1, x2) ;
                    }
                    else
                    {
                        long x = Long.parseLong(str) ;
                        setTimeout(x) ;
                    }
                } catch (RuntimeException ex) { Log.warn(this, "Can't interpret string for timeout: "+obj) ; }
            }
            else
                Log.warn(this, "Can't interpret timeout: "+obj) ;
        }
    }
    
    @Override
    public void close()
    {
        if ( queryIterator != null )
            queryIterator.close() ;
        if ( plan != null )
            plan.close() ;
        if ( timeout1Callback != null )
            alarmClock.cancel(timeout1Callback) ;
        if ( timeout2Callback != null )
            alarmClock.cancel(timeout2Callback) ;
    }

    @Override
    public void abort()
    {
        synchronized(lockTimeout)
        {
            // This is called asynchronously to the execution.
            // synchronized is for coordination with other calls of
            // .abort and with the timeout2 reset code. 
            if ( queryIterator != null ) 
                // we notify the chain of iterators, however, we do *not* close the iterators. 
                // That happens after the cancellation is properly over.
                queryIterator.cancel() ;
            isCancelled = true ;
        }
    }
    
    @Override
    public ResultSet execSelect()
    {
        if ( ! query.isSelectType() )
            throw new QueryExecException("Attempt to have ResultSet from a "+labelForQuery(query)+" query") ; 
        return execResultSet() ;
    }

    // Construct
    @Override
    public Model execConstruct()
    {
        return execConstruct(GraphFactory.makeJenaDefaultModel()) ;
    }

//    /**
//     * Executes as a construct query, placing the results into a newly constructed {@link com.hp.hpl.jena.sparql.graph.GraphDistinctDataBag}.
//     * The threshold policy is set from the current context.
//     */
//    @Override
//    public Model execConstructDataBag()
//    {
//        ThresholdPolicy<Triple> thresholdPolicy = ThresholdPolicyFactory.policyFromContext(context);
//        return execConstruct(GraphFactory.makeDataBagModel(thresholdPolicy)) ;
//    }
    
    @Override
    public Model execConstruct(Model model)
    {
        try
        {
            Iterator<Triple> it = execConstructTriples();
            
            // Prefixes for result
            insertPrefixesInto(model);
            
            while (it.hasNext())
            {
                Triple t = it.next();
                Statement stmt = ModelUtils.tripleToStatement(model, t);
                if ( stmt != null )
                    model.add(stmt);
            }
        }
        finally
        {
            this.close();
        }
        return model;
    }
    
    @Override
    public Iterator<Triple> execConstructTriples()
    {
        if ( ! query.isConstructType() )
            throw new QueryExecException("Attempt to get a CONSTRUCT model from a "+labelForQuery(query)+" query") ;
        // This causes there to be no PROJECT around the pattern.
        // That in turn, exposes the initial bindings.  
        query.setQueryResultStar(true) ;

        startQueryIterator() ;
        
        Template template = query.getConstructTemplate() ;
        return TemplateLib.calcTriples(template.getTriples(), queryIterator);
    }

    @Override
    public Model execDescribe()
    { return execDescribe(GraphFactory.makeJenaDefaultModel()) ; }


    @Override
    public Model execDescribe(Model model)
    {
        if ( ! query.isDescribeType() )
            throw new QueryExecException("Attempt to get a DESCRIBE result from a "+labelForQuery(query)+" query") ; 
        //Was: query.setQueryResultStar(true) ; but why?
        query.setResultVars() ;
        // If there was no WhereClause, use an empty pattern (one solution, no columns). 
        if ( query.getQueryPattern() == null )
            query.setQueryPattern(new ElementGroup()) ;
        
        Set<RDFNode> set = new HashSet<RDFNode>() ;

        //May return null (no query pattern) 
        ResultSet qRes = execResultSet() ;

        // Prefixes for result (after initialization)
        insertPrefixesInto(model) ;
        if ( qRes != null )
        {
            for ( ; qRes.hasNext() ; )
            {
                QuerySolution rb = qRes.nextSolution() ;
                for ( String varName : query.getResultVars() )
                {
                    RDFNode n = rb.get(varName) ;
                    set.add(n) ;
                }
            }
        }

        if ( query.getResultURIs() != null )
        {
            // Any URIs in the DESCRIBE
            for (Node n : query.getResultURIs())
            {
                // Need to make dataset available to describe handlers.
                RDFNode rNode = ModelUtils.convertGraphNodeToRDFNode(n, dataset.getDefaultModel()) ;
                set.add(rNode) ;
            }
        }

        // Create new handlers for this process.
        List<DescribeHandler> dhList = DescribeHandlerRegistry.get().newHandlerList() ;

        getContext().put(ARQConstants.sysCurrentDataset, getDataset()) ;
        // Notify start of describe phase
        for (DescribeHandler dh : dhList)
            dh.start(model, getContext()) ;

        // Do describe for each resource found.
        for (Iterator<RDFNode> iter = set.iterator() ; iter.hasNext() ;)
        {
            RDFNode n = iter.next() ;

            if ( n instanceof Resource )
            {
                for (DescribeHandler dh : dhList)
                    dh.describe((Resource)n) ;
            }
            else
                // Can't describe literals
                continue ;
        }

        for (DescribeHandler dh : dhList)
            dh.finish() ;

        this.close() ;
        return model ; 
    }
    
    // TODO not memory efficient
    @Override
    public Iterator<Triple> execDescribeTriples()
    {
        return ModelUtils.statementsToTriples(execDescribe().listStatements());
    }

    @Override
    public boolean execAsk()
    {
        if ( ! query.isAskType() )
            throw new QueryExecException("Attempt to have boolean from a "+labelForQuery(query)+" query") ; 

        startQueryIterator() ;
        boolean r = queryIterator.hasNext() ;
        this.close() ;
        return r ; 
    }

    @Override
    public void setTimeout(long timeout, TimeUnit timeUnit)
    {
        // Overall timeout - recorded as (UNSET,N)
        long x = asMillis(timeout, timeUnit) ;
        this.timeout1 = TIMEOUT_UNSET ;
        this.timeout2 = x ;
    }

    @Override
    public void setTimeout(long timeout)
    {
        setTimeout(timeout, TimeUnit.MILLISECONDS) ;
    }

    @Override
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2)
    {
        // Two timeouts.
        long x1 = asMillis(timeout1, timeUnit1) ;
        long x2 = asMillis(timeout2, timeUnit2) ;
        this.timeout1 = x1 ;
        if ( timeout2 < 0 )
            this.timeout2 = TIMEOUT_UNSET ;
        else
            this.timeout2 = x2 ;
    }

    @Override
    public void setTimeout(long timeout1, long timeout2)
    {
        setTimeout(timeout1, TimeUnit.MILLISECONDS, timeout2, TimeUnit.MILLISECONDS) ;
    }

    private static long asMillis(long duration, TimeUnit timeUnit)
    {
        return (duration < 0 ) ? duration : timeUnit.toMillis(duration) ;
    }
    
    @Override
    public long getTimeout1() { return timeout1 ; }
    @Override
    public long getTimeout2() { return timeout2 ; }
    
    private static boolean isTimeoutSet(long x)
    { 
        return x >= 0 ;
    }

    class TimeoutCallback implements Runnable
    {
        @Override
        public void run()
        {
            synchronized(lockTimeout)
            {
                // Abort query if and only if we are the expected callback.
                // If the first row has appeared, and we are removing timeout1 callback,
                // it still may go off so it needs to check here it's still wanted.
                if ( expectedCallback == this )
                    QueryExecutionBase.this.abort() ;
            }
        }
    }
    
    private class QueryIteratorTimer2 extends QueryIteratorWrapper
    {
        public QueryIteratorTimer2(QueryIterator qIter)
        {
            super(qIter) ;
        }
        
        long yieldCount = 0 ;
        boolean resetDone = false ;
        @Override
        protected Binding moveToNextBinding()
        { 
            Binding b = super.moveToNextBinding() ;
            yieldCount++ ;
            
            if ( ! resetDone )
            {
                // Sync on calls of .abort.
                // So nearly not needed.
                synchronized(lockTimeout)
                {
                    expectedCallback = timeout2Callback ;
                    // Lock against calls of .abort() nor of timeout1Callback. 
                    
                    // Update/check the volatiles in a careful order.
                    // This cause timeout1 not to call .abort and hence not set isCancelled 

                    // But if timeout1 went off after moveToNextBinding, before expectedCallback is set,
                    // then formget the row and cacnel the query. 
                    if ( isCancelled )
                        // timeout1 went off after the binding was yielded but 
                        // before we got here.
                        throw new QueryCancelledException() ;
                    if ( timeout1Callback != null )
                        alarmClock.cancel(timeout1Callback) ;
                        timeout1Callback = null ;

                    // Now arm the second timeout, if any.
                    if ( timeout2 > 0 )
                        // Not first timeout - finite second timeout. 
                        alarmClock.add(timeout2Callback, timeout2) ;
                    resetDone = true ;
                }
            }
            return b ;
        }
    }
    
    protected void execInit() { }

    private ResultSet asResultSet(QueryIterator qIter)
    {
        Model model = null ;
        if ( dataset != null )
            model = dataset.getDefaultModel() ;
        else
            model = ModelFactory.createDefaultModel() ;
        
        ResultSetStream rStream = new ResultSetStream(query.getResultVars(), model, qIter) ;
        return rStream ;
    }
    
    /** Start the query iterator, setting timeouts as needed. */ 
    private void startQueryIterator()
    {
        execInit() ;
        if ( queryIterator != null )
            Log.warn(this, "Query iterator has already been started") ;
        
        /* Timeouts:
         * -1,-1                No timeouts
         * N, same as -1,N      Overall timeout only.  No wrapper needed.
         * N,-1                 Timeout on first row only. Need to cancel on first row. 
         * N,M                  First/overall timeout. Need to reset on first row.
         */
        
        if ( ! isTimeoutSet(timeout1) && ! isTimeoutSet(timeout2) )
        {
            // Case -1,-1
            queryIterator = getPlan().iterator() ;
            return ;
        }
        
        if ( ! isTimeoutSet(timeout1) && isTimeoutSet(timeout2) )
        {
            // Single overall timeout.
            timeout2Callback = new TimeoutCallback() ; 
            expectedCallback = timeout2Callback ; 
            alarmClock.add(timeout2Callback, timeout2) ;
            // Start the query.
            queryIterator = getPlan().iterator() ;
            // But don't add resetter.
            return ;
        }

        // Case isTimeoutSet(timeout1)
        // Add timeout to first row.
        timeout1Callback = new TimeoutCallback() ; 
        alarmClock.add(timeout1Callback, timeout1) ;
        expectedCallback = timeout1Callback ;

        // We don't know if getPlan().iterator() does a lot of work or not
        // (ideally it shouldn't start executing the query but in some sub-systems 
        // it might be necessary)
        queryIterator = getPlan().iterator() ;
        
        // Add the timeout resetter wrapper.
        timeout2Callback = new TimeoutCallback() ; 
        // Wrap with a resetter.
        queryIterator = new QueryIteratorTimer2(queryIterator) ;

        // Minor optimization - the first call of hasNext() or next() will
        // throw QueryCancelledExcetion anyway.  This just makes it a bit earlier
        // in the case when the timeout (timoeut1) is so short it's gone off already.
        
        if ( isCancelled ) queryIterator.cancel() ;
    }
    
    private ResultSet execResultSet()
    {
        startQueryIterator() ;
        return asResultSet(queryIterator) ; 
    }

    public Plan getPlan() 
    {
        if ( plan == null )
        {
            DatasetGraph dsg = prepareDataset(dataset, query, fileManager) ;
            Binding inputBinding = null ;
            if ( initialBinding != null )
                inputBinding = BindingUtils.asBinding(initialBinding) ;
            if ( inputBinding == null )
                inputBinding = BindingRoot.create() ;

            plan = qeFactory.create(query, dsg, inputBinding, getContext()) ;
        }            
        return plan ;
    }

    private void insertPrefixesInto(Model model)
    {
        try {
            if ( dataset != null )
            {
                // Load the models prefixes first
                PrefixMapping m = dataset.getDefaultModel() ;
                model.setNsPrefixes(m) ;
            }
            // Then add the queries (just the declared mappings)
            // so the query declarations override the data sources. 
            model.setNsPrefixes(query.getPrefixMapping()) ;

        } catch (Exception ex)
        {
            Log.warn(this, "Exception in insertPrefixes: "+ex.getMessage(), ex) ;
        }
    }

    static private String labelForQuery(Query q)
    {
        if ( q.isSelectType() )     return "SELECT" ; 
        if ( q.isConstructType() )  return "CONSTRUCT" ; 
        if ( q.isDescribeType() )   return "DESCRIBE" ; 
        if ( q.isAskType() )        return "ASK" ;
        return "<<unknown>>" ;
    }

    @Override
    public Context getContext() { return context ; }
    
    @Override
    public Dataset getDataset() { return dataset ; }

    @Override
    public Query getQuery()     { return query ; }

    // Call after setFM called.
    private static DatasetGraph prepareDataset(Dataset dataset, Query query, FileManager fileManager)
    {
        if ( dataset != null )
            return dataset.asDatasetGraph() ;
        
        if ( ! query.hasDatasetDescription() ) 
            //Query.Log.warn(this, "No data for query (no URL, no model)");
            throw new QueryExecException("No dataset description for query");
        
        String baseURI = query.getBaseURI() ;
        if ( baseURI == null )
            baseURI = IRIResolver.chooseBaseURI() ;
        
        DatasetGraph dsg = DatasetUtils.createDatasetGraph(query.getDatasetDescription(),
                                                           fileManager, baseURI ) ;
        return dsg ;
    }
    
    @Override
    public void setFileManager(FileManager fm) { fileManager = fm ; }
    
    @Override
    public void setInitialBinding(QuerySolution startSolution)
    { 
        initialBinding = startSolution ;
    }
    
    //protected QuerySolution getInputBindings() { return initialBinding ; }

    public void setInitialBindings(ResultSet table)
    { throw new UnsupportedOperationException("setInitialBindings(ResultSet)") ; }
}
