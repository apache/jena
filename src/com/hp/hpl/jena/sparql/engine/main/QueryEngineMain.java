/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformScopeRename ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Substitute ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryEngineBase ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRoot ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorCheck ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorTiming ;
import com.hp.hpl.jena.sparql.util.Context ;

public class QueryEngineMain extends QueryEngineBase
{
    static public QueryEngineFactory getFactory() { return factory ; } 
    static public void register()       { QueryEngineRegistry.addFactory(factory) ; }
    static public void unregister()     { QueryEngineRegistry.removeFactory(factory) ; }

    public QueryEngineMain(Op op, DatasetGraph dataset, Binding input, Context context)
    { super(op, dataset, input, context) ; }
    
    public QueryEngineMain(Query query, DatasetGraph dataset, Binding input, Context context)
    { 
        super(query, dataset, input, context) ;
    }

    // This does initial bindings by substituting immediately, rather than flowing the binding only.
    public static boolean SUBSTITUE = true ;
    
    @Override
    public QueryIterator eval(Op op, DatasetGraph dsg, Binding input, Context context)
    {
        if ( SUBSTITUE && ! input.isEmpty() )
            op = Substitute.substitute(op, input) ;
        
        ExecutionContext execCxt = new ExecutionContext(context, dsg.getDefaultGraph(), dsg, QC.getFactory(context)) ;
        QueryIterator qIter1 = QueryIterRoot.create(input, execCxt) ;
        QueryIterator qIter = QC.execute(op, qIter1, execCxt) ;
        // Wrap with something to check for closed iterators.
        qIter = QueryIteratorCheck.check(qIter, execCxt) ;
        // Need call back.
        if ( context.isTrue(ARQ.enableExecutionTimeLogging) )
            qIter = QueryIteratorTiming.time(qIter) ;
        return qIter ;
    }
    
//    protected Op createOp(Query query, AlgebraGenerator gen)
//    {
//        Op op = super.createOp(query, gen) ;
//        return op ;
//    }
    
    @Override
    protected Op modifyOp(Op op)
    { 
        if ( context.isFalse(ARQ.optimization) )
            return minimalModifyOp(op) ;
        return Algebra.optimize(op, super.context) ;
    }
    
    protected Op minimalModifyOp(Op op)
    {
        // Must always do this for QueryEngineMain
        // The optimizer does do this.
        return TransformScopeRename.transform(op) ;
    }
    
//    @Override
//    public void close()
//    { 
//        super.close() ; 
//    }
    
    // -------- Factory
    
    private static QueryEngineFactory factory = new QueryEngineFactory()
    {
        public boolean accept(Query query, DatasetGraph dataset, Context context) 
        { return true ; }

        public Plan create(Query query, DatasetGraph dataset, Binding input, Context context)
        {
            QueryEngineMain engine = new QueryEngineMain(query, dataset, input, context) ;
            return engine.getPlan() ;
        }
        
        public boolean accept(Op op, DatasetGraph dataset, Context context) 
        { return true ; }

        public Plan create(Op op, DatasetGraph dataset, Binding binding, Context context)
        {
            QueryEngineMain engine = new QueryEngineMain(op, dataset, binding, context) ;
            return engine.getPlan() ;
        }
    } ;
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