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

package org.apache.jena.sdb.layout2.hash;
/* H2 contribution from Martin HEIN (m#)/March 2008 */
import org.apache.jena.sdb.StoreDesc ;
import org.apache.jena.sdb.core.sqlnode.GenerateSQL ;
import org.apache.jena.sdb.layout2.LoaderTuplesNodes ;
import org.apache.jena.sdb.layout2.SQLBridgeFactory2 ;
import org.apache.jena.sdb.sql.SDBConnection ;

public class StoreTriplesNodesHashH2 extends StoreBaseHash
{
    public StoreTriplesNodesHashH2(SDBConnection connection, StoreDesc desc)
    {
        super(connection, desc,
              new FmtLayout2HashH2(connection),
              //new LoaderHashH2(connection),
              new LoaderTuplesNodes(connection, TupleLoaderHashH2.class),
              new QueryCompilerFactoryHash(),
              new SQLBridgeFactory2(),
              new GenerateSQL()) ;
        
        ((LoaderTuplesNodes) this.getLoader()).setStore(this);
    }
}
