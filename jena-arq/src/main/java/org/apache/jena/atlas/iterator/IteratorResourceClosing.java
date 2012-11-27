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

package org.apache.jena.atlas.iterator;

import java.util.Iterator;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Closeable ;

/**
 * This iterator will automatically close a {@link org.apache.jena.atlas.lib.Closeable} or {@link java.io.Closeable}
 * resource when the iterator is exhausted.  Alternatively, the resource will be closed when {@link #close()} is
 * called.  An {@link AtlasException} will be thrown if access is attempted after {@link #close()} has been called.
 */
public class IteratorResourceClosing<T> implements Iterator<T>, Closeable
{
    private final Iterator<T> iter ;
    private final Object resource ;
    private boolean finished;
    
    public IteratorResourceClosing(Iterator<T> iter, Closeable resource)
    {
        this.iter = iter;
        this.resource = resource;
        this.finished = false;
    }
    
    public IteratorResourceClosing(Iterator<T> iter, java.io.Closeable resource)
    {
        this.iter = iter;
        this.resource = resource ;
        this.finished = false;
    }
    
    private void checkFinished()
    {
        if (finished) throw new AtlasException("IteratorResourceClosing is closed, no further operations can be performed on it.") ;
    }

    @Override
    public boolean hasNext()
    {
        if (finished)
            return false;
        
        boolean toReturn = iter.hasNext();
        
        // Clean up if we are done
        if (!toReturn)
        {
            close();
        }
        return toReturn ;
    }

    @Override
    public T next()
    {
        checkFinished();
        return iter.next() ;
    }

    @Override
    public void remove()
    {
        checkFinished();
        iter.remove() ;
    }

    @Override
    public void close()
    {
        if (!finished)
        {
            Iter.close(iter) ;
            if (null != resource)
            {
                if (resource instanceof Closeable)
                {
                    ((Closeable)resource).close() ;
                }
                else
                {
                    IO.close((java.io.Closeable)resource) ;
                }
            }
            finished = true;
        }
    }
}
