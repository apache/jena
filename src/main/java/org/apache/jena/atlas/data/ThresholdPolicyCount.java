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

/**
 * A threshold policy based on the number of tuples added.
 */
public class ThresholdPolicyCount<T> implements ThresholdPolicy<T>
{
    protected final long threshold;
    protected long count;
    
    public ThresholdPolicyCount(long threshold)
    {
        if (threshold < 0)
        {
            throw new IllegalArgumentException("Threshold must be greater than or equal to zero");
        }
        this.threshold = threshold;
        reset();
    }

    /* (non-Javadoc)
     * @see org.openjena.atlas.io.ThresholdPolicy#increment(java.lang.Object)
     */
    @Override
    public void increment(T item)
    {
        count++;
    }

    /* (non-Javadoc)
     * @see org.openjena.atlas.io.ThresholdPolicy#isThresholdExceeded()
     */
    @Override
    public boolean isThresholdExceeded()
    {
        return (count >= threshold);
    }

    /*
     * (non-Javadoc)
     * @see org.openjena.atlas.data.ThresholdPolicy#reset()
     */
    @Override
    public void reset()
    {
        count = 0;
    }

    /**
     * Returns the threshold before the list is written to disk.
     * @return The threshold point.
     */
    public long getThreshold()
    {
        return threshold;
    }
    
    /**
     * Returns the current count of the number of items incremented in this policy.
     * @return The item count.
     */
    public long getCount()
    {
        return count;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ThresholdPolicyCount:  Threshold=" + threshold + "  Count=" + count;
    }
}
