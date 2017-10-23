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

package org.apache.jena.tdb.transaction;

import org.apache.jena.tdb.base.objectfile.ObjectFile ;
import org.apache.jena.tdb.base.objectfile.ObjectFileWrapper ;

/** Add transactionality control to an ObjectFile.
 * ObjectFiles are "append only" so with a single writer environment, 
 * we just need to manage a reset on abort.
 * A crash in a transaction will accumulate some junk in the file.
 * This is now a tradeoff of speed and space.
 * 
 * Speed : append to the original file directly and tolerate junk.
 * 
 * Space : use a journal file and write to main file on commit.
 * 
 * @see ObjectFileTransComplex  
 */
class ObjectFileTrans extends ObjectFileWrapper implements TransactionLifecycle {
    ObjectFileTrans(Transaction txn /*unused*/, ObjectFile other) {
        super(other);
    }

    private long start = 0;
    
    @Override
    public void begin(Transaction txn) {
        start = other.length();
    }

    @Override
    public void abort(Transaction txn) {
        other.truncate(start);
    }

    @Override
    public void commitPrepare(Transaction txn) {
        // Sync early - before the journal, with its index blocks, is committed.
        other.sync();
    }

    @Override
    public void commitEnact(Transaction txn) { }

    @Override
    public void commitClearup(Transaction txn) {}
}
