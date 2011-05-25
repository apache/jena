/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import java.util.List ;
import java.util.zip.Adler32 ;
import java.util.zip.CRC32 ;
import java.util.zip.Checksum ;

import org.openjena.atlas.logging.Log ;

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
    
    /** Return true if obj1 and obj are ! equal */
    public static final <T> boolean notEqual(T obj1, T obj2)
    {
        return ! equal(obj1, obj2) ;
    }

    static public final String className(Object obj)
    { return classShortName(obj.getClass()) ; }
    
    static public final String classShortName(Class<?> cls)
    {
        String tmp = cls.getName() ;
        int i = tmp.lastIndexOf('.') ;
        tmp = tmp.substring(i+1) ;
        return tmp ;
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
    
    // Confess - these constructors are very cheap (objects are one private int) so this is unnecessary.
    private static Checksum crc32 = new CRC32() ;
    private static Checksum adler32 = new Adler32() ;
    
    public static long crc32(byte[] bytes)
    {
        return crc(crc32, bytes) ;
    }
    
    /** Faster than CRC32, nearly as good.
     * @see Adler32
     */
    public static long adler32(byte[] bytes)
    {
        return crc(adler32, bytes) ;
    }

    private static long crc(Checksum alg, byte[] bytes)
    {
        alg.reset() ;
        alg.update(bytes, 0, bytes.length) ;
        return alg.getValue() ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
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