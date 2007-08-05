/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import static com.hp.hpl.jena.sdb.store.DatabaseType.* ;
import static com.hp.hpl.jena.sdb.store.LayoutType.* ;
import static java.lang.String.format ;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.layout1.*;
import com.hp.hpl.jena.sdb.layout2.hash.*;
import com.hp.hpl.jena.sdb.layout2.index.*;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;
import com.hp.hpl.jena.sdb.util.Pair;

/** Construct Stores
 * @author Andy Seaborne
 * @version $Id: StoreFactory.java,v 1.5 2006/04/22 19:51:12 andy_seaborne Exp $
 */

public class StoreFactory
{
    private static Log log = LogFactory.getLog(StoreFactory.class) ;
    
    static { SDB.init() ; } 

    /**
     * Create a store, based on the store description and
     * connection information read from the file. 
     * @param filename
     * @return
     */
    public static Store create(String filename)
    { return create(null, StoreDesc.read(filename)) ; }
    
    /**
     * Create a store, based on the store description.
     * The store description must include connection details. 
     * @param desc
     * @return
     */
    public static Store create(StoreDesc desc)
    { return create(null, desc) ; }
    
    /** 
     * Create a store, based on the store description and connection.
     * @param sdb
     * @param desc
     * @return
     */
    public static Store create(SDBConnection sdb, StoreDesc desc)
    {
        Store store = _create(sdb, desc) ;
        return store ;
    }
    
    private static Store _create(SDBConnection sdb, StoreDesc desc)
    {
        if ( sdb == null && desc.connDesc == null )
            throw new SDBException("StoreFactory: No connection and no connection description") ;
        
        if ( sdb == null )
            sdb = SDBConnectionFactory.create(desc.connDesc) ;
        
        DatabaseType dbType = desc.getDbType() ;
        LayoutType layoutType = desc.getLayout() ;
        
        return _create(desc, sdb, dbType, layoutType) ;
    }
    
    private static Store _create(StoreDesc desc, SDBConnection sdb, DatabaseType dbType, LayoutType layoutType)
    {
        StoreMaker f = registry.get(dbType, layoutType) ;
        if ( f == null )
        {
            log.warn(format("No factory for (%s, %s)", dbType.getName(), layoutType.getName())) ;
            return null ;
        }
        
        return f.create(desc, sdb) ;
    }
    
    // Need to sort out that SDBConnection needs the type as well
    
    /** Register a new store maker for a given database/layout pair.
     *  Overwrites any previous StoreMaker for this pair.
     *  Only used when adding a new database or layout - the standard
     *  ones are automatically registered.  
     */ 
    public static void register(DatabaseType dbType, LayoutType layoutType, StoreMaker factory)
    {
        registry.put(dbType, layoutType, factory) ;
    }
    
    private static class Registry extends MapK2<DatabaseType, LayoutType, StoreMaker> {}
    private static Registry registry = new Registry() ;

    static { setRegistry() ; checkRegistry() ; }
    
    static private void setRegistry()
    {
        // registry.clear() ;
        // -- Hash layout
        
        register(Derby, LayoutTripleNodesHash, 
            new StoreMaker(){
                public Store create(StoreDesc desc, SDBConnection conn)
                { return new StoreTriplesNodesHashDerby(conn) ; } }) ;
        
        register(HSQLDB, LayoutTripleNodesHash, 
                 new StoreMaker(){
                     public Store create(StoreDesc desc, SDBConnection conn)
                     { return new StoreTriplesNodesHashHSQL(conn) ; }} ) ;
        
        register(MySQL, LayoutTripleNodesHash,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesHashMySQL(conn, desc.engineType) ; } }) ;

        register(PostgreSQL, LayoutTripleNodesHash,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesHashPGSQL(conn) ; } }) ;

        register(SQLServer, LayoutTripleNodesHash,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesHashSQLServer(conn) ; } }) ;

        register(Oracle, LayoutTripleNodesHash,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesHashOracle(conn) ; } }) ;

        // -- Index layout
        
        register(Derby, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesIndexDerby(conn) ; }
                    }) ;
        
        register(HSQLDB, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesIndexHSQL(conn) ; } }) ;
        
        register(MySQL, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesIndexMySQL(conn, desc.engineType) ; } }) ;

        register(PostgreSQL, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesIndexPGSQL(conn) ; } }) ;

        register(SQLServer, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesIndexSQLServer(conn) ; } }) ;

        register(Oracle, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreTriplesNodesIndexOracle(conn) ; } }) ;
        
        // -- Simple layout
        
        register(Derby, LayoutSimple,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreSimpleDerby(conn) ; }
                    }) ;
        
        register(HSQLDB, LayoutSimple,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreSimpleHSQL(conn) ; } }) ;
        
        register(MySQL, LayoutSimple,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreSimpleMySQL(conn, desc.engineType) ; } }) ;

        register(PostgreSQL, LayoutSimple,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreSimplePGSQL(conn) ; } }) ;

        register(SQLServer, LayoutSimple,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreSimpleSQLServer(conn) ; } }) ;

        register(Oracle, LayoutSimple,
                 new StoreMaker() {
                    public Store create(StoreDesc desc, SDBConnection conn)
                    { return new StoreSimpleOracle(conn) ; } }) ;
    }
    
    static private void checkRegistry()
    {
        DatabaseType[] dbTypes = {Derby, HSQLDB, MySQL, PostgreSQL, SQLServer, Oracle} ;
        LayoutType[] layoutTypes = {LayoutTripleNodesHash, LayoutTripleNodesIndex, LayoutSimple} ;
        
        Set <StoreMaker> seen = new HashSet<StoreMaker>() ;
        
        for ( DatabaseType k1 : dbTypes )
            for ( LayoutType k2 : layoutTypes )
            {
                if ( ! registry.containsKey(k1, k2) )
                    log.warn(format("Missing store maker: (%s, %s)", k1.getName(), k2.getName())) ;
                StoreMaker x = registry.get(k1, k2) ;
                if ( seen.contains(x) )
                    log.warn(format("Duplicate store maker: (%s, %s)", k1.getName(), k2.getName())) ;
                seen.add(x) ;
            }
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
