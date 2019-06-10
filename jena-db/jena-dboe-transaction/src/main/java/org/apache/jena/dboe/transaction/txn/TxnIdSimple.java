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

import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.atlas.lib.Bytes;

/**
 *  Simple TxnId, mainly for debugging.
 */
public class TxnIdSimple implements TxnId {
    private static AtomicLong counter = new AtomicLong(0);

    static TxnIdSimple create() {
        return new TxnIdSimple(counter.incrementAndGet());
    }

    public static TxnIdSimple create(byte[] bytes) {
        return new TxnIdSimple(Bytes.getLong(bytes));
    }

    private final long x;

    public TxnIdSimple(long x) {
        this.x = x;
    }

    @Override
    public String name() {
        return String.format("0x%04X",x);
    }

    @Override
    public byte[] bytes() {
        return Bytes.packLong(x);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(x);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        TxnIdSimple other = (TxnIdSimple)obj;
        if ( x != other.x )
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "txn:"+x;
    }

    @Override
    public long runtime() {
        return x;
    }
}

