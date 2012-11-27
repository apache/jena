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

import java.util.Iterator;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sink ;

/**
 * A collection of Tuples.  A DataBag may or may not fit into memory.
 * It proactively spills to disk when its size exceeds the threshold.
 * When it spills, it takes whatever it has in memory, opens a spill file,
 * and writes the contents out.  This may happen multiple times.  The bag
 * tracks all of the files it's spilled to.
 * <p>
 * DataBag provides an Iterator interface, that allows callers to read
 * through the contents.  The iterators are aware of the data spilling.
 * They have to be able to handle reading from files.
 * <p>
 * The DataBag interface assumes that all data is written before any is
 * read.  That is, a DataBag cannot be used as a queue.  If data is written
 * after data is read, the results are undefined.  This condition is not
 * checked on each add or read, for reasons of speed.  Caveat emptor.
 * <p>
 * DataBags come in several types, default, sorted, and distinct.  The type
 * must be chosen up front, there is no way to convert a bag on the fly.
 * Default data bags do not guarantee any particular order of retrieval for 
 * the tuples and may contain duplicate tuples.  Sorted data bags guarantee
 * that tuples will be retrieved in order, where "in order" is defined either
 * by the default comparator for Tuple or the comparator provided by the
 * caller when the bag was created.  Sorted bags may contain duplicates.
 * Distinct bags do not guarantee any particular order of retrieval, but do
 * guarantee that they will not contain duplicate tuples.
 * <p>
 * Inspired by Apache Pig
 * @see <a href="http://svn.apache.org/repos/asf/pig/tags/release-0.9.0/src/org/apache/pig/data/DataBag.java">DataBag from Apache Pig</a>
 */
public interface DataBag<T> extends Sink<T>, Iterable<T>, Closeable
{
    /**
     * Get the number of elements in the bag, both in memory and on disk.
     * @return number of elements in the bag
     */
    long size();
    
    /**
     * Find out if the bag is sorted.
     * @return true if this is a sorted data bag, false otherwise.
     */
    boolean isSorted();
    
    /**
     * Find out if the bag is distinct.
     * @return true if the bag is a distinct bag, false otherwise.
     */
    boolean isDistinct();
    
    /**
     * Add a tuple to the bag.
     * @param t tuple to add.
     */
    void add(T t);

    /**
     * Add contents of an Iterable to the bag.
     * @param it iterable to add contents of.
     */
    void addAll(Iterable<? extends T> it);
    
    /**
     * Add contents of an Iterator to the bag.
     * @param it iterator to add contents of.
     */
    void addAll(Iterator<? extends T> it);
}
