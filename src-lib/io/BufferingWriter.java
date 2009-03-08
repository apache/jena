/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.WritableByteChannel;

import lib.Bytes;
import lib.Sink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.util.ALog;

import com.hp.hpl.jena.tdb.TDBException;

public class BufferingWriter //extends Writer
{
    private static Logger log = LoggerFactory.getLogger(BufferingWriter.class) ;
    
    // Opposite of PeekReader. 
    // As usualy, the java.io classes have hidden synchronization
    // so in some very critical situations, this can be expensive.
    // Also, this class generalises the notion of destination via the Sink
    // abstraction. 
    
    // If we are buffering, will write decent size units so
    // simply check if output stream of a socket has an additional copy
    // otherwise use a charbuffer/bytebuffer.
    // NIO is probably optimized (e.g. sun.misc.unsafe).
    
    // UTF-8 notes:
    // In the very worse case, one codepoint is 4 bytes.  That's very unlikely.
    //   http://www.unicode.org/versions/Unicode5.0.0/ch03.pdf#G7404 Table 3-7
    // We assume worse case (alternative would be encode to a temp buffer
    // to find the length).  The effect is just that not quite 8K bytes will
    // be sent, assumning that typical items are in the range 0-100 chars.
  
    
    private static final int SIZE = 8*1024 ;                // Unit size in bytes.
    private static final int BLOB_SIZE = SIZE/2 ;           // Large object size, worse case, bytes
    
    private final int blockSize ;
    private final int blobSize ;
    
    private ByteBuffer buffer = ByteBuffer.allocate(SIZE) ;
    private Sink<ByteBuffer> out ;
    
//    /** Create output to a byte buffer - for testing - the byte buffer must be large enough */ 
//    public static BufferingWriter create(ByteBuffer byteBuffer, int size, int blobSize)
//    { return new  BufferingWriter(byteBuffer, size, blobSize) ; }
    
    public static BufferingWriter create(WritableByteChannel out)
    {
        return create(out, SIZE) ;
    }
    
    public static BufferingWriter create(WritableByteChannel out, int size)
    {
        return new BufferingWriter(new SinkChannel(out), size, size/2) ;
    }
    
    public BufferingWriter(Sink<ByteBuffer> sink) { this(sink, SIZE, BLOB_SIZE) ; }
    
    public BufferingWriter(Sink<ByteBuffer> sink, int size, int blobSize)
    {
        this.out = sink ;
        this.blockSize = size ;
        this.blobSize = blobSize ;
    }
    
//    public BufferingWriter(OutputStream out)
//    {
//        this.out = FileUtils.asUTF8(out) ;
//    }

    // Nice names.  No exceptions.
    public void output(CharSequence string)
    {
        int space = string.length() ;   // Chars
        space = 4*space ;               // Very worst case bytes.
        
        boolean largeBlob = (space > blobSize) ; 
        
        // There is no space or too big
        if ( largeBlob || (blockSize-bufferSize()) < space ) 
            flush() ;
        // If too big, do directly.
        if ( largeBlob /* too big */ )
        {
            ByteBuffer bb = ByteBuffer.allocate(space) ;
            Bytes.toByteBuffer(string, bb) ;
            send(out, bb) ;
            return ;
        }
            
        // This always sets "end of input" in the encoder which is
        // fine if we assume no spanning char sequnces across strings or other
        // units written to this writer.
        Bytes.toByteBuffer(string, buffer) ;
    }

    private int bufferSize()
    {
        return buffer.position() ;
    }

    public void output(char chars[])
    {
        output(CharBuffer.wrap(chars)) ;
    }
    
    // Not ideal.
    public void output(int ch)
    {
        char[] b = { (char)ch } ;
        output(b) ;
    }
    
    private static void send(Sink<ByteBuffer> out, ByteBuffer bb)
    {
        if ( log.isDebugEnabled() )
            log.debug("send: "+bb) ;
        if ( out == null )
        {
            System.out.write(bb.array(), 0, bb.position()) ;
            try { System.out.flush() ; } catch (Throwable th) {}
            return ;
        }
        
        if ( bb.position() == 0 )
            ALog.warn(BufferingWriter.class, "Sending zero bytes") ;
        
        bb.flip() ;
        out.send(bb) ;
    }
    
    private static void exception(IOException ex)
    { throw new TDBException(ex) ; }

    // ---- Writer
    
    //@Override
    public void close()
    {
        out.close() ;
    }

    //@Override
    public void flush()
    {
        if ( bufferSize() > 0 )
        {
            send(out, buffer) ;
            buffer.clear() ;
            out.flush() ;
        }
    }

    // Good, old fashioned macros would be nice.
    public static class SinkChannel implements Sink<ByteBuffer>
    {
        private WritableByteChannel out ;

        public SinkChannel(WritableByteChannel out)
        { this.out = out ; }

        @Override
        public void send(ByteBuffer bb)
        { try { out.write(bb) ; } catch (IOException ex) { exception(ex) ; } }
        
        @Override
        public void close()
        { try { out.close() ; } catch (IOException ex) { exception(ex) ; } }

        @Override
        public void flush()
        { }
    }
    
    public static class SinkBuffer implements Sink<ByteBuffer>
    {
        private ByteBuffer out ;

        public SinkBuffer(ByteBuffer out)
        { this.out = out ; }

        @Override
        public void send(ByteBuffer bb)
        { out.put(bb) ; }
        
        @Override
        public void close()
        { }

        @Override
        public void flush()
        { }
    }
    
    public static class SinkOutputStream implements Sink<ByteBuffer>
    {
        private OutputStream out ;

        public SinkOutputStream(OutputStream out)
        { this.out = out ; }

        @Override
        public void send(ByteBuffer bb)
        { try { out.write(bb.array(), 0, bb.limit()) ; } catch (IOException ex) { exception(ex) ; } }
        
        @Override
        public void close()
        { try { out.close(); } catch (IOException ex) { exception(ex) ; } }

        @Override
        public void flush()
        { try { out.flush() ; } catch (IOException ex) { exception(ex) ; } }
    }
    

    
//    @Override
//    public void write(char[] cbuf, int off, int len) throws IOException
//    {
//        output(CharBuffer.wrap(cbuf, off, len)) ;
//    }
//    
//    @Override
//    public void write(char[] cbuf) throws IOException
//    {
//        write(cbuf, 0, cbuf.length) ;
//    }
//    
////    @Override
////    public void write(String string, int off, int len) throws IOException
////    { }
//    
//    @Override
//    public void write(String string) throws IOException
//    { output(string) ; }
//    
//    @Override
//    public void write(int ch) throws IOException
//    { outpout(ch) ; } 
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