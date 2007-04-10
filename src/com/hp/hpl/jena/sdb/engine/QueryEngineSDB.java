/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sdb.compiler.QueryCompiler;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import com.hp.hpl.jena.sparql.algebra.AlgebraGeneratorQuad;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.engine.QueryEngineOpBase;
import com.hp.hpl.jena.sparql.util.Context;


public class QueryEngineSDB extends QueryEngineOpBase
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
        super(q, new AlgebraGeneratorQuad(context), context, new OpExecSDB()) ;
        this.store = store ;
        request = new SDBRequest(store, query) ;
        if ( StoreUtils.isHSQL(store) )
            request.LeftJoinTranslation = false ;
        
        queryCompiler = store.getQueryCompilerFactory().createQueryCompiler(request) ;
    }

    
    
    public SDBRequest getRequest()      { return request ; }

    @Override
    protected Op modifyPatternOp(Op op)
    {
        // After turning into the quadded form of the algebra, 
        // look for parts (or all of) we can turn into SQL.
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