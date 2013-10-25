package com.hp.hpl.jena.tdb.base.file;

import java.nio.ByteBuffer ;

/**
 * Delegates to {@link ByteBuffer#allocateDirect(int)}.
 */
public class BufferAllocatorDirect implements BufferAllocator
{
    @Override
    public ByteBuffer allocate(int capacity)
    {
        return ByteBuffer.allocateDirect(capacity);
    }

    @Override
    public void clear()
    {
        // Do nothing
    }
    
    @Override
    public void close()
    {
        // Do nothing
    }

}
