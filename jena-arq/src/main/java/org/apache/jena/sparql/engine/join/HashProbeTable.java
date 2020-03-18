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

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.ext.com.google.common.collect.ArrayListMultimap;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.sparql.engine.binding.Binding;

/** The probe table for a hash join */
class HashProbeTable {
    /*package*/ long s_count           = 0;
    /*package*/ long s_bucketCount     = 0;
    /*package*/ long s_maxBucketSize   = 0;
    /*package*/ long s_noKeyBucketSize = 0;
    /*package*/ long s_maxMatchGroup   = 0;
    /*package*/ long s_countScanMiss   = 0;

    private final List<Binding>             noKeyBucket = new ArrayList<>();
    private final Multimap<Object, Binding> buckets;
    private final JoinKey                   joinKey;

    HashProbeTable(JoinKey joinKey) {
        this.joinKey = joinKey;
        buckets = ArrayListMultimap.create();
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

    public Iterator<Binding> getCandidates(Binding row) {
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
        if ( noKeyBucket != null )
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

    // Should not need these operations.
    public Collection<Binding> getNoKey$() {
        if ( noKeyBucket == null )
            return null;
        return noKeyBucket;
    }

    public Collection<Binding> getHashMatch$(Binding row) {
        Object longHash = JoinLib.hash(joinKey, row);
        if ( longHash == JoinLib.noKeyHash )
            return noKeyBucket;
        Collection<Binding> list = buckets.get(longHash);
        return list;
    }

    public Iterator<Binding> values() {
        return Iter.concat(buckets.values().iterator(),
                           noKeyBucket.iterator()) ;
    }
    
    public void clear() {
        buckets.clear();
    }
}
