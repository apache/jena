/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb;

import java.sql.Connection;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreDesc;
import com.hp.hpl.jena.sdb.store.StoreFactory;

/** Various operations to create or connect: SDBConnections, Stores, Models, Graphs.
 *  Convenience calls to other factories.
 * @author Andy Seaborne
 * @version $Id$
 */

public class SDBFactory
{
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
    
    public static Store connectStore(SDBConnection sdbConnection, StoreDesc desc) 
    { return StoreFactory.create(sdbConnection, desc) ; }
    
    public static Store connectStore(Connection sqlConnection, StoreDesc desc) 
    {
        SDBConnection sdb = SDBConnectionFactory.create(sqlConnection) ;
        return StoreFactory.create(sdb, desc) ;
    }
    
    public static Graph connectGraph(Store store)
    { return StoreFactory.createGraph(store) ; }

    public static Graph connectGraph(StoreDesc storeDesc)
    { return StoreFactory.createGraph(storeDesc) ; }
    
    public static Graph connectGraph(String configFile)
    { return StoreFactory.createGraph(configFile) ; }
    
    public static Model connectModel(Store store)
    { return StoreFactory.createModel(store) ; }

    public static Model connectModel(StoreDesc storeDesc)
    { return StoreFactory.createModel(storeDesc) ; }

    public static Model connectModel(String configFile)
    { return StoreFactory.createModel(configFile) ; }

    //public static ModelSDB modelForGraph(GraphSDB graph) { return new ModelSDB(graph) ; }
    
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