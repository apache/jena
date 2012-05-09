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

package com.hp.hpl.jena.tdb.base.record;

import static java.lang.String.format ;
import java.nio.ByteBuffer;

/** Record creator */
final
public class RecordFactory
{
    private final int keyLength ;
    private final int valueLength ;
    private final int slotLen ;
    private final boolean checking = false ;

    public RecordFactory(int keyLength, int valueLength)
    {
        if ( keyLength <= 0 )
            throw new IllegalArgumentException("Bad key length: "+keyLength) ;
        if ( valueLength < 0 )
            throw new IllegalArgumentException("Bad value length: "+valueLength) ;

        this.keyLength = keyLength ;
        this.valueLength = valueLength ;
        this.slotLen = keyLength + (valueLength>0 ? valueLength : 0 ) ;
    }
    
    /** Return a RecordFactory that makes key-only records of the same key size */ 
    public RecordFactory keyFactory()
    {
        return new RecordFactory(keyLength, 0) ;
    }

    /** Create a key-only record, allocating blank space for the key  */
    public Record createKeyOnly()
    {
        return create(new byte[keyLength], null) ;
    }
    
    /** Create a key-only record */
    public Record createKeyOnly(Record record)
    {
        checkKey(record.getKey()) ;
        if ( record.getValue() == null )
            return record ;
        
        return create(record.getKey(), null) ;
    }
    
    /** Create a key and value record (value uninitialized) */
    public Record create(byte[] key)
    { 
        checkKey(key) ;
        byte[] v = null ;
        if ( valueLength > 0 )
            v = new byte[valueLength] ;
        return create(key, v) ;
    }
    
    /** Create a record, allocaing space for the key and value (if any) */
    public Record create()
    { return create(new byte[keyLength], 
                    (valueLength > 0) ? new byte[valueLength] : null) ;
    }
    
    /** Create a key and value record */
    public Record create(byte[] key, byte[] value)
    {
        check(key, value) ;
        return new Record(key, value) ;
    }
    
    public void insertInto(Record record, ByteBuffer bb, int idx)
    {
        check(record) ;
        bb.position(idx*slotLen) ;
        bb.put(record.getKey(), 0, keyLength) ;
        if ( hasValue() && record.getValue() != null )
            bb.put(record.getValue(), 0, valueLength) ;
    }
    
    public Record buildFrom(ByteBuffer bb, int idx)
    {
        byte[] key = new byte[keyLength] ;
        byte[] value = (hasValue() ? new byte[valueLength] :null ) ;

//        int posnKey = idx*slotLen ;
//        // Avoid using position() so we can avoid needing synchronized.
//        copyInto(key, bb, posnKey, keyLength) ;
//        if ( value != null )
//        {
//            int posnValue = idx*slotLen+keyLength ;
//            copyInto(value, bb, posnValue, valueLength) ;
//        }
        
        // Using bb.get(byte[],,) may be potentially faster but requires the synchronized
        // There's no absolute version.
        synchronized(bb)
        {
            bb.position(idx*slotLen) ;
            bb.get(key, 0, keyLength) ;
            if ( value != null )
                bb.get(value, 0, valueLength) ;
        }
        return create(key, value) ;
    }
    
    private final void copyInto(byte[] dst, ByteBuffer src, int start, int length)
    {
        // Thread safe.
        for ( int i = 0 ; i < length ; i++ )
            dst[i] = src.get(start+i) ;
        // Would otherwise be ...
//        src.position(start) ;
//        src.get(dst, 0, length) ;
    }
    
    public boolean hasValue()   { return valueLength > 0 ; }

    public int recordLength()   { return keyLength + valueLength ; }
    
    public int keyLength()      { return keyLength ; }

    public int valueLength()    { return valueLength ; }
    
    @Override
    public String toString()
    {
        return format("<RecordFactory k=%d v=%d>", keyLength, valueLength) ; 
    }
    
    private final void check(Record record)
    {
        if ( ! checking ) return ;
        check(record.getKey(), record.getValue()) ;
    }
    
    private final void checkKey(byte[] k)
    {
        if ( ! checking ) return ;
        if ( k == null )
            throw new RecordException("Null key byte[]") ;
        if ( keyLength != k.length ) 
            throw new RecordException(format("Key length error: This RecordFactory manages records of key length %d, not %d", keyLength, k.length)) ;
    }
    
    private final void check(byte[] k, byte[] v)
    {
        if ( ! checking ) return ;
        checkKey(k) ;
        if ( valueLength <= 0 )
        {
            if ( v != null ) 
                throw new RecordException("Value array error: This RecordFactory manages records that are all key") ;
        }
        else
        {
            // v == null for a key-only record from this factory.
            if ( v != null && v.length != valueLength )
                throw new RecordException(format("This RecordFactory manages record of value length %d, not (%d,-)", valueLength, v.length)) ;
        }
    }
}
