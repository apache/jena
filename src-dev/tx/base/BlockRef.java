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
