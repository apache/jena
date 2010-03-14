/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.record;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.Bytes ;


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
    
    static Record intToRecord(int v, RecordFactory factory)
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
        List<Record> x = new ArrayList<Record>() ;
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
            //@Override
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
        List<Integer> x = new ArrayList<Integer>() ;
        for ( int i : vals )
            x.add(i) ;
        return x ;
    }

    public static List<Integer> r(Iterator<Record> iter)
    {
        return RecordLib.toIntList(iter) ;
    }


}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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