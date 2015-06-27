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

package org.apache.jena.sdb.layout2.index;

import org.apache.jena.graph.Node ;
import org.apache.jena.sdb.StoreDesc ;
import org.apache.jena.sdb.layout2.LoaderTuplesNodes ;
import org.apache.jena.sdb.layout2.SQLBridgeFactory2 ;
import org.apache.jena.sdb.layout2.TableDescQuads ;
import org.apache.jena.sdb.layout2.TableDescTriples ;
import org.apache.jena.sdb.sql.SDBConnection ;
import org.apache.jena.sdb.store.StoreBaseHSQL ;

public class StoreTriplesNodesIndexHSQL extends StoreBaseHSQL
{
    public StoreTriplesNodesIndexHSQL(SDBConnection connection, StoreDesc desc)
    {
        super(connection, desc,
              new FmtLayout2IndexHSQL(connection),
              new LoaderTuplesNodes(connection, TupleLoaderIndexHSQL.class),
              new QueryCompilerFactoryIndex(),
              new SQLBridgeFactory2(), new TableDescTriples(), new TableDescQuads(), new TableNodesIndex()) ;
        
        ((LoaderTuplesNodes) this.getLoader()).setStore(this);
    }

    @Override
    public long getSize(Node node)
    {
        return StoreBaseIndex.getSize(getConnection(), getQuadTableDesc(), node) ;
    }
}
