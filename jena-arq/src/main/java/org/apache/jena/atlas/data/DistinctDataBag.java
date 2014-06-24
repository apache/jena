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

import java.util.Comparator ;
import java.util.HashSet ;
import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.PeekIterator ;
import org.apache.jena.atlas.lib.Closeable ;

/**
 * <p>
 * This data bag will gather distinct items in memory until a size threshold is passed, at which point it will write
 * out all of the items to disk using the supplied serializer.
 * </p>
 * <p>
 * After adding is finished, call {@link #iterator()} to set up the data bag for reading back items and iterating over them.
 * The iterator will retrieve only distinct items.
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
 * Implementation Notes: Data is stored without duplicates as it comes in in a HashSet.  When it is time to spill,
 * that data is sorted and written to disk.  An iterator that eliminates adjacent duplicates is used in conjunction
 * with the SortedDataBag's iterator.
 * </p>
 */
public class DistinctDataBag<E> extends SortedDataBag<E>
{
    public DistinctDataBag(ThresholdPolicy<E> policy, SerializationFactory<E> serializerFactory, Comparator<E> comparator)
    {
        super(policy, serializerFactory, comparator);
        this.memory = new HashSet<>();
    }
    
    @Override
    public boolean isSorted()
    {
        // The bag may not be sorted if we havn't spilled
        return false;
    }

    @Override
    public boolean isDistinct()
    {
        return true;
    }

    @Override
    public Iterator<E> iterator()
    {
        // We could just return super.iterator() in all cases,
        // but no need to waste time sorting if we havn't spilled
        if (!spilled)
        {
            checkClosed();
            finishedAdding = true;
            
            if (memory.size() > 0)
            {
                return memory.iterator();
            }
            else
            {
                return Iter.nullIterator();
            }
        }
        else
        {
            return new DistinctReducedIterator<>(super.iterator());
        }
    }
    
    protected static class DistinctReducedIterator<T> extends PeekIterator<T> implements Closeable
    {
        private Iterator<T> iter;
        
        public DistinctReducedIterator(Iterator<T> iter)
        {
            super(iter);
            this.iter = iter;
        }
        
        @Override
        public T next()
        {
            T item = super.next();
            
            // Keep going until as long as the next item is the same as the current one
            while (hasNext() && ((null == item && null == peek()) || (null != item && item.equals(peek()))))
            {
                item = super.next();
            }
            
            return item;
        }
        
        @Override
        public void close()
        {
            Iter.close(iter);
        }
        
    }

}
