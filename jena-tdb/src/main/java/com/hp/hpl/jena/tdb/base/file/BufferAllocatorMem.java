package com.hp.hpl.jena.tdb.base.file;

import java.nio.ByteBuffer ;

/**
 * Delegates to {@link ByteBuffer#allocate(int)}.
 */
public class BufferAllocatorMem implements BufferAllocator
{
    @Override
    public ByteBuffer allocate(int capacity)
    {
        return ByteBuffer.allocate(capacity);
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
