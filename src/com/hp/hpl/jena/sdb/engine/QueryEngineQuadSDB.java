/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine2.QueryEngineQuad;
import com.hp.hpl.jena.query.engine2.op.Op;
import com.hp.hpl.jena.query.util.Context;

import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.compiler.OpSQL;
import com.hp.hpl.jena.sdb.core.compiler.QC;
import com.hp.hpl.jena.sdb.core.compiler.QueryCompiler;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.store.SQLBridge;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.util.StoreUtils;


/** Highly experimental quad engine */

public class QueryEngineQuadSDB extends QueryEngineQuad
{
    private static Log log = LogFactory.getLog(QueryEngineQuadSDB.class) ; 
    private Store store ;
    private SDBRequest request = null ;
    private QueryCompiler queryCompiler = null ;

    public QueryEngineQuadSDB(Store store, Query q)
    {
        this(store, q, null) ;
    }
    
    public QueryEngineQuadSDB(Store store, Query q, Context context)
    {
        super(q, context) ;
        this.store = store ;
        request = new SDBRequest(store, query) ;
        if ( StoreUtils.isHSQL(store) )
            request.LeftJoinTranslation = false ;
        
        queryCompiler = store.getQueryCompilerFactory().createQueryCompiler(request) ;
    }
    
    public SDBRequest getRequest()      { return request ; }     
    
//    @Override
//    protected PlanElement makePlanForQueryPattern(Context request, Element queryPatternElement)
//    {
//        if ( queryPatternElement == null )
//            return null ;
//        
//        Op op = makeOpForQueryPattern(request, queryPatternElement) ;
//        // May be incomplete translation    
//        return new PlanElementSDB(query, store, (OpSQL)op) ;
//    }
    
    
    @Override
    protected Op createPatternOp()
    {
        if ( query.getQueryPattern() == null )
            return null ;
        Op op = super.createPatternOp() ;
        op = queryCompiler.compile(op) ;
        
        if ( ! ( op instanceof OpSQL ) )
            return op ;
        
        // Other stuff?
        OpSQL opSQL = (OpSQL)op ;
        return opSQL ;
    }
    
//    @Override
//    protected Op createOp()

    
    /** For debugging and inspectation.  Assumes whole query has been converted */ 
    public SqlNode getSqlNode()
    {
        SDBRequest request = new SDBRequest(store, query) ;
        Op op = getPatternOp() ;
        OpSQL opSQL = (OpSQL)op ;
        List<Var> projectVars = QC.projectVars(getQuery()) ;
        SQLBridge bridge = store.getSQLBridgeFactory().create(request) ;
        SqlNode sqlNode = QC.toSqlTopNode(opSQL.getSqlNode(), projectVars, bridge) ;
        return sqlNode ;
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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