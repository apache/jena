/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.DataSourceGraphImpl;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.*;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorCheck;
import com.hp.hpl.jena.sparql.util.Context;

public class QueryEngineMain extends QueryEngineOpBase
{
    static public QueryEngineFactory getFactory() { return factory ; } 
    static public void register()       { QueryEngineRegistry.addFactory(factory) ; }
    static public void unregister()     { QueryEngineRegistry.removeFactory(factory) ; }

    public QueryEngineMain(Query query, Context context)
    { super(query, context) ; }

    public QueryEngineMain(Query query)
    { super(query, ARQ.getContext()) ; }
    
    //@Override
    protected QueryIterator createQueryIterator(Op op)
    {
        Binding b = BindingRoot.create() ;
        
        // If there is some initial bindings
        if ( super.startBinding != null )
        {
            b = new BindingMap(b) ;
            BindingUtils.addToBinding(b, startBinding) ;
            // Substitute in the Op, and use this b as the root. 
            op = QC.substitute(op, b) ;
        }
        return eval(op, b, getExecContext()) ;
    }

    public static QueryIterator eval(Op op, Graph graph)
    {
        return eval(op, new DataSourceGraphImpl(graph)) ;
    }
    
    public static QueryIterator eval(Op op, DatasetGraph dsg)
    {
        ExecutionContext execCxt = new ExecutionContext(ARQ.getContext(), null, dsg.getDefaultGraph(), dsg) ;
        return eval(op, BindingRoot.create(), execCxt) ; 
    }
    
    private static QueryIterator eval(Op op, Binding b, ExecutionContext execCxt)
    {
        QueryIterator qIter = new QueryIterSingleton(b, execCxt) ;
        qIter = OpCompiler.compile(op, qIter, execCxt) ;
        // Wrap with something to check for closed iterators.
        qIter = QueryIteratorCheck.check(qIter, execCxt) ;
        return qIter ;
    }
    
    // -------- Factory
    
    private static QueryEngineFactory factory = new QueryEngineFactory()
    {
        public boolean accept(Query query, Dataset dataset) 
        { return true ; }

        public QueryExecution create(Query query, Dataset dataset)
        {
            QueryEngineMain engine = new QueryEngineMain(query) ;
            engine.setDataset(dataset) ;
            return engine ;
        }
    } ;
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