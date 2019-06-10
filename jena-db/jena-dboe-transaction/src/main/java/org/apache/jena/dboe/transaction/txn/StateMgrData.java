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

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.jena.dboe.base.file.BufferChannel;

/** StateManagement for a number of long values, a common need
 * (might as well store ints as long on disk
 * for small numbers of integers)
 */
public class StateMgrData extends StateMgrBase {
    private final long[] data;

    public StateMgrData(BufferChannel storage, long... initialData) {
        super(storage, numBytes(initialData));
        data = copy(initialData);
        super.init();
    }

    @Override
    protected void init() { throw new TransactionException("Don't call init()"); }

    private static long[] copy(long[] data) { return Arrays.copyOf(data, data.length); }

    private static int numBytes(long[] data) { return data.length * Long.BYTES; }

    // Protected - leave whether to expose these operations as "public"
    // to the subclass.  A subclass may choose instead to make these
    // to more meaningful names, or ensure that daat consistentecny rules are applied.

    protected long[] getData() {
        return copy(data);
    }

    protected void setData(long... newData) {
        if ( newData.length != data.length )
            throw new IllegalArgumentException();
        System.arraycopy(newData, 0, data, 0, data.length);
    }

    protected long get(int i) {
        return data[i];
    }

    protected void set(int i, long v) {
        data[i] = v;
        super.setDirtyFlag();
    }

    @Override
    protected ByteBuffer serialize(ByteBuffer bytes) {
        for ( int i = 0; i < data.length ; i++ )
            bytes.putLong(data[i]);
        return bytes;
    }

    @Override
    protected void deserialize(ByteBuffer bytes) {
        for ( int i = 0; i < data.length ; i++ )
            data[i] = bytes.getLong();
    }

    @Override
    protected void writeStateEvent() {}

    @Override
    protected void readStateEvent() {}
}

