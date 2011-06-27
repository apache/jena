/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx.base;

import com.hp.hpl.jena.tdb.sys.FileRef ;

final
public class BlockRef
{
    private final FileRef file ;
    private final long blockId ;
    
    static public BlockRef create(FileRef file, long blockId)    { return new BlockRef(file, blockId) ; }
    //static public BlockRef create(String file, Integer blockId)     { return new BlockRef(file, blockId) ; }

    private BlockRef(String file, long blockId)
    {
        this(FileRef.create(file), blockId) ;
    }
    
    private BlockRef(FileRef file, long blockId)
    {
        this.file = file ;
        this.blockId = blockId ;
    }
    
    public FileRef getFile()        { return file ; }

    public int getFileId()          { return getFile().getId() ; }

    public long getBlockId()         { return blockId ; }

    @Override
    public int hashCode()
    {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + (int)(blockId ^ (blockId >>> 32)) ;
        result = prime * result + ((file == null) ? 0 : file.hashCode()) ;
        return result ;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true ;
        if (obj == null) return false ;
        if (getClass() != obj.getClass()) return false ;
        BlockRef other = (BlockRef)obj ;
        if (blockId != other.blockId) return false ;
        if (file == null)
        {
            if (other.file != null) return false ;
        } else
            if (!file.equals(other.file)) return false ;
        return true ;
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