/**
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

package tx;

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
    
    // Like Data(Input|Output)Stream
    
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
