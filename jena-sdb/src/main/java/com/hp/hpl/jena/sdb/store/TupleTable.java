/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sdb.store;

import java.sql.SQLException ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;

import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.sdb.Store ;
import com.hp.hpl.jena.sdb.core.SDBRequest ;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn ;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable ;
import com.hp.hpl.jena.sdb.sql.RS ;
import com.hp.hpl.jena.sdb.sql.ResultSetJDBC ;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.util.Context ;

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
        ResultSetJDBC tableData = null ;
        List<String> colVars = new ArrayList<String>() ;
        try
        {
            // Need to portable get the column names.
            tableData = store.getConnection().execQuery("SELECT * FROM " + tableName) ;
            java.sql.ResultSetMetaData meta = tableData.get().getMetaData() ;
            int N = meta.getColumnCount() ;
            for (int i = 1; i <= N; i++)
            {
                String colName = meta.getColumnName(i) ;
                colVars.add(colName) ;
            }
            return new TableDesc(tableName, colVars) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL(ex) ;
        } finally
        {
            RS.close(tableData) ;
        }
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
            String sqlStr = store.getSQLGenerator().generateSQL(request, b.getSqlNode()) ;
            //System.out.println(sqlStr) ;
            ResultSetJDBC tableData = store.getConnection().execQuery(sqlStr) ;
            ExecutionContext execCxt = new ExecutionContext(new Context(), null, null, null) ;
            return b.assembleResults(tableData, BindingRoot.create(), execCxt) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL(ex) ; }
    }
    
    // Dump, using SQL.
    public void dump()
    {
        QueryIterator qIter = iterator() ;
        ResultSetFormatter.out(ResultSetFactory.create(qIter, Var.varNames(vars))) ;

    }

}
