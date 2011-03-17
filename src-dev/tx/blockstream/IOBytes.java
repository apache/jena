/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx.blockstream;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.OutputStream ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.StrUtils ;

public class IOBytes
{
    // ** Alternative.
    // Use InStreamUTF.advance to get a single char.  Use with InputStreamBuffered
    // OutStreamUTF8.output (but how to avoid a copy into the string).
    
    /** Read an integer */
    public static int readInt(InputStream in)
    {
        try {
            int b3 = in.read() ;
            if ( b3 == -1 )
                return -1 ;
            int b2 = in.read() ;
            if ( b2 == -1 )
                return -1 ;
            int b1 = in.read() ;
            if ( b1 == -1 )
                return -1 ;
            int b0 = in.read() ;
            if ( b0 == -1 )
                return -1 ;
            return ((b3 & 0xFF) << 24) | ((b2 & 0xFF) << 16) | ((b1 & 0xFF) <<  8) | ((b0 & 0xFF) <<  0) ;
        }
        catch (IOException ex) { IO.exception(ex) ; return -1 ; }
    }

    /** Read bytes, as a (length,bytes pair). return null if bad I/O or insufficient bytes. */
    public static byte[] readBytes(InputStream in)
    {
        int len = readInt(in) ;
        byte b[] = new byte[len] ;
        int r = readBytes(in, b) ;
        if ( r == len )
            return b ;
        else
            return null ;
    }
    
    /** Read a fixed length of bytes, trying quite hard.  Return len read. */
    public static int readBytes(InputStream in, byte b[])
    {
        try {
            int len = b.length ;
            int x = 0 ;
            while ( x < len )
            {
                int z = in.read(b, x, len-x) ;
                if ( z < 0 )
                    return x ;
                x = x+z ;
            }
            return x ;
        }
        catch (IOException ex) { IO.exception(ex) ; return -1 ; }
    }

    /** Read string (UTF-8 bytes) */
    public static String readStr(InputStream in)
    {
        byte[] b = readBytes(in) ;
        if ( b == null )
            return null ;
        // Faster to use InStreamUTF8?
        return StrUtils.fromUTF8bytes(b) ;
    }

    /** Write an integer */
    public static void writeInt(OutputStream out, int x)
    {
        try {
            int b3 = (x >> 24)&0xFF ;
            int b2 = (x >> 16)&0xFF ;
            int b1 = (x >>  8)&0xFF ;
            int b0 = (x & 0xFF) ;
            out.write(b3) ;
            out.write(b2) ;
            out.write(b1) ;
            out.write(b0) ;
        }
        catch (IOException ex) { IO.exception(ex) ; }
    }

    /** Write bytes as (length, bytes) */
    public static void writeBytes(OutputStream out, byte[] b)
    {
        writeInt(out, b.length) ;
        try {
            out.write(b) ;
        }
        catch (IOException ex) { IO.exception(ex) ; }
    }

    public static void writeStr(OutputStream out, String str)
    {
        byte[] b = StrUtils.asUTF8bytes(str) ;
        writeBytes(out, b) ;
    }

}

/*
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