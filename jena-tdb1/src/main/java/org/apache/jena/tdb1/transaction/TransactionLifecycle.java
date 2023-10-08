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

package org.apache.jena.tdb1.transaction;

/** Interface that components of the transaction system implement.  
 * This is not the public/application interface to transactions.
 */ 
public interface TransactionLifecycle
{
    // begin - commitPrepare commit, then later. commitEnact - clearup
    // begin - abort
    // May be reused via a new call to begin - see impl for if that's possible. 
    
    /** Start an update transaction */  
    public void begin(Transaction txn) ;
    
    /** End of active phase - will not be making these changes. */
    public void abort(Transaction txn);
    
    /**
     * Prepare to commit; end of active phase. 
     * Make changes safe; do not update the base data.
     */
    public void commitPrepare(Transaction txn);

    /** 
     *  The commit has happened
     */
    public void committed(Transaction txn) ;

    /** Update the base data - called during journal flush. */ 
    public void enactCommitted(Transaction txn) ;
    
    /**
     * All done - transaction committed and incorporated in the base dataset - can now
     * tidy up - called during journal flush if commiting.
     */
    public void clearupCommitted(Transaction txn) ;
}
