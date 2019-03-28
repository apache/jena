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

import java.util.UUID;

import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.dboe.migrate.L;
import org.apache.jena.shared.uuid.JenaUUID;

/** {@link TxnId} based on a {@link UUID}.
 */
public class TxnIdUuid implements TxnId {

    static TxnIdUuid create() {
        UUID id = JenaUUID.generate().asUUID(); // UUID.randomUUID() ;
        return new TxnIdUuid(id);
    }

    public static TxnIdUuid create(byte[] bytes) {
        long mostSignificantBits = Bytes.getLong(bytes, 0);
        long leastSignificantBits = Bytes.getLong(bytes, 8);
        return new TxnIdUuid(mostSignificantBits, leastSignificantBits);
    }

    private long mostSignificantBits;
    private long leastSignificantBits;
    private byte[] bytes = null;
    private String name = null;

    /*package*/ TxnIdUuid(UUID id) {
        mostSignificantBits = id.getMostSignificantBits();
        leastSignificantBits = id.getLeastSignificantBits();
    }

    /*package*/ TxnIdUuid(long mostSig, long leastSig) {
        mostSignificantBits = mostSig;
        leastSignificantBits = leastSig;
    }

    @Override
    public String name() {
        if ( name == null )
            name = L.uuidToString(mostSignificantBits, leastSignificantBits);
        return name;
    }

    @Override
    public byte[] bytes() {
        if ( bytes == null )
            bytes = L.uuidAsBytes(mostSignificantBits, leastSignificantBits);
        return bytes;
    }

    @Override
    public long runtime() {
        // In type 1, the mostSignificantBits have the timestamp in it.
        return mostSignificantBits;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int)(leastSignificantBits ^ (leastSignificantBits >>> 32));
        result = prime * result + (int)(mostSignificantBits ^ (mostSignificantBits >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        TxnIdUuid other = (TxnIdUuid)obj;
        if ( leastSignificantBits != other.leastSignificantBits )
            return false;
        if ( mostSignificantBits != other.mostSignificantBits )
            return false;
        return true;
    }

    @Override
    public String toString() {
        //return name();
        return String.format("[%04X]", mostSignificantBits&0xFFFF);
    }
}

