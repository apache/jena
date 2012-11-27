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

package com.hp.hpl.jena.tdb.store.bulkloader2;

import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.ColumnMap ;

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
