/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.util;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.util.FileManager;

public class StoreUtils
{
    
    public static boolean isHSQL(Store store)
    { 
        return store.getDatabaseType().equals(DatabaseType.HSQLDB) ;
//        if ( store instanceof StoreBaseHSQL ) return true ;
//        if ( store instanceof StoreSimpleHSQL ) return true ;
//        return false ;
    }
    
    public static boolean isDerby(Store store)
    { 
        return store.getDatabaseType().equals(DatabaseType.Derby) ;
    }
    
    
    public static boolean isPostgreSQL(Store store)
    {
        return store.getDatabaseType().equals(DatabaseType.PostgreSQL) ;
//        if ( store instanceof StoreTriplesNodesIndexPGSQL ) return true ;
//        if ( store instanceof StoreTriplesNodesHashPGSQL ) return true ;
//        if ( store instanceof StoreSimplePGSQL ) return true ;
//        return false ;
    }

    public static boolean isMySQL(Store store)
    {
        return store.getDatabaseType().equals(DatabaseType.MySQL) ;
//        if ( store instanceof StoreTriplesNodesIndexMySQL ) return true ;
//        if ( store instanceof StoreTriplesNodesHashMySQL ) return true ;
//        if ( store instanceof StoreSimpleMySQL ) return true ;
//        return false ;
    }
    
    public static boolean isSQLServer(Store store)
    {
        return store.getDatabaseType().equals(DatabaseType.SQLServer) ;
    }
    
    public static boolean isOracle(Store store)
    {
        return store.getDatabaseType().equals(DatabaseType.Oracle) ;
    }
    

    public static void load(Store store, String filename)
    {
        Model model = SDBFactory.connectDefaultModel(store) ;
        FileManager.get().readModel(model, filename) ;
    }

    public static void load(Store store, String graphIRI, String filename)
    {
        Model model = SDBFactory.connectNamedModel(store, graphIRI) ;
        FileManager.get().readModel(model, filename) ;
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