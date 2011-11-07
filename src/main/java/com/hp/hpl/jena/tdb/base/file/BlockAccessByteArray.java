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

package com.hp.hpl.jena.tdb.base.file;

import java.nio.ByteBuffer ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfInt ;

/** 
 *  FileAccess interface backed by a byte array.
 */
public class BlockAccessByteArray implements BlockAccess
{
    private ByteBuffer bytes ;
    private long length ;           // Bytes in use: 0 to length-1 
    private long alloc ;            // Bytes allocated
    private final String label ;
    
    public BlockAccessByteArray(String label)
    {
        bytes = ByteBuffer.allocate(1024) ;
        length = 0 ; 
        alloc = 0 ;
        this.label = label ;
    }
    
    @Override
    public String getLabel () { return label ; }
    
    @Override
    public Block allocate(int size)
    {
        long addr = alloc ;
        ByteBuffer bb = ByteBuffer.allocate(size) ;
        alloc += (size + SizeOfInt) ;
        return new Block((int)addr, bb) ; 
    }

    @Override
    public Block read(long id)
    {
        // Variable length blocks.
        if ( id < 0 || id >= length || id >= bytes.capacity() )
            throw new FileException("Bad id (read): "+id) ;
        bytes.position((int)id) ;
        int len = bytes.getInt() ;
        ByteBuffer bb = ByteBuffer.allocate(len) ;
        // Copy out the bytes - copy for safety.
        bytes.get(bb.array(), 0, len) ;
        return new Block(id, bb) ; 
    }

    @Override
    public void write(Block block)
    {
        // Variable length blocks.
        long loc = block.getId() ;
        if ( loc < 0 || loc > length )  // Can be equal => append.
            throw new FileException("Bad id (write): "+loc+" ("+alloc+","+length+")") ;
        ByteBuffer bb = block.getByteBuffer() ; 
        int len = bb.capacity() ;
        
        if ( loc == length )
        {
            if ( bytes.capacity()-length < len )
            {
                int cap2 = bytes.capacity()+1024 ;
                while(bytes.capacity()-length < len)
                    cap2 += 1024 ; 
                
                ByteBuffer bytes2 = ByteBuffer.allocate(cap2) ;
                bytes2.position(0) ;
                bytes2.put(bytes) ;
            }
            length += len +SizeOfInt ;
        }
        bytes.position((int)loc) ;
        bytes.putInt(len) ;
        bytes.put(bb.array(), 0, bb.capacity()) ;
    }
    
    @Override
    public void overwrite(Block block)
    {
        write(block) ;
    }    

    @Override
    public boolean isEmpty()
    {
        return length == 0  ;
    }

    @Override
    public boolean valid(long id)
    {
        return ( id >= 0 && id < length ) ;
    }

    @Override
    public void sync()
    {}

    @Override
    public void close()
    {}
}
