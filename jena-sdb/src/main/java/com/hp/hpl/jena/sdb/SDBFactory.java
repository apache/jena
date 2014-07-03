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

package com.hp.hpl.jena.sdb;

import java.sql.Connection;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.graph.GraphSDB;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;
import com.hp.hpl.jena.sdb.store.DatasetStore;
import com.hp.hpl.jena.sdb.store.DatasetGraphSDB;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.update.GraphStore;

/** Various operations to create or connect objects to do with SDB:
 *  SDBConnections, Stores, Models, Graphs.
 *  Convenience calls.
 */

public class SDBFactory
{
    // ---- Connections
    /**
     * Create a connection to a database.
     * @param jdbcURL
     * @param user
     * @param password
     * @return SDBConnection
     */
    public static SDBConnection createConnection(String jdbcURL, String user, String password)
    { return SDBConnectionFactory.create(jdbcURL, user, password); }
    
    /**
     * Create a connection to a database from connection description. 
     * @param desc              SDB connection description
     * @return SDBConnection
     */
    public static SDBConnection createConnection(SDBConnectionDesc desc)
    { return SDBConnectionFactory.create(desc) ; }

    /**
     * Create a connection to a database from a JDBC connection. 
     * @param  conn              JDBC connection
     * @return SDBConnection
     */
    public static SDBConnection createConnection(java.sql.Connection conn)
    { return SDBConnectionFactory.create(conn) ; }

    /**
     * Create a connection to a database from connection description in a file.
     * @param configFile        Filename to be read and parsed (Jena assembler)
     * @return SDBConnection
     */
    public static SDBConnection createConnection(String configFile)
    { return SDBConnectionFactory.create(configFile) ; }

    /**
     * Create JDBC connection associated with the SDB connection description.
     * @param desc              SDB connection description
     * @return Connection
     */
    public static Connection createSqlConnection(SDBConnectionDesc desc)
    { return SDBConnectionFactory.createSqlConnection(desc) ; }

    /**
     * Create JDBC connection associated with the SDB connection description in a file.
     * @param configFile        Filename
     * @return Connection
     */
    public static Connection createSqlConnection(String configFile)
    { return SDBConnectionFactory.createJDBC(configFile) ; }
    
    /**
     * Create JDBC connection associated with the SDB connection description in a model.
     * @param model        Model
     * @return Connection
     */
    public static Connection createSqlConnection(Model model)
    { return SDBConnectionFactory.createJDBC(model) ; }
    
    /**
     * Connect to a store, based on store and connection descriptions in a file. 
     * @param configFile        Filename for assembler for Store and SDBConenction
     * @return Store
     */
    public static Store connectStore(String configFile) 
    { return StoreFactory.create(configFile) ; }
    
    /**
     * Connect to a store, based on a store description. 
     * @param desc              Store description object
     * @return Store
     */
    public static Store connectStore(StoreDesc desc) 
    { return StoreFactory.create(desc) ; }

    /**
     * Connect to a store, based on an existing SDB connection and a store description.
     * @param sdbConnection     SDBConnection object 
     * @param desc              Store description object
     * @return Store
     */
    public static Store connectStore(SDBConnection sdbConnection, StoreDesc desc) 
    { return StoreFactory.create(desc, sdbConnection) ; }
    
    /**
     * Connect to a store, based on an existing JDBC connection and a store description. 
     * @param sqlConnection     JDBC connection
     * @param desc              Store description object
     * @return  Store
     */
    public static Store connectStore(Connection sqlConnection, StoreDesc desc) 
    {
        SDBConnection sdb = SDBConnectionFactory.create(sqlConnection) ;
        return StoreFactory.create(desc, sdb) ;
    }

    // ---- Dataset

    /**
     * Connect to the RDF dataset in a store.
     * @param store
     * @return Dataset
     */
    public static Dataset connectDataset(Store store)
    { return DatasetStore.create(store) ; }

    /**
     * Connect to the RDF dataset in a store.
     * @param desc Store description
     * @return Dataset
     */
    public static Dataset connectDataset(StoreDesc desc)
    { return DatasetStore.create(connectStore(desc)) ; }

    /**
     * Connect to the RDF dataset in a store, using existing SDBConnection and a store description.
     * @param sdbConnection     SDB connection
     * @param desc              Store description object
     * @return Dataset
     */
    public static Dataset connectDataset(SDBConnection sdbConnection, StoreDesc desc)
    { return DatasetStore.create(connectStore(sdbConnection, desc)) ; }
    
    /**
     * Connect to the RDF dataset in a store, using existing JDBC connection and a store description.
     * @param jdbcConnection    JDBC connection
     * @param desc              Store description object
     * @return Dataset
     */
    public static Dataset connectDataset(Connection jdbcConnection, StoreDesc desc)
    { return DatasetStore.create(connectStore(jdbcConnection, desc)) ; }
    
    /**
     * Connect to the RDF dataset in a store, based on a Store assembler file.
     * @param configFile
     * @return Dataset
     */
    public static Dataset connectDataset(String configFile)
    { return DatasetStore.create(connectStore(configFile)) ; }
    
    // ---- GraphStore
    
    /**
     * Connect to the store as a GraphStore
     * @param store
     * @return GraphStore
     */
    public static GraphStore connectGraphStore(Store store)
    {
        return new DatasetGraphSDB(store, SDB.getContext().copy()) ; 
    }

    /**
     * Connect to the store as a GraphStore.
     * @param desc Store description
     * @return GraphStore
     */
    public static GraphStore connectGraphStore(StoreDesc desc)
    { return connectGraphStore(connectStore(desc)) ; }

    /**
     * Connect to the store as a GraphStore, using existing SDBConnection and a store description.
     * @param sdbConnection     SDB connection
     * @param desc              Store description object
     * @return GraphStore
     */
    public static GraphStore connectGraphStore(SDBConnection sdbConnection, StoreDesc desc)
    { return connectGraphStore(connectStore(sdbConnection, desc)) ; }
    
    /**
     * Connect to the store as a GraphStore, using existing JDBC connection and a store description.
     * @param jdbcConnection    JDBC connection
     * @param desc              Store description object
     * @return GraphStore
     */
    public static GraphStore connectGraphStore(Connection jdbcConnection, StoreDesc desc)
    { return connectGraphStore(connectStore(jdbcConnection, desc)) ; }
    
    /**
     * Connect to the store as a GraphStore, based on a Store assembler file.
     * @param configFile
     * @return GraphStore
     */
    public static GraphStore connectGraphStore(String configFile)
    { return connectGraphStore(connectStore(configFile)) ; }
    
    // ---- Graph
    
    /**
     * Connect to the default graph in a store, based on a Store assembler file.
     * @param configFile
     * @return Graph
     */
    public static Graph connectDefaultGraph(String configFile)
    { return connectDefaultGraph(StoreFactory.create(configFile)) ; }

    /**
     * Connect to the default graph in a store, based on a Store description.
     * @param desc              Store description object    
     * @return Graph
     */
    public static Graph connectDefaultGraph(StoreDesc desc)
    { return connectDefaultGraph(StoreFactory.create(desc)) ; }

    /**
     * Connect to the default graph in a store, based on a Store description.
     * @param store    The store containing the dataset for the default graph
     * @return Graph
     */
    public static Graph connectDefaultGraph(Store store)
    { return new GraphSDB(store) ; }

    /**
     * Connect to a named graph in a store, based on a store description file. 
     * @param configFile
     * @param iri
     * @return Graph
     */
    public static Graph connectNamedGraph(String configFile, String iri)
    { return connectNamedGraph(StoreFactory.create(configFile), iri) ; }

    /**
     * Connect to a named graph in a store, based on a store description. 
     * @param desc
     * @param iri
     * @return Graph
     */
    public static Graph connectNamedGraph(StoreDesc desc, String iri)
    { return connectNamedGraph(StoreFactory.create(desc), iri) ; }

    /**
     * Connect to a named graph in a store.
     * @param store
     * @param iri
     * @return Graph
     */
    public static Graph connectNamedGraph(Store store, String iri)
    { return new GraphSDB(store, iri) ; }

    /**
     * Connect to a named graph in a store, based on a store description file. 
     * @param configFile
     * @param node
     * @return Graph
     */
    public static Graph connectNamedGraph(String configFile, Node node)
    { return connectNamedGraph(StoreFactory.create(configFile), node) ; }
    
    /**
     * Connect to a named graph in a store, based on a store description. 
     * @param desc
     * @param node
     * @return Graph
     */
    public static Graph connectNamedGraph(StoreDesc desc, Node node)
    { return connectNamedGraph(StoreFactory.create(desc), node) ; }
    
    /**
     * Connect to a named graph in a store.
     * @param store
     * @param node
     * @return Graph
     */
    public static Graph connectNamedGraph(Store store, Node node)
    { return new GraphSDB(store, node) ; }
    
    
    // ---- Model
    
    /**
     * Connect to the default model in a store, using the store description in a file.
     * @param configFile        Filename
     * @return Model
     */
    public static Model connectDefaultModel(String configFile)
    { return connectDefaultModel(StoreFactory.create(configFile)) ; }

    /**
     * Connect to the default model in a store
     * @param desc
     * @return Model
     */
    public static Model connectDefaultModel(StoreDesc desc)
    { return connectDefaultModel(StoreFactory.create(desc)) ; }

    /**
     * Connect to the default model in a store
     * @param store
     * @return Model
     */
    public static Model connectDefaultModel(Store store)
    { return createModelSDB(store) ; }

    /**
     * Connect to the named model in a store
     * @param desc
     * @param iri
     * @return Model
     */
    public static Model connectNamedModel(StoreDesc desc, String iri)
    { return connectNamedModel(StoreFactory.create(desc), iri) ; }

    /**
     * Connect to the named model in a store
     * @param store
     * @param iri
     * @return Model
     */
    public static Model connectNamedModel(Store store, String iri)
    { return createModelSDB(store, iri) ; }

    /**
     * Connect to the named model in a store
     * @param configFile
     * @param iri
     * @return Model
     */
    public static Model connectNamedModel(String configFile, String iri)
    { return connectNamedModel(StoreFactory.create(configFile), iri) ; }

    /**
     * Connect to the named model in a store
     * @param desc
     * @param resource
     * @return Model
     */
    public static Model connectNamedModel(StoreDesc desc, Resource resource)
    { return connectNamedModel(StoreFactory.create(desc), resource) ; }

    /**
     * Connect to the named model in a store
     * @param store
     * @param resource
     * @return Model
     */
    public static Model connectNamedModel(Store store, Resource resource)
    { return createModelSDB(store, resource) ; }

    /**
     * Connect to the named model in a store
     * @param configFile
     * @param resource
     * @return Model
     */
    public static Model connectNamedModel(String configFile, Resource resource)
    { return connectNamedModel(StoreFactory.create(configFile), resource) ; }

    
    // ---- Workers
    
    private static Model createModelSDB(Store store)
    { return ModelFactory.createModelForGraph(new GraphSDB(store)) ; }
    
    private static Model createModelSDB(Store store, String iri)
    { return ModelFactory.createModelForGraph(new GraphSDB(store, iri)) ; }

    private static Model createModelSDB(Store store, Resource resource)
    { return ModelFactory.createModelForGraph(new GraphSDB(store, resource.asNode())) ; }
}
