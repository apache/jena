/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.pattern;

import static com.hp.hpl.jena.sdb.util.StrUtils.sqlList;
import static com.hp.hpl.jena.sdb.util.StrUtils.strjoin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlConstant;
import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.Store;

public abstract class TupleLoaderOne extends TupleLoaderBase
{
    private static Log log = LogFactory.getLog(TupleLoaderOne.class);
    
    private String tableName ;
    private int tableWidth ;
    private List<String> colNames ;
    private String colNamesStr ;
    protected Store store ;

    
    public TupleLoaderOne(Store store, String tableName, List<String> colNames)
    {
        super(tableName) ;
        this.store = store ;
        this.tableName = tableName ;
        this.colNames = colNames ;
        this.colNamesStr = sqlList(colNames) ;
        this.tableWidth = colNames.size() ;
    }
    
    @Override
    public void start()
    { super.start() ; }
    
    @Override
    public void finish()
    { super.finish(); }

    public void load(List<Node> row)
    {
        if ( row.size() != tableWidth )
        {
            String fmt = "PatternTableLoader(%s) Expected row length: %d but got %d" ;
            String msg = String.format(fmt, tableName, tableWidth, row.size()) ;
            throw new SDBException(msg) ;
        }

        // Process nodes.
        List<String> vals = prepareNodes(row) ;
        
        // Load if not present.
        if ( ! entryExists(vals) )
            loadRow(vals) ;
    }

    private void loadRow(List<String> vals)
    {
        /*
        INSERT INTO table
        (column-1, column-2, ... column-n)
        VALUES
        (value-1, value-2, ... value-n);
         */
        String insertTemplate = "INSERT INTO %s\n  (%s)\nVALUES\n  (%s)" ;
        String sqlStmt = String.format(insertTemplate, tableName, colNamesStr, sqlList(vals)) ;
        exec(sqlStmt) ;
    }

    private void exec(String sqlStmt)
    {
        try
        { store.getConnection().exec(sqlStmt) ; } 
        catch (SQLException ex)
        { throw new SDBExceptionSQL(ex) ; }
    }
    
    private List<String> prepareNodes(List<Node> row)
    {
        List<String> vals = new ArrayList<String>(tableWidth) ;
        for ( Node node : row  )
        {
            SqlConstant val = prepareNode(node) ;
            vals.add(val.asSqlString()) ;
        }
        return vals ;
    }

    private SqlConstant prepareNode(Node node)
    { 
        try {
            long ref = insertNode(node) ;
            return new SqlConstant(ref) ; 
        } catch (SQLException ex){
            throw new SDBExceptionSQL("PatternTableLoader.prepareNode", ex) ;
        }
    } 
    
    private boolean entryExists(List<String> vals)
    {
        List<String> rowValues = new ArrayList<String>(tableWidth) ;
        for ( int i = 0 ; i < tableWidth ; i++ )
        {
            String x = colNames.get(i)+"="+vals.get(i) ;
            rowValues.add(x) ; 
        }
        String selectTemplate = "SELECT count(*) FROM %s WHERE %s\n" ;
        String sqlStmt = String.format(selectTemplate,
                                       getTableName(),
                                       strjoin(" AND ", rowValues)) ;
        
        try {
            ResultSet rs = store.getConnection().execQuery(sqlStmt) ;
            rs.next() ;
            int count = rs.getInt(1) ;
            RS.close(rs) ;

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
    }
    
    abstract public long getRefForNode(Node node) throws SQLException ;
    abstract public long insertNode(Node node) throws SQLException ;
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