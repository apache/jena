/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dump;

import org.openjena.atlas.lib.Bytes ;

import com.hp.hpl.jena.tdb.base.record.Record;

public class DumpLib
{
    static int outputBytes(Record record, byte[] bytes)
    {
        return outputBytes(record, bytes, 0) ;
    }
    
    /** Record to bytes (for output) */
    static int outputBytes(Record record, byte[] bytes, int idx)
    {
        idx = toByteBufferAsHex(record.getKey(), idx, bytes) ;
        
        if ( record.getValue() != null )
        {
            bytes[idx++] = ' ' ;
            idx = toByteBufferAsHex(record.getValue(), idx, bytes) ;
        }

        bytes[idx++] = ' ' ;
        bytes[idx++] = '.' ;
        bytes[idx++] = '\n' ;
        return idx ;
    }

    // Bytes.ashex
    private static int toByteBufferAsHex(byte[] input, int idx, byte[] output)
    {
        output[idx++] = '0' ;
        output[idx++] = 'x' ;
        for ( byte b : input )
        {
            int hi = (0xFF & b) >> 4 ;
            int lo = b & 0xF ;
            output[idx++] = Bytes.hexDigitsUC[hi] ;
            output[idx++] = Bytes.hexDigitsUC[lo] ;
        }
        return idx ;
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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