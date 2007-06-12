/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.inf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashHSQL;
import com.hp.hpl.jena.sdb.layout2.hash.TupleLoaderOneHash;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.SQLBridge;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.TableDesc;
import com.hp.hpl.jena.sdb.store.TupleLoader;
import com.hp.hpl.jena.sdb.util.Iter;
import com.hp.hpl.jena.sdb.util.Pair;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.Context;

public class sdbRDFSload
{
    public static void main(String... args) throws FileNotFoundException
    {
        // Read data.
        InputStream in = System.in ;
        in = new FileInputStream("T.sse") ;
        Item x = SSE.parse(in) ;
        TransGraphNode tg = TransGraphNode.build(x) ;

        Store store = makeMemHash() ;
        TupleLoader loader = new TupleLoaderOneHash(store.getConnection()) ;

        // ---- Create auxillary table
        String tableName = "classes" ;
        try {
            String sqlCreateStr = "CREATE TABLE "+tableName+"( sub bigint not null, super bigint not null )" ;
            store.getConnection().execUpdate(sqlCreateStr) ;
        } catch (SQLException ex) { throw new SDBExceptionSQL(ex) ; }
        
        // ---- Load auxillary table
        String subColName = "sub" ;
        String superColName = "super" ;
        TableDesc tableDesc = new TableDesc(tableName, subColName, superColName) ;
        loader.setTableDesc(tableDesc) ;
        
        loader.start() ;
        for (Pair<Node, Node> p : tg )
            loader.load(new Node[]{p.getLeft(), p.getRight()}) ;
        loader.finish() ;
        
        // ---- Dump it.
        dumpTupleTable(store, tableDesc) ;
//        
//        SDBRequest request = new SDBRequest(store, null) ;
//
////        TableUtils.dump(store.getConnection(), tableName) ;
////        TableUtils.dump(store.getConnection(), nodeTable) ;
//        
//        // -- Dump as Nodes.
//        Var var1 = Var.alloc("var1") ;
//        Var var2 = Var.alloc("var2") ;
//        
//        SqlTable sqlTable = new SqlTable(tableName, "X") ;
//        sqlTable.setIdColumnForVar(var1, new SqlColumn(sqlTable, subColName)) ;
//        sqlTable.setIdColumnForVar(var2, new SqlColumn(sqlTable, superColName)) ;
//
//        List<Var> vars = new ArrayList<Var>() ;
//        vars.add(var1) ;
//        vars.add(var2) ;
//
//        
//        SQLBridge b = store.getSQLBridgeFactory().create(request, sqlTable, vars) ;
//        b.build() ;
//        
//        QueryIterator qIter = null ;
//        try {
//            String sqlStr = store.getSQLGenerator().generateSQL(b.getSqlNode()) ;
//            //System.out.println(sqlStr) ;
//            ResultSet tableData = store.getConnection().execQuery(sqlStr) ;
//            ExecutionContext execCxt = new ExecutionContext(new Context(), null, null) ;
//            qIter = b.assembleResults(tableData, BindingRoot.create(), execCxt) ;
//        } catch (SQLException ex)
//        { throw new SDBExceptionSQL(ex) ; }
//        
//        ResultSetFormatter.out(ResultSetFactory.create(qIter, vars)) ;
        store.close() ;
    }
    
    static public final String TAG_CLASS = "trans-class" ;
    static public final String TAG_PROP  = "trans-property" ; 
    
    static Store makeMemHash()
    {
        JDBC.loadDriverHSQL();

        SDBConnection sdb = SDBFactory.createConnection(
                "jdbc:hsqldb:mem:aname", "sa", "");

        Store store = new StoreTriplesNodesHashHSQL(sdb);
        store.getTableFormatter().format();
        return store ;
    }
    
    // Move to TupleTable.
    static void dumpTupleTable(Store store, TableDesc tableDesc)
    {
        SDBRequest request = new SDBRequest(store, null) ;
        String tableName = tableDesc.getTableName() ;

//      TableUtils.dump(store.getConnection(), tableName) ;
//      TableUtils.dump(store.getConnection(), nodeTable) ;

        SqlTable sqlTable = new SqlTable(tableName, tableName) ;
        List<Var> vars = new ArrayList<Var>() ;
        for (String colName : Iter.wrap(tableDesc.colNames()) )
        {
            Var var = Var.alloc(colName) ;
            vars.add(var) ;
            sqlTable.setIdColumnForVar(var, new SqlColumn(sqlTable, colName)) ;
        }

        SQLBridge b = store.getSQLBridgeFactory().create(request, sqlTable, vars) ;
        b.build() ;

        QueryIterator qIter = null ;
        try {
            String sqlStr = store.getSQLGenerator().generateSQL(b.getSqlNode()) ;
            //System.out.println(sqlStr) ;
            ResultSet tableData = store.getConnection().execQuery(sqlStr) ;
            ExecutionContext execCxt = new ExecutionContext(new Context(), null, null) ;
            qIter = b.assembleResults(tableData, BindingRoot.create(), execCxt) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL(ex) ; }

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