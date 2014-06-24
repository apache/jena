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

package org.apache.jena.atlas.data;

import java.io.File ;
import java.io.FileNotFoundException ;
import java.util.ArrayList ;
import java.util.Comparator ;
import java.util.Iterator ;
import java.util.List ;
import java.util.NoSuchElementException ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.FileOps ;


/**
 * This class is like {@link DistinctDataBag} except that you are informed if the item you just
 * added was known to be distinct.  This will normally only work until the first spill.  After that,
 * the system may not be able to tell for sure, and will thus return false.  When you are finished
 * adding items, you may call {@link #netIterator()} to get any distinct items that are in the
 * spill files but were not indicated as distinct previously.  This is useful for a distinct
 * operator that streams results until it exceeds the spill threshold.
 */
public class DistinctDataNet<E> extends DistinctDataBag<E>
{
    protected File firstSpillFile;
    
    public DistinctDataNet(ThresholdPolicy<E> policy, SerializationFactory<E> serializerFactory, Comparator<E> comparator)
    {
        super(policy, serializerFactory, comparator) ;
    }
    
    /**
     * @return true if the item added is known to be distinct.
     */
    public boolean netAdd(E item)
    {
        long s = size ;
        super.add(item) ;
        return !spilled && size > s ;
    }
    
    @Override
    protected void registerSpillFile(File spillFile)
    {
        // If this is the first time spilling, then keep this spill file separate
        if (!spilled)
        {
            firstSpillFile = spillFile;
        }
        else
        {
            super.registerSpillFile(spillFile);
        }
    }
    
    @Override
    protected void deleteSpillFiles()
    {
        super.deleteSpillFiles();
        if (null != firstSpillFile)
        {
            FileOps.delete(firstSpillFile, false);
            firstSpillFile = null;
        }
    }
    
    // Used by the .iterator() method
    @Override
    protected List<File> getSpillFiles()
    {
        List<File> toReturn = new ArrayList<>(super.getSpillFiles());
        if (null != firstSpillFile)
        {
            toReturn.add(firstSpillFile);
        }
        return toReturn;
    }
    
    // TODO: Will be used by the .netIterator() method
    protected List<File> getNetSpillFiles()
    {
        return super.getSpillFiles();
    }
    
    /**
     * Returns an iterator to all additional items that are distinct but were
     * not reported to be so at the time {@link #netAdd(Object)} was invoked.
     * <p/>
     * If you do not exhaust the iterator, you should call {@link org.apache.jena.atlas.iterator.Iter#close(Iterator)}
     * to be sure any open file handles are closed.
     */
    public Iterator<E> netIterator()
    {
        // If we havn't spilled, then we have already indicated all distinct values via .netAdd()
        if (!spilled)
        {
            return Iter.nullIter();
        }
        
        Iterator<E> blacklist;
        try
        {
            blacklist = getInputIterator(firstSpillFile);
        }
        catch ( FileNotFoundException e )
        {
            throw new AtlasException("Cannot find the first spill file", e);
        }
        
        // TODO: Improve performance by making the superclass .iterator() use getNetSpillFiles()
        // instead of getSpillFiles() so it doesn't contain the contents of the first file
        Iterator<E> rest = super.iterator();
        
        SortedDiffIterator<E> sdi = SortedDiffIterator.create(rest, blacklist, comparator);
        registerCloseableIterator(sdi);
        
        return sdi;
    }
    
    /**
     * Produces the set difference of two sorted set sequences.
     */
    protected static class SortedDiffIterator<T> implements Iterator<T>, Closeable
    {
        private final Iterator<T> grayList;
        private final Iterator<T> blackList;
        private final Comparator<? super T> comp;
        
        private boolean finished = false;
        private boolean blackSlotFull = false;
        private T white;
        private T black;
        
        /**
         * Produces the set difference of two sorted set sequences using the natural ordering of the items
         * (null items will always be considered less than any other items).
         * 
         * @param first An Iterator&lt;T&gt; whose elements that are not also in second will be returned.
         * @param second An Iterator&lt;T&gt; whose elements that also occur in the first sequence will cause those elements to be removed from the returned sequence. 
         */
        public static <S extends Comparable<? super S>> SortedDiffIterator<S> create(Iterator<S> first, Iterator<S> second)
        {
            return create(first, second, new Comparator<S>()
            {
                @Override
                public int compare(S o1, S o2)
                {
                    if (null == o1 && null == o2) return 0;
                    if (null == o1) return -1;
                    if (null == o2) return 1;
                    return o1.compareTo(o2);
                }
            });
        }
        
        /**
         * Produces the set difference of two sorted set sequences using the specified comparator.
         * 
         * @param first An Iterator&lt;T&gt; whose elements that are not also in second will be returned.
         * @param second An Iterator&lt;T&gt; whose elements that also occur in the first sequence will cause those elements to be removed from the returned sequence.
         * @param comparator The comparator used to compare the elements from each iterator. 
         */
        public static <S> SortedDiffIterator<S> create(Iterator<S> first, Iterator<S> second, Comparator<? super S> comparator)
        {
            return new SortedDiffIterator<>(first, second, comparator);
        }
        
        
        private SortedDiffIterator(Iterator<T> first, Iterator<T> second, Comparator<? super T> comparator)
        {
            this.grayList = first;
            this.blackList = second;
            this.comp = comparator;
            
            // Prime the white item
            fill();
        }
        
        private void fill()
        {
            if (finished) return;
            
            if (!grayList.hasNext())
            {
                close();
                return;
            }
            
            if (!blackSlotFull)
            {
                if (!blackList.hasNext())
                {
                    white = grayList.next();
                    return;
                }
                
                black = blackList.next();
                blackSlotFull = true;
            }
            
            // Outer loop advances white
            while (true)
            {
                if (!grayList.hasNext())
                {
                    close();
                    return;
                }
                white = grayList.next();
                
                int cmp = comp.compare(white, black);
                
                if (cmp < 0) return;
                
                // Inner loop advances black until white is less than or equal to it
                while (cmp > 0)
                {
                    if (!blackList.hasNext())
                    {
                        black = null;
                        blackSlotFull = false;
                        return;
                    }
                    black = blackList.next();
                    cmp = comp.compare(white, black);
                    
                    if (cmp < 0) return;
                }
            }
        }
        
        @Override
        public boolean hasNext()
        {
            return !finished;
        }

        @Override
        public T next()
        {
            if (finished) throw new NoSuchElementException();
            T toReturn = white;
            fill();
            return toReturn;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("SortedDiffIterator.remove");
        }
        
        @Override
        public void close()
        {
            finished = true;
            white = null;
            black = null;
            Iter.close(grayList);
            Iter.close(blackList);
        }
    }

}

