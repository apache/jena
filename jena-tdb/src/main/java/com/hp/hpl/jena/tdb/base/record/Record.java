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

import static java.lang.String.format;

import java.util.Arrays;

import org.apache.jena.atlas.lib.Bytes ;


import com.hp.hpl.jena.tdb.sys.SystemTDB;

/** A record is pair of key and value.  It may be all key, in which case value is null. 
 */

final
public class Record //implements Comparable<Record>
{
    /*
     * Records of fixed size (controlled by the factory).
     */
    public static final Record NO_REC = null ;
    
    final private byte[] key ;
    final private byte[] value ;
    
    public Record(byte[] key, byte[] value)
    { 
        this.key = key ;
        this.value = value ;
        if ( SystemTDB.Checking )
        {
            if ( value != null && value.length == 0 )
                throw new RecordException("Zero length value") ;
        }
    }
    
    public byte[] getKey()          { return key ; }
    public byte[] getValue()        { return value ; }

//    public boolean eq(Record record)
//    {
//        return eq(this, record) ;
//    }
//
//    public boolean lt(Record record)
//    {
//        return lt(this, record) ;
//    }
//    
//    public boolean le(Record record)
//    {
//        return le(this, record) ;
//    }
//    
//    public boolean ge(Record record)
//    {
//        return ge(this, record) ;
//    }
//    
//    public boolean gt(Record record)
//    {
//        return gt(this, record) ;
//    }
    
    public boolean hasSeparateValue() { return value!=null ; }
    
    @Override
    public int hashCode()
    { 
        return Arrays.hashCode(key) ^ Arrays.hashCode(value) ;
    } 

    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof Record ) ) return false ;
        Record r = (Record)other ;
        return compareByKeyValue(this, r) == 0 ;
    }
    
    @Override
    public String toString()
    {
        if ( value == null )
            return str(key) ;
        return str(key)+":"+str(value) ;
    }
    
    /** Is the key of record1 == key of record2 */
    public static boolean keyEQ(Record record1, Record record2)
    {
        int x = compareByKey(record1, record2) ;
        return x == 0 ;
    }

    /** Is the key of record1 != key of record2 */
    public static boolean keyNE(Record record1, Record record2)
    {
        int x = compareByKey(record1, record2) ;
        return x != 0 ;
    }

    /** Is the key of record1 < key of record2 */
    public static boolean keyLT(Record record1, Record record2)
    {
        int x = compareByKey(record1, record2) ;
        return x < 0 ;
    }

    /** Is the key of record1 <= key of record2 */
    public static boolean keyLE(Record record1, Record record2)
    {
        int x = compareByKey(record1, record2) ;
        return x <= 0 ;
    }
    
    /** Is the key of record1 >= key of record2 */
    public static boolean keyGE(Record record1, Record record2)
    {
        int x = compareByKey(record1, record2) ;
        return x >= 0 ;
    }
    
    /** Is the key of record1 > key of record2 */
    public static boolean keyGT(Record record1, Record record2)
    {
        int x = compareByKey(record1, record2) ;
        return x > 0 ;
    }
    
    /** Is (key, value) of record1 equal to (key,value) of record2 */
    public static boolean equals(Record record1, Record record2)
    {
        int x = compareByKeyValue(record1, record2) ;
        return x == 0 ;
    }


    
    static public String str(byte[] b)
    {
        StringBuilder str = new StringBuilder() ;
        for ( byte aB : b )
        {
            str.append( format( "%02x", aB ) );
        }
        return str.toString() ;
    }
    
    public static int compareByKey(Record record1, Record record2)
    {
        checkKeyCompatible(record1, record2) ;
        return Bytes.compare(record1.key, record2.key) ; 
    }
    
    public static int compareByKeyValue(Record record1, Record record2)
    {
        checkCompatible(record1, record2) ;
        int x = Bytes.compare(record1.key, record2.key) ;
        if ( x == 0 )
        {
            if ( record1.value != null )
                x = Bytes.compare(record1.value, record2.value) ;
        }
        return x ;
    }

    static void checkCompatible(Record record1, Record record2)
    {
        if ( ! compatible(record1, record2, true) )
            throw new RecordException(format("Incompatible: %s, %s", record1, record2)) ;
    }
    
    static void checkKeyCompatible(Record record1, Record record2)
    {
        if ( ! compatible(record1, record2, false) )
            throw new RecordException(format("Incompatible: %s, %s", record1, record2)) ;
    }
    
    static boolean compatible(Record record1, Record record2, boolean checkValue)
    {
        if ( record1.key.length != record2.key.length )
            return false ;
        
        if ( checkValue )
        {
            if ( record1.value == null && record2.value == null )
                return true ;
            if ( record1.value == null )
                return false ;
            if ( record2.value == null )
                return false ;
            if ( record1.value.length != record2.value.length )
                return false;
        }
        return true ;
    }
    
//    private static int compare(byte[] x1, byte[] x2)
//    {
//        for ( int i = 0 ; i < x1.length ; i++ )
//        {
//            byte b1 = x1[i] ;
//            byte b2 = x2[i] ;
//            if ( b1 == b2 )
//                continue ;
//            // Treat as unsigned values in the bytes. 
//            return (b1&0xFF) - (b2&0xFF) ;  
//        }
//        return  0 ;
//    }

}
