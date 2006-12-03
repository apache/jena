/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.alq;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.core.Element;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine2.QueryEngineQuad;
import com.hp.hpl.jena.query.engine2.op.Op;
import com.hp.hpl.jena.query.engine2.op.Transform;
import com.hp.hpl.jena.query.engine2.op.Transformer;
import com.hp.hpl.jena.query.util.Context;
import com.hp.hpl.jena.sdb.core.CompileContext;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.layout2.SQLBridge2;
import com.hp.hpl.jena.sdb.store.SQLBridge;
import com.hp.hpl.jena.sdb.store.Store;

/** Highly experimental quad engine */

public class QueryEngineQuadSDB extends QueryEngineQuad
{
    private static Log log = LogFactory.getLog(QueryEngineQuadSDB.class) ; 
    private Store store ;
    
    public QueryEngineQuadSDB(Store store, Query q)
    {
        super(q) ;
        this.store = store ;
    }
    
//    @Override
//    protected PlanElement makePlanForQueryPattern(Context context, Element queryPatternElement)
//    {
//        if ( queryPatternElement == null )
//            return null ;
//        
//        Op op = makeOpForQueryPattern(context, queryPatternElement) ;
//        // May be imcomplete translation    
//        return new PlanElementSDB(query, store, (OpSQL)op) ;
//    }
    
    @Override
    protected Op makeOpForQueryPattern(Context context, Element queryPatternElement)
    {
        if ( queryPatternElement == null )
            return null ;
        
        Op op = super.makeOpForQueryPattern(context, queryPatternElement) ;
        CompileContext c = new CompileContext(store, getQuery().getPrefixMapping()) ;
        Transform t = new TransformSDB(store, getQuery(), c) ;
        op = Transformer.transform(t, op) ;
        
        if ( ! ( op instanceof OpSQL ) )
            return op ;
        
        OpSQL opSQL = (OpSQL)op ;
        // Currently, all variables out of a BGP/QuadP are returned
        // nothing to do.
        // Currently, QueryIterSDB does the "right thing" 
        return opSQL ;
    }
    
    /** For debugging and inspectation.  Assumes whole query has been converted */ 
    public SqlNode getSqlNode()
    {
        Op op = makeOpForQueryPattern(getContext(), query.getQueryPattern()) ;
        OpSQL opSQL = (OpSQL)op ;
        List<Var> projectVars = QP.projectVars(getQuery()) ;
        SQLBridge bridge = new SQLBridge2() ;
        SqlNode sqlNode = QP.toSqlTopNode(opSQL.getSqlNode(), projectVars, bridge, store) ;
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