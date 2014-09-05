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

package com.hp.hpl.jena.tdb.store.tupletable;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.ColumnMap ;
import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.atlas.lib.Tuple ;

import com.hp.hpl.jena.tdb.store.NodeId ;

public interface TupleIndex extends Sync, Closeable
{
    /** Insert a tuple - return true if it was really added, false if it was a duplicate */
    public boolean add(Tuple<NodeId> tuple) ;

    /** Delete a tuple - return true if it was deleted, false if it didn't exist */
    public boolean delete(Tuple<NodeId> tuple) ; 
    
    /** Get a convenient display string for the index - do not rely on the format */ 
    public String getName() ;
    
    /** Get a convenient display string based on the details of the column map - do not rely on the format */ 
    public String getMapping() ;
    
    public ColumnMap getColumnMap() ;
    
    /** Find all matching tuples - a slot of NodeId.NodeIdAny (or null) means match any.
     *  Input pattern in natural order, not index order.
     */

    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> pattern) ;
    
    /** return an iterator of everything */
    public Iterator<Tuple<NodeId>> all() ;
    
    /** Weight a pattern - specified in normal order (not index order).
     * Large numbers means better match. */
    public int weight(Tuple<NodeId> pattern) ;

    /** Length of tuple supported */
    public int getTupleLength() ;

    /** Size of index (number of slots). May be an estimate and not exact. -1 for unknown.  */
    public long size() ;

    /** Answer whether empty or not */
    public boolean isEmpty() ;
    
    /** Clear the index */
    public void clear() ;
}
