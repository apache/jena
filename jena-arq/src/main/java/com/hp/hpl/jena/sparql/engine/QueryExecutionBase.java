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

import org.openjena.atlas.lib.AlarmClock ;
import org.openjena.atlas.lib.Callback ;
import org.openjena.atlas.lib.Pingback ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.n3.IRIResolver ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecException ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandler ;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandlerRegistry ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorWrapper ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.modify.TemplateLib ;
import com.hp.hpl.jena.sparql.syntax.ElementGroup ;
import com.hp.hpl.jena.sparql.syntax.Template ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.DatasetUtils ;
import com.hp.hpl.jena.sparql.util.ModelUtils ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.util.FileManager ;

/** All the SPARQL query result forms made from a graph-level execution object */ 

public class QueryExecutionBase implements QueryExecution
{
    // Pull over the "build dataset code"
    // Initial bindings.
    // Split : QueryExecutionGraph already has the dataset.

    private Query              query ;
    private Dataset            dataset ;
    private QueryEngineFactory qeFactory ;
    private QueryIterator      queryIterator = null ;
    private Plan               plan = null ;
    private Context            context ;
    private FileManager        fileManager = FileManager.get() ;
    private QuerySolution      initialBinding = null ;      

    // has cancel() been called?
    private volatile boolean   cancel = false ;
    
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
        
        context.set(ARQConstants.sysCurrentTime, NodeFactory.nowAsDateTime()) ;
        
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
                //System.err.println("timeout("+x+")") ;
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
                        //System.err.println("timeout("+x1+", "+x2+")") ;
                        setTimeout(x1, x2) ;
                    }
                    else
                    {
                        long x = Long.parseLong(str) ;
                        //System.err.println("timeout("+x+")") ;
                        setTimeout(x) ;
                    }
                } catch (RuntimeException ex) { Log.warn(this, "Can't interpret string for timeout: "+obj) ; }
            }
            else
                Log.warn(this, "Can't interpret timeout: "+obj) ;
        }
    }
    
    // Old, synchronous code.
    // Delete when we are sure cancellation is stable.
//    public void abort()
//    {
//        abort = true ;
//        if ( queryIterator != null )
//            queryIterator.abort() ;
//        cancel = true ;
//    }

    @Override
    public void close()
    {
        if ( queryIterator != null )
            queryIterator.close() ;
        if ( plan != null )
            plan.close() ;
        cancelPingback() ;
    }

    @Deprecated
    public static boolean cancelAllowDrain = false ; 
    //public synchronized void cancel()
    @Override
    public synchronized void abort()
	{
	    // This is called asynchronously to the execution.
        // synchronized is for coordination with other calls of .abort.
		if ( queryIterator != null ) 
		{
			// we cancel the chain of iterators, however, we do *not* close the iterators. 
			// That happens after the cancellation is properly over.
		    if ( cancelAllowDrain && queryIterator instanceof QueryIteratorBase )
		    {
		        QueryIteratorBase qIter = (QueryIteratorBase)queryIterator ;
		        qIter.cancelAllowContinue() ;
		    }
		    else
		        // Normal case - correct SPARQL
		        queryIterator.cancel() ;
			cancel = true ;
		}
        cancel = true ;
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
        long x = asMillis(timeout, timeUnit) ;
        this.timeout1 = x ;
        this.timeout2 = TIMEOUT_UNSET ;
    }

    @Override
    public void setTimeout(long timeout)
    {
        setTimeout(timeout, TimeUnit.MILLISECONDS) ;
    }

    @Override
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2)
    {
        long x1 = asMillis(timeout1, timeUnit1) ;
        long x2 = asMillis(timeout2, timeUnit2) ;
        this.timeout1 = x1 ;
        if ( timeout2 < 0 )
            this.timeout2 = TIMEOUT_INF ;
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
    
    private static final long TIMEOUT_UNSET = -1 ;
    private static final long TIMEOUT_INF = -2 ;
    private long timeout1 = TIMEOUT_UNSET ;
    private long timeout2 = TIMEOUT_UNSET ;
    
    private static AlarmClock alarmClock = AlarmClock.get() ; 
    private static final Callback<QueryExecution> callback = 
        new Callback<QueryExecution>() {
            @Override
            public void proc(QueryExecution qExec)
            {
                qExec.abort() ;
                
            }
        } ;
        
    private Pingback<QueryExecution> pingback = null ;
    
    private void initTimeout1()
    {
        if ( timeout1 == TIMEOUT_UNSET ) return ;
        
        if ( pingback != null )
            alarmClock.reset(pingback, timeout1) ;
        else
            pingback = alarmClock.add(callback, this, timeout1) ;
        return ;
        // Second timeout done by wrapping the iterator.
    }
    
    private QueryIterator initTimeout2(QueryIterator queryIterator)
    {
        if ( timeout2 < 0 && timeout2 != TIMEOUT_INF )
            return queryIterator ;
        // Wrap with a resetter.
        return new QueryIteratorWrapper(queryIterator)
        {
            boolean resetDone = false ;
            @Override
            protected Binding moveToNextBinding()
            { 
                Binding b = super.moveToNextBinding() ;
                //System.out.println(b) ;
                if ( ! resetDone )
                {
                    //System.out.printf("Reset timer: ==> %d\n", timeout2) ;
                    if ( pingback == null )
                    {
                        if ( timeout2 > 0 )
                            // No first timeout - finite second timeout. 
                            pingback = alarmClock.add(callback, QueryExecutionBase.this, timeout2) ;
                    }
                    else
                    {
                        // We have moved for the first time.
                        // Reset the timer if finite timeout else cancel.
                        if ( timeout2 < 0 )
                            alarmClock.cancel(pingback) ;
                        else
                            pingback = alarmClock.reset(pingback, timeout2) ;
                    }
                    resetDone = true ;
                }
                return b ;
            }
        };
    }
    
    private void cancelPingback()
    {
        if ( pingback != null )
            alarmClock.cancel(pingback) ;
    }
    
    protected final void execInit()
    { }

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
    
    private void startQueryIterator()
    {
        execInit() ;
        if ( queryIterator != null )
            Log.warn(this, "Query iterator has already been started") ;
        initTimeout1() ;
        // We don't know if getPlan().iterator() does a lot of work or not
        // (ideally it shouldn't start executing the query but in some sub-systems 
        // it might be necessary)
        queryIterator = getPlan().iterator() ;
        // Add the second timeout wrapper.
        queryIterator = initTimeout2(queryIterator) ;
        if ( cancel ) queryIterator.cancel() ;
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
        
        DatasetGraph dsg =
            DatasetUtils.createDatasetGraph(query.getDatasetDescription(),
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
