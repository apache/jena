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

package dev.inf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.layout2.hash.TupleLoaderOneHash;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.sdb.store.TableDesc;
import com.hp.hpl.jena.sdb.store.TupleLoader;
import com.hp.hpl.jena.sdb.store.TupleTable;
import com.hp.hpl.jena.sdb.util.Pair;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.SSE;

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

        // ---- Config
        String tableName = "classes" ;

        // ---- Load auxillary table
        String subColName = "sub" ;
        String superColName = "super" ;
        TableDesc tableDesc = new TableDesc(tableName, subColName, superColName) ;
        TupleTable tuples = new TupleTable(store, tableDesc) ;
        
        // ---- Create auxillary table
        
        String colType = store.getNodeTableDesc().getNodeRefTypeString() ;
        try {
            String sqlCreateStr = 
                String.format("CREATE TABLE %s ( sub %s not null, super %s not null )",
                              tableName, colType, colType) ;
            store.getConnection().execUpdate(sqlCreateStr) ;
        } catch (SQLException ex) { throw new SDBExceptionSQL(ex) ; }
        
        loader.setTableDesc(tableDesc) ;
        
        loader.start() ;
        for (Pair<Node, Node> p : tg )
            loader.load(p.getLeft(), p.getRight()) ;
        loader.finish() ;
        
        // ---- Dump it.
        tuples.dump() ;
        store.close() ;
    }
    
    static public final String TAG_CLASS = "trans-class" ;
    static public final String TAG_PROP  = "trans-property" ; 
    
    static Store makeMemHash()
    {
        JDBC.loadDriverHSQL();

        SDBConnection sdb = SDBFactory.createConnection(
                "jdbc:hsqldb:mem:aname", "sa", "");
        Store store = StoreFactory.create(sdb, LayoutType.LayoutTripleNodesHash, DatabaseType.HSQLDB) ;
        store.getTableFormatter().format();
        return store ;
    }
}
