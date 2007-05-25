/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.n3.RelURI;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.sparql.ARQNotImplemented;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.DatasetUtils;
import com.hp.hpl.jena.util.FileManager;

/** Build the graph level objects needed for query execution */

public abstract class QueryEngineBase
{
    private static Log log = LogFactory.getLog(QueryEngineBase.class) ;

    private Query query ;
    private DatasetGraph dataset = null ;
    private Context context ;
    private Binding startBinding ;
    private FileManager fileManager = FileManager.get();
    private Op queryOp = null ;

    public QueryEngineBase(Query query, DatasetGraph dataset, Context context)
    {
        this.query = query ;
        // Fixup query.
        query.setResultVars() ;
        this.dataset = dataset ;    // Maybe null i.e. in query
        if ( context == null )      // Copy of global context to protect against chnage.
            context = new Context(ARQ.getContext()) ;
        this.context = context ;
        startBinding = null ;
    }
    
    public DatasetGraph getDatasetGraph() 
    { 
        if ( dataset == null )
            dataset = prepareDataset(dataset, query, fileManager) ;
        return dataset ;
    }
    
    public Binding getInputBinding()
    {
        if ( startBinding == null )
            return BindingRoot.create() ;
        return startBinding ;
    }
    
    protected abstract Op getOp() ;

    protected Query getQuery() { return query ; }
    
    public Context getContext() { return context ; }

    protected void _setFileManager(FileManager fm) { fileManager = fm ; }

    protected void _setInitialBinding(Binding inputBinding)
    { startBinding = inputBinding ; }
    
    // Call after setFM called.
    private static DatasetGraph prepareDataset(DatasetGraph dataset, Query query, FileManager fileManager)
    {
        if ( dataset != null )
            throw new ARQNotImplemented("dataset.asDatasetGraph()") ;
            //return dataset.asDatasetGraph() ;
        
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