/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test;

import static com.hp.hpl.jena.sdb.test.SDBTest.storeDescBase;
import static com.hp.hpl.jena.sdb.util.Alg.append;
import static com.hp.hpl.jena.sdb.util.Alg.toList;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.sdb.util.Pair;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import com.hp.hpl.jena.sdb.util.Vocab;

public class StoreList
{
    static Property description = Vocab.property(SDB.namespace, "description") ;
    static Property list = Vocab.property(SDB.namespace, "list") ;
    static Resource storeListClass = Vocab.property(SDB.namespace, "StoreList") ;
    
    
    // The list of stores to test.
    // TODO Read from an RDF file.
    static boolean includeHash      = true ;
    static boolean includeIndex     = true ;
    
    static boolean formatStores     = false ;
    
    static boolean includeDerby     = true ;
    static boolean includePGSQL     = true ;
    static boolean includeHSQL      = true ;
    static boolean includeSQLServer = true ;
    static boolean includeMySQL     = true ;
    
    public static List<Pair<Store, String>> stores()
    {
        return toList(append(stores1(), stores2())) ;
    }
    
    public static List<Pair<Store, String>> stores1()
    {
        return null ;
    }
    
    public static List<Pair<Store, String>> stores2() 
    {
        //  [ :assembler <file:sdb.ttl> ; :rdfs:label "foobar" ] ;
        // SELECT * {
        //  ?x rdf:type %s ; :list [ :listMember [ :description ?desc ; :rdfs:label ?label ] ] }
        
        List<Pair<Store, String>> stores = new ArrayList<Pair<Store, String>>() ;

        if ( includeDerby )
        {
            if ( includeHash )  worker(stores, "Derby/Hash",  storeDescBase+"derby-hash.ttl") ;
            if ( includeIndex ) worker(stores, "Derby/Index", storeDescBase+"derby-index.ttl") ;
        }
        
        if ( includeMySQL )
        {
            if ( includeHash )  worker(stores, "MySQL/Hash",  storeDescBase+"mysql-hash.ttl") ;
            if ( includeIndex ) worker(stores, "MySQL/Index", storeDescBase+"mysql-index.ttl") ;
        }

        if ( includePGSQL )
        {
            if ( includeHash )  worker(stores, "PGSQL/Hash",  storeDescBase+"pgsql-hash.ttl") ;
            if ( includeIndex ) worker(stores, "PGSQL/Index", storeDescBase+"pgsql-index.ttl") ;
        }

        if ( includeSQLServer )
        {
            if ( includeHash )  worker(stores, "MS-SQL-e/Hash",  storeDescBase+"ms-sql-e-hash.ttl") ;
            if ( includeIndex ) worker(stores, "MS-SQL-e/Index", storeDescBase+"ms-sql-e-index.ttl") ;
        }
        
        if ( includeHSQL )
        {
            if ( includeHash )  worker(stores, "HSQLDB/Hash",  storeDescBase+"hsqldb-hash.ttl") ;
            if ( includeIndex ) worker(stores, "HSQLDB/Index", storeDescBase+"hsqldb-index.ttl") ;
        }
        
        return stores ;
    }

    private static void worker(List<Pair<Store, String>> data, String label, String storeDescFile)
    {
        Store store = StoreFactory.create(storeDescFile) ;
        if ( formatStores || StoreUtils.isHSQL(store) )
            // HSQL (in memory) needs formatting always.
            store.getTableFormatter().create() ;
        Pair<Store, String> e = new Pair<Store, String>(store, label) ;
        data.add(e) ;
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