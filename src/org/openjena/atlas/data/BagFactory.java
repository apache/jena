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

package org.openjena.atlas.data;

import java.util.Comparator;


/**
 * Factory for constructing different types of {@link DataBag} instances.
 */
public class BagFactory
{
    // TODO Read these thresholds from a config file
    private static long spillCountThreshold = 50000;
    //public static final Symbol spillCountThresholdSymbol = ARQConstants.allocSymbol("spillCountThreshold") ;
    
    private static long spillMemoryThreshold = 20000000;
    
    
    
    public static long getSpillCountThreshold()
    {
        return spillCountThreshold;
    }
    
    /**
     * Used for testing.
     */
    public static void setSpillCountThreshold(long value)
    {
        if (value < 0)
        {
            throw new IllegalArgumentException("Threshold must be greater than or equal to zero");
        }
        spillCountThreshold = value;
    }
    
    private static <T> ThresholdPolicy<T> newCountPolicy()
    {
        //long threshold = ((Long)context.getContext().get(spillCountThresholdSymbol, spillCountThreshold)).longValue();
        long threshold = spillCountThreshold;
        
        return new ThresholdPolicyCount<T>(threshold);
    }
    
    private static <T> ThresholdPolicy<T> newMemoryPolicy(SerializationFactory<T> serializerFactory)
    {
        long threshold = spillMemoryThreshold;
        return new ThresholdPolicyMemory<T>(threshold, serializerFactory);
    }
    
    private static <T> ThresholdPolicy<T> newDefaultPolicy()
    {
        return newCountPolicy();
    }
    
    /**
     * Get a default (unordered, not distinct) data bag.
     */
    public static <T> DefaultDataBag<T> newDefaultBag(SerializationFactory<T> serializerFactory)
    {
        ThresholdPolicy<T> policy = newDefaultPolicy();
        return new DefaultDataBag<T>(policy, serializerFactory);
    }

    /**
     * Get a sorted data bag.
     */
    public static <T extends Comparable<? super T>> SortedDataBag<T> newSortedBag(SerializationFactory<T> serializerFactory)
    {
        return newSortedBag(serializerFactory, null);
    }
    
    /**
     * Get a sorted data bag.
     */
    public static <T> SortedDataBag<T> newSortedBag(SerializationFactory<T> serializerFactory, Comparator<T> comparator)
    {
        ThresholdPolicy<T> policy = newDefaultPolicy();
        return newSortedBag(policy, serializerFactory, comparator);
    }
    
    /**
     * Get a sorted data bag.
     */
    public static <T> SortedDataBag<T> newSortedBag(ThresholdPolicy<T> policy, SerializationFactory<T> serializerFactory, Comparator<T> comparator)
    {
        return new SortedDataBag<T>(policy, serializerFactory, comparator);
    }
    
    /**
     * Get a distinct data bag.
     */
    public static <T extends Comparable<? super T>> DistinctDataBag<T> newDistinctBag(SerializationFactory<T> serializerFactory)
    {
        return newDistinctBag(serializerFactory, null);
    }
    
    /**
     * Get a distinct data bag.
     */
    public static <T> DistinctDataBag<T> newDistinctBag(SerializationFactory<T> serializerFactory, Comparator<T> comparator)
    {
        ThresholdPolicy<T> policy = newDefaultPolicy();
        return newDistinctBag(policy, serializerFactory, comparator);
    }

    /**
     * Get a distinct data bag.
     */
    public static <T> DistinctDataBag<T> newDistinctBag(ThresholdPolicy<T> policy, SerializationFactory<T> serializerFactory, Comparator<T> comparator)
    {
        return new DistinctDataBag<T>(policy, serializerFactory, comparator);
    }

}
