/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.util.Context;

import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;

import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.iterator.Iter;
import com.hp.hpl.jena.sdb.sql.ResultSetJDBC;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;

public class TupleTable
{
    private TableDesc desc ;
    private Store store ;
    private List<Var> vars ;
    private SqlTable sqlTable ;

    // Column type?
    
    public TupleTable(Store store, String tableName)
    {
        this(store, getDesc(store, tableName)) ;
    }
    
    public TupleTable(Store store, TableDesc desc)
    { 
        this.store = store ;
        this.desc = desc ;
        sqlTable = new SqlTable(desc.getTableName(), desc.getTableName()) ;
        vars = new ArrayList<Var>() ;
        for (String colName : Iter.iter(desc.colNames()) )
        {
            Var var = Var.alloc(colName) ;
            vars.add(var) ;
            sqlTable.setIdColumnForVar(var, new SqlColumn(sqlTable, colName)) ;
        }
    }
    
    private static TableDesc getDesc(Store store, String tableName)
    {
        List<String> colVars = new ArrayList<String>() ;
        try {
            // Need to portable get the column names. 
            ResultSetJDBC tableData = 
                store.getConnection().execQuery("SELECT * FROM "+tableName) ;
            java.sql.ResultSetMetaData meta = tableData.get().getMetaData() ;
            int N = meta.getColumnCount() ;
            for ( int i = 1 ; i <= N ; i++ )
            {
                String colName = meta.getColumnName(i) ;
                colVars.add(colName) ;
            }
            tableData.close() ;
            return new TableDesc(tableName, colVars) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL(ex) ; }
    }
    
    //public void format
    //public void drop
    //public void create
    //public load
    
    public List<Var> getVars() 
    {
        return vars ;
    }
    
    public QueryIterator iterator()
    {
        SDBRequest request = new SDBRequest(store, null) ;
        String tableName = desc.getTableName() ;
        
        SQLBridge b = store.getSQLBridgeFactory().create(request, sqlTable, vars) ;
        b.build() ;

        try {
            String sqlStr = store.getSQLGenerator().generateSQL(b.getSqlNode()) ;
            //System.out.println(sqlStr) ;
            ResultSetJDBC tableData = store.getConnection().execQuery(sqlStr) ;
            ExecutionContext execCxt = new ExecutionContext(new Context(), null, null) ;
            return b.assembleResults(tableData, BindingRoot.create(), execCxt) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL(ex) ; }
    }
    
    // Dump, using SQL.
    public void dump()
    {
        QueryIterator qIter = iterator() ;
        ResultSetFormatter.out(ResultSetFactory.create(qIter, vars)) ;

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