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

package org.apache.jena.atlas.lib;

import java.util.List ;
import java.util.zip.Adler32 ;
import java.util.zip.CRC32 ;
import java.util.zip.Checksum ;

import org.apache.jena.atlas.logging.Log ;

public class Lib
{
    private Lib() {}
    
    public static final void sync(Object object)
    {
        if ( object instanceof Sync )
            ((Sync)object).sync() ;
    }
    
    /** Return true if obj1 and obj are both null or are .equals, else return false */
    public static final <T> boolean equal(T obj1, T obj2)
    {
        if ( obj1 == null )
            return obj2 == null ;
        // obj1 != null
        if ( obj2 == null )
            return false ;
        return obj1.equals(obj2) ;
    }
    
    /** Return true if obj1 and obj are both null or are .equals, else return false */
    public static final boolean equalsIgnoreCase(String str1, String str2)
    {
        if ( str1 == null )
            return str2 == null ;
        return str1.equalsIgnoreCase(str2) ;
    }
    

    /** Return true if obj1 and obj are ! equal */
    public static final <T> boolean notEqual(T obj1, T obj2)
    {
        return ! equal(obj1, obj2) ;
    }

    static public final String className(Object obj)
    { return classShortName(obj.getClass()) ; }
    
    static public final String classShortName(Class<?> cls)
    {
        return cls.getSimpleName() ;
//        String tmp = cls.getName() ;
//        int i = tmp.lastIndexOf('.') ;
//        tmp = tmp.substring(i+1) ;
//        return tmp ;
    }
    
    /** Do two lists have the same elements? */ 
    public static <T> boolean equalsListAsSet(List<T> list1, List<T> list2)
    {
        if ( list1 == null && list2 == null )
            return true ;
        if ( list1 == null ) return false ;
        if ( list2 == null ) return false ;
        return list1.containsAll(list2) && list2.containsAll(list1) ;
    }

    /** HashCode - allow nulls */
    public static final int hashCodeObject(Object obj) { return hashCodeObject(obj, -4) ; }
    
    /** HashCode - allow nulls */
    public static final int hashCodeObject(Object obj, int nullHashCode)
    {
        if ( obj == null )
            return nullHashCode ; 
        return obj.hashCode() ;
    }
    
    public static final void sleep(int milliSeconds)
    {
        try  { Thread.sleep(milliSeconds) ; }
        catch (InterruptedException ex) { Log.warn(Lib.class, "interrupted", ex) ; }
    }
    
    /** 
     * @see CRC32
     */
    public static long crc32(byte[] bytes)
    {
        return crc(new CRC32(), bytes) ;
    }
    
    /** Faster than CRC32, nearly as good.
     * @see Adler32
     */
    public static long adler32(byte[] bytes)
    {
        return crc(new Adler32(), bytes) ;
    }

    private static long crc(Checksum alg, byte[] bytes)
    {
        alg.reset() ;
        alg.update(bytes, 0, bytes.length) ;
        return alg.getValue() ;
    }
}
