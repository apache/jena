/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.algebra.Op;
import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryEngineOpQuadBase;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.main.QueryEngineMain;
import com.hp.hpl.jena.query.engine.ref.Evaluator;
import com.hp.hpl.jena.query.engine.ref.EvaluatorFactory;
import com.hp.hpl.jena.query.util.Context;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.engine.compiler.OpSQL;
import com.hp.hpl.jena.sdb.engine.compiler.QC;
import com.hp.hpl.jena.sdb.engine.compiler.QueryCompiler;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.util.StoreUtils;


public class QueryEngineSDB extends QueryEngineOpQuadBase
{
    private static Log log = LogFactory.getLog(QueryEngineSDB.class) ; 
    private Store store ;
    private SDBRequest request = null ;
    private QueryCompiler queryCompiler = null ;

    public QueryEngineSDB(Store store, Query q)
    {
        this(store, q, null) ;
    }
    
    public QueryEngineSDB(Store store, Query q, Context context)
    {
        super(q, context) ;
        this.store = store ;
        request = new SDBRequest(store, query) ;
        if ( StoreUtils.isHSQL(store) )
            request.LeftJoinTranslation = false ;
        
        queryCompiler = store.getQueryCompilerFactory().createQueryCompiler(request) ;
    }

    public SDBRequest getRequest()      { return request ; }

    @Override
    protected QueryIterator createQueryIterator(Op op)
    {
        if ( ! ( op instanceof OpSQL ) )
        {
            Evaluator eval = EvaluatorFactory.create(getExecContext()) ;
            QueryIterator qIter = QueryEngineMain.eval(op, getExecContext().getDataset()) ;
            return qIter ;
        }

        // Direct
        OpSQL opSQL = (OpSQL)op ;
        ExecutionContext execCxt = getExecContext() ;
        QueryIterator qIter = QC.exec(opSQL,
                                      request,
                                      null, //BindingRoot.create(),
                                      execCxt) ;
        return qIter ;
    }

    @Override
    protected Op modifyPatternOp(Op op)
    {
        Op op2 =  queryCompiler.compile(op) ;
        return op2 ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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