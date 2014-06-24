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

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.util.Context ;

public class ThresholdPolicyFactory
{
    private static final long defaultThreshold = -1 ; // Use the never() policy by default

    private static final ThresholdPolicy<?> NEVER = new ThresholdPolicy<Object>()
    {
        @Override
        public void increment(Object item)
        {
            // Do nothing
        }

        @Override
        public boolean isThresholdExceeded()
        {
            return false ;
        }

        @Override
        public void reset()
        {
            // Do nothing
        }
    } ;

    /**
     * A threshold policy that is never exceeded.
     */
    public static final <E> ThresholdPolicy<E> never()
    {
        @SuppressWarnings("unchecked")
        ThresholdPolicy<E> policy = (ThresholdPolicy<E>) NEVER ;
        return policy ;
    }
    
    /**
     * A threshold policy based on the number of tuples added.
     */
    public static <E> ThresholdPolicy<E> count(long threshold)
    {
        return new ThresholdPolicyCount<>(threshold) ;
    }

    /**
     * A threshold policy based on the {@link com.hp.hpl.jena.query.ARQ#spillToDiskThreshold} symbol in the given Context.
     * If the symbol is not set, then the {@link #never()} policy is used by default.
     */
    public static <E> ThresholdPolicy<E> policyFromContext(Context context)
    {
        long threshold = (Long) context.get(ARQ.spillToDiskThreshold, defaultThreshold) ;
        if ( threshold >= 0 )
        {
            return count(threshold);
        }
        else
        {
            return never() ;
        }
    }
}
