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

package org.apache.jena.atlas.data ;

/**
 * A threshold policy based on the estimated memory size of the tuples added.
 * <p>
 * TODO This policy isn't ready to be used because the SerializationFactorys
 * don't support generating memory estimates yet.
 * <p>
 * TODO It might be too expensive to examine each tuple individually. We could
 * change it to sample the first 100 tuples to calculate an average tuple size.
 */
public class ThresholdPolicyMemory<T> implements ThresholdPolicy<T> {
    protected final SerializationFactory<T> serializerFactory ;
    protected final long                    threshold ;
    protected long                          count ;
    protected long                          size ;

    public ThresholdPolicyMemory(long threshold, SerializationFactory<T> serializerFactory) {
        if ( threshold < 0 ) { throw new IllegalArgumentException("Threshold must be greater than or equal to zero") ; }
        this.threshold = threshold ;
        this.serializerFactory = serializerFactory ;
        reset() ;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ThresholdPolicy#increment(java.lang.Object)
     */
    @Override
    public void increment(T item) {
        count++ ;
        size += serializerFactory.getEstimatedMemorySize(item) ;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ThresholdPolicy#isThresholdExceeded()
     */
    @Override
    public boolean isThresholdExceeded() {
        return (size >= threshold) ;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openjena.atlas.data.ThresholdPolicy#reset()
     */
    @Override
    public void reset() {
        count = 0 ;
        size = 0 ;
    }

    /**
     * Returns the threshold before the list is written to disk.
     * 
     * @return The threshold point.
     */
    public long getThreshold() {
        return threshold ;
    }

    /**
     * Returns the current count of the number of items incremented in this
     * policy.
     * 
     * @return The item count.
     */
    public long getCount() {
        return count ;
    }

    /**
     * Returns the current total estimated memory size of all the items
     * incremented in this policy.
     * 
     * @return The item count.
     */
    public long getMemorySize() {
        return size ;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ThresholdPolicyMemory:  Threshold (bytes)=" + threshold + "  Memory Size (bytes)=" + size + "  Count="
               + count ;
    }
}
