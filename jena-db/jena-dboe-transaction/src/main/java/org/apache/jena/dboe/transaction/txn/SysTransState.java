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

package org.apache.jena.dboe.transaction.txn;

/** State for one detached components. */
public class SysTransState {
    private final TransactionalComponent elt;
    private final Transaction transaction;
    private final Object state;

    public SysTransState(TransactionalComponent elt, Transaction transaction, Object state) {
        this.elt = elt;
        this.transaction = transaction;
        this.state = state;
    }

    public TransactionalComponent getComponent()    { return elt; }
    public Transaction getTransaction()             { return transaction; }
    public Object getState()                         { return state; }
}
