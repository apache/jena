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

package arq.examples.patterns;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalTrait;

public class ExTransactions {

    // Usage example: these can't be code.
    private static class Example1 implements TransactionalTrait {

        private Transactional theOther;
        Example1(Transactional transactional) {
            this.theOther = transactional;
        }

        private final Transactional txn                     = theOther ;
        @Override
        public final Transactional getTxn()                 { return txn; }
//        For DatasetGraphs:
        @Override public boolean supportsTransactions()     { return true; }
        @Override public boolean supportsTransactionAbort() { return false; }
    }

    // Without public getTxn
    private static class Example2 implements Transactional {

        private Transactional theOther;
        Example2(Transactional transactional) {
            this.theOther = transactional;
        }

        private final Transactional txn                     = theOther ;
        private final Transactional txn()                   { return txn; }
        @Override public void begin()                       { txn().begin(); }
        @Override public void begin(TxnType txnType)        { txn().begin(txnType); }
        @Override public boolean promote(Promote txnType)   { return txn().promote(txnType); }
        @Override public void commit()                      { txn().commit(); }
        @Override public void abort()                       { txn().abort(); }
        @Override public boolean isInTransaction()          { return txn().isInTransaction(); }
        @Override public void end()                         { txn().end(); }
        @Override public ReadWrite transactionMode()        { return txn().transactionMode(); }
        @Override public TxnType transactionType()          { return txn().transactionType(); }
//        For DatasetGraphs:
        /*@Override*/ public boolean supportsTransactions()     { return true; }
        /*@Override*/ public boolean supportsTransactionAbort() { return false; }
    }
}
