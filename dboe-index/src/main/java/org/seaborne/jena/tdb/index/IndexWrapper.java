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

package org.seaborne.jena.tdb.index ;

import java.nio.ByteBuffer ;
import java.util.Iterator ;

import org.seaborne.jena.tdb.base.record.Record ;
import org.seaborne.jena.tdb.base.record.RecordFactory ;
import org.seaborne.transaction.txn.ComponentId ;
import org.seaborne.transaction.txn.Transaction ;

public class IndexWrapper implements Index {
    protected final Index index ;

    public IndexWrapper(Index idx) {
        this.index = idx ;
    }

    @Override
    public ComponentId getComponentId() {
        return index.getComponentId() ;
    }

    @Override
    public Record find(Record record) {
        return index.find(record) ;
    }

    @Override
    public boolean contains(Record record) {
        return index.contains(record) ;
    }

    @Override
    public boolean add(Record record) {
        return index.add(record) ;
    }

    @Override
    public boolean delete(Record record) {
        return index.delete(record) ;
    }

    @Override
    public Iterator<Record> iterator() {
        return index.iterator() ;
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

    @Override
    public RecordFactory getRecordFactory() {
        return index.getRecordFactory() ;
    }

    @Override
    public void check() {
        index.check() ;
    }

    @Override
    public long size() {
        return index.size() ;
    }

    @Override
    public void startRecovery() {
        index.startRecovery();
    }

    @Override
    public void recover(ByteBuffer ref) {
        index.recover(ref) ;
    }

    @Override
    public void finishRecovery() {
        index.finishRecovery() ;
    }

    @Override
    public void begin(Transaction transaction) {
        index.begin(transaction) ;
    }

    @Override
    public ByteBuffer commitPrepare(Transaction transaction) {
        return index.commitPrepare(transaction) ;
    }

    @Override
    public void commit(Transaction transaction) {
        index.commit(transaction);
    }

    @Override
    public void commitEnd(Transaction transaction) {
        index.commitEnd(transaction);
    }

    @Override
    public void abort(Transaction transaction) {
        index.abort(transaction);
    }

    @Override
    public void complete(Transaction transaction) {
        index.complete(transaction);
    }

    @Override
    public void shutdown() {
        index.shutdown();
    }
}
