/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb;

import java.sql.Connection;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import com.hp.hpl.jena.query.Dataset;

import com.hp.hpl.jena.sdb.graph.GraphSDB;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;
import com.hp.hpl.jena.sdb.store.DatasetStore;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreDesc;
import com.hp.hpl.jena.sdb.store.StoreFactory;

/** Various operations to create or connect objects to do with SDB:
 *  SDBConnections, Stores, Models, Graphs.
 *  Convenience calls.
 *  
 * @author Andy Seaborne
 * @version $Id$
 */

public class SDBFactory
{
    // ---- Connections
    
    public static SDBConnection createConnection(String jdbcURL, String user, String password)
    { return new SDBConnection(jdbcURL, user, password) ; }
    
    public static SDBConnection createConnection(SDBConnectionDesc desc)
    { return SDBConnectionFactory.create(desc) ; }

    public static SDBConnection createConnection(String configFile)
    { return SDBConnectionFactory.create(configFile) ; }

    public static Connection createSqlConnection(SDBConnectionDesc desc)
    { return SDBConnectionFactory.createJDBC(desc) ; }

    public static Connection createSqlConnection(String configFile)
    { return SDBConnectionFactory.createJDBC(configFile) ; }
    
    public static Store connectStore(String configFile) 
    { return StoreFactory.create(configFile) ; }
    
    public static Store connectStore(StoreDesc desc) 
    { return StoreFactory.create(desc) ; }

    public static Store connectStore(SDBConnection sdbConnection, StoreDesc desc) 
    { return StoreFactory.create(sdbConnection, desc) ; }
    
    public static Store connectStore(Connection sqlConnection, StoreDesc desc) 
    {
        SDBConnection sdb = SDBConnectionFactory.create(sqlConnection) ;
        return StoreFactory.create(sdb, desc) ;
    }

    // ---- Dataset
    
    public static Dataset connectDataset(Store store)
    { return DatasetStore.create(store) ; }

    public static Dataset connectDataset(SDBConnection sdbConnection, StoreDesc desc)
    { return DatasetStore.create(connectStore(sdbConnection, desc)) ; }
    
    public static Dataset connectDataset(String configFile)
    { return DatasetStore.create(connectStore(configFile)) ; }
    
    // ---- Graph
    
    public static Graph connectDefaultGraph(String configFile)
    { return connectDefaultGraph(StoreFactory.create(configFile)) ; }

    public static Graph connectDefaultGraph(StoreDesc desc)
    { return connectDefaultGraph(StoreFactory.create(desc)) ; }

    public static Graph connectDefaultGraph(Store store)
    { return new GraphSDB(store) ; }

    
    public static Graph connectNamedGraph(String configFile, String iri)
    { return connectNamedGraph(StoreFactory.create(configFile), iri) ; }

    public static Graph connectNamedGraph(StoreDesc desc, String iri)
    { return connectNamedGraph(StoreFactory.create(desc), iri) ; }

    public static Graph connectNamedGraph(Store store, String iri)
    { return new GraphSDB(store, iri) ; }

    // ---- Model

    public static Model connectDefaultModel(String configFile)
    { return connectDefaultModel(StoreFactory.create(configFile)) ; }

    public static Model connectDefaultModel(StoreDesc desc)
    { return connectDefaultModel(StoreFactory.create(desc)) ; }

    public static Model connectDefaultModel(Store store)
    { return createModelSDB(store) ; }

    
    public static Model connectNamedModel(StoreDesc desc, String iri)
    { return connectNamedModel(StoreFactory.create(desc), iri) ; }

    public static Model connectNamedModel(Store store, String iri)
    { return createModelSDB(store, iri) ; }

    public static Model connectNamedModel(String configFile, String iri)
    { return connectNamedModel(StoreFactory.create(configFile), iri) ; }

    public static Model connectNamedModel(StoreDesc desc, Node gn)
    { return connectNamedModel(StoreFactory.create(desc), gn) ; }

    public static Model connectNamedModel(Store store, Node gn)
    { return createModelSDB(store, gn) ; }

    public static Model connectNamedModel(String configFile, Node gn)
    { return connectNamedModel(StoreFactory.create(configFile), gn) ; }

    // ----
    
    private static Model createModelSDB(Store store)
    { return ModelFactory.createModelForGraph(new GraphSDB(store)) ; }
    
    private static Model createModelSDB(Store store, String iri)
    { return ModelFactory.createModelForGraph(new GraphSDB(store, iri)) ; }

    private static Model createModelSDB(Store store, Node gn)
    { return ModelFactory.createModelForGraph(new GraphSDB(store, gn)) ; }
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