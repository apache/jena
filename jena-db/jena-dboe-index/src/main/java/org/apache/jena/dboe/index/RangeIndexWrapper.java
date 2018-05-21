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

package org.apache.jena.dboe.index;

import java.util.Iterator ;

import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.base.record.RecordMapper;

public class RangeIndexWrapper implements RangeIndex
{
    // Could extend IndexWrapper but it's nice to have one place to look and set breakpoints.
    protected final RangeIndex rIndex ;
    
    public RangeIndexWrapper(RangeIndex rIdx) { this.rIndex = rIdx ; }
    
    @Override
    public Record find(Record record)
    { return rIndex.find(record) ; }
    
    @Override
    public boolean contains(Record record)
    { return rIndex.contains(record) ; }
    
    @Override
    public Record minKey()
    { return rIndex.minKey() ; }
    
    @Override
    public Record maxKey()
    { return rIndex.maxKey() ; }
    
    @Override
    public boolean insert(Record record)
    { return rIndex.insert(record) ; }
    
    @Override
    public boolean delete(Record record)
    { return rIndex.delete(record) ; }
    
//  public Record addAndReturnOld(Record record)
//  { return bpt.addAndReturnOld(record) ; }
  
//    public Record deleteAndReturnOld(Record record)
//    { return bpt.deleteAndReturnOld(record) ; }
    
    @Override
    public Iterator<Record> iterator()
    { return rIndex.iterator() ; }
    
    @Override
    public Iterator<Record> iterator(Record minRec, Record maxRec)
    { return rIndex.iterator(minRec, maxRec) ; }
    
    @Override
    public <X> Iterator<X> iterator(Record minRec, Record maxRec, RecordMapper<X> mapper)
    { return rIndex.iterator(minRec, maxRec, mapper) ; }

    @Override
    public boolean isEmpty()
    { return rIndex.isEmpty() ; }
    
    @Override
    public void clear()
    { rIndex.clear() ; }
    
    @Override
    public void sync()
    { rIndex.sync() ; }
    
    @Override
    public void close()
    { rIndex.close() ; }

    public RangeIndex getWrapped()
    { return rIndex ; }
    
    @Override
    public RecordFactory getRecordFactory()
    { return rIndex.getRecordFactory() ; }

    @Override
    public void check()
    { rIndex.check() ; }

    @Override
    public long size()
    { return rIndex.size() ; }
}
