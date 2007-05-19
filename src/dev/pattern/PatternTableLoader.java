/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.pattern;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.asSqlList;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.sse.SSE;

import com.hp.hpl.jena.sdb.compiler.PatternTable;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlConstant;
import com.hp.hpl.jena.sdb.layout2.LoaderOneTripleBase;
import com.hp.hpl.jena.sdb.layout2.hash.LoaderHashLJ;
import com.hp.hpl.jena.sdb.layout2.hash.LoaderOneTripleHash;
import com.hp.hpl.jena.sdb.layout2.index.LoaderIndexLJ;
import com.hp.hpl.jena.sdb.layout2.index.LoaderOneTripleIndex;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreFactory;

public class PatternTableLoader
{
    public static void main(String...argv)
    {
        Store store = StoreFactory.create("sdb.ttl") ;
        
        PatternTable pTable = new PatternTable("PAT") ;
        
        List<String> colNames = new ArrayList<String>() ;
        colNames.add("col1") ;
        colNames.add("col2") ;
        colNames.add("col3") ;
        
        PatternTableLoader loader = new PatternTableLoader(store, "PAT", colNames) ;

        List<Node> row = new ArrayList<Node>() ;
        row.add(Node.createLiteral("5")) ;
        row.add(SSE.parseNode("<http://example.org/>")) ;
        row.add(SSE.parseNode("<http://example.org/ns#>")) ;

        loader.prepareRow(row) ;
    }
    
    // Fake implemenetation.
    // Sometime : convert to a batchloader
    // Better - expose the bulk loader's node management
    // Better, better - a N-wide Node loader.
    
    private LoaderOneTripleBase nodeControl = null ;
    private String tableName ;
    private List<String> colNames ;
    private String colNamesStr ;
    private Store store ;
    
    public PatternTableLoader(Store store, String tableName, List<String> colNames)
    {
        this.store = store ;
        this.tableName = tableName ;
        this.colNames = colNames ;
        
        if ( store.getLoader() instanceof LoaderHashLJ )
            nodeControl = new LoaderOneTripleHash(store.getConnection()) ;
        if ( store.getLoader() instanceof LoaderIndexLJ )
            nodeControl = new LoaderOneTripleIndex(store.getConnection()) ;
        if ( nodeControl == null )
        {
            System.err.println("Can't make LoaderOneTriple") ;
            System.exit(1) ;
        }
        this.colNamesStr = asSqlList(colNames) ;
        //exec("DROP TABLE "+tableName) ;

        List<String> decls = new ArrayList<String>() ;
        for (String x : colNames )
            decls.add(x+" BIGINT") ;
        exec("CREATE TABLE "+tableName+" ("+asSqlList(decls)+" )") ;
    }
    
    private SqlConstant prepareNode(Node node)
    { 
        try {
            long ref = nodeControl.insertNode(node) ;
            return new SqlConstant(ref) ; 
        } catch (SQLException ex){
            throw new SDBExceptionSQL("PatternTableLoader.prepareNode", ex) ;
        }
    } 

    public void prepareRow(List<Node> row)
    {
        /*
        INSERT INTO table
        (column-1, column-2, ... column-n)
        VALUES
        (value-1, value-2, ... value-n);
         */

        String template = "INSERT INTO %s\n  (%s)\nVALUES\n  (%s)" ;
        
        int N = row.size() ;
        final String NL = "\n"; 
        List<String> vals = new ArrayList<String>(N) ;
        for ( Node node : row  )
        {
            SqlConstant val = prepareNode(node) ;
            vals.add(val.asSqlString()) ;
        }
        
        String sqlStmt = String.format(template, tableName, colNamesStr, asSqlList(vals)) ;
        System.out.println(sqlStmt) ;
        exec(sqlStmt) ;
    }

    private void exec(String sqlStmt)
    {
        try
        {
            store.getConnection().exec(sqlStmt) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL(ex) ;
        }
    }
    
    // Convert from Iterator to Iterable. 
    private static class Iter<T>  implements Iterable<T>
    {
        private Iterator<T> iterator ;
        Iter(Iterator<T> iterator) { this.iterator = iterator ; }
        public Iterator<T>  iterator() { return iterator ; }
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