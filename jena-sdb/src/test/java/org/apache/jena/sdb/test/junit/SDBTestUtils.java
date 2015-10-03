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

package org.apache.jena.sdb.test.junit;

import org.apache.jena.sdb.SDBFactory ;
import org.apache.jena.sdb.Store ;
import org.apache.jena.sdb.StoreDesc ;
import org.apache.jena.sdb.sql.JDBC ;
import org.apache.jena.sdb.sql.SDBConnection ;
import org.apache.jena.sdb.store.DatabaseType ;
import org.apache.jena.sdb.store.LayoutType ;

public class SDBTestUtils
{
    static { JDBC.loadDriverHSQL() ; }

    /** Create an HSQLDB-backed in-memory store for testing. */
    public static Store createInMemoryStore()
    {
        SDBConnection conn = SDBFactory.createConnection("jdbc:hsqldb:mem:test", "sa", "") ;
        StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.HSQLDB) ;
        
        Store store = SDBFactory.connectStore(conn, desc) ;
        store.getTableFormatter().create() ;
        store.getTableFormatter().truncate() ;
        return  store ;
    }
}
