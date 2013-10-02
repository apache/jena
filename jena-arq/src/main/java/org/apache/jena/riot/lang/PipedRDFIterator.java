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

package org.apache.jena.riot.lang;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;

/**
 * A {@code PipedRDFIterator} should be connected to a {@link PipedRDFStream}
 * implementation; the piped iterator then provides whatever RDF primitives are
 * written to the {@code PipedRDFStream}. Typically, data is read from a
 * {@code PipedRDFIterator} by one thread and data is written to the
 * corresponding {@code PipedRDFStream} by some other thread. Attempting to use
 * both objects from a single thread is not recommended, as it may deadlock the
 * thread. The {@code PipedRDFIterator} contains a buffer, decoupling read
 * operations from write operations, within limits.
 * <p/>
 * Inspired by Java's {@link java.io.PipedInputStream} and
 * {@link java.io.PipedOutputStream}
 * 
 * @param <T>
 *            The type of the RDF primitive, should be one of {@code Triple},
 *            {@code Quad}, or {@code Tuple<Node>}
 * 
 * @see PipedTriplesStream
 * @see PipedQuadsStream
 * @see PipedTuplesStream
 */
public class PipedRDFIterator<T> implements Iterator<T>, Closeable {
    /**
     * Constant for default buffer size
     */
    public static final int DEFAULT_BUFFER_SIZE = 10000;

    /**
     * Constant for default poll timeout in milliseconds, used to stop the
     * consumer deadlocking in certain circumstances
     */
    public static final int DEFAULT_POLL_TIMEOUT = 1000; // one second
    /**
     * Constant for max number of failed poll attempts before the producer will
     * be declared as dead
     */
    public static final int DEFAULT_MAX_POLLS = 10;

    private final BlockingQueue<T> queue;

    @SuppressWarnings("unchecked")
    private final T endMarker = (T) new Object();

    private volatile boolean closedByReader = false;
    private volatile boolean closedByWriter = false;
    private volatile boolean finished = false;
    private volatile boolean threadReused = false;
    private volatile Thread readSide;
    private volatile Thread writeSide;

    private boolean connected = false;
    private int pollTimeout = DEFAULT_POLL_TIMEOUT;
    private int maxPolls = DEFAULT_MAX_POLLS;

    private T slot;

    private final Object lock = new Object(); // protects baseIri and prefixes
    private String baseIri;
    private final PrefixMap prefixes = PrefixMapFactory.createForInput();

    /**
     * Creates a new piped RDF iterator with the default buffer size of
     * {@code DEFAULT_BUFFER_SIZE}.
     * <p>
     * Buffer size must be chosen carefully in order to avoid performance
     * problems, if you set the buffer size too low you will experience a lot of
     * blocked calls so it will take longer to consume the data from the
     * iterator. For best performance the buffer size should be at least 10% of
     * the expected input size though you may need to tune this depending on how
     * fast your consumer thread is.
     * </p>
     */
    public PipedRDFIterator() {
        this(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates a new piped RDF iterator
     * <p>
     * Buffer size must be chosen carefully in order to avoid performance
     * problems, if you set the buffer size too low you will experience a lot of
     * blocked calls so it will take longer to consume the data from the
     * iterator. For best performance the buffer size should be roughly 10% of
     * the expected input size though you may need to tune this depending on how
     * fast your consumer thread is.
     * </p>
     * 
     * @param bufferSize
     *            Buffer size
     */
    public PipedRDFIterator(int bufferSize) {
        this(bufferSize, false, DEFAULT_POLL_TIMEOUT, DEFAULT_MAX_POLLS);
    }

    /**
     * Creates a new piped RDF iterator
     * <p>
     * Buffer size must be chosen carefully in order to avoid performance
     * problems, if you set the buffer size too low you will experience a lot of
     * blocked calls so it will take longer to consume the data from the
     * iterator. For best performance the buffer size should be roughly 10% of
     * the expected input size though you may need to tune this depending on how
     * fast your consumer thread is.
     * </p>
     * <p>
     * The fair parameter controls whether the locking policy used for the
     * buffer is fair. When enabled this reduces throughput but also reduces the
     * chance of thread starvation. This likely need only be set to {@code true}
     * if there will be multiple consumers.
     * </p>
     * 
     * @param bufferSize
     *            Buffer size
     * @param fair
     *            Whether the buffer should use a fair locking policy
     */
    public PipedRDFIterator(int bufferSize, boolean fair) {
        this(bufferSize, fair, DEFAULT_POLL_TIMEOUT, DEFAULT_MAX_POLLS);
    }

    /**
     * Creates a new piped RDF iterator
     * <p>
     * Buffer size must be chosen carefully in order to avoid performance
     * problems, if you set the buffer size too low you will experience a lot of
     * blocked calls so it will take longer to consume the data from the
     * iterator. For best performance the buffer size should be roughly 10% of
     * the expected input size though you may need to tune this depending on how
     * fast your consumer thread is.
     * </p>
     * <p>
     * The {@code fair} parameter controls whether the locking policy used for
     * the buffer is fair. When enabled this reduces throughput but also reduces
     * the chance of thread starvation. This likely need only be set to
     * {@code true} if there will be multiple consumers.
     * </p>
     * <p>
     * The {@code pollTimeout} parameter controls how long each poll attempt
     * waits for data to be produced. This prevents the consumer thread from
     * blocking indefinitely and allows it to detect various potential deadlock
     * conditions e.g. dead producer thread, another consumer closed the
     * iterator etc. and errors out accordingly. It is unlikely that you will ever
     * need to adjust this from the default value provided by
     * {@link #DEFAULT_POLL_TIMEOUT}.
     * </p>
     * <p>
     * The {@code maxPolls} parameter controls how many poll attempts will be
     * made by a single consumer thread within the context of a single call to
     * {@link #hasNext()} before the iterator declares the producer to be dead
     * and errors out accordingly. You may need to adjust this if you have a slow
     * producer thread or many consumer threads.
     * </p>
     * 
     * @param bufferSize
     *            Buffer size
     * @param fair
     *            Whether the buffer should use a fair locking policy
     * @param pollTimeout
     *            Poll timeout in milliseconds
     * @param maxPolls
     *            Max poll attempts
     */
    public PipedRDFIterator(int bufferSize, boolean fair, int pollTimeout, int maxPolls) {
        if (pollTimeout <= 0)
            throw new IllegalArgumentException("Poll Timeout must be > 0");
        if (maxPolls <= 0)
            throw new IllegalArgumentException("Max Poll attempts must be > 0");
        this.queue = new ArrayBlockingQueue<T>(bufferSize, fair);
        this.pollTimeout = pollTimeout;
        this.maxPolls = maxPolls;
    }

    @Override
    public boolean hasNext() {
        if (!connected)
            throw new IllegalStateException("Pipe not connected");

        if (closedByReader)
            throw new RiotException("Pipe closed");

        if (finished)
            return false;

        readSide = Thread.currentThread();

        // Depending on how code schedules the threads involved there is a
        // scenario that exists where a writer can finish/die before the reader
        // is started and the reader is scheduled onto the same thread thus
        // resulting in a deadlock on the consumer because it will never be able
        // to detect that the writer died
        // In this scenario we need to set a special flag to indicate the
        // possibility
        if (writeSide != null && writeSide == readSide)
            threadReused = true;

        if (slot != null)
            return true;
        
        int attempts = 0;
        while (true) {
            attempts++;
            try {
                slot = queue.poll(DEFAULT_POLL_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new CancellationException();
            }

            if (null != slot)
                break;

            // If the producer thread died and did not call finish() then
            // declare this pipe to be "broken"
            // Since check is after the break, we will drain as much as possible
            // out of the queue before throwing this exception
            if (threadReused || (writeSide != null && !writeSide.isAlive() && !closedByWriter)) {
                closedByReader = true;
                throw new RiotException("Write end dead");
            }

            // Need to check this inside the loop as otherwise outside code that
            // attempts to break the deadlock by causing close() on the iterator
            // cannot do so
            if (closedByReader)
                throw new RiotException("Pipe closed");
            
            // Need to check whether polling attempts have been exceeded
            // If so declare the writer dead and exit
            if (attempts >= this.maxPolls) {
                closedByReader = true;
                if (writeSide != null) {
                    throw new RiotException("Write end failed to produce any data within the specified number of polling attempts, declaring write end dead");
                } else {
                    throw new RiotException("Write end failed to ever call start(), declaring write end dead");
                }
            }
        }

        // When the end marker is seen set slot to null
        if (slot == endMarker) {
            finished = true;
            slot = null;
            return false;
        }
        return true;
    }

    @Override
    public T next() {
        if (!hasNext())
            throw new NoSuchElementException();
        T item = slot;
        slot = null;
        return item;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void checkStateForReceive() {
        if (closedByWriter || closedByReader) {
            throw new RiotException("Pipe closed");
        } else if (readSide != null && !readSide.isAlive()) {
            throw new RiotException("Read end dead");
        }
    }

    protected void connect() {
        this.connected = true;
    }

    protected void receive(T t) {
        checkStateForReceive();
        writeSide = Thread.currentThread();

        try {
            queue.put(t);
        } catch (InterruptedException e) {
            throw new CancellationException();
        }
    }

    protected void base(String base) {
        synchronized (lock) {
            this.baseIri = base;
        }
    }

    /**
     * Gets the most recently seen Base IRI
     * 
     * @return Base IRI
     */
    public String getBaseIri() {
        synchronized (lock) {
            return baseIri;
        }
    }

    protected void prefix(String prefix, String iri) {
        synchronized (lock) {
            prefixes.add(prefix, iri);
        }
    }

    /**
     * Gets the prefix map which contains the prefixes seen so far in the stream
     * 
     * @return Prefix Map
     */
    public PrefixMap getPrefixes() {
        synchronized (lock) {
            // Need to return a copy since PrefixMap is not concurrent
            return PrefixMapFactory.create(this.prefixes);
        }
    }

    protected void start() {
        // Track the writer thread in case it never delivers us anything and
        // dies before calling finish
        writeSide = Thread.currentThread();
    }

    // Called by the producer
    protected void finish() {
        receive(endMarker);
        closedByWriter = true;
    }

    // Called by the consumer
    @Override
    public void close() {
        closedByReader = true;
    }
}
