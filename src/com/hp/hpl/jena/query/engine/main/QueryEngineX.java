/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.main;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.algebra.op.Op;
import com.hp.hpl.jena.query.engine.*;
import com.hp.hpl.jena.query.engine.ref.QueryEngineRef;
import com.hp.hpl.jena.query.syntax.Element;
import com.hp.hpl.jena.query.util.Context;

public class QueryEngineX extends QueryEngineRef
{
    private static QueryEngineFactory factory = new QueryEngineFactory()
    {
        public boolean accept(Query query, Dataset dataset) 
        { return true ; }

        public QueryExecution create(Query query, Dataset dataset)
        {
            QueryEngineX engine = new QueryEngineX(query) ;
            engine.setDataset(dataset) ;
            return engine ;
        }
    } ;
    
    static public void register()
    {
        QueryEngineRegistry.addFactory(factory) ;
    }
    
    static public void unregister()
    {
        QueryEngineRegistry.removeFactory(factory) ;
    }

    public QueryEngineX(Query query, Context context)
    { super(query, context) ; }

    public QueryEngineX(Query query)
    { super(query) ; }
    
    //@Override
    protected Plan queryToPlan(Query query, Modifiers mods, Element pattern)
    {
        Op op = getOp() ;
        op = modifyQueryOp(op) ;
        QueryIterator qIter = OpCompiler.compile(op, getExecContext()) ;
        return new PlanOp(op, qIter) ;
    }
    
    // Allow plan to be modifed
    // Have some subclass QueryEngineRef and add all extensibility points.
    // This one, others?
    // All this is not necessary but could clarify how to extend QueryEngines 
    protected Op modifyQueryOp(Op op)
    {
        return op ;
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