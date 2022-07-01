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

package org.apache.jena.query.text;

import org.apache.jena.tdb.transaction.Transaction;
import org.apache.jena.tdb.transaction.TransactionLifecycle;

/**
 * Adapter to put Lucene text indexes into the TDB1 transaction system.
 */
public class TextIndexTDB1 implements TransactionLifecycle {

    private final TextIndex textIndex;

    public TextIndexTDB1(TextIndex textIndex) {
        this.textIndex = textIndex;
    }

    @Override
    public void begin(Transaction txn) {
    }

    @Override
    public void abort(Transaction txn) {
        textIndex.rollback();
    }

    @Override
    public void commitPrepare(Transaction txn) {
        textIndex.prepareCommit();
        textIndex.commit();
    }

    @Override
    public void committed(Transaction txn) { }

    @Override
    public void enactCommitted(Transaction txn) {}

    @Override
    public void clearupCommitted(Transaction txn) {}
}
