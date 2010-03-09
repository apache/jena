/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
        
        synchronized(bb)
        {
            bb.position(idx*slotLen) ;
            bb.get(key, 0, keyLength) ;
            if ( value != null )
                bb.get(value, 0, valueLength) ;
        }
        return create(key, value) ;
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
        check(record.getKey(), record.getValue()) ;
    }
    
    private final void checkKey(byte[] k)
    {
        if ( k == null )
            throw new RecordException("Null key byte[]") ;
        if ( keyLength != k.length ) 
            throw new RecordException(format("Key length error: This RecordFactory manages records of key length %d, not %d", keyLength, k.length)) ;
    }
    
    private final void check(byte[] k, byte[] v)
    {
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

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */