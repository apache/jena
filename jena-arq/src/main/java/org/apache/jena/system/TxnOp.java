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

package org.apache.jena.system;

import java.util.Objects;

import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.Transactional;

public class TxnOp {
    
    /* Transaction compatibility:
     * A TxnType is compatible with (can run inside) another TxType if:
     *  READ < PROMOTE (either) < WRITE  
     * 
     * So
     *   Inner is READ works with any outer.
     *   Outer is WRITE works with any inner.
     *    
     */

    /**
     * Check whether a {@link TxnType} is compatible with another.
     * Typically the "inner" transaction is a requested new transaction
     * and the outer one an existing transaction.
     * 
     * Throw an exception if not compatible.
     * 
     * @see #isTxnTypeCompatible
     * @see #compatibleWithPromote
     * 
     * @param innerTxnType
     * @param outerTxnType
     * @throws JenaTransactionException
     */
    public static void txnTypeCompatibleEx(TxnType innerTxnType, TxnType outerTxnType) {
        boolean b = isTxnTypeCompatible(innerTxnType, outerTxnType);
        if ( ! b )
            throw new JenaTransactionException("Already in a transaction of an incompatable type: "+"outer="+outerTxnType+" : inner="+innerTxnType);
    }
    /** Check whether a {@link TxnType} is compatible with another.
     * Typically then "inner" transaction is a requested new transaction
     * and the outer one an existing transaction.
     * 
     * A TxnType is compatible with (can run inside) another TxnType (for an an existing or outer transaction) if:
     * the outer one covers the needs of the inner one <br/>
     * READ < PROMOTE (either) < WRITE < no outer
     * 
     * In particular:
     *   Inner is READ works with any outer.
     *   Outer is WRITE works with any inner.
     * 
     * For promoting if necessary, see {@link #compatibleWithPromote}
     * 
     * @param innerTxnType
     * @param outerTxnType
     */
    public static boolean isTxnTypeCompatible(TxnType innerTxnType, TxnType outerTxnType) {
        if ( outerTxnType == null ) 
            // Not in an outer transaction.
            return true;
        if ( Objects.equals(innerTxnType, outerTxnType) )
            return true;
        // Outer any, inner READ 
        if ( TxnType.READ.equals(innerTxnType) )
            return true;
        // Outer WRITE, inner any 
        if ( TxnType.WRITE.equals(outerTxnType) )
            return true;
        // Outer READ, inner WRITE or PROMOTE 
        if ( TxnType.READ.equals(outerTxnType) )
            return false;
        return false;
    }

    /**
     * Check an existing {@link Transactional} for compatibility with a {@link TxnType} and
     * promote the {@link Transactional} if necessary.
     * The {@link Transactional} does not need to already be in a transaction.
     * Throw a {@link JenaTransactionException} if not compatible (e.g. current READ, requesting a WRITE). 
     * @param requestedTxnType
     * @param currentTxn
     * @throws JenaTransactionException if not possible.
     */
    public static void compatibleWithPromote(TxnType requestedTxnType, Transactional currentTxn) {
        TxnType currentTxnType = currentTxn.transactionType();
        if ( currentTxnType == null ) 
            // Not in an outer transaction.
            return;
        // Outer any, inner READ 
        if ( TxnType.READ.equals(requestedTxnType) )
            return;
           // Outer WRITE, inner any 
        if ( TxnType.WRITE.equals(currentTxnType) )
            return;
        if ( TxnType.READ.equals(currentTxnType) )
            throw new JenaTransactionException("Already in a READ transaction: outer="+currentTxnType+" : inner="+requestedTxnType);
        
        // Outer PROMOTE (either kind), inner is not READ.
        // Try to promote outer if inner is WRITE.
        if ( requestedTxnType == TxnType.WRITE ) {
            boolean x = currentTxn.promote();
            if ( x )
                return ;
            throw new JenaTransactionException("Can't promote outer transaction: "+"outer="+currentTxnType+" : inner="+requestedTxnType);
        }
        
        if ( Objects.equals(requestedTxnType, currentTxnType) )
            return;
        
        throw new JenaTransactionException("Already in a transaction of an incompatable type: "+"outer="+currentTxnType+" : inner="+requestedTxnType);
    }
}
