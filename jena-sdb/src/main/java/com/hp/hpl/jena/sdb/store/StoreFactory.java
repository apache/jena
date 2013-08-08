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

package com.hp.hpl.jena.sdb.store;

import static com.hp.hpl.jena.sdb.store.DatabaseType.* ;
import static com.hp.hpl.jena.sdb.store.LayoutType.* ;
import static java.lang.String.format ;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.layout1.*;
import com.hp.hpl.jena.sdb.layout2.hash.*;
import com.hp.hpl.jena.sdb.layout2.index.*;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;
import com.hp.hpl.jena.sdb.util.Pair;

/** Construct Stores */

public class StoreFactory
{
    private static Logger log = LoggerFactory.getLogger(StoreFactory.class) ;
    
    static { SDB.init() ; } 

    /**
     * Create a store, based on the store description and
     * connection information read from the file. 
     * @param filename
     * @return Store
     */
    public static Store create(String filename)
    { return create(StoreDesc.read(filename), null) ; }
    
    
    /**
     * Create a store, based on connection, layout type and database type.
     *
     * @param sdb
     * @param layout
     * @param dbType
     * @return Store
     */
    public static Store create(SDBConnection sdb, LayoutType layout, DatabaseType dbType)
    { 
        StoreDesc desc = new StoreDesc(layout, dbType) ;
        return create(desc, sdb) ;
    }

    /**
     * Create a store, based on the store description.
     * The store description must include connection details if to be used to actually connect.
     */
    public static Store create(LayoutType layout, DatabaseType dbType)
    { 
        StoreDesc desc = new StoreDesc(layout, dbType) ;
        return create(desc, null) ;
    }

    /**
     * Create a store, based on the store description.
     * The store description must include connection details if to be used to actually connect.
     */
    public static Store create(StoreDesc desc)
    { return create(desc, null) ; }
    
    /** 
     * Create a store, based on the store description and connection.
     */
    public static Store create(StoreDesc desc, SDBConnection sdb)
    {
        Store store = _create(sdb, desc) ;
        return store ;
    }
    
    private static Store _create(SDBConnection sdb, StoreDesc desc)
    {
        if ( sdb == null && desc.connDesc == null )
            desc.connDesc = SDBConnectionDesc.none() ;

        if ( sdb == null && desc.connDesc.getType() == null && desc.getDbType() != null )
            desc.connDesc.setType(desc.getDbType().getName()) ;
        
        if ( sdb == null && desc.connDesc != null)
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
        
        return f.create(sdb, desc) ;
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
                @Override
                public Store create(SDBConnection conn, StoreDesc desc)
                { return new StoreTriplesNodesHashDerby(conn, desc) ; } }) ;
        
        register(HSQLDB, LayoutTripleNodesHash, 
                 new StoreMaker(){
                     @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                     { return new StoreTriplesNodesHashHSQL(conn, desc) ; }} ) ;
        
        /* H2 contribution from Martin HEIN (m#)/March 2008 */
        register(H2, LayoutTripleNodesHash, 
                 new StoreMaker(){
                     @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                     { return new StoreTriplesNodesHashH2(conn, desc) ; }} ) ;
        
        register(MySQL, LayoutTripleNodesHash,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesHashMySQL(conn, desc, desc.engineType) ; } }) ;

        register(PostgreSQL, LayoutTripleNodesHash,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesHashPGSQL(conn, desc) ; } }) ;

        register(SQLServer, LayoutTripleNodesHash,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesHashSQLServer(conn, desc) ; } }) ;

        register(Oracle, LayoutTripleNodesHash,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesHashOracle(conn, desc) ; } }) ;

        register(DB2, LayoutTripleNodesHash,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesHashDB2(conn, desc) ; } }) ;

        /* SAP contribution from Fergal Monaghan (m#)/May 2012 */
        register(SAP, LayoutTripleNodesHash,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesHashSAP(conn, desc, desc.storageType) ; } }) ;

        // -- Index layout
        
        register(Derby, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesIndexDerby(conn, desc) ; }
                    }) ;
        
        register(HSQLDB, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesIndexHSQL(conn, desc) ; } }) ;
        
        register(H2, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesIndexH2(conn, desc) ; } }) ;
        
        register(MySQL, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesIndexMySQL(conn, desc, desc.engineType) ; } }) ;

        register(PostgreSQL, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesIndexPGSQL(conn, desc) ; } }) ;

        register(SQLServer, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesIndexSQLServer(conn, desc) ; } }) ;

        register(Oracle, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesIndexOracle(conn, desc) ; } }) ;
        
        register(DB2, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesIndexDB2(conn, desc) ; } }) ;

        register(SAP, LayoutTripleNodesIndex,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreTriplesNodesIndexSAP(conn, desc, desc.storageType) ; } }) ;

        
        // -- Simple layout
        
        register(Derby, LayoutSimple,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimpleDerby(conn, desc) ; }
                    }) ;
        
        register(HSQLDB, LayoutSimple,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimpleHSQL(conn, desc) ; } }) ;
        
        register(H2, LayoutSimple,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimpleH2(conn, desc) ; } }) ;
        
        register(MySQL, LayoutSimple,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimpleMySQL(conn, desc, desc.engineType) ; } }) ;

        register(PostgreSQL, LayoutSimple,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimplePGSQL(conn, desc) ; } }) ;

        register(SQLServer, LayoutSimple,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimpleSQLServer(conn, desc) ; } }) ;

        register(Oracle, LayoutSimple,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimpleOracle(conn, desc) ; } }) ;

        register(DB2, LayoutSimple,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimpleDB2(conn, desc) ; } }) ;

        register(SAP, LayoutSimple,
                 new StoreMaker() {
                    @Override
                    public Store create(SDBConnection conn, StoreDesc desc)
                    { return new StoreSimpleSAP(conn, desc, desc.storageType) ; } }) ;
    }
    
    static private void checkRegistry()
    {
        DatabaseType[] dbTypes = {Derby, HSQLDB, H2, MySQL, PostgreSQL, SQLServer, Oracle, SAP} ;
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
