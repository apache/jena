/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx.base;

import java.io.InputStream ;
import java.io.OutputStream ;
import java.nio.ByteBuffer ;
import java.util.Iterator ;


/** A file handler - everything TDB needs to do with files */
public interface FileHandle
{
    // Some ideas for centralizing all file operations to enable caching, management, closedown etc.
    
    // FileHandlerManager
    
    // Create from location , inc memory.
    //Location.mem
    
    public void getFileName() ;
    
    /* Sync to disk */
    public void sync();
    public void close();
    
    // Stream view - add buffering wrappers.
    static interface Stream {
        public InputStream getInputStream() ;
        public OutputStream getOutputStream() ;
    }
    
    
    static interface BlockStream
    {
        // Stream block view
        public BlockInputStream getInputBlockStream() ;
        public BlockOutputStream getOutputBlockStreamStream() ;
    }
    
    static interface BlockFile
    {
        // Random block view
        public Block get(int id) ;
        public Block put(int id, Block block) ;
        public Block free(int id) ;
    }
                     
    static interface BlockInputStream extends Iterator<Block>
    {
    }
    
    static interface BlockOutputStream 
    {
        public void write(Block block) ;
    }
    
    static class Block
    {
        // Header stuff
        // File - ptr to name
        // id - number / int
        // type - int. -1 means free.
        ByteBuffer data ;
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
