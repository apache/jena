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

package org.apache.jena.atlas.io;

import static org.apache.jena.atlas.io.IO.EOF ;

import java.io.IOException ;
import java.io.InputStream ;

/** InputStream optimizing for one byte at a time operation.
 *  BufferedInputStream operations have synchronization making 
 *  reading one byte at a time expensive.
 *  
 *  @see java.io.InputStream
 *  @see java.io.BufferedInputStream
 */
public final class InputStreamBuffered extends InputStream 
{
    public static int DFT_BUFSIZE = 16*1024 ;
    private InputStream source ;
    private byte[] buffer ;
    private int buffLen = 0 ;
    private int idx = 0 ;
    private long count = 0 ;
    
    public InputStreamBuffered(InputStream input)
    {
        this(input, DFT_BUFSIZE) ;
    }
    
    public InputStreamBuffered(InputStream input, int bufsize)
    {
        super() ;
        this.source = input ;
        this.buffer = new byte[bufsize] ;
        this.idx = 0 ;
        this.buffLen = 0 ;
    }

//    @Override
//    public int read(byte b[], int off, int len) throws IOException
//    {
//    }
    
    @Override
    public int read() throws IOException
    {
        return advance() ;
    }
    
    @Override
    public void close() throws IOException
    {
        source.close() ;
    }
    
    public final int advance()
    {
        if ( idx >= buffLen )
            // Points outside the array.  Refill it 
            fillArray() ;
        
        // Advance one character.
        if ( buffLen >= 0 )
        {
            byte ch = buffer[idx] ;
            // Advance the lookahead character
            idx++ ;
            count++ ;
            return ch & 0xFF ;
        }  
        else
            // Buffer empty, end of stream.
            return EOF ;
    }

    private int fillArray()
    {
        try
        {
            int x = source.read(buffer) ;
            idx = 0 ;
            buffLen = x ;   // Maybe -1
            return x ;
        } 
        catch (IOException ex) { IO.exception(ex) ; return -1 ; }
    }
}
