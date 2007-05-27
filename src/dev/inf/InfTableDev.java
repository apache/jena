/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.inf;



import java.sql.SQLException;

import arq.cmd.CmdUtils;

import com.hp.hpl.jena.rdf.model.Model;

import com.hp.hpl.jena.query.*;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.sdb.store.DatasetStore;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreDesc;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.sdb.util.StrUtils;


public class InfTableDev
{
    static { CmdUtils.setLog4j() ; CmdUtils.setN3Params() ; }
    
    static final String prefixes = 
        StrUtils.strjoinNL(
            "PREFIX  :     <http://example/>",
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
            "") ;
    
    
    public static void main(String[]argv)
    {
        StoreDesc storeDesc = StoreDesc.read("sdb.ttl") ;
        Store store = StoreFactory.create(storeDesc) ;
        store.getConnection().setLogSQLExceptions(true) ;
        try {
            if ( true )
            {
                formatAndLoadStore(store) ;
                TransTableMgr X = null ;
                
                X = new TransTableMgr(store, new TransSubClassTable()) ; 
                X.buildLinks(true) ;
                X.write(store) ;

                X = new TransTableMgr(store, new TransSubPropertyTable()) ;
                X.buildLinks(true) ;
                X.write(store) ;
            }
            if ( true )
            {
                dumpTable(store, new TransSubClassTable()) ;
                dumpTable(store, new TransSubPropertyTable()) ;
            }
            // DO SOMETHING
        } catch (Exception ex) 
        { ex.printStackTrace(System.err) ; }
        finally { store.close() ; }
        // HSQL has daemon threads.
        System.exit(0) ;
    }
    



    static void divider()
    {
        System.out.println("-------------------------------------------------") ;
    }

    private static void dumpTable(Store store, TransTable table) throws SQLException
    {
        dumpTable(store, table.getTableName()) ;  
    }
    
    private static void dumpTable(Store store, String tableName) throws SQLException
    {
        String s = "SELECT * FROM "+tableName ;
        java.sql.ResultSet rs = store.getConnection().execQuery(s) ;
        RS.printResultSet(rs) ;
        RS.close(rs) ;
    }

    private static void formatAndLoadStore(Store store)
    {
        store.getTableFormatter().format() ;
        Model model = SDBFactory.connectModel(store) ;
        model.read("file:D.ttl", null, "N3") ;
        
        //execQuery(store, "SELECT * { ?ss ?pp ?oo}") ;
        
    }

    static void sql(Store store, String sql) throws SQLException
    {
        if ( true )
            store.getConnection().execUpdate(sql) ;
        else
            System.out.println(sql) ;
    }
    

    private static void execQuery(Store store, String queryString)
    {
        Query query = QueryFactory.create(queryString) ;
        if ( false )
        {
            divider() ; 
            query.serialize(System.out) ;
        }
        
        QueryExecution qExec = QueryExecutionFactory.create(query, DatasetStore.create(store)) ;
        try {
            ResultSetFormatter.out(qExec.execSelect(), query) ;
        } finally { qExec.close() ; }

    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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