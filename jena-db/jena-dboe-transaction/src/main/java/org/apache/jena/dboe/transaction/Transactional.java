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

package org.apache.jena.dboe.transaction;

import org.apache.jena.dboe.jenax.Txn;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;

/** Interface that encapsulates the  begin/abort|commit/end operations.
 * <p>The read lifecycle is:
 * <pre> begin(READ) ... end()</pre>
 * <p>{@code commit} and {@code abort} are allowed. 
 * <p>The write lifecycle is:
 * <pre> begin(WRITE) ... abort() or commit()</pre>
 * <p>{@code end()} is optional but preferred.
 * <p>
 * Helper code is available {@link Txn} so, for example:
 * <pre>Txn.execRead(dataset, ()-> { ... sparql query ... });</pre> 
 * <pre>Txn.execWrite(dataset, ()-> { ... sparql update ... });</pre>
 * <p>
 * Directly called, code might look like:
 * <pre>
 *     Transactional object = ...
 *     object.begin(ReadWrite.READ);
 *     try {
 *       ... actions inside a read transaction ...
 *     } finally { object.end(); }
 * </pre>
 * or
 * <pre>
 *     Transactional object = ...
 *     object.begin(ReadWrite.WRITE);
 *     try {
 *        ... actions inside a write transaction ...
 *        object.commit();
 *     } finally {
 *        // This causes an abort if {@code commit} has not been called.
 *        object.end();
 *     }
 * </pre>
 * Exceptions will not be thrown.
 * If any do occur, this indicates serious internal problems with the transaction system.
 */
public interface Transactional extends org.apache.jena.sparql.core.Transactional
{
   /** Start either a READ or WRITE transaction */
   @Override
   public default void begin(ReadWrite readWrite) { 
       begin(TxnType.convert(readWrite));
   }
   
   @Override
   public void begin(TxnType type);
   
   /** Attempt to promote a read transaction to a write transaction.
    * This is not guaranteed to succeed - any changes by another write transaction
    * may restrict promotion.  This depends on the transaction type as to whether
    * it is "read commited" or not.
    * <p>
    * If not "read committed", any intervening write transaction will block promotion.
    * Otherwise, at the point or promotion, changes by other writers become visible. 
    * <p>
    * Promoting a transaction which is already a write transaction will return true. 
    * 
    * @return boolean indicating whether the transaction is now a write transaction or not.
    */
   @Override
   public boolean promote();

   /** Commit a transaction - finish the transaction and make any changes permanent (if a "write" transaction) */
   @Override
   public void commit();
   
   /** Abort a transaction - finish the transaction and undo any changes (if a "write" transaction) */  
   @Override
   public void abort();
   
   /** Finish the transaction - if a write transaction and commit() has not been called, then abort. */
   @Override
   public void end();
   
   /** Return the current mode of the transaction - "read" or "write" */ 
   @Override
   public ReadWrite transactionMode();

   /** Return the transaction type used in {@code begin(TxnType)}. */ 
   @Override
   public TxnType transactionType();
   
   /** Say whether inside a transaction. */ 
   @Override
   public boolean isInTransaction();
}