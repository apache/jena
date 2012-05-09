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

package com.hp.hpl.jena.tdb.index;

import java.util.Iterator ;

import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.Index ;

public class IndexWrapper implements Index
{
    private final Index index ;
    
    public IndexWrapper(Index idx) { this.index = idx ; }
    
    @Override
    public Record find(Record record)
    { return index.find(record) ; }

    @Override
    public boolean contains(Record record)
    { return index.contains(record) ; }
    
    @Override
    public boolean add(Record record)
    { return index.add(record) ; }
    
    @Override
    public boolean delete(Record record)
    { return index.delete(record) ; }
    
    @Override
    public Iterator<Record> iterator()
    { return index.iterator() ; }
    
    @Override
    public boolean isEmpty()
    { return index.isEmpty() ; }
    
    @Override
    public void clear()
    { index.clear() ; }
    
    @Override
    public void sync()
    { index.sync() ; }
    
    @Override
    public void close()
    { index.close() ; }

    @Override
    public RecordFactory getRecordFactory()
    { return index.getRecordFactory() ; }

    @Override
    public void check()
    { index.check() ; }

    @Override
    public long size()
    { return index.size() ; }
}
