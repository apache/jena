/**
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

package org.openjena.riot;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.Iterator ;
import java.util.NoSuchElementException ;
import java.util.concurrent.ArrayBlockingQueue ;
import java.util.concurrent.BlockingQueue ;
import java.util.concurrent.CancellationException ;
import java.util.concurrent.TimeUnit ;

import org.openjena.atlas.AtlasException ;
import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.SinkToQueue ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.system.ParserProfile ;

/**
 * A wrapper that converts the RIOT parsing API from push to pull.  It does this by starting up a
 * thread that feeds results through a {@link BlockingQueue}.  You must call {@link #parse()} in
 * order to start the background thread parsing the InputStream.  You then use the iterator methods
 * to retrieve the statements.
 * <p/>
 * Note: You can avoid using this class if you are dealing with N-Triples or N-Quads, because RIOT
 * provides pull interfaces directly for those parsers.
 */
public abstract class RiotParsePuller<T> implements Iterator<T>, Closeable, LangRIOT
{
    private static final int QUEUE_CAPACITY = 1000;
    private static final int ITERATOR_POLL_TIMEOUT = 1000;  // one second
    private static final TimeUnit ITERATOR_POLL_TIMEUNIT = TimeUnit.MILLISECONDS;
    
    protected final InputStream in;
    protected final Lang lang;
    protected final String baseIRI;
    protected final LangRIOT parser;
    private final BlockingQueue<T> queue;
    
    @SuppressWarnings("unchecked")
    private final T endMarker = (T)new Object();
    
    private Thread readThread;
    private volatile RuntimeException uncaughtException;
    
    // For the iterator bit
    private boolean finished;
    private T slot;
    
    public RiotParsePuller(InputStream in, Lang lang, String baseIRI)
    {
        this.in = in;
        this.lang = lang;
        this.baseIRI = baseIRI;
        this.queue = new ArrayBlockingQueue<T>(QUEUE_CAPACITY);
        
        Sink<T> sink = new SinkToQueue<T>(queue) ;
        this.parser = createParser(sink);
    }
    
    @Override
    public Lang getLang()
    {
        return lang;
    }
    
    @Override
    public ParserProfile getProfile()
    {
        return parser.getProfile();
    }
    
    @Override
    public void setProfile(ParserProfile profile)
    {
        parser.setProfile(profile);
    }
    
    protected abstract LangRIOT createParser(Sink<T> sink);
    
    /**
     * Starts the background thread parsing the InputStream.  This method
     * returns immediately.  To retrieve the results, use the iterator methods.
     */
    @Override
    public void parse()
    {
        readThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                boolean noExceptions = true;
                try
                {
                    parser.parse();
                }
                catch (RuntimeException e)
                {
                    uncaughtException = e;
                    noExceptions = false;
                }
                finally
                {
                    if (noExceptions)
                    {
                        try
                        {
                            queue.put(endMarker);
                        }
                        catch (InterruptedException e)
                        {
                            // Someone cancelled right as we were trying to add the endMarker!
                            uncaughtException = new CancellationException();
                        }
                    }
                    
                    try
                    {
                        in.close();
                    }
                    catch (IOException e)
                    {
                        uncaughtException = new AtlasException("Error closing input stream", e);
                    }
                }
            }
        });
        readThread.start();
    }
    
    @Override
    public void close()
    {
        if (null != readThread)
        {
            readThread.interrupt();
        }
    }
    
    @Override
    public boolean hasNext()
    {
        if ( finished ) return false ;
        if ( slot != null ) return true ;
        while (true)
        {
            try
            {
                slot = queue.poll(ITERATOR_POLL_TIMEOUT, ITERATOR_POLL_TIMEUNIT);
            }
            catch (InterruptedException e)
            {
                // Someone wants us to finish I guess
                return false;
            }
            
            if (null != slot) break;
            
            // put this down here so we can drain as much as possible from the queue before rethrowing the exception
            if (null != uncaughtException)
            {
                finished = true;
                // Don't throw an exception if cancellation was requested 
                if (uncaughtException instanceof CancellationException)
                {
                    return false;
                }
                else
                {
                    throw uncaughtException;
                }
            }
        }
        
        if ( slot == endMarker )
        {
            finished = true ;
            slot = null ;
            return false ;
        }
        return true ;
    }

    @Override
    public T next()
    {
        if ( ! hasNext() )
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

    
}

