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

package org.apache.jena.dboe.base.record;

import static java.lang.String.format;

import java.nio.ByteBuffer;

import org.apache.jena.atlas.lib.ByteBufferLib;

/** Record creator */
final
public class RecordFactory
{
    private final int keyLength;
    private final int valueLength;
    private final int slotLen;
    private final boolean checking = false;

    public RecordFactory(int keyLength, int valueLength) {
        if ( keyLength <= 0 )
            throw new IllegalArgumentException("Bad key length: " + keyLength);
        if ( valueLength < 0 )
            throw new IllegalArgumentException("Bad value length: " + valueLength);

        this.keyLength = keyLength;
        this.valueLength = valueLength;
        this.slotLen = keyLength + valueLength;
    }

    /** Return a RecordFactory that makes key-only records of the same key size */
    public RecordFactory keyFactory() {
        return new RecordFactory(keyLength, 0);
    }

    /** Create a key-only record, allocating blank space for the key */
    public Record createKeyOnly() {
        return create(new byte[keyLength], null);
    }

    /** Create a key-only record */
    public Record createKeyOnly(Record record) {
        checkKey(record.getKey());
        if ( record.getValue() == null )
            return record;

        return create(record.getKey(), null);
    }

    /** Create a key and value record (value uninitialized) */
    public Record create(byte[] key) {
        checkKey(key);
        byte[] v = null;
        if ( valueLength > 0 )
            v = new byte[valueLength];
        return create(key, v);
    }

    /** Create a record, allocating space for the key and value (if any) */
    public Record create() {
        return create(new byte[keyLength], (valueLength > 0) ? new byte[valueLength] : null);
    }

    /** Create a key and value record */
    public Record create(byte[] key, byte[] value) {
        check(key, value);
        return new Record(key, value);
    }

    public void insertInto(Record record, ByteBuffer bb, int idx) {
        check(record);
        bb.position(idx * slotLen);
        bb.put(record.getKey(), 0, keyLength);
        if ( hasValue() && record.getValue() != null )
            bb.put(record.getValue(), 0, valueLength);
    }

    public static final RecordMapper<Record> mapperRecord = (bb, idx, keyBytes, factory) -> {
        byte[] key = new byte[factory.keyLength];
        byte[] value = (factory.hasValue() ? new byte[factory.valueLength] :null );

        // 2017
        // It is better use synchronize and bulk get. (~10% faster)

//        int slotLen = factory.recordLength();
//        int posnKey = idx*slotLen;
//        // Avoid using position() so we can avoid needing synchronized.
//        copyInto(key, bb, posnKey, factory.keyLength);
//        if ( value != null ) {
//            int posnValue = idx*slotLen+factory.keyLength;
//            copyInto(value, bb, posnValue, factory.valueLength);
//        }

        // Using bb.get(byte[],,) - synchronize and bulk get
        // There's no absolute version.
        synchronized(bb) {
            try {
                bb.position(idx*factory.slotLen);
                bb.get(key, 0, factory.keyLength);
                if ( value != null )
                    bb.get(value, 0, factory.valueLength);
            } catch (Throwable ex) {
                // JENA-1908 investigation
                String msg = String.format("bb.position(%d) idx=%d %s %s\n", idx*factory.slotLen, idx, factory, ByteBufferLib.details(bb));  
                System.err.printf(msg);
                throw ex;
            }
        }
        if ( keyBytes != null )
            System.arraycopy(key, 0, keyBytes, 0, factory.keyLength);
        return factory.create(key, value);
    };

    public <X> X access(ByteBuffer bb, int idx, byte[] keyBytes, RecordMapper<X> mapper) {
        return mapper.map(bb, idx, keyBytes, this);
    }

    public Record buildFrom(ByteBuffer bb, int idx) {
        // Switchover when working. Fornow, assis breakpointing "access" leaf calls.
        return mapperRecord.map(bb, idx, null, this);
        //return access(bb, idx, null, mapperRecord);
    }

    private final static void copyInto(byte[] dst, ByteBuffer src, int start, int length) {
        // Thread safe.
        for ( int i = 0; i < length; i++ )
            dst[i] = src.get(start+i);
    }

    public boolean hasValue()   { return valueLength > 0; }

    public int recordLength()   { return keyLength + valueLength; }

    public int keyLength()      { return keyLength; }

    public int valueLength()    { return valueLength; }

    @Override
    public String toString() {
        return format("<RecordFactory k=%d v=%d>", keyLength, valueLength);
    }

    private final void check(Record record) {
        if ( ! checking ) return;
        check(record.getKey(), record.getValue());
    }

    private final void checkKey(byte[] k) {
        if ( ! checking ) return;
        if ( k == null )
            throw new RecordException("Null key byte[]");
        if ( keyLength != k.length )
            throw new RecordException(format("Key length error: This RecordFactory manages records of key length %d, not %d", keyLength,
                                             k.length));
    }

    private final void check(byte[] k, byte[] v) {
        if ( ! checking ) return;
        checkKey(k);
        if ( valueLength <= 0 ) {
            if ( v != null )
                throw new RecordException("Value array error: This RecordFactory manages records that are all key");
        } else {
            // v == null for a key-only record from this factory.
            if ( v != null && v.length != valueLength )
                throw new RecordException(format("This RecordFactory manages record of value length %d, not (%d,-)", valueLength,
                                                 v.length));
        }
    }
}
