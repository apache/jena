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

/** Interface that encapulsated begin/abort|commit/close.
 * <p>The read lifcycle is:
 * <pre> begin(READ) ... end()</pre>
 * <p>The write lifcycle is:
 * <pre> begin(WRITE) ... abort() or commit() [end() is optional but preferred]</pre>
 *
 * Code might look like:
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
 *       ... actions inside a write transaction ...
 *     object.commit() ;
 *     } finally {
 *     // This causes an abort if {@code commit} has not been called.
 *     object.end() ;
 *     }
 * </pre>
 * Exceptions will not be thrown.
 * If any do occur, this indicates serious internal problems with the transaction system.
 */
public interface Transactional 
{
   /** Start either a READ or WRITE transaction */ 
   public void begin(ReadWrite readWrite) ;
   
   /** Commit a transaction - finish the transaction and make any changes permanent (if a "write" transaction) */  
   public void commit() ;
   
   /** Abort a transaction - finish the transaction and undo any changes (if a "write" transaction) */  
   public void abort() ;
   
   /** Finish the transaction - if a write transaction and commit() has not been called, then abort. */
   public void end() ;
}