/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package lib;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/** Byte-oriented operations.  Packing and unpacking integers
 *  is in network order (Big endian - which is the preferred ordr in Java)
 *  {@link http://en.wikipedia.org/wiki/Endianness}
 */  

public class Bytes
{
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
    
    public static final int getInt(byte[]b)
    { return getInt(b, 0) ; }
    
    public static final int getInt(byte[]b, int idx)
    {
        return assembleInt(b[idx+0],
                           b[idx+1],
                           b[idx+2],
                           b[idx+3]) ;
    }

    public static final long getLong(byte[]b)
    { return getLong(b, 0) ; }
    
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

    public static final void setInt(int value, byte[]b)
    { setInt(value, b, 0) ; }
    
    public static final void setInt(int value, byte[]b, int idx)
    {
        // Network order - high value byte first
        b[idx+0] = byte3(value) ;
        b[idx+1] = byte2(value) ;
        b[idx+2] = byte1(value) ;
        b[idx+3] = byte0(value) ;
    }
    
    public static final void setLong(long value, byte[]b)
    { setLong(value, b, 0) ; }
    
    public static final void setLong(long value, byte[]b, int idx)
    {
        int lo = (int)(value&0xFFFFFFFFL) ;
        int hi = (int)(value>>>32) ;
        setInt(hi, b, idx) ;
        setInt(lo, b, idx+4) ;
    }

    // Order of args -- high to low
    static private int assembleInt(byte b3, byte b2, byte b1, byte b0)
    {
        return (int)( ((b3 & 0xFF) << 24) |
                      ((b2 & 0xFF) << 16) |
                      ((b1 & 0xFF) <<  8) |
                      ((b0 & 0xFF) <<  0)
                    );
    }

    // Order of args -- high to low
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
    
    /** Java name for UTF-8 encoding */
    private static final String encodingUTF8     = "utf-8" ;
    
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
    
//
//
//    public static String string(ByteBuffer bb, int len)
//    {
//        byte b[] = new byte[len] ;
//        bb.get(b) ;
//        return bytes2string(b) ;
//        
//    }

    static Charset utf8 = null ;
    static CharsetEncoder enc = null ;
    static CharsetDecoder dec = null ;
    static {
        try {
            utf8 = Charset.forName(encodingUTF8) ;
            enc = utf8.newEncoder() ;
            dec = utf8.newDecoder() ;
        } catch (Throwable ex)
        {
            ex.printStackTrace(System.err);
        }
    }
    
    public static void toByteBuffer(String s, ByteBuffer bb)
    {
        CharBuffer cBuff = CharBuffer.wrap(s);
        enc.reset() ;
        enc.encode(cBuff, bb, true) ;
        enc.flush(bb) ;
    }
    
    public static String fromByteBuffer(ByteBuffer bb)
    {
        try
        {
            CharBuffer cBuff = dec.decode(bb) ;
            return cBuff.toString() ;
        } catch (CharacterCodingException ex)
        {
            ex.printStackTrace(System.err);
            return null ;
        }
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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