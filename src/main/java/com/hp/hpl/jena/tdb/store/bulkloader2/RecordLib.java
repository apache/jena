/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader2;

import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.ColumnMap ;

import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordException ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;

public class RecordLib
{
    public static void main(String...argv)
    {
        RecordFactory rf = new RecordFactory(8,4) ;
        Record r = rf.create() ;
        Bytes.setLong(0x123456789ABCDEF0L, r.getKey()) ;
        Bytes.setInt(0x22334455, r.getValue()) ;
        ColumnMap cMap = new ColumnMap("XYAB", "BYXA") ;
        Record r2 = copyRecord(rf, r, cMap) ;
        System.out.println(r) ;
        System.out.println(r2) ;
    }
    
    /** Copy a record, with reordering of the key */
    public static Record copyRecord(RecordFactory recordFactory, Record record, ColumnMap colMap)
    {
        int kLen = record.getKey().length ;
        int vLen = record.getValue() == null ? 0 : record.getValue().length ;
        if ( recordFactory == null )
            recordFactory = new RecordFactory(kLen, vLen) ;
        int N = colMap.length() ;
        if ( kLen%N != 0 )
            throw new RecordException("Key length is not a multiple of the number of slots") ;
        int itemLen = kLen/N ;
        Record record2 = recordFactory.create() ;
        byte[] k = record2.getKey() ;
        for ( int i = 0 ; i < N ; i++ )
        {
            int j = colMap.mapSlotIdx(i) ;
            System.arraycopy(record.getKey(), i*itemLen, record2.getKey(), j*itemLen, itemLen) ;
        }
        if ( vLen != 0 )
            System.arraycopy(record.getValue(), 0, record2.getValue(), 0, vLen) ;
        return record2 ;
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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