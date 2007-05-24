/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.n3.RelURI;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.*;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandler;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandlerRegistry;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.DatasetUtils;
import com.hp.hpl.jena.sparql.util.GraphUtils;
import com.hp.hpl.jena.sparql.util.ModelUtils;
import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.query.*;

/**
 * @author     Andy Seaborne
 * @version    $Id: QueryEngineBase.java,v 1.12 2007/02/05 17:11:13 andy_seaborne Exp $
 */
 
public abstract class QueryEngineBase implements QueryExecution//, QueryExecutionGraph
{
    private static Log      log                       = LogFactory.getLog(QueryEngineBase.class) ;

    protected Query         query ;

    static int              queryCount                = 0 ;
    protected boolean       queryExecutionInitialised = false ;
    protected boolean       queryExecutionClosed      = false ;
    protected int           idQueryExecution ;

    protected QueryIterator resultsIter ;
    private Context         context ;
    protected Plan          plan                      = null ;
    // private ExecutionContext execContext = null ;
    private QuerySolution   startBinding              = null ;
    private FileManager     fileManager               = null ;
    private Dataset         dataset                   = null ;
    private DatasetGraph    datasetGraph              = null ;
    
    protected QueryEngineBase(Query q, Context context)
    {
        // System initialized by now : Query class ensures ARQ initialized.
        if ( context == null || context == ARQ.getContext())
            context = ARQ.getContext().copy() ;
        query = q ;
        idQueryExecution = (++queryCount) ;
        this.context = context ;
    }
    
    public Query getQuery() { return query ; }
    
    /** Initialise a query execution.  
     * Does not build the plan though.
     * May be called before exec.
     * If it has not be called, the query engine will initialise
     * itself during the exec() method.
     */

    protected void init()
    {
        if (queryExecutionInitialised)
            return;

        startInitializing() ;

        // Fixup query.
        query.setResultVars() ;
        
        if ( getDataset() != null && getDatasetGraph() != null )
        {
            log.warn("Both dataset and datasetGraph are set (ignoring dataset") ;
            dataset = null ;
        }

        if ( getDataset() == null && getDatasetGraph() == null )
            datasetGraph  = buildDatasetForQuery() ;
        else
        {
            if ( getDataset() != null )
            {
                if ( getDataset().getDefaultModel() == null )
                    log.warn("Default model is null in the dataset") ;
                datasetGraph = new DataSourceGraphImpl(getDataset()) ;
            }
        }
        
        if ( getDataset() == null )
            dataset = new DataSourceImpl(datasetGraph) ;

        queryExecutionInitialised = true ;
        finishInitializing() ;
    }
    
    protected void startInitializing()
    {}

    protected void finishInitializing()
    {}

    public boolean hasDatasetOrDescription()
    {
        if ( getDataset() != null && getDatasetGraph() != null )
            return true ;
        if ( query.hasDatasetDescription() )
            return true ;
        return false ;
    }
    
    /** Called if a query execution needs a dataset : not called if a dataset has been
     * explicitly set (see also QueryEngineRegistry) 
     */
    protected DatasetGraph buildDatasetForQuery()
    {
        if ( ! query.hasDatasetDescription() ) 
            //Query.log.warn("No data for query (no URL, no model)");
            throw new QueryExecException("No dataset description for query");
        
        String baseURI = query.getBaseURI() ;
        if ( baseURI == null )
            baseURI = RelURI.chooseBaseURI() ;
        log.debug("init: baseURI for query is: "+baseURI) ; 
        
        DatasetGraph dsg =
            DatasetUtils.createDatasetGraph(query.getGraphURIs(),
                                            query.getNamedGraphURIs(),
                                            fileManager, baseURI ) ;
        return dsg ;
    }
    
    public void setInitialBinding(QuerySolution rb) { startBinding = rb ; }
    public void setFileManager(FileManager fm) { fileManager = fm ; }

    /**
     * @return Returns the dataset.
     */
    public Dataset getDataset()
    {
        return dataset;
    }
    /**
     * @param dataset The dataset to set.
     */
    public void setDataset(Dataset dataset)
    {
        this.dataset = dataset;
    }

    /**
     * @return Returns the dataset.
     */
    public DatasetGraph getDatasetGraph()
    {
        return datasetGraph;
    }

    
    
    /**
    * @param dataset The dataset to set.
    */
   public void setDatasetGraph(DatasetGraph dataset)
   {
       this.datasetGraph = dataset;
   }
   
    
    /** @return Return the parameters associated with this QueryEngine */
    public Context getContext() { return context ; }

    /** Execute the query and get back an iterator of bindings (graph level) */
    public QueryIterator exec()
    {
        return execInternalGraph() ;
    }
    
    
    public ResultSet execSelect()
    {
        if ( ! query.isSelectType() )
            throw new QueryExecException("Attempt to have ResultSet from a "+labelForQuery(query)+" query") ; 
        return execInternal() ;
    }


    // Construct
    public Model execConstruct()
    { return execConstruct(GraphUtils.makeJenaDefaultModel()) ; }
    
    public Model execConstruct(Model model)
    {
        if ( ! query.isConstructType() )
            throw new QueryExecException("Attempt to get a CONSTRUCT model from a "+labelForQuery(query)+" query") ;
        // This causes there to be no PROJECT around the pattern.
        // That in turn, exposes the initial bindings.  
        query.setQueryResultStar(true) ;
        
        ResultSet qRes = execInternal() ;

        // Prefixes for result
        insertPrefixesInto(model) ;
        Set set = new HashSet() ;
        Template template = query.getConstructTemplate() ;
        
        // Build each template substitution as triples.
        for ( ; qRes.hasNext() ; )
        {
            Map bNodeMap = new HashMap() ;
            QuerySolution qs = qRes.nextSolution() ;
            ResultBinding rb = (ResultBinding)qs ;
            template.subst(set, bNodeMap, rb.getBinding()) ; 
        }
        
        // Convert and merge into Model.
        for ( Iterator iter = set.iterator() ; iter.hasNext() ; )
        {
            Triple t = (Triple)iter.next() ;
            Statement stmt = ModelUtils.tripleToStatement(model, t) ;
            if ( stmt != null )
                model.add(stmt) ;
        }
        
        this.close() ;
        return model ;
    }

    public Model execDescribe()
    { return execDescribe(GraphUtils.makeJenaDefaultModel()) ; }

    
    public Model execDescribe(Model model)
    {
        if ( ! query.isDescribeType() )
            throw new QueryExecException("Attempt to get a DESCRIBE result from a "+labelForQuery(query)+" query") ; 
        query.setQueryResultStar(true) ;
        
        Set set = new HashSet() ;
        
        ResultSet qRes = execInternal() ;
        
        // Prefixes for result (after initialization)
        insertPrefixesInto(model) ;
        if ( qRes != null )
        {
            for ( ; qRes.hasNext() ; )
            {
                QuerySolution rb = qRes.nextSolution() ;
                for ( Iterator iter = query.getResultVars().iterator() ; iter.hasNext() ; )
                {
                    String varName = (String)iter.next() ;
                    RDFNode n = rb.get(varName) ;
                    set.add(n) ;
                }
            }
        }
        
        if ( query.getResultURIs() != null )
        {
            // Any URIs in the DESCRIBE
            for ( Iterator iter = query.getResultURIs().iterator() ; iter.hasNext() ; )
            {
                Node n = (Node)iter.next() ;
                RDFNode rNode = ModelUtils.convertGraphNodeToRDFNode(n, dataset.getDefaultModel()) ;
                set.add(rNode) ;
            }
        }

        // Create new handlers for this process.
        List dhList = DescribeHandlerRegistry.get().newHandlerList() ;
        
        // Notify start of describe phase
        for ( Iterator handlers = dhList.iterator() ; handlers.hasNext() ; )
        {
            DescribeHandler dh = (DescribeHandler)handlers.next() ;
            dh.start(model, getContext()) ;
        }
        
        // Do describe for each resource found.
        for (Iterator iter = set.iterator() ; iter.hasNext() ;)
        {
            RDFNode n = (RDFNode)iter.next() ;
        
            if ( n instanceof Resource )
            {
                for ( Iterator handlers = dhList.iterator() ; handlers.hasNext() ; )
                {
                    DescribeHandler dh = (DescribeHandler)handlers.next() ;
                    dh.describe((Resource)n) ;
                }
            }
            else
                // Can't describe literals
                continue ;
        }
        
        // Notify end of describe phase
        for ( Iterator handlers = dhList.iterator() ; handlers.hasNext() ; )
        {
            DescribeHandler dh = (DescribeHandler)handlers.next() ;
            dh.finish() ;
        }

        this.close() ;
        return model ; 
    }
    
    public boolean execAsk()
    {
        if ( ! query.isAskType() )
            throw new QueryExecException("Attempt to have boolean from a "+labelForQuery(query)+" query") ; 

        ResultSet results = execInternal() ;
        boolean r = results.hasNext() ;
        this.close() ;
        return r ; 
    }

    private ResultSet execInternal()
    {
        resultsIter = execInternalGraph() ;
        if ( resultsIter == null )
            // Empty WHERE clause
            return null ;
        
        // Ensure model set for rebuilding Resources
        Model model = null ;
        if ( dataset != null )
            model = dataset.getDefaultModel() ;
        if ( model == null )
        {
            Graph g = datasetGraph.getDefaultGraph() ;
            if ( g != null )
                model = ModelFactory.createModelForGraph(g) ;
            else
                model = null ;
        }
        
        // Not mods.projectVars which is List<Var>
        ResultSetStream rStream = new ResultSetStream(query.getResultVars(), model, resultsIter) ;
        
        // Set flags (the plan has the elements for solution modifiers)
        if ( query.hasOrderBy() )
            rStream.setOrdered(true) ;
        if ( query.isDistinct() )
            rStream.setDistinct(true) ;
        return rStream ;
    }
    
    private QueryIterator execInternalGraph()
    {
        init() ;
        if ( query.getQueryPattern() == null )
            // No WHERE part to query
            return null ;
        
        // Done in execInternal()
        // init() ;
        Plan plan = getPlan() ;
        QueryIterator qIter = plan.iterator() ;
        return qIter ;
    }
//    private void build() { build(false) ; }
//    
//    private void build(boolean surpressProject) { }
    
    // Further out ...
    
    /** Turn a query into a Plan - a thing that can produce the query iterator.
     * The modifiers and pattern are a convenience - the information has
     * been extracted for the query */
    
    protected abstract 
    Plan queryToPlan(Query query, QuerySolution startBinding) ;
    
    public Plan getPlan()
    {
        if ( plan != null )
            return plan ;
        
        if ( queryExecutionInitialised )
        {
            plan = queryToPlan(query, startBinding) ;
            return plan ;
        }
        //log.warn("getPlan()/Uninitialized route") ;
        // Not initialized - fake initialization, do plan, unfake.
        DatasetGraph dsg = datasetGraph ;
        if ( dsg == null )
        {
            DataSourceGraph gsrc = new DataSourceGraphImpl() ;
            gsrc.setDefaultGraph(GraphUtils.makeJenaDefaultGraph()) ;
            setDatasetGraph(gsrc) ;
        }
        Plan p = queryToPlan(query, startBinding) ;
        if ( dsg == null )
            setDatasetGraph(dsg) ;
        return p ;
    }
    
    /** Abnormal end of this execution  */
    public void abort()
    {
        // The close operation below does not mind if the
        // query has not been exhausted.
        close() ;
    }

    /** Normal end of use of this execution  */
    public void close()
    {
        if ( ! queryExecutionInitialised )
        {
            log.warn("Closing a query that has not been run") ;
            return ;
        }
        
        if ( resultsIter != null )
            resultsIter.close() ;
        resultsIter = null ;
    }
    
    private void insertPrefixesInto(Model model)
    {
        try {
            if ( datasetGraph == null )
                log.fatal("No default set graph") ;
            
            // Load the models prefixes first
            PrefixMapping m = datasetGraph.getDefaultGraph().getPrefixMapping() ;
            model.setNsPrefixes(m) ;
            
            // Then add the queries (just the declared mappings)
            // so the query declarations override the data sources. 
            model.setNsPrefixes(query.getPrefixMapping()) ;

        } catch (Exception ex)
        {
            log.warn("Exception in insertPrefixes: "+ex.getMessage(), ex) ;
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
}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
