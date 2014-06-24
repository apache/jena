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

package org.apache.jena.atlas.data;

import java.io.BufferedInputStream ;
import java.io.File ;
import java.io.FileInputStream ;
import java.io.FileNotFoundException ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.OutputStream ;
import java.util.Iterator ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.IteratorResourceClosing ;
import org.apache.jena.atlas.lib.Sink ;

/**
 * <p>
 * This data bag will gather items in memory until a size threshold is passed, at which point it will write
 * out all of the items to disk using the supplied serializer.
 * </p>
 * <p>
 * After adding is finished, call {@link #iterator()} to set up the data bag for reading back items and iterating over them.
 * </p>
 * <p>
 * IMPORTANT: You may not add any more items after this call.  You may subsequently call {@link #iterator()} multiple
 * times which will give you a new iterator for each invocation.  If you do not consume the entire iterator, you should
 * call {@link Iter#close(Iterator)} to close any FileInputStreams associated with the iterator.
 * </p>
 * <p>
 * Additionally, make sure to call {@link #close()} when you are finished to free any system resources (preferably in a finally block).
 * </p>
 * <p>
 * Implementation Notes: Data is stored in an ArrayList as it comes in.  When it is time to spill, that
 * data is written to disk.  Additional data is appended directly to that file for the rest of the add phase.
 * Creating an iterator will read all the data out of that single file.
 * </p>
 */
public class DefaultDataBag<E> extends AbstractDataBag<E>
{
    private final ThresholdPolicy<E> policy;
    private final SerializationFactory<E> serializationFactory;
    
    protected boolean finishedAdding = false;
    protected boolean spilled = false;
    protected boolean closed = false;
    
    private Sink<E> serializer;
    private OutputStream out;
    
    public DefaultDataBag(ThresholdPolicy<E> policy, SerializationFactory<E> serializerFactory)
    {
        this.policy = policy;
        this.serializationFactory = serializerFactory;
    }
    
    private void checkClosed()
    {
        if (closed) throw new AtlasException("DefaultDataBag is closed, no operations can be performed on it.") ;
    }
    
    @Override
    public void add(E item)
    {
        checkClosed();
        if (finishedAdding)
            throw new AtlasException("DefaultDataBag: Cannot add any more items after the writing phase is complete.");
        
        if (!policy.isThresholdExceeded())
        {
            memory.add(item);
        }
        else
        {
            if (!spilled)
            {
                spill();
                spilled = true;
            }
            
            // Write to disk
            serializer.send(item);
        }
        
        policy.increment(item);
        size++;
    }
    
    private void spill()
    {
        // In the case where we've just hit the threshold, set up the serializer and transfer all existing content to disk.
        // This makes the logic a little simpler, and also prevents us from using what may be a fair amount of memory for
        // a prolonged period of time.
        try
        {
            out = getSpillStream();
        }
        catch (IOException e)
        {
            throw new AtlasException(e);
        }
        serializer = serializationFactory.createSerializer(out);
        
        for (E e : memory)
        {
            serializer.send(e);
        }
        memory = null;
    }
    
    @Override
    public boolean isSorted()
    {
        return false;
    }

    @Override
    public boolean isDistinct()
    {
        return false;
    }
    
    @Override
    public void flush()
    {
        if (policy.isThresholdExceeded() && (null != serializer))
        {
            serializer.flush();
        }
    }
    
    @Override
    public Iterator<E> iterator()
    {
        Iterator<E> toReturn;
        
        checkClosed();
        
        // Close the writer
        closeWriter();
        
        // Create a new reader
        if (policy.isThresholdExceeded())
        {
            File spillFile = getSpillFiles().get(0);
            
            InputStream in;
            try
            {
                in = new BufferedInputStream(new FileInputStream(spillFile)) ;
            }
            catch ( FileNotFoundException ex )
            {
                throw new AtlasException(ex) ;
            }
            Iterator<E> deserializer = serializationFactory.createDeserializer(in) ;
            IteratorResourceClosing<E> irc = new IteratorResourceClosing<>(deserializer, in) ;
            registerCloseableIterator(irc);
            toReturn = irc;
        }
        else
        {
            toReturn = memory.iterator();
        }
        
        return toReturn;
    }
    
    protected void closeWriter()
    {
        if (!finishedAdding)
        {
            if (policy.isThresholdExceeded())
            {
                // It is possible for "serializer" and "out" to be null even if the policy is exceeded.
                // This can happen if nothing was ever added (i.e. a zero count policy)
                if (null != serializer)
                {
                    serializer.close();
                }
                if (null != out)
                {
                    IO.close(out);
                }
            }
            finishedAdding = true;
        }
    }
    
    @Override
    public void close()
    {
        if (!closed)
        {
            closeWriter();
            closeIterators();
            deleteSpillFiles();
            
            memory = null;
            closed = true;
        }
    }

}
