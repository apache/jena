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

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.Adler32 ;
import java.util.zip.CRC32 ;
import java.util.zip.Checksum ;

import org.apache.commons.codec.digest.MurmurHash3;
import org.apache.jena.atlas.logging.Log ;

public class Lib
{
    private Lib() {}

    /**
     * Concatenate two "/" delimited path names.
     * <ul>
     * <li>If path (second argument) starts with "/", it is assumed to be absolute and path is returned.</li>
     * <li>If path (second argument) is "", return the directory.</li>
     * <li>Otherwise the arguments are concatenated ensuring there is a "/" between them.</li>
     * </ul>
     */
    public static String concatPaths(String directory, String path) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(path);
        if ( path.startsWith("/") )
            return path;
        if ( path.isEmpty())
            return directory;
        if ( directory.endsWith("/") )
            return directory+path;
        else
            return directory+"/"+path;
    }

    /** Stream to {@link List} */
    public static <X> List<X> toList(Stream<X> stream) {
        // Findability.
        return StreamOps.toList(stream);
    }

    /** "ConcurrentHashSet" */
    public static final <X> Set<X> concurrentHashSet() {
        return SetUtils.concurrentHashSet() ;
    }

    public static final void sync(Object object) {
        if ( object instanceof Sync )
            ((Sync)object).sync() ;
    }

    /**
     * Return true if obj1 and obj are both null or are .equals, else return false
     * Prefer {@link Objects#equals(Object, Object)}
     */
    public static final <T> boolean equals(T obj1, T obj2) {
        // Include because other equals* operations are here.
        return Objects.equals(obj1, obj2) ;
    }

    /** Return true if obj1 and obj are both null or are .equals, else return false */
    public static final boolean equalsIgnoreCase(String str1, String str2) {
        if ( str1 == null )
            return str2 == null ;
        return str1.equalsIgnoreCase(str2) ;
    }

    /** Return true if obj1 and obj are not equal */
    public static final <T> boolean notEqual(T obj1, T obj2) {
        return !Objects.equals(obj1, obj2) ;
    }

    /** Safely return the class short name for an object -- obj.getClass().getSimpleName() */
    static public final String className(Object obj) {
        if ( obj == null )
            return "null" ;
        return classShortName(obj.getClass()) ;
    }

    /** Safely return the class short name for a class */
    static public final String classShortName(Class<? > cls) {
        if ( cls == null )
            return "null" ;
        return cls.getSimpleName() ;
    }

    /** Create {@link UnsupportedOperationException} with formatted message. */
    static public UnsupportedOperationException unsupportedMethod(Object object, String method) {
        return new UnsupportedOperationException(Lib.className(object) + "." + method);
    }

    /** Do two lists have the same elements without considering the order of the lists nor duplicates? */
    public static <T> boolean equalsListAsSet(List<T> list1, List<T> list2) {
        if ( list1 == null && list2 == null )
            return true ;
        if ( list1 == null ) return false ;
        if ( list2 == null ) return false ;
        return list1.containsAll(list2) && list2.containsAll(list1) ;
    }

    /** HashCode - allow nulls */
    public static final int hashCodeObject(Object obj) { return hashCodeObject(obj, -4) ; }

    /** HashCode - allow nulls */
    public static final int hashCodeObject(Object obj, int nullHashCode) {
        if ( obj == null )
            return nullHashCode ;
        return obj.hashCode() ;
    }

    public static final void sleep(int milliSeconds) {
        try  { Thread.sleep(milliSeconds) ; }
        catch (InterruptedException ex) { Log.warn(Lib.class, "interrupted", ex) ; }
    }

    /** Get an environment variable value; if not found try in the system properties. */
    public static String getenv(String name) {
        return getenv(name, name);
    }

    /**
     * Get system properties (argument sysPropName) or if not found, read an
     * environment variable value (argument envName).
     */
    public static String getenv(String sysPropName, String envName) {
        String x = System.getProperty(sysPropName);
        if ( x == null )
            x = System.getenv(envName);
        return x;
    }

    /** Test whether a property (environment variable or system property) is true. */
    public static boolean isPropertyOrEnvVarSetToTrue(String name) {
        return isPropertyOrEnvVarSetToTrue(name, name);
    }

    /**
     * Test whether a property (argument sysPropName) or an environment variable
     * (argument envName) is true.
     */
    public static boolean isPropertyOrEnvVarSetToTrue(String sysPropName, String envName) {
        String value = getenv(sysPropName, envName);
        if ( value == null )
            return false;
        return value.equalsIgnoreCase("true");
    }

    /**
     * Read thread local, assuming that "null" means it does not exist for this thread.
     * If null is read, the thread local is removed.
     */
    public static <X> X readThreadLocal(ThreadLocal<X> threadLocal) {
        X x = threadLocal.get();
        if ( x == null )
            threadLocal.remove();
        return x ;
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
    public static long adler32(byte[] bytes) {
        return crc(new Adler32(), bytes) ;
    }

    private static long crc(Checksum alg, byte[] bytes) {
        alg.reset() ;
        alg.update(bytes, 0, bytes.length) ;
        return alg.getValue() ;
    }

    /** Calculate the Murmur3 hash of a string, and return it as a hex-encoded string. */
    public static String murmurHashHex(String string) {
        byte[] input = string.getBytes(StandardCharsets.UTF_8);
        long[] x = MurmurHash3.hash128x64(input);
        // Important to do long to hex in lo to hi byte order. Then, it agrees with the Guava function
        // To put it another way, a hash value is bytes, not longs (in Java long is hi-lo byte order).
        //    String xs = String.format("%016x%016x", Long.reverseBytes(x[0]), Long.reverseBytes(x[1]));
        char[] chars = new char[32];
        longAsHexLC(x[0], chars, 0);
        longAsHexLC(x[1], chars, 16);
        return new String(chars);
    }

    /** Long to hex (lower case) chars with low byte first. */
    private static void longAsHexLC(long value, char[] chars, int start) {
        // Avoiding generating intermediate strings from e.g. Bytes.asHexLC
        // Byte loop.
        // Bytes get encoded "high bits first". "AF" is value A*16+F
        for ( int idx = 0 ; idx < 8 ; idx++ ) {
            int i = idx * 8;
            int bValue = (int)((value >> i) & 0xFF);
            // Keep order of the byte - high nibble, low nibble.
            int hi = (bValue & 0xF0) >> 4;
            int lo = (bValue & 0x0F);
            char chHi = Chars.hexDigitsLC[hi];
            char chLo = Chars.hexDigitsLC[lo];
            chars[start + 2 * idx] = chHi;
            chars[start + 2 * idx + 1] = chLo;
        }
    }

    // Powerset (returned as a list).
    // Calculate 2^N then loop i on 0 to 2^n-1
    //     create the set for this entry.
    //     for the j-th element of the input
    //        include the element if the j-th bit is set in i.
    // See also Guava.Sets.powerSet
    /** PowerSet */
    public static <X> Set<Set<X>> powerSet(Set<X> elts) {
        List<X> list = new ArrayList<>(elts);
        int size = list.size();
        long N = (long) Math.pow(2, list.size());
        Set<Set<X>> output = new HashSet<>();
        for (int i = 0; i < N; i++) {
            // This elements.
            Set<X> elt = new HashSet<>();
            // for every bit in N
            for (int j = 0; j < size; j++) {
                int x = (1<<j);
                if ( (x & i) != 0 )
                    elt.add(list.get(j));
            }
            output.add(elt);
        }
        return output;
    }


}
