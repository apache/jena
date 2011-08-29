/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.io;

import java.io.IOException ;
import java.io.OutputStream ;
import java.io.Writer ;
import java.nio.ByteBuffer ;
import java.nio.CharBuffer ;
import java.nio.channels.WritableByteChannel ;

import org.openjena.atlas.AtlasException ;
import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.Sink ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import org.openjena.atlas.logging.Log ;


/** A buffering writer, send output to a channel (WriteableByteChannel)
 *  (and own methods which do not throw checked exceptions).
 *  Only supports UTF-8. 
 *  <p>
 *  The java.io classes have hidden synchronization so in some very critical
 *  situations, this can be expensive (such situations are not common).
 *  This class generalises the notion of destination via the Sink
 *  abstraction (block output based on ByteBuffers).
 *  </p>
 *  This class is not thread safe.

 *  @see PeekReader 
 */

public final class BufferingWriter extends Writer
{
    private static Logger log = LoggerFactory.getLogger(BufferingWriter.class) ;
    
    // Opposite of PeekReader. 
    // As usualy, the java.io classes have hidden synchronization
    // so in some very critical situations, this can be expensive.
    // Also, this class generalises the notion of destination via the Sink
    // abstraction (block  outout based on ByteBuffers). 
    
    // UTF-8 notes:
    // In the very worse case, one codepoint is 4 bytes.  That's very unlikely.
    //   http://www.unicode.org/versions/Unicode5.0.0/ch03.pdf#G7404 Table 3-7
    // We assume worse case (alternative would be encode to a temp buffer
    // to find the length).  The effect is just that not quite 8K bytes will
    // be sent, assumning that typical items are in the range 0-100 chars.
  
    // Default sizes
    private static final int SIZE = 8*1024 ;                // Unit size in bytes.
    private static final int BLOB_SIZE = SIZE/2 ;           // Large object size, worse case, bytes
    
    // Sizes for this instance
    private final int blockSize ;
    private final int blobSize ;
    
    private ByteBuffer buffer = ByteBuffer.allocate(SIZE) ;
    private Sink<ByteBuffer> out ;
    private char[] oneChar = new char[1] ;

    /** Convenience operation to output to a WritableByteChannel */
    public static BufferingWriter create(WritableByteChannel out)
    {
        return create(out, SIZE) ;
    }
    
    /** Convenience operation to output to a WritableByteChannel */
    public static BufferingWriter create(WritableByteChannel out, int size)
    {
        return new BufferingWriter(new SinkChannel(out), size, size/2) ;
    }
    
    /** Writer(chars) over OutputStream (bytes) -- heavily buffered -- flushing may be needed */
    public static BufferingWriter create(OutputStream out)
    {
        return new BufferingWriter(new SinkOutputStream(out), SIZE, BLOB_SIZE) ;
    }


    /** Convenience operation to output to a Writer */
    public static BufferingWriter create(OutputStream out, int size)
    {
        return new BufferingWriter(new SinkOutputStream(out), size, size/2) ;
    }

    /** Create a buffering output stream of charcaters to a {@link org.openjena.atlas.lib.Sink} */
    public BufferingWriter(Sink<ByteBuffer> sink) { this(sink, SIZE, BLOB_SIZE) ; }
    
    /** Create a buffering output stream of charcaters to a {@link org.openjena.atlas.lib.Sink} */
    public BufferingWriter(Sink<ByteBuffer> sink, int size, int blobSize)
    {
        this.out = sink ;
        this.blockSize = size ;
        this.blobSize = blobSize ;
    }
    
    /** Output characters (The String class implements CharSequence)*/
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

    /** Output an array of characters */
    public void output(char chars[])
    {
        output(CharBuffer.wrap(chars)) ;
    }
    
    /** Output an array of characters
     * 
     * @param chars     Characters
     * @param start     Start (inclusive)
     * @param finish    Finish (exclusive)
     */
    public void output(char chars[],int start, int finish)
    {
        output(CharBuffer.wrap(chars, start, finish)) ;
    }
    

    /** Output a single character */
    public void output(int ch)
    {
        // TODO It might be worth recoding this to directly put UTF-8 bytes
        // into the output buffer, rather than use oneChar.  
        oneChar[0] = (char)ch ;
        output(oneChar) ;
        oneChar[0] = 0;
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
            Log.warn(BufferingWriter.class, "Sending zero bytes") ;
        
        bb.flip() ;
        out.send(bb) ;
    }
    
    private static void exception(IOException ex)
    { throw new AtlasException(ex) ; }

    // ---- Writer
    
    @Override
    public void close()
    {
        flush() ;
        out.close() ;
    }

    @Override
    public void flush()
    {
        if ( bufferSize() > 0 )
        {
            send(out, buffer) ;
            out.flush() ;
            buffer.clear() ;
        }
    }

    // Good, old fashioned macros would be nice.
    public static class SinkChannel implements Sink<ByteBuffer>
    {
        private WritableByteChannel out ;

        public SinkChannel(WritableByteChannel out)
        { this.out = out ; }

        //@Override
        public void send(ByteBuffer bb)
        { try { out.write(bb) ; } catch (IOException ex) { exception(ex) ; } }
        
        //@Override
        public void close()
        { try { out.close() ; } catch (IOException ex) { exception(ex) ; } }

        //@Override
        public void flush()
        { }
    }
    
    public static class SinkBuffer implements Sink<ByteBuffer>
    {
        private ByteBuffer out ;

        public SinkBuffer(ByteBuffer out)
        { this.out = out ; }

        //@Override
        public void send(ByteBuffer bb)
        { out.put(bb) ; }
        
        //@Override
        public void close()
        { }

        //@Override
        public void flush()
        { }
    }
    
    public static class SinkOutputStream implements Sink<ByteBuffer>
    {
        private OutputStream out ;

        public SinkOutputStream(OutputStream out)
        { this.out = out ; }

        //@Override
        public void send(ByteBuffer bb)
        { try { out.write(bb.array(), 0, bb.limit()) ; } catch (IOException ex) { exception(ex) ; } }
        
        //@Override
        public void close()
        { try { out.close(); } catch (IOException ex) { exception(ex) ; } }

        //@Override
        public void flush()
        { try { out.flush() ; } catch (IOException ex) { exception(ex) ; } }
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException
    {
        output(CharBuffer.wrap(cbuf, off, len)) ;
    }
    
    @Override
    public void write(char[] cbuf) throws IOException
    {
        write(cbuf, 0, cbuf.length) ;
    }
    
//    @Override
//    public void write(String string, int off, int len) throws IOException
//    { }
    
    @Override
    public void write(String string) throws IOException
    { output(string) ; }
    
    @Override
    public void write(int ch) throws IOException
    { output(ch) ; } 
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