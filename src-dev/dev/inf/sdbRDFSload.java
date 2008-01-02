/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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