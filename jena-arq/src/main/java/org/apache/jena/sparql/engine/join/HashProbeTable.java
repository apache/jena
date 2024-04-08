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

package org.apache.jena.sparql.engine.join;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.sparql.engine.binding.Binding;

/** The probe table for a hash join */
class HashProbeTable {
    /*package*/ long s_count           = 0;
    /*package*/ long s_bucketCount     = 0;
    /*package*/ long s_maxBucketSize   = 0;
    /*package*/ long s_noKeyBucketSize = 0;
    /*package*/ long s_maxMatchGroup   = 0;
    /*package*/ long s_countScanMiss   = 0;

    private final List<Binding>                   noKeyBucket = new ArrayList<>();
    private final MultiValuedMap<Object, Binding> buckets;
    private final JoinKey                         joinKey;

    HashProbeTable(JoinKey joinKey) {
        this.joinKey = joinKey;
        buckets = MultiMapUtils.newListValuedHashMap();
    }

    public JoinKey getJoinKey() {
        return joinKey;
    }

    public void putNoKey(Binding row) {
        s_count++;
        noKeyBucket.add(row);
    }

    public void put(Binding row) {
        s_count++;
        Object longHash = JoinLib.hash(joinKey, row);
        if ( longHash == JoinLib.noKeyHash ) {
            noKeyBucket.add(row);
            return;
        }
        buckets.put(longHash, row);
    }

    /** Shorthand for {@code getCandidates(row, true)}. See {@link #getCandidates(Binding, boolean)}. */
    public Iterator<Binding> getCandidates(Binding row) {
        return getCandidates(row, true);
    }

    /**
     * Find the rows of this table that are compatible with a given lookup binding.
     *
     * @param row The lookup binding.
     * @param appendNoBucket If true then also append the data from the no-key-bucket (if present).
     * @return An iterator over the rows in this table that are compatible with the lookup binding.
     */
    public Iterator<Binding> getCandidates(Binding row, boolean appendNoBucket) {
        Iterator<Binding> iter = null;
        Object longHash = JoinLib.hash(joinKey, row);
        if ( longHash == JoinLib.noKeyHash )
            iter = buckets.values().iterator();
        else {
            Collection<Binding> x = buckets.get(longHash);
            if ( x != null ) {
                s_maxMatchGroup = Math.max(s_maxMatchGroup, x.size());
                iter = x.iterator();
            } else {
                s_countScanMiss ++ ;
            }
        }
        // And the rows with no common hash key
        if ( appendNoBucket && noKeyBucket != null )
            iter = Iter.concat(iter, noKeyBucket.iterator());
        return iter;
    }

    public void stats() {
        long max = 0;
        for ( Object key : buckets.keys() ) {
            long s = buckets.get(key).size();
            max = Math.max(max, s);
        }
        s_maxBucketSize = max;
        s_bucketCount = buckets.keys().size();
        s_noKeyBucketSize = (noKeyBucket == null) ? 0 : noKeyBucket.size();
        // s_count
        // s_maxMatchGroup
        // What to do with them?
    }

    public Collection<Binding> getNoKey() {
        return noKeyBucket;
    }

    public Iterator<Binding> values() {
        return Iter.concat(buckets.values().iterator(),
                           noKeyBucket.iterator()) ;
    }

    public void clear() {
        buckets.clear();
    }

    @Override
    public String toString() {
        stats();
        long hashedItems = s_count - s_noKeyBucketSize;
        String str = String.format("JoinKey=%s Items=%d HashedItems=%d NoKeyItems=%d Buckets=%d RightMisses=%d MaxBucket=%d",
                joinKey, s_count, hashedItems, s_noKeyBucketSize, s_bucketCount, s_countScanMiss, s_maxBucketSize);
        return str;
    }
}
