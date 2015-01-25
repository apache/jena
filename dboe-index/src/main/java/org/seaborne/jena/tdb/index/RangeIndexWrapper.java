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

package org.seaborne.jena.tdb.index;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.seaborne.jena.tdb.base.record.Record ;
import org.seaborne.jena.tdb.base.record.RecordFactory ;
import org.seaborne.jena.tdb.base.record.RecordMapper ;
import org.seaborne.transaction.txn.ComponentId ;
import org.seaborne.transaction.txn.Transaction ;

public class RangeIndexWrapper implements RangeIndex
{
    // Could extend IndexWrapper but it's nice to have one place to look and set breakpoints.
    protected final RangeIndex rIndex ;
    
    public RangeIndexWrapper(RangeIndex rIdx) { this.rIndex = rIdx ; }
    
    @Override
    public ComponentId getComponentId() {
        return rIndex.getComponentId() ;
    }

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
    public boolean add(Record record)
    { return rIndex.add(record) ; }
    
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
    @Override
    public void startRecovery() {
        rIndex.startRecovery();
    }

    @Override
    public void recover(ByteBuffer ref) {
        rIndex.recover(ref) ;
    }

    @Override
    public void finishRecovery() {
        rIndex.finishRecovery() ;
    }

    @Override
    public void begin(Transaction transaction) {
        rIndex.begin(transaction) ;
    }

    @Override
    public ByteBuffer commitPrepare(Transaction transaction) {
        return rIndex.commitPrepare(transaction) ;
    }

    @Override
    public void commit(Transaction transaction) {
        rIndex.commit(transaction);
    }

    @Override
    public void commitEnd(Transaction transaction) {
        rIndex.commitEnd(transaction);
    }

    @Override
    public void abort(Transaction transaction) {
        rIndex.abort(transaction);
    }

    @Override
    public void complete(Transaction transaction) {
        rIndex.complete(transaction);
    }

    @Override
    public void shutdown() {
        rIndex.shutdown();
    }

}
