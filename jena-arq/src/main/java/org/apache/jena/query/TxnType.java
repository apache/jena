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

package org.apache.jena.query;

import java.util.Objects;

import org.apache.jena.sparql.JenaTransactionException;

public enum TxnType {
    /** Transaction mode:
     * <ul>
     * <li>{@code WRITE}: this guarantees a WRITE will complete if {@code commit()} is
     * called. The same as {@code begin(ReadWrite.WRITE)}.
     * 
     * <li>{@code READ}: the transaction can not promote to WRITE,ensuring read-only
     * access to the data. The same as {@code begin(ReadWrite.READ)}.
     * 
     * <li>{@code READ_PROMOTE}: the transaction will go from "read" to "write" if the
     * dataset has not been modified but if it has, the promotion fails with
     * exception.
     * 
     * <li>{@code READ_COMMITTED_PROMOTE}: Use this with care. The promotion will succeed but 
     * changes from other transactions become visible.
     * </ul>
     * 
     * Read committed: at the point transaction attempts promotion from "read" to
     * "write", the system checks if the dataset has changed since the transaction started
     * (called {@code begin}). If {@code READ_PROMOTE}, the dataset must not have
     * changed; if {@code READ_COMMITTED_PROMOTE} any intermediate changes are
     * visible but the application can not assume any data it has read in the
     * transaction is the same as it was at the point the transaction started.
     */
    READ, WRITE, READ_PROMOTE, READ_COMMITTED_PROMOTE
    ;
    /** Convert a {@link ReadWrite} mode to {@code TxnType} */
    public static TxnType convert(ReadWrite rw) {
        switch(rw) {
            case READ: return READ;
            case WRITE: return WRITE;
            default: throw new NullPointerException();
        }
    }
    /** Convert a {@code TxnType} mode to {@link ReadWrite} : "promote" not supported.  */
    public static ReadWrite convert(TxnType txnType) {
        Objects.requireNonNull(txnType);
        switch(txnType) {
            case READ: return ReadWrite.READ;
            case WRITE: return ReadWrite.WRITE;
            default: throw new JenaTransactionException("Incompatible mode: "+txnType);
        }
    }
    /** 
     * Translate a {@code TxnType} to it's initial {@link ReadWrite} mode.
     * {@code WRITE -> WRITE}, {@code READ* -> READ} regardless of promotion setting. 
     */ 
    public static ReadWrite initial(TxnType txnType) {
        Objects.requireNonNull(txnType);
        return (txnType == TxnType.WRITE) ? ReadWrite.WRITE : ReadWrite.READ;
    }

}