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
import java.util.function.Supplier ;

import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.Transactional ;

/** Application utilities for executing code in transactions.
 * <p>
 * Nested transaction are not supported but calling inside an existing transaction, 
 * which must be compatible, (i.e. a write needs a WRITE transaction).
 * causes the exising transaction to be used.
 */

public class Txn {
    /**
     * Execute in a "read" transaction that can promote to "write".
     * <p>
     * Such a transaction may abort if an update is executed
     * by another thread before this one is promoted to "write" mode.
     * If so, the data protected by {@code txn} is unchanged.
     * <p>
     * If the application knows updates will be needed, consider using {@link #executeWrite}
     * which starts in "write" mode.
     * <p>
     * The application code can call {@link Transactional#promote} to attempt to
     * change from "read" to "write"; the {@link Transactional#promote promote} method
     * returns a boolean indicating whether the promotion was possible or not. 
     */
    public static <T extends Transactional> void execute(T txn, Runnable r) {
        exec(txn, TxnType.READ_PROMOTE, r);
    }

    /**
     * Execute in a "read" transaction that can promote to "write" and return some calculated value. 
     * <p>
     * Such a transaction may abort if an update is executed
     * by another thread before this one is promoted to "write" mode.
     * If so, the data protected by {@code txn} is unchanged.
     * <p>
     * If the application knows updates will be needed, consider using {@link #executeWrite}
     * which starts in "write" mode.
     * <p>
     * The application code can call {@link Transactional#promote} to attempt to
     * change from "read" to "write"; the {@link Transactional#promote promote} method
     * returns a boolean indicating whether the promotion was possible or not. 
     */
    public static <T extends Transactional, X> X calculate(T txn, Supplier<X> r) {
        return calc(txn, TxnType.READ_PROMOTE, r);
    }

    /** Execute application code in a transaction with the given {@link TxnType trasnaction type}. */
    public static <T extends Transactional> void exec(T txn, TxnType txnType, Runnable r) {
        boolean b = txn.isInTransaction() ;
        if (  b )
            checkCompatible(txn, txnType);
        else
            txn.begin(txnType) ;
        try { r.run() ; }
        catch (Throwable th) {
            onThrowable(th, txn);
            throw th ;
        }
        if ( !b ) {
            if ( txn.isInTransaction() )
                // May have been explicit commit or abort.
                txn.commit() ;
            txn.end() ;
        }
    }

    /** Execute and return a value in a transaction with the given {@link TxnType trasnaction type}. */
    public static <T extends Transactional, X> X calc(T txn, TxnType txnType, Supplier<X> r) {
        boolean b = txn.isInTransaction() ;
        if (  b )
            checkCompatible(txn, txnType);
        else
            txn.begin(txnType) ;
        X x;
        try { x = r.get() ; } 
        catch (Throwable th) {
            onThrowable(th, txn);
            throw th ;
        }
        
        if ( !b ) {
            if ( txn.isInTransaction() )
                // May have been explicit commit or abort.
                txn.commit() ;
            txn.end() ;
        }
        return x;
    }

    /** Execute in a read transaction */
    public static <T extends Transactional> void executeRead(T txn, Runnable r) {
        exec(txn, TxnType.READ, r);
    }
    
    /** Execute and return a value in a read transaction */
    public static <T extends Transactional, X> X calculateRead(T txn, Supplier<X> r) {
        return calc(txn, TxnType.READ, r);
    }

    /** Execute the Runnable in a write transaction */
    public static <T extends Transactional> void executeWrite(T txn, Runnable r) {
        exec(txn, TxnType.WRITE, r);
    }

    /** Execute and return a value in a write transaction. */
    public static <T extends Transactional, X> X calculateWrite(T txn, Supplier<X> r) {
        return calc(txn, TxnType.WRITE, r);
    }
    
    /** Check the requested transaction {@code innerTxnType} is compatible with the transactional.
     * @param txn
     * @param innerTxnType
     */
    private static void checkCompatible(Transactional txn, TxnType innerTxnType) {
        TxnType outerTxnType = txn.transactionType();
        if ( outerTxnType == null ) 
            // Not in an outer transaction.
            return;
        // innerTxnType must be "less than or equal to the outer".
        //    TxnType.level == int?
        // Inner is READ works with any outer.
        // Outer is WRITE works with any inner. 
        // Must match:
        // Outer is READ, then inner must be READ.
        // Promotion must be the same.
        
        // Outer any, inner READ 
        if ( TxnType.READ.equals(innerTxnType) )
            return;
           // Outer WRITE, inner any 
        if ( TxnType.WRITE.equals(outerTxnType) )
            return;
        if ( TxnType.READ.equals(outerTxnType) )
            throw new JenaTransactionException("Already in a READ transaction: outer="+outerTxnType+" : inner="+innerTxnType);
        
        // Outer PROMOTE (either kind), inner is not READ.
        // Try to promote outer if inner is WRITE.
        if ( innerTxnType == TxnType.WRITE ) {
            boolean x = txn.promote();
            if ( x )
                return ;
            throw new JenaTransactionException("Can't promote outer transaction: "+"outer="+outerTxnType+" : inner="+innerTxnType);
        }
        
        if ( Objects.equals(innerTxnType, outerTxnType) )
            return;
        
        throw new JenaTransactionException("Already in a transaction of an incompatable type: "+"outer="+outerTxnType+" : inner="+innerTxnType);
    }
    
    // Attempt some kind of cleanup.
    private static <T extends Transactional> void onThrowable(Throwable th, T txn) {
        try {
            txn.abort() ;
            txn.end() ;
        } catch (Throwable th2) { th.addSuppressed(th2); }
    }
}
