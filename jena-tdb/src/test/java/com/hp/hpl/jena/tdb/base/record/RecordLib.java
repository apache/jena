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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.lib.Bytes ;


import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;



/** Record support operations (mainly for testing using ints) */

public class RecordLib
{
    // Size of a record when testing (one integer)
    public final static int TestRecordLength = 4 ;
    
    public final static RecordFactory recordFactory    = new RecordFactory(TestRecordLength, 0) ; 
    
    public static Record intToRecord(int v) { return intToRecord(v, recordFactory) ; }
    public static Record intToRecord(int v, int recLen) { return intToRecord(v, new RecordFactory(recLen, 0)) ; }
    
    public static Record intToRecord(int v, RecordFactory factory)
    {
        byte[] vb = Bytes.packInt(v) ;

        int recLen = factory.recordLength() ;
        byte[] bb = new byte[recLen] ;
        int x = 0 ; // Start point in bb.
        if ( recLen > 4 )
            x = recLen-4 ;
        
        int len = Math.min(4, recLen) ;
        int z = 4-len ; // Start point in vb
    
        // Furthest right bytes.
        for ( int i = len-1 ; i >= 0 ; i-- ) 
           bb[x+i] = vb[z+i] ; 
        
        return factory.create(bb) ;
    }

    public static List<Record> intToRecord(int[] v) { return intToRecord(v, recordFactory) ; }

    public static List<Record> intToRecord(int[] v, int recLen)
    { return intToRecord(v, new RecordFactory(recLen, 0)) ; }
    
    static List<Record> intToRecord(int[] v, RecordFactory factory)
    {
        List<Record> x = new ArrayList<>() ;
        for ( int i : v )
            x.add(intToRecord(i, factory)) ;
        return x ;
    }

    public static int recordToInt(Record key)
    {
        return Bytes.getInt(key.getKey()) ;
    }

    public static List<Integer> toIntList(Iterator<Record> iter)
    {
        return Iter.toList(Iter.map(iter, new Transform<Record, Integer>(){
            @Override
            public Integer convert(Record item)
            {
                return recordToInt(item) ;
            }}
        )) ;
    }
    
    public static Record r(int v)
    {
        return RecordLib.intToRecord(v, RecordLib.TestRecordLength) ; 
    }

    public static int r(Record rec)
    {
        return RecordLib.recordToInt(rec) ; 
    }

    public static List<Integer> toIntList(int... vals)
    {
        List<Integer> x = new ArrayList<>() ;
        for ( int i : vals )
            x.add(i) ;
        return x ;
    }

    public static List<Integer> r(Iterator<Record> iter)
    {
        return RecordLib.toIntList(iter) ;
    }


}
