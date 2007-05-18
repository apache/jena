/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;

import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sdb.compiler.PatternTable;
import com.hp.hpl.jena.sdb.compiler.QueryCompiler;
import com.hp.hpl.jena.sdb.compiler.QueryCompilerMain;
import com.hp.hpl.jena.sdb.compiler.SlotCompiler;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlConstant;
import static com.hp.hpl.jena.sdb.sql.SQLUtils.asSqlList; 
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.sparql.sse.SSE;

public class PatternTableLoader
{
    // Need more complexity to allow a bunch of look ups in the node table.
    
    public static void main(String...argv)
    {
        SSE.parseNode("<http://example.org/>") ;
        
        Store store = StoreFactory.create("sdb.ttl") ;
        
        PatternTable pTable = new PatternTable("PAT") ;
        Map<String,Node> row = new HashMap<String,Node>() ;
        row.put("col1", Node.createLiteral("5")) ;
        row.put("col2", SSE.parseNode("<http://example.org/>")) ;
        row.put("col3", SSE.parseNode("<http://example.org/ns#>")) ;
        String sql = load(store, pTable, row) ;
        System.out.println(sql) ;
    }
    
    public static String load(Store store, PatternTable pTable, Map<String,Node> row)
    {
        // Unnecessary creation each time.
        // But let's get something working first!
        Query query = QueryFactory.make() ;
        SDBRequest request = new SDBRequest(store, query) ;
        
        QueryCompiler qc = store.getQueryCompilerFactory().createQueryCompiler(request) ;
        SlotCompiler slotCompiler = ((QueryCompilerMain)qc).createQuadBlockCompiler().getSlotCompiler() ;
        return load(slotCompiler, pTable, row) ;
    }
    
    public static String load(SlotCompiler slotCompiler, PatternTable pTable, Map<String,Node> row)
    {
        StringBuilder buff = new StringBuilder() ;
        buff.append("INSERT INTO ") ;
        buff.append(pTable.getTableName()) ;

        int N = row.size() ;
        List<String> cols = new ArrayList<String>(N) ;
        List<String> vals = new ArrayList<String>(N) ;
        /*
        INSERT INTO table
        (column-1, column-2, ... column-n)
        VALUES
        (value-1, value-2, ... value-n);
         */

        boolean first= true ;
        for ( String colName : row.keySet() )
        {
            Node n = row.get(colName) ;
            cols.add(colName) ;
            SqlConstant val = slotCompiler.tableRef(n) ;
            vals.add(val.asSqlString()) ;
        }
        
        
        buff.append(" (").append(asSqlList(cols)).append(")") ;
        buff.append(" VALUES ") ;
        buff.append("(").append(asSqlList(vals)).append(")") ;
        return buff.toString() ;
    }
    
    public void start() {}
    
    // Column order
    //public void add(List<Node> nodes) {}
    public void add(Node subject, Node object)
    {
        long subj = slot(subject) ;
        long obj = slot(object) ;
    }
    
    public void finish() {}

    private long slot(Node node)
    {
        return 0L ;
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