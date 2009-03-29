/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package atlas.lib;

import java.io.PrintStream;
import java.nio.ByteBuffer;

public class ByteBufferLib
{
    private ByteBufferLib() {}
    
//    protected static void fill(ByteBuffer bb, byte fillValue)
//    {
//        for ( int i = 0; i < bb.limit(); i++ )
//            bb.put(i, fillValue) ;
//    }
    

    public static void fill(ByteBuffer bb, byte v)
    { fill(bb, bb.position(), bb.limit(), v) ; }
    
    public static void fill(ByteBuffer bb, int start, int finish, byte v)
    {
        for ( int i = start ; i < finish ; i++ )
            bb.put(i, v) ;
    }

    public static void print(ByteBuffer byteBuffer)
    {
        print(System.out, byteBuffer) ; 
    }

    public static void print(PrintStream out, ByteBuffer byteBuffer)
    {
        out.printf("ByteBuffer[pos=%d lim=%d cap=%d]",byteBuffer.position(), byteBuffer.limit(), byteBuffer.capacity()) ;
        
        // Print bytes.
        int i = 0 ;
        int maxBytes = 3*24 ;
        for ( ; i < maxBytes && i < byteBuffer.limit() ; i++ )
        {
            if ( i%24 == 0 )
                out.println() ;
            out.printf(" 0x%02X", byteBuffer.get(i)) ;
        }
        if ( i < maxBytes )
            out.print(" ...") ;
        // Print as 4-byte ints
//        int maxSlots = 8 ;
//        int i = 0 ;
//        for ( ; i < maxSlots && 4*i < byteBuffer.limit() ; i++ )
//            out.printf(" 0x%04X", byteBuffer.getInt(4*i)) ;
//        if ( i < maxSlots )
//            out.print(" ...") ;
        out.println();
    }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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