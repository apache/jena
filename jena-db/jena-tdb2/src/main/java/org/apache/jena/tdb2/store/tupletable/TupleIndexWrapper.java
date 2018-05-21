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

package org.apache.jena.tdb2.store.tupletable;

import java.util.Collection ;
import java.util.Iterator ;

import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.atlas.lib.tuple.TupleMap ;
import org.apache.jena.tdb2.store.NodeId;

public class TupleIndexWrapper implements TupleIndex
{
    protected final TupleIndex index ;

    public TupleIndexWrapper(TupleIndex index) { this.index = index ; }
    
    @Override
    public final TupleIndex wrapped() {
        return index ;
    }

    @Override
    public void add(Tuple<NodeId> tuple) {
        index.add(tuple) ;
    }

    @Override
    public void addAll(Collection<Tuple<NodeId>> tuples) {
        index.addAll(tuples) ;
    }

    @Override
    public void delete(Tuple<NodeId> tuple) {
        index.delete(tuple) ;
    }

    @Override
    public void deleteAll(Collection<Tuple<NodeId>> tuples) {
        index.deleteAll(tuples);
    }

    @Override
    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> pattern) {
        return index.find(pattern) ;
    }

    @Override
    public Iterator<Tuple<NodeId>> all() {
        return index.all() ;
    }

    @Override
    public int getTupleLength() {
        return index.getTupleLength() ;
    }

    @Override
    public String getMappingStr() {
        return index.getMappingStr() ;
    }

    @Override
    public TupleMap getMapping() {
        return index.getMapping() ;
    }

    @Override
    public String getName() {
        return index.getName() ;
    }

    @Override
    public int weight(Tuple<NodeId> pattern) {
        return index.weight(pattern) ;
    }

    @Override
    public long size() {
        return index.size() ;
    }

    @Override
    public boolean isEmpty() {
        return index.isEmpty() ;
    }

    @Override
    public void clear() {
        index.clear() ;
    }

    @Override
    public void sync() {
        index.sync() ;
    }

    @Override
    public void close() {
        index.close() ;
    }
}
