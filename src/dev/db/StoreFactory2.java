/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.db;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.layout2.hash.* ;
import com.hp.hpl.jena.sdb.layout2.index.* ;
import com.hp.hpl.jena.sdb.layout1.* ;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreDesc;
import com.hp.hpl.jena.sdb.util.Pair;

public class StoreFactory2
{
    private static Store _create(StoreDesc desc, SDBConnection sdb)
    {
        // Temp translate.
        DatabaseType2 dbType = DatabaseType2.convert(desc.getDbType().name()) ;
        LayoutType t = desc.getLayout() ;
        LayoutType2 layoutType = LayoutType2.convert(t.name()) ;
        
        return _create(desc, sdb, dbType, layoutType) ;
    }
    
    private static Store _create(StoreDesc desc, SDBConnection sdb, DatabaseType2 dbType, LayoutType2 layoutType)
    {
        StoreMaker f = registry.get(dbType, layoutType) ;
        if ( f == null )
        {
            LogFactory.getLog(StoreFactory2.class).warn(String.format("No factory for (%s, %s)", dbType, layoutType)) ;
            return null ;
        }
        
        return f.create(desc, sdb) ;
    }
    
    // Need to sort out that SDBConnection needs the type as well
    
    public static void register(DatabaseType2 dbType, LayoutType2 layoutType, StoreMaker factory)
    {
        registry.put(dbType, layoutType, factory) ;
    }
    
    
    public static interface StoreMaker
    {
        Store create(StoreDesc desc, SDBConnection conn) ;
    }
    
    
    static Registry registry = new Registry() ;
    static class Registry extends MapK2<DatabaseType2, LayoutType2, StoreMaker>
    {}

    static { setRegistry() ; }
    
    static private void setRegistry()
    {
        // registry.clear() ;
        // -- Hash layout
        
        register(DatabaseType2.Derby, LayoutType2.LayoutHash, 
            new StoreMaker(){
                public Store create(StoreDesc desc, SDBConnection conn)
                { return new StoreTriplesNodesHashDerby(conn) ; } }) ;
        
        register(DatabaseType2.HSQLDB, LayoutType2.LayoutHash, 
                 new StoreMaker(){
                     public Store create(StoreDesc desc, SDBConnection conn)
                     { return new StoreTriplesNodesHashHSQL(conn) ; }} ) ;
        
        register(DatabaseType2.MySQL, LayoutType2.LayoutTripleNodesHash,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesHashMySQL(conn, desc.engineType) ; } }) ;

        register(DatabaseType2.PostgreSQL, LayoutType2.LayoutTripleNodesHash,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesHashPGSQL(conn) ; } }) ;

        register(DatabaseType2.SQLServer, LayoutType2.LayoutTripleNodesHash,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesHashSQLServer(conn) ; } }) ;

        register(DatabaseType2.Oracle, LayoutType2.LayoutTripleNodesHash,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesHashOracle(conn) ; } }) ;

        // -- Index layout
        
        register(DatabaseType2.Derby, LayoutType2.LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesIndexDerby(conn) ; }
                    }) ;
        
        register(DatabaseType2.HSQLDB, LayoutType2.LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesIndexHSQL(conn) ; } }) ;
        
        register(DatabaseType2.MySQL, LayoutType2.LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesIndexMySQL(conn, desc.engineType) ; } }) ;

        register(DatabaseType2.PostgreSQL, LayoutType2.LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesIndexPGSQL(conn) ; } }) ;

        register(DatabaseType2.SQLServer, LayoutType2.LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesIndexSQLServer(conn) ; } }) ;

        register(DatabaseType2.Oracle, LayoutType2.LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesIndexOracle(conn) ; } }) ;
        
        // -- Simple layout
        
        register(DatabaseType2.Derby, LayoutType2.LayoutSimple,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreSimpleDerby(conn) ; }
                    }) ;
        
        register(DatabaseType2.HSQLDB, LayoutType2.LayoutSimple,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreSimpleHSQL(conn) ; } }) ;
        
        register(DatabaseType2.MySQL, LayoutType2.LayoutSimple,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreSimpleMySQL(conn, desc.engineType) ; } }) ;

        register(DatabaseType2.PostgreSQL, LayoutType2.LayoutSimple,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreSimplePGSQL(conn) ; } }) ;

        register(DatabaseType2.SQLServer, LayoutType2.LayoutSimple,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreSimpleSQLServer(conn) ; } }) ;

        register(DatabaseType2.Oracle, LayoutType2.LayoutSimple,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreSimpleOracle(conn) ; } }) ;
    }
    
    // Convenience.
    static class MapK2<K1, K2, V>
    {
        private Map <Pair<K1, K2>, V> map = null ;
        
        public MapK2() { map = new HashMap<Pair<K1, K2>, V>() ; }
        public MapK2(Map <Pair<K1, K2>, V> map) { this.map = map ; }
        
        public V get(K1 key1, K2 key2) { return map.get(new Pair<K1, K2>(key1, key2)) ; }
        public void put(K1 key1, K2 key2, V value) { map.put(new Pair<K1, K2>(key1, key2), value) ; }
        public boolean containsKey(K1 key1, K2 key2) { return map.containsKey(new Pair<K1, K2>(key1, key2)) ; }
        public boolean containsValue(V value) { return map.containsValue(value) ; }
        public int size() { return map.size() ; }
        public boolean isEmpty() { return map.isEmpty() ; }
        public void clear() { map.clear() ; }
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