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

import static com.hp.hpl.jena.sdb.util.StrUtils.sqlList;
import static org.apache.jena.atlas.lib.StrUtils.strjoin ;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlConstant;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;
import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.sdb.sql.ResultSetJDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;

public abstract class TupleLoaderOne extends TupleLoaderBase
{
    private static Logger log = LoggerFactory.getLogger(TupleLoaderOne.class);
    
    public TupleLoaderOne(SDBConnection connection)
    {
        super(connection) ;
    }
    
    public TupleLoaderOne(SDBConnection connection, TableDesc tableDesc)
    { super(connection, tableDesc) ; }
    
    @Override
    public void start()
    { super.start() ; }
    
    @Override
    public void finish()
    { super.finish(); }

    @Override
    public void load(Node... row)
    {
        if ( row.length != getTableWidth() )
        {
            String fmt = "TupleLoaderOne(%s) Expected row length: %d but got %d" ;
            String msg = String.format(fmt, getTableName(), getTableWidth(), row.length) ;
            throw new SDBInternalError(msg) ;
        }

        // Process nodes.
        String[] vals = prepareNodes(row) ;
        
        // Load if not present.
        if ( ! entryExists(vals) )
            loadRow(vals) ;
    }

    protected String[] prepareNodes(Node[] row)
    {
        String[] vals = new String[getTableWidth()] ;
        for ( int i = 0 ; i < getTableWidth() ; i++ )
        {
            vals[i] = ensureNode(row[i]).asSqlString() ;
        }
        return vals ;
    }

    protected boolean entryExists(String[] vals)
    {
        String rowValues = whereRow(vals) ;
        String selectTemplate = "SELECT count(*) FROM %s WHERE %s\n" ;
        String sqlStmt = String.format(selectTemplate, getTableName(), rowValues) ;
        
        ResultSetJDBC rs = null ; 
        try {
            rs = connection().execQuery(sqlStmt) ;
            rs.get().next() ;
            int count = rs.get().getInt(1) ;
    
            if ( count > 0 )
            {
                log.debug("Duplicate tuple detected: count="+count+" :: "+vals) ;
                return true; 
            }
                
            // Otherwise deos not exist
            return false ;
        }
        catch (SQLException ex)
        { throw new SDBExceptionSQL(ex) ; }
        finally { RS.close(rs) ; }
    }

    protected void loadRow(String[] vals)
    {
        /*
        INSERT INTO table
        (column-1, column-2, ... column-n)
        VALUES
        (value-1, value-2, ... value-n);
         */
        
//        String insertTemplate = "INSERT INTO %s\n  (%s)\nVALUES\n  (%s)" ;
//        String colNameList = sqlList(getColumnNames()) ;
//        String sqlStmt = String.format(insertTemplate, getTableName(), colNameList, sqlList(vals)) ;
//        exec(sqlStmt) ;
        
      String insertTemplate = "INSERT INTO %s VALUES\n  (%s)" ;
      
      String sqlStmt = String.format(insertTemplate, getTableName(), sqlList(vals)) ;
      exec(sqlStmt) ;
        
    }

    @Override
    public void unload(Node... row)
    {
        String[] vals = new String[getTableWidth()] ;
        for ( int i = 0 ; i < getTableWidth() ; i++ )
            vals[i] = refNode(row[i]).asSqlString() ;
        
        String rowValues = whereRow(vals) ;
        String deleteTemplate = "DELETE FROM %s WHERE %s" ;
        String sqlStmt = String.format(deleteTemplate, getTableName(), rowValues) ; 
        exec(sqlStmt) ;
    }
    
    protected void exec(String sqlStmt)
    {
        try
        { connection().exec(sqlStmt) ; } 
        catch (SQLException ex)
        { throw new SDBExceptionSQL(ex) ; }
    }
    
    private SqlConstant ensureNode(Node node)
    { 
        try {
            return insertNode(node) ;
        } catch (SQLException ex){
            throw new SDBExceptionSQL("PatternTableLoader.prepareNode", ex) ;
        }
    } 
    
    private SqlConstant refNode(Node node)
    { 
        try {
            return getRefForNode(node) ;
        } catch (SQLException ex){
            throw new SDBExceptionSQL("PatternTableLoader.getRefForNode", ex) ;
        }
    }
    
    private String whereRow(String[] vals)
    {
        List<String> rowValues = new ArrayList<String>(getTableWidth()) ;
        for ( int i = 0 ; i < getTableWidth() ; i++ )
        {
            String x = getTableDesc().getColNames().get(i)+"="+vals[i] ;
            rowValues.add(x) ; 
        }
        return strjoin(" AND ", rowValues) ;
    }
    
    abstract public SqlConstant getRefForNode(Node node) throws SQLException ;
    abstract public SqlConstant insertNode(Node node) throws SQLException ;
}
