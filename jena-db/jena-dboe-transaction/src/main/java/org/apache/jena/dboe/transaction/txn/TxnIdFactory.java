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

import org.apache.commons.lang3.NotImplementedException;

/** Factory for some forms of {@link TxnId}.
 * This is ony some possible {@link TxnIdGenerator}
 *
 * @see TxnId
 */
public class TxnIdFactory {
    /** Generator for {@link TxnId}s for the counter based implementation. */
    public static final TxnIdGenerator txnIdGenSimple = ()->TxnIdSimple.create();
    /** Generator for {@link TxnId}s for the UUID based implementation. */
    public static final TxnIdGenerator txnIdGenUuid   = ()->TxnIdUuid.create();

    /** Return the default, good enough for one JVM
     * (usually the simple counter based implementation)
     */
    public static TxnId create() {
        return createSimple();
    }

    /** Return a TxnId from the counter based implementation. */
    public static TxnId createSimple() {
        return txnIdGenSimple.generate();
    }

    /** Return a TxnId from the UUID based implementation. */
    public static TxnId createUuid() {
        return txnIdGenUuid.generate();
    }

    public static TxnId create(byte[] bytes) {
        switch(bytes.length) {
            case 8 : return TxnIdSimple.create(bytes);
            case 16 :return TxnIdUuid.create(bytes);
            default:
                throw new NotImplementedException("Unrecognized bytes length: "+bytes.length);
        }
    }

}

