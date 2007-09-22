/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sparql.algebra.AlgebraGeneratorQuad;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpSubstitute;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.*;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorCheck;
import com.hp.hpl.jena.sparql.engine.main.OpCompiler;
import com.hp.hpl.jena.sparql.util.Context;

import com.hp.hpl.jena.query.Query;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.compiler.OpSQL;
import com.hp.hpl.jena.sdb.compiler.QueryCompiler;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.store.DatasetStoreGraph;
import com.hp.hpl.jena.sdb.util.StoreUtils;


public class QueryEngineSDB extends QueryEngineBase
{
    private static Log log = LogFactory.getLog(QueryEngineSDB.class) ; 
    private Store store ;
    private SDBRequest request = null ;
    private Op originalOp = null ;

    public QueryEngineSDB(Store store, Query q)
    {
        this(new DatasetStoreGraph(store), q, null) ;
    }
    
    public QueryEngineSDB(DatasetStoreGraph dsg, Query query, Context context)
    {
        super(query, dsg, new AlgebraGeneratorQuad(context), BindingRoot.create(), context) ;
        init(dsg, query, context) ;
    }

    public QueryEngineSDB(DatasetStoreGraph dsg, Op op, Context context)
    {
        super(op, dsg, BindingRoot.create(), context) ;
        init(dsg, null, context) ;
    }
    
    private void init(DatasetStoreGraph dsg, Query query, Context context)
    {
        this.store = dsg.getStore() ;
        this.request = new SDBRequest(store, query, context) ;
        this.originalOp = getOp() ;
        
        // Convenience - compile now even though it would get done on execution.    
        Op op = compile(store, originalOp, null, context, request) ;
        setOp(op) ;
    }
    
    // -----
    // Utilities.
    public static Op compile(Store store, Op op)
    {
        return compile(store, op, null) ;
    }
    
    public static Op compile(Store store, Op op, Context context)
    {
        if ( context == null )
            context = SDB.getContext() ;
        
        SDBRequest request = new SDBRequest(store, null, context) ;
        return compile(store, op, null, context, request) ;
    }
    
    // and the main compilation algorithm.
    // QueryCompilerMain does the bridge generation.
    private static Op compile(Store store, Op op, Binding binding, Context context, SDBRequest request)
    {
        if ( binding != null && ! binding.isEmpty() )
            op = OpSubstitute.substitute(op, binding) ;
        
        if ( StoreUtils.isHSQL(store) )
            request.LeftJoinTranslation = false ;
        if ( StoreUtils.isPostgreSQL(store) || StoreUtils.isMySQL(store) )
            request.LimitOffsetTranslation = true ;

        QueryCompiler queryCompiler = store.getQueryCompilerFactory().createQueryCompiler(request) ;
        Op op2 = queryCompiler.compile(op) ;
        return op2 ;
    }
    
    // -----    
    public SDBRequest getRequest()      { return request ; }

    @Override
    public QueryIterator eval(Op op, DatasetGraph dsg, Binding binding, Context context)
    {
        if ( ! binding.isEmpty() )
            // Assumes we compiled in the constructor.
            // Substitute and recompile.
            op = compile(store, op, binding, context, request) ;
        
        ExecutionContext execCxt = new ExecutionContext(context, dsg.getDefaultGraph(), dsg) ;
        
        // This pattern is common to QueryEngineMain - find a sharing pattern 
        if ( ! ( op instanceof OpSQL ) )
        {
            // Not top - invoke the main query engine as a framework to
            // put all the sub-opSQL parts together.
            QueryIterator input = new QueryIterSingleton(binding, execCxt) ;
            QueryIterator qIter = OpCompiler.compile(op, input, execCxt) ;  // OpCompiler from main query engine.
            qIter = QueryIteratorCheck.check(qIter, execCxt) ;
            return qIter ;
          }
          // Direct.
          OpSQL opSQL = (OpSQL)op ;
          QueryIterator qIter = opSQL.exec(binding, execCxt) ;
          qIter = QueryIteratorCheck.check(qIter, execCxt) ;
          return qIter ;
    }
    
    // -------- Factory
    
    static private QueryEngineFactory factory = new QueryEngineFactorySDB() ;
    static public QueryEngineFactory getFactory()   { return factory ; } 
    static public void register()       { QueryEngineRegistry.addFactory(factory) ; }
    static public void unregister()     { QueryEngineRegistry.removeFactory(factory) ; }
    
    
    private static class QueryEngineFactorySDB implements QueryEngineFactory
    {
        public boolean accept(Query query, DatasetGraph dataset, Context context)
        {
            if ( dataset instanceof DatasetStoreGraph )
                return true ;
            return false ;
        }

        public Plan create(Query query, DatasetGraph dataset, Binding inputBinding, Context context)
        {
            QueryEngineSDB qe = new QueryEngineSDB((DatasetStoreGraph)dataset , query, context) ;
            return qe.getPlan() ;
        }

        public boolean accept(Op op, DatasetGraph dataset, Context context)
        {
            if ( dataset instanceof DatasetStoreGraph )
                return true ;
            return false ;
        }

        public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context)
        {
            QueryEngineSDB qe = new QueryEngineSDB((DatasetStoreGraph)dataset , op, context) ;
            return qe.getPlan() ;
        }

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