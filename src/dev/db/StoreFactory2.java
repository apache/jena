/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.db;

import java.util.HashMap;

import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashDerby;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreDesc;
import com.hp.hpl.jena.sdb.util.Pair;

public class StoreFactory2
{
    private static Store _create(StoreDesc desc, SDBConnection sdb)
    {
        DatabaseType dbType = desc.getDbType() ;
        LayoutType t = desc.getLayout() ;
        LayoutType2 layoutType = new LayoutType2(t.name()) ;    // Temp 
        return _create(sdb, dbType, layoutType) ;
    }
    
    private static Store _create(SDBConnection sdb, DatabaseType dbType, LayoutType2 layoutType)
    {
        Pair<DatabaseType, LayoutType2> key = new Pair<DatabaseType, LayoutType2>(dbType, layoutType) ;
        Factory f = registry.get(key) ;
        if ( f == null )
        {}
        
        return f.create(sdb) ;
        
    }
    
    // OR
    // A single "type" which is DB+Layout
    // StoreType:  "Oracle::Layout2/hash"
    // Need to sort out that SDBConnection needs the type as well
    
    public static void register(DatabaseType dbType, LayoutType2 layoutType, Factory factory)
    {
        registry.put(
                     new Pair<DatabaseType, LayoutType2>(dbType, layoutType),
                     factory) ;
    }
    
    
    public static interface Factory
    {
        Store create(SDBConnection conn) ;
    }
    
    
    static Registry registry = new Registry() ;
    static class Registry extends HashMap<Pair<DatabaseType, LayoutType2>, Factory>
    {
        
    }
    
    static 
    {
        register(DatabaseType.Derby, LayoutType2.LayoutHash, 
            new Factory(){
                public Store create(SDBConnection conn)
                { return new StoreTriplesNodesHashDerby(conn) ; }} ) ;
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