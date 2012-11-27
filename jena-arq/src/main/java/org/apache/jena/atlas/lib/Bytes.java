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

import java.io.UnsupportedEncodingException ;
import java.nio.ByteBuffer ;
import java.nio.ByteOrder ;
import java.nio.CharBuffer ;
import java.nio.charset.CharsetDecoder ;
import java.nio.charset.CharsetEncoder ;
import java.nio.charset.CoderResult ;

/** Byte-oriented operations.  Packing and unpacking integers
 *  is in network order (Big endian - which is the preferred order in Java)
 *  {@link "http://en.wikipedia.org/wiki/Endianness"}
 */  

public class Bytes
{
    private Bytes() {}
    
    // Binary search - see atlas.lib.Alg
    
    // http://en.wikipedia.org/wiki/Endianness
    // Java is, by default, network order (big endian)
    // i.e what you get from ByteBuffer.allocate/.allocateDirect();
    
    public static void main(String ... args)
    {
        ByteBuffer bb = ByteBuffer.allocate(8) ;
        System.out.println("Native order = "+ByteOrder.nativeOrder()) ;
        System.out.println("Default order = "+bb.order()) ;
        //bb.order(ByteOrder.BIG_ENDIAN) ;
        //bb.order(ByteOrder.LITTLE_ENDIAN) ;
        System.out.println("Order = "+bb.order()) ;
        bb.asLongBuffer().put(0x0102030405060708L) ;
        for ( int i = 0 ; i < bb.capacity(); i++ )
            System.out.printf("0x%02X ",bb.get(i)) ;
        // Comes out hight to low : 0x01 0x02 0x03 0x04 0x05 0x06 0x07 0x08 
    }
    
    /** Compare two byte arrays which may be of different lengths */ 
    public static int compare(byte[] x1, byte[] x2)
    {
        int n = Math.min(x1.length, x2.length) ;
        
        for ( int i = 0 ; i < n ; i++ )
        {
            byte b1 = x1[i] ;
            byte b2 = x2[i] ;
            if ( b1 == b2 )
                continue ;
            // Treat as unsigned values in the bytes. 
            return (b1&0xFF) - (b2&0xFF) ;  
        }

        return x1.length - x2.length ;
    }

    public static int compareByte(byte b1, byte b2)
    {
        return (b1&0xFF) - (b2&0xFF) ;  
    }
    
    public static byte[] copyOf(byte[] bytes)
    {
        return copyOf(bytes, 0, bytes.length) ;
    }
    
    public static byte[] copyOf(byte[] bytes, int start)
    {
        return copyOf(bytes, start, bytes.length-start) ;
    }
    
    public static byte[] copyOf(byte[] bytes, int start, int length)
    {
        byte[] newByteArray = new byte[length] ;
        System.arraycopy(bytes, start, newByteArray, 0, length) ;
        return newByteArray ;
    }
    
    final public static byte[] hexDigitsUC = {
        '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' ,
        '9' , 'A' , 'B' , 'C' , 'D' , 'E' , 'F' };

    final public static byte[] hexDigitsLC = {
        '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' ,
        '9' , 'a' , 'b' , 'c' , 'd' , 'e' , 'f' };

    /** Get an int from a byte array (network order)
     * @param b Byte Array
     */
    public static final int getInt(byte[]b)
    { return getInt(b, 0) ; }
    
    /** Get an int from a byte array (network order)
     * @param b Byte Array
     * @param idx Starting point of bytes 
     */
    public static final int getInt(byte[]b, int idx)
    {
        return assembleInt(b[idx+0],
                           b[idx+1],
                           b[idx+2],
                           b[idx+3]) ;
    }

    /** Get a long from a byte array (network order)
     * @param b Byte Array
     */
    public static final long getLong(byte[]b)
    { return getLong(b, 0) ; }
    
    /** Get a long from a byte array (network order)
     * @param b Byte Array
     * @param idx Starting point of bytes 
     */
    public static final long getLong(byte[]b, int idx)
    {
        return assembleLong(b[idx+0],
                            b[idx+1],
                            b[idx+2],
                            b[idx+3],
                            b[idx+4],
                            b[idx+5],
                            b[idx+6],
                            b[idx+7]) ;

    }

    /** Put an int into a byte array
     * @param value The integer
     * @param b byte array 
     */
    public static final void setInt(int value, byte[]b)
    { setInt(value, b, 0) ; }
    
    /** Put an int into a byte array from a given position
     * @param x The integer
     * @param b byte array 
     * @param idx starting point
     */
    public static final void setInt(int x, byte[]b, int idx)
    {
//        b[idx+0] = byte3(value) ;
//        b[idx+1] = byte2(value) ;
//        b[idx+2] = byte1(value) ;
//        b[idx+3] = byte0(value) ;
      b[idx+0] = (byte)((x >> 24)&0xFF) ;
      b[idx+1] = (byte)((x >> 16)&0xFF);
      b[idx+2] = (byte)((x >>  8)&0xFF);
      b[idx+3] = (byte)(x &0xFF);

    }
    
    
    /** Put a long into a byte array
     * @param value The integer
     * @param b byte array 
     */
    public static final void setLong(long value, byte[]b)
    { setLong(value, b, 0) ; }
    
    /** Put a long into a byte array from a given position
     * @param value The integer
     * @param b byte array 
     * @param idx starting point
     */
    public static final void setLong(long value, byte[]b, int idx)
    {
        int lo = (int)(value&0xFFFFFFFFL) ;
        int hi = (int)(value>>>32) ;
        setInt(hi, b, idx) ;
        setInt(lo, b, idx+4) ;
    }

    /** int to byte array */
    public static byte[] packInt(int val)
    {
        byte[] valBytes = new byte[Integer.SIZE/Byte.SIZE] ;
        setInt(val, valBytes, 0) ;
        return valBytes ;
    }
    
    /** long to byte array */
    public static byte[] packLong(long val)
    {
        byte[] valBytes = new byte[Long.SIZE/Byte.SIZE] ;
        setLong(val, valBytes, 0) ;
        return valBytes ;
    }
    
    /** Make an int order of args -- high to low */
    static private int assembleInt(byte b3, byte b2, byte b1, byte b0)
    {
        return ( ((b3 & 0xFF) << 24) |
                      ((b2 & 0xFF) << 16) |
                      ((b1 & 0xFF) <<  8) |
                      ((b0 & 0xFF) <<  0)
                    );
    }

    /** Make a long order of args -- high to low */
    static private Long assembleLong(byte b7, byte b6, byte b5, byte b4, byte b3, byte b2, byte b1, byte b0)
    {
        
        return  (((long)b7 & 0xFF) << 56) |
                (((long)b6 & 0xFF) << 48) |
                (((long)b5 & 0xFF) << 40) |
                (((long)b4 & 0xFF) << 32) |
                (((long)b3 & 0xFF) << 24) |
                (((long)b2 & 0xFF) << 16) |
                (((long)b1 & 0xFF) <<  8) |
                (((long)b0 & 0xFF) <<  0) ;
    }

    private static byte byte3(int x) { return (byte)(x >> 24); }
    private static byte byte2(int x) { return (byte)(x >> 16); }
    private static byte byte1(int x) { return (byte)(x >>  8); }
    private static byte byte0(int x) { return (byte)(x >>  0); }

    /** Return the UTF-8 bytes for a string */
    public static byte[] string2bytes(String x)
    {
        try
        {
            return x.getBytes("UTF-8") ;
        } catch (UnsupportedEncodingException ex)
        {
            // Impossible.
            ex.printStackTrace();
            return null ;
        }
    }
    
    /** Return the string for some UTF-8 bytes */
    public static String bytes2string(byte[] x)
    {
        try
        {
            return new String(x, "UTF-8") ;
        } catch (UnsupportedEncodingException ex)
        {
            // Impossible-ish.
            ex.printStackTrace();
            return null ;
        }
    }

    /** Encode a string into a ByteBuffer : on return position is the end of the encoding */
    public static int toByteBuffer(CharSequence s, ByteBuffer bb)
    {
        //BlockUTF8.fromChars(s, bb) ;
        // To be removed (Dec 2011)
        CharsetEncoder enc = Chars.allocEncoder();
        int x = toByteBuffer(s, bb, enc) ;
        Chars.deallocEncoder(enc) ;
        return x ;
    }
    
    /** Encode a string into a ByteBuffer : on return position is the end of the encoding */
    public static int toByteBuffer(CharSequence s, ByteBuffer bb, CharsetEncoder enc)
    {
        int start = bb.position() ;
        CharBuffer cBuff = CharBuffer.wrap(s);
        enc.reset();
        CoderResult r = enc.encode(cBuff, bb, true) ;
        if ( r.isOverflow() )
            throw new InternalErrorException("Bytes.toByteBuffer: encode overflow (1)") ;
        r = enc.flush(bb) ;
        if ( r.isOverflow() )
            throw new InternalErrorException("Bytes.toByteBuffer: encode overflow (2)") ;
//        if ( r.isUnderflow() )
//            throw new InternalErrorException("Bytes.toByteBuffer: encode underflow") ;
        int finish = bb.position() ;
        return finish-start ;
    }
    
    /** Decode a string into a ByteBuffer */
    public static String fromByteBuffer(ByteBuffer bb)
    {
        //return BlockUTF8.toString(bb) ;
        // To be removed (Dec 2011)
        CharsetDecoder dec = Chars.allocDecoder();
        String x = fromByteBuffer(bb, dec) ;
        Chars.deallocDecoder(dec) ;
        return x ;
    }
    
    /** Decode a string into a ByteBuffer */
    public static String fromByteBuffer(ByteBuffer bb, CharsetDecoder dec)
    {
        if ( bb.remaining() == 0 )
            return "" ;

        dec.reset();
        CharBuffer cBuff = CharBuffer.allocate(bb.remaining()) ;
        CoderResult r = dec.decode(bb, cBuff, true) ;
        if ( r.isOverflow() )
            throw new InternalErrorException("fromByteBuffer: decode overflow (1)") ;
        r = dec.flush(cBuff) ;
        if ( r.isOverflow() )
            throw new InternalErrorException("fromByteBuffer: decode overflow (2)") ;
        cBuff.flip();
        return cBuff.toString() ;
    }

    /** Return a hex string representing the bytes, zero padded to length of byte array. */
    public static String asHex(byte[] bytes)
    {
        return asHexUC(bytes) ; 
    }

    public static String asHexUC(byte[] bytes)
    {
        return asHex(bytes, 0, bytes.length, Chars.hexDigitsUC) ; 
    }

    public static String asHexLC(byte[] bytes)
    {
        return asHex(bytes, 0, bytes.length, Chars.hexDigitsLC) ; 
    }
    
    public static String asHex(byte[] bytes, int start, int finish, char[] hexDigits)
    {
        StringBuilder sw = new StringBuilder() ;
        for ( int i = start ; i < finish ; i++ )
        {
            byte b = bytes[i] ;
            int hi = (b & 0xF0) >> 4 ;
            int lo = b & 0xF ;
            //if ( i != start ) sw.append(' ') ;
            sw.append(hexDigits[hi]) ;
            sw.append(hexDigits[lo]) ;
        }
        return sw.toString() ;
    }
    
    /** Return a hex string representing the bytes, zero padded to length of byte array. */
    public static String asHex(byte b)
    {
        return asHexUC(b) ; 
    }

    public static String asHexUC(byte b)
    {
        return asHex(b, Chars.hexDigitsUC) ; 
    }

    public static String asHexLC(byte b)
    {
        return asHex(b, Chars.hexDigitsLC) ; 
    }
    
    private static String asHex(byte b, char[] hexDigits)
    {
        int hi = (b & 0xF0) >> 4 ;
        int lo = b & 0xF ;
        char[] chars = new char[2] ;
        chars[0] = hexDigits[hi] ;
        chars[1] = hexDigits[lo] ;
        return new String(chars) ;
    }

    
    public static int hexCharToInt(char c)
    {
        if ( '0' <= c && c <= '9' )   
            return c-'0' ;
        else if ( 'A' <= c && c <= 'F' )
            return c-'A'+10 ;
        else if ( 'a' <= c && c <= 'f' )
            return c-'a'+10 ;
        else
            throw new IllegalArgumentException("Bad index char : "+c) ;
    }
}
