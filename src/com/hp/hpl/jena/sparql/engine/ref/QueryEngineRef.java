/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.ref;

import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.algebra.opt.TransformPropertyFunction;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.engine.*;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorCheck;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.util.Context;

import com.hp.hpl.jena.query.Query;


public class QueryEngineRef extends QueryEngineBase
{
    public QueryEngineRef(Op op, DatasetGraph dataset, Context context)
    { this(op, dataset, null, context) ; }
    
    public QueryEngineRef(Op op, DatasetGraph dataset, Binding input, Context context)
    { super(op, dataset, input, context) ; }

    protected QueryEngineRef(Query query, DatasetGraph dataset,
                             Binding input, Context context)
    {
        super(query, dataset, input, context) ;
    }
    
    @Override
    protected Op modifyOp(Op op)
    {
        // Just property functions
        Transform t = new TransformPropertyFunction(context) ;
        op = Transformer.transform(t, op) ;
        return op ;
    }
    
    @Override
    public QueryIterator eval(Op op, DatasetGraph dsg, Binding binding, Context context)
    {
        if ( binding.vars().hasNext() )
            op = Substitute.substitute(op, binding) ;

        ExecutionContext execCxt = new ExecutionContext(context, dsg.getDefaultGraph(), dsg, QC.getFactory(context)) ;
        Evaluator eval = EvaluatorFactory.create(execCxt) ;
        Table table = Eval.eval(eval, op) ;
        QueryIterator qIter = table.iterator(execCxt) ;
        return QueryIteratorCheck.check(qIter, execCxt) ;
    }
    
    static public QueryEngineFactory getFactory()   { return factory ; } 
    static public void register()       { QueryEngineRegistry.addFactory(factory) ; }
    static public void unregister()     { QueryEngineRegistry.removeFactory(factory) ; }
    
    private static QueryEngineFactory factory = new QueryEngineFactory()
    {
        public boolean accept(Query query, DatasetGraph dataset, Context context) 
        { return true ; }

        public Plan create(Query query, DatasetGraph dataset, Binding binding, Context context)
        {
            QueryEngineRef engine = new QueryEngineRef(query, dataset, binding, context) ;
            return engine.getPlan() ;
        }
        
        public boolean accept(Op op, DatasetGraph dataset, Context context) 
        { return true ; }

        public Plan create(Op op, DatasetGraph dataset, Binding binding, Context context)
        {
            QueryEngineRef engine = new QueryEngineRef(op, dataset, binding, context) ;
            return engine.getPlan() ;
        }

    } ;
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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