/*
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdql;
import java.util.* ;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.util.ModelLoader ;

/**
 * @author     Andy Seaborne
 * @version    $Id: QueryEngine.java,v 1.9 2003-08-27 12:25:58 andy_seaborne Exp $
 */
 
public class QueryEngine implements QueryExecution
{
    Query query ;

    static int queryCount = 0 ;
    boolean queryInitialised = false ;
    int idQueryExecution ;
    
    ResultsIterator resultsIter ;
    // Statistics
    long queryStartTime = -1 ;

    public QueryEngine(Query q)
    {
        query = q ;
        idQueryExecution = (++queryCount) ;
    }

    /** Initialise a query execution.  May be called before exec.
     *  If it has not be called, the query engine will initialise
     *  itself during the exec() method.
     */

    public void init()
    {
        if (queryInitialised)
            return;

        if (query.getSource() == null)
        {
            if (query.sourceURL == null)
            {
                Query.logger.warn("No data for query (no URL, no model)");
                throw new QueryException("No model for query");
            }
            long startTime = System.currentTimeMillis();
            Model src = ModelLoader.loadModel(query.sourceURL, null) ;
            if ( src == null )
                throw new QueryException("Failed to load data source") ;
            query.setSource(src);
            query.loadTime = System.currentTimeMillis() - startTime;
        }
        queryInitialised = true;
    }
    
    /** Execute a query and get back the results.
     * @return QueryResults
     */
    
    public QueryResults exec() { return exec(null) ; }

    /** Execute a query, passing in an initial binding of some of the variables in the query.
     * @param  startBinding Initial values of variables.
     * @return QueryResults
     */
    
    public QueryResults exec(ResultBinding startBinding)
    {
        init() ;
        resultsIter = new ResultsIterator(query, startBinding) ;
        return new QueryResultsStream(query, this, resultsIter) ;
    }

    public void abort()
    {
        resultsIter.close() ;
    }

    /** Normal end of use of this execution  */
    public void close()
    {
        resultsIter.close() ;
    }


    static class ResultsIterator implements ClosableIterator
    {
        Node[] projectionVars ;
        Query query ;

        ResultBinding nextBinding = null ;
        boolean finished = false ;
        ClosableIterator planIter ;
        ResultBinding initialBindings ;
         
        ResultsIterator(Query q, ResultBinding presets)
        {
            query = q ;
            initialBindings = presets ;
            // Build the graph query plan etc.        
            Graph graph = query.getSource().getGraph();

            QueryHandler queryHandler = graph.queryHandler();
            com.hp.hpl.jena.graph.query.Query graphQuery = new com.hp.hpl.jena.graph.query.Query();

            for (Iterator iter = query.getTriplePatterns().listIterator(); iter.hasNext();)
            {
                Triple t = (Triple) iter.next();
                t = substituteIntoTriple(t, presets) ;
                graphQuery.addMatch(t);
            }

            projectionVars = new Node[query.getBoundVars().size()];

            for (int i = 0; i < projectionVars.length; i++)
            {
                projectionVars[i] = Node.createVariable((String) query.getBoundVars().get(i));
            }

            BindingQueryPlan plan = queryHandler.prepareBindings(graphQuery, projectionVars);
            planIter = plan.executeBindings() ;
        }
          
        public boolean hasNext()
        {
            if ( finished )
                return false ;
            // Loop until we get a binding that is satifactory
            // or we run out of candidates. 
            while ( nextBinding == null )
            {
                if ( ! planIter.hasNext() )
                    break ;
                // Convert from graph form to model form
                Domain d = (Domain)planIter.next() ;
                nextBinding = new ResultBinding(initialBindings) ;
                nextBinding.setQuery(query) ;
                for ( int i = 0 ; i < projectionVars.length ; i++ )
                {
                    String name = projectionVars[i].toString().substring(1) ;
                    Node n = (Node)d.get(i) ;
                    if ( n == null )
                    {
                        // There was no variable of this name
                        // May have been prebound.
                        // (Later) may have optionally bound variables.
                        // Otherwise, should not occur but this is safe.
                        continue ;
                    }
    
                    // Convert graph node to model RDFNode
                    RDFNode rdfNode = convertNodeToRDFNode(n, query.getSource()) ;
                    nextBinding.add(name, rdfNode) ;
                }
                
                // Verify constriants
                boolean passesTests = true;
                for (Iterator cIter = query.constraints.iterator(); cIter.hasNext();)
                {
                    Constraint constraint = (Constraint)cIter.next();
                    if (!constraint.isSatisfied(query, nextBinding))
                    {
                        passesTests = false;
                        break;
                    }
                }
                if (!passesTests)
                {
                    nextBinding = null ;
                    continue ;
                }
            }       

            if ( nextBinding == null )
            {
                close() ;
                return false ;
            }
            return true ;
        }

        public Object next()
        {
            if ( nextBinding == null )
                throw new NoSuchElementException("QueryEngine.ResultsIterator") ;
            ResultBinding x = nextBinding ;
            nextBinding = null ;
            return x ;
        }

        public void remove()
        {
            throw new UnsupportedOperationException("QueryEngine.ResultsIterator.remove") ; 
        }
        
        public void close()
        {
            if ( ! finished )
            {
                planIter.close() ;
                finished = true ;
            }
        }
    }

    static Node convertValueToNode(Value value)
    {
        if ( value.isRDFLiteral())
            return Node.createLiteral(
                    value.getRDFLiteral().getLexicalForm(),
                    value.getRDFLiteral().getLanguage(),
                    value.getRDFLiteral().getDatatype()) ;
            
        if ( value.isRDFResource())
        {
            if ( value.getRDFResource().isAnon())
                return Node.createAnon(value.getRDFResource().getId()) ;
            return Node.createURI(value.getRDFResource().getURI()) ;
        }
            
        if ( value.isURI())
            return Node.createURI(value.getURI()) ;
            
        return Node.createLiteral(value.asUnquotedString(),null ,null) ;
    }

    static RDFNode convertNodeToRDFNode(Node n, Model model)
    {
        if ( n.isLiteral() )
            return new LiteralImpl(n, model) ;
                
        if ( n.isURI() || n.isBlank() )
            return new ResourceImpl(n, model) ;
                
        if ( n.isVariable() )
        {
            // Hack
            System.err.println("Variable unbound: "+n) ;
            //binding.add(name, n) ;
            return null ;
        }
                
        System.err.println("Unknown node type for node: "+n) ;
        return null ;

    }

    static Triple substituteIntoTriple(Triple t, ResultBinding binding)
    {
        if ( binding == null )
           return t ;
        
        boolean keep = true ;
        Node subject = substituteNode(t.getSubject(), binding) ;
        Node predicate = substituteNode(t.getPredicate(), binding) ;
        Node object = substituteNode(t.getObject(), binding) ;
        
        if ( subject == t.getSubject() &&
             predicate == t.getPredicate() &&
             object == t.getObject() )
             return t ;
             
        return new Triple(subject, predicate, object) ;
    }
    
    static Node substituteNode(Node n, ResultBinding binding)
    {
        if ( ! n.isVariable() )
            return n ;
            
        String name = ((Node_Variable)n).getName() ;
        Object obj = binding.get(name) ;
        if ( obj == null )
            return n ;
            
        if ( obj instanceof RDFNode )
            return ((RDFNode)obj).asNode() ;
            
        if ( obj instanceof Value )
            return convertValueToNode((Value)obj) ;

        System.err.println("Unknown object in binding: ignored: "+obj.getClass().getName()) ;        
        return n ;
    }
}

/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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
