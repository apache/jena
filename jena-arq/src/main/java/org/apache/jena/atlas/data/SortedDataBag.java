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

import java.io.File ;
import java.io.FileNotFoundException ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.OutputStream ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Comparator ;
import java.util.Iterator ;
import java.util.List ;
import java.util.NoSuchElementException ;
import java.util.PriorityQueue ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.IteratorResourceClosing ;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sink ;

/**
 * <p>
 * This data bag will gather items in memory until a size threshold is passed, at which point it will write
 * out all of the items to disk using the supplied serializer.
 * </p>
 * <p>
 * After adding is finished, call {@link #iterator()} to set up the data bag for reading back items and iterating over them.
 * The iterator will retrieve the items in sorted order using the supplied comparator.
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
 * data is sorted and written to disk.  An iterator will read in each file and perform a merge-sort as
 * the results are returned.
 * </p>
 */
public class SortedDataBag<E> extends AbstractDataBag<E>
{
    /**
     * The the maximum number of files to merge at the same time.  Without this, you can run out of file handles and other bad things.
     */
    protected static int MAX_SPILL_FILES = 100 ;
    
    protected final ThresholdPolicy<E> policy;
    protected final SerializationFactory<E> serializationFactory;
    protected final Comparator<? super E> comparator;
    
    protected boolean finishedAdding = false;
    protected boolean spilled = false;
    protected boolean closed = false;
    
    public SortedDataBag(ThresholdPolicy<E> policy, SerializationFactory<E> serializerFactory, Comparator<? super E> comparator)
    {
        this.policy = policy;
        this.serializationFactory = serializerFactory;
        this.comparator = comparator;
    }
    
    protected void checkClosed()
    {
        if (closed) throw new AtlasException("SortedDataBag is closed, no operations can be performed on it.") ;
    }
    
    @Override
    public boolean isSorted()
    {
        return true;
    }

    @Override
    public boolean isDistinct()
    {
        return false;
    }

    @Override
    public void add(E item)
    {
        checkClosed();
        if (finishedAdding)
            throw new AtlasException("SortedDataBag: Cannot add any more items after the writing phase is complete.");
        
        if (policy.isThresholdExceeded())
        {
            spill();
        }
        
        if (memory.add(item))
        {
            policy.increment(item);
            size++;
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void spill()
    {
        // Make sure we have something to spill.
        if (memory.size() > 0)
        {
            OutputStream out;
            try
            {
                out = getSpillStream();
            }
            catch (IOException e)
            {
                throw new AtlasException(e);
            }
            
            // Sort the tuples
            // Collections.sort() will copy to an array, sort, and then copy back.  Avoid that
            // extra copy by copying to an array and using Arrays.sort().  Also it lets us use
            // Collection<E> instead of List<E> as the type for the memory object.  Unfortunately
            // because of Java's crazy generics we have to do it as an Object array.
            Object[] array = memory.toArray();
            Arrays.sort(array, (Comparator)comparator);
            
            Sink<E> serializer = serializationFactory.createSerializer(out);
            try
            {
                for (Object tuple : array)
                {
                    serializer.send((E)tuple);
                }
            }
            finally
            {
                serializer.close();
            }
            
            spilled = true;
            policy.reset();
            memory.clear();
        }
    }

    @Override
    public void flush()
    {
        spill();
    }
    
    protected Iterator<E> getInputIterator(File spillFile) throws FileNotFoundException
    {
        InputStream in = getInputStream(spillFile);
        Iterator<E> deserializer = serializationFactory.createDeserializer(in) ;
        return new IteratorResourceClosing<>(deserializer, in);
    }

    /**
     * Returns an iterator over a set of elements of type E.  If you do not exhaust
     * the iterator, you should call {@link org.apache.jena.atlas.iterator.Iter#close(Iterator)}
     * to be sure any open file handles are closed.
     * 
     * @return an Iterator
     */
    @Override
	public Iterator<E> iterator()
    {
        preMerge();

        return iterator(getSpillFiles().size());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Iterator<E> iterator(int size)
    {
        checkClosed();
        
        int memSize = memory.size();
        
        // Constructing an iterator from this class is not thread-safe (just like all the the other methods)
        if (!finishedAdding && memSize > 1)
        {
            // Again, some ugliness for speed
            Object[] array = memory.toArray();
            Arrays.sort(array, (Comparator)comparator);
            memory = Arrays.asList((E[])array);
        }
        
        finishedAdding = true;
        
        if (spilled)
        {
            List<Iterator<E>> inputs = new ArrayList<>(size + (memSize > 0 ? 1 : 0));
                        
            if (memSize > 0)
            {
                inputs.add(memory.iterator());
            }
            
            for ( int i = 0; i < size; i++ )
            {
                File spillFile = getSpillFiles().get(i);
                try
                {
                    Iterator<E> irc = getInputIterator(spillFile);
                    inputs.add(irc);
                }
                catch (FileNotFoundException e)
                {
                    // Close any open streams before we throw an exception
                    for (Iterator<E> it : inputs)
                    {
                        Iter.close(it);
                    }
                    
                    throw new AtlasException("Cannot find one of the spill files", e);
                }
            }
            
            SpillSortIterator<E> ssi = new SpillSortIterator<>(inputs, comparator);
            registerCloseableIterator(ssi);
            
            return ssi;
        }
        else
        {
            if (memSize > 0)
            {
                return memory.iterator();
            }
            else
            {
                return Iter.nullIterator();
            }
        }
    }

    private void preMerge()
    {
        if (getSpillFiles() == null || getSpillFiles().size() <= MAX_SPILL_FILES)
        {
            return ;
        }

        try
        {
            while (getSpillFiles().size() > MAX_SPILL_FILES)
            {
                Sink<E> sink = serializationFactory.createSerializer(getSpillStream()) ;
                Iterator<E> ssi = iterator(MAX_SPILL_FILES) ;
                try
                {
                    while (ssi.hasNext())
                    {
                        sink.send(ssi.next()) ;
                    }
                }
                finally
                {
                    Iter.close(ssi) ;
                    sink.close() ;
                }

                List<File> toRemove = new ArrayList<>(MAX_SPILL_FILES) ;
                for (int i = 0; i < MAX_SPILL_FILES; i++)
                {
                    File file = getSpillFiles().get(i) ;
                    file.delete() ;
                    toRemove.add(file) ;
                }

                getSpillFiles().removeAll(toRemove) ;

                memory = new ArrayList<>() ;
            }
        }
        catch (IOException e)
        {
            throw new AtlasException(e) ;
        }
    }

    @Override
    public void close()
    {
        if (!closed)
        {
            closeIterators();
            deleteSpillFiles();
            
            memory = null;
            closed = true;
        }
    }
    
    /**
     * An iterator that handles getting the next tuple from the bag.
     */
    protected static class SpillSortIterator<T> implements Iterator<T>, Closeable
    {
        private final List<Iterator<T>> inputs;
        private final Comparator<? super T> comp;
        private final PriorityQueue<Item<T>> minHeap;
        
        public SpillSortIterator(List<Iterator<T>> inputs, Comparator<? super T> comp)
        {
            this.inputs = inputs;
            this.comp = comp;
            this.minHeap = new PriorityQueue<>(inputs.size());
            
            // Prime the heap
            for (int i=0; i<inputs.size(); i++)
            {
                replaceItem(i);
            }
        }
        
        private void replaceItem(int index)
        {
            Iterator<T> it = inputs.get(index);
            if (it.hasNext())
            {
                T tuple = it.next();
                minHeap.add(new Item<>(index, tuple, comp));
            }
        }

        @Override
        public boolean hasNext()
        {
            return (minHeap.peek() != null);
        }

        @Override
        public T next()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }
            
            Item<T> curr = minHeap.poll();
            // Read replacement item
            replaceItem(curr.getIndex());
            
            return curr.getTuple();
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("SpillSortIterator.remove");
        }

        @Override
        public void close()
        {
            for (Iterator<T> it : inputs)
            {
                Iter.close(it);
            }
        }
        
        private final class Item<U> implements Comparable<Item<U>>
        {
            private final int index;
            private final U tuple;
            private final Comparator<? super U> c;
            
            public Item(int index, U tuple, Comparator<? super U> c)
            {
                this.index = index;
                this.tuple = tuple;
                this.c = c;
            }
            
            public int getIndex()
            {
                return index;
            }
            
            public U getTuple()
            {
                return tuple;
            }
            
            @Override
            @SuppressWarnings("unchecked")
            public int compareTo(Item<U> o)
            {
                return (null != c) ? c.compare(tuple, o.getTuple()) : ((Comparable<U>)tuple).compareTo(o.getTuple());
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public boolean equals(Object obj)
            {
                if (obj instanceof Item)
                {
                    return compareTo((Item<U>)obj) == 0;
                }
                
                return false;
            }
            
            @Override
            public int hashCode()
            {
                return tuple.hashCode();
            }
        }
        
    }

}
