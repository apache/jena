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

import java.util.Comparator;


/**
 * Factory for constructing different types of {@link DataBag} instances.
 */
public class BagFactory
{
    /**
     * Get a default (unordered, not distinct) data bag.
     */
    public static <T> DefaultDataBag<T> newDefaultBag(ThresholdPolicy<T> policy, SerializationFactory<T> serializerFactory)
    {
        return new DefaultDataBag<>(policy, serializerFactory);
    }

    /**
     * Get a sorted data bag.
     */
    public static <T extends Comparable<? super T>> SortedDataBag<T> newSortedBag(ThresholdPolicy<T> policy, SerializationFactory<T> serializerFactory)
    {
        return newSortedBag(policy, serializerFactory, null);
    }
    
    /**
     * Get a sorted data bag.
     */
    public static <T> SortedDataBag<T> newSortedBag(ThresholdPolicy<T> policy, SerializationFactory<T> serializerFactory, Comparator<T> comparator)
    {
        return new SortedDataBag<>(policy, serializerFactory, comparator);
    }
    
    /**
     * Get a distinct data bag.
     */
    public static <T extends Comparable<? super T>> DistinctDataBag<T> newDistinctBag(ThresholdPolicy<T> policy, SerializationFactory<T> serializerFactory)
    {
        return newDistinctBag(policy, serializerFactory, null);
    }

    /**
     * Get a distinct data bag.
     */
    public static <T> DistinctDataBag<T> newDistinctBag(ThresholdPolicy<T> policy, SerializationFactory<T> serializerFactory, Comparator<T> comparator)
    {
        return new DistinctDataBag<>(policy, serializerFactory, comparator);
    }
    
    /**
     * Get a distinct data net.
     */
    public static <T extends Comparable<? super T>> DistinctDataNet<T> newDistinctNet(ThresholdPolicy<T> policy, SerializationFactory<T> serializerFactory)
    {
        return newDistinctNet(policy, serializerFactory, null);
    }

    /**
     * Get a distinct data net.
     */
    public static <T> DistinctDataNet<T> newDistinctNet(ThresholdPolicy<T> policy, SerializationFactory<T> serializerFactory, Comparator<T> comparator)
    {
        return new DistinctDataNet<>(policy, serializerFactory, comparator);
    }
}
