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

package org.apache.jena.riot.lang ;

import java.util.Iterator ;
import java.util.NoSuchElementException ;
import java.util.concurrent.ArrayBlockingQueue ;
import java.util.concurrent.BlockingQueue ;
import java.util.concurrent.CancellationException ;
import java.util.concurrent.TimeUnit ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.LightweightPrefixMap ;
import org.apache.jena.riot.system.PrefixMap ;

/**
 * A {@code PipedRDFIterator} should be connected to a {@link PipedRDFStream} implementation;
 * the piped iterator then provides whatever RDF primitives are written to the {@code PipedRDFStream}.
 * Typically, data is read from a {@code PipedRDFIterator} by one thread and data is written
 * to the corresponding {@code PipedRDFStream} by some other thread.  Attempting to use both
 * objects from a single thread is not recommended, as it may deadlock the thread.
 * The {@code PipedRDFIterator} contains a buffer, decoupling read operations from write operations,
 * within limits.
 * <p/>
 * Inspired by Java's {@link java.io.PipedInputStream} and {@link java.io.PipedOutputStream}
 * 
 * @param <T> The type of the RDF primitive, should be one of {@code Triple}, {@code Quad}, or {@code Tuple<Node>}
 * 
 * @see PipedTriplesStream
 * @see PipedQuadsStream
 * @see PipedTuplesStream
 */
public class PipedRDFIterator<T> implements Iterator<T>, Closeable
{
    /**
     * Constant for default buffer size
     */
    public static final int DEFAULT_BUFFER_SIZE = 10000 ;
    
    private static final int ITERATOR_POLL_TIMEOUT = 1000 ; // one second
    private static final TimeUnit ITERATOR_POLL_TIMEUNIT = TimeUnit.MILLISECONDS ;

    private final BlockingQueue<T> queue ;

    @SuppressWarnings("unchecked")
    private final T endMarker = (T) new Object() ;

    private volatile boolean closedByReader = false ;
    private volatile boolean closedByWriter = false ;
    private volatile boolean finished = false;
    private volatile Thread readSide ;
    private volatile Thread writeSide ;
    
    private boolean connected = false ;

    private T slot ;

    private final Object lock = new Object() ; // protects baseIri and prefixes
    private String baseIri ;
    private final LightweightPrefixMap prefixes = new PrefixMap() ;


    /**
     * Creates a new piped RDF iterator with the default buffer size of {@code DEFAULT_BUFFER_SIZE}.
     * <p>
     * Buffer size must be chosen carefully in order to avoid performance
     * problems, if you set the buffer size too low you will experience a lot of
     * blocked calls so it will take longer to consume the data from the
     * iterator. For best performance the buffer size should be at least 10% of
     * the expected input size though you may need to tune this depending on how
     * fast your consumer thread is.
     * </p>
     */
    public PipedRDFIterator()
    {
        this(DEFAULT_BUFFER_SIZE);
    }
    
    /**
     * Creates a new piped RDF iterator
     * <p>
     * Buffer size must be chosen carefully in order to avoid performance
     * problems, if you set the buffer size too low you will experience a lot of
     * blocked calls so it will take longer to consume the data from the
     * iterator. For best performance the buffer size should be at least 10% of
     * the expected input size though you may need to tune this depending on how
     * fast your consumer thread is.
     * </p>
     * 
     * @param bufferSize
     *            Buffer size
     */
    public PipedRDFIterator(int bufferSize)
    {
        this.queue = new ArrayBlockingQueue<T>(bufferSize) ;
    }

    /**
     * Creates a new piped RDF iterator
     * <p>
     * Buffer size must be chosen carefully in order to avoid performance
     * problems, if you set the buffer size too low you will experience a lot of
     * blocked calls so it will take longer to consume the data from the
     * iterator. For best performance the buffer size should be at least 10% of
     * the expected input size though you may need to tune this depending on how
     * fast your consumer thread is.
     * </p>
     * <p>
     * The fair parameter controls whether the locking policy used for the
     * buffer is fair. When enabled this reduces throughput but also reduces the
     * chance of thread starvation.
     * </p>
     * 
     * @param bufferSize
     *            Buffer size
     * @param fair
     *            Whether the buffer should use a fair locking policy
     */
    public PipedRDFIterator(int bufferSize, boolean fair)
    {
        this.queue = new ArrayBlockingQueue<T>(bufferSize, fair) ;
    }

    @Override
    public boolean hasNext()
    {
        if (!connected)
            throw new IllegalStateException("Pipe not connected");
        
        if (closedByReader)
            throw new RiotException("Pipe closed");
        
        if (finished)
            return false;
        
        readSide = Thread.currentThread();
        
        if (slot != null)
            return true ;
        while (true)
        {
            try
            {
                slot = queue.poll(ITERATOR_POLL_TIMEOUT, ITERATOR_POLL_TIMEUNIT) ;
            }
            catch (InterruptedException e)
            {
                throw new CancellationException() ;
            }

            if (null != slot)
                break ;
            
            // If the producer thread died and did not call finish() then declare this pipe to be "broken"
            // Since check is after the break, we will drain as much as possible out of the queue before throwing this exception
            if (writeSide != null && !writeSide.isAlive() && !closedByWriter)
            {
                closedByReader = true ;
                throw new RiotException("Write end dead") ;
            }
        }

        // When the end marker is seen set slot to null
        if (slot == endMarker)
        {
            finished = true;
            slot = null ;
            return false ;
        }
        return true ;
    }

    @Override
    public T next()
    {
        if (!hasNext())
            throw new NoSuchElementException() ;
        T item = slot ;
        slot = null ;
        return item ;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException() ;
    }
    
    private void checkStateForReceive()
    {
        if (closedByWriter || closedByReader)
        {
            throw new RiotException("Pipe closed") ;
        }
        else if (readSide != null && !readSide.isAlive())
        {
            throw new RiotException("Read end dead") ;
        }
    }
    
    protected void connect()
    {
        this.connected = true;
    }

    protected void receive(T t)
    {
        checkStateForReceive();
        writeSide = Thread.currentThread() ;

        try
        {
            queue.put(t) ;
        }
        catch (InterruptedException e)
        {
            throw new CancellationException() ;
        }
    }

    protected void base(String base)
    {
        synchronized (lock)
        {
            this.baseIri = base ;
        }
    }

    /**
     * Gets the most recently seen Base IRI
     * 
     * @return Base IRI
     */
    public String getBaseIri()
    {
        synchronized (lock)
        {
            return baseIri ;
        }
    }

    protected void prefix(String prefix, String iri)
    {
        synchronized (lock)
        {
            prefixes.add(prefix, iri) ;
        }
    }

    /**
     * Gets the prefix map which contains the prefixes seen so far in the stream
     * 
     * @return Prefix Map
     */
    public LightweightPrefixMap getPrefixes()
    {
        synchronized (lock)
        {
            // Need to return a copy since PrefixMap is not concurrent
            return new PrefixMap(prefixes) ;
        }
    }

    protected void start()
    {
        // Do nothing
    }

    // Called by the producer
    protected void finish()
    {
        receive(endMarker);
        closedByWriter = true;
    }

    // Called by the consumer
    @Override
    public void close()
    {
        closedByReader = true ;
    }
}
