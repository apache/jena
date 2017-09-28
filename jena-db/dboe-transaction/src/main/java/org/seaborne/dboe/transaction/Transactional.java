/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.transaction;

import org.apache.jena.query.ReadWrite ;
import org.seaborne.dboe.jenax.Txn ;

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
 *     object.begin(ReadWrite.READ) ;
 *     try {
 *       ... actions inside a read transaction ...
 *     } finally { object.end() ; }
 * </pre>
 * or
 * <pre>
 *     Transactional object = ...
 *     object.begin(ReadWrite.WRITE) ;
 *     try {
 *        ... actions inside a write transaction ...
 *        object.commit() ;
 *     } finally {
 *        // This causes an abort if {@code commit} has not been called.
 *        object.end() ;
 *     }
 * </pre>
 * Exceptions will not be thrown.
 * If any do occur, this indicates serious internal problems with the transaction system.
 */
public interface Transactional extends org.apache.jena.sparql.core.Transactional
{
   /** Start either a READ or WRITE transaction */
   @Override
   public void begin(ReadWrite readWrite) ;
   
   /** Attempt to promote a read transaction to a write transaction.
    * This is not guaranteed to succeed - any changes by another write transaction
    * may restrict promotion.  
    * <p>
    * In the MR+SW implementation, any intervening write transaction will block promotion.
    * <p>
    * Promoting a transaction which is already a write transaction will return true. 
    * <p>
    * Consider also:
    *  <pre>
    *    .end() ;
    *    .begin(WRITE) ;
    *  </pre>
    *  to see any intermediate commits from another writer.
    * 
    * @return boolean indicating whether the transaction is now a write transaction or not.
    */
   public boolean promote() ;

   /** Commit a transaction - finish the transaction and make any changes permanent (if a "write" transaction) */
   @Override
   public void commit() ;
   
   /** Abort a transaction - finish the transaction and undo any changes (if a "write" transaction) */  
   @Override
   public void abort() ;
   
   /** Finish the transaction - if a write transaction and commit() has not been called, then abort. */
   @Override
   public void end() ;
   
   /** Say whether inside a transaction. */ 
   @Override
   public boolean isInTransaction() ;
}