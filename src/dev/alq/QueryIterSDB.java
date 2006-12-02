/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.alq;

import java.sql.SQLException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.core.Binding;
import com.hp.hpl.jena.query.core.Element;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.engine1.iterator.QueryIterRepeatApply;
import com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQL;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.layout2.SQLBridge2;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.SQLBridge;
import com.hp.hpl.jena.sdb.store.Store;

public class QueryIterSDB extends QueryIterRepeatApply
{
    private Store store ;
    private Query query ;
    
    public QueryIterSDB(Query query, Store store,
                        QueryIterator input, ExecutionContext execCxt)
    {
        super(input, execCxt) ;
        this.store = store ;
        this.query = query ;
    }
    
    @Override
    protected QueryIterator nextStage(Binding binding)
    {
        //TODO SUBSTITUTE binding
        //return store.getQueryCompiler().exec(store, block2, binding, super.getExecContext()) ;
        return exec(store, query.getQueryPattern(), binding, getExecContext()) ;
    }
    
    // TODO To QC
    public static QueryIterator exec(Store store, Element element, Binding binding, ExecutionContext execCxt)
    {
        SQLBridge bridge = new SQLBridge2() ;
        SqlNode sqlNode = QP.toSqlNode(element, execCxt.getQuery(), bridge, store, execCxt.getQuery().getPrefixMapping(), execCxt.getContext()) ;
        String sqlStmt = GenerateSQL.toSQL(sqlNode) ;
        
        // QueryCompilerMain for verbose control
        try {
            java.sql.ResultSet jdbcResultSet = store.getConnection().execQuery(sqlStmt) ;
            try {
                return bridge.assembleResults(jdbcResultSet, binding, execCxt) ;
            } finally { jdbcResultSet.close() ; }
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException in executing SQL statement", ex) ;
        }
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