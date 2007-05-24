/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.sparql.ARQNotImplemented;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandler;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandlerRegistry;
import com.hp.hpl.jena.sparql.engine.QueryExecutionGraph;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.GraphUtils;
import com.hp.hpl.jena.sparql.util.ModelUtils;

import com.hp.hpl.jena.query.*;

/** All the SPARQL query result forms made form a graph-level execution object */ 

public class QueryExecutionBase implements QueryExecution
{
    // Pull over the "build dataset code"
    // Initial bindings.
    // Split : QueryExecutionGraph already has the dataset.
    
    private static Log log = LogFactory.getLog(QueryExecutionBase.class) ;

    private Query query ;
    private Dataset dataset ;
    private QueryExecutionGraph execGraph ;
    private QueryIterator queryIterator = null ;
    private Op queryOp = null ;

    // Make this on the way out.
    public QueryExecutionBase(Query query, Dataset dataset, QueryExecutionGraph execGraph)
    {
        this.query = query ;
        this.execGraph = execGraph ;
        this.dataset = dataset ;
    }
    
    public void abort()
    {
        if ( queryIterator != null )
            queryIterator.abort() ;
    }

    public void close()
    {
        if ( queryIterator != null )
            queryIterator.close() ;
    }

    public ResultSet execSelect()
    {
        execInit() ;
        if ( ! query.isSelectType() )
            throw new QueryExecException("Attempt to have ResultSet from a "+labelForQuery(query)+" query") ; 
        return execInternal() ;
    }


    // Construct
    public Model execConstruct()
    { return execConstruct(GraphUtils.makeJenaDefaultModel()) ; }

    public Model execConstruct(Model model)
    {
        execInit() ;
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
        execInit() ;
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
        execInit() ;
        if ( ! query.isAskType() )
            throw new QueryExecException("Attempt to have boolean from a "+labelForQuery(query)+" query") ; 

        ResultSet results = execInternal() ;
        boolean r = results.hasNext() ;
        this.close() ;
        return r ; 
    }

    protected void execInit()
    { }

    private ResultSet execInternal()
    {
        Model model = dataset.getDefaultModel() ;
        queryIterator = execGraph.exec() ;
        ResultSetStream rStream = new ResultSetStream(query.getResultVars(), model, queryIterator) ;
        
        // Set flags (the plan has the elements for solution modifiers)
        if ( query.hasOrderBy() )
            rStream.setOrdered(true) ;
        if ( query.isDistinct() )
            rStream.setDistinct(true) ;
        return rStream ;
    }

    private void insertPrefixesInto(Model model)
    {
        try {
            // Load the models prefixes first
            PrefixMapping m = dataset.getDefaultModel() ;
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

    public Context getContext()
    {
        return null ;
    }

    public void setFileManager(FileManager fm)
    { throw new ARQNotImplemented("setFileManager") ; }

    public void setInitialBinding(QuerySolution binding)
    { throw new ARQNotImplemented("setInitialBinding") ; }
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