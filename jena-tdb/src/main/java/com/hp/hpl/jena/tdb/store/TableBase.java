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

package com.hp.hpl.jena.tdb.store;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sync ;

import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.nodetupletable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.store.nodetupletable.NodeTupleTableConcrete ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;
import com.hp.hpl.jena.tdb.sys.DatasetControl ;

public class TableBase implements Sync, Closeable
{
    final protected NodeTupleTable table ;
    
    protected TableBase(int N, TupleIndex[] indexes, NodeTable nodeTable, DatasetControl policy)
    {
        table = new NodeTupleTableConcrete(N, indexes, nodeTable, policy) ;
    }

    public NodeTupleTable getNodeTupleTable()   { return table ; }
    public DatasetControl getPolicy()           { return table.getPolicy() ; }
    
//  /** Clear - including the associated node tuple table */
//  public void clear()
//  { 
//      table.getTupleTable().clear() ;
//      table.getNodeTable().clear() ;
//  }

    @Override
    public void sync()
    { 
        table.sync() ;
    }

    @Override
    public void close()
    { table.close() ; }
    
    public boolean isEmpty()        { return table.isEmpty() ; }
    
}
